import { useQuery } from "@tanstack/react-query";
import { useLocalSearchParams } from "expo-router";
import {
  ActivityIndicator,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from "react-native";
import { ErrorHttp } from "../../src/api/clienteHttp";
import { pedidosApi } from "../../src/api/pedidosApi";
import {
  formatearEstadoPedido,
  formatearFechaEntrega,
  formatearHoraEntrega,
} from "../../src/utilidades/formatearPedido";
import { formatearPrecio } from "../../src/utilidades/formatearPrecio";

export default function DetallePedido() {
  const parametros = useLocalSearchParams<{ pedidoId?: string }>();
  const pedidoId = Number(parametros.pedidoId);
  const identificadorValido = Number.isInteger(pedidoId) && pedidoId > 0;
  const pedido = useQuery({
    queryKey: ["pedidos", "detalle", pedidoId],
    queryFn: () => pedidosApi.consultar(pedidoId),
    enabled: identificadorValido,
  });

  if (!identificadorValido) {
    return (
      <View style={estilos.estadoPantalla}>
        <Text style={estilos.error}>El pedido solicitado no es válido.</Text>
      </View>
    );
  }

  if (pedido.isPending) {
    return (
      <View accessibilityLiveRegion="polite" style={estilos.estadoPantalla}>
        <ActivityIndicator size="large" />
        <Text>Cargando pedido…</Text>
      </View>
    );
  }

  if (pedido.isError) {
    const noEncontrado = pedido.error instanceof ErrorHttp && pedido.error.estado === 404;
    return (
      <View accessibilityLiveRegion="polite" style={estilos.estadoPantalla}>
        <Text style={estilos.error}>
          {noEncontrado ? "El pedido no existe o no está disponible." : "No se pudo cargar el pedido."}
        </Text>
        <Pressable
          accessibilityRole="button"
          onPress={() => pedido.refetch()}
          style={estilos.botonPrincipal}
        >
          <Text style={estilos.textoBoton}>Reintentar</Text>
        </Pressable>
      </View>
    );
  }

  const detalle = pedido.data;
  return (
    <ScrollView contentContainerStyle={estilos.contenedor}>
      <View style={estilos.encabezado}>
        <View style={estilos.datosCodigo}>
          <Text style={estilos.etiqueta}>Pedido</Text>
          <Text style={estilos.titulo}>{detalle.codigo}</Text>
        </View>
        <View style={estilos.badge}>
          <Text style={estilos.textoBadge}>{formatearEstadoPedido(detalle.estado)}</Text>
        </View>
      </View>

      <View style={estilos.seccion}>
        <Text style={estilos.subtitulo}>Productos</Text>
        {detalle.detalles.map((item, indice) => (
          <View key={`${item.productoId ?? "retirado"}-${indice}`} style={estilos.filaDetalle}>
            <View style={estilos.nombreDetalle}>
              <Text style={estilos.textoDestacado}>{item.nombreProducto}</Text>
              <Text style={estilos.textoSecundario}>
                {item.cantidad} × {formatearPrecio(item.precioUnitario)}
              </Text>
            </View>
            <Text style={estilos.textoDestacado}>{formatearPrecio(item.subtotal)}</Text>
          </View>
        ))}
      </View>

      <View style={estilos.seccion}>
        <Text style={estilos.subtitulo}>Entrega</Text>
        <Text>{detalle.conjuntoResidencial}</Text>
        <Text>{detalle.apartamento}</Text>
        <Text>
          {formatearFechaEntrega(detalle.fechaEntrega)}, {formatearHoraEntrega(detalle.horaEntrega)}
        </Text>
        {detalle.notas ? <Text style={estilos.textoSecundario}>Notas: {detalle.notas}</Text> : null}
      </View>

      <View style={estilos.totalContenedor}>
        <Text style={estilos.subtitulo}>Total</Text>
        <Text style={estilos.total}>{formatearPrecio(detalle.total)}</Text>
      </View>
    </ScrollView>
  );
}

const estilos = StyleSheet.create({
  contenedor: { flexGrow: 1, gap: 18, padding: 24 },
  encabezado: { alignItems: "center", flexDirection: "row", gap: 12 },
  datosCodigo: { flex: 1 },
  etiqueta: { color: "#6b7280" },
  titulo: { fontSize: 24, fontWeight: "700" },
  badge: { backgroundColor: "#dbeafe", borderRadius: 999, paddingHorizontal: 12, paddingVertical: 7 },
  textoBadge: { color: "#1e40af", fontWeight: "700" },
  seccion: {
    borderColor: "#d1d5db",
    borderRadius: 12,
    borderWidth: 1,
    gap: 10,
    padding: 16,
  },
  subtitulo: { fontSize: 17, fontWeight: "700" },
  filaDetalle: { alignItems: "center", flexDirection: "row", gap: 12 },
  nombreDetalle: { flex: 1, gap: 3 },
  textoDestacado: { fontWeight: "700" },
  textoSecundario: { color: "#4b5563" },
  totalContenedor: {
    alignItems: "center",
    backgroundColor: "#f3f4f6",
    borderRadius: 12,
    flexDirection: "row",
    justifyContent: "space-between",
    padding: 18,
  },
  total: { color: "#075985", fontSize: 24, fontWeight: "700" },
  estadoPantalla: {
    alignItems: "center",
    flex: 1,
    gap: 16,
    justifyContent: "center",
    padding: 24,
  },
  error: { color: "#b91c1c", textAlign: "center" },
  botonPrincipal: {
    alignItems: "center",
    backgroundColor: "#0a7ea4",
    borderRadius: 8,
    justifyContent: "center",
    minHeight: 48,
    paddingHorizontal: 18,
  },
  textoBoton: { color: "#fff", fontWeight: "700" },
});
