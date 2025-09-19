#!/usr/bin/env bash
set -euo pipefail

MONGO_NAME="mongo-local"
MONGO_IMAGE="mongo:6"
MONGO_PORT="27017"

echo "==> 1) Mongo (Docker)"
if ! docker ps --format '{{.Names}}' | grep -qx "$MONGO_NAME"; then
  if docker ps -a --format '{{.Names}}' | grep -qx "$MONGO_NAME"; then
    docker start "$MONGO_NAME" >/dev/null
  else
    docker run -d --name "$MONGO_NAME" -p ${MONGO_PORT}:27017 -v mongo_data:/data/db "$MONGO_IMAGE" >/dev/null
  fi
fi
echo "    OK -> mongodb://localhost:${MONGO_PORT}"

echo "==> 2) Backend (Quarkus hot reload)"
(
  cd backend
  MVN="./mvnw"; [ -x "$MVN" ] || MVN="mvn"
  $MVN -q -DskipTests clean install || true
  $MVN -q quarkus:dev
) &
BACK_PID=$!

echo "==> 3) Frontend (Vite hot reload)"
(
  cd frontend
  if [ ! -f .env ]; then
    cat > .env <<EOF
VITE_API_BASE=http://localhost:8080
VITE_APP_BASE_URL=http://localhost:5173
EOF
  fi
  [ -d node_modules ] || npm i
  npm run dev
) &
FRONT_PID=$!

echo
echo "========================================"
echo " Frontend: http://localhost:5173"
echo " Backend : http://localhost:8080  (Swagger: /q/swagger-ui)"
echo " Mongo   : mongodb://localhost:${MONGO_PORT}"
echo "Ctrl+C pour arrêter."
echo "========================================"
echo

wait $BACK_PID $FRONT_PID
