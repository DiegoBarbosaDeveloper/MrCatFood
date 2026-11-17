import { router } from "expo-router";
import { FlatList, Pressable, StyleSheet, Text, View } from "react-native";
import {
  calcularSubtotal,
  useCarrito,
  type ItemCarrito,
} from "../../src/funcionalidades/carrito/tiendaCarrito";
import { formatearPrecio } from "../../src/utilidades/formatearPrecio";

export default function Carrito() {
  const items = useCarrito((estado) => estado.items);
  const actualizarCantidad = useCarrito((estado) => estado.actualizarCantidad);
  const quitarProducto = useCarrito((estado) => estado.quitarProducto);
  const subtotal = calcularSubtotal(items);

  if (items.length === 0) {
    return (
      <View style={estilos.vacio}>
        <Text style={estilos.tituloVacio}>Tu carrito está vacío</Text>
        <Text style={estilos.descripcion}>Agrega un sándwich desde el catálogo.</Text>
        <Pressable
          accessibilityRole="button"
          onPress={() => router.push("/(cliente)")}
          style={estilos.botonPrincipal}
        >
          <Text style={estilos.textoBotonPrincipal}>Ver catálogo</Text>
        </Pressable>
      </View>
    );
  }

  function renderizarItem(item: ItemCarrito) {
    return (
      <View style={estilos.tarjeta}>
        <View style={estilos.filaSuperior}>
          <View style={estilos.datosProducto}>
            <Text style={estilos.nombre}>{item.nombre}</Text>
            <Text style={estilos.precio}>{formatearPrecio(item.precio)} c/u</Text>
          </View>
          <Pressable
            accessibilityLabel={`Quitar ${item.nombre} del carrito`}
            accessibilityRole="button"
            onPress={() => quitarProducto(item.productoId)}
            style={estilos.botonQuitar}
          >
            <Text style={estilos.textoQuitar}>Quitar</Text>
          </Pressable>
        </View>

        <View style={estilos.filaCantidad}>
          <Text style={estilos.etiqueta}>Cantidad</Text>
          <View style={estilos.controlesCantidad}>
            <Pressable
              accessibilityLabel={`Disminuir cantidad de ${item.nombre}`}
              accessibilityRole="button"
              onPress={() => actualizarCantidad(item.productoId, item.cantidad - 1)}
              style={estilos.botonCantidad}
            >
              <Text style={estilos.textoCantidad}>−</Text>
            </Pressable>
            <Text accessibilityLiveRegion="polite" style={estilos.numeroCantidad}>
              {item.cantidad}
            </Text>
            <Pressable
              accessibilityLabel={`Aumentar cantidad de ${item.nombre}`}
              accessibilityRole="button"
              disabled={item.cantidad >= item.stock}
              onPress={() => actualizarCantidad(item.productoId, item.cantidad + 1)}
              style={[
                estilos.botonCantidad,
                item.cantidad >= item.stock ? estilos.deshabilitado : null,
              ]}
            >
              <Text style={estilos.textoCantidad}>+</Text>
            </Pressable>
          </View>
        </View>
        <View style={estilos.filaSubtotalItem}>
          <Text style={estilos.disponibles}>Máximo disponible: {item.stock}</Text>
          <Text style={estilos.subtotalItem}>
            {formatearPrecio(item.precio * item.cantidad)}
          </Text>
        </View>
      </View>
    );
  }

  return (
    <FlatList
      contentContainerStyle={estilos.lista}
      data={items}
      keyExtractor={(item) => item.productoId.toString()}
      ListHeaderComponent={<Text style={estilos.titulo}>Tu carrito</Text>}
      ListFooterComponent={
        <View style={estilos.resumen}>
          <Text style={estilos.etiquetaTotal}>Subtotal</Text>
          <Text accessibilityLiveRegion="polite" style={estilos.total}>
            {formatearPrecio(subtotal)}
          </Text>
        </View>
      }
      renderItem={({ item }) => renderizarItem(item)}
    />
  );
}

const estilos = StyleSheet.create({
  lista: { flexGrow: 1, gap: 14, padding: 20 },
  titulo: { fontSize: 26, fontWeight: "700", marginBottom: 4 },
  tarjeta: {
    borderColor: "#d1d5db",
    borderRadius: 12,
    borderWidth: 1,
    gap: 14,
    padding: 16,
  },
  filaSuperior: { alignItems: "flex-start", flexDirection: "row", gap: 12 },
  datosProducto: { flex: 1, gap: 5 },
  nombre: { fontSize: 17, fontWeight: "700" },
  precio: { color: "#4b5563" },
  botonQuitar: { justifyContent: "center", minHeight: 44, paddingHorizontal: 8 },
  textoQuitar: { color: "#b91c1c", fontWeight: "700" },
  filaCantidad: {
    alignItems: "center",
    flexDirection: "row",
    justifyContent: "space-between",
  },
  etiqueta: { fontWeight: "600" },
  controlesCantidad: { alignItems: "center", flexDirection: "row", gap: 10 },
  botonCantidad: {
    alignItems: "center",
    borderColor: "#9ca3af",
    borderRadius: 8,
    borderWidth: 1,
    height: 44,
    justifyContent: "center",
    width: 44,
  },
  textoCantidad: { fontSize: 22, fontWeight: "700" },
  numeroCantidad: { fontSize: 18, fontWeight: "700", minWidth: 28, textAlign: "center" },
  deshabilitado: { opacity: 0.35 },
  filaSubtotalItem: {
    alignItems: "center",
    flexDirection: "row",
    justifyContent: "space-between",
  },
  disponibles: { color: "#6b7280", fontSize: 13 },
  subtotalItem: { color: "#075985", fontSize: 17, fontWeight: "700" },
  resumen: {
    alignItems: "center",
    backgroundColor: "#f3f4f6",
    borderRadius: 12,
    flexDirection: "row",
    justifyContent: "space-between",
    marginTop: 8,
    padding: 18,
  },
  etiquetaTotal: { fontSize: 18, fontWeight: "700" },
  total: { color: "#075985", fontSize: 23, fontWeight: "700" },
  vacio: {
    alignItems: "center",
    flex: 1,
    gap: 12,
    justifyContent: "center",
    padding: 24,
  },
  tituloVacio: { fontSize: 20, fontWeight: "700", textAlign: "center" },
  descripcion: { color: "#4b5563", textAlign: "center" },
  botonPrincipal: {
    alignItems: "center",
    backgroundColor: "#0a7ea4",
    borderRadius: 8,
    justifyContent: "center",
    minHeight: 48,
    paddingHorizontal: 18,
  },
  textoBotonPrincipal: { color: "#fff", fontWeight: "700" },
});
