# Módulos, backlog y estándares de desarrollo

| Campo | Valor |
|---|---|
| Proyecto | MrCatFood — sistema de pedidos de sándwiches |
| Tarea | M1.4 · Priorizar el backlog con MoSCoW |
| Versión | 1.0 |
| Estado | En revisión (Pull Request) |
| Documentos relacionados | `docs/especificacion-requisitos.md`, `docs/historias-de-usuario.md`, `docs/casos-de-uso.md`, `docs/modelo-datos.md` |

---

# 0. Estado de los módulos

Tablero oficial del avance. Cada tarea y cada módulo se marcan **Hecho**, **En proceso** o **No hecho**.

**Leyenda:** ☐ No hecho — no ha comenzado · ◐ En proceso — la rama existe y se trabaja en ella, sin fusionar · ☑ Hecho — cumple su Definition of Done y está fusionado en la rama estable

| # | Módulo | Requisitos | Tareas | Progreso | Estado |
|---|---|---|---|---|---|
| M1 | Levantamiento y análisis de requisitos | Todos | 6 | 4/6 | ◐ En proceso |
| M2 | Registro de clientes | RF-01, RNF-01 | 10 | 0/10 | ☐ No hecho |
| M3 | Ubicación: conjunto y apartamento | RF-02 | 3 | 0/3 | ☐ No hecho |
| M4 | Catálogo de sándwiches | RF-03, RF-04, RF-05 | 7 | 0/7 | ☐ No hecho |
| M5 | Gestión de pedidos | RF-06, RF-07, RF-08, RF-10, RNF-03, RNF-04 | 11 | 0/11 | ☐ No hecho |
| M6 | Notificación mediante WhatsApp | RF-09 | 5 | 0/5 | ☐ No hecho |
| M7 | Pruebas | Transversal | 9 | 0/9 | ☐ No hecho |
| M8 | Documentación | Transversal | 6 | 0/6 | ☐ No hecho |
| | **Total del proyecto** | | **57** | **4/57 (7 %)** | ◐ En proceso |

**Cerrado en este ciclo:** M1.1, M1.2 y M1.3 fusionadas en `main` · M1.4 con PR abierto hacia `dev`.

**En proceso ahora:** M1.5 Plan técnico del backend.

**Reglas del tablero**

1. Una tarea pasa a ◐ cuando su rama existe y se trabaja en ella; a ☑ cuando su Definition of Done está satisfecha y está fusionada.
2. Un módulo pasa a ☑ cuando **todas** sus tareas están fusionadas.
3. La tarea se marca en el Pull Request que la implementa; el PR no se aprueba con la tarea sin marcar.
4. Ninguna tarea pasa a ☑ sin revisión de otro integrante.
5. Este tablero es la fuente oficial del avance. Si un estado discrepa entre el tablero, el Pull Request y el código, manda el tablero.

---

# 1. Estándares

## 1.0 Repositorio y ramas

| Dato | Valor |
|---|---|
| Remoto | `origin` |
| URL | `https://github.com/DiegoBarbosaDeveloper/MrCatFood.git` |
| Rama estable | `main` |
| Rama de integración | `dev` |

**Autenticación.** El repositorio se accede por HTTPS. GitHub no acepta la contraseña de la cuenta para Git por HTTPS desde agosto de 2021: hace falta un **Personal Access Token**. Esta máquina usa `credential.helper = libsecret`, que guarda el token en el llavero del sistema para no escribirlo en el código ni en la terminal.

**Nunca** se versiona `.env`, `*.jks` ni tokens. Solo `.env.example`.

## 1.2 Priorización MoSCoW

Cada tarea del backlog tiene una prioridad. Es lo que define el orden de trabajo cuando hay que elegir.

| Nivel | Significado | Regla |
|---|---|---|
| **Must** | Obligatorio. Sin esto el sistema no cumple su propósito | Se hace siempre |
| **Should** | Importante, pero el sistema funciona sin ello | Se hace si el tiempo alcanza |
| **Could** | Deseable a futuro | Solo si sobra tiempo |
| **Won't** | Fuera de alcance en esta versión | No se hace |

Orden de ejecución resultante:

