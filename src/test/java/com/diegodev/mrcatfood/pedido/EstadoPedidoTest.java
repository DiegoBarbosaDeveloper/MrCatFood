package com.diegodev.mrcatfood.pedido;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EstadoPedidoTest {

    @Test
    void permiteUnicamenteElFlujoHaciaAdelanteYLaCancelacionPreviaALaEntrega() {
        assertThat(EstadoPedido.PENDIENTE.permiteTransicionA(EstadoPedido.CONFIRMADO)).isTrue();
        assertThat(EstadoPedido.CONFIRMADO.permiteTransicionA(EstadoPedido.EN_PREPARACION))
                .isTrue();
        assertThat(EstadoPedido.EN_PREPARACION.permiteTransicionA(EstadoPedido.ENTREGADO)).isTrue();
        assertThat(EstadoPedido.PENDIENTE.permiteTransicionA(EstadoPedido.CANCELADO)).isTrue();
        assertThat(EstadoPedido.CONFIRMADO.permiteTransicionA(EstadoPedido.CANCELADO)).isTrue();
        assertThat(EstadoPedido.EN_PREPARACION.permiteTransicionA(EstadoPedido.CANCELADO)).isTrue();

        assertThat(EstadoPedido.PENDIENTE.permiteTransicionA(EstadoPedido.ENTREGADO)).isFalse();
        assertThat(EstadoPedido.ENTREGADO.permiteTransicionA(EstadoPedido.CANCELADO)).isFalse();
        assertThat(EstadoPedido.CANCELADO.permiteTransicionA(EstadoPedido.PENDIENTE)).isFalse();
    }
}
