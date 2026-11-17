package com.diegodev.mrcatfood.pedido;

import java.util.Map;
import java.util.Set;

/** Estados persistidos de un pedido y sus transiciones permitidas. */
public enum EstadoPedido {
    PENDIENTE,
    CONFIRMADO,
    EN_PREPARACION,
    ENTREGADO,
    CANCELADO;

    private static final Map<EstadoPedido, Set<EstadoPedido>> TRANSICIONES =
            Map.of(
                    PENDIENTE, Set.of(CONFIRMADO, CANCELADO),
                    CONFIRMADO, Set.of(EN_PREPARACION, CANCELADO),
                    EN_PREPARACION, Set.of(ENTREGADO, CANCELADO),
                    ENTREGADO, Set.of(),
                    CANCELADO, Set.of());

    public boolean permiteTransicionA(EstadoPedido nuevoEstado) {
        return TRANSICIONES.get(this).contains(nuevoEstado);
    }
}
