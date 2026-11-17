package com.diegodev.mrcatfood.comun;

import org.springframework.http.HttpStatus;

/** Códigos de error del contrato REST (docs/plan.md §6.2). */
public enum CodigoError {
    DATOS_INVALIDOS(400, "Datos inválidos"),
    NO_AUTENTICADO(401, "No autenticado"),
    CREDENCIALES_INVALIDAS(401, "Credenciales inválidas"),
    ACCESO_DENEGADO(403, "Acceso denegado"),
    RECURSO_NO_ENCONTRADO(404, "Recurso no encontrado"),
    CORREO_EN_USO(409, "Correo en uso"),
    STOCK_INSUFICIENTE(409, "Stock insuficiente"),
    FUERA_DE_HORARIO(422, "Fuera de horario"),
    FECHA_ENTREGA_INVALIDA(422, "Fecha de entrega inválida"),
    TRANSICION_INVALIDA(422, "Transición inválida"),
    ERROR_INTERNO(500, "Error interno");

    private final HttpStatus estado;
    private final String titulo;

    CodigoError(int estado, String titulo) {
        this.estado = HttpStatus.valueOf(estado);
        this.titulo = titulo;
    }

    public HttpStatus estado() {
        return estado;
    }

    public String titulo() {
        return titulo;
    }

    public String codigo() {
        return name();
    }
}
