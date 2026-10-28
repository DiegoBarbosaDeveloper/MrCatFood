# Plan técnico del backend — MrCatFood

| Campo | Valor |
|---|---|
| Proyecto | MrCatFood — sistema de pedidos de sándwiches |
| Tarea | M1.5 · Redactar el plan técnico del backend |
| Versión | 1.0 |
| Estado | En revisión (Pull Request) |
| Documentos de origen | `docs/especificacion-requisitos.md`, `docs/casos-de-uso.md`, `docs/modelo-datos.md`, `docs/modulos-backlog.md` |

## 1. Propósito

Este documento describe **cómo** se implementan los requisitos de M2 a M7 sobre el backend: arquitectura, paquetes, contrato REST, seguridad, reglas de negocio, transacciones, configuración, migraciones y pruebas. Es la referencia que cada tarea del backlog cita al abrir su rama.

Lo que **no** cubre: pantallas de la aplicación móvil (ver `docs/plan-frontend.md`) y decisiones de despliegue (M8.6).

## 2. Stack y versiones

| Elemento | Versión | Nota |
|---|---|---|
| Java | 21 | Bytecode 21 compilado con el JDK 25 instalado mediante `options.release = 21` (M2.1) |
| Spring Boot | 4.1.1 | Security 7, Jackson 3, Hibernate 7 |
| Gradle | 9.7.1 | Wrapper incluido |
| PostgreSQL | 16 | `TIMESTAMPTZ`, `NUMERIC(12,2)`, `BIGSERIAL` |
| Flyway | Según BOM de Boot | Migraciones versionadas |
| springdoc | 3.1.1 | Swagger UI y esquema Bearer (M8.1) |
| Testcontainers | 2.0.5 | PostgreSQL efímero para integración |
| Spotless | 8.10.4 | `googleJavaFormat().aosp()`, obligatorio en `build` |

**Regla de oro:** el proyecto compila y prueba con Java 21; no se usa ninguna API posterior a esa versión.

## 3. Arquitectura por capas

```text
Petición HTTP
    │
    ▼
Controller      valida el DTO de entrada, delega y devuelve el DTO de salida
    │
    ▼
Service         aplica reglas de negocio, transacciones y permisos de recurso
    │
    ▼
Repository      Spring Data JPA; consultas derivadas o @Query
    │
    ▼
Entidad JPA  ──►  PostgreSQL (Flyway administra el esquema)
```

Reglas de dependencia:

- El controlador nunca toca repositorios ni entidades directamente.
- El servicio no conoce `HttpServletRequest` ni códigos HTTP; lanza excepciones de negocio.
- Las entidades no se serializan: siempre se mapean a DTO (`*Request` / `*Response`).
- Los validadores de reglas (`ValidadorHorarioRecepcion`, `ValidadorFechaEntrega`, `MensajePedidoFormatter`) son clases sin estado, inyectables y probables por unidad.

## 4. Estructura de paquetes

```text
com.diegodev.mrcatfood
├── MrCatFoodApplication.java
├── config
│   ├── SeguridadConfig.java          SecurityFilterChain, PasswordEncoder
│   ├── JwtConfig.java                secreto y expiración
│   ├── RelojConfig.java              Clock del sistema (zona America/Bogota)
│   ├── OpenApiConfig.java            esquema Bearer (M8.1)
│   └── PropiedadesPedido.java        horarios y ventana de entrega
├── seguridad
│   ├── FiltroJwt.java                OncePerRequestFilter
│   ├── ServicioJwt.java              emitir, validar, revocar
│   ├── UsuarioAutenticado.java       principal con id, email y rol
│   └── CreadorCuentaAdministradora.java   ApplicationRunner idempotente
├── usuario
│   ├── Usuario.java  Rol.java  UsuarioRepository.java
│   ├── UsuarioService.java
│   ├── AutenticacionController.java  UsuarioController.java
│   └── dto/  (RegistroRequest, LoginRequest, TokenResponse, UsuarioResponse, UbicacionRequest)
├── producto
│   ├── Producto.java  Ingrediente.java  ProductoRepository.java
│   ├── ProductoService.java
│   ├── ProductoController.java  AdminProductoController.java
│   └── dto/  (ProductoResponse, ProductoRequest, DisponibilidadRequest)
├── pedido
│   ├── Pedido.java  DetallePedido.java  EstadoPedido.java  PedidoRepository.java
│   ├── PedidoService.java  EstadoPedidoService.java
│   ├── PedidoController.java  AdminPedidoController.java
│   ├── ValidadorHorarioRecepcion.java  ValidadorFechaEntrega.java
│   ├── MensajePedidoFormatter.java  WhatsAppService.java
│   └── dto/  (PrevisualizarPedidoRequest, CrearPedidoRequest, PedidoResponse, DetalleResponse, CambiarEstadoRequest)
└── comun
    ├── ApiException.java             excepción de negocio con código
    ├── ManejadorGlobalErrores.java   @RestControllerAdvice → ProblemDetail
    └── Paginacion.java               página, tamaño y orden comunes
```

