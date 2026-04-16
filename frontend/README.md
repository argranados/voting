# Frontend MVP

Frontend simple con HTML, CSS, Bootstrap, JavaScript vanilla y Nginx en Docker.

## Qué hace

- Ver health del backend
- Ver ronda actual
- Ver nominados
- Votar
- Ver resultados
- Crear round
- Nominar contestants
- Abrir y cerrar round

## Uso local con Docker Compose

Agrega este servicio a tu `docker-compose.yml` principal:

```yaml
  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    container_name: voting-frontend
    depends_on:
      - voting-service
    ports:
      - "3000:80"
```

## Acceso

- Frontend: http://localhost:3000
- Backend: http://localhost:8080
