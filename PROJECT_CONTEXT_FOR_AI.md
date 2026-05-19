# Campino - Project Specifications & Context for AI Agent

## 1. Project Overview
**Campino** is a full-stack platform for camping management, social networking, and e-commerce. It connects campers with organizers, provides a marketplace for gear, and uses AI for safety and planning.

## 2. Technical Stack
### Backend
- **Framework:** Spring Boot 3.3.5 (Java 17)
- **Database:** MongoDB (Local/Atlas)
- **Security:** Spring Security + JWT
- **Communication:** 
  - REST APIs
  - WebSockets (STOMP) for Real-time Chat
  - WebRTC Signaling for P2P calls
- **External Integrations:**
  - **Stripe:** Payment processing
  - **Cloudinary:** Image/Video storage
  - **Vosk:** Offline voice-to-text (French model)
  - **Ollama:** Local LLM for chatbot/recommendations
  - **External AI API:** Flask/FastAPI at `http://localhost:5000/predict` for specialized predictions

### Frontend
- **Framework:** Angular 19
- **State Management:** RxJS
- **Styling:** CSS / Bootstrap
- **Key Libraries:** `@stripe/stripe-js`, `ngx-toastr`, `jspdf`

## 3. Architecture
- **Layered Backend:** `tn.comping.spring.backendcomping`
  - `config/`: Security, WebSockets, Cloudinary, Stripe, Swagger.
  - `controllers/`: REST Endpoints.
  - `services/`: Business logic.
  - `entities/`: MongoDB Documents.
  - `dto/`: Request/Response objects.
  - `repositories/`: MongoRepository interfaces.
- **Component-based Frontend:** `src/app/`
  - `admin/`: Admin-specific views (Dashboard, User management).
  - `client/`: User-facing features (Events, Activities).
  - `marketplace/`: Product listing, Cart, Checkout.
  - `services/`: API wrappers and state logic.

## 4. Key Features & Implementation Status

### Core Modules
- **Auth & Profiles:** Full JWT auth, role-based access (Admin, Organizer, Client, Livreur).
- **Marketplace:** Product catalog, Shopping cart, Stripe checkout, Order history.
- **Events & Activities:** CRUD for camping events, user participation, AI-based recommendations.
- **Social Network:** Post feed, comments, reactions, and user-to-user messaging.

### AI & Planning (Advanced)
- **AI Checklists:** Generates gear lists based on trip difficulty/weather.
- **Emergency Chatbot:** Safety-focused AI assistant using local LLM.
- **Incident Management:** Reporting system with AI-driven impact analysis.
- **Team Assignment:** Algorithmic matching of campers to teams based on skill.

### Real-time Communication
- **Chat:** One-to-one and group messaging (WebSocket).
- **Voice Messages:** Support for audio uploads with server-side transcription (Vosk).
- **WebRTC Calls:** (Partially Implemented) Signaling logic is present; P2P connection needs completion.

## 5. Main Route Mapping (Angular)
- `/Campino`: Landing Page
- `/marketplace`: Product Listing
- `/dashboard`: User Personal Space
- `/admin/dashboard`: Admin/Organizer Back-office
- `/sorties`: Trip/Excursion discovery
- `/checklist-ia`: AI Gear Planner
- `/panier` -> `/command` -> `/confirm-order`: E-commerce Flow

## 6. Development Context
- **Server Port:** 8087
- **Database Name:** `Comping`
- **Profiles:** `application.properties` contains Stripe keys, Cloudinary credentials, and AI API URLs.
- **Critical Files:**
  - `SecurityConfig.java`: Handles all API permissions.
  - `ChatWebSocketConfig.java`: Manages real-time message routing.
  - `app.routes.ts`: Definitive list of frontend entry points.

## 7. Instructions for Future Implementation
1. **WebRTC:** Complete the peer-to-peer connection logic in the frontend chat component.
2. **Vosk Integration:** Ensure the voice model is downloaded and placed in `src/main/resources/`.
3. **AI Fine-tuning:** Enhance the `AiRecommendationController` logic to use actual user interaction data from the `Interaction` entity.
4. **Mobile Responsiveness:** Many frontend components need final CSS adjustments for mobile devices.
