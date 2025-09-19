# Bets App (Frontend + Backend)

Monorepo minimal pour ton MVP **paris entre amis** (liens invités uniques, host admin, paris VRAI/FAUX).
- Frontend: **Vue 3 + TypeScript + Vite**
- Backend: **Quarkus (RESTEasy Reactive) + Panache MongoDB**

## Prérequis
- Node 18+
- Java 17+
- Maven 3.9+
- Docker (optionnel, pour MongoDB via docker-compose)

## Lancer MongoDB (option 1) — Docker
```bash
cd backend
docker compose up -d
# Mongo sera sur mongodb://localhost:27017
```

## Lancer le backend (Quarkus)
```bash
cd backend
./mvnw quarkus:dev
# API sur http://localhost:8080
# Swagger UI: http://localhost:8080/q/swagger-ui
```

## Lancer le frontend (Vue)
```bash
cd frontend
npm i
npm run dev
# Front sur http://localhost:5173
```

## Variables
- `backend/src/main/resources/application.properties` définit la connexion Mongo et le secret HMAC.
- `frontend/.env` contient `VITE_API_BASE` (par défaut `http://localhost:8080`).

## Endpoints clés (rappel)
- `POST /events` → crée un event + liens host/invités
- `GET /events/{eventId}` → vue publique
- `GET /events/{eventId}/as-invite?inviteId=...&sig=...` → vue centrée invité
- `POST /events/{eventId}/bets?sig=HOST_SIG` → ajouter un pari (host)
- `PATCH /events/{eventId}/bets/{betId}?sig=HOST_SIG` → statut `open|true|false` (host)
- `POST /events/{eventId}/bets/{betId}/predict?inviteId=...&sig=...` → pronostic invité `YES|NO`
- `GET /events/{eventId}/invites/links?sig=HOST_SIG` → lister tous les liens invités (host)
- `GET /events/{eventId}/predictions/matrix?sig=HOST_SIG` → matrix pronostics (host)
