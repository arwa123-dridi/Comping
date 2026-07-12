# Comping — Module Logistique

Application de gestion de camping (Spring Boot + MongoDB + Angular). Ce document couvre uniquement le module **logistique** (demandes de transport, incidents, notifications, notation, IA legere) et l'infrastructure associee.

## Architecture

- `backendComping/` — Spring Boot 3.3.5 (Java 17), MongoDB, JWT, port `8087`.
- `frontendComping/frontendCompingApp/` — Angular 19 (standalone components), port `4200`.
- `monitoring/` — configuration Prometheus + Grafana.
- `k8s/` — manifests Kubernetes (cible : Minikube).
- `Jenkinsfile` — pipeline CI/CD (build, test, image Docker, push Docker Hub, deploiement).

## Fonctionnement logistique

- **Campeur** : cree/modifie/annule des demandes de transport et des incidents (`/dashboard/logistique/...`), consulte son historique, note le service une fois la demande livree.
- **Organisateur** : traite les demandes/incidents (`/admin/logistique/...`), change le statut, ajoute un commentaire, assigne un creneau de livraison. Une popup affiche le detail au clic.
- **Notifications** : entite Mongo + polling frontend (30s) via la cloche dans l'en-tete. Creation → notifie le role ORGANISATEUR ; traitement → notifie le campeur.
- **IA legere** : systeme expert a regles ponderees (`IncidentPriorityServiceImpl`) qui classe la priorite d'un incident (type + mots-cles + historique), et une heuristique de suggestion de creneau (`CreneauSuggestionServiceImpl`) basee sur la disponibilite et la charge.

## Lancer en local (sans Docker)

```bash
# Backend (necessite un MongoDB local sur 27017)
cd backendComping
./mvnw spring-boot:run

# Frontend
cd frontendComping/frontendCompingApp
npm install
npm start
```

## Lancer avec Docker Compose

```bash
cp .env.example .env   # renseigner les secrets reels
docker compose up --build
```

- Frontend : http://localhost:4200
- Backend : http://localhost:8087 (Swagger : `/swagger-ui.html`)
- Prometheus : http://localhost:9090
- Grafana : http://localhost:3000 (admin / admin), dashboard "Comping Backend" pre-provisionne.

## Deployer sur Kubernetes (Minikube)

```bash
minikube start
cp k8s/backend-secret.example.yaml k8s/backend-secret.yaml   # renseigner les secrets reels
kubectl apply -f k8s/backend-secret.yaml
kubectl apply -f k8s/mongo.yaml -f k8s/backend.yaml -f k8s/frontend.yaml -f k8s/prometheus.yaml -f k8s/grafana.yaml
minikube service frontend
```

Remplacer `<dockerhub-user>` dans `k8s/backend.yaml` et `k8s/frontend.yaml` par le compte Docker Hub utilise.

## Pipeline Jenkins

Le `Jenkinsfile` attend un agent avec Docker + kubectl, et un credential Jenkins `dockerhub-creds` (username/password Docker Hub). Il build/teste le backend et le frontend, construit et pousse les images `<dockerhub-user>/comping-backend` et `<dockerhub-user>/comping-frontend`, puis deploie sur `main`.
