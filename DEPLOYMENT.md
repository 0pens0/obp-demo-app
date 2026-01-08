# Deployment Guide

## Combined Manifest Deployment

This project includes a combined `manifest.yml` at the root that deploys both applications in a single `cf push` command.

### Prerequisites

1. Build both applications:
```bash
# Build obp-demo-banking
cd obp-demo-banking
mvn clean package -DskipTests
cd ..

# Build mcp-server
cd open-banking-mcp/mcp-server
mvn clean package -DskipTests
cd ../../..
```

2. Set OBP API Key as space variable:
```bash
cf set-space-variable <space-name> OBP_API_KEY "your-obp-api-key-here"
```

3. Create required services:
```bash
cf create-service postgresql standard postgres-vector-db
cf create-service <llm-service-name> <plan> llm-service
```

### Deploy Both Applications

From the root directory:

```bash
cf push -f manifest.yml
```

This will deploy:
- `obp-demo-banking` on port 8080 (or configured route)
- `obp-mcp-server` on port 8081 (or configured route)

### Individual Application Deployment

If you prefer to deploy applications separately:

```bash
# Deploy banking app
cd obp-demo-banking
cf push

# Deploy MCP server
cd ../open-banking-mcp/mcp-server
cf push
```

### Manifest Configuration

The combined manifest includes:
- Both applications with appropriate memory allocation
- Shared OBP_API_KEY environment variable
- Service bindings for PostgreSQL and LLM services
- Health check endpoints
- Route configuration

Update the routes in `manifest.yml` to match your Cloud Foundry domain.
