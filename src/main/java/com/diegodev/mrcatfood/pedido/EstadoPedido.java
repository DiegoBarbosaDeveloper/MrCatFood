package com.diegodev.mrcatfood.pedido;

/** Estados persistidos de un pedido. Las transiciones se implementan en M5.7. */
public enum EstadoPedido {
    PENDIENTE,
    CONFIRMADO,
    EN_PREPARACION,
    ENTREGADO,
    CANCELADO
}
