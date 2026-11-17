package com.diegodev.mrcatfood.pedido;

import com.diegodev.mrcatfood.comun.ApiException;
import com.diegodev.mrcatfood.comun.CodigoError;
import com.diegodev.mrcatfood.pedido.dto.ClientePedidoResponse;
import com.diegodev.mrcatfood.pedido.dto.CrearPedidoRequest;
import com.diegodev.mrcatfood.pedido.dto.DetalleResponse;
import com.diegodev.mrcatfood.pedido.dto.PaginaResponse;
import com.diegodev.mrcatfood.pedido.dto.PedidoResponse;
import com.diegodev.mrcatfood.pedido.dto.PedidoResumenResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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

    @Transactional(readOnly = true)
    public List<PedidoResponse> consultarPedidosCliente(Long clienteId) {
        return pedidos.buscarTodosDelCliente(clienteId).stream()
                .map(this::convertirRespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponse consultarPedidoCliente(Long clienteId, Long pedidoId) {
        Pedido pedido =
                pedidos.buscarPorIdYCliente(pedidoId, clienteId)
                        .orElseThrow(PedidoService::pedidoNoEncontrado);
        return convertirRespuesta(pedido);
    }

    @Transactional(readOnly = true)
    public PaginaResponse<PedidoResumenResponse> consultarPedidosAdministracion(
            EstadoPedido estado,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            int pagina,
            int tamano) {
        validarPaginacionYRango(fechaDesde, fechaHasta, pagina, tamano);
        Specification<Pedido> filtros = (raiz, consulta, constructor) -> constructor.conjunction();
        if (estado != null) {
            filtros =
                    filtros.and(
                            (raiz, consulta, constructor) ->
                                    constructor.equal(raiz.get("estado"), estado));
        }
        if (fechaDesde != null) {
            filtros =
                    filtros.and(
                            (raiz, consulta, constructor) ->
                                    constructor.greaterThanOrEqualTo(
                                            raiz.get("fechaEntrega"), fechaDesde));
        }
        if (fechaHasta != null) {
            filtros =
                    filtros.and(
                            (raiz, consulta, constructor) ->
                                    constructor.lessThanOrEqualTo(
                                            raiz.get("fechaEntrega"), fechaHasta));
        }

        Page<Pedido> resultado =
                pedidos.findAll(
                        filtros,
                        PageRequest.of(pagina, tamano, Sort.by(Sort.Direction.DESC, "creadoEn")));
        List<PedidoResumenResponse> contenido =
                resultado.getContent().stream().map(this::convertirResumen).toList();
        return new PaginaResponse<>(
                contenido,
                resultado.getNumber(),
                resultado.getSize(),
                resultado.getTotalElements(),
                resultado.getTotalPages());
    }

    @Transactional(readOnly = true)
    public PedidoResponse consultarPedidoAdministracion(Long pedidoId) {
        Pedido pedido =
                pedidos.buscarDetalleAdministrativo(pedidoId)
                        .orElseThrow(PedidoService::pedidoNoEncontrado);
        return convertirRespuesta(pedido);
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
                convertirCliente(pedido.getCliente()),
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

    private PedidoResumenResponse convertirResumen(Pedido pedido) {
        return new PedidoResumenResponse(
                pedido.getId(),
                pedido.getCodigo(),
                pedido.getEstado(),
                convertirCliente(pedido.getCliente()),
                pedido.getConjuntoResidencial(),
                pedido.getApartamento(),
                pedido.getFechaEntrega(),
                pedido.getHoraEntrega(),
                pedido.getTotal(),
                pedido.getCreadoEn());
    }

    private ClientePedidoResponse convertirCliente(Usuario cliente) {
        return new ClientePedidoResponse(
                cliente.getId(), cliente.getNombre(), cliente.getTelefono());
    }

    private void validarPaginacionYRango(
            LocalDate fechaDesde, LocalDate fechaHasta, int pagina, int tamano) {
        if (pagina < 0 || tamano < 1 || tamano > 100) {
            throw new ApiException(
                    CodigoError.DATOS_INVALIDOS,
                    "La página debe ser positiva y el tamaño debe estar entre 1 y 100");
        }
        if (fechaDesde != null && fechaHasta != null && fechaDesde.isAfter(fechaHasta)) {
            throw new ApiException(
                    CodigoError.DATOS_INVALIDOS,
                    "La fecha inicial no puede ser posterior a la fecha final");
        }
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

    private static ApiException pedidoNoEncontrado() {
        return new ApiException(
                CodigoError.RECURSO_NO_ENCONTRADO, "El pedido solicitado no existe");
    }
}