```
M1 (Must) ──► M2 (Must) ──┬──► M3 (Must) ──┐
                           │                ├──► M5 (Must) ──► M6 (Must)
                           └──► M4 (Must) ──┘         │
                                                        ▼
                                          M7 (Must + Should) ──► M8 (Must + Should)
```

M3 y M4 pueden trabajarse en paralelo porque no se tocan. M5 no arranca hasta que ambos estén cerrados.

## 1.3 Ramas

**Las ramas se nombran con el identificador de la tarea del backlog.**

```
M2.7
M5.4
```

El nombre de la rama es exactamente el de la tarea, para que cualquier rama se pueda rastrear contra el tablero sin ambigüedad. Git **no** admite `:` ni espacios en nombres de rama, por lo que la convención `[tipo]/[módulo]: [explicación]` se aplica únicamente a los mensajes de commit.

**Tipos de cambio (para los commits):**

| Tipo | Cuándo se usa |
|---|---|
| `feature` | Nueva funcionalidad (un RF) |
| `fix` | Corrección de un defecto |
| `refactor` | Reorganización sin cambiar comportamiento |
| `test` | Pruebas nuevas o corregidas |
| `docs` | Documentación |
| `chore` | Dependencias, configuración, migraciones, build, formato |
| `perf` | Mejora de rendimiento |

**Reglas de uso:**

1. Una tarea = una rama.
2. La rama nace desde `dev` actualizada.
3. La rama abre **un Pull Request** con el nombre del módulo en el título: `[Módulo] M5.4 — Registro del pedido y descuento de stock`.
4. Nadie aprueba su propio PR (requisito de la Definition of Done).
5. El PR se fusiona con **squash**.
6. `dev` se fusiona en `main` al cerrar cada módulo, con revisión.

Si dos tareas de documentación dependen una de otra, la segunda se apila sobre la primera (`M1.3` nace de `M1.2`) y se re-apila sobre `dev` cuando la anterior entre. Es el caso de este ciclo: M1.1, M1.2 y M1.3 entraron a `main` mediante los PR #1 y #2, y M1.4, M1.5 y M1.6 se apilan sobre `dev` en ese orden.

## 1.4 Formateador: Spotless

**Regla:** el código sin formatear **no** pasa la compilación. `check` depende de `spotlessCheck`, así que `./gradlew build` falla si algo está desalineado.

**Configuración en `build.gradle.kts`:**

```kotlin
plugins {
    java
    id("com.diffplug.spotless") version "8.10.4"   // compatible con Gradle 9
    id("org.springframework.boot") version "4.1.1"
    id("io.spring.dependency-management") version "1.1.7"
}

spotless {
    java {
        target("src/**/*.java")
        googleJavaFormat("<fijar-version>").aosp()  // sangría de 4, línea de 100
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
    kotlinGradle {
        target("*.gradle.kts")
        ktlint("<fijar-version>")
    }
}
```

| Comando | Para qué |
|---|---|
| `./gradlew spotlessApply` | Corregir el formato de todos los archivos |
| `./gradlew spotlessCheck` | Verificar sin modificar (lo corre `build`) |

- El IDE debe usar lo mismo (`.editorconfig`: `ij_java_indent_size=4`, `ij_java_continuation_indent_size=8`, `max_line_length=100`).
- **Nunca** se sube código sin pasar `spotlessApply` antes del commit.
- Un PR con cambios de formato en archivos que no tocó se rechaza: el formato se resuelve antes de abrir el PR.
- El aplicativo móvil no usa Spotless; se formatea con Prettier y se valida con ESLint.

## 1.5 Regla de nombres 1 — variables con nombres claros

Significado completo, sin abreviaturas ni palabras comodín. Nada de `x`, `data`, `info`, `tmp`, `obj`, `aux`, `val`.

| ❌ No | ✅ Sí |
|---|---|
| `double t` | `BigDecimal totalPedido` |
| `String[] a` | `List<String> ingredientes` |
| `var d = fecha;` | `LocalDate fechaEntregaSolicitada` |
| `int c = 0;` | `int cantidadDisponible` |
| `String nom` | `String nombreProducto` |

## 1.6 Regla de nombres 2 — las funciones indican su acción

