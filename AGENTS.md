# MrCatFood — instrucciones para agentes

MrCatFood es un sistema de pedidos de sándwiches: backend REST en Spring Boot y
aplicativo móvil en Expo/React Native. La raíz del repositorio contiene el backend;
el aplicativo vive en `movil/`.

## 1. Estructura del repositorio

- `src/main/java/com/diegodev/mrcatfood/` — código del backend.
- `src/main/resources/` — `application.yaml`, `application-test.yaml` y migraciones Flyway.
- `docs/` — especificación, casos de uso, modelo de datos, backlog, planes técnico y móvil.
- `movil/` — aplicativo Expo (tiene su propio `AGENTS.md`).
- `.env.example`, `.env.produccion.example` — plantillas de variables (nunca se versiona `.env`).

## 2. Backend (raíz)

### Stack

- Java 21 (toolchain con autoaprovisionamiento mediante `foojay-resolver`).
- Spring Boot 4.1.1 (Security 7, Jackson 3, Hibernate 7).
- Gradle 9.7.1 con wrapper incluido.
- PostgreSQL 18 en desarrollo/producción; H2 (perfil `test`) para pruebas.
- Flyway para migraciones; springdoc 3.1.1 para OpenAPI.

### Comandos

```bash
./gradlew build          # compila + spotlessCheck + pruebas
./gradlew test           # solo pruebas
./gradlew spotlessApply  # corrige el formato
./gradlew spotlessCheck  # verifica el formato sin modificar
./gradlew bootRun        # levanta el backend
```

`./gradlew build` debe quedar en verde antes de cualquier PR.

### Configuración y entorno

- Copia `.env.example` a `.env` y reemplaza los valores. `.env` está ignorado por Git.
- Base local con Podman:
  ```bash
  podman run -d --name postgres \
    -e POSTGRES_USER=admin -e POSTGRES_PASSWORD=admin123 -e POSTGRES_DB=mydatabase \
    -p 5432:5432 docker.io/library/postgres:18
  ```
- Las pruebas usan el perfil `test` (H2) y no dependen de `.env` ni de PostgreSQL.

### Arquitectura y paquetes

Capas: Controller → Service → Repository → Entidad JPA. El controlador nunca toca
repositorios ni entidades; las entidades no se serializan (siempre DTOs).

Paquetes bajo `com.diegodev.mrcatfood`: `config`, `seguridad`, `usuario`, `producto`,
`pedido`, `comun`. Ver `docs/plan.md` §4 para el detalle.

### Convenciones de código

- Formato obligatorio: Spotless con `googleJavaFormat().aosp()` (4 espacios, 100 columnas).
  Ejecutar `./gradlew spotlessApply` antes de commitear.
- Variables con nombre completo (sin abreviaturas ni `data`/`info`/`tmp`).
- Funciones con verbo + sustantivo (`calcularTotalPedido`, `validarHorarioRecepcion`).
- `BigDecimal` con `setScale(2, RoundingMode.HALF_UP)` para importes; nunca punto flotante.
- Inyectar `Clock` (zona `America/Bogota`); nunca `LocalDateTime.now()` dentro de un servicio.
- Totales siempre se recalculan en el servidor.

### Seguridad y errores

- JWT HS256, stateless, `Authorization: Bearer <token>`.
- Errores en `ProblemDetail` con el campo `codigo` (ver `docs/plan.md` §6.2).
- Registro público ignora cualquier `rol` enviado y siempre crea `CLIENTE`.
- Un recurso ajeno responde 404, no 403.

### Base de datos y migraciones

- Flyway administra el esquema (`ddl-auto: validate`).
- Migraciones en `src/main/resources/db/migration/`, nombre `V{n}__descripcion.sql`.
- Nunca se edita una migración ya aplicada.

### Pruebas

- JUnit 5 + AssertJ + Mockito (unidad); `@SpringBootTest` + MockMvc + Testcontainers (integración).
- Perfil `test` con H2. Cada RN debe tener al menos una prueba que falle si se rompe.

## 3. Aplicativo móvil (`movil/`)

Tiene su propio `movil/AGENTS.md` con las reglas de Expo/React Native, comandos,
navegación y convenciones. Léelo antes de tocar código en `movil/`. No dupliques aquí
esas reglas.

## 4. Flujo de trabajo Git

- Rama estable: `main`. Rama de integración: `dev`.
- Ramas nombradas con el identificador de la tarea (`M4.5`, `M5.4`).
- Commits con Conventional Commits en español (`feat(...)`, `fix(...)`, etc.).
- Una tarea = una rama; PR con squash; sin auto-aprobación.
- Marcar la tarea como ☑ en `docs/modulos-backlog.md` al fusionar.

## 5. Reglas para agentes

- No asumas que módulos posteriores a M2 están implementados: verifica el código y el
  tablero en `docs/modulos-backlog.md` antes de usar endpoints o entidades.
- No modifiques migraciones ya aplicadas ni escribas secretos en el repositorio.
- Ejecuta `./gradlew spotlessApply` y `./gradlew build` (backend) o lint + typecheck
  (móvil) antes de declarar una tarea terminada.
- Para APIs de Expo/EAS/React Native, sigue las instrucciones de `movil/AGENTS.md`
  (consultar docs versionadas, no fiarse del entrenamiento).
- **Cierre de tarea:** al terminar, marca la tarea como ☑ Hecho en `docs/modulos-backlog.md`
  (§0 y tabla del módulo), adjunta la evidencia del build en el PR y fusiona con squash.
  Una tarea **no** se considera terminada mientras no esté marcada en el tablero ni fusionada.

## 6. Estado actual

- M1 (análisis) ☑ Hecho.
- M2 (registro de clientes) ◐ En proceso:
  - ☑ Hecho: M2.1, M2.2, M2.3, M2.4, M2.5, M2.6, M2.9.
  - ◐ En proceso: M2.10 (andamiaje móvil y pantallas de acceso).
  - ☐ No hecho: M2.7, M2.8.
- M3 a M8: ☐ No hecho.

## 7. Documentación de referencia

- `docs/especificacion-requisitos.md` — RF/RNF.
- `docs/historias-de-usuario.md` — historias con criterios de aceptación.
- `docs/casos-de-uso.md` — casos de uso.
- `docs/modelo-datos.md` — esquema de base de datos.
- `docs/modulos-backlog.md` — tablero y estándares.
- `docs/plan.md` — plan técnico del backend.
- `docs/plan-frontend.md` — plan del aplicativo móvil.
