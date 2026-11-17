import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { router } from "expo-router";
import { useMemo, useState } from "react";
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
import { useCarrito } from "../../src/funcionalidades/carrito/tiendaCarrito";
import type { PedidoRequest, PedidoResponse } from "../../src/tipos/pedido";
import { formatearPrecio } from "../../src/utilidades/formatearPrecio";

export default function Confirmacion() {
  const clienteConsultas = useQueryClient();
  const [pedidoConfirmado, setPedidoConfirmado] = useState<PedidoResponse | null>(null);
  const items = useCarrito((estado) => estado.items);
  const entrega = useCarrito((estado) => estado.entrega);
  const vaciarCarrito = useCarrito((estado) => estado.vaciarCarrito);
  const solicitud = useMemo<PedidoRequest>(
    () => ({
      productos: items.map((item) => ({
        productoId: item.productoId,
        cantidad: item.cantidad,
      })),
      fechaEntrega: entrega.fechaEntrega,
      horaEntrega: entrega.horaEntrega,
      notas: entrega.notas || null,
    }),
    [entrega, items],
  );
  const datosCompletos =
    items.length > 0 && Boolean(entrega.fechaEntrega) && Boolean(entrega.horaEntrega);
  const previsualizacion = useQuery({
    queryKey: ["pedidos", "previsualizacion", solicitud],
    queryFn: () => pedidosApi.previsualizar(solicitud),
    enabled: datosCompletos,
    retry: false,
  });
  const confirmacion = useMutation({
    mutationFn: pedidosApi.crear,
    onSuccess: (pedido) => {
      setPedidoConfirmado(pedido);
      vaciarCarrito();
      void clienteConsultas.invalidateQueries({ queryKey: ["productos"] });
      void clienteConsultas.invalidateQueries({ queryKey: ["pedidos"] });
    },
  });

  if (pedidoConfirmado) {
    return (
      <View style={estilos.estado}>
        <Text style={estilos.tituloExito}>¡Pedido confirmado!</Text>
        <Text style={estilos.codigo}>{pedidoConfirmado.codigo}</Text>
        <Text style={estilos.descripcionCentrada}>
          Estado actual: {pedidoConfirmado.estado.replaceAll("_", " ")}
        </Text>
        <Pressable
          accessibilityRole="button"
          onPress={() => router.replace("/(cliente)")}
          style={estilos.botonPrincipal}
        >
          <Text style={estilos.textoBoton}>Volver al catálogo</Text>
        </Pressable>
      </View>
    );
  }

  if (!datosCompletos) {
    return (
      <View style={estilos.estado}>
        <Text style={estilos.error}>Completa el carrito y los datos de entrega.</Text>
        <Pressable
          accessibilityRole="button"
          onPress={() => router.replace("/(cliente)/carrito")}
          style={estilos.botonPrincipal}
        >
          <Text style={estilos.textoBoton}>Volver al carrito</Text>
        </Pressable>
      </View>
    );
  }

  if (previsualizacion.isPending) {
    return (
      <View accessibilityLiveRegion="polite" style={estilos.estado}>
        <ActivityIndicator size="large" />
        <Text>Calculando el pedido…</Text>
      </View>
    );
  }

  if (previsualizacion.isError) {
    const mensaje =
      previsualizacion.error instanceof ErrorHttp
        ? previsualizacion.error.detalle
        : "No se pudo revisar el pedido.";
    return (
      <View accessibilityLiveRegion="polite" style={estilos.estado}>
        <Text style={estilos.error}>{mensaje}</Text>
        <Pressable
          accessibilityRole="button"
          onPress={() => previsualizacion.refetch()}
          style={estilos.botonPrincipal}
        >
          <Text style={estilos.textoBoton}>Reintentar</Text>
        </Pressable>
        <Pressable accessibilityRole="button" onPress={() => router.back()}>
          <Text style={estilos.enlace}>Corregir datos</Text>
        </Pressable>
      </View>
    );
  }

  const resumen = previsualizacion.data;
  const errorConfirmacion =
    confirmacion.error instanceof ErrorHttp
      ? confirmacion.error.detalle
      : confirmacion.isError
        ? "No se pudo confirmar el pedido."
        : null;

  return (
    <ScrollView contentContainerStyle={estilos.contenedor}>
      <Text style={estilos.titulo}>Confirma tu pedido</Text>

      <View style={estilos.seccion}>
        <Text style={estilos.subtitulo}>Productos</Text>
        {resumen.detalles.map((detalle, indice) => (
          <View key={`${detalle.productoId ?? "retirado"}-${indice}`} style={estilos.filaDetalle}>
            <View style={estilos.nombreDetalle}>
              <Text style={estilos.textoDestacado}>{detalle.nombreProducto}</Text>
              <Text style={estilos.textoSecundario}>
                {detalle.cantidad} × {formatearPrecio(detalle.precioUnitario)}
              </Text>
            </View>
            <Text style={estilos.textoDestacado}>{formatearPrecio(detalle.subtotal)}</Text>
          </View>
        ))}
      </View>

      <View style={estilos.seccion}>
        <Text style={estilos.subtitulo}>Entrega</Text>
        <Text>{resumen.conjuntoResidencial}</Text>
        <Text>{resumen.apartamento}</Text>
        <Text>
          {resumen.fechaEntrega} a las {resumen.horaEntrega.slice(0, 5)}
        </Text>
        {resumen.notas ? <Text style={estilos.textoSecundario}>Notas: {resumen.notas}</Text> : null}
      </View>

      <View style={estilos.totalContenedor}>
        <Text style={estilos.subtitulo}>Total</Text>
        <Text style={estilos.total}>{formatearPrecio(resumen.total)}</Text>
      </View>

      {errorConfirmacion ? (
        <Text accessibilityLiveRegion="polite" style={estilos.error}>
          {errorConfirmacion}
        </Text>
      ) : null}

      <Pressable
        accessibilityRole="button"
        disabled={confirmacion.isPending}
        onPress={() => confirmacion.mutate(solicitud)}
        style={[estilos.botonPrincipal, confirmacion.isPending ? estilos.deshabilitado : null]}
      >
        {confirmacion.isPending ? (
          <ActivityIndicator color="#fff" />
        ) : (
          <Text style={estilos.textoBoton}>Confirmar y enviar pedido</Text>
        )}
      </Pressable>
      <Pressable
        accessibilityRole="button"
        disabled={confirmacion.isPending}
        onPress={() => router.back()}
      >
        <Text style={estilos.enlace}>Corregir datos</Text>
      </Pressable>
    </ScrollView>
  );
}

const estilos = StyleSheet.create({
  contenedor: { flexGrow: 1, gap: 18, padding: 24 },
  titulo: { fontSize: 25, fontWeight: "700" },
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
  botonPrincipal: {
    alignItems: "center",
    backgroundColor: "#0a7ea4",
    borderRadius: 8,
    justifyContent: "center",
    minHeight: 50,
    paddingHorizontal: 18,
  },
  textoBoton: { color: "#fff", fontWeight: "700" },
  deshabilitado: { opacity: 0.6 },
  enlace: { color: "#075985", fontWeight: "700", minHeight: 44, textAlign: "center" },
  error: { color: "#b91c1c", textAlign: "center" },
  estado: {
    alignItems: "center",
    flex: 1,
    gap: 16,
    justifyContent: "center",
    padding: 24,
  },
  tituloExito: { fontSize: 24, fontWeight: "700", textAlign: "center" },
  codigo: { color: "#075985", fontSize: 22, fontWeight: "700" },
  descripcionCentrada: { color: "#4b5563", textAlign: "center" },
});
