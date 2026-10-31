#!/usr/bin/env bash
# Deploy na VM: pobiera gotowe obrazy z GHCR i restartuje stack. Niczego nie buduje.
#
# Użycie (na VM, w katalogu repo):
#   ./scripts/deploy.sh                     - najnowsze obrazy (tag latest)
#   IMAGE_TAG=sha-abc1234 ./scripts/deploy.sh   - konkretna wersja (np. rollback)

set -euo pipefail
cd "$(dirname "$0")/.."

# Tylko plik produkcyjny - override z buildem jest dla pracy lokalnej
compose() { docker compose -f docker-compose.yml "$@"; }

[ -f .env ] || { echo "Brak pliku .env (co najmniej POSTGRES_PASSWORD=...)"; exit 1; }

# compose, nginx/default.conf i init.sql pochodzą z repo
git pull --ff-only

compose pull
compose up -d --remove-orphans

# Stare wersje obrazów zajmują dysk
docker image prune -f

compose ps
