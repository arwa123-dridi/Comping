# Guide de démarrage — Comping

Ce guide couvre, de A à Z : le clonage du projet, le lancement complet de l'application (frontend + backend + base de données + monitoring), et la mise en route du pipeline Jenkins. Il est écrit pour quelqu'un qui n'a jamais touché ce projet et doit pouvoir le lancer seul, sans mauvaise surprise le jour de la présentation.

Tout ce qui est décrit ici a été testé et validé sur un cluster **Kubernetes de Docker Desktop** (pas Minikube — voir encadré plus bas si tu vois une différence avec l'ancien `README.md`).

---

## 1. Vue d'ensemble du projet

Comping est une application de gestion de camping/randonnées, composée de 3 briques principales :

| Brique | Techno | Rôle | Port |
|---|---|---|---|
| `backendComping/` | Spring Boot 3.3.5 (Java 17) + MongoDB | API REST, authentification JWT, logique métier | 8087 |
| `frontendComping/frontendCompingApp/` | Angular 19 (standalone components) | Interface web (campeur + organisateur/admin) | 4200 (dev) / 80 dans le conteneur |
| MongoDB 7 | — | Base de données | 27017 |

Autour de ça :

- **`monitoring/`** : configuration Prometheus (scrape des métriques Spring Actuator) + dashboard Grafana pré-fait.
- **`k8s/`** : manifests Kubernetes pour déployer les 5 services (mongo, backend, frontend, prometheus, grafana) sur un cluster.
- **`docker-compose.yml`** : alternative plus simple à Kubernetes pour lancer tout en local en une commande.
- **`Jenkinsfile`** : pipeline CI/CD (build + test des deux apps, build des images Docker, push sur Docker Hub, déploiement sur `main`).

**Fonctionnement métier (module logistique)** :
- Un **campeur** crée des demandes de transport et déclare des incidents (`/dashboard/logistique/...`).
- Un **organisateur/admin** les traite depuis son back-office (`/admin/logistique/...`) : change le statut, commente, planifie.
- Un système de notifications (polling 30s) prévient l'organisateur d'une nouvelle demande, et le campeur quand elle est traitée.
- Une petite IA à règles pondérées classe automatiquement la priorité des incidents et suggère des créneaux de livraison.

---

## 2. Prérequis à installer

| Outil | Pourquoi | Où l'avoir |
|---|---|---|
| **Git** | Cloner le repo | git-scm.com |
| **Docker Desktop** | Construire les images, faire tourner MongoDB/Prometheus/Grafana, et fournir le cluster Kubernetes | docker.com |
| **Kubernetes activé dans Docker Desktop** | C'est le cluster qu'on utilise (Settings → Kubernetes → *Enable Kubernetes* → Apply & Restart) | inclus dans Docker Desktop |
| **kubectl** | Piloter le cluster | installé automatiquement par Docker Desktop |
| **Node.js 20 LTS** + npm | Lancer/builder le frontend Angular | nodejs.org |
| **Java 17 (JDK)** | Lancer/builder le backend en dehors de Docker (optionnel si tu restes 100% Docker/k8s) | adoptium.net |
| Un compte **Docker Hub** (gratuit) | Seulement nécessaire pour la partie Jenkins (push d'images) | hub.docker.com |

Vérifie que Kubernetes tourne bien sur Docker Desktop (et pas Minikube ou un autre cluster) :

```bash
kubectl config current-context
# doit afficher : docker-desktop

kubectl get nodes
# doit afficher un nœud "docker-desktop" en état Ready
```

> **Différence avec l'ancien `README.md`** : ce fichier mentionne Minikube. Cette session a été validée sur le Kubernetes intégré à Docker Desktop, ce qui est plus simple (pas de `minikube start`, pas de `eval $(minikube docker-env)` — Docker Desktop partage déjà son daemon Docker avec le cluster). Les commandes ci-dessous utilisent ce mode.

---

## 3. Cloner le projet

```bash
git clone <url-du-repo> Comping
cd Comping
```

---

## 4. Autocomplétion des adresses (OpenStreetMap / Nominatim)

Le formulaire "Nouvelle demande de transport" (`Demande-transport/nouvelle-demande-transport/`) utilise l'API publique **Nominatim** (OpenStreetMap) pour suggérer des adresses réelles sur les champs "Adresse de départ" et "Adresse d'arrivée" (limité à la Tunisie via `countrycodes=tn`). C'est un appel API externe côté navigateur, indépendant du backend Spring.

**Aucune clé API, aucun compte, aucune facturation à configurer** — c'est un service public gratuit. C'est pour ça qu'on l'a préféré à Google Maps (qui exige une carte bancaire même pour l'usage gratuit).

Implémentation :
- `src/app/services/nominatim.service.ts` — appelle `https://nominatim.openstreetmap.org/search`.
- `src/app/shared/address-autocomplete/address-autocomplete.component.ts` — composant réutilisable (`<app-address-autocomplete formControlName="...">`) qui affiche une liste de suggestions sous le champ, avec un **debounce de 500ms** et un minimum de 3 caractères avant d'interroger l'API (limite de la politique d'usage de Nominatim : 1 requête/seconde max — voir [operations.osmfoundation.org/policies/nominatim](https://operations.osmfoundation.org/policies/nominatim/)).

⚠️ Si l'API est temporairement indisponible ou en cas de coupure réseau, le champ reste utilisable en **saisie libre classique** — l'appel échoue silencieusement (`catchError`), il ne bloque jamais l'envoi du formulaire.

Rien à configurer avant de lancer l'app — cette section existe surtout pour expliquer d'où viennent les suggestions d'adresse si tu regardes le code.

---

## 5. Configurer les secrets

Le projet a besoin de quelques secrets (mail SMTP pour les notifications, Stripe pour le paiement, Cloudinary pour l'upload d'images, une clé JWT pour l'authentification). Aucun n'est obligatoire pour faire fonctionner l'essentiel de l'app (sorties, équipes, logistique) — seul **`JWT_SECRET`** est critique.

Deux fichiers d'exemple existent, à copier et compléter :

- `.env.example` → `.env` (utilisé par **Docker Compose**)
- `k8s/backend-secret.example.yaml` → `k8s/backend-secret.yaml` (utilisé par **Kubernetes**)

⚠️ **Piège connu** : ne jamais laisser `JWT_SECRET` vide dans `k8s/backend-secret.yaml`. Une chaîne vide écrase la valeur par défaut codée dans l'app (contrairement à une clé absente), et la signature JWT plante avec une erreur "clé de 0 bit". La valeur par défaut fournie dans le fichier d'exemple (`compingSecretKeyForJWTMustBe256BitsLongAtLeast!!`) suffit très bien pour une démo.

---

## 6. Option A — Lancer avec Docker Compose (le plus simple)

Pas besoin de Kubernetes ici, une seule commande :

```bash
cp .env.example .env
# éditer .env si besoin (JWT_SECRET a déjà une valeur par défaut dans le code)
docker compose up --build
```

Accès :
- Frontend : http://localhost:4200
- Backend : http://localhost:8087 (Swagger : `/swagger-ui.html`)
- Prometheus : http://localhost:9090
- Grafana : http://localhost:3000 (identifiants `admin` / `admin`)

C'est l'option la plus simple pour un test rapide, mais **l'option B (Kubernetes) est celle qui a été testée pour la présentation** et qui correspond à ce qui tourne actuellement.

---

## 7. Option B — Lancer sur Kubernetes (Docker Desktop) — recommandé pour la présentation

### 7.1 Construire les images Docker en local

Comme on ne pousse pas forcément sur Docker Hub pour une démo locale, on construit les images directement — Docker Desktop les rend immédiatement visibles au cluster (même daemon Docker) :

```bash
docker build -t comping-backend:latest ./backendComping
docker build -t comping-frontend:latest ./frontendComping/frontendCompingApp
```

⚠️ **Piège connu** : l'étape `ng build` télécharge les polices Google (Google Fonts) pendant le build du frontend ("inlining"). Si le réseau est instable à ce moment précis, le build échoue une fois avec une erreur du type `Inlining of fonts failed / socket hang up`. **Il suffit de relancer la même commande** — ça a été le cas pendant cette session et le 2ᵉ essai a fonctionné.

### 7.2 Adapter les manifests Kubernetes pour un usage 100% local

Les fichiers `k8s/backend.yaml` et `k8s/frontend.yaml` référencent par défaut une image `<dockerhub-user>/comping-backend:latest` (pensée pour un déploiement via Jenkins qui pousse sur Docker Hub). Pour un lancement **local sans Docker Hub**, il faut éditer ces deux fichiers.

Dans `k8s/backend.yaml`, remplacer :
```yaml
image: <dockerhub-user>/comping-backend:latest
```
par :
```yaml
image: comping-backend:latest
imagePullPolicy: IfNotPresent
```

Faire la même chose dans `k8s/frontend.yaml` avec `comping-frontend:latest`.

> **Pourquoi c'est indispensable** : sans `imagePullPolicy: IfNotPresent`, Kubernetes considère qu'une image taguée `:latest` doit *toujours* être retéléchargée depuis un registre distant. Comme `<dockerhub-user>` n'est pas un vrai compte, ça se solde par une erreur `ErrImagePull` / `ImagePullBackOff` — le pod ne démarre jamais. C'est le piège n°1 pour quelqu'un qui clone le repo et fait juste `kubectl apply` sans cette étape.

### 7.3 Configurer le secret Kubernetes

```bash
cp k8s/backend-secret.example.yaml k8s/backend-secret.yaml
# éditer k8s/backend-secret.yaml si besoin (voir section 5)
kubectl apply -f k8s/backend-secret.yaml
```

### 7.4 Déployer tous les services

```bash
kubectl apply -f k8s/mongo.yaml -f k8s/backend.yaml -f k8s/frontend.yaml -f k8s/prometheus.yaml -f k8s/grafana.yaml
```

### 7.5 Vérifier que tout tourne

```bash
kubectl get pods
```

Attendre que tous les pods soient `1/1 Running` (le tout premier démarrage de MongoDB peut prendre 30s à 1 min).

### 7.6 Accéder à l'application

Le frontend est exposé directement via un NodePort :
- **Frontend** : http://localhost:30080
- **Grafana** : http://localhost:30300 (`admin` / `admin`)

Le **backend n'est pas exposé** en NodePort (accès interne au cluster uniquement). Or le frontend Angular appelle directement `http://localhost:8087` depuis le navigateur de l'utilisateur (pas de proxy nginx vers l'API à ce jour). Il faut donc ouvrir un tunnel :

```bash
kubectl port-forward svc/backend 8087:8087
```

⚠️ **Point d'attention important** : ce tunnel doit rester **ouvert dans un terminal** pendant toute la démo. S'il s'interrompt — ou si le pod backend redémarre (redéploiement, crash...) — le port-forward meurt, et le frontend affiche alors une erreur du type **"Serveur inaccessible (port 8087)"**, qui donne l'impression que "le backend est down" alors qu'il tourne parfaitement côté cluster (c'est arrivé plusieurs fois pendant cette session).

**Solution recommandée pour la démo** : au lieu de la commande simple ci-dessus, utiliser une boucle qui relance automatiquement le tunnel s'il tombe, et la laisser tourner dans un terminal dédié pendant toute la présentation :

```bash
# bash / Git Bash
while true; do kubectl port-forward svc/backend 8087:8087; echo "Port-forward interrompu, relance dans 2s..."; sleep 2; done
```

```powershell
# PowerShell
while ($true) { kubectl port-forward svc/backend 8087:8087; Write-Host "Port-forward interrompu, relance dans 2s..."; Start-Sleep -Seconds 2 }
```

Avec cette boucle, un redémarrage du pod backend (ou une coupure ponctuelle) se répare tout seul en 2 secondes, sans intervention manuelle.

(Même logique pour Mongo si besoin d'y accéder directement en local : `kubectl port-forward svc/mongo 27017:27017`.)

---

## 8. Mettre à jour l'application après une modification de code

Contrairement à `ng serve` ou `mvnw spring-boot:run` en local, les pods Kubernetes tournent sur une **image Docker figée** au moment du build. Toute modification de code (frontend ou backend) ne sera visible dans l'app **qu'après** :

```bash
# Si le backend a changé :
docker build -t comping-backend:latest ./backendComping
kubectl rollout restart deployment/backend
kubectl rollout status deployment/backend

# Si le frontend a changé :
docker build -t comping-frontend:latest ./frontendComping/frontendCompingApp
kubectl rollout restart deployment/frontend
kubectl rollout status deployment/frontend
```

Puis :
1. Si le pod backend a redémarré et que le port-forward n'est **pas** lancé en boucle auto-relançable (section 7.6), le relancer manuellement.
2. Faire un **hard refresh** du navigateur (Ctrl+Shift+R) pour vider le cache du bundle JS Angular — sinon l'ancien code continue de s'afficher même si le nouveau pod tourne.

---

## 9. Dépannage — problèmes déjà rencontrés

| Symptôme | Cause réelle | Solution |
|---|---|---|
| `GET /actuator/health` renvoie `DOWN` alors que l'app répond normalement | Le `MailHealthIndicator` de Spring échoue car aucun mot de passe d'application Gmail valide n'est configuré (SMTP) | Sans impact réel : les probes k8s (`/actuator/health/readiness` et `/liveness`) restent `UP`. Pour supprimer ce faux négatif, configurer un vrai `MAIL_PASSWORD` (mot de passe d'application Gmail) dans le secret — sinon, l'ignorer, ce n'est pas bloquant. |
| `403 Forbidden` sur des routes comme `/api/demandes-transport/me` | Token JWT expiré, ou utilisateur pas connecté (pas de token dans le `localStorage` du navigateur) | Se reconnecter. La durée de vie du token a été portée de 10 minutes à **24h** (`JwtUtils.java`) pour éviter que ça arrive en pleine démo. |
| Le dashboard/sidebar affiche encore d'anciennes données figées après une modif de code | L'image Docker n'a pas été reconstruite/redéployée (section 8), ou le navigateur sert un bundle JS en cache | Rebuild + `rollout restart` + hard refresh (Ctrl+Shift+R). |
| `Serveur inaccessible (port 8087)` / erreurs réseau dans la console navigateur | Le tunnel `kubectl port-forward svc/backend 8087:8087` s'est arrêté (souvent après un redéploiement ou un crash du pod backend) | Relancer la commande de port-forward, ou mieux, utiliser la boucle auto-relançable (section 7.6) pour que ça ne se reproduise plus pendant la démo. |
| `docker build` du frontend échoue avec `Inlining of fonts failed` | Coupure réseau ponctuelle pendant le téléchargement des Google Fonts par `ng build` | Relancer simplement la même commande `docker build`. |
| Le champ adresse n'affiche aucune suggestion | Moins de 3 caractères saisis, requête Nominatim en cours (debounce 500ms), ou service OpenStreetMap temporairement indisponible/rate-limité | Attendre un instant ou continuer en saisie libre — ça ne bloque jamais l'envoi du formulaire (voir section 4). |

---

## 10. Pipeline Jenkins — lancement pas à pas

**Jenkins n'est pas nécessaire pour lancer ou présenter l'application** — les sections 7 et 8 suffisent. Cette partie sert à démontrer/valider le pipeline CI/CD si c'est demandé.

### 10.1 Ce que fait le `Jenkinsfile`

1. **Checkout** : récupère le code.
2. **Backend Build & Test** : `./mvnw -B -DskipTests=false verify` dans `backendComping/`.
3. **Frontend Build & Test** : `npm ci`, `ng build --configuration production`, `ng test` en headless Chrome (l'échec des tests ne bloque pas le pipeline, `|| true`).
4. **Docker Build** : construit `comping-backend` et `comping-frontend`, chacune taguée avec le hash du commit **et** `latest`.
5. **Docker Push** : login Docker Hub puis push des 4 tags.
6. **Deploy** *(uniquement sur la branche `main`)* : `kubectl apply` sur les 5 manifests + `kubectl set image` pour pointer vers les images fraîchement poussées.

### 10.2 Prérequis

- Un agent Jenkins qui a accès à la commande `docker` (le daemon Docker Desktop) **et** à `kubectl` (configuré sur le contexte `docker-desktop`).
- Un compte Docker Hub — nécessaire seulement pour les stages *Push*/*Deploy*.

### 10.3 Lancer un Jenkins local avec les bons outils

L'image officielle `jenkins/jenkins:lts` ne contient ni `docker` ni `kubectl`. La solution la plus simple : construire une image Jenkins un peu enrichie.

Créer un fichier `Dockerfile.jenkins` (peut rester en dehors du repo, c'est un outil local) :

```dockerfile
FROM jenkins/jenkins:lts
USER root
RUN apt-get update && apt-get install -y lsb-release curl gnupg && \
    curl -fsSL https://download.docker.com/linux/debian/gpg | gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg && \
    echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/debian $(lsb_release -cs) stable" > /etc/apt/sources.list.d/docker.list && \
    apt-get update && apt-get install -y docker-ce-cli && \
    curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl" && \
    install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl
USER jenkins
```

Puis :

```bash
docker build -t jenkins-with-tools -f Dockerfile.jenkins .
docker run -d --name jenkins -p 8080:8080 -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  jenkins-with-tools
```

### 10.4 Débloquer Jenkins

```bash
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

Ouvrir http://localhost:8080, coller le mot de passe affiché, puis installer les plugins suggérés (au minimum : *Git*, *Pipeline*, *Docker Pipeline*).

### 10.5 Ajouter les identifiants Docker Hub

*Manage Jenkins* → *Credentials* → *System* → *Global credentials* → *Add Credentials* :
- Kind : `Username with password`
- Username / Password : tes identifiants Docker Hub
- **ID : `dockerhub-creds`** ⚠️ doit correspondre exactement à `credentials('dockerhub-creds')` en haut du `Jenkinsfile`, sinon le pipeline échoue dès la 1ère variable d'environnement.

### 10.6 Donner à Jenkins l'accès au cluster Kubernetes

Copier le kubeconfig de la machine hôte dans le conteneur Jenkins :

```bash
docker cp $HOME/.kube/config jenkins:/var/jenkins_home/.kube/config
docker exec jenkins chown -R jenkins:jenkins /var/jenkins_home/.kube
```

(sous Windows/PowerShell, le kubeconfig de Docker Desktop se trouve en général dans `C:\Users\<toi>\.kube\config`.)

### 10.7 Créer le job Pipeline

*New Item* → nom `comping-pipeline` → type *Pipeline* → OK, puis :
- Pipeline → Definition : **Pipeline script from SCM**
- SCM : **Git**
- Repository URL : l'URL (ou le chemin local) de ce repo
- Branch : la branche à builder (ex. `feature/logistique-module` pour tester juste build/test/push, ou `main` pour déclencher aussi le déploiement)
- Script Path : `Jenkinsfile`

Save.

### 10.8 Avant un vrai déploiement (stage *Deploy*, branche `main` uniquement)

Remplacer `<dockerhub-user>` par le vrai identifiant Docker Hub dans `k8s/backend.yaml` et `k8s/frontend.yaml`, puis committer — sinon le `kubectl apply` du stage *Deploy* réintroduit le même piège `ErrImagePull` que celui décrit en section 7.2.

### 10.9 Lancer le build

*Build Now*, puis suivre le *Console Output* stage par stage. Erreurs fréquentes :

| Erreur | Cause | Solution |
|---|---|---|
| `mvnw: Permission denied` | Le bit exécutable du script `mvnw` n'est pas préservé par git | `chmod +x backendComping/mvnw` puis `git update-index --chmod=+x backendComping/mvnw` |
| `npm test` échoue faute de Chrome headless | Chromium absent de l'image Jenkins | Installer `chromium` dans `Dockerfile.jenkins`, ou laisser faire — le `|| true` du Jenkinsfile absorbe déjà cet échec |
| `docker: permission denied while trying to connect to the Docker daemon socket` | L'utilisateur `jenkins` du conteneur n'a pas les droits sur le socket Docker monté | Ajouter l'utilisateur au bon groupe (même GID que `docker` sur l'hôte), ou lancer le conteneur Jenkins avec un utilisateur qui a accès au socket |
| `kubectl` : `Unable to connect to the server` | Le kubeconfig copié référence des certificats par un chemin absolu de l'hôte introuvable dans le conteneur | Copier aussi les fichiers de certificats référencés, ou régénérer un kubeconfig autonome (certs inline) avant de le copier |

### 10.10 Vérifier le résultat

- Sur Docker Hub : les images `<user>/comping-backend` et `<user>/comping-frontend` doivent apparaître avec un tag correspondant au commit + `latest`.
- Si le build était sur `main` : `kubectl rollout status deployment/backend` et `deployment/frontend` doivent aboutir avec succès, et `kubectl get pods` montre les nouveaux pods créés.

---

## 11. Mémo express

```bash
# Cloner
git clone <url> Comping && cd Comping

# Secrets
cp .env.example .env
cp k8s/backend-secret.example.yaml k8s/backend-secret.yaml

# Images locales
docker build -t comping-backend:latest ./backendComping
docker build -t comping-frontend:latest ./frontendComping/frontendCompingApp
# -> éditer k8s/backend.yaml et k8s/frontend.yaml : image sans <dockerhub-user>/ + imagePullPolicy: IfNotPresent

# Déployer
kubectl apply -f k8s/backend-secret.yaml
kubectl apply -f k8s/mongo.yaml -f k8s/backend.yaml -f k8s/frontend.yaml -f k8s/prometheus.yaml -f k8s/grafana.yaml
kubectl get pods   # attendre 1/1 Running partout

# Ouvrir l'accès au backend (boucle auto-relançable, à garder ouverte !)
while true; do kubectl port-forward svc/backend 8087:8087; sleep 2; done

# Accéder à l'app
# Frontend : http://localhost:30080
# Grafana  : http://localhost:30300 (admin/admin)
```

Après toute modif de code : rebuild l'image concernée → `kubectl rollout restart deployment/<backend|frontend>` → hard refresh navigateur (le port-forward en boucle se rétablit tout seul si le backend a redémarré).
