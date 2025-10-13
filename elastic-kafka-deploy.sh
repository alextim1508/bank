#!/bin/bash

set -e

RELEASE_NAME=${1:-elastic-kafka}
NAMESPACE=${2:-default}

echo "Deploying $RELEASE_NAME to namespace $NAMESPACE"

# Add required Helm repositories
echo "Adding Helm repositories..."
helm repo add elastic https://helm.elastic.co 2>dev/null || true
helm repo add bitnami https://charts.bitnami.com/bitnami 2>dev/null || true
helm repo update

# Create the namespace if it doesn't exist
echo "Creating namespace $NAMESPACE if it doesn't exist..."
kubectl create namespace $NAMESPACE --dry-run=client -o yaml | kubectl apply -f -

# Deploy the chart
echo "Deploying the chart..."
helm upgrade --install $RELEASE_NAME ./elastic-kafka-chart \
  --namespace $NAMESPACE \
  --values ./elastic-kafka-chart/values.yaml

echo "Deployment completed!"
echo "You can check the status with: helm status $RELEASE_NAME -n $NAMESPACE"