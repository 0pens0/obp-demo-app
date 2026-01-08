# OBP MCP Server

A Model Context Protocol (MCP) server that exposes Open Bank Project (OBP) account API tools for fetching banking data.

## Overview

This MCP server provides tools to interact with the OBP sandbox API, allowing clients to:
- Retrieve user accounts
- Fetch transaction history
- Get account balances
- List account counterparties

## Features

### Available Tools

1. **getAccounts**
   - Retrieves all accounts for an authenticated user
   - Parameters: `token` (OBP authentication token)
   - Returns: List of accounts

2. **getTransactions**
   - Retrieves transactions for a specific account
   - Parameters: `token`, `bankId`, `accountId`
   - Returns: List of transactions

3. **getAccountBalance**
   - Retrieves the balance for a specific account
   - Parameters: `token`, `bankId`, `accountId`
   - Returns: Account balance with currency

4. **getCounterparties**
   - Retrieves counterparties for a specific account
   - Parameters: `token`, `bankId`, `accountId`
   - Returns: List of counterparties

## Tech Stack

- **Spring Boot**: 3.3.5
- **Spring AI**: 1.0.1
- **Java**: 17+
- **Maven**: Dependency management

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- OBP Sandbox account and API key

## Local Development

### 1. Configure OBP API Key

Set environment variable:
```bash
export OBP_API_KEY=your-api-key-here
```

Or update `src/main/resources/application.yml`:
```yaml
obp:
  api:
    api-key: your-api-key-here
```

### 2. Build and Run

```bash
mvn clean install
mvn spring-boot:run
```

The server will start on: http://localhost:8081

## API Endpoints

### MCP Protocol Endpoints

- `POST /mcp/call` - Execute MCP tool calls

### Health Check

- `GET /actuator/health` - Application health status

## MCP Protocol Usage

### List Available Tools

```json
POST /mcp/call
{
  "jsonrpc": "2.0",
  "method": "tools/list",
  "id": "1"
}
```

### Call a Tool

```json
POST /mcp/call
{
  "jsonrpc": "2.0",
  "method": "tools/call",
  "id": "2",
  "params": {
    "name": "getAccounts",
    "arguments": {
      "token": "your-obp-token"
    }
  }
}
```

## Deployment to Cloud Foundry / Tanzu

### 1. Build Application

```bash
mvn clean package -DskipTests
```

### 2. Set Environment Variables

```bash
cf set-env obp-mcp-server OBP_API_KEY "your-api-key"
```

### 3. Deploy

```bash
cf push
```

## Configuration

### Environment Variables

- `OBP_API_KEY`: OBP sandbox API key (required)
- `SPRING_PROFILES_ACTIVE`: Set to `cloud` for Cloud Foundry deployment
- `SERVER_PORT`: Server port (default: 8081)

## Error Handling

The server implements proper error handling with:
- JSON-RPC 2.0 compliant error responses
- Detailed error messages
- Proper HTTP status codes

## License

This is a demo application for educational purposes.
