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