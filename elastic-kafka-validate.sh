#!/bin/bash

RELEASE_NAME=${1:-elastic-kafka}
NAMESPACE=${2:-default}

echo "Validating $RELEASE_NAME deployment in namespace $NAMESPACE"

# Check Helm release status
echo "Checking Helm release status..."
helm status $RELEASE_NAME -n $NAMESPACE

# Check Kubernetes pods
echo "Checking pod status..."
kubectl get pods -n $NAMESPACE | grep $RELEASE_NAME

# Check Kubernetes services
echo "Checking service status..."
kubectl get svc -n $NAMESPACE | grep $RELEASE_NAME

echo "Validation completed!"