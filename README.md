# MrCatFood

MrCatFood es un sistema de pedidos de sándwiches compuesto por una API REST en Spring Boot y
una aplicación Expo/React Native para clientes y administradores. Permite administrar el
catálogo, registrar ubicaciones, crear y consultar pedidos, actualizar su estado y preparar el
mensaje del pedido para WhatsApp.

## Tecnologías

| Componente | Tecnología |
|---|---|
| Backend | Java 21, Spring Boot 4.1.1, Gradle 9.7.1 |
| Datos | PostgreSQL 18, Flyway, Hibernate 7 |
| Seguridad | JWT HS256, Spring Security 7, BCrypt |
| Aplicación | Expo SDK 58, React Native 0.88, TypeScript |
| Pruebas | JUnit 5, MockMvc, Testcontainers, Jest |

La arquitectura del backend sigue las capas `Controller → Service → Repository → Entidad`. La
aplicación móvil se encuentra en [`movil/`](movil/) y consume exclusivamente los DTO de la API.

## Requisitos

- Git.
- Podman o Docker para PostgreSQL y las pruebas de integración.
- Node.js **22.13 o superior** y npm para Expo SDK 58.
- Java no tiene que estar instalado: el wrapper de Gradle aprovisiona Java 21 automáticamente.
  Tener un JDK 21 local evita la descarga inicial.
- Expo Go en el teléfono o un navegador moderno para probar la aplicación.

## 1. Obtener y configurar el proyecto

```bash
git clone https://github.com/DiegoBarbosaDeveloper/MrCatFood.git
cd MrCatFood
cp .env.example .env
```

En PowerShell, reemplaza el último comando por:

```powershell
Copy-Item .env.example .env
```

Edita `.env` y cambia, como mínimo, estos valores:

```properties
DB_PASSWORD=una_clave_local
ADMIN_EMAIL=admin@example.com
ADMIN_PASSWORD=una_clave_segura
JWT_SECRETO=un_secreto_aleatorio_de_al_menos_32_bytes
WHATSAPP_NUMERO=573001234567
```

El número de WhatsApp usa formato internacional, sin `+`, espacios ni guiones. `.env` está
ignorado por Git: nunca guardes allí credenciales que deban compartirse.

## 2. Iniciar PostgreSQL

El siguiente contenedor coincide con la URL y el usuario de `.env.example`:

```bash
podman run -d --name mrcatfood-postgres \
  -e POSTGRES_USER=admin \
  -e POSTGRES_PASSWORD=una_clave_local \
  -e POSTGRES_DB=mydatabase \
  -p 5432:5432 \
  docker.io/library/postgres:18
```

Usa exactamente la misma contraseña en `POSTGRES_PASSWORD` y `DB_PASSWORD`. Si utilizas Docker,
cambia `podman` por `docker`. En ejecuciones posteriores basta con:

```bash
podman start mrcatfood-postgres
```

## 3. Iniciar el backend

Linux o macOS:

```bash
./gradlew bootRun
```

Windows:

```powershell
.\gradlew.bat bootRun
```

Flyway crea y valida el esquema automáticamente. La API queda disponible en
`http://localhost:8080`. Al arrancar por primera vez se crea la cuenta administrativa indicada
en `.env`; volver a arrancar no la duplica ni cambia su contraseña.

Comprobación rápida:

```bash
curl http://localhost:8080/api/configuracion/horario
```

### Swagger y OpenAPI

- Swagger UI: <http://localhost:8080/swagger-ui.html>
- Documento OpenAPI: <http://localhost:8080/v3/api-docs>

Para probar una operación protegida, registra un usuario o inicia sesión, copia el campo `token`,
pulsa **Authorize** en Swagger y pega únicamente el JWT, sin escribir el prefijo `Bearer`.

## 4. Iniciar la aplicación

En otra terminal:

```bash
cd movil
npm ci
npm start
```

Expo muestra opciones para abrir Android, iOS o web. La URL del backend se configura con
`EXPO_PUBLIC_API_URL`:

```bash
EXPO_PUBLIC_API_URL=http://127.0.0.1:8080 npm start
```

En PowerShell:

```powershell
$env:EXPO_PUBLIC_API_URL="http://127.0.0.1:8080"
npm start
```

Escoge la URL según dónde se ejecute la aplicación:

| Entorno | URL habitual |
|---|---|
| Web o simulador iOS | `http://127.0.0.1:8080` |
| Emulador Android Studio | `http://10.0.2.2:8080` |
| Teléfono físico | `http://IP_LAN_DEL_EQUIPO:8080` |

El teléfono y el equipo deben estar en la misma red. El firewall debe permitir el puerto 8080
para usar un dispositivo físico.

## Comandos de desarrollo

### Backend

```bash
./gradlew spotlessApply       # aplica el formato obligatorio
./gradlew build               # formato, compilación y pruebas rápidas
./gradlew integrationTest     # integración con PostgreSQL efímero
./gradlew jacocoTestReport    # cobertura HTML y XML
```

El informe JaCoCo queda en `build/reports/jacoco/test/html/index.html`. Las pruebas de integración
requieren que Podman o Docker esté activo.

### Aplicación

```bash
cd movil
npm run lint
npm run typecheck
npm test
npm run test:coverage
```

El informe móvil queda en `movil/coverage/lcov-report/index.html`.

## Configuración

| Variable | Uso | Obligatoria |
|---|---|---|
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | Conexión PostgreSQL | Sí |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | Cuenta administrativa inicial | Sí |
| `JWT_SECRETO` | Firma HS256, mínimo 32 bytes | Sí |
| `JWT_EXPIRACION_MINUTOS` | Vigencia del token | No, 120 |
| `WHATSAPP_NUMERO` | Destinatario del enlace `wa.me` | Sí |
| `CORS_ORIGENES_PERMITIDOS` | Orígenes web separados por comas | No, localhost:8081 |
| `PEDIDOS_HORARIO_INICIO`, `PEDIDOS_HORARIO_FIN` | Horario de recepción | No |
| `PEDIDOS_ENTREGA_INICIO`, `PEDIDOS_ENTREGA_FIN` | Ventana de entrega | No |
| `PEDIDOS_ZONA` | Zona horaria | No, `America/Bogota` |
| `EXPO_PUBLIC_API_URL` | URL pública de la API para Expo | No, localhost:8080 |

Consulta [`.env.example`](.env.example) y [`.env.produccion.example`](.env.produccion.example) para
ver todos los nombres y formatos.

## Solución de problemas

### El backend no conecta con PostgreSQL

Comprueba que el contenedor esté activo, que el puerto 5432 no esté ocupado y que la contraseña
del contenedor coincida con `DB_PASSWORD`. El mensaje de Hibernate sobre el dialecto suele ser una
consecuencia de que la conexión falló; revisa unas líneas antes en el registro.

### La aplicación no conecta con la API

No uses `127.0.0.1` desde Android Emulator o un teléfono físico. Usa `10.0.2.2` en el emulador o la
IP local del equipo en el teléfono. Reinicia Expo después de cambiar `EXPO_PUBLIC_API_URL`.

### Las pruebas de integración no encuentran contenedores

Inicia Podman o Docker antes de ejecutar `integrationTest`. En Linux con Podman, el build detecta
el socket bajo `XDG_RUNTIME_DIR`; si utilizas otro socket, define `DOCKER_HOST` explícitamente.

### El correo administrativo ya existe

La cuenta se crea de forma idempotente. Cambiar `ADMIN_PASSWORD` después del primer arranque no
modifica la contraseña almacenada. Para desarrollo, elimina el contenedor y su volumen solo si
puedes perder todos los datos locales.

## Documentación

- [Especificación de requisitos](docs/especificacion-requisitos.md)
- [Historias de usuario](docs/historias-de-usuario.md)
- [Casos de uso](docs/casos-de-uso.md)
- [Modelo de datos](docs/modelo-datos.md)
- [Plan técnico del backend](docs/plan.md)
- [Plan de la aplicación](docs/plan-frontend.md)
- [Backlog y estado del proyecto](docs/modulos-backlog.md)
- [Reporte de cobertura](docs/reporte-cobertura.md)

## Seguridad

- No versiones `.env`, tokens, contraseñas, certificados ni archivos de firma.
- El registro público siempre crea usuarios `CLIENTE`.
- Las rutas `/api/admin/**` exigen rol `ADMIN`.
- Un recurso de otro cliente responde 404 para no revelar su existencia.
- Los totales y el stock siempre se validan nuevamente en el servidor.
