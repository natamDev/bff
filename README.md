# Bets App — Local Dev (Simple)

Stack: **MongoDB (Docker)** + **Quarkus** + **Vue 3 (Vite)**

## Démarrage (Git Bash conseillé sous Windows)
```bash
# 1) Donner les droits d'exécution au script (une fois)
chmod +x run-local.sh

# 2) Lancer tout (Mongo + Backend + Front en hot reload)
./run-local.sh

# Frontend : http://localhost:5173
# Backend  : http://localhost:8080  (Swagger: /q/swagger-ui)
# Mongo    : mongodb://localhost:27017
```
### Prérequis
- Docker installé (`docker ps` doit fonctionner).
- Java 21 (OpenJDK).
- Node 18+.

## Structure
```
backend/   # Quarkus + Panache Mongo (API)
frontend/  # Vue 3 + Vite (UI)
run-local.sh
```
