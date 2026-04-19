# voting

# Voting App

## Mejoras técnicas

### Cambio 1 — Eliminación de N+1 query en nominees (`NomineeProjection`)

**Problema:** `PublicRoundController` hacía 1 query para traer las nominations y luego
1 query adicional por cada nomination para buscar el contestant asociado.
Con 10 nominados se ejecutaban 11 queries innecesarias.

**Solución:** Se creó `NomineeProjection` en `repo/projection` y se agregó el método
`findNomineesByRoundId` en `NominationRepository` con un JOIN explícito que resuelve
todo en una sola query.

**Aprendizaje:** Spring Data Projection es una interfaz que Spring implementa en runtime
para mapear resultados de queries con JOIN directamente a un tipo, sin necesidad de
instanciar una clase concreta. Vive en la capa repositorio y no debe exponerse fuera de ella.

---

### Cambio 2 — Eliminación de `Object[]` con `VoteCountProjection`

**Problema:** `VoteRepository` regresaba `List<Object[]>` y `ResultsService` accedía
a los campos por índice (`r[0]`, `r[1]`, `r[2]`). Esto es frágil porque si se cambia
el orden de campos en el `@Query` el código compila pero falla en runtime.

**Solución:** Se creó `VoteCountProjection` en `repo/projection` con getters explícitos
`getContestantId()`, `getContestantName()` y `getVoteCount()`. El `@Query` usa alias
que coinciden con los nombres de los getters para que Spring Data pueda hacer el mapeo.

**Aprendizaje:** Siempre preferir tipos explícitos sobre `Object[]` en queries. Los alias
en el `@Query` deben coincidir exactamente con los getters de la Projection sin el prefijo `get`.

Cómo quedó la separación de responsabilidades
PublicRoundController
  → recibe request
  → llama nominationService.getNominees()
  → regresa List<NomineeResponse>

NominationService
  → valida que la ronda exista
  → llama nominationRepository.findNomineesByRoundId()
  → mapea Projection → DTO
  → regresa List<NomineeResponse>

NominationRepository
  → ejecuta el JOIN en base de datos
  → regresa List<NomineeProjection>

### Cambio 3 — Controllers delgados, lógica en Services

**Problema:** `PublicRoundController` accedía directamente a `NominationRepository`
saltándose la capa de Service, y además hacía el mapeo de Projection a DTO dentro
del controller.

**Solución:** Se movió la lógica al método `getNominees()` en `NominationService`.
El controller ahora solo recibe el request, llama al service y regresa el response.

**Regla:** Un controller solo hace tres cosas:
1. Recibir el request
2. Llamar un service
3. Regresar el response

Nunca accede directamente a un repository ni transforma datos.

### Cambio 4 — Manejo de errores consistente

**Problema:** Spring regresaba dos formatos distintos para errores 400. Los errores
de `@Valid` usaban el formato default de Spring y los errores de negocio usaban
el formato de `GlobalExceptionHandler`. Un cliente del API no puede manejar
errores de forma consistente si el formato cambia según el origen del error.

**Solución:**
- Se creó `ErrorResponse` como record con campos fijos: `timestamp`, `status`,
  `error`, `message`. Reemplaza el `Map<String, Object>` que no tiene estructura
  garantizada.
- Se agregó `handleValidation()` en `GlobalExceptionHandler` que captura
  `MethodArgumentNotValidException` (errores de `@Valid`) y los convierte al
  mismo formato que el resto de errores.

**Regla:** Todos los errores del API deben tener el mismo formato sin importar
su origen. El cliente no debe adivinar qué estructura esperar.

### Cambio 5 — Logs estructurados con SLF4J

**Problema:** Sin logs en los services era imposible debuggear problemas en
producción. `docker compose logs voting-service` solo mostraba el banner
de Spring Boot.

**Solución:** Se agregó `Logger` de SLF4J en `VotingService`, `RoundService`
y `NominationService` siguiendo esta convención de niveles:

- `INFO`  → operaciones exitosas importantes: voto registrado, ronda abierta
- `WARN`  → algo inesperado pero recuperable: contestant ya nominado, ronda no abierta
- `ERROR` → fallos que no deberían ocurrir
- `DEBUG` → detalle útil solo en desarrollo, no en producción

