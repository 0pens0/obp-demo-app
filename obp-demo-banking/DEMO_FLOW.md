# OBP Demo Banking Application - Demo Flow Guide

## Overview
This document describes the demo flow and steps needed to run the OBP Demo Banking Application. The application integrates with the Open Bank Project (OBP) sandbox to demonstrate banking functionality with an AI-powered chatbot.

## Prerequisites

### 1. OBP Sandbox Account
- Register at: https://apisandbox.openbankproject.com/
- Create an account and obtain your API key
- Note your API key for configuration

### 2. Development Environment
- Java 17 or higher
- Maven 3.6+
- IDE (IntelliJ IDEA, Eclipse, or VS Code)
- Internet connection (for OBP sandbox access)

### 3. For Tanzu Deployment
- Tanzu Application Platform access
- AI LLM service instance (OpenAI, Azure OpenAI, etc.) configured in Tanzu
- Service binding capability

## Local Development Setup

### Step 1: Clone/Download the Project
```bash
cd obp-demo-banking
```

### Step 2: Configure OBP API Key
Edit `src/main/resources/application.yml`:
```yaml
obp:
  api:
    api-key: ${OBP_API_KEY:your-actual-api-key-here}
```

Or set environment variable:
```bash
export OBP_API_KEY=your-actual-api-key-here
```

### Step 3: Configure Spring AI (Optional for Local)
For local testing without Tanzu service binding, set:
```bash
export OPENAI_API_KEY=your-openai-api-key
```

Or update `application.yml`:
```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY:your-openai-api-key}
```

### Step 4: Build the Application
```bash
mvn clean install
```

### Step 5: Run the Application
```bash
mvn spring-boot:run
```

The application will start on: http://localhost:8080

## Demo Flow

### Phase 1: Admin Section - Create Customer

1. **Access Admin Login**
   - Navigate to: http://localhost:8080/admin/login
   - Default credentials:
     - Username: `admin`
     - Password: `admin`

2. **Admin Dashboard**
   - After login, you'll see the admin dashboard
   - This is where you create new customers for the OBP sandbox

3. **Create a New Customer**
   - Fill in the form:
     - **Username**: Choose a unique username (e.g., `john.doe`)
     - **Email**: Valid email address (e.g., `john.doe@example.com`)
     - **Password**: At least 6 characters (e.g., `password123`)
     - **First Name**: (e.g., `John`)
     - **Last Name**: (e.g., `Doe`)
   - Click "Create Customer"
   - The system will:
     - Create the user in the OBP sandbox
     - Display success message
     - Show the new customer in the "Created Customers" table

4. **Verify Customer Creation**
   - Check the "Created Customers" table
   - Note the User ID assigned by OBP
   - The customer is now ready to log in

### Phase 2: Customer Section - View Banking Data

1. **Access Customer Login**
   - Navigate to: http://localhost:8080/customer/login
   - Or click logout from admin and navigate manually

2. **Customer Login**
   - Use the credentials you created in the admin section:
     - **Username**: The username you created (e.g., `john.doe`)
     - **Password**: The password you set
   - Click "Login"

3. **Customer Dashboard**
   - After successful login, you'll see:
     - **Accounts Section**: 
       - Account details (label, number, IBAN)
       - Account balance
       - Account type
     - **Recent Transactions**: 
       - Last 10 transactions
       - Description, amount, date
     - **Counterparties**: 
       - List of counterparties associated with the account
     - **Account Summary**: 
       - Total balance information

4. **AI Chatbot Interaction**
   - Click the chat icon (💬) in the bottom-right corner
   - The chatbot widget will open
   - Try asking questions like:
     - "What is my account balance?"
     - "Show me my recent transactions"
     - "Who are my counterparties?"
     - "What accounts do I have?"
   - The chatbot uses Spring AI to provide context-aware responses based on your banking data

## Tanzu Deployment

### Step 1: Prepare for Deployment
Ensure your `application.yml` is configured to use environment variables:
```yaml
obp:
  api:
    api-key: ${OBP_API_KEY}
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
```

### Step 2: Build Application
```bash
mvn clean package
```

### Step 3: Deploy to Tanzu
```bash
tanzu apps workload create obp-demo-banking \
  --git-repo <your-repo-url> \
  --git-branch main \
  --type web \
  --env OBP_API_KEY=<your-obp-api-key> \
  --service-ref ai-service=<your-ai-service-name>
```

### Step 4: Service Binding
The application will automatically use Tanzu service binding for the AI LLM service. The service binding will inject:
- `OPENAI_API_KEY` or equivalent credentials
- Connection details for the AI service

### Step 5: Access Application
Once deployed, access the application via the Tanzu-provided URL.

## Troubleshooting

### Issue: Cannot create user in OBP
**Solution**: 
- Verify your OBP API key is correct
- Check that you have proper permissions in the OBP sandbox
- Ensure internet connectivity to apisandbox.openbankproject.com

### Issue: Customer login fails
**Solution**:
- Verify the user was created successfully in admin dashboard
- Check that username and password match what was created
- Ensure OBP sandbox is accessible

### Issue: No data shown on customer dashboard
**Solution**:
- The OBP sandbox may not have pre-populated data for new users
- This is normal - the application will show empty states
- You can still test the chatbot functionality

### Issue: Chatbot not responding
**Solution**:
- For local: Verify OPENAI_API_KEY is set correctly
- For Tanzu: Check service binding is configured correctly
- Check application logs for AI service connection errors

### Issue: Application won't start
**Solution**:
- Verify Java 17+ is installed: `java -version`
- Check Maven is installed: `mvn -version`
- Review application logs for specific errors

## API Endpoints

### Admin Endpoints
- `GET /admin/login` - Admin login page
- `POST /admin/authenticate` - Admin authentication
- `GET /admin/dashboard` - Admin dashboard
- `POST /admin/users` - Create new customer
- `GET /admin/logout` - Admin logout

### Customer Endpoints
- `GET /customer/login` - Customer login page
- `POST /customer/authenticate` - Customer authentication
- `GET /customer/dashboard` - Customer dashboard
- `GET /customer/logout` - Customer logout

### API Endpoints
- `POST /api/chat` - Chatbot API endpoint

### Health Check
- `GET /actuator/health` - Application health status

## Architecture Notes

### OBP Integration
- Uses OBP Direct Login for authentication
- Fetches accounts, transactions, balances, and counterparties
- All API calls go to: https://apisandbox.openbankproject.com

### Spring AI Integration
- Uses Spring AI ChatClient for chatbot functionality
- Supports OpenAI and other LLM providers
- Context-aware responses based on customer banking data

### Security
- Simple session-based authentication
- Separate admin and customer roles
- CSRF protection disabled for demo purposes

## Next Steps (Future Phases)

Potential enhancements for future phases:
- Database persistence for user sessions
- Enhanced transaction history
- Account management features
- Multi-bank support
- Advanced AI features
- Real-time notifications

## Support

For issues or questions:
- OBP Documentation: https://www.openbankproject.com/
- OBP API Explorer: https://apisandbox.openbankproject.com/
- Spring AI Documentation: https://docs.spring.io/spring-ai/reference/
