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

    @Test
    void rechazaCualquierTransicionHaciaAtras() {
        assertThat(EstadoPedido.CONFIRMADO.permiteTransicionA(EstadoPedido.PENDIENTE)).isFalse();
        assertThat(EstadoPedido.EN_PREPARACION.permiteTransicionA(EstadoPedido.CONFIRMADO))
                .isFalse();
        assertThat(EstadoPedido.ENTREGADO.permiteTransicionA(EstadoPedido.EN_PREPARACION))
                .isFalse();
        assertThat(EstadoPedido.CANCELADO.permiteTransicionA(EstadoPedido.ENTREGADO)).isFalse();
    }

    @Test
    void rechazaLasTransicionesAUnMismoEstado() {
        for (EstadoPedido estado : EstadoPedido.values()) {
            assertThat(estado.permiteTransicionA(estado)).isFalse();
        }
    }
}
