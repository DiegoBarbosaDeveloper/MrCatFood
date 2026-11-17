package com.diegodev.mrcatfood.seguridad;

/** Token ausente, alterado, vencido o revocado. Se traduce a 401 en la capa web. */
public class TokenInvalidoException extends RuntimeException {

    public TokenInvalidoException(String mensaje) {
        super(mensaje);
    }

    public TokenInvalidoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
