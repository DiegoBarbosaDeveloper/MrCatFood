package com.diegodev.mrcatfood.pedido;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.diegodev.mrcatfood.comun.ApiException;
import com.diegodev.mrcatfood.comun.CodigoError;
import com.diegodev.mrcatfood.config.PropiedadesPedido;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;

class ValidadorFechaEntregaTest {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");
    private static final LocalDate HOY = LocalDate.of(2026, 11, 16);
    private static final PropiedadesPedido PROPIEDADES =
            new PropiedadesPedido(
                    LocalTime.of(8, 0),
                    LocalTime.of(20, 0),
                    LocalTime.of(11, 0),
                    LocalTime.of(21, 0),
                    ZONA);
    private final ValidadorFechaEntrega validador =
            new ValidadorFechaEntrega(relojA(LocalTime.of(10, 0)), PROPIEDADES);

    @Test
    void aceptaUnaEntregaFuturaEnLosExtremosDeLaVentana() {
        assertThatCode(() -> validador.validarFechaEntrega(HOY, LocalTime.of(11, 0)))
                .doesNotThrowAnyException();
        assertThatCode(() -> validador.validarFechaEntrega(HOY, LocalTime.of(21, 0)))
                .doesNotThrowAnyException();
    }

    @Test
    void rechazaUnaEntregaQueNoEsFutura() {
        ValidadorFechaEntrega validadorAlMediodia =
                new ValidadorFechaEntrega(relojA(LocalTime.of(12, 0)), PROPIEDADES);

        assertThatThrownBy(() -> validadorAlMediodia.validarFechaEntrega(HOY, LocalTime.of(12, 0)))
                .isInstanceOfSatisfying(
                        ApiException.class,
                        excepcion -> {
                            assertThat(excepcion.codigo())
                                    .isEqualTo(CodigoError.FECHA_ENTREGA_INVALIDA);
                            assertThat(excepcion.codigo().estado().value()).isEqualTo(422);
                            assertThat(excepcion.getMessage()).contains("deben ser futuras");
                        });
    }

    @Test
    void rechazaElBordeSuperiorInmediatamenteFueraDeLaVentana() {
        assertThatThrownBy(
                        () -> validador.validarFechaEntrega(HOY.plusDays(1), LocalTime.of(21, 1)))
                .isInstanceOfSatisfying(
                        ApiException.class,
                        excepcion ->
                                assertThat(excepcion.codigo())
                                        .isEqualTo(CodigoError.FECHA_ENTREGA_INVALIDA));
    }

    @Test
    void rechazaUnaHoraFueraDeLaVentanaIndicandoElHorarioValido() {
        assertThatThrownBy(
                        () -> validador.validarFechaEntrega(HOY.plusDays(1), LocalTime.of(10, 59)))
                .isInstanceOfSatisfying(
                        ApiException.class,
                        excepcion -> {
                            assertThat(excepcion.codigo())
                                    .isEqualTo(CodigoError.FECHA_ENTREGA_INVALIDA);
                            assertThat(excepcion.getMessage())
                                    .contains("11:00", "21:00", "America/Bogota");
                            assertThat(excepcion.campos()).containsKey("fechaEntrega");
                        });
    }

    private static Clock relojA(LocalTime hora) {
        return Clock.fixed(ZonedDateTime.of(HOY, hora, ZONA).toInstant(), ZONA);
    }
}
