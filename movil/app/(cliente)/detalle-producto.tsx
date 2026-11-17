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
import { productosApi } from "../../src/api/productosApi";
import { formatearPrecio } from "../../src/utilidades/formatearPrecio";

export default function DetalleProducto() {
  const parametros = useLocalSearchParams<{ productoId?: string }>();
  const productoId = Number(parametros.productoId);
  const identificadorValido = Number.isInteger(productoId) && productoId > 0;
  const detalle = useQuery({
    queryKey: ["productos", "detalle", productoId],
    queryFn: () => productosApi.consultar(productoId),
    enabled: identificadorValido,
  });

  if (!identificadorValido) {
    return (
      <View style={estilos.estado}>
        <Text style={estilos.error}>El producto solicitado no es válido.</Text>
      </View>
    );
  }

  if (detalle.isPending) {
    return (
      <View accessibilityLiveRegion="polite" style={estilos.estado}>
        <ActivityIndicator size="large" />
        <Text>Cargando producto…</Text>
      </View>
    );
  }

  if (detalle.isError) {
    const noDisponible = detalle.error instanceof ErrorHttp && detalle.error.estado === 404;
    return (
      <View accessibilityLiveRegion="polite" style={estilos.estado}>
        <Text style={estilos.error}>
          {noDisponible
            ? "Este producto ya no está disponible."
            : "No se pudo consultar el producto."}
        </Text>
        <Pressable
          accessibilityRole="button"
          onPress={() => detalle.refetch()}
          style={estilos.boton}
        >
          <Text style={estilos.textoBoton}>Reintentar</Text>
        </Pressable>
      </View>
    );
  }

  const producto = detalle.data;
  return (
    <ScrollView contentContainerStyle={estilos.contenedor}>
      <View style={estilos.filaTitulo}>
        <Text style={estilos.titulo}>{producto.nombre}</Text>
        <View style={[estilos.badge, producto.disponible ? estilos.disponible : estilos.agotado]}>
          <Text
            style={producto.disponible ? estilos.textoDisponible : estilos.textoAgotado}
          >
            {producto.disponible ? "Disponible" : "Agotado"}
          </Text>
        </View>
      </View>

      <Text style={estilos.precio}>{formatearPrecio(producto.precio)}</Text>
      <Text style={estilos.descripcion}>{producto.descripcion}</Text>

      <View style={estilos.seccion}>
        <Text style={estilos.subtitulo}>Ingredientes</Text>
        {producto.ingredientes.map((ingrediente, indice) => (
          <Text key={`${ingrediente}-${indice}`} style={estilos.ingrediente}>
            • {ingrediente}
          </Text>
        ))}
      </View>

      <View style={estilos.inventario}>
        <Text style={estilos.subtitulo}>Cantidad disponible</Text>
        <Text style={estilos.cantidad}>{producto.stock}</Text>
      </View>

      {!producto.disponible ? (
        <Text accessibilityLiveRegion="polite" style={estilos.aviso}>
          Este producto está agotado temporalmente.
        </Text>
      ) : null}
    </ScrollView>
  );
}

const estilos = StyleSheet.create({
  contenedor: {
    flexGrow: 1,
    gap: 20,
    padding: 24,
  },
  filaTitulo: {
    alignItems: "flex-start",
    flexDirection: "row",
    gap: 12,
    justifyContent: "space-between",
  },
  titulo: {
    flex: 1,
    fontSize: 26,
    fontWeight: "700",
  },
  precio: {
    color: "#075985",
    fontSize: 24,
    fontWeight: "700",
  },
  descripcion: {
    color: "#374151",
    fontSize: 16,
    lineHeight: 24,
  },
  seccion: {
    gap: 8,
  },
  subtitulo: {
    fontSize: 17,
    fontWeight: "700",
  },
  ingrediente: {
    color: "#374151",
    fontSize: 16,
    lineHeight: 24,
  },
  inventario: {
    backgroundColor: "#f3f4f6",
    borderRadius: 10,
    gap: 6,
    padding: 16,
  },
  cantidad: {
    fontSize: 22,
    fontWeight: "700",
  },
  badge: {
    borderRadius: 999,
    paddingHorizontal: 10,
    paddingVertical: 6,
  },
  disponible: {
    backgroundColor: "#dcfce7",
  },
  agotado: {
    backgroundColor: "#fee2e2",
  },
  textoDisponible: {
    color: "#166534",
    fontWeight: "700",
  },
  textoAgotado: {
    color: "#991b1b",
    fontWeight: "700",
  },
  aviso: {
    color: "#991b1b",
    fontWeight: "600",
  },
  estado: {
    alignItems: "center",
    flex: 1,
    gap: 16,
    justifyContent: "center",
    padding: 24,
  },
  error: {
    color: "#b91c1c",
    textAlign: "center",
  },
  boton: {
    alignItems: "center",
    backgroundColor: "#0a7ea4",
    borderRadius: 8,
    justifyContent: "center",
    minHeight: 48,
    paddingHorizontal: 18,
  },
  textoBoton: {
    color: "#fff",
    fontWeight: "700",
  },
});
