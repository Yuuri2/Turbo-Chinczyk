## Komendy
`sudo docker compose up -d` odpalamy z katalogu /config
`sudo docker compose down -v` wyłaczenie dockera wraz z usunięciem konteneru
`sudo docker logs chinczyk_db --tail 50` ostatnie 50 logów chinczyk_db
`sudo docker ps -a` sprawdzenie działających kontenerów

```
Jest nowy commit z dockera 
POLECAM:
1. sudo docker compose down -v
2. sudo docker compose up -d --build
3. sudo docker compose ps -a
```

Baza porty: 5432:5432
