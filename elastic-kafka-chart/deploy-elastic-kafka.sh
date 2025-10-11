#!/bin/bash
set -ex

echo "Cleaning up any existing deployments..."
helm uninstall --debug elastic-kafka 2>/dev/null || true

# Clean up any existing jobs before we start
kubectl delete job --all 2>/dev/null || true
kubectl delete serviceaccounts --all 2>/dev/null || true
kubectl delete roles.rbac.authorization.k8s.io --all 2>/dev/null || true
kubectl delete rolebindings.rbac.authorization.k8s.io --all 2>/dev/null || true

kubectl delete pvc --all 2>/dev/null || true
kubectl delete pods --all 2>/dev/null || true

kubectl delete PodDisruptionBudget elasticsearch-master-pdb 2>/dev/null || true
kubectl delete statefulset elasticsearch-master 2>/dev/null || true
kubectl delete service elasticsearch-master 2>/dev/null || true
kubectl delete service elasticsearch-master-headless 2>/dev/null || true

kubectl delete configmap elastic-kafka-kibana-helm-scripts 2>/dev/null || true
kubectl delete serviceaccounts pre-install-elastic-kafka-kibana 2>/dev/null || true

# Delete secrets with more specific selectors and force removal
kubectl delete secret --field-selector=metadata.name=elasticsearch-master-certs 2>/dev/null || true
kubectl delete secret elasticsearch-master-certs 2>/dev/null || true
kubectl delete secret elasticsearch-master-credentials 2>/dev/null || true
kubectl delete secret kibana-kibana-es-cert 2>/dev/null || true

kubectl delete rolebindings.rbac.authorization.k8s.io pre-install-elastic-kafka-kibana 2>/dev/null || true
kubectl delete roles.rbac.authorization.k8s.io pre-install-elastic-kafka-kibana 2>/dev/null || true

# Clean up any remaining Kibana jobs specifically
kubectl delete job -l app=kibana 2>/dev/null || true

echo "Waiting for resources to be fully deleted..."
sleep 15

echo "Adding required Helm repositories..."
helm repo add elastic https://helm.elastic.co
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update

# Apply the elasticsearch secret with proper Helm ownership
kubectl apply -f elasticsearch-secret.yaml

# Add a check to ensure the secret has the correct labels and annotations
echo "Checking and updating secret ownership metadata..."
kubectl label secret elasticsearch-master-credentials app.kubernetes.io/managed-by=Helm --overwrite 2>/dev/null || true
kubectl annotate secret elasticsearch-master-credentials meta.helm.sh/release-name=elastic-kafka --overwrite 2>/dev/null || true
kubectl annotate secret elasticsearch-master-credentials meta.helm.sh/release-namespace=default --overwrite 2>/dev/null || true

echo "Deploying Elastic-Kafka stack..."
if ! helm upgrade --install elastic-kafka . -f values.yaml --timeout 2m0s --debug --no-hooks; then
    echo "Deployment failed. Checking for existing resources..."
    kubectl get pods | grep elastic-kafka
    echo "Checking Kibana pods specifically:"
    kubectl get pods -l app=kibana
    exit 1
fi

echo "Waiting for pods to be ready..."
if ! kubectl wait --for=condition=ready pod -l app=elasticsearch-master --timeout=300s; then
    echo "Elasticsearch pod failed to start. Checking logs..."
    kubectl logs -l app=elasticsearch-master
    exit 1
fi

if ! kubectl wait --for=condition=ready pod -l app=kafka --timeout=300s; then
    echo "Kafka pod failed to start. Checking logs..."
    kubectl logs -l app=kafka
    exit 1
fi

if ! kubectl wait --for=condition=ready pod -l app=logstash --timeout=300s; then
    echo "Logstash pod failed to start. Checking logs..."
    kubectl logs -l app=logstash
    exit 1
fi

if ! kubectl wait --for=condition=ready pod -l app=kibana --timeout=300s; then
    echo "Kibana pod failed to start. Checking logs..."
    kubectl logs -l app=kibana
    echo "Describing Kibana pod for more details:"
    kubectl describe pod -l app=kibana
    exit 1
fi

echo "Deployment completed successfully!"
echo "You can access Kibana on NodePort 32000"
echo "To access Kibana locally, run: kubectl port-forward svc/elastic-kafka-kibana 5601:5601"