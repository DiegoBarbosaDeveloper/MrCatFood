# Plan de despliegue — MrCatFood

Destino seleccionado para producción:

```text
Aplicación Android/iOS ─┐
                       ├──► Render (Spring Boot) ──► Supabase PostgreSQL
Expo Hosting (web) ────┘
```

- **Render** ejecuta el backend Java 21 desde el `Dockerfile` del repositorio.
- **Supabase** aporta PostgreSQL administrado; la aplicación se conecta por JDBC y Flyway.
- **Expo Build** genera los binarios Android/iOS y **Expo Hosting** publica la versión web.

Este documento no contiene credenciales. Los valores entre `<...>` deben reemplazarse en los
paneles de cada proveedor.

## 1. Verificación previa

Desde una copia limpia de `main`:

```bash
./gradlew build
./gradlew integrationTest

cd movil
npm ci
npm run lint
npm run typecheck
npm test
npx expo-doctor
```

No despliegues si una verificación falla. La suite de integración necesita Podman o Docker.

## 2. Base de datos en Supabase

1. Crea un proyecto en la región más cercana al servicio de Render.
2. Guarda la contraseña de base de datos en un gestor de secretos.
3. Abre **Connect → Session pooler → JDBC**.
4. Copia host, puerto, base y usuario. El Session Pooler usa puerto **5432** y funciona desde
   redes IPv4. No uses el Transaction Pooler 6543 con Flyway.
5. Añade `sslmode=require` a la URL JDBC.

Formato esperado:

```properties
DB_URL=jdbc:postgresql://<pooler-host>:5432/postgres?sslmode=require
DB_USER=postgres.<project-ref>
DB_PASSWORD=<contraseña-codificada-si-contiene-caracteres-reservados>
```

MrCatFood no usa Supabase Auth ni Data API. Restringe los esquemas expuestos por Data API o
desactívala para este proyecto; nunca incluyas claves de Supabase en la aplicación móvil.

Flyway aplica `V1`, `V2` y `V3` cuando el backend abre la primera conexión. Hibernate usa
`ddl-auto: validate`, por lo que no modifica el esquema por su cuenta. Nunca edites una migración
ya aplicada.

## 3. Backend en Render

Render no ofrece runtime Java nativo: [`Dockerfile`](../Dockerfile) crea una imagen Java 21 en dos
etapas y la ejecuta con un usuario sin privilegios. [`render.yaml`](../render.yaml) declara el
servicio y solicita los secretos durante la creación.

1. Fusiona el módulo terminado en `main`.
2. En Render selecciona **New → Blueprint** y conecta el repositorio.
3. Confirma el servicio `mrcatfood-api`, runtime Docker y región Virginia.
4. Introduce las variables marcadas `sync: false`.
5. Inicia el despliegue y revisa que Flyway termine sin errores.
6. Comprueba `https://<servicio>.onrender.com/api/configuracion/horario`.

Render proporciona `PORT`; `application.yaml` lo usa y conserva 8080 como valor local. El health
check consulta un endpoint público que también verifica que la aplicación haya arrancado.

### Variables del backend

| Variable | Tipo | Ejemplo o regla |
|---|---|---|
| `DB_URL` | Secreto | JDBC del Session Pooler con `sslmode=require` |
| `DB_USER` | Secreto | `postgres.<project-ref>` |
| `DB_PASSWORD` | Secreto | Contraseña de Supabase |
| `JWT_SECRETO` | Secreto | Render lo genera; mínimo 32 bytes |
| `JWT_EXPIRACION_MINUTOS` | Configuración | `120` |
| `ADMIN_EMAIL` | Secreto | Correo de la propietaria |
| `ADMIN_PASSWORD` | Secreto | Contraseña inicial robusta |
| `WHATSAPP_NUMERO` | Configuración sensible | Internacional, sin `+` ni separadores |
| `CORS_ORIGENES_PERMITIDOS` | Configuración | URL de Expo Hosting, sin `/` final |
| `PEDIDOS_HORARIO_INICIO/FIN` | Configuración | Horas `HH:mm` en zona del negocio |
| `PEDIDOS_ENTREGA_INICIO/FIN` | Configuración | Ventana de entrega `HH:mm` |
| `PEDIDOS_ZONA` | Configuración | `America/Bogota` |
| `PORT` | Plataforma | Render la define automáticamente |

El plan gratuito de Render puede suspender el servicio y producir arranques lentos. Sirve para
demostración, pero un entorno con requisito estricto de disponibilidad debe usar una instancia de
pago.

## 4. Configurar el proyecto Expo

La aplicación usa Expo SDK 58 y requiere Node.js 22.13 o superior. Los identificadores de tienda
son `com.diegodev.mrcatfood`; deben considerarse permanentes después de publicar.

```bash
cd movil
npx eas-cli@latest login
npx eas-cli@latest whoami
npx eas-cli@latest init
```

`eas init` agrega el identificador real del proyecto Expo a `app.json`. Revisa el cambio antes de
versionarlo y no copies el ID de otro proyecto.

Configura la URL HTTPS de Render como variable pública en Expo. No es un secreto: cualquier URL
incluida en una aplicación cliente es visible para sus usuarios.

```bash
npx eas-cli@latest env:set \
  --name EXPO_PUBLIC_API_URL \
  --value https://<servicio>.onrender.com \
  --environment preview \
  --visibility plaintext

npx eas-cli@latest env:set \
  --name EXPO_PUBLIC_API_URL \
  --value https://<servicio>.onrender.com \
  --environment production \
  --visibility plaintext
```

Comprueba ambas configuraciones:

```bash
npx eas-cli@latest env:list --environment preview
npx eas-cli@latest env:list --environment production
```

## 5. Publicar la aplicación web

`app.json` usa `web.output: single`, apropiado para esta SPA sin API Routes de Expo.

```bash
cd movil
npx eas-cli@latest env:pull --environment production
npx expo export --platform web
npx eas-cli@latest deploy --environment production
```

El primer despliegue crea una URL de vista previa. Cuando el equipo la valide:

```bash
npx eas-cli@latest deploy --environment production --prod
```

Después de conocer el dominio `https://<proyecto>.expo.app`, actualiza en Render:

```properties
CORS_ORIGENES_PERMITIDOS=https://<proyecto>.expo.app
```

Render debe redesplegarse para usar el nuevo origen. Si cambia `EXPO_PUBLIC_API_URL`, vuelve a
exportar y desplegar: Expo incorpora las variables públicas dentro del bundle.

## 6. Generar Android e iOS

[`movil/eas.json`](../movil/eas.json) ofrece dos perfiles:

- `preview`: distribución interna para pruebas en dispositivos.
- `production`: artefactos firmados para tiendas y versión incrementada por Expo.

```bash
cd movil
npx eas-cli@latest build --platform all --profile preview
npx eas-cli@latest build --platform all --profile production
```

La compilación iOS y su publicación requieren una cuenta Apple Developer. Android requiere una
cuenta de Google Play para distribuir el AAB. Expo puede administrar las credenciales de firma;
nunca se versionan archivos `.jks`, certificados ni perfiles.

Para enviar un build ya validado:

```bash
npx eas-cli@latest submit --platform android --profile production
npx eas-cli@latest submit --platform ios --profile production
```

## 7. Pruebas posteriores al despliegue

Ejecuta en este orden:

1. `GET /api/configuracion/horario` responde 200 por HTTPS.
2. Swagger UI carga y **Authorize** agrega el JWT a una operación protegida.
3. La colección [`http/flujo-completo.http`](http/flujo-completo.http) termina sin fallos usando la
   URL de Render y una cuenta administrativa de prueba.
4. La web permite registro, catálogo, ubicación, pedido y enlace de WhatsApp.
5. Un cliente recibe 403 al consultar `/api/admin/productos`.
6. Android/iOS consumen la URL HTTPS y abren correctamente el enlace `wa.me`.
7. Los logs no muestran contraseñas, tokens ni trazas internas para errores del cliente.

## 8. Operación, respaldo y rollback

- Activa respaldos de Supabase acordes con el plan contratado y prueba periódicamente la
  restauración en otro proyecto.
- Antes de una migración nueva, crea respaldo y verifica la migración sobre una copia.
- Para revertir código, usa **Rollback** en Render hacia una imagen anterior. No reviertas una
  migración editando archivos `V*` existentes; crea una migración correctiva.
- Expo Hosting conserva despliegues inmutables; reasigna el alias de producción a una versión
  anterior si la web falla.
- Para una aplicación nativa publicada, corrige JavaScript mediante Expo Update solo cuando el
  runtime siga siendo compatible; los cambios nativos exigen un build nuevo.
- Rota `JWT_SECRETO` si se filtra. La rotación cierra todas las sesiones existentes.

## 9. Lista de salida a producción

- [ ] Build backend, integración, lint, typecheck y pruebas móviles en verde.
- [ ] Supabase exige SSL y no expone tablas mediante Data API.
- [ ] Render usa secretos del panel, no archivos `.env` versionados.
- [ ] Flyway aplicó todas las migraciones.
- [ ] Health check y Swagger responden por HTTPS.
- [ ] `EXPO_PUBLIC_API_URL` apunta a Render en preview y producción.
- [ ] CORS contiene únicamente los dominios web autorizados.
- [ ] Flujo HTTP y ambos roles probados.
- [ ] Respaldos y rollback revisados por otra persona.
