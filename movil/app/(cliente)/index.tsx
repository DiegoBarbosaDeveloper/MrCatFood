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
import { productosApi } from "../../src/api/productosApi";
import { useSesion } from "../../src/funcionalidades/sesion/contextoSesion";
import type { ProductoResponse } from "../../src/tipos/producto";
import { formatearPrecio } from "../../src/utilidades/formatearPrecio";

export default function Catalogo() {
  const { usuario, cerrarSesion } = useSesion();
  const catalogo = useQuery({
    queryKey: ["productos", "catalogo"],
    queryFn: productosApi.listar,
  });

  if (catalogo.isPending) {
    return (
      <View accessibilityLiveRegion="polite" style={estilos.estado}>
        <ActivityIndicator size="large" />
        <Text>Cargando sándwiches…</Text>
      </View>
    );
  }

  if (catalogo.isError) {
    return (
      <View accessibilityLiveRegion="polite" style={estilos.estado}>
        <Text style={estilos.error}>No se pudo cargar el catálogo.</Text>
        <Pressable
          accessibilityRole="button"
          onPress={() => catalogo.refetch()}
          style={estilos.botonPrincipal}
        >
          <Text style={estilos.textoBotonPrincipal}>Reintentar</Text>
        </Pressable>
      </View>
    );
  }

  function abrirDetalle(producto: ProductoResponse) {
    router.push({
      pathname: "/(cliente)/detalle-producto",
      params: { productoId: producto.id.toString() },
    });
  }

  return (
    <FlatList
      contentContainerStyle={estilos.lista}
      data={catalogo.data}
      keyExtractor={(producto) => producto.id.toString()}
      ListHeaderComponent={
        <View style={estilos.encabezado}>
          <View style={estilos.filaEncabezado}>
            <View style={estilos.presentacion}>
              <Text style={estilos.saludo}>Hola, {usuario?.nombre}</Text>
              <Text style={estilos.titulo}>Catálogo</Text>
            </View>
            <Pressable
              accessibilityLabel="Cerrar sesión"
              accessibilityRole="button"
              onPress={() => void cerrarSesion()}
              style={estilos.botonSecundario}
            >
              <Text style={estilos.textoBotonSecundario}>Salir</Text>
            </Pressable>
          </View>
          <Text style={estilos.descripcion}>Elige tu sándwich favorito.</Text>
        </View>
      }
      ListEmptyComponent={
        <View accessibilityLiveRegion="polite" style={estilos.vacio}>
          <Text style={estilos.tituloVacio}>Aún no hay sándwiches disponibles</Text>
          <Text style={estilos.descripcion}>Vuelve a consultar más tarde.</Text>
          <Pressable
            accessibilityRole="button"
            onPress={() => catalogo.refetch()}
            style={estilos.botonPrincipal}
          >
            <Text style={estilos.textoBotonPrincipal}>Actualizar</Text>
          </Pressable>
        </View>
      }
      renderItem={({ item }) => (
        <Pressable
          accessibilityHint="Abre el detalle del producto"
          accessibilityLabel={`${item.nombre}, ${formatearPrecio(item.precio)}, ${
            item.disponible ? "disponible" : "agotado"
          }`}
          accessibilityRole="button"
          onPress={() => abrirDetalle(item)}
          style={({ pressed }) => [estilos.tarjeta, pressed ? estilos.tarjetaPresionada : null]}
        >
          <View style={estilos.filaTarjeta}>
            <Text style={estilos.nombreProducto}>{item.nombre}</Text>
            <View style={[estilos.badge, item.disponible ? estilos.disponible : estilos.agotado]}>
              <Text
                style={[
                  estilos.textoBadge,
                  item.disponible ? estilos.textoDisponible : estilos.textoAgotado,
                ]}
              >
                {item.disponible ? "Disponible" : "Agotado"}
              </Text>
            </View>
          </View>
          <Text numberOfLines={2} style={estilos.descripcionProducto}>
            {item.descripcion}
          </Text>
          <Text numberOfLines={2} style={estilos.ingredientes}>
            {item.ingredientes.join(" · ")}
          </Text>
          <View style={estilos.filaTarjeta}>
            <Text style={estilos.precio}>{formatearPrecio(item.precio)}</Text>
            <Text style={estilos.stock}>Disponibles: {item.stock}</Text>
          </View>
        </Pressable>
      )}
    />
  );
}

const estilos = StyleSheet.create({
  lista: {
    flexGrow: 1,
    gap: 14,
    padding: 20,
  },
  encabezado: {
    gap: 6,
    marginBottom: 8,
  },
  filaEncabezado: {
    alignItems: "center",
    flexDirection: "row",
    gap: 12,
    justifyContent: "space-between",
  },
  presentacion: {
    flex: 1,
  },
  saludo: {
    color: "#4b5563",
  },
  titulo: {
    fontSize: 26,
    fontWeight: "700",
  },
  descripcion: {
    color: "#4b5563",
    lineHeight: 21,
  },
  tarjeta: {
    backgroundColor: "#fff",
    borderColor: "#d1d5db",
    borderRadius: 12,
    borderWidth: 1,
    gap: 10,
    minHeight: 160,
    padding: 16,
  },
  tarjetaPresionada: {
    opacity: 0.75,
  },
  filaTarjeta: {
    alignItems: "center",
    flexDirection: "row",
    gap: 10,
    justifyContent: "space-between",
  },
  nombreProducto: {
    flex: 1,
    fontSize: 18,
    fontWeight: "700",
  },
  descripcionProducto: {
    color: "#374151",
    lineHeight: 20,
  },
  ingredientes: {
    color: "#6b7280",
    fontSize: 13,
  },
  precio: {
    color: "#075985",
    fontSize: 18,
    fontWeight: "700",
  },
  stock: {
    color: "#4b5563",
  },
  badge: {
    borderRadius: 999,
    paddingHorizontal: 10,
    paddingVertical: 5,
  },
  disponible: {
    backgroundColor: "#dcfce7",
  },
  agotado: {
    backgroundColor: "#fee2e2",
  },
  textoBadge: {
    fontSize: 12,
    fontWeight: "700",
  },
  textoDisponible: {
    color: "#166534",
  },
  textoAgotado: {
    color: "#991b1b",
  },
  estado: {
    alignItems: "center",
    flex: 1,
    gap: 16,
    justifyContent: "center",
    padding: 24,
  },
  vacio: {
    alignItems: "center",
    gap: 12,
    paddingVertical: 60,
  },
  tituloVacio: {
    fontSize: 18,
    fontWeight: "700",
    textAlign: "center",
  },
  error: {
    color: "#b91c1c",
    textAlign: "center",
  },
  botonPrincipal: {
    alignItems: "center",
    backgroundColor: "#0a7ea4",
    borderRadius: 8,
    justifyContent: "center",
    minHeight: 48,
    paddingHorizontal: 18,
  },
  textoBotonPrincipal: {
    color: "#fff",
    fontWeight: "700",
  },
  botonSecundario: {
    alignItems: "center",
    borderColor: "#9ca3af",
    borderRadius: 8,
    borderWidth: 1,
    justifyContent: "center",
    minHeight: 44,
    paddingHorizontal: 14,
  },
  textoBotonSecundario: {
    color: "#374151",
    fontWeight: "600",
  },
});
