# Historias de usuario — MrCatFood

| Campo | Valor |
|---|---|
| Proyecto | MrCatFood — sistema de pedidos de sándwiches |
| Tarea | M1.2 · Redactar historias de usuario con criterios de aceptación |
| Versión | 1.0 |
| Estado | En revisión (Pull Request) |
| Requisito base | `docs/especificacion-requisitos.md` |

## 1. Convenciones

Formato de historia:

> **Como** [rol] **quiero** [acción] **para** [beneficio]

Cada historia referencia el requisito funcional que la origina (`RF-xx`) y se descompone en criterios de aceptación verificables escritos en Given/When/Then.

Prioridad con MoSCoW: **Must** (obligatorio), **Should** (deseable), **Could** (deseable a futuro).

## 2. Resumen de historias

| ID | Historia | Requisito | Rol | Prioridad |
|---|---|---|---|---|
| HU-01 | Registrarme con mis datos | RF-01 | Cliente | Must |
| HU-02 | Iniciar sesión | RF-01 | Cliente | Must |
| HU-03 | Ingresar como administradora | RF-01, RNF-01 | Administradora | Must |
| HU-04 | Guardar mi conjunto y apartamento | RF-02 | Cliente | Must |
| HU-05 | Ver mis datos de ubicación | RF-02 | Cliente | Must |
| HU-06 | Ver el catálogo de sándwiches | RF-03 | Cliente | Must |
| HU-07 | Ver los detalles de un sándwich | RF-04 | Cliente | Must |
| HU-08 | Ver la disponibilidad de cada sándwich | RF-05 | Cliente | Must |
| HU-09 | Cambiar el precio de un sándwich | RF-05, RNF-05 | Administradora | Must |
| HU-10 | Reponer la cantidad disponible | RF-05 | Administradora | Must |
| HU-11 | Activar o desactivar un sándwich | RF-05 | Administradora | Must |
| HU-12 | Armar mi pedido con cantidades | RF-06 | Cliente | Must |
| HU-13 | Elegir la fecha y hora de entrega | RF-07 | Cliente | Must |
| HU-14 | Revisar mi pedido antes de enviarlo | RF-08 | Cliente | Must |
| HU-15 | Confirmar y enviar mi pedido | RF-08 | Cliente | Must |
| HU-16 | Enviar el pedido por WhatsApp | RF-09 | Cliente | Must |
| HU-17 | Consultar mis pedidos | RF-10 | Cliente | Must |
| HU-18 | Ver todos los pedidos del negocio | RF-10 | Administradora | Must |
| HU-19 | Cambiar el estado de un pedido | RF-10 | Administradora | Must |
| HU-20 | Intentar usar funciones administrativas sin permiso | RNF-01 | Cliente | Must |

## 3. Historias de detalle

### HU-01 — Registrarme con mis datos
**Requisito:** RF-01 · **Prioridad:** Must

> **Como** cliente nuevo **quiero** registrarme con mi nombre, correo, teléfono y contraseña **para** poder hacer pedidos.

**Criterios de aceptación**
- **Dado** un correo que no existe **cuando** envío datos válidos **entonces** la cuenta queda creada y recibo un token de acceso.
- **Dado** un correo ya registrado **cuando** intento registrarme **entonces** el sistema informa que el correo está en uso.
- **Dado** una contraseña de menos de 8 caracteres **cuando** me registro **entonces** el sistema la rechaza indicando el mínimo.
- **Dado** datos con formato inválido **cuando** los envío **entonces** el sistema señala el campo exacto con el error.

### HU-02 — Iniciar sesión
**Requisito:** RF-01 · **Prioridad:** Must

> **Como** cliente registrado **quiero** iniciar sesión con mi correo y contraseña **para** volver a pedir sin crear otra cuenta.

**Criterios de aceptación**
- **Dado** un correo y contraseña correctos **cuando** inicio sesión **entonces** recibo un token y acceso a mi cuenta.
- **Dado** un correo o contraseña incorrectos **cuando** inicio sesión **entonces** el sistema rechaza el acceso sin revelar cuál de los dos datos falló.
- **Dado** una sesión iniciada **cuando** cierro sesión **entonces** el token deja de servir.

### HU-03 — Ingresar como administradora
**Requisito:** RF-01, RNF-01 · **Prioridad:** Must

> **Como** administradora **quiero** ingresar con mi usuario y contraseña **para** administrar los pedidos y el catálogo desde mi celular.

**Criterios de aceptación**
- **Dado** las credenciales de la administradora **cuando** ingreso **entonces** accedo al panel de administración.
- **Dado** las credenciales de un cliente **cuando** ingreso **entonces** no veo ninguna función de administración.
- **Dado** que existe el rol administradora **cuando** entro **entonces** también puedo usar el flujo de cliente.

### HU-04 — Guardar mi conjunto y apartamento
**Requisito:** RF-02 · **Prioridad:** Must

> **Como** cliente **quiero** registrar mi conjunto residencial y apartamento **para** que mis pedidos lleguen al lugar correcto.

**Criterios de aceptación**
- **Dado** que estoy registrado **cuando** guardo conjunto y apartamento **entonces** quedan almacenados en mi perfil.
- **Dado** que dejo alguno de los dos campos vacío **cuando** guardo **entonces** el sistema indica qué campo falta.
- **Dado** que ya guardé mi ubicación **cuando** creo un pedido **entonces** el pedido la incluye.

### HU-05 — Ver mis datos de ubicación
**Requisito:** RF-02 · **Prioridad:** Must

> **Como** cliente **quiero** ver y corregir mi conjunto y apartamento **para** no tener datos viejos.

**Criterios de aceptación**
- **Dado** que tengo ubicación guardada **cuando** consulto mi perfil **entonces** veo conjunto y apartamento.
- **Dado** que modifico mi ubicación **cuando** vuelvo a consultar **entonces** veo el valor nuevo.
- **Dado** que ya hice pedidos **cuando** cambio mi ubicación **entonces** los pedidos anteriores conservan la ubicación original.

### HU-06 — Ver el catálogo de sándwiches
**Requisito:** RF-03 · **Prioridad:** Must

> **Como** cliente **quiero** ver los sándwiches disponibles **para** elegir qué pedir.

**Criterios de aceptación**
- **Dado** que existen productos activos e inactivos **cuando** consulto el catálogo **entonces** solo veo los activos.
- **Dado** que consulto el catálogo **cuando** lo visualizo **entonces** cada producto muestra nombre, precio y disponibilidad.
- **Dado** que la base está vacía **cuando** consulto el catálogo **entonces** veo un mensaje de que no hay productos, no una pantalla en blanco.

### HU-07 — Ver los detalles de un sándwich
**Requisito:** RF-04 · **Prioridad:** Must

> **Como** cliente **quiero** ver los ingredientes y el precio de un sándwich **para** decidir si lo quiero.

**Criterios de aceptación**
- **Dado** un producto activo **cuando** consulto su detalle **entonces** veo nombre, descripción, ingredientes y precio.
- **Dado** un producto que no existe o está inactivo **cuando** consulto su detalle **entonces** el sistema responde que no existe.

### HU-08 — Ver la disponibilidad de cada sándwich
**Requisito:** RF-05 · **Prioridad:** Must

> **Como** cliente **quiero** ver la cantidad disponible de cada sándwich **para** no pedir algo que ya se agotó.

**Criterios de aceptación**
- **Dado** un producto con inventario mayor que cero **cuando** consulto el catálogo **entonces** aparece como disponible.
- **Dado** un producto sin inventario **cuando** consulto el catálogo **entonces** aparece como agotado y no puedo agregar más unidades.

### HU-09 — Cambiar el precio de un sándwich
**Requisito:** RF-05, RNF-05 · **Prioridad:** Must

> **Como** ajusto el precio de un sándwich **para** que el negocio actualice sus tarifas sin esperar un despliegue.

**Criterios de aceptación**
- **Dado** que soy administradora **cuando** cambio el precio de un producto **entonces** el catálogo público muestra el nuevo valor.
- **Dado** un precio negativo o cero **cuando** lo guardo **entonces** el sistema lo rechaza.
- **Dado** un pedido ya creado **cuando** cambio el precio **entonces** ese pedido conserva el precio con el que se hizo.

### HU-10 — Reponer la cantidad disponible
**Requisito:** RF-05 · **Prioridad:** Must

> **Como** administradora **quiero** reponer el inventario de un sándwich **para** poder recibir más pedidos.

**Criterios de aceptación**
- **Dado** que soy administradora **cuando** aumento el inventario de un producto **entonces** la cantidad disponible aumenta.
- **Dado** un producto agotado **cuando** repongo su inventario **entonces** vuelve a aparecer como disponible para los clientes.
- **Dado** que dos clientes piden el último sándwich al mismo tiempo **cuando** ambos confirman **entonces** solo uno lo obtiene.

### HU-11 — Activar o desactivar un sándwich
**Requisito:** RF-05 · **Prioridad:** Must

> **Como** administradora **quiero** quitar un sándwich del catálogo sin borrarlo **para** dejar de ofrecerlo sin perder su información.

**Criterios de aceptación**
- **Dado** que soy administradora **cuando** desactivo un producto **entonces** deja de aparecer en el catálogo del cliente.
- **Dado** un producto desactivado **cuando** lo reactivo **entonces** vuelve a aparecer con los mismos datos.

### HU-12 — Armar mi pedido con cantidades
**Requisito:** RF-06 · **Prioridad:** Must

> **Como** cliente **quiero** seleccionar sándwiches y cantidades **para** pedir lo que necesito.

**Criterios de aceptación**
- **Dado** el catálogo **cuando** agrego productos con sus cantidades **entonces** veo el subtotal de mi pedido.
- **Dado** que agregué productos **cuando** modifico una cantidad **entonces** el subtotal se recalcula.
- **Dado** que quiero quitar un producto **cuando** lo elimino **entonces** desaparece del pedido.

### HU-13 — Elegir la fecha y hora de entrega
**Requisito:** RF-07 · **Prioridad:** Must

> **Como** cliente **quiero** indicar la fecha y hora de entrega **para** recibir el pedido cuando me sirva.

**Criterios de aceptación**
- **Dado** que estoy dentro del horario de recepción **cuando** elijo fecha y hora **entonces** el pedido las registra.
- **Dado** una fecha en el pasado **cuando** la elijo **entonces** el sistema la rechaza.
- **Dado** una hora fuera de la ventana de entrega **cuando** la elijo **entonces** el sistema la rechaza indicando el horario válido.

### HU-14 — Revisar mi pedido antes de enviarlo
**Requisito:** RF-08 · **Prioridad:** Must

> **Como** cliente **quiero** ver el resumen de mi pedido **para** confirmar antes de enviarlo.

**Criterios de aceptación**
- **Dado** un pedido en borrador **cuando** solicito el resumen **entonces** veo productos, cantidades, precios, total, ubicación, fecha, hora y notas.
- **Dado** un pedido en borrador **cuando** solo consulto el resumen **entonces** no queda registrado ningún pedido.

### HU-15 — Confirmar y enviar mi pedido
**Requisito:** RF-08 · **Prioridad:** Must

> **Como** cliente **quiero** confirmar mi pedido **para** que el negocio lo reciba.

**Criterios de aceptación**
- **Dado** un pedido revisado **cuando** lo confirmo **entonces** queda registrado con un código y el inventario se descuenta.
- **Dado** que el inventario no alcanza **cuando** confirmo **entonces** el sistema informa que no hay suficientes unidades.
- **Dado** un pedido confirmado **cuando** lo consulto **entonces** aparece en mi historial con estado pendiente.

### HU-16 — Enviar el pedido por WhatsApp
**Requisito:** RF-09 · **Prioridad:** Must

> **Como** cliente **quiero** enviar el pedido a la propietaria por WhatsApp **para** que sepa qué preparar.

**Criterios de aceptación**
- **Dado** un pedido confirmado **cuando** solicito el enlace de WhatsApp **entonces** obtengo una conversación con el número de la propietaria.
- **Dado** el mensaje generado **cuando** se abre **entonces** incluye código, cliente, ubicación, fecha y hora, notas, detalle de productos y total.
- **Dado** que WhatsApp no está instalado **cuando** pulso el botón **entonces** se abre la versión web.

