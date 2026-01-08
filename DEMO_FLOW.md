# OBP Demo Application - Tanzu Platform Deployment and Demo Flow

This guide provides step-by-step instructions for deploying and demonstrating the OBP Demo Application on Tanzu Application Platform.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Pre-Deployment Setup](#pre-deployment-setup)
- [Deployment Steps](#deployment-steps)
- [Post-Deployment Configuration](#post-deployment-configuration)
- [Demo Flow](#demo-flow)
- [Troubleshooting](#troubleshooting)

## Prerequisites

### Required Access and Tools

1. **Tanzu Application Platform Access**
   - Access to a Tanzu Application Platform instance
   - Kubernetes cluster access (if using TAP workloads)
   - Cloud Foundry organization and space access (if using CF deployment)
   - Permissions to create services and deploy applications

2. **Installed Tools**
   ```bash
   # Tanzu CLI (for TAP workloads)
   tanzu version  # Should be 1.x or higher
   
   # Cloud Foundry CLI (for CF deployment)
   cf --version  # Should be 8.x or higher
   
   # Maven (for building)
   mvn --version  # Should be 3.6+
   
   # Java (for building)
   java --version  # Should be 17+
   
   # kubectl (for Kubernetes operations)
   kubectl version --client
   ```

3. **OBP Sandbox Account**
   - Register at: https://apisandbox.openbankproject.com/
   - Obtain your API key from the dashboard
   - Note your API key for deployment

4. **Service Access**
   - Access to PostgreSQL service in Tanzu marketplace
   - Access to LLM service (OpenAI/Azure OpenAI) in Tanzu marketplace

### Verify Prerequisites

```bash
# Login to Tanzu Application Platform
tanzu login

# Verify Tanzu context
tanzu apps workload list

# Or if using Cloud Foundry
cf login -a <your-cf-api-endpoint> -u <username> -o <org> -s <space>
cf target

# Check available services
cf marketplace
```

## Pre-Deployment Setup

### 1. Clone and Navigate to Project

```bash
cd ~/git/obp-demo-app
```

### 2. Verify OBP API Key

Ensure you have your OBP sandbox API key ready. You'll need it for:
- Tanzu platform manifest variable configuration
- Testing the application

### 3. Check Available Services

```bash
# List available PostgreSQL services
cf marketplace -s postgresql

# List available LLM services (name may vary)
cf marketplace | grep -i "llm\|openai\|azure"

# Or using Tanzu services
tanzu services list-available
```

Note the exact service names and plans available in your environment.

## Deployment Steps

### Step 1: Set OBP API Key as Tanzu Platform Variable

**IMPORTANT**: Set this before deploying applications. The manifest files use `((OBP_API_KEY))` which will be resolved during deployment.

**Option 1: Set Space Variable (Recommended)**
```bash
# Get current space name
SPACE_NAME=$(cf target | grep space | awk '{print $2}')

# Set the variable at the space level
cf set-space-variable $SPACE_NAME OBP_API_KEY "your-obp-api-key-here"

# Verify it was set
cf space-variables
```

**Option 2: Set Organization Variable**
```bash
# Get current org name
ORG_NAME=$(cf target | grep org | awk '{print $2}')

# Set at the organization level (applies to all spaces)
cf set-org-variable $ORG_NAME OBP_API_KEY "your-obp-api-key-here"

# Verify it was set
cf org-variables
```

**Option 3: Using Tanzu Secret (Alternative for TAP)**
```bash
# Create a Kubernetes secret for TAP workloads
kubectl create secret generic obp-api-key \
  --from-literal=OBP_API_KEY="your-obp-api-key-here" \
  -n <your-namespace>

# Reference in workload YAML or use service binding
```

### Step 2: Create Required Services

#### 2.1 Create PostgreSQL Service

```bash
# Create PostgreSQL service instance
cf create-service postgresql standard postgres-vector-db

# Wait for service to be ready (this may take a few minutes)
cf service postgres-vector-db

# Monitor service creation status
watch -n 5 'cf service postgres-vector-db | grep status'

# Wait until status shows "create succeeded"
```

**Note**: If your environment has a PostgreSQL service with pgvector pre-installed, use that. Otherwise, you may need to enable the extension manually after deployment.

#### 2.2 Create LLM Service

```bash
# Create LLM service (service name and plan may vary by environment)
# Example for OpenAI service:
cf create-service openai-service standard llm-service

# Or for Azure OpenAI:
# cf create-service azure-openai-service standard llm-service

# Wait for service to be ready
cf service llm-service

# Monitor service creation
watch -n 5 'cf service llm-service | grep status'
```

**Important**: Replace `openai-service` and `standard` with the actual service name and plan available in your Tanzu marketplace. Check with:
```bash
cf marketplace | grep -i "openai\|llm\|azure"
```

### Step 3: Build Applications

#### 3.1 Build obp-demo-banking

```bash
cd ~/git/obp-demo-app/obp-demo-banking

# Clean and build
mvn clean package -DskipTests

# Verify JAR was created
ls -lh target/demo-banking-*.jar
```

#### 3.2 Build MCP Server

```bash
cd ~/git/obp-demo-app/open-banking-mcp/mcp-server

# Clean and build
mvn clean package -DskipTests

# Verify JAR was created
ls -lh target/mcp-server-*.jar
```

### Step 4: Deploy Applications

#### 4.1 Deploy obp-demo-banking

**Using Cloud Foundry (Recommended for this setup):**
```bash
cd ~/git/obp-demo-app/obp-demo-banking

# Deploy using manifest (variable will be resolved automatically)
cf push

# Or deploy with explicit configuration
cf push obp-demo-banking \
  -p target/demo-banking-1.0.0-SNAPSHOT.jar \
  -f manifest.yml
```

**Using Tanzu Application Platform Workload (Alternative):**
```bash
cd ~/git/obp-demo-app/obp-demo-banking

# Create workload from local source
tanzu apps workload create obp-demo-banking \
  --local-path . \
  --source-image <your-registry>/obp-demo-banking \
  --type web \
  --env OBP_API_KEY=((OBP_API_KEY)) \
  --service-ref postgres-vector-db=services.apps.tanzu.vmware.com/v1alpha1:PostgreSQL:postgres-vector-db \
  --service-ref llm-service=services.apps.tanzu.vmware.com/v1alpha1:OpenAI:llm-service \
  --yes

# Monitor deployment
tanzu apps workload get obp-demo-banking
tanzu apps workload tail obp-demo-banking
```

#### 4.2 Deploy MCP Server

**Using Cloud Foundry:**
```bash
cd ~/git/obp-demo-app/open-banking-mcp/mcp-server

# Deploy using manifest
cf push

# Or deploy with explicit configuration
cf push obp-mcp-server \
  -p target/mcp-server-1.0.0-SNAPSHOT.jar \
  -f manifest.yml
```

**Using Tanzu Application Platform Workload:**
```bash
cd ~/git/obp-demo-app/open-banking-mcp/mcp-server

tanzu apps workload create obp-mcp-server \
  --local-path . \
  --source-image <your-registry>/obp-mcp-server \
  --type web \
  --env OBP_API_KEY=((OBP_API_KEY)) \
  --yes
```

### Step 5: Verify Deployments

```bash
# Check application status (Cloud Foundry)
cf apps

# Check application status (Tanzu)
tanzu apps workload list

# Check service bindings
cf services
cf service postgres-vector-db
cf service llm-service

# View application logs (Cloud Foundry)
cf logs obp-demo-banking --recent
cf logs obp-mcp-server --recent

# View application logs (Tanzu)
tanzu apps workload tail obp-demo-banking
tanzu apps workload tail obp-mcp-server

# Check application health endpoints
# Get the app URL first
APP_URL=$(cf app obp-demo-banking | grep urls | awk '{print $2}')
curl $APP_URL/actuator/health
```

### Step 6: Verify Service Bindings

```bash
# Check environment variables (service bindings are injected automatically)
cf env obp-demo-banking

# Verify PostgreSQL connection details
cf env obp-demo-banking | grep -A 10 VCAP_SERVICES | grep postgres

# Verify LLM service connection details
cf env obp-demo-banking | grep -A 10 VCAP_SERVICES | grep llm

# For Tanzu workloads, check service bindings
tanzu apps workload get obp-demo-banking | grep -A 20 "Service Bindings"
```

### Step 7: Initialize Database

The application will automatically run Flyway migrations on startup to:
- Create pgvector extension
- Set up vector store tables

To verify database initialization:

```bash
# Check Flyway migration logs
cf logs obp-demo-banking --recent | grep -i "flyway\|migration\|database"

# SSH into the application container (Cloud Foundry)
cf ssh obp-demo-banking

# Inside the container, check VCAP_SERVICES for database connection
env | grep VCAP_SERVICES

# Exit SSH
exit
```

**Note**: If pgvector extension is not available, you may need to:
1. Contact your platform administrator to enable it
2. Use a PostgreSQL service that includes pgvector
3. Manually enable it after deployment (if you have database admin access)

## Post-Deployment Configuration

### 1. Get Application URLs

```bash
# Get obp-demo-banking URL (Cloud Foundry)
cf app obp-demo-banking | grep urls
OBP_URL=$(cf app obp-demo-banking | grep urls | awk '{print $2}')

# Get MCP server URL
cf app obp-mcp-server | grep urls
MCP_URL=$(cf app obp-mcp-server | grep urls | awk '{print $2}')

# Get URLs (Tanzu)
tanzu apps workload get obp-demo-banking | grep "https://"
tanzu apps workload get obp-mcp-server | grep "https://"
```

### 2. Verify Service Bindings

```bash
# Check all environment variables
cf env obp-demo-banking

# Verify PostgreSQL connection
cf logs obp-demo-banking --recent | grep -i "database\|postgres\|flyway\|jdbc"

# Verify LLM service connection
cf logs obp-demo-banking --recent | grep -i "openai\|llm\|ai\|chat"

# Check Spring Cloud Bindings
cf logs obp-demo-banking --recent | grep -i "spring.cloud.bindings"
```

### 3. Test Health Endpoints

```bash
# Test obp-demo-banking health
curl $OBP_URL/actuator/health

# Test MCP server health
curl $MCP_URL/actuator/health

# Expected response:
# {"status":"UP"}
```

### 4. Verify OBP API Key Variable

```bash
# Check if variable is set at space/org level
cf space-variables
cf org-variables

# Check app environment (after deployment)
cf env obp-demo-banking | grep OBP_API_KEY

# Verify it's not empty
cf env obp-demo-banking | grep OBP_API_KEY | grep -v "^#"
```

## Demo Flow

### Phase 1: Admin Section - Create Customer

1. **Access Admin Login**
   ```
   URL: https://obp-demo-banking.apps.<your-domain>/admin/login
   ```
   Or use the URL from: `cf app obp-demo-banking | grep urls`

2. **Login to Admin Dashboard**
   - Username: `admin`
   - Password: `admin`
   - Click "Login"

3. **Admin Dashboard Overview**
   - You'll see the admin dashboard
   - This section allows you to create new customers in the OBP sandbox
   - View previously created customers in the "Created Customers" table

4. **Create a New Customer**
   - Fill in the customer creation form:
     - **Username**: Choose a unique username (e.g., `john.doe`)
     - **Email**: Valid email address (e.g., `john.doe@example.com`)
     - **Password**: At least 6 characters (e.g., `password123`)
     - **First Name**: (e.g., `John`)
     - **Last Name**: (e.g., `Doe`)
   - Click "Create Customer"
   - The system will:
     - Create the user in the OBP sandbox via API
     - Display a success message
     - Show the new customer in the "Created Customers" table with their OBP User ID
   - **Important**: Note the OBP User ID assigned to the customer (you'll need this for verification)

5. **Verify Customer Creation**
   - Check the "Created Customers" table
   - Verify the User ID is displayed (format: UUID)
   - The customer is now ready to log in to the customer portal

### Phase 2: Customer Section - View Banking Data

1. **Access Customer Login**
   ```
   URL: https://obp-demo-banking.apps.<your-domain>/customer/login
   ```
   Or navigate from the admin dashboard by clicking "Logout" and then accessing the customer login page

2. **Customer Login**
   - Use the credentials you created in the admin section:
     - **Username**: The username you created (e.g., `john.doe`)
     - **Password**: The password you set
   - Click "Login"
   - You'll be redirected to the customer dashboard upon successful authentication

3. **Customer Dashboard Overview**
   After successful login, you'll see the customer dashboard with:

   **Accounts Section**:
   - Account details including:
     - Account label/name
     - Account number
     - IBAN (International Bank Account Number)
     - Account type
     - Account balance with currency
   
   **Recent Transactions**:
   - Last 10 transactions displayed in a table
   - Transaction details:
     - Description
     - Amount and currency
     - Posted date
     - Transaction type
   
   **Counterparties**:
   - List of counterparties associated with the account
   - Counterparty names and account information
   
   **Account Summary**:
   - Total balance information
   - Currency display

4. **AI Chatbot Interaction**
   - Click the chat icon (💬) in the bottom-right corner of the page
   - The chatbot widget will open as a slide-out panel
   - The chatbot is powered by:
     - Spring AI for natural language processing
     - PostgreSQL vector store for RAG (Retrieval Augmented Generation)
     - Context-aware responses based on your banking data
     - Chat history stored in vector database for improved responses over time
   
   **Try asking questions like**:
   - "What is my account balance?"
   - "Show me my recent transactions"
   - "Who are my counterparties?"
   - "What accounts do I have?"
   - "Tell me about my last transaction"
   - "What's the total amount of my transactions this month?"
   
   **Observe the RAG capabilities**:
   - The chatbot retrieves relevant context from previous conversations
   - It combines current account data with historical chat context
   - Responses become more contextual over time as chat history accumulates

### Phase 3: MCP Server - API Tools Demonstration

1. **Access MCP Server**
   ```
   URL: https://obp-mcp-server.apps.<your-domain>
   ```
   Or use: `cf app obp-mcp-server | grep urls`

2. **List Available Tools**
   ```bash
   curl -X POST https://obp-mcp-server.apps.<your-domain>/mcp/call \
     -H "Content-Type: application/json" \
     -d '{
       "jsonrpc": "2.0",
       "method": "tools/list",
       "id": "1"
     }'
   ```
   
   **Expected Response**: JSON containing list of 4 tools:
   - `getAccounts`
   - `getTransactions`
   - `getAccountBalance`
   - `getCounterparties`

3. **Get OBP Authentication Token**
   - First, authenticate with OBP to get a token
   - You can get a token by:
     - Using the customer login flow (token stored in session)
     - Calling OBP API directly:
       ```bash
       curl -X POST https://apisandbox.openbankproject.com/obp/v5.1.0/my/logins/direct \
         -H "Content-Type: application/json" \
         -d '{
           "username": "your-username",
           "password": "your-password",
           "consumer_key": "your-obp-api-key"
         }'
       ```
   - Extract the `token` from the response

4. **Call MCP Tools**

   **Get Accounts**:
   ```bash
   curl -X POST https://obp-mcp-server.apps.<your-domain>/mcp/call \
     -H "Content-Type: application/json" \
     -d '{
       "jsonrpc": "2.0",
       "method": "tools/call",
       "id": "2",
       "params": {
         "name": "getAccounts",
         "arguments": {
           "token": "your-obp-token-here"
         }
       }
     }'
   ```
   
   **Get Transactions**:
   ```bash
   # First get bankId and accountId from getAccounts response
   curl -X POST https://obp-mcp-server.apps.<your-domain>/mcp/call \
     -H "Content-Type: application/json" \
     -d '{
       "jsonrpc": "2.0",
       "method": "tools/call",
       "id": "3",
       "params": {
         "name": "getTransactions",
         "arguments": {
           "token": "your-obp-token",
           "bankId": "bank-id-from-getAccounts",
           "accountId": "account-id-from-getAccounts"
         }
       }
     }'
   ```
   
   **Get Account Balance**:
   ```bash
   curl -X POST https://obp-mcp-server.apps.<your-domain>/mcp/call \
     -H "Content-Type: application/json" \
     -d '{
       "jsonrpc": "2.0",
       "method": "tools/call",
       "id": "4",
       "params": {
         "name": "getAccountBalance",
         "arguments": {
           "token": "your-obp-token",
           "bankId": "bank-id",
           "accountId": "account-id"
         }
       }
     }'
   ```
   
   **Get Counterparties**:
   ```bash
   curl -X POST https://obp-mcp-server.apps.<your-domain>/mcp/call \
     -H "Content-Type: application/json" \
     -d '{
       "jsonrpc": "2.0",
       "method": "tools/call",
       "id": "5",
       "params": {
         "name": "getCounterparties",
         "arguments": {
           "token": "your-obp-token",
           "bankId": "bank-id",
           "accountId": "account-id"
         }
       }
     }'
   ```

## Troubleshooting

### Deployment Issues

**Issue**: Application fails to start
```bash
# Check logs
cf logs obp-demo-banking --recent

# For Tanzu workloads
tanzu apps workload tail obp-demo-banking

# Common issues:
# - Missing OBP_API_KEY variable (check: cf space-variables)
# - Service binding failures (check: cf services)
# - Database connection issues (check: cf env obp-demo-banking | grep DATABASE)
# - Build failures (check: cf logs obp-demo-banking --recent | grep -i "build\|maven")
```

**Issue**: OBP_API_KEY variable not resolved
```bash
# Verify variable is set
cf space-variables
cf org-variables

# If not set, set it:
cf set-space-variable <space-name> OBP_API_KEY "your-key"

# Redeploy the application
cf push obp-demo-banking
```

**Issue**: Service binding not working
```bash
# Verify services are created and bound
cf services
cf service postgres-vector-db
cf service llm-service

# Check service credentials are injected
cf env obp-demo-banking | grep VCAP_SERVICES

# Rebind service if needed
cf bind-service obp-demo-banking postgres-vector-db
cf restart obp-demo-banking
```

**Issue**: Database migration failures
```bash
# Check Flyway logs
cf logs obp-demo-banking --recent | grep -i "flyway\|migration"

# Verify pgvector extension is available
# May need to contact platform admin or use a PostgreSQL service with pgvector
```

### Runtime Issues

**Issue**: Cannot create user in OBP
- Verify OBP API key is correct:
  ```bash
  # Check if variable is set
  cf space-variables | grep OBP_API_KEY
  cf org-variables | grep OBP_API_KEY
  
  # Check app environment
  cf env obp-demo-banking | grep OBP_API_KEY
  ```
- Check OBP sandbox is accessible: `curl https://apisandbox.openbankproject.com/obp/v5.1.0/banks`
- Review OBP API logs: `cf logs obp-demo-banking --recent | grep -i "obp\|api"`

**Issue**: Customer login fails
- Verify user was created successfully in admin dashboard
- Check username and password match exactly (case-sensitive)
- Review authentication logs: `cf logs obp-demo-banking --recent | grep -i "auth\|login"`

**Issue**: No data shown on customer dashboard
- OBP sandbox may not have pre-populated data for new users
- This is normal - the application will show empty states
- You can still test the chatbot functionality
- Verify OBP API connectivity: `cf logs obp-demo-banking --recent | grep -i "accounts\|transactions"`

**Issue**: Chatbot not responding
- Check LLM service binding:
  ```bash
  cf env obp-demo-banking | grep -A 20 llm-service
  ```
- Verify service credentials are injected correctly
- Check application logs for AI service connection errors:
  ```bash
  cf logs obp-demo-banking --recent | grep -i "openai\|llm\|ai\|chat"
  ```
- Verify OpenAI API key is accessible through service binding
- Check if LLM service is properly provisioned: `cf service llm-service`

**Issue**: Vector store not working
- Check PostgreSQL service binding: `cf env obp-demo-banking | grep postgres`
- Verify pgvector extension is enabled (check migration logs)
- Review database migration logs: `cf logs obp-demo-banking --recent | grep -i "flyway\|vector"`
- Check vector store configuration in logs: `cf logs obp-demo-banking --recent | grep -i "vector\|embedding"`

### Service-Specific Issues

**PostgreSQL Service**:
```bash
# Check service status
cf service postgres-vector-db

# View service details and bindings
cf service postgres-vector-db

# Check connection details in app
cf env obp-demo-banking | grep -A 10 postgres

# Verify pgvector extension (SSH into app)
cf ssh obp-demo-banking
env | grep DATABASE_URL
# Connect to database and check: CREATE EXTENSION IF NOT EXISTS vector;
exit
```

**LLM Service**:
```bash
# Check service status
cf service llm-service

# Verify credentials are injected
cf env obp-demo-banking | grep -A 20 llm-service

# Check service plan supports your needs
cf marketplace -s <llm-service-name>

# Verify service binding
cf services | grep llm-service
```

## Monitoring and Maintenance

### View Application Metrics

```bash
# Application stats (Cloud Foundry)
cf app obp-demo-banking
cf app obp-mcp-server

# Application stats (Tanzu)
tanzu apps workload get obp-demo-banking
tanzu apps workload get obp-mcp-server

# Recent logs
cf logs obp-demo-banking --recent
tanzu apps workload tail obp-demo-banking

# Stream logs
cf logs obp-demo-banking
tanzu apps workload tail obp-demo-banking --follow
```

### Scale Applications

```bash
# Scale obp-demo-banking (Cloud Foundry)
cf scale obp-demo-banking -i 2 -m 2G

# Scale MCP server
cf scale obp-mcp-server -i 2 -m 1G

# Scale (Tanzu - update workload)
tanzu apps workload update obp-demo-banking --instances 2
```

### Update Applications

```bash
# Rebuild and redeploy (Cloud Foundry)
cd ~/git/obp-demo-app/obp-demo-banking
mvn clean package -DskipTests
cf push

cd ../open-banking-mcp/mcp-server
mvn clean package -DskipTests
cf push

# Update (Tanzu)
tanzu apps workload update obp-demo-banking --local-path .
```

### View Application URLs

```bash
# Get URLs (Cloud Foundry)
cf app obp-demo-banking | grep urls
cf app obp-mcp-server | grep urls

# Get URLs (Tanzu)
tanzu apps workload get obp-demo-banking | grep "https://"
tanzu apps workload get obp-mcp-server | grep "https://"
```

## Cleanup

To remove the demo applications and services:

```bash
# Delete applications (Cloud Foundry)
cf delete obp-demo-banking -f
cf delete obp-mcp-server -f

# Delete applications (Tanzu)
tanzu apps workload delete obp-demo-banking -y
tanzu apps workload delete obp-mcp-server -y

# Delete services (will prompt for confirmation)
cf delete-service postgres-vector-db
cf delete-service llm-service

# Remove space/org variables (optional)
cf unset-space-variable <space-name> OBP_API_KEY
cf unset-org-variable <org-name> OBP_API_KEY
```

## Additional Resources

- [OBP API Documentation](https://www.openbankproject.com/developers/)
- [OBP Sandbox](https://apisandbox.openbankproject.com/)
- [OBP API Explorer](https://apisandbox.openbankproject.com/obp-api-explorer)
- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [Tanzu Application Platform Documentation](https://docs.vmware.com/en/VMware-Tanzu-Application-Platform/)
- [Cloud Foundry CLI Documentation](https://docs.cloudfoundry.org/cf-cli/)
- [Tanzu CLI Documentation](https://docs.vmware.com/en/VMware-Tanzu-CLI/)

## Support

For issues or questions:
- Review application logs: `cf logs <app-name> --recent` or `tanzu apps workload tail <workload-name>`
- Check service bindings: `cf services` or `tanzu services list`
- Verify variables: `cf space-variables` and `cf org-variables`
- Review OBP API status: https://apisandbox.openbankproject.com/
- Contact your Tanzu platform administrator for:
  - Service provisioning issues
  - PostgreSQL pgvector extension availability
  - LLM service access and configuration
  - Platform-specific deployment questions