## 5. Modelo de dominio

Se implementa exactamente el esquema de `docs/modelo-datos.md`:

| Entidad | Notas de implementación |
|---|---|
| `Usuario` | `email` único, normalizado a minúsculas; `passwordHash` BCrypt; `rol` como `enum` `CLIENTE`/`ADMIN`; `activo` |
| `Producto` | `precio` y `stock` como `BigDecimal`/`Integer`; `@Version` para bloqueo optimista; `activo` para baja lógica |
| `Ingrediente` | `@ManyToOne Producto`; sin catálogo central ni sobrecosto |
| `Pedido` | Copia de `conjuntoResidencial` y `apartamento`; `fechaEntrega`, `horaEntrega`, `notas`, `subtotal`, `total`; `estado` |
| `DetallePedido` | Copia de `nombreProducto` y `precioUnitario`; `producto` con `ON DELETE SET NULL`; `cantidad > 0` |

Convenciones:

- Entidades en singular, tablas en `snake_case` en español.
- `BigDecimal` con `setScale(2, RoundingMode.HALF_UP)` al calcular totales.
- Los totales se recalculan **siempre** en el servidor a partir de los precios vigentes; nunca se confía en el total que envía el cliente.
- `EstadoPedido` es un `enum` con la tabla de transiciones permitidas; el estado se guarda como texto.

## 6. Contrato REST

### 6.1 Endpoints

| Módulo | Método y ruta | Acceso | Éxito | Errores esperados |
|---|---|---|---|---|
| M2 | `POST /api/auth/registro` | Público | 201 + token | 400, 409 |
| M2 | `POST /api/auth/login` | Público | 200 + token | 401 |
| M2 | `POST /api/auth/logout` | Token | 204 | 401 |
| M2 | `GET /api/auth/yo` | Token | 200 | 401 |
| M3 | `PUT /api/usuarios/yo/ubicacion` | Token | 200 | 400, 401 |
| M4 | `GET /api/productos` | Público | 200 (solo activos) | — |
| M4 | `GET /api/productos/{id}` | Público | 200 | 404 |
| M4 | `GET /api/admin/productos` | ADMIN | 200 (incluye inactivos) | 401, 403 |
| M4 | `POST /api/admin/productos` | ADMIN | 201 | 400, 401, 403 |
| M4 | `PUT /api/admin/productos/{id}` | ADMIN | 200 | 400, 401, 403, 404 |
| M4 | `PATCH /api/admin/productos/{id}/disponibilidad` | ADMIN | 200 | 400, 401, 403, 404 |
| M5 | `POST /api/pedidos/preview` | Token | 200 (no persiste) | 400, 401, 422 |
| M5 | `POST /api/pedidos` | Token | 201 | 400, 401, 409, 422 |
| M5 | `GET /api/pedidos/mios` | Token | 200 | 401 |
| M5 | `GET /api/pedidos/{id}` | Token (dueño) | 200 | 401, 404 |
| M5 | `GET /api/admin/pedidos` | ADMIN | 200 paginado | 400, 401, 403 |
| M5 | `GET /api/admin/pedidos/{id}` | ADMIN | 200 | 401, 403, 404 |
| M5 | `PATCH /api/admin/pedidos/{id}/estado` | ADMIN | 200 | 401, 403, 404, 422 |
| M6 | `POST /api/pedidos/{id}/whatsapp` | Token (dueño) | 200 con `enlace` | 401, 404 |
| RNF-03 | `GET /api/configuracion/horario` | Público | 200 | — |

**Regla de privacidad:** un pedido o una ubicación de otro cliente responde **404**, no 403, para no revelar su existencia (RF-10, CU-12 A2).

### 6.2 Códigos de error

| Situación | HTTP | `codigo` en el ProblemDetail |
|---|---|---|
| DTO inválido, campo faltante, formato incorrecto | 400 | `DATOS_INVALIDOS` |
| Sin token, token vencido o alterado | 401 | `NO_AUTENTICADO` |
| Credenciales incorrectas | 401 | `CREDENCIALES_INVALIDAS` |
| Rol insuficiente | 403 | `ACCESO_DENEGADO` |
| Producto o pedido inexistente o ajeno | 404 | `RECURSO_NO_ENCONTRADO` |
| Correo ya registrado | 409 | `CORREO_EN_USO` |
| Stock insuficiente | 409 | `STOCK_INSUFICIENTE` |
| Fuera del horario de recepción | 422 | `FUERA_DE_HORARIO` |
| Fecha de entrega inválida | 422 | `FECHA_ENTREGA_INVALIDA` |
| Transición de estado no permitida | 422 | `TRANSICION_INVALIDA` |
| Error no controlado | 500 | `ERROR_INTERNO` (sin detalles internos) |

