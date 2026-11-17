import { useQuery } from "@tanstack/react-query";
import { router } from "expo-router";
import {
  ActivityIndicator,
  FlatList,
  Pressable,
  StyleSheet,
  Text,
  View,
} from "react-native";
import { pedidosApi } from "../../src/api/pedidosApi";
import type { EstadoPedido, PedidoResponse } from "../../src/tipos/pedido";
import {
  formatearEstadoPedido,
  formatearFechaEntrega,
  formatearHoraEntrega,
} from "../../src/utilidades/formatearPedido";
import { formatearPrecio } from "../../src/utilidades/formatearPrecio";

export default function MisPedidos() {
  const pedidos = useQuery({
    queryKey: ["pedidos", "mios"],
    queryFn: pedidosApi.listarMios,
  });

  if (pedidos.isPending) {
    return (
      <View accessibilityLiveRegion="polite" style={estilos.estadoPantalla}>
        <ActivityIndicator size="large" />
        <Text>Cargando tus pedidos…</Text>
      </View>
    );
  }

  if (pedidos.isError) {
    return (
      <View accessibilityLiveRegion="polite" style={estilos.estadoPantalla}>
        <Text style={estilos.error}>No se pudieron cargar tus pedidos.</Text>
        <Pressable
          accessibilityRole="button"
          onPress={() => pedidos.refetch()}
          style={estilos.botonPrincipal}
        >
          <Text style={estilos.textoBoton}>Reintentar</Text>
        </Pressable>
      </View>
    );
  }

  function abrirPedido(pedido: PedidoResponse) {
    router.push({
      pathname: "/(cliente)/detalle-pedido",
      params: { pedidoId: pedido.id.toString() },
    });
  }

  return (
    <FlatList
      contentContainerStyle={estilos.lista}
      data={pedidos.data}
      keyExtractor={(pedido) => pedido.id.toString()}
      ListHeaderComponent={<Text style={estilos.titulo}>Mis pedidos</Text>}
      ListEmptyComponent={
        <View style={estilos.vacio}>
          <Text style={estilos.tituloVacio}>Aún no tienes pedidos</Text>
          <Text style={estilos.descripcion}>Cuando confirmes uno, aparecerá aquí.</Text>
          <Pressable
            accessibilityRole="button"
            onPress={() => router.push("/(cliente)")}
            style={estilos.botonPrincipal}
          >
            <Text style={estilos.textoBoton}>Ver catálogo</Text>
          </Pressable>
        </View>
      }
      onRefresh={() => pedidos.refetch()}
      refreshing={pedidos.isRefetching}
      renderItem={({ item }) => (
        <Pressable
          accessibilityHint="Abre el detalle del pedido"
          accessibilityLabel={`${item.codigo}, ${formatearEstadoPedido(item.estado)}`}
          accessibilityRole="button"
          onPress={() => abrirPedido(item)}
          style={({ pressed }) => [estilos.tarjeta, pressed ? estilos.presionado : null]}
        >
          <View style={estilos.fila}>
            <Text style={estilos.codigo}>{item.codigo}</Text>
            <BadgeEstado estado={item.estado} />
          </View>
          <Text style={estilos.fecha}>
            Entrega: {formatearFechaEntrega(item.fechaEntrega)}, {formatearHoraEntrega(item.horaEntrega)}
          </Text>
          <View style={estilos.fila}>
            <Text style={estilos.descripcion}>{item.detalles.length} producto(s)</Text>
            <Text style={estilos.total}>{formatearPrecio(item.total)}</Text>
          </View>
        </Pressable>
      )}
    />
  );
}

function BadgeEstado({ estado }: { estado: EstadoPedido }) {
  const esFinal = estado === "ENTREGADO";
  const esCancelado = estado === "CANCELADO";
  return (
    <View
      style={[
        estilos.badge,
        esCancelado ? estilos.badgeCancelado : esFinal ? estilos.badgeEntregado : estilos.badgeActivo,
      ]}
    >
      <Text
        style={[
          estilos.textoBadge,
          esCancelado
            ? estilos.textoCancelado
            : esFinal
              ? estilos.textoEntregado
              : estilos.textoActivo,
        ]}
      >
        {formatearEstadoPedido(estado)}
      </Text>
    </View>
  );
}

const estilos = StyleSheet.create({
  lista: { flexGrow: 1, gap: 14, padding: 20 },
  titulo: { fontSize: 26, fontWeight: "700", marginBottom: 4 },
  tarjeta: {
    borderColor: "#d1d5db",
    borderRadius: 12,
    borderWidth: 1,
    gap: 11,
    padding: 16,
  },
  presionado: { opacity: 0.7 },
  fila: { alignItems: "center", flexDirection: "row", gap: 10, justifyContent: "space-between" },
  codigo: { flex: 1, fontSize: 17, fontWeight: "700" },
  fecha: { color: "#374151" },
  descripcion: { color: "#6b7280" },
  total: { color: "#075985", fontSize: 18, fontWeight: "700" },
  badge: { borderRadius: 999, paddingHorizontal: 10, paddingVertical: 5 },
  badgeActivo: { backgroundColor: "#dbeafe" },
  badgeEntregado: { backgroundColor: "#dcfce7" },
  badgeCancelado: { backgroundColor: "#fee2e2" },
  textoBadge: { fontSize: 12, fontWeight: "700" },
  textoActivo: { color: "#1e40af" },
  textoEntregado: { color: "#166534" },
  textoCancelado: { color: "#991b1b" },
  estadoPantalla: {
    alignItems: "center",
    flex: 1,
    gap: 16,
    justifyContent: "center",
    padding: 24,
  },
  vacio: { alignItems: "center", gap: 12, paddingVertical: 60 },
  tituloVacio: { fontSize: 19, fontWeight: "700", textAlign: "center" },
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
