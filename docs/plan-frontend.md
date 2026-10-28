# Plan de la aplicación móvil — MrCatFood

| Campo | Valor |
|---|---|
| Proyecto | MrCatFood — sistema de pedidos de sándwiches |
| Tarea | M1.6 · Redactar el plan de la aplicación móvil |
| Versión | 1.0 |
| Estado | En revisión (Pull Request) |
| Documentos de origen | `docs/especificacion-requisitos.md`, `docs/historias-de-usuario.md`, `docs/plan.md` |

## 1. Propósito

Define **cómo** se construye el aplicativo del cliente y de la administradora: stack, estructura, navegación, pantallas, estado, cliente HTTP, formato de moneda, integración con WhatsApp y pruebas. Complementa `docs/plan.md`, que describe el backend.

La app es la única interfaz del sistema: el cliente pide desde el celular y la administradora gestiona catálogo y pedidos desde el suyo.

## 2. Stack

| Elemento | Elección | Motivo |
|---|---|---|
| Framework | Expo (SDK estable) + React Native | Un solo código para Android, iOS y web |
| Lenguaje | TypeScript estricto | Tipos compartidos con el contrato REST |
| Navegación | Expo Router (rutas por archivo) | Rutas declarativas y enlaces profundos |
| Estado del servidor | TanStack Query | Caché, reintentos y estados de carga/error listos |
| Estado local | Zustand + AsyncStorage | Carrito persistente entre sesiones |
| Formularios | React Hook Form + Zod | Validación declarativa y mensajes por campo |
| Almacenamiento seguro | `expo-secure-store` | Token fuera de `AsyncStorage` |
| Formato | Intl (`es-CO`) | Moneda COP y fechas sin dependencias extra |
| Formato de código | Prettier + ESLint | El aplicativo no usa Spotless (§1.4 del backlog) |
| Pruebas | Jest + React Native Testing Library | Cubre la tarea M7.8 |

La URL del backend se inyecta con `EXPO_PUBLIC_API_URL`. Ningún secreto vive en el código.

## 3. Estructura de carpetas

```text
app/
├── _layout.tsx                 proveedores: QueryClient, sesión, tema
├── (auth)/
│   ├── _layout.tsx
│   ├── Acceso.tsx              inicio de sesión
│   └── Registro.tsx            creación de cuenta
├── (cliente)/
│   ├── _layout.tsx             pestañas: Catálogo, Pedidos, Perfil
│   ├── Catalogo.tsx
│   ├── DetalleProducto.tsx
│   ├── Carrito.tsx
│   ├── Entrega.tsx             fecha, hora y notas
│   ├── Confirmacion.tsx        resumen y confirmación
│   ├── MisPedidos.tsx
│   ├── DetallePedido.tsx
│   ├── MiUbicacion.tsx
│   └── Perfil.tsx
└── (admin)/
    ├── _layout.tsx             pestañas: Productos, Pedidos
    ├── AdminProductos.tsx
    ├── EditarProducto.tsx
    ├── AdminPedidos.tsx
    └── DetallePedidoAdmin.tsx

src/
├── api/                        cliente HTTP y funciones por recurso
│   ├── clienteHttp.ts
│   ├── autenticacionApi.ts
│   ├── productosApi.ts
│   ├── pedidosApi.ts
│   └── whatsappApi.ts
├── componentes/                botones, campos, tarjetas, estados vacíos
├── funcionalidades/
│   ├── sesion/                 contexto, guardas y token
│   ├── carrito/                store de Zustand y selectores
│   └── pedidos/                hooks de TanStack Query
├── tipos/                      DTO equivalentes a los del backend
├── utilidades/                 moneda, fechas, errores, validaciones Zod
└── tema/                       colores, tipografía y espaciados
```

Convención de nombres: pantalla en PascalCase (`Catalogo.tsx`, `AdminPedidos.tsx`), hooks `use<Concepto>`, utilidades con verbo (`formatearPrecio`, `validarCantidad`).

## 4. Navegación y control de acceso

- **Grupo `(auth)`**: acceso y registro. Si hay token válido, redirige al grupo según el rol.
- **Grupo `(cliente)`**: catálogo, carrito, pedidos y perfil. Requiere token.
- **Grupo `(admin)`**: productos y pedidos. Requiere token con `rol = ADMIN`; si un cliente entra, el guardia lo devuelve al catálogo.
- La administradora puede usar el flujo de cliente (HU-03): el grupo `(admin)` se suma a las pestañas, no reemplaza al cliente.
- El token se lee de `expo-secure-store` al iniciar; si no existe o está vencido, se muestra `(auth)`.

