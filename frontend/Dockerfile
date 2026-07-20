# build
FROM node:22-alpine AS build
WORKDIR /app

COPY package.json package-lock.json .
RUN npm ci

COPY . .
RUN npm run build

# --- Etap 2: runtime ---
FROM node:22-alpine
WORKDIR /app
ENV NODE_ENV=production

# W runtime potrzebne tylko zaleznosci produkcyjne (w praktyce: pg)
COPY package.json package-lock.json .
RUN npm ci --omit=dev

COPY --from=build /app/build ./build

USER node
EXPOSE 3000
CMD ["node", "build"]