Formato único:

```json
{
  "type": "about:blank",
  "title": "Stock insuficiente",
  "status": 409,
  "detail": "Solo quedan 1 unidades de Sándwich de pollo",
  "instance": "/api/pedidos",
  "codigo": "STOCK_INSUFICIENTE",
  "campos": { "cantidad": "supera la disponibilidad" }
}
```

## 7. Seguridad

### 7.1 JWT

- Firma **HS256** con secreto `JWT_SECRETO` (mínimo 32 bytes, solo en `.env`).
- Claims: `sub` (email), `uid` (id del usuario), `rol`, `jti` (identificador único), `iat`, `exp`.
- Expiración `JWT_EXPIRACION_MINUTOS` (por defecto 120).
- Se envía en `Authorization: Bearer <token>`; la API es *stateless* (`SessionCreationPolicy.STATELESS`) y CSRF está deshabilitado.

### 7.2 Cierre de sesión (HU-02)

Un JWT no se puede invalidar por sí solo. Para que el criterio «el token deja de servir» sea verificable se agrega una tabla `token_revocado (jti, expira_en)`; el filtro rechaza cualquier `jti` presente y una tarea programada purga los vencidos. `POST /api/auth/logout` inserta el `jti` del token actual.

### 7.3 Autorización

| Ruta | Regla |
|---|---|
| `POST /api/auth/registro`, `POST /api/auth/login` | `permitAll` |
| `GET /api/productos/**`, `GET /api/configuracion/horario` | `permitAll` |
| `/api/admin/**` | `hasRole("ADMIN")` |
| Resto de `/api/**` | `authenticated` |

- El registro público **ignora** cualquier `rol` enviado y siempre crea `CLIENTE`; si el cuerpo trae `rol: "ADMIN"`, responde 400 (prueba de escalada de privilegios de M2).
- La cuenta administrativa se crea al arrancar desde `ADMIN_EMAIL`/`ADMIN_PASSWORD`; si ya existe, no se duplica ni se sobrescribe.
- Contraseñas con `BCryptPasswordEncoder` (fuerza 12). Nunca se registran en logs.

## 8. Reglas de negocio

| Regla | Dónde vive | Cómo se garantiza |
|---|---|---|
| RN-01 inventario nunca negativo | `ProductoService.descontarStock` | `UPDATE` condicionado por `stock >= cantidad` + `@Version`; si no afecta filas → 409 |
| RN-02 pedidos solo en horario | `ValidadorHorarioRecepcion` | Compara `LocalTime`/día contra `PropiedadesPedido.horarioRecepcion` |
| RN-03 entrega futura y en ventana | `ValidadorFechaEntrega` | `fechaHoraEntrega > ahora` y dentro de `ventanaEntrega` |
| RN-04 cancelar devuelve stock | `EstadoPedidoService.cambiarEstado` | En la misma transacción del cambio a `CANCELADO` |
| RN-05 transiciones permitidas | `EstadoPedido` | Tabla `Map<EstadoPedido, Set<EstadoPedido>>`; `ENTREGADO` es final |
| RN-06 correo sin mayúsculas ni espacios | `UsuarioService` | `email.trim().toLowerCase(Locale.ROOT)` + índice `LOWER(email)` |

**Dos ventanas distintas**, que la especificación separa y el código no debe confundir:

- **Horario de recepción** (RNF-03): cuándo se puede *crear* un pedido.
- **Ventana de entrega** (RF-07): qué horas se pueden *programar* para la entrega.

Ambas se configuran por variables de entorno y se exponen en `GET /api/configuracion/horario`.

## 9. Transacciones y concurrencia

`PedidoService.registrarPedido` es una sola transacción:

1. Validar horario de recepción y fecha de entrega (422 si falla).
2. Cargar los productos activos y verificar cantidades.
3. Descontar stock con actualización condicionada (`stock >= cantidad`).
4. Calcular subtotal y total con los precios leídos en la transacción.
5. Insertar `pedido` y `detalle_pedido` con copia de nombre, precio y ubicación.
6. Generar el código `MRC-{año}-{secuencial}`.
7. Confirmar; cualquier fallo revierte todo, incluido el stock.

Concurrencia: si dos clientes compiten por la última unidad, el `UPDATE` condicionado y el `@Version` hacen que solo uno confirme; el otro recibe `409 STOCK_INSUFICIENTE`. No se reintenta automáticamente.