### HU-17 — Consultar mis pedidos
**Requisito:** RF-10 · **Prioridad:** Must

> **Como** cliente **quiero** ver mis pedidos y su estado **para** saber si ya me lo entregaron.

**Criterios de aceptación**
- **Dado** que tengo pedidos **cuando** consulto mi historial **entonces** veo todos con su estado actual.
- **Dado** un pedido en particular **cuando** consulto su detalle **entonces** veo el resumen completo.
- **Dado** un pedido de otra persona **cuando** intento consultarlo **entonces** el sistema no me lo entrega.
- **Dado** que la administradora cambió el estado de mi pedido **cuando** consulto mi historial **entonces** veo el estado nuevo.

### HU-18 — Ver todos los pedidos del negocio
**Requisito:** RF-10 · **Prioridad:** Must

> **Como** administradora **quiero** ver todos los pedidos **para** saber qué preparar.

**Criterios de aceptación**
- **Dado** que soy administradora **cuando** consulto los pedidos **entonces** veo los de todos los clientes.
- **Dado** que hay muchos pedidos **cuando** filtro por estado o fecha **entonces** veo solo los que coinciden.
- **Dado** un pedido **cuando** lo abro **entonces** veo datos del cliente, ubicación, fecha y hora, productos y total.

### HU-19 — Cambiar el estado de un pedido
**Requisito:** RF-10 · **Prioridad:** Must

> **Como** administradora **quiero** actualizar el estado de un pedido **para** que el cliente sepa en qué etapa está.

**Criterios de aceptación**
- **Dado** un pedido pendiente **cuando** lo marco como confirmado, en preparación o entregado **entonces** el estado cambia.
- **Dado** un pedido ya entregado **cuando** intento devolverlo a pendiente **entonces** el sistema rechaza el cambio.
- **Dado** un pedido cancelado **cuando** consulto el inventario **entonces** las unidades del pedido vuelven a estar disponibles.

### HU-20 — Intentar usar funciones administrativas sin permiso
**Requisito:** RNF-01 · **Prioridad:** Must

> **Como** cliente **no quiero** poder entrar a funciones de administración **para** que el catálogo y los pedidos solo los gestione la administradora.

**Criterios de aceptación**
- **Dado** que tengo una sesión de cliente **cuando** intento una función administrativa **entonces** el sistema me lo niega.
- **Dado** que envío un registro indicando que soy administradora **entonces** la cuenta se crea como cliente.
- **Dado** que uso un token alterado **cuando** intento cualquier operación **entonces** el sistema rechaza mi acceso.

## 4. Cobertura de requisitos

| Requisito | Historias que lo cubren |
|---|---|
| RF-01 | HU-01, HU-02, HU-03 |
| RF-02 | HU-04, HU-05 |
| RF-03 | HU-06 |
| RF-04 | HU-07 |
| RF-05 | HU-08, HU-09, HU-10, HU-11 |
| RF-06 | HU-12 |
| RF-07 | HU-13 |
| RF-08 | HU-14, HU-15 |
| RF-09 | HU-16 |
| RF-10 | HU-17, HU-18, HU-19 |
| RNF-01 | HU-03, HU-20 |

## 5. Historia no cubierta de forma explícita

La historia asociada a **RNF-03 (disponibilidad en el horario de recepción)** no tiene historia propia: se manifesta dentro de HU-13, cuando el sistema rechaza una hora de entrega o un pedido fuera del horario. Los criterios de HU-13 cubren el comportamiento observable. Se mantiene esta decisión para no duplicar una historia que describe la misma interacción.

## 6. Trazabilidad de esta tarea

| Elemento | Ubicación |
|---|---|
| Tarea del backlog | M1.2 — ver `docs/modulos-backlog.md` |
| Requisitos de origen | M1.1 — `docs/especificacion-requisitos.md` |
| Diagramas derivados | M1.3 — `docs/casos-de-uso.md` y `docs/modelo-datos.md` |
| Tablas de pruebas | M7 — cada criterio se convierte en un caso de prueba |