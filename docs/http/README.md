# Colección HTTP de MrCatFood

[`flujo-completo.http`](flujo-completo.http) recorre el flujo principal de la API y conserva en
variables los tokens e identificadores obtenidos. Está escrita para el HTTP Client de IntelliJ
IDEA; cada bloque también muestra una petición HTTP estándar y puede copiarse a otra herramienta.

## Preparación

1. Inicia PostgreSQL y el backend según el [README principal](../../README.md).
2. Copia `http-client.env.json` como `http-client.private.env.json` en esta carpeta.
3. En el archivo privado, escribe los valores reales de `ADMIN_EMAIL` y `ADMIN_PASSWORD`.
4. Selecciona el entorno `local` en el HTTP Client.
5. Ejecuta las peticiones en orden, de arriba hacia abajo.

El archivo privado está ignorado por Git. No escribas credenciales reales en
`http-client.env.json` ni en la colección.

## Consideraciones de horario

La colección programa la entrega para mañana a las 12:00. La creación del pedido solo funciona
dentro del horario de recepción configurado en el backend. Para una prueba fuera de ese horario,
arranca temporalmente el entorno local con una ventana que incluya la hora actual:

```properties
PEDIDOS_HORARIO_INICIO=00:00
PEDIDOS_HORARIO_FIN=23:59
```

No uses esa ampliación en producción.

## Resultado esperado

El flujo registra un cliente único, guarda su ubicación, selecciona un producto con stock,
previsualiza y crea el pedido, obtiene el enlace de WhatsApp y cierra la sesión. La sección final
inicia sesión como administrador, consulta el pedido creado y lo cambia de `PENDIENTE` a
`CONFIRMADO`.

Si el catálogo no contiene productos disponibles, repón el stock con la cuenta administrativa y
vuelve a ejecutar la consulta del catálogo.
