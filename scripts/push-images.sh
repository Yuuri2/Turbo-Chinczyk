#!/usr/bin/env bash
# Ręczny build i push obrazów do GHCR - tymczasowo, do czasu pipeline'u w GitHub Actions.
# Buduje na komputerze dewelopera (nie na VM), pod platformę VM (linux/amd64).
#
# Jednorazowo: docker login ghcr.io -u <login GitHub>   (hasło = PAT z uprawnieniem write:packages)
# Użycie: ./scripts/push-images.sh            - obie usługi
#         ./scripts/push-images.sh backend    - tylko wybrana

set -euo pipefail
cd "$(dirname "$0")/.."

REGISTRY=ghcr.io/yuuri2
SHA=$(git rev-parse --short HEAD)

if [ -n "$(git status --porcelain -- backend frontend)" ]; then
  echo "Uwaga: niezacommitowane zmiany w backend/ lub frontend/ - tag sha-$SHA nie odpowiada kodowi w obrazie."
fi

[ $# -gt 0 ] || set -- backend frontend

for svc in "$@"; do
  image="$REGISTRY/turbo-chinczyk-$svc"
  echo "=== $image (latest, sha-$SHA)"
  docker buildx build "./$svc" \
    --platform linux/amd64 \
    --tag "$image:latest" \
    --tag "$image:sha-$SHA" \
    --push
done
