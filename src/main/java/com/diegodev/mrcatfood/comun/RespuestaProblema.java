package com.diegodev.mrcatfood.comun;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Escribe una respuesta de error en formato ProblemDetail sin depender del controlador. */
public final class RespuestaProblema {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private RespuestaProblema() {}

    public static void escribir(
            HttpServletResponse respuesta,
            int estado,
            String codigo,
            String titulo,
            String detalle,
            String instancia)
            throws IOException {
        respuesta.setStatus(estado);
        respuesta.setContentType("application/problem+json;charset=UTF-8");
        ObjectNode nodo = MAPPER.createObjectNode();
        nodo.put("type", "about:blank");
        nodo.put("title", titulo);
        nodo.put("status", estado);
        nodo.put("detail", detalle);
        if (instancia != null) {
            nodo.put("instance", instancia);
        }
        nodo.put("codigo", codigo);
        MAPPER.writeValue(respuesta.getOutputStream(), nodo);
    }
}