Verbo + sustantivo, y el verbo dice **qué hace exactamente**: calcular, validar, buscar, guardar, generar, descontar. Se prohíben los verbos vagos (`procesar`, `manejar`, `hacer`, `realizar`, `ejecutar`).

| ❌ No | ✅ Sí |
|---|---|
| `procesarPedido(p)` | `calcularTotalPedido(pedido)` |
| `validar(p)` | `validarHorarioRecepcion(fechaHora)` |
| `getDatos()` | `buscarProductosActivos()` |
| `guardar(p)` | `guardarUbicacionDelCliente(ubicacion)` |
| `manejar(d)` | `descontarStock(producto, cantidad)` |
| `generarWhatsApp(o)` | `generarEnlaceWhatsAppDelPedido(pedido)` |

## 1.7 Regla de nombres 3 — clases y archivos nombrados por su función

El nombre de la clase dice qué hace y el archivo se llama igual. Una clase por archivo.

| ❌ No | ✅ Sí |
|---|---|
| `Utils`, `Helper`, `Manager`, `Service2` | `MensajePedidoFormatter` |
| `PedidoService` (hace validación, formato y envío) | `PedidoService` + `ValidadorHorarioRecepcion` + `WhatsAppLinkService` |
| `PedidoDTO`, `PedidoResponseDTO`, `PedidoResponseDto` | `PedidoResponse` (respuesta), `CrearPedidoRequest` (entrada) |

| Elemento | Convención | Ejemplos |
|---|---|---|
| Entidad JPA | Nombre del concepto | `Usuario`, `Producto`, `Ingrediente`, `Pedido`, `DetallePedido` |
| Controlador | `<Recurso>Controller` | `PedidoController`, `AdminPedidoController` |
| Servicio | `<Concepto>Service` | `PedidoService`, `ProductoService` |
| Formateador / validador | `<Concepto><Acción>` | `MensajePedidoFormatter`, `ValidadorHorarioRecepcion` |
| Excepción de negocio | `ApiException` + motivo | `new ApiException("STOCK_INSUFICIENTE", ...)` |
| Tabla y columna | `snake_case` en español | `conjunto_residencial`, `fecha_entrega` |
| Variable en Kotlin/TS | `camelCase` | `totalPedido`, `fechaEntrega` |
| Pantalla (móvil) | `<Concepto>.tsx` | `Catalogo.tsx`, `AdminPedidos.tsx` |

## 1.8 Commits

Conventional Commits en español:

```
feat(pedidos): registra pedido y descuenta stock de inventario
fix(pedidos): valida hora de entrega contra el horario de recepción
docs(pedidos): documenta endpoint de previsualización
test(pedidos): cubre stock insuficiente con 409
chore(datos): crea migraciones flyway y configura datasource
```

- Commits pequeños y con un solo propósito.
- Todo el formato se aplica antes de commitear: `./gradlew spotlessApply && git add .`

## 1.9 Definition of Ready

> **La tarea está clara, tiene prioridad, se entiende el resultado esperado y puede verificarse.**

- [ ] El requisito está identificado con su ID (`RF-03`, `RNF-01`) y la tarea tiene prioridad MoSCoW asignada.
- [ ] El criterio de aceptación está escrito en una frase verificable (no "funciona bien", sino "devuelve 409 con `stock insuficiente`").
- [ ] Está claro qué archivos se van a tocar, o al menos qué paquete o endpoint.
- [ ] Las dependencias previas están cerradas y fusionadas.
- [ ] No hay dos personas asignadas a la misma tarea.
- [ ] Si toca base de datos, la migración está identificada y es compatible con el esquema actual.
- [ ] Si toca el móvil, la pantalla y su ruta en la navegación están definidas.

Si falta un punto, la tarea **no** se inicia.

## 1.10 Definition of Done

> **La tarea está realizada, probada, revisada por otro integrante, sin errores que impidan su uso y registrada en el repositorio.**

