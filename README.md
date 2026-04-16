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