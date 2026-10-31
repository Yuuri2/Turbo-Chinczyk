#!/bin/bash
# Lokalny rebuild całego stacku (docker-compose.yml + override z buildem).
# Użycie: ./rebuild.sh              - dane w bazie zostają
#         ./rebuild.sh --reset-db   - usuwa też wolumen bazy (init.sql wykona się od nowa)

set -e
cd "$(dirname "$0")"

if [ "${1:-}" = "--reset-db" ]; then
  sudo docker compose down -v --remove-orphans
else
  sudo docker compose down --remove-orphans
fi

sudo docker compose up -d --build
sudo docker compose ps -a
