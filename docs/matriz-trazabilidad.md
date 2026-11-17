# Matriz de trazabilidad — MrCatFood

Esta matriz relaciona los requisitos definidos en
[`especificacion-requisitos.md`](especificacion-requisitos.md) con su implementación y evidencia
verificable. Las rutas con `{id}` requieren el identificador del recurso; las rutas bajo
`/api/admin` exigen rol `ADMIN`.

## Requisitos funcionales

| ID | Implementación verificable | Ramas | Pruebas y evidencia |
|---|---|---|---|
| **RF-01** Registro de clientes | `POST /api/auth/registro`, `POST /api/auth/login`, `POST /api/auth/logout`, `GET /api/auth/yo`; pantallas `acceso` y `registro` | `M2.7`, `M2.8`, `M2.10` | `AutenticacionTest`, `AutenticacionIntegracionTest`, `SeguridadTest`, `clienteHttp-test.ts` |
| **RF-02** Ubicación | `PUT /api/usuarios/yo/ubicacion`; pantallas `perfil` y `mi-ubicacion` | `M3.1`, `M3.2`, `M3.3` | `UsuarioControllerTest`, `UbicacionNotificacionIntegracionTest`, `esquemasFormularios-test.ts` |
| **RF-03** Catálogo | `GET /api/productos`; pantalla de catálogo | `M4.2`, `M4.6` | `ProductoControllerTest`, `CatalogoIntegracionTest` |
| **RF-04** Información del producto | `GET /api/productos`, `GET /api/productos/{id}`; pantalla `detalle-producto` | `M4.2`, `M4.3`, `M4.6` | `ProductoControllerTest`, `CatalogoIntegracionTest`, `formateo-test.ts` |
| **RF-05** Disponibilidad y mantenimiento | `GET/POST/PUT /api/admin/productos`, `PATCH /api/admin/productos/{id}/disponibilidad`; pantallas administrativas de productos | `M4.4`, `M4.5`, `M4.7` | `AdminProductoControllerTest`, `CatalogoIntegracionTest`, `PedidosStockIntegracionTest` |
| **RF-06** Creación de pedido | `POST /api/pedidos/preview`, `POST /api/pedidos`; pantalla `carrito` | `M5.3`, `M5.4`, `M5.8` | `PedidoPreviewControllerTest`, `RegistroPedidoTest`, `PedidosStockIntegracionTest`, `tiendaCarrito-test.ts` |
| **RF-07** Hora de entrega | `GET /api/configuracion/horario`, `POST /api/pedidos/preview`, `POST /api/pedidos`; pantalla `entrega` | `M5.2`, `M5.9` | `ConfiguracionHorarioControllerTest`, `ValidadorFechaEntregaTest`, `ValidadorHorarioRecepcionTest`, `esquemasFormularios-test.ts` |
| **RF-08** Confirmación del pedido | `POST /api/pedidos/preview`, `POST /api/pedidos`; pantalla `confirmacion` | `M5.3`, `M5.4`, `M5.9` | `PedidoPreviewControllerTest`, `RegistroPedidoTest`, `PedidosStockIntegracionTest`, `formateo-test.ts` |
| **RF-09** WhatsApp | `POST /api/pedidos/{id}/whatsapp`; acción “Enviar por WhatsApp” en el detalle | `M6.2`, `M6.3`, `M6.4`, `M6.5` | `MensajePedidoFormatterTest`, `WhatsAppServiceTest`, `PedidoWhatsAppControllerTest`, `UbicacionNotificacionIntegracionTest` |
| **RF-10** Consulta del pedido | `GET /api/pedidos/mios`, `GET /api/pedidos/{id}`, `GET /api/admin/pedidos`, `GET /api/admin/pedidos/{id}`, `PATCH /api/admin/pedidos/{id}/estado`; pantallas de pedidos | `M5.5`, `M5.6`, `M5.7`, `M5.10`, `M5.11` | `ConsultaPedidoClienteTest`, `AdminPedidoControllerTest`, `CambioEstadoPedidoTest`, `EstadoPedidoTest` |

## Requisitos no funcionales

| ID | Implementación verificable | Ramas | Pruebas y evidencia |
|---|---|---|---|
| **RNF-01** Seguridad | JWT en rutas protegidas; rol `ADMIN` en `/api/admin/**`; registro sin asignación pública de rol; errores 401/403 | `M2.4`, `M2.5`, `M2.6`, `M2.9`, `M2.10`, `M7.4` | `ServicioJwtTest`, `SeguridadTest`, `SeguridadIntegracionTest`, `AutenticacionIntegracionTest`, `clienteHttp-test.ts` |
| **RNF-02** Usabilidad | No corresponde a un endpoint único: navegación Expo Router, validación de formularios, estados de carga/vacío/error y mensajes basados en `ProblemDetail` | Pantallas móviles de `M2.10`, `M3.3`, `M4.6`, `M4.7`, `M5.8`–`M5.11`, `M6.5`, `M8.5` | `esquemasFormularios-test.ts`, `formateo-test.ts`, `tiendaCarrito-test.ts`, manuales de M8.5 |
| **RNF-03** Disponibilidad | Horarios configurables; `GET /api/configuracion/horario`; rechazo 422 fuera del horario | `M5.2`, `M7.2` | `ConfiguracionHorarioControllerTest`, `ValidadorHorarioRecepcionTest`, `IntegracionBaseTest` |
| **RNF-04** Integridad | Copia de ubicación, producto y precio en el pedido; transacción de pedido y stock; migración con restricciones | `M5.1`, `M5.4`, `M7.6`, `M7.7` | `MigracionPedidoTest`, `RegistroPedidoTest`, `PedidosStockIntegracionTest`, `UbicacionNotificacionIntegracionTest` |
| **RNF-05** Mantenibilidad | Flyway versiona el esquema; catálogo editable desde `/api/admin/productos`; OpenAPI generado; instrucciones reproducibles | `M2.2`, `M4.4`, `M4.5`, `M4.7`, `M8.1`, `M8.2`, `M8.6` | `MigracionProductoTest`, `OpenApiConfigTest`, `AdminProductoControllerTest`, `README.md` |

