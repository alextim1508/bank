# Using the Elastic-Kafka Chart

## Prerequisites

Before deploying this chart, you need to add the required Helm repositories:

```bash
helm repo add elastic https://helm.elastic.co
helm repo add bitnami oci://registry-1.docker.io/bitnamicharts
helm repo update
```

## Installation

### Simple Installation

To install the chart with default values:

```bash
helm install elastic-kafka ./elastic-kafka-chart
```

### Custom Installation

To install with custom values:

```bash
helm install elastic-kafka ./elastic-kafka-chart -f values-production.yaml
```

### Installation in a Specific Namespace

```bash
helm install elastic-kafka ./elastic-kafka-chart --namespace logging --create-namespace
```

## Configuration

You can customize the deployment by modifying the [values.yaml](values.yaml) file or by providing your own values file.

### Enabling/Disabling Components

You can enable or disable specific components by setting the corresponding `enabled` flag:

```yaml
elasticsearch:
  enabled: true

kibana:
  enabled: true

logstash:
  enabled: false  # This will disable Logstash deployment

kafka:
  enabled: true
```

### Resource Configuration

You can adjust resource allocations for each component:

```yaml
elasticsearch:
  resources:
    limits:
      cpu: 2000m
      memory: 4Gi
    requests:
      cpu: 1000m
      memory: 2Gi
```

## Upgrading

To upgrade the chart:

```bash
helm upgrade elastic-kafka ./elastic-kafka-chart
```

If any of the services were turned off (enabled: false) and then turned on,
upgrade the chart:

```bash
helm upgrade --install elastic-kafka ./elastic-kafka-chart --values ./elastic-kafka-chart/values.yaml
```

## Uninstalling

To uninstall the chart:

```bash
helm uninstall elastic-kafka --cascade foreground --debug --ignore-not-found
```

## Accessing the Services

After deployment, you can access the services using kubectl port-forward:

### Elasticsearch
```bash
kubectl port-forward svc/elastic-kafka-elasticsearch 9200:9200
```

### Kibana
```bash
kubectl port-forward svc/elastic-kafka-kibana 5601:5601
```

### Kafka
```bash
kubectl port-forward svc/elastic-kafka-kafka 9092:9092
```

Then access the services at:
- Elasticsearch: http://localhost:9200
- Kibana: http://localhost:5601