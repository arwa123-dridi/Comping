# Comping - Camping Management System

A comprehensive camping management platform featuring a Spring Boot backend and an Angular frontend. The system manages camping trips, teams, products, and integrations with AI for recommendations, Stripe for payments, and Cloudinary for media.

## Project Structure

This repository is organized as a monorepo:

- **`backendComping/`**: Spring Boot REST API (Java 17, MongoDB).
- **`frontendComping/frontendCompingApp/`**: Angular Web Application (Angular 19).

---

## Tech Stack

### Backend
- **Language**: Java 17
- **Framework**: Spring Boot 3.3.5
- **Database**: MongoDB
- **Security**: Spring Security with JWT
- **Documentation**: Springdoc OpenAPI (Swagger)
- **Integrations**:
  - **Stripe**: Payment processing
  - **Cloudinary**: Media management
  - **FastAPI**: ML predictions service (external)

### Frontend
- **Framework**: Angular 19
- **Styling**: CSS / Bootstrap (implied)
- **Charts**: Chart.js
- **PDF Generation**: jsPDF

---

## Prerequisites

Before running the application, ensure you have the following installed:
- **Java 17 JDK**
- **Node.js** (v18+ recommended) & **npm**
- **Maven** 3.6+
- **MongoDB** (Local instance or Atlas URI)
- **Angular CLI** (`npm install -g @angular/cli`)

---

## Setup & Installation

### 1. Clone the repository
```bash
git clone <repository-url>
cd Comping
```

### 2. Backend Configuration
Navigate to `backendComping/` and configure `src/main/resources/application.properties`.

Key properties to set:
- `spring.mongodb.uri`: Your MongoDB connection string.
- `spring.mail.username`: Email for notifications.
- `stripe.secret.key`: Your Stripe secret key.
- `cloudinary.cloud.name`: Your Cloudinary cloud name.
- `ia.api.url`: URL of the FastAPI ML service (default: `http://localhost:5000/predict`).

### 3. Frontend Configuration
Navigate to `frontendComping/frontendCompingApp/`.
If there are environment-specific settings, they are typically found in `src/environments/`.

---

## Running the Application

### Start the Backend
```bash
cd backendComping
mvn spring-boot:run
```
The API will be available at `http://localhost:8087`.
Swagger UI: `http://localhost:8087/swagger-ui/index.html`

### Start the Frontend
```bash
cd frontendComping/frontendCompingApp
npm install
npm start
```
The application will be available at `http://localhost:4200`.

---

## Scripts

### Backend (Maven)
- `mvn clean install`: Install dependencies and build.
- `mvn test`: Run backend tests.
- `mvn package`: Create an executable JAR.

### Frontend (npm)
- `npm install`: Install dependencies.
- `npm start`: Run development server.
- `npm run build`: Build for production.
- `npm test`: Run frontend unit tests.

---

## Features
- **User Management**: Secure authentication and JWT-based authorization.
- **Camping Trips (Sorties)**: Full CRUD for managing camping events.
- **Team Management**: Form and manage teams with AI-driven recommendations.
- **Participation System**: Register and track participants for trips.
- **Product Store**: Manage camping gear with payment integration.
- **AI Integration**: External FastAPI service for predictive features and checklists.
- **Payment Processing**: Integrated Stripe checkout.
- **Media Management**: Automated image uploads to Cloudinary.

---

## Environment Variables (Summary)

| Variable | Description | Location |
|----------|-------------|----------|
| `MONGODB_URI` | Connection string for MongoDB | Backend properties |
| `STRIPE_KEY` | Stripe Secret API Key | Backend properties |
| `CLOUDINARY_URL` | Cloudinary configuration | Backend properties |
| `ML_SERVICE_URL` | URL for the FastAPI service | Backend properties |

---

## Tests
- **Backend**: Run `mvn test` in the `backendComping` directory.
- **Frontend**: Run `ng test` in the `frontendComping/frontendCompingApp` directory.

---

## TODOs
- [ ] Add specific license (e.g., MIT, Apache 2.0).
- [ ] Document Docker deployment if applicable.
- [ ] Add CI/CD pipeline configuration details.
- [ ] Define production environment variables for the frontend.

## License
TODO: Add license information.
