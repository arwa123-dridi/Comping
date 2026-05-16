# How to Run the Full Project

This project has two parts:
- Backend: Spring Boot in `backendComping`
- Frontend: Angular in `frontendComping/frontendCompingApp`

## 1) Prerequisites

Install:
- Docker and Docker Compose
- Node.js 20+ and npm
- Java 17
- Maven 3.9+ or use the included Maven wrapper

## 2) Add API keys

Create a `.env` file at the project root with:

```env
GOOGLE_MAPS_API_KEY=your_google_maps_key
OPENWEATHER_API_KEY=your_openweather_key
```

The backend reads these keys from environment variables.

For the Angular map, also set the key in:
- `frontendComping/frontendCompingApp/src/environments/environment.ts`

## 3) Run with Docker

From the project root:

1. Build and start the stack with Docker Compose.
2. Wait for the containers to become healthy.
3. Open the app in your browser.

### Services and ports
- Frontend: `http://localhost:4200`
- Backend: `http://localhost:8087`
- MongoDB: `localhost:27017`

### Useful endpoints
- Weather forecast by city:
  - `GET /api/weather/forecast?city=Tunis`
- Weather forecast by coordinates:
  - `GET /api/weather/forecast?lat=36.8065&lon=10.1815`
- Geocode a location:
  - `GET /api/location/geocode?address=Tunis`
- Google Maps page in the frontend:
  - `http://localhost:4200/map`

## 4) Run backend locally without Docker

Open a terminal in `backendComping` and run:

```bash
./mvnw spring-boot:run
```

Or on Windows:

```powershell
mvnw.cmd spring-boot:run
```

Backend runs on port `8087`.

## 5) Run frontend locally without Docker

Open a terminal in `frontendComping/frontendCompingApp` and run:

```bash
npm install
npm start
```

Then open:
- `http://localhost:4200`

## 6) Test the backend

Run the unit test for the weather service:

```bash
./mvnw test
```

Or on Windows:

```powershell
mvnw.cmd test
```

## 7) Build the frontend

From `frontendComping/frontendCompingApp`:

```bash
npm run build
```

## 8) Common issues

### Frontend does not open on port 4200
- Rebuild the Docker image after changing the frontend Dockerfile.
- Make sure the frontend container is exposing port `4200:4200`.

### Weather or map calls fail
- Check that both API keys are set.
- Verify Google Maps Geocoding API and OpenWeatherMap are enabled.

### CORS errors
- The backend already allows requests from `http://localhost:4200`.

## 9) Recommended startup order

1. Set the API keys.
2. Start Docker Compose.
3. Open `http://localhost:4200`.
4. Go to `/map`.

## 10) Summary

If everything is configured correctly, the full flow is:
- Angular frontend on `4200`
- Spring Boot backend on `8087`
- MongoDB in Docker
- Weather and geocoding features working through the backend APIs
