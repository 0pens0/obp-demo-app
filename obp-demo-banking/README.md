# OBP Demo Banking Application

A simple Spring Boot demo application that integrates with the Open Bank Project (OBP) sandbox to demonstrate banking functionality with an AI-powered chatbot.

## Features

- **Admin Section**: Create new customers in the OBP sandbox
- **Customer Dashboard**: View accounts, transactions, balances, and counterparties
- **AI Chatbot**: Interactive chatbot powered by Spring AI to answer banking questions
- **Tanzu Ready**: Configured for deployment on Tanzu Application Platform with service binding

## Tech Stack

- **Backend**: Spring Boot 3.2.0
- **Frontend**: Thymeleaf (server-side rendering)
- **AI**: Spring AI with OpenAI support
- **API Integration**: WebClient for OBP API calls
- **Security**: Spring Security with session-based authentication

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+
- OBP Sandbox account and API key

### Configuration

1. Set your OBP API key in `application.yml` or as environment variable:
```bash
export OBP_API_KEY=your-api-key-here
```

2. (Optional) For local AI testing, set OpenAI API key:
```bash
export OPENAI_API_KEY=your-openai-key
```

### Run Locally

```bash
mvn clean install
mvn spring-boot:run
```

Access the application at: http://localhost:8080

### Default Admin Credentials
- Username: `admin`
- Password: `admin`

## Project Structure

```
obp-demo-banking/
├── src/main/java/com/obp/demo/
│   ├── config/          # Configuration classes
│   ├── controller/      # MVC controllers
│   ├── service/         # Business logic services
│   ├── model/           # Domain models
│   └── dto/             # Data transfer objects
├── src/main/resources/
│   ├── templates/       # Thymeleaf templates
│   ├── static/          # Static resources (CSS)
│   └── application.yml  # Application configuration
└── pom.xml              # Maven dependencies
```

## Demo Flow

See [DEMO_FLOW.md](DEMO_FLOW.md) for detailed demo instructions.

## Deployment to Tanzu

The application is configured to work with Tanzu service binding for AI services. See DEMO_FLOW.md for deployment instructions.

## License

This is a demo application for educational purposes.
