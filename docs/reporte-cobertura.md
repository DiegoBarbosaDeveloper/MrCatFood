# Reporte de cobertura — M7

Generado el 17 de noviembre de 2026 tras la implementación de M7. Los artefactos
completos (HTML y XML) no se versionan: se regeneran con los comandos de abajo.

## Resumen

| Suite | Comando | Pruebas | Resultado |
|---|---|---:|---|
| Backend rápida (H2) | `./gradlew test` | 122 | ✅ 0 fallos |
| Backend integración (PostgreSQL efímero) | `./gradlew integrationTest` | 23 | ✅ 0 fallos |
| Móvil (Jest) | `cd movil && npm test` | 27 | ✅ 0 fallos |

## Backend — JaCoCo

Comando: `./gradlew jacocoTestReport` (ejecuta ambas suites backend y publica el
informe en `build/reports/jacoco/test/`).

| Métrica | Cubierto | Total | Porcentaje |
|---|---:|---:|---:|
| Instrucciones | 3335 | 3532 | 94,4 % |
| Ramas | 116 | 148 | 78,4 % |
| Líneas | 787 | 832 | 94,6 % |
| Métodos | 201 | 208 | 96,6 % |

Cobertura de líneas por paquete:

| Paquete | Porcentaje |
|---|---:|
| `pedido` | 96,8 % |
| `pedido.dto` | 100 % |
| `seguridad` | 90,1 % |
| `config` | 93,8 % |
| `comun` | 86,1 % |
| `usuario` | 97,4 % |
| `producto` | 100 % |
| `MrCatFoodApplication` | 33,3 % |

## Móvil — Jest

Comando: `cd movil && npm run test:coverage` (informe HTML en `movil/coverage/`).

| Archivo | Instrucciones | Ramas | Funciones | Líneas |
|---|---:|---:|---:|---:|
| `utilidades/formatearPedido.ts` | 100 % | 100 % | 100 % | 100 % |
| `utilidades/formatearPrecio.ts` | 100 % | 100 % | 100 % | 100 % |
| `validacion/esquemasFormularios.ts` | 100 % | 100 % | 100 % | 100 % |
| `api/clienteHttp.ts` | 95,2 % | 89,5 % | 66,7 % | 100 % |
| `funcionalidades/carrito/tiendaCarrito.ts` | 83,8 % | 78,9 % | 79,2 % | 82,8 % |
| **Global** | **42,4 %** | **57,1 %** | **40,9 %** | **40,3 %** |

El porcentaje global es bajo porque las capas de API (`autenticacionApi`,
`pedidosApi`, etc.) y el contexto de sesión son envoltorios delgados que se
prueban de forma indirecta; la lógica pura exigida por M7.8 (validaciones,
formato de moneda y carrito) está cubierta al 100 % o cerca.

## Trazabilidad de requisitos

| Requisito | Evidencia |
|---|---|
| RF-01 Registro de clientes | `AutenticacionTest`, `AutenticacionIntegracionTest` |
| RF-02 Ubicación | `UsuarioControllerTest`, `UbicacionNotificacionIntegracionTest`, `esquemasFormularios-test.ts` |
| RF-03/RF-04/RF-05 Catálogo | `ProductoControllerTest`, `AdminProductoControllerTest`, `CatalogoIntegracionTest` |
| RF-06 Creación de pedido | `RegistroPedidoTest`, `PedidosStockIntegracionTest`, `tiendaCarrito-test.ts` |
| RF-07 Hora de entrega | `ValidadorFechaEntregaTest`, `ValidadorHorarioRecepcionTest`, `esquemasFormularios-test.ts` |
| RF-08 Confirmación del pedido | `CambioEstadoPedidoTest`, `ConsultaPedidoClienteTest`, `formateo-test.ts` |
| RF-09 WhatsApp | `MensajePedidoFormatterTest`, `PedidoWhatsAppControllerTest`, `UbicacionNotificacionIntegracionTest` |
| RF-10 Consulta del pedido | `ConsultaPedidoClienteTest`, `AdminPedidoControllerTest` |
| RNF-01 Seguridad | `SeguridadTest`, `SeguridadIntegracionTest`, `clienteHttp-test.ts` |
| RNF-03 Disponibilidad | `IntegracionBaseTest` (Flyway sobre PostgreSQL real) |
| RNF-04 Integridad | `MigracionPedidoTest`, `PedidosStockIntegracionTest` |

## Regeneración

```bash
# Backend
./gradlew spotlessApply test integrationTest jacocoTestReport
# Abrir build/reports/jacoco/test/html/index.html

# Móvil
cd movil && npm run lint && npm run typecheck && npm run test:coverage
# Abrir movil/coverage/lcov-report/index.html
```

Notas: la suite de integración requiere un runtime de contenedores (ver
`docs/plan.md` §13). `./gradlew build` ejecuta solo la suite rápida y debe
quedar en verde antes de cada PR.