- [ ] El código está en su rama y pasa `./gradlew build` (incluye `spotlessCheck`) en verde.
- [ ] Existe al menos una prueba que falla si se rompe el comportamiento (no se acepta "probado a mano").
- [ ] La prueba se ejecutó y pasó; el resultado está en el PR.
- [ ] El PR tiene **una revisión aprobada de otro integrante**, sin comentarios pendientes.
- [ ] No hay errores en consola ni stack traces al usar la funcionalidad.
- [ ] Las respuestas de error son las esperadas: nada de 500 por datos inválidos (400/404/409/422 según corresponda).
- [ ] Las variables, funciones, clases y archivos cumplen las reglas 1, 2 y 3.
- [ ] Si toca la base de datos, la migración está versionada y aplica sobre el esquema existente.
- [ ] Si toca el móvil, funciona en emulador y en la versión web.
- [ ] **La tarea está marcada como ☑ Hecho** en este tablero (§0 y su tabla de módulo).
- [ ] La rama está fusionada con squash y el PR queda cerrado.

---

# 2. Backlog por módulos

La **rama** de cada tarea lleva su mismo identificador, por eso no se repite una columna de nombre de rama.

## Módulo 1 — Levantamiento y análisis de requisitos

**Estado:** ◐ En proceso · **Requisitos:** todos · **Depende de:** nada

| # | Tarea | Prioridad | Resultado verificable | Estado |
|---|---|---|---|---|
| M1.1 | Especificar requisitos funcionales y no funcionales | Must | `docs/especificacion-requisitos.md` con RF-01…RF-10 y RNF-01…RNF-05 | ☑ Hecho |
| M1.2 | Redactar historias de usuario con criterios de aceptación | Must | `docs/historias-de-usuario.md`, 20 historias con Given/When/Then | ☑ Hecho |
| M1.3 | Modelar casos de uso y entidades | Must | `docs/casos-de-uso.md` y `docs/modelo-datos.md` | ☑ Hecho |
| M1.4 | Priorizar el backlog con MoSCoW | Must | Este tablero con prioridad por tarea | ☑ Hecho |
| M1.5 | Redactar el plan técnico del backend | Must | `docs/plan.md` con arquitectura, endpoints, seguridad y pruebas | ☐ No hecho |
| M1.6 | Redactar el plan de la aplicación móvil | Must | `docs/plan-frontend.md` con stack, navegación, pantallas y estado | ☐ No hecho |

**Ready:** alcance del módulo acordado; fuentes de información listadas.
**Done:** documentos en `docs/`; IDs estables usados literalmente en el resto de módulos; cada requisito tiene al menos una tarea en el backlog; los planes técnico y móvil describen la implementación de M2–M8.

## Módulo 2 — Registro de clientes

**Estado:** ☐ No hecho · **Requisitos:** RF-01, RNF-01 · **Depende de:** M1

| # | Tarea | Prioridad | Resultado verificable | Estado |
|---|---|---|---|---|
| M2.1 | Andamiaje: dependencias, Spotless, `.editorconfig` | Must | `spotlessCheck` pasa y falla si se desformatea un archivo | ☐ No hecho |
| M2.2 | Migraciones Flyway y datasource por esquema | Must | La app arranca; `flyway_schema_history` con las versiones aplicadas; **build en verde** | ☐ No hecho |
| M2.3 | Entidad `Usuario`, roles y repositorio | Must | `Usuario` mapeada y validada contra el esquema | ☐ No hecho |
| M2.4 | Emisión y validación del JWT | Must | Token firmado y verificado; token alterado → 401 | ☐ No hecho |
| M2.5 | Reglas de seguridad por rol | Must | `/api/admin/**` exige rol ADMIN; el resto de `/api/**` exige token | ☐ No hecho |
| M2.6 | Cuenta de la administradora al arrancar | Must | Arranca dos veces sin duplicar el admin; contraseña con BCrypt | ☐ No hecho |
| M2.7 | Endpoints de registro e inicio de sesión | Must | `POST /api/auth/registro` → 201 + token; email duplicado → 409 | ☐ No hecho |
| M2.8 | Usuario autenticado actual | Must | `GET /api/auth/yo` devuelve los datos del token, nunca los de otro | ☐ No hecho |
| M2.9 | Manejo global de errores | Must | Errores en `ProblemDetail`; ningún 500 por datos inválidos | ☐ No hecho |
| M2.10 | Pantalla de acceso de la administradora (móvil) | Must | Entra al panel con su usuario y clave; con rol cliente recibe 403 | ☐ No hecho |

**Ready:** M1 cerrado; contrato de registro y login definido con ejemplos; regla de la cuenta admin decidida (cuenta única).
**Done:** **prueba de escalada de privilegios**: un registro con `"rol":"ADMIN"` en el cuerpo devuelve 400 y el usuario queda `CLIENTE`; contraseña nunca en texto plano ni en el repositorio.

## Módulo 3 — Ubicación: conjunto y apartamento

**Estado:** ☐ No hecho · **Requisitos:** RF-02 · **Depende de:** M2

| # | Tarea | Prioridad | Resultado verificable | Estado |
|---|---|---|---|---|
| M3.1 | Endpoints de perfil y de ubicación | Must | `PUT /api/usuarios/yo/ubicacion` guarda y devuelve los datos | ☐ No hecho |
| M3.2 | Validación y copia de los datos | Must | Campos vacíos → 400; el pedido guarda copia de los valores del momento | ☐ No hecho |
| M3.3 | Pantalla Mi ubicación (móvil) | Must | El cliente guarda y vuelve a ver conjunto y apartamento | ☐ No hecho |

**Done:** un cliente no puede modificar la ubicación de otro; los pedidos anteriores conservan la ubicación original (RNF-04).

## Módulo 4 — Catálogo de sándwiches

**Estado:** ☐ No hecho · **Requisitos:** RF-03, RF-04, RF-05 · **Depende de:** M2

| # | Tarea | Prioridad | Resultado verificable | Estado |
|---|---|---|---|---|
| M4.1 | Migración de productos, ingredientes y datos de ejemplo | Must | Tablas creadas y catálogo de ejemplo cargado | ☐ No hecho |
| M4.2 | Catálogo público | Must | `GET /api/productos` devuelve solo activos con los datos del RF-04 | ☐ No hecho |
| M4.3 | Detalle de producto | Must | `GET /api/productos/{id}`; inexistente o inactivo → 404 | ☐ No hecho |
| M4.4 | Alta y edición de productos (admin) | Must | Crear y editar con rol ADMIN; con rol CLIENTE → 403 | ☐ No hecho |
| M4.5 | Actualización de precio y disponibilidad | Must | El cambio se refleja en el catálogo público en la siguiente consulta | ☐ No hecho |
| M4.6 | Pantallas de catálogo y detalle (móvil) | Must | Lista con precio, ingredientes y badge de disponibilidad | ☐ No hecho |
| M4.7 | Pantallas admin de productos (móvil) | Must | La administradora edita precio, stock e ingredientes desde el celular | ☐ No hecho |

**Done:** producto inactivo nunca aparece en el catálogo; stock en cero muestra "Agotado"; `stock >= 0` y `precio >= 0` garantizados por el esquema.

## Módulo 5 — Gestión de pedidos

**Estado:** ☐ No hecho · **Requisitos:** RF-06, RF-07, RF-08, RF-10, RNF-03, RNF-04 · **Depende de:** M3 y M4

| # | Tarea | Prioridad | Resultado verificable | Estado |
|---|---|---|---|---|
| M5.1 | Migración de pedidos y detalles | Must | Tablas con copia de nombre, precio, conjunto y apartamento | ☐ No hecho |
| M5.2 | Validación de horarios y de entrega | Must | Fuera de horario → 422 con el mensaje del horario válido | ☐ No hecho |
| M5.3 | Previsualización del pedido | Must | `POST /api/pedidos/preview` devuelve totales **sin** guardar nada | ☐ No hecho |
| M5.4 | Registro del pedido y descuento de stock | Must | `POST /api/pedidos` descuenta stock; stock insuficiente → 409 | ☐ No hecho |
| M5.5 | Consulta de pedidos del cliente | Must | `GET /api/pedidos/mios`; un pedido ajeno → 404 | ☐ No hecho |
| M5.6 | Listado global con filtros (admin) | Must | Filtros por estado y rango de fechas, paginado | ☐ No hecho |
| M5.7 | Cambio de estado con máquina de estados | Must | Transición válida → 200; inválida → 422; cancelar devuelve el stock | ☐ No hecho |
| M5.8 | Carrito (móvil) | Must | Cantidades editables y subtotal correcto | ☐ No hecho |
| M5.9 | Pantallas de entrega y confirmación (móvil) | Must | Fecha, hora y notas; el resumen muestra todo antes de confirmar | ☐ No hecho |
| M5.10 | Pantallas de consulta de pedidos (móvil) | Must | El cliente ve sus pedidos y el estado actual | ☐ No hecho |
| M5.11 | Pantallas admin de pedidos (móvil) | Must | La administradora filtra, abre y cambia el estado de un pedido | ☐ No hecho |