**Patrón usado:**
```java
private static final Logger log = LoggerFactory.getLogger(VotingService.class);
log.info("Voto registrado: roundId={}, contestantId={}", roundId, contestantId);
```

Los `{}` son placeholders que SLF4J reemplaza en runtime. Nunca concatenar
strings en logs porque se evalúa aunque el nivel esté desactivado.

### Cambio 6 — Spring Security + JWT

**Problema:** Todos los endpoints admin eran públicos. Cualquiera con Postman
podía crear seasons, abrir o cerrar rondas sin ninguna restricción.

**Solución:** Se implementó autenticación stateless con JWT y Spring Security.

**Archivos nuevos:**
- `JwtService` — genera y valida tokens JWT con firma HMAC-SHA256
- `JwtAuthFilter` — intercepta cada request, extrae y valida el token del
  header `Authorization: Bearer <token>`
- `SecurityConfig` — define qué rutas son públicas y cuáles requieren rol ADMIN
- `AuthService` — lógica de registro y login con BCrypt para hashear passwords
- `AuthController` — endpoints `POST /auth/register` y `POST /auth/login`
- `AppUser` + `UserRepository` — entidad de usuario con rol en base de datos
- `V2__add_users.sql` — migración Flyway para la tabla `app_user`

**Flujo:**
1. `POST /auth/register` con username, password y role → regresa token
2. `POST /auth/login` con credenciales → regresa token
3. Requests a `/api/v1/admin/**` requieren header `Authorization: Bearer <token>`
4. `/api/v1/public/**` y `/actuator/health` siguen siendo públicos

**Conceptos clave:**
- `SessionCreationPolicy.STATELESS` — el servidor no guarda sesión, cada
  request es independiente y lleva su propio token
- BCrypt — nunca se guarda el password en texto plano, el hash incluye
  salt interno resistente a rainbow table attacks
- El token JWT contiene username y role en el payload, firmado con HMAC-SHA256.
  El servidor valida la firma sin consultar la base de datos en cada request

  ### Cambio 7 — HTTPS con Let's Encrypt y Nginx reverse proxy

**Problema:** La app era accesible solo con IP y puertos expuestos directamente:
`http://52.55.67.197:3000` y `http://52.55.67.197:8080`. Sin HTTPS, sin dominio,
sin aspecto profesional.

**Solución:**
1. Dominio gratuito `ciberaccion-voting-app.duckdns.org` apuntando a la Elastic IP
2. Certificado SSL gratuito de Let's Encrypt obtenido con Certbot
3. Nginx instalado en EC2 como reverse proxy que:
   - Redirige HTTP → HTTPS automáticamente
   - Enruta `/api/` y `/auth/` al backend en `localhost:8080`
   - Enruta `/` al frontend en `localhost:3000`
   - Expone solo los puertos 80 y 443 al exterior

**Resultado:**
- `https://ciberaccion-voting-app.duckdns.org` → frontend
- `https://ciberaccion-voting-app.duckdns.org/api/` → backend
- `https://ciberaccion-voting-app.duckdns.org/auth/` → auth
- Certificado se renueva automáticamente antes de expirar

**Notas:**
- Certbot expira 2026-07-17 pero tiene renovación automática configurada
- Nginx corre fuera de Docker directamente en EC2
- Los puertos 8080 y 3000 pueden cerrarse del Security Group
  ya que el tráfico ahora entra por 443

 ### Pendiente — Credenciales fuera de docker-compose.yml

**Problema:** Las credenciales de postgres y el JWT secret están hardcodeadas
en `docker-compose.yml` y `application.properties`, lo cual es un riesgo de
seguridad si el repo es público.

**Solución recomendada:**
1. Crear un archivo `.env` en la raíz del proyecto con las credenciales
2. Agregar `.env` al `.gitignore` para que nunca se suba al repo
3. Referenciar las variables en `docker-compose.yml` con `${VARIABLE}`
4. En EC2 crear el `.env` manualmente una sola vez

**Ejemplo `.env`:** 