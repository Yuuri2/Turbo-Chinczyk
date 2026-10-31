#syntax=docker/dockerfile:1
FROM node:22.21-alpine AS build
WORKDIR /app

COPY package.json package-lock.json ./
RUN --mount=type=cache,target=/root/.npm npm ci

COPY . .
#Po buildzie zostawiamy w node_modules tylko zaleznosci produkcyjne (w praktyce: pg)
RUN npm run build && npm prune --omit=dev


FROM node:22.21-alpine
LABEL org.opencontainers.image.source=https://github.com/Yuuri2/Turbo-Chinczyk
WORKDIR /app
ENV NODE_ENV=production

COPY --from=build --chown=node:node /app/package.json ./
COPY --from=build --chown=node:node /app/node_modules ./node_modules
COPY --from=build --chown=node:node /app/build ./build

USER node
EXPOSE 3000
CMD ["node", "build"]