**Done:** dos pedidos simultáneos sobre el último sándwich → solo uno se confirma; el pedido guarda copia del precio aunque el admin lo cambie; ningún pedido se crea fuera de horario.

## Módulo 6 — Notificación mediante WhatsApp

**Estado:** ☐ No hecho · **Requisitos:** RF-09 · **Depende de:** M5

| # | Tarea | Prioridad | Resultado verificable | Estado |
|---|---|---|---|---|
| M6.1 | Configuración del número de la propietaria | Must | Número configurable por variable de entorno, no en el código | ☐ No hecho |
| M6.2 | Formateador del mensaje | Must | Mensaje con código, cliente, ubicación, fecha/hora, ítems y total | ☐ No hecho |
| M6.3 | Generación del enlace `wa.me` | Must | URL válida con acentos, espacios y saltos de línea codificados | ☐ No hecho |
| M6.4 | Endpoint del enlace | Must | `POST /api/pedidos/{id}/whatsapp`; pedido ajeno → 404 | ☐ No hecho |
| M6.5 | Botón de envío en la app (móvil) | Must | Abre WhatsApp con el mensaje listo; fallback si no hay app instalada | ☐ No hecho |

**Done:** el enlace abre el chat correcto con el pedido completo; acentos y saltos de línea se ven bien; funciona en Android, iOS y web.

## Módulo 7 — Pruebas

**Estado:** ☐ No hecho · **Requisitos:** transversal (RNF-01, RNF-03, RNF-04) · **Depende de:** M2–M6

| # | Tarea | Prioridad | Resultado verificable | Estado |
|---|---|---|---|---|
| M7.1 | Pruebas del formateador de WhatsApp | Must | Casos con acentos, saltos de línea y total | ☐ No hecho |
| M7.2 | Pruebas de horario, totales y estados | Must | Bordes exactos del horario; transición inválida detectada | ☐ No hecho |
| M7.3 | Base de integración con Testcontainers | Must | PostgreSQL efímero; la base de desarrollo no se toca | ☐ No hecho |
| M7.4 | Integración de registro, login y roles | Must | 201/409/401/403; escalada de privilegios cubierta | ☐ No hecho |
| M7.5 | Integración de catálogo y productos | Must | Solo activos en el público; 403 en administración | ☐ No hecho |
| M7.6 | Integración de pedidos y stock | Must | 409 por stock insuficiente; stock descontado una sola vez | ☐ No hecho |
| M7.7 | Integración de ubicación y notificación | Must | Ubicación aislada por cliente; enlace correcto y sin fugas | ☐ No hecho |
| M7.8 | Pruebas del aplicativo móvil | Should | Validaciones, formato de moneda y carrito cubiertos | ☐ No hecho |
| M7.9 | Reporte de cobertura | Should | Reporte adjunto al repositorio y revisado | ☐ No hecho |

**Done:** `./gradlew test` en verde; cada RF tiene al menos una prueba que lo evidencia; ningún test depende del orden de ejecución.

## Módulo 8 — Documentación

**Estado:** ☐ No hecho · **Requisitos:** transversal (RNF-05, evaluación) · **Depende de:** M1–M7

