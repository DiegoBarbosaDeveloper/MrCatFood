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

class ValidadorHorarioRecepcionTest {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");
    private static final LocalDate FECHA = LocalDate.of(2026, 11, 16);
    private static final PropiedadesPedido PROPIEDADES =
            new PropiedadesPedido(
                    LocalTime.of(8, 0),
                    LocalTime.of(20, 0),
                    LocalTime.of(11, 0),
                    LocalTime.of(21, 0),
                    ZONA);

    @Test
    void aceptaLosExtremosDelHorarioDeRecepcion() {
        assertThatCode(() -> validadorA(LocalTime.of(8, 0)).validarHorarioRecepcion())
                .doesNotThrowAnyException();
        assertThatCode(() -> validadorA(LocalTime.of(20, 0)).validarHorarioRecepcion())
                .doesNotThrowAnyException();
    }

    @Test
    void rechazaUnaHoraAnteriorIndicandoElHorarioValido() {
        assertThatThrownBy(() -> validadorA(LocalTime.of(7, 59)).validarHorarioRecepcion())
                .isInstanceOfSatisfying(
                        ApiException.class,
                        excepcion -> {
                            assertThat(excepcion.codigo()).isEqualTo(CodigoError.FUERA_DE_HORARIO);
                            assertThat(excepcion.codigo().estado().value()).isEqualTo(422);
                            assertThat(excepcion.getMessage())
                                    .contains("08:00", "20:00", "America/Bogota");
                        });
    }

    @Test
    void rechazaUnaHoraPosteriorAlHorario() {
        assertThatThrownBy(() -> validadorA(LocalTime.of(20, 1)).validarHorarioRecepcion())
                .isInstanceOfSatisfying(
                        ApiException.class,
                        excepcion ->
                                assertThat(excepcion.codigo())
                                        .isEqualTo(CodigoError.FUERA_DE_HORARIO));
    }

    private ValidadorHorarioRecepcion validadorA(LocalTime hora) {
        ZonedDateTime fechaHora = ZonedDateTime.of(FECHA, hora, ZONA);
        return new ValidadorHorarioRecepcion(Clock.fixed(fechaHora.toInstant(), ZONA), PROPIEDADES);
    }
}