```text
(auth) ──login/registro──► (cliente) ──rol ADMIN──► (admin)
   ▲                            │                      │
   └──────── cerrar sesión ─────┴──────────────────────┘
```

En web, `expo-secure-store` no está disponible: se usa un adaptador con `localStorage` documentado en `src/funcionalidades/sesion`.

## 5. Cliente HTTP

`clienteHttp.ts` centraliza:

- Base `EXPO_PUBLIC_API_URL` y `Content-Type: application/json`.
- Inserción del `Authorization: Bearer` desde el almacén de sesión.
- Tiempo límite de 15 s con `AbortController`.
- Lectura del `ProblemDetail` del backend y traducción de `codigo` a un mensaje en español para el usuario.
- 401 → cierra sesión y navega a `(auth)`; 403 → mensaje de permisos; 404 → «no encontrado o no disponible»; 409 → mensaje del conflicto; 422 → mensaje de regla de negocio.
- Sin reintentos automáticos en `POST` (evita pedidos duplicados). TanStack Query reintenta solo consultas `GET`.

## 6. Estado

| Estado | Dónde vive | Persistencia |
|---|---|---|
| Sesión y token | contexto de sesión + `expo-secure-store` | Sí |
| Catálogo, pedidos, perfil | caché de TanStack Query | No (se refresca al enfocar) |
| Carrito (borrador) | store de Zustand | Sí, `AsyncStorage` |
| Formularios | React Hook Form | No |

El carrito es **solo del cliente** hasta confirmar: refleja CU-09 (el resumen no persiste nada). Al confirmar, se llama a `POST /api/pedidos`, se limpia el carrito y se navega al detalle del pedido creado.

## 7. Pantallas por módulo

Cada pantalla implementa los tres estados visibles de RNF-02: **cargando**, **vacío** y **error**, con acción de reintento.

| Módulo | Ruta | Pantalla | Requisito | Contenido principal |
|---|---|---|---|---|
| M2 | `(auth)/Acceso` | `Acceso.tsx` | RF-01, RNF-01 | Correo y contraseña; error genérico de credenciales |
| M2 | `(auth)/Registro` | `Registro.tsx` | RF-01 | Nombre, correo, teléfono y contraseña; errores por campo |
| M3 | `(cliente)/MiUbicacion` | `MiUbicacion.tsx` | RF-02 | Conjunto y apartamento; guarda y relee |
| M4 | `(cliente)/Catalogo` | `Catalogo.tsx` | RF-03, RF-04, RF-05 | Tarjetas con nombre, precio, ingredientes y badge Disponible/Agotado |
| M4 | `(cliente)/DetalleProducto` | `DetalleProducto.tsx` | RF-04 | Descripción, ingredientes, precio y selector de cantidad |
| M4 | `(admin)/AdminProductos` | `AdminProductos.tsx` | RF-05 | Listado con inactivos, activar/desactivar y stock |
| M4 | `(admin)/EditarProducto` | `EditarProducto.tsx` | RF-05, RNF-05 | Precio, stock e ingredientes con validación |
| M5 | `(cliente)/Carrito` | `Carrito.tsx` | RF-06 | Cantidades editables, subtotal y total |
| M5 | `(cliente)/Entrega` | `Entrega.tsx` | RF-07 | Fecha, hora dentro de la ventana y notas |
| M5 | `(cliente)/Confirmacion` | `Confirmacion.tsx` | RF-08 | Resumen completo antes de confirmar |
| M5 | `(cliente)/MisPedidos` | `MisPedidos.tsx` | RF-10 | Historial con código, fecha de entrega y estado |
| M5 | `(cliente)/DetallePedido` | `DetallePedido.tsx` | RF-10 | Detalle completo del pedido |
| M5 | `(admin)/AdminPedidos` | `AdminPedidos.tsx` | RF-10 | Listado global con filtros por estado y fecha |
| M5 | `(admin)/DetallePedidoAdmin` | `DetallePedidoAdmin.tsx` | RF-10 | Datos del cliente, ubicación, ítems y cambio de estado |
| M6 | acción en `DetallePedido` | botón «Enviar por WhatsApp» | RF-09 | Obtiene el enlace del backend y abre WhatsApp |

Reglas de interfaz:

- El catálogo **nunca** muestra productos inactivos y los agotados aparecen como «Agotado» sin permitir agregar.
- La cantidad del carrito se limita al stock disponible (CU-07 A1).
- La fecha de entrega se elige con un selector que deshabilita días y horas fuera de la ventana.
- El cambio de estado solo ofrece las transiciones válidas; no se puede cancelar un pedido entregado.
- El historial del cliente no expone pedidos de otras personas: la app solo consume `GET /api/pedidos/mios`.

## 8. Formato de moneda y fechas

- Moneda: `new Intl.NumberFormat("es-CO", { style: "currency", currency: "COP", maximumFractionDigits: 0 })` → `$ 12.500`.
- Fechas: `Intl.DateTimeFormat("es-CO", { dateStyle: "medium" })` y horas en formato de 12 h con `America/Bogota`.
- El backend envía números y fechas ISO; la app formatea, nunca recalcula precios.

## 9. Integración con WhatsApp

1. La app llama a `POST /api/pedidos/{id}/whatsapp`.
2. Recibe `{ "enlace": "https://wa.me/57...?text=..." }`.
3. Abre con `Linking.openURL(enlace)`.
4. Si no hay aplicación instalada, el mismo enlace abre WhatsApp Web (HU-16).
5. Si `Linking` falla, se muestra el enlace con opción de copiarlo.

El número de la propietaria nunca se escribe en la app: lo entrega el backend desde `WHATSAPP_NUMERO`.

## 10. Validaciones de formulario

Los esquemas Zod replican las reglas del backend para dar retroalimentación inmediata, sin reemplazar la validación del servidor:

| Campo | Regla |
|---|---|
| Correo | Formato válido y obligatorio |
| Contraseña | Mínimo 8 caracteres (HU-01) |
| Teléfono | 7 a 15 dígitos |
| Conjunto y apartamento | Obligatorios, sin espacios únicamente |
| Cantidad | Entero mayor que cero y menor o igual al stock |
| Fecha de entrega | Futura y dentro de la ventana publicada por `GET /api/configuracion/horario` |
| Precio y stock (admin) | No negativos; precio mayor que cero |

## 11. Usabilidad y accesibilidad (RNF-02)

- Mensajes de error junto al campo que los origina, en español y sin códigos técnicos.
- Estados de carga con indicador y texto; estados vacíos con explicación y acción («Aún no tienes pedidos»).
- Objetivos táctiles de al menos 44 px, contraste AA y etiquetas accesibles en botones e imágenes.
- El flujo catálogo → carrito → entrega → confirmación es lineal y no exige ayuda externa.
- La app funciona en modo web con las mismas rutas.

## 12. Pruebas (M7.8)

| Tipo | Qué cubre |
|---|---|
| Unidad | `formatearPrecio`, `formatearFecha`, selectores del carrito y esquemas Zod |
| Componentes | Catálogo con productos activos/agotados, carrito con cantidades y total, estados vacío y error |
| Integración ligera | Cliente HTTP con API simulada (MSW): 401, 404, 409 y 422 traducidos a mensajes |

Criterio: `npm test` en verde y ninguna prueba depende del orden de ejecución.

## 13. Variables y entornos

| Variable | Uso | Obligatoria |
|---|---|---|
| `EXPO_PUBLIC_API_URL` | URL base del backend | Sí |

Perfiles: desarrollo apunta al backend local y web permite CORS del origen de Expo; producción apunta al backend desplegado.

## 14. Riesgos

| Riesgo | Mitigación |
|---|---|
| Diferencias entre Expo SDK y React Native | Fijar versiones con `expo install` y no actualizar por separado |
| `expo-secure-store` no existe en web | Adaptador con `localStorage` y aviso en la documentación |
| CORS bloquea la versión web en desarrollo | Configurar orígenes permitidos en el backend y documentarlo en M8.2 |
| Enlaces `wa.me` con acentos o saltos de línea | Los codifica el backend; la app solo abre la URL |
| Sin conexión al confirmar | Deshabilitar el botón durante el envío y mostrar error con reintento manual |

## 15. Trazabilidad

| Elemento | Ubicación |
|---|---|
| Tarea del backlog | M1.6 — ver `docs/modulos-backlog.md` |
| Requisitos | M1.1 — `docs/especificacion-requisitos.md` |
| Historias | M1.2 — `docs/historias-de-usuario.md` |
| Casos de uso | M1.3 — `docs/casos-de-uso.md` |
| Plan del backend | M1.5 — `docs/plan.md` |
| Implementación | Tareas móviles de M2 a M6 y M7.8 |