| # | Tarea | Prioridad | Resultado verificable | Estado |
|---|---|---|---|---|
| M8.1 | Documentación OpenAPI y esquema Bearer | Should | Swagger UI navegable y el botón *Authorize* funciona | ☐ No hecho |
| M8.2 | README de instalación y uso | Must | Alguien sin contexto sigue los pasos y levanta el sistema | ☐ No hecho |
| M8.3 | Colección HTTP del flujo completo | Must | `.http` encadena registro → catálogo → pedido → WhatsApp | ☐ No hecho |
| M8.4 | Matriz de trazabilidad RF/RNF | Must | Cada requisito apunta a endpoint, rama y prueba | ☐ No hecho |
| M8.5 | Manuales de cliente y administradora | Should | Manuales con capturas o secuencia de pasos | ☐ No hecho |
| M8.6 | Plan de despliegue | Could | Variables de producción documentadas; destino de despliegue definido | ☐ No hecho |

**Done:** el README se prueba desde cero en una máquina limpia; la matriz de trazabilidad no tiene requisitos huérfanos ni pruebas sin requisito.

---

# 3. Trazabilidad requisito → módulo → rama

| Requisito | Módulo | Ramas que lo implementan |
|---|---|---|
| RF-01 Registro de clientes | M2 | `M2.7`, `M2.8`, `M2.10` |
| RF-02 Ubicación | M3 | `M3.1`, `M3.2`, `M3.3` |
| RF-03 Catálogo de sándwiches | M4 | `M4.2`, `M4.6` |
| RF-04 Información del producto | M4 | `M4.2`, `M4.3`, `M4.6` |
| RF-05 Disponibilidad y mantenimiento | M4 | `M4.5`, `M4.7` |
| RF-06 Creación de pedido | M5 | `M5.3`, `M5.4`, `M5.8` |
| RF-07 Hora de entrega | M5 | `M5.2`, `M5.9` |
| RF-08 Confirmación | M5 | `M5.3`, `M5.9` |
| RF-09 Notificación WhatsApp | M6 | `M6.4`, `M6.5` |
| RF-10 Consulta del pedido | M5 | `M5.5`, `M5.7`, `M5.11` |
| RNF-01 Seguridad | M2 | `M2.5`, `M2.6`, `M2.10` |
| RNF-02 Usabilidad | transversal | Pantallas de cada módulo, `M8.5` |
| RNF-03 Disponibilidad | M5 | `M5.2` |
| RNF-04 Integridad | M5 | `M5.1`, `M5.4` |
| RNF-05 Mantenibilidad | M4, M8 | `M4.5`, `M8.2`, `M8.6` |

# 4. Riesgos abiertos

| Riesgo | Estado | Mitigación |
|---|---|---|
| `./gradlew build` en rojo: *toolchain* Java 21 ausente (solo hay JDK 25) y datasource sin configurar | **Abierto** | **M2.1** compila con `options.release = 21`; **M2.2** configura el datasource. Documentado en `docs/especificacion-requisitos.md` §9 |
| Spring Boot 4 es muy reciente (Security 7, Jackson 3, Hibernate 7) | Abierto | No usar APIs directos de Jackson; DSL de lambda en Security |
| springdoc 3.1.1 es la primera línea para Boot 4 | Abierto | Si falla, la API sigue funcionando: solo se pierde Swagger |
| Testcontainers 2.0.5 cambió de coordenadas | Abierto | Plan B con H2 en modo PostgreSQL |
| El pooler de Supabase puede dar problemas con Flyway | Abierto | En despliegue, usar la URL directa (5432) o modo session |
| El usuario administrador escribe con el mismo correo en `.env` y base | Abierto | `ADMIN_EMAIL`/`ADMIN_PASSWORD` viven en `.env`, ignorado por git |

# 5. Guion de trabajo por tarea

```bash
# 1. Antes de empezar: la tarea cumple la Definition of Ready
git checkout dev && git pull

# 2. Rama con el identificador de la tarea
git checkout -b M4.5

# 3. Desarrollo, con formato antes de commitear
./gradlew spotlessApply
./gradlew build

# 4. Commit con Conventional Commits en español
git add .
git commit -m "feat(catalogo): permite actualizar precio y stock de producto"
git push -u origin M4.5

# 5. Pull Request, con la evidencia de la Definition of Done
#    Título: [Catálogo de sándwiches] M4.5 — Actualización de precio y disponibilidad
#    Marcar la tarea como Hecho en docs/modulos-backlog.md
#    Adjuntar salida de ./gradlew build

# 6. Tras la aprobación de otro integrante: squash merge y cierre del PR
```