package com.diegodev.mrcatfood.pedido;

import com.diegodev.mrcatfood.comun.ApiException;
import com.diegodev.mrcatfood.comun.CodigoError;
import com.diegodev.mrcatfood.pedido.dto.DetalleResponse;
import com.diegodev.mrcatfood.pedido.dto.PrevisualizacionPedidoResponse;
import com.diegodev.mrcatfood.pedido.dto.PrevisualizarPedidoRequest;
import com.diegodev.mrcatfood.pedido.dto.ProductoCantidadRequest;
import com.diegodev.mrcatfood.producto.Producto;
import com.diegodev.mrcatfood.producto.ProductoRepository;
import com.diegodev.mrcatfood.usuario.Usuario;
import com.diegodev.mrcatfood.usuario.UsuarioRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Calcula previsualizaciones y, en tareas posteriores, registrará pedidos. */
@Service
public class PedidoService {

    private final UsuarioRepository usuarios;
    private final ProductoRepository productos;
    private final ValidadorFechaEntrega validadorFechaEntrega;

    public PedidoService(
            UsuarioRepository usuarios,
            ProductoRepository productos,
            ValidadorFechaEntrega validadorFechaEntrega) {
        this.usuarios = usuarios;
        this.productos = productos;
        this.validadorFechaEntrega = validadorFechaEntrega;
    }

    @Transactional(readOnly = true)
    public PrevisualizacionPedidoResponse previsualizarPedido(
            Long clienteId, PrevisualizarPedidoRequest solicitud) {
        validadorFechaEntrega.validarFechaEntrega(
                solicitud.fechaEntrega(), solicitud.horaEntrega());
        Usuario cliente =
                usuarios.findById(clienteId).orElseThrow(PedidoService::clienteNoEncontrado);
        validarUbicacion(cliente);
        validarProductosSinRepetir(solicitud.productos());

        List<Long> productoIds =
                solicitud.productos().stream().map(ProductoCantidadRequest::productoId).toList();
        Map<Long, Producto> productosPorId =
                productos.findAllByIdInAndActivoTrue(productoIds).stream()
                        .collect(Collectors.toMap(Producto::getId, Function.identity()));

        List<DetalleResponse> detalles =
                solicitud.productos().stream()
                        .map(item -> calcularDetalle(item, productosPorId))
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
            ProductoCantidadRequest item, Map<Long, Producto> productosPorId) {
        Producto producto = productosPorId.get(item.productoId());
        if (producto == null) {
            throw recursoNoEncontrado();
        }
        if (item.cantidad() > producto.getStock()) {
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