## Reglas de negocio derivadas

| Regla | Implementación | Prueba principal |
|---|---|---|
| **RN-01** El inventario no queda negativo | Descuento transaccional condicionado al stock | `PedidosStockIntegracionTest` |
| **RN-02** No se aceptan pedidos fuera del horario | `ValidadorHorarioRecepcion` | `ValidadorHorarioRecepcionTest` |
| **RN-03** Entrega futura y dentro de la ventana | `ValidadorFechaEntrega` | `ValidadorFechaEntregaTest` |
| **RN-04** Cancelar devuelve inventario | `PedidoService.cambiarEstadoPedido` | `CambioEstadoPedidoTest` |
| **RN-05** Transiciones de estado permitidas | `EstadoPedido` | `EstadoPedidoTest` |
| **RN-06** Correo normalizado | `UsuarioService` | `AutenticacionTest`, `UsuarioRepositoryTest` |

## Inventario inverso de pruebas

Esta sección evita pruebas sin requisito o propósito técnico identificado. Los nombres corresponden
a clases bajo `src/test/java` y archivos bajo `movil/__tests__`.

| Prueba | Requisito o propósito cubierto |
|---|---|
| `MrCatFoodApplicationTests` | RNF-05: arranque y composición del contexto |
| `ManejadorGlobalErroresTest` | RNF-02 y RNF-05: errores consistentes y mantenibles |
| `OpenApiConfigTest` | RNF-05: OpenAPI y esquema Bearer |
| `CorsConfigTest`, `PropiedadesCorsTest` | RNF-01: orígenes autorizados |
| `WhatsAppPropertiesTest` | RF-09: configuración del destinatario |
| `ServicioJwtTest`, `SeguridadTest`, `SeguridadIntegracionTest`, `CreadorCuentaAdministradoraTest` | RNF-01 y RF-01 |
| `AutenticacionTest`, `AutenticacionIntegracionTest`, `UsuarioRepositoryTest` | RF-01 y RN-06 |
| `UsuarioControllerTest` | RF-02 |
| `UbicacionNotificacionIntegracionTest` | RF-02, RF-09 y RNF-04 |
| `ProductoControllerTest`, `CatalogoIntegracionTest` | RF-03 y RF-04 |
| `AdminProductoControllerTest`, `MigracionProductoTest` | RF-05 y RNF-05 |
| `ConfiguracionHorarioControllerTest`, `ValidadorHorarioRecepcionTest` | RF-07, RNF-03 y RN-02 |
| `ValidadorFechaEntregaTest` | RF-07 y RN-03 |
| `PedidoPreviewControllerTest` | RF-06 y RF-08 |
| `RegistroPedidoTest`, `PedidosStockIntegracionTest` | RF-06, RF-08, RNF-04 y RN-01 |
| `ConsultaPedidoClienteTest` | RF-10 y privacidad de recursos |
| `AdminPedidoControllerTest` | RF-10 y RNF-01 |
| `CambioEstadoPedidoTest`, `EstadoPedidoTest` | RF-10, RN-04 y RN-05 |
| `MigracionPedidoTest` | RNF-04 y RNF-05 |
| `MensajePedidoFormatterTest`, `WhatsAppServiceTest`, `PedidoWhatsAppControllerTest` | RF-09 |
| `BaseIntegracionTest` | Infraestructura compartida; no contiene casos ejecutables |
| `IntegracionBaseTest` | RNF-03 y RNF-05: PostgreSQL y Flyway reales |
| `clienteHttp-test.ts` | RF-01 y RNF-01: token y `ProblemDetail` |
| `esquemasFormularios-test.ts` | RF-01, RF-02, RF-07 y RNF-02 |
| `formateo-test.ts` | RF-04, RF-08, RF-10 y RNF-02 |
| `tiendaCarrito-test.ts` | RF-06 y RNF-02 |

## Evidencia transversal

- Contrato interactivo: Swagger UI en `/swagger-ui.html` y OpenAPI en `/v3/api-docs`.
- Flujo extremo a extremo: [`http/flujo-completo.http`](http/flujo-completo.http).
- Cobertura medida: [`reporte-cobertura.md`](reporte-cobertura.md).
- Criterios Given/When/Then: [`historias-de-usuario.md`](historias-de-usuario.md).
- Casos principales y alternos: [`casos-de-uso.md`](casos-de-uso.md).
