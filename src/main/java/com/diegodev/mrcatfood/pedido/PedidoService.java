package com.diegodev.mrcatfood.pedido;

import com.diegodev.mrcatfood.comun.ApiException;
import com.diegodev.mrcatfood.comun.CodigoError;
import com.diegodev.mrcatfood.pedido.dto.CrearPedidoRequest;
import com.diegodev.mrcatfood.pedido.dto.DetalleResponse;
import com.diegodev.mrcatfood.pedido.dto.PedidoResponse;
import com.diegodev.mrcatfood.pedido.dto.PrevisualizacionPedidoResponse;
import com.diegodev.mrcatfood.pedido.dto.PrevisualizarPedidoRequest;
import com.diegodev.mrcatfood.pedido.dto.ProductoCantidadRequest;
import com.diegodev.mrcatfood.producto.Producto;
import com.diegodev.mrcatfood.producto.ProductoRepository;
import com.diegodev.mrcatfood.usuario.Usuario;
import com.diegodev.mrcatfood.usuario.UsuarioRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Calcula previsualizaciones y registra pedidos con descuento transaccional de inventario. */
@Service
public class PedidoService {

    private final UsuarioRepository usuarios;
    private final ProductoRepository productos;
    private final ValidadorFechaEntrega validadorFechaEntrega;
    private final ValidadorHorarioRecepcion validadorHorarioRecepcion;
    private final PedidoRepository pedidos;
    private final Clock reloj;

    public PedidoService(
            UsuarioRepository usuarios,
            ProductoRepository productos,
            ValidadorFechaEntrega validadorFechaEntrega,
            ValidadorHorarioRecepcion validadorHorarioRecepcion,
            PedidoRepository pedidos,
            Clock reloj) {
        this.usuarios = usuarios;
        this.productos = productos;
        this.validadorFechaEntrega = validadorFechaEntrega;
        this.validadorHorarioRecepcion = validadorHorarioRecepcion;
        this.pedidos = pedidos;
        this.reloj = reloj;
    }

    @Transactional(readOnly = true)
    public PrevisualizacionPedidoResponse previsualizarPedido(
            Long clienteId, PrevisualizarPedidoRequest solicitud) {
        return calcularPrevisualizacion(clienteId, solicitud, true);
    }

    @Transactional
    public PedidoResponse registrarPedido(Long clienteId, CrearPedidoRequest solicitud) {
        validadorHorarioRecepcion.validarHorarioRecepcion();
        PrevisualizacionPedidoResponse previsualizacion =
                calcularPrevisualizacion(clienteId, solicitud.comoPrevisualizacion(), false);
        Usuario cliente =
                usuarios.findById(clienteId).orElseThrow(PedidoService::clienteNoEncontrado);
        Map<Long, Producto> productosPorId =
                cargarProductosActivos(
                        solicitud.productos().stream()
                                .map(ProductoCantidadRequest::productoId)
                                .toList());

        solicitud.productos().stream()
                .sorted((primero, segundo) -> primero.productoId().compareTo(segundo.productoId()))
                .forEach(item -> descontarStock(item, productosPorId));

        Pedido pedido =
                new Pedido(
                        codigoTemporal(),
                        cliente,
                        solicitud.fechaEntrega(),
                        solicitud.horaEntrega(),
                        previsualizacion.notas(),
                        previsualizacion.subtotal(),
                        previsualizacion.total());
        for (DetalleResponse detalle : previsualizacion.detalles()) {
            pedido.agregarDetalle(
                    productosPorId.get(detalle.productoId()),
                    detalle.nombreProducto(),
                    detalle.precioUnitario(),
                    detalle.cantidad());
        }

        Pedido guardado = pedidos.saveAndFlush(pedido);
        guardado.asignarCodigoDefinitivo(generarCodigo(guardado.getId()));
        pedidos.flush();
        return convertirRespuesta(guardado);
    }

    private PrevisualizacionPedidoResponse calcularPrevisualizacion(
            Long clienteId, PrevisualizarPedidoRequest solicitud, boolean validarStock) {
        validadorFechaEntrega.validarFechaEntrega(
                solicitud.fechaEntrega(), solicitud.horaEntrega());
        Usuario cliente =
                usuarios.findById(clienteId).orElseThrow(PedidoService::clienteNoEncontrado);
        validarUbicacion(cliente);
        validarProductosSinRepetir(solicitud.productos());

        List<Long> productoIds =
                solicitud.productos().stream().map(ProductoCantidadRequest::productoId).toList();
        Map<Long, Producto> productosPorId = cargarProductosActivos(productoIds);

        List<DetalleResponse> detalles =
                solicitud.productos().stream()
                        .map(item -> calcularDetalle(item, productosPorId, validarStock))
                        .toList();
        BigDecimal subtotal =
                detalles.stream()
                        .map(DetalleResponse::subtotal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .setScale(2, RoundingMode.HALF_UP);

        return new PrevisualizacionPedidoResponse(
                detalles,
                subtotal,
                subtotal,
                cliente.getConjuntoResidencial(),
                cliente.getApartamento(),
                solicitud.fechaEntrega(),
                solicitud.horaEntrega(),
                normalizarNotas(solicitud.notas()));
    }

    private DetalleResponse calcularDetalle(
            ProductoCantidadRequest item,
            Map<Long, Producto> productosPorId,
            boolean validarStock) {
        Producto producto = productosPorId.get(item.productoId());
        if (producto == null) {
            throw recursoNoEncontrado();
        }
        if (validarStock && item.cantidad() > producto.getStock()) {
            throw new ApiException(
                    CodigoError.DATOS_INVALIDOS,
                    "La cantidad solicitada supera el stock de " + producto.getNombre(),
                    Map.of("productos", "Cantidad superior a la disponible"));
        }

        BigDecimal precio = producto.getPrecio().setScale(2, RoundingMode.HALF_UP);
        BigDecimal subtotal =
                precio.multiply(BigDecimal.valueOf(item.cantidad()))
                        .setScale(2, RoundingMode.HALF_UP);
        return new DetalleResponse(
                producto.getId(), producto.getNombre(), precio, item.cantidad(), subtotal);
    }

    private Map<Long, Producto> cargarProductosActivos(List<Long> productoIds) {
        return productos.findAllByIdInAndActivoTrue(productoIds).stream()
                .collect(Collectors.toMap(Producto::getId, Function.identity()));
    }

    private void descontarStock(ProductoCantidadRequest item, Map<Long, Producto> productosPorId) {
        Producto producto = productosPorId.get(item.productoId());
        if (producto == null) {
            throw recursoNoEncontrado();
        }
        int productosActualizados =
                productos.descontarStockSiDisponible(item.productoId(), item.cantidad());
        if (productosActualizados == 0) {
            throw new ApiException(
                    CodigoError.STOCK_INSUFICIENTE,
                    "No hay stock suficiente de " + producto.getNombre(),
                    Map.of("productos", "Cantidad superior a la disponible"));
        }
    }

    private PedidoResponse convertirRespuesta(Pedido pedido) {
        List<DetalleResponse> detalles =
                pedido.getDetalles().stream()
                        .map(
                                detalle ->
                                        new DetalleResponse(
                                                detalle.getProducto() == null
                                                        ? null
                                                        : detalle.getProducto().getId(),
                                                detalle.getNombreProducto(),
                                                detalle.getPrecioUnitario(),
                                                detalle.getCantidad(),
                                                detalle.getSubtotal()))
                        .toList();
        return new PedidoResponse(
                pedido.getId(),
                pedido.getCodigo(),
                pedido.getEstado(),
                detalles,
                pedido.getSubtotal(),
                pedido.getTotal(),
                pedido.getConjuntoResidencial(),
                pedido.getApartamento(),
                pedido.getFechaEntrega(),
                pedido.getHoraEntrega(),
                pedido.getNotas(),
                pedido.getCreadoEn());
    }

    private String generarCodigo(Long pedidoId) {
        int anio = LocalDate.now(reloj).getYear();
        return "MRC-%d-%06d".formatted(anio, pedidoId);
    }

    private String codigoTemporal() {
        return "TMP-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    private void validarUbicacion(Usuario cliente) {
        if (cliente.getConjuntoResidencial() == null || cliente.getApartamento() == null) {
            throw new ApiException(
                    CodigoError.DATOS_INVALIDOS,
                    "Registra tu conjunto residencial y apartamento antes de continuar",
                    Map.of("ubicacion", "La ubicación es obligatoria"));
        }
    }

    private void validarProductosSinRepetir(List<ProductoCantidadRequest> items) {
        Map<Long, Integer> repeticiones = new HashMap<>();
        for (ProductoCantidadRequest item : items) {
            int cantidad = repeticiones.merge(item.productoId(), 1, Integer::sum);
            if (cantidad > 1) {
                throw new ApiException(
                        CodigoError.DATOS_INVALIDOS,
                        "Un producto no puede aparecer más de una vez",
                        Map.of("productos", "Hay productos repetidos"));
            }
        }
    }

    private String normalizarNotas(String notas) {
        if (notas == null || notas.isBlank()) {
            return null;
        }
        return notas.trim();
    }

    private static ApiException recursoNoEncontrado() {
        return new ApiException(
                CodigoError.RECURSO_NO_ENCONTRADO,
                "El producto solicitado no existe o está inactivo");
    }

    private static ApiException clienteNoEncontrado() {
        return new ApiException(
                CodigoError.RECURSO_NO_ENCONTRADO, "El cliente autenticado no existe");
    }
}
