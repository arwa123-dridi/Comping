# backendComping

## Overview
`backendComping` is the backend service for a camping management application. It provides a RESTful API to manage camping trips (sorties), teams (equipes), products, participations, and user profiles. It integrates with MongoDB for data storage, Cloudinary for image management, Stripe for payments, and an external FastAPI service for ML predictions.

## Features
- **User Management**: Authentication and authorization using Spring Security and JWT.
- **Camping Trips (Sorties)**: Create, update, and manage camping events.
- **Team Management**: Manage teams and their recommendations.
- **Participation System**: Handle user registrations for camping trips.
- **Product Management**: Manage camping gear and products.
- **Payment Integration**: Secure payments via Stripe.
- **Image Upload**: Integration with Cloudinary for handling media.
- **ML Integration**: Connects to a FastAPI service for predictive features.
- **API Documentation**: Interactive API docs using Swagger/OpenAPI.

## Tech Stack
- **Language**: Java 17
- **Framework**: Spring Boot 3.3.5
- **Database**: MongoDB
- **Security**: Spring Security, JWT (JSON Web Token - JJWT)
- **API Documentation**: Springdoc OpenAPI (Swagger UI)
- **External APIs**: 
  - **Cloudinary**: Media management
  - **Stripe**: Payment processing
  - **FastAPI**: Machine Learning predictions
- **Build Tool**: Maven

## Requirements
- **JDK 17** or higher
- **Maven 3.6+**
- **MongoDB** (running locally or a connection string)
- **FastAPI Service** (running on port 5000 for ML features)

## Setup & Installation

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd backendComping
   ```

2. **Configure Environment Variables**:
   The application uses `src/main/resources/application.properties`. You can override properties using environment variables:
   - `SPRING_MONGODB_URI`: MongoDB connection string.
   - `SMTP_PASSWORD`: Password for the mail server.
   - `STRIPE_SECRET_KEY`: Stripe API secret key.
   - `STRIPE_WEBHOOK_SECRET`: Stripe webhook secret.
   - `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET`: Cloudinary credentials.
   - `IA_API_URL`: FastAPI ML prediction URL.

3. **Install Dependencies**:
   ```bash
   mvn clean install
   ```

## Running the Application

### Using Maven
```bash
mvn spring-boot:run
```
The application will start on port **8087** by default.

### Build Executable JAR
```bash
mvn clean package
java -jar target/backendComping-0.0.1-SNAPSHOT.jar
```

## Environment Variables / Properties
The following key properties are configured in `application.properties`:

| Property | Description | Default Value |
|----------|-------------|---------------|
| `server.port` | Server port | `8087` |
| `spring.mongodb.uri` | MongoDB Connection URI | `mongodb://localhost:27017/Comping` |
| `spring.mail.username` | Email for notifications | `ahmetchaouch19@gmail.com` |
| `stripe.secret.key` | Stripe Secret Key | `${STRIPE_SECRET_KEY}` |
| `cloudinary.cloud.name` | Cloudinary Cloud Name | `dyeyeeb49` |
| `ia.api.url` | FastAPI ML Predict URL | `http://localhost:5000/predict` |

## Project Structure
```
backendComping/
├── src/
│   ├── main/
│   │   ├── java/tn/comping/spring/backendcomping/
│   │   │   ├── config/          # Security, Cloudinary, and other configurations
│   │   │   ├── controllers/     # REST Controllers (API endpoints)
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── entities/        # MongoDB Documents (Data Models)
│   │   │   ├── exceptions/      # Global Exception Handling
│   │   │   ├── repositories/    # Spring Data MongoDB Repositories
│   │   │   ├── services/        # Business Logic Interfaces
│   │   │   │   └── serviceImpl/ # Business Logic Implementation
│   │   │   └── utils/           # Utilities and Mappers
│   │   └── resources/
│   │       ├── application.properties # Main configuration file
│   │       └── static/ / templates/   # Web resources
│   └── test/                    # Unit and Integration Tests
├── uploads/                     # Local file uploads
├── pom.xml                      # Maven Configuration
└── mvnw / mvnw.cmd              # Maven Wrapper
```

## Scripts & Commands
- `mvn clean`: Clean the build directory.
- `mvn compile`: Compile the source code.
- `mvn test`: Run tests.
- `mvn package`: Build the JAR file.
- `mvn spring-boot:run`: Run the application.

## Tests
To run the automated tests:
```bash
mvn test
```
*Note: Ensure MongoDB is accessible or mocked for integration tests.*

## API Documentation
Once the application is running, you can access the Swagger UI at:
`http://localhost:8087/swagger-ui/index.html`

## License
TODO: Add license information here (e.g., MIT, Apache 2.0).
