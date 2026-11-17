package com.diegodev.mrcatfood.pedido;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.springframework.stereotype.Component;

/** Construye el texto que el cliente envía a la propietaria por WhatsApp (RF-09). */
@Component
public class MensajePedidoFormatter {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public String formatear(Pedido pedido) {
        StringBuilder mensaje = new StringBuilder();
        mensaje.append("Nuevo pedido MrCatFood\n\n");
        mensaje.append("Código: ").append(pedido.getCodigo()).append('\n');
        mensaje.append("Cliente: ")
                .append(pedido.getCliente().getNombre())
                .append(" — ")
                .append(pedido.getCliente().getTelefono())
                .append('\n');
        mensaje.append("Ubicación: ")
                .append(pedido.getConjuntoResidencial())
                .append(", Apto ")
                .append(pedido.getApartamento())
                .append('\n');
        mensaje.append("Entrega: ").append(formatearFechaHora(pedido)).append('\n');
        mensaje.append("Notas: ").append(formatearNotas(pedido.getNotas())).append('\n');
        mensaje.append("\nDetalle:\n");
        for (DetallePedido detalle : pedido.getDetalles()) {
            mensaje.append("- ")
                    .append(detalle.getCantidad())
                    .append(" × ")
                    .append(detalle.getNombreProducto())
                    .append(" — ")
                    .append(formatearMoneda(detalle.getSubtotal()))
                    .append('\n');
        }
        mensaje.append("\nTotal: ").append(formatearMoneda(pedido.getTotal()));
        return mensaje.toString();
    }

    private String formatearFechaHora(Pedido pedido) {
        int hora = pedido.getHoraEntrega().getHour();
        int horaDoce = hora % 12;
        if (horaDoce == 0) {
            horaDoce = 12;
        }
        String sufijo = hora < 12 ? "a. m." : "p. m.";
        return String.format(
                Locale.ROOT,
                "%s a las %02d:%02d %s",
                pedido.getFechaEntrega().format(FORMATO_FECHA),
                horaDoce,
                pedido.getHoraEntrega().getMinute(),
                sufijo);
    }

    private String formatearNotas(String notas) {
        if (notas == null || notas.isBlank()) {
            return "Sin notas";
        }
        return notas.trim();
    }

    private String formatearMoneda(BigDecimal valor) {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(Locale.of("es", "CO"));
        simbolos.setGroupingSeparator('.');
        simbolos.setDecimalSeparator(',');
        DecimalFormat formato = new DecimalFormat("#,##0", simbolos);
        return "$ " + formato.format(valor);
    }
}