Aislamiento: `READ_COMMITTED` (por defecto de PostgreSQL), suficiente porque la condición de stock se evalúa en el `UPDATE` y no en una lectura previa.

## 10. Configuración por entorno

`application.yaml` solo declara nombres; los valores viven en `.env` (ignorado por Git) y en el entorno de despliegue.

| Variable | Uso | Obligatoria |
|---|---|---|
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | Datasource | Sí |
| `JWT_SECRETO` | Firma del token | Sí |
| `JWT_EXPIRACION_MINUTOS` | Vigencia | No (120) |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | Cuenta única de la administradora | Sí |
| `WHATSAPP_NUMERO` | Número de la propietaria | Sí |
| `PEDIDOS_HORARIO_INICIO`, `PEDIDOS_HORARIO_FIN` | Horario de recepción | No |
| `PEDIDOS_ENTREGA_INICIO`, `PEDIDOS_ENTREGA_FIN` | Ventana de entrega | No |
| `PEDIDOS_ZONA` | Zona horaria | No (`America/Bogota`) |

Perfiles: `dev` (PostgreSQL local), `test` (Testcontainers) y `prod` (variables de entorno). Ningún secreto se escribe en el repositorio.

## 11. Mensaje y enlace de WhatsApp

- `MensajePedidoFormatter` construye el texto con código, nombre y teléfono del cliente, conjunto y apartamento, fecha y hora, notas, líneas (`cantidad × producto — precio`) y total.
- Formato de moneda: `$ 12.500` con separador de miles y sin decimales cuando son cero.
- `WhatsAppService` genera `https://wa.me/{numero}?text={mensaje}` usando `URLEncoder.encode(mensaje, UTF_8)`; el número se normaliza sin `+`, espacios ni guiones.
- Si el pedido no pertenece al cliente autenticado → 404.
- El envío lo hace el cliente; el backend no llama a la API de WhatsApp (RF-09).

## 12. Migraciones Flyway

| Versión | Tarea | Contenido |
|---|---|---|
| `V1__crea_usuario.sql` | M2.2 | `usuario`, índice `LOWER(email)` y `token_revocado` |
| `V2__crea_producto.sql` | M4.1 | `producto`, `producto_ingrediente` y datos de ejemplo |
| `V3__crea_pedido.sql` | M5.1 | `pedido`, `detalle_pedido`, índices y checks |

Reglas: nombres `V{n}__descripcion_en_snake_case.sql`; una migración por tarea que toque el esquema; nunca se edita una migración ya aplicada; `stock >= 0` y `precio >= 0` se garantizan con `CHECK` además de en el servicio.

## 13. Pruebas

| Nivel | Herramienta | Qué cubre |
|---|---|---|
| Unidad | JUnit 5 + AssertJ + Mockito | Formateador de WhatsApp, validador de horario, transiciones de estado, cálculo de totales |
| Integración | `@SpringBootTest` + MockMvc + Testcontainers | Endpoints completos sobre PostgreSQL real |
| Seguridad | MockMvc + tokens reales | 401/403, escalada de privilegios, pedido ajeno → 404 |

Reglas: ninguna prueba depende del orden de ejecución; cada prueba de integración parte de una base limpia (`@Transactional` o truncado); cada RN de §8 tiene al menos una prueba que falla si la regla se rompe; `./gradlew test` debe quedar en verde antes del PR.

## 14. Riesgos técnicos

| Riesgo | Mitigación |
|---|---|
| Spring Boot 4 es reciente (Security 7, Jackson 3) | Usar el DSL de lambda en Security y no tocar APIs de Jackson directamente |
| springdoc 3.1.1 puede no cubrir Boot 4 | La API funciona sin Swagger; se documenta el fallback en M8.1 |
| Testcontainers 2.0.5 cambió coordenadas | Si falla, plan B: H2 en modo PostgreSQL solo para pruebas |
| Supabase con pooler puede romper Flyway | Usar la URL directa (5432) o modo *session* en despliegue |
| Reloj y zona horaria en pruebas | Inyectar `Clock` y fijar `America/Bogota`; nunca `LocalDateTime.now()` dentro de un servicio |

## 15. Trazabilidad

| Elemento | Ubicación |
|---|---|
| Tarea del backlog | M1.5 — ver `docs/modulos-backlog.md` |
| Requisitos | M1.1 — `docs/especificacion-requisitos.md` |
| Casos de uso | M1.3 — `docs/casos-de-uso.md` |
| Modelo de datos | M1.3 — `docs/modelo-datos.md` |
| Plan de la aplicación móvil | M1.6 — `docs/plan-frontend.md` |
| Implementación | M2.1 a M7.7, según `docs/modulos-backlog.md` |
