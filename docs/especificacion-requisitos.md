# Especificación de requisitos — MrCatFood

| Campo | Valor |
|---|---|
| Proyecto | MrCatFood — sistema de pedidos de sándwiches |
| Tarea | M1.1 · Especificar requisitos funcionales y no funcionales |
| Versión | 1.0 |
| Estado | En revisión (Pull Request) |

## 1. Propósito y alcance

MrCatFood permite que los clientes de un negocio de sándwiches OrderingHAM un pedido desde su celular y que la información llegue a la propietaria por WhatsApp. La propietaria administra el catálogo, los precios y la disponibilidad, y actualiza el estado de cada pedido.

**Alcance incluido:** registro e inicio de sesión de clientes, registro de ubicación (conjunto residencial y apartamento), catálogo de sándwiches, creación y consulta de pedidos, notificación por WhatsApp y administración de productos y pedidos.

**Fuera de alcance:** pagos en línea, cobros, facturación electrónica, inventario de insumos, múltiples sucursales, repartidores, y cualquier integración con servicios distintos de WhatsApp.

## 2. Actores

| Actor | Descripción | Acceso |
|---|---|---|
| Cliente | Persona que vive en un conjunto residencial y pide sándwiches | Se registra, navega el catálogo y crea sus propios pedidos |
| Administradora | Propietaria del negocio | Cuenta única protegida; administra catálogo, precios, disponibilidad y estados de pedidos. Puede además usar el flujo de cliente |
| Sistema | Componente automatizado | Valida datos, controla horarios, descuenta inventario y genera el mensaje de WhatsApp |

## 3. Convenciones de identificación

- Los requisitos funcionales se identifican como `RF-xx`.
- Los requisitos no funcionales se identifican como `RNF-xx`.
- Los identificadores son estables: el código, las pruebas y la matriz de trazabilidad los citan literalmente.
- La prioridad se asigna con MoSCoW: **Must** (obligatorio), **Should** (deseable), **Could** (deseable a futuro), **Won't** (fuera de alcance).

## 4. Requisitos funcionales

| ID | Nombre | Actor | Prioridad |
|---|---|---|---|
| RF-01 | Registro de clientes | Cliente | Must |
| RF-02 | Ubicación: conjunto residencial y apartamento | Cliente | Must |
| RF-03 | Catálogo de sándwiches | Cliente | Must |
| RF-04 | Información del producto | Cliente | Must |
| RF-05 | Disponibilidad y mantenimiento del catálogo | Administradora | Must |
| RF-06 | Creación de pedido | Cliente | Must |
| RF-07 | Hora de entrega | Cliente | Must |
| RF-08 | Confirmación del pedido | Cliente | Must |
| RF-09 | Notificación mediante WhatsApp | Cliente | Must |
| RF-10 | Consulta del pedido | Cliente, Administradora | Must |

### RF-01 — Registro de clientes

El sistema debe permitir que un cliente se registre con sus datos básicos e iniciar sesión de forma segura.

- **Datos mínimos:** nombre, correo electrónico, teléfono y contraseña.
- El correo electrónico es único en el sistema.
- La contraseña se almacena cifrada y nunca en texto plano.
- Al registrarse correctamente, el cliente recibe un token de acceso y queda con sesión iniciada.
- Las credenciales incorrectas no revelan si el correo existe o no.

**Criterio de aceptación:** dado un cliente sin cuenta, cuando envía datos válidos, entonces la API responde con código de creación y un token utilizable; cuando el correo ya existe, entonces responde con conflicto; cuando la contraseña es incorrecta, entonces responde con error de autenticación.

### RF-02 — Ubicación: conjunto residencial y apartamento

El sistema debe registrar como mínimo el conjunto residencial y el apartamento del cliente.

- Los datos se capturan como texto libre que escribe el cliente.
- Ambos campos son obligatorios para poder crear un pedido.
- El cliente puede consultarlos y modificarlos desde su perfil.

**Criterio de aceptación:** dado un cliente con sesión iniciada, cuando guarda su conjunto y apartamento, entonces la información queda almacenada y disponible para el pedido.

### RF-03 — Catálogo de sándwiches

El sistema debe mostrar los sándwiches disponibles.

- Solo se muestran los productos activos.
- El listado indica nombre, precio y disponibilidad.

**Criterio de aceptación:** dado un catálogo con productos activos e inactivos, cuando un cliente consulta el catálogo, entonces la respuesta incluye únicamente los activos.

### RF-04 — Información del producto

El sistema debe mostrar el nombre, los ingredientes y el precio de cada sándwich.

- Cada producto expone nombre, descripción, lista de ingredientes y precio.
- El precio se muestra en pesos colombianos.

**Criterio de aceptación:** dado un producto activo, cuando se consulta su detalle, entonces la respuesta contiene nombre, ingredientes y precio sin campos vacíos.

### RF-05 — Disponibilidad y mantenimiento del catálogo

El sistema debe mostrar la cantidad disponible de cada sándwich, y la información de productos, precios y disponibilidad debe poder actualizarse cuando el negocio lo requiera.

- Cada producto tiene una cantidad disponible (inventario) y un indicador de activo/inactivo.
- La administradora puede crear, editar y activar o desactivar productos.
- La cantidad se descuenta al confirmar el pedido y se repone cuando el pedido se cancela.

**Criterio de aceptación:** dado un producto con inventario, cuando se actualiza su precio o su inventario desde la administración, entonces el catálogo público refleja el cambio en la siguiente consulta.

### RF-06 — Creación de pedido

El sistema debe permitir seleccionar productos y cantidades.

- El cliente selecciona uno o varios productos del catálogo con su cantidad.
- El sistema calcula el subtotal y el total a partir de los precios vigentes.

**Criterio de aceptación:** dado un conjunto de productos seleccionados, cuando el cliente revisa el pedido, entonces los totales corresponden a la suma de cada producto por su cantidad.

### RF-07 — Hora de entrega

El sistema debe permitir seleccionar o registrar la hora solicitada para la entrega.

- El cliente indica fecha y hora de entrega, y puede dejar notas.
- La fecha de entrega no puede ser anterior al momento del pedido.
- La hora debe estar dentro de la ventana de entrega configurada.

**Criterio de aceptación:** dado un cliente, cuando registra una fecha y hora de entrega válidas, entonces el pedido queda asociado a esa fecha y hora.

### RF-08 — Confirmación del pedido

El sistema debe permitir al cliente revisar y confirmar el pedido antes de enviarlo.

- El cliente visualiza el resumen completo: productos, cantidades, precios, total, ubicación, fecha y hora.
- El pedido solo se registra después de la confirmación explícita.
- La previsualización no persiste información.

**Criterio de aceptación:** dado un pedido en borrador, cuando el cliente lo revisa sin confirmar, entonces no existe ningún registro del pedido en el sistema.

### RF-09 — Notificación mediante WhatsApp

El sistema debe enviar o facilitar el envío de la información del pedido a la propietaria mediante WhatsApp.

- El sistema genera un enlace de WhatsApp con el pedido ya redactado: código, datos del cliente, ubicación, fecha y hora, notas, detalle de productos y total.
- El cliente solo debe confirmar el envío en la aplicación de WhatsApp.

**Criterio de aceptación:** dado un pedido confirmado, cuando se genera el enlace, entonces la URL abre una conversación con el número de la propietaria y el mensaje incluye el detalle completo del pedido.

### RF-10 — Consulta del pedido

El sistema debe permitir consultar la información registrada del pedido.

- El cliente consulta sus propios pedidos y el detalle de cada uno.
- La administradora consulta todos los pedidos, puede filtrar y puede cambiar el estado.
- El cliente no puede consultar pedidos de otras personas.

**Criterio de aceptación:** dado un cliente con pedidos, cuando consulta su historial, entonces ve únicamente los suyos; cuando intenta consultar un pedido ajeno, entonces el sistema lo rechaza.

## 5. Requisitos no funcionales

| ID | Nombre | Prioridad |
|---|---|---|
| RNF-01 | Seguridad | Must |
| RNF-02 | Usabilidad | Should |
| RNF-03 | Disponibilidad | Must |
| RNF-04 | Integridad | Must |
| RNF-05 | Mantenibilidad | Must |

### RNF-01 — Seguridad

El sistema debe proteger el acceso a las funciones administrativas mediante autenticación.

- Las funciones administrativas exigen un rol específico además de un token válido.
- Un token de cliente en una función administrativa es rechazado.
- El registro público nunca permite crear una cuenta administrativa.

**Criterio de aceptación:** dado un cliente autenticado, cuando intenta usar una función administrativa, entonces el sistema lo rechaza con error de autorización.

### RNF-02 — Usabilidad

La interfaz debe presentar las opciones principales de manera clara para facilitar su utilización por clientes y administradora.

- El flujo principal (catálogo, selección, confirmación) es navegable sin ayuda externa.
- Los mensajes de error son comprensibles e indican qué corregir.
- Los estados de carga, vacío y error son visibles.

### RNF-03 — Disponibilidad

El sistema debe estar disponible durante el horario definido para la recepción de pedidos.

- El horario de recepción es configurable.
- Un intento de pedido fuera del horario se rechaza indicando el horario válido.
- El sistema responde con el horario vigente para que la interfaz lo muestre.

### RNF-04 — Integridad

La información del pedido debe conservar los datos registrados por el cliente sin alteraciones durante su procesamiento.

- El pedido guarda copia de los datos del cliente y de los precios aplicados.
- Un cambio posterior de precio o de ubicación del perfil no modifica pedidos ya registrados.

**Criterio de aceptación:** dado un pedido registrado, cuando el precio de un producto o la ubicación del cliente cambian, entonces el pedido anterior conserva sus valores originales.

### RNF-05 — Mantenibilidad

La información de productos, precios y disponibilidad debe poder actualizarse cuando el negocio lo requiera.

- El catálogo se administra desde la aplicación sin modificar el código.
- El esquema de base de datos se versiona con migraciones.
- La documentación de la API se genera automáticamente.

## 6. Reglas de negocio derivadas

Estas reglas no fueron pedidas explícitamente, pero son necesarias para que los requisitos anteriores sean verificables.

| ID | Regla | Origen |
|---|---|---|
| RN-01 | El inventario no puede quedar negativo | RF-05, RNF-04 |
| RN-02 | No se aceptan pedidos fuera del horario de recepción | RNF-03 |
| RN-03 | La fecha de entrega debe ser futura y dentro de la ventana de entrega | RF-07 |
| RN-04 | Cancelar un pedido devuelve el inventario de sus productos | RF-05 |
| RN-05 | Los estados de un pedido solo avanzan por transiciones permitidas | RF-10 |
| RN-06 | El correo se compara sin distinguir mayúsculas y sin espacios | RF-01 |

## 7. Flujo general del sistema

1. El cliente ingresa al aplicativo.
2. El cliente se registra o inicia sesión.
3. El cliente registra o selecciona su conjunto residencial y apartamento.
4. El cliente consulta el catálogo de sándwiches.
5. El sistema muestra ingredientes, precio y disponibilidad.
6. El cliente selecciona el producto y la cantidad.
7. El cliente selecciona la hora de entrega.
8. El cliente revisa la información del pedido.
9. El cliente confirma y envía el pedido.
10. La propietaria recibe la información del pedido mediante WhatsApp.
11. La propietaria prepara y entrega el pedido.

## 8. Glosario

| Término | Significado |
|---|---|
| Conjunto residencial | Edificio o complejo de apartamentos donde vive el cliente |
| Apartamento | Unidad habitacional dentro del conjunto |
| Producto | Sándwich disponible en el catálogo |
| Inventario o disponibilidad | Cantidad de unidades de un producto disponibles |
| Pedido | Solicitud con productos, cantidades, ubicación y hora de entrega |
| Horario de recepción | Franja horaria en la que se aceptan pedidos |
| Estado del pedido | Situación actual del pedido dentro de su ciclo de vida |

## 9. Trazabilidad de esta tarea

| Elemento | Ubicación |
|---|---|
| Tarea del backlog | M1.1 — ver `docs/modulos-backlog.md` |
| Ramas de trabajo | `M1.1`, `M1.2`, `M1.3`, `M1.4` |
| Historias de usuario derivadas | M1.2 — `docs/historias-de-usuario.md` |
| Diagramas derivados | M1.3 — `docs/casos-de-uso.md` y `docs/modelo-datos.md` |
| Plan técnico | `docs/plan.md` |
| Plan de la aplicación móvil | `docs/plan-frontend.md` |