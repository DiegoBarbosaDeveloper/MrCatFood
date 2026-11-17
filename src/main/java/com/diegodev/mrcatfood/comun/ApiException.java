package com.diegodev.mrcatfood.comun;

import java.util.Map;

/**
 * Excepción de negocio con código y estado HTTP. Se traduce a ProblemDetail por el handler global.
 */
public class ApiException extends RuntimeException {

    private final CodigoError codigo;
    private final Map<String, String> campos;

    public ApiException(CodigoError codigo, String mensaje) {
        this(codigo, mensaje, null);
    }

    public ApiException(CodigoError codigo, String mensaje, Map<String, String> campos) {
        super(mensaje);
        this.codigo = codigo;
        this.campos = campos == null ? Map.of() : Map.copyOf(campos);
    }

    public CodigoError codigo() {
        return codigo;
    }

    public Map<String, String> campos() {
        return campos;
    }
}
