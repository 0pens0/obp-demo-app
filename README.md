# OBP Demo Application with MCP Server

A comprehensive demo application integrating with the Open Bank Project (OBP) sandbox, featuring a mock bank frontend with admin and customer sections, an AI-powered chatbot with RAG capabilities, and an MCP server exposing OBP account API tools.

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Project Structure](#project-structure)
- [Local Development](#local-development)
- [Tanzu Platform Deployment](#tanzu-platform-deployment)
- [Usage Guide](#usage-guide)
- [MCP Server](#mcp-server)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [License](#license)

## Overview

This demo application showcases a complete banking application integration with:

1. **obp-demo-banking**: A Spring Boot web application with:
   - Admin dashboard for creating customer users in OBP sandbox
   - Customer dashboard for viewing account information
   - AI-powered chatbot with RAG (Retrieval Augmented Generation) capabilities
   - PostgreSQL vector store for chat history and context

2. **MCP Server**: A standalone Model Context Protocol server that exposes OBP API tools:
   - `getAccounts`: Retrieve user accounts
   - `getTransactions`: Fetch transaction history
   - `getAccountBalance`: Get account balance
   - `getCounterparties`: List account counterparties

## Architecture

```
┌─────────────────────────────────┐
│   obp-demo-banking (Port 8080)  │
│   - Admin Dashboard              │
│   - Customer Dashboard           │
│   - AI Chatbot (with RAG)        │
└─────────────────────────────────┘
              │
              │ Uses OBP API
              ▼
┌─────────────────────────────────┐
│   OBP Sandbox API               │
│   apisandbox.openbankproject.com │
└─────────────────────────────────┘
              ▲
              │ MCP Tools expose
              │
┌─────────────────────────────────┐
│   MCP Server (Port 8081)        │
│   - getAccounts tool            │
│   - getTransactions tool        │
│   - getAccountBalance tool      │
│   - getCounterparties tool      │
└─────────────────────────────────┘

┌─────────────────────────────────┐
│   PostgreSQL (with pgvector)   │
│   - Vector embeddings storage   │
│   - Similarity search           │
│   - Chat history & context      │
└─────────────────────────────────┘
              ▲
              │ Service Binding
              │
┌─────────────────────────────────┐
│   Tanzu Marketplace Services     │
│   - LLM Service (OpenAI/Azure)  │
│   - PostgreSQL Service          │
└─────────────────────────────────┘
```

## Features

### obp-demo-banking Application

- **Admin Section**
  - Create and manage customer users in OBP sandbox
  - View created users with their OBP user IDs
  - Simple authentication (admin/admin)

- **Customer Dashboard**
  - View account details (label, number, IBAN, type)
  - Display account balances with currency
  - View recent transactions (last 10)
  - List account counterparties
  - Account summary with total balance

- **AI Chatbot with RAG**
  - Interactive chatbot powered by Spring AI
  - Retrieval Augmented Generation (RAG) using PostgreSQL vector store
  - Context-aware responses based on customer banking data
  - Chat history stored in vector database for improved responses
  - Similarity search for relevant context retrieval

### MCP Server

- **MCP Protocol Implementation**
  - JSON-RPC 2.0 compliant
  - RESTful API endpoints
  - Tool registration and discovery

- **Available Tools**
  - `getAccounts`: Fetch all accounts for authenticated user
  - `getTransactions`: Retrieve transaction history for an account
  - `getAccountBalance`: Get account balance with currency
  - `getCounterparties`: List counterparties for an account

## Tech Stack

### Backend
- **Spring Boot**: 3.3.5
- **Spring AI**: 1.0.1
- **Spring Data JPA**: For database operations
- **PostgreSQL**: With pgvector extension for vector storage
- **Flyway**: Database migrations
- **Spring WebFlux**: Reactive HTTP client for OBP API
- **Spring Security**: Authentication and authorization
- **Thymeleaf**: Server-side templating

### Frontend
- **Thymeleaf Templates**: Server-side rendering
- **Bootstrap CSS**: Modern UI styling
- **JavaScript**: Interactive chatbot widget

### Infrastructure
- **Tanzu Application Platform**: Cloud Foundry deployment
- **Spring Cloud Bindings**: Automatic service binding
- **PostgreSQL Service**: From Tanzu marketplace
- **LLM Service**: From Tanzu marketplace (OpenAI/Azure OpenAI)

## Prerequisites

### For Local Development
- Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+ with pgvector extension
- OBP Sandbox account and API key
- (Optional) OpenAI API key for local AI testing

### For Tanzu Deployment
- Access to Tanzu Application Platform
- Cloud Foundry CLI (`cf`) installed and configured
- Tanzu CLI (`tanzu`) installed (optional, for TAP)
- OBP Sandbox API key
- Access to Tanzu marketplace services:
  - PostgreSQL service (with pgvector support)
  - LLM service (OpenAI or Azure OpenAI)

## Project Structure

```
obp-demo-app/
├── .gitignore
├── README.md (this file)
├── DEMO_FLOW.md (deployment and usage guide)
├── obp-demo-banking/
│   ├── pom.xml
│   ├── manifest.yml (Cloud Foundry deployment)
│   ├── README.md
│   ├── DEMO_FLOW.md
│   └── src/
│       ├── main/
│       │   ├── java/com/obp/demo/
│       │   │   ├── config/ (Configuration classes)
│       │   │   ├── controller/ (MVC controllers)
│       │   │   ├── service/ (Business logic)
│       │   │   ├── model/ (Domain models)
│       │   │   ├── dto/ (Data transfer objects)
│       │   │   └── exception/ (Exception classes)
│       │   └── resources/
│       │       ├── application.yml
│       │       ├── application-cloud.yml
│       │       ├── db/migration/ (Flyway migrations)
│       │       ├── templates/ (Thymeleaf templates)
│       │       └── static/ (CSS, JS)
│       └── test/
└── open-banking-mcp/
    └── mcp-server/
        ├── pom.xml
        ├── manifest.yml (Cloud Foundry deployment)
        ├── README.md
        └── src/
            ├── main/
            │   ├── java/com/openbanking/mcpserver/
            │   │   ├── config/ (Configuration)
            │   │   ├── controller/ (REST controllers)
            │   │   ├── service/ (OBP API service)
            │   │   ├── model/ (Data models)
            │   │   ├── protocol/ (MCP protocol)
            │   │   ├── tools/ (MCP tools)
            │   │   └── exception/ (Exception handling)
            │   └── resources/
            │       ├── application.yml
            │       └── application-cloud.yml
            └── test/
```

## Local Development

### 1. Clone and Setup

```bash
cd ~/git/obp-demo-app
```

### 2. Configure OBP API Key

Set environment variable:
```bash
export OBP_API_KEY=your-api-key-here
```

Or update `obp-demo-banking/src/main/resources/application.yml`:
```yaml
obp:
  api:
    api-key: your-api-key-here
```

### 3. Setup PostgreSQL (Local)

Install PostgreSQL and pgvector extension:
```bash
# On macOS
brew install postgresql
brew install pgvector

# Create database
createdb obp_demo

# Connect and enable extension
psql obp_demo
CREATE EXTENSION vector;
```

Update `application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/obp_demo
    username: your-username
    password: your-password
```

### 4. Configure AI (Optional for Local)

For local testing without Tanzu service binding:
```bash
export OPENAI_API_KEY=your-openai-api-key
```

### 5. Run obp-demo-banking

```bash
cd obp-demo-banking
mvn clean install
mvn spring-boot:run
```

Access at: http://localhost:8080

### 6. Run MCP Server

```bash
cd ../open-banking-mcp/mcp-server
mvn clean install
mvn spring-boot:run
```

Runs on: http://localhost:8081

## Tanzu Platform Deployment

See [DEMO_FLOW.md](DEMO_FLOW.md) for detailed deployment instructions.

### Quick Start

1. **Set OBP API Key Variable** (Required before deployment):
   ```bash
   # Set as space variable (recommended)
   cf set-space-variable <space-name> OBP_API_KEY "your-obp-api-key-here"
   ```

2. **Create Services**:
   ```bash
   cf create-service postgresql standard postgres-vector-db
   cf create-service <llm-service-name> <plan> llm-service
   ```

3. **Build Applications**:
   ```bash
   cd obp-demo-banking && mvn clean package -DskipTests
   cd ../open-banking-mcp/mcp-server && mvn clean package -DskipTests
   ```

4. **Deploy**:
   ```bash
   cd obp-demo-banking && cf push
   cd ../open-banking-mcp/mcp-server && cf push
   ```
   
   **Note**: The manifest files use `((OBP_API_KEY))` which will automatically resolve from space/org variables during deployment.

## Usage Guide

### Admin Section

1. Navigate to: `http://your-app-url/admin/login`
2. Login with: `admin` / `admin`
3. Create a new customer:
   - Fill in username, email, password, first name, last name
   - Click "Create Customer"
   - Note the OBP User ID assigned

### Customer Section

1. Navigate to: `http://your-app-url/customer/login`
2. Login with credentials created in admin section
3. View dashboard:
   - Account information
   - Recent transactions
   - Counterparties
   - Account balance
4. Use AI Chatbot:
   - Click chat icon (💬) in bottom-right
   - Ask questions like:
     - "What is my account balance?"
     - "Show me my recent transactions"
     - "Who are my counterparties?"

## MCP Server

### API Endpoints

- `POST /mcp/call` - Execute MCP tool calls
- `GET /actuator/health` - Health check

### Example Usage

**List Available Tools**:
```bash
curl -X POST http://localhost:8081/mcp/call \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "tools/list",
    "id": "1"
  }'
```

**Call getAccounts Tool**:
```bash
curl -X POST http://localhost:8081/mcp/call \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "tools/call",
    "id": "2",
    "params": {
      "name": "getAccounts",
      "arguments": {
        "token": "your-obp-token"
      }
    }
  }'
```

## Troubleshooting

### Application Won't Start

- Verify Java 17+ is installed: `java -version`
- Check Maven is installed: `mvn -version`
- Review application logs for specific errors

### Database Connection Issues

- Verify PostgreSQL is running
- Check database credentials in `application.yml`
- Ensure pgvector extension is installed: `CREATE EXTENSION vector;`

### OBP API Errors

- Verify OBP API key is correct
- Check internet connectivity to `apisandbox.openbankproject.com`
- Review OBP API documentation for endpoint changes

### Chatbot Not Responding

- For local: Verify `OPENAI_API_KEY` is set
- For Tanzu: Check LLM service binding is configured
- Review application logs for AI service connection errors

### Service Binding Issues (Tanzu)

- Verify services are created: `cf services`
- Check service bindings: `cf service postgres-vector-db`
- Review application logs for binding errors
- Ensure Spring Cloud Bindings dependency is included

## Contributing

This is a demo application for educational purposes. Contributions and improvements are welcome!

## License

This is a demo application for educational purposes.

## References

- [Open Bank Project](https://www.openbankproject.com/)
- [OBP Sandbox](https://apisandbox.openbankproject.com/)
- [OBP API Documentation](https://www.openbankproject.com/developers/)
- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [Tanzu Application Platform](https://tanzu.vmware.com/application-platform)
- [Cloud Foundry Documentation](https://docs.cloudfoundry.org/)
