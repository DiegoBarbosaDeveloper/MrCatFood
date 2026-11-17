import { useQuery } from "@tanstack/react-query";
import { router, useLocalSearchParams } from "expo-router";
import { useState } from "react";
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
import { useCarrito } from "../../src/funcionalidades/carrito/tiendaCarrito";
import { formatearPrecio } from "../../src/utilidades/formatearPrecio";

export default function DetalleProducto() {
  const [cantidad, setCantidad] = useState(1);
  const [agregado, setAgregado] = useState(false);
  const agregarProducto = useCarrito((estado) => estado.agregarProducto);
  const parametros = useLocalSearchParams<{ productoId?: string }>();
  const productoId = Number(parametros.productoId);
  const cantidadActualEnCarrito = useCarrito(
    (estado) => estado.items.find((item) => item.productoId === productoId)?.cantidad ?? 0,
  );
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

      {producto.disponible ? (
        <View style={estilos.seccionCarrito}>
          <View style={estilos.filaCantidad}>
            <Text style={estilos.subtitulo}>Cantidad</Text>
            <View style={estilos.controlesCantidad}>
              <Pressable
                accessibilityLabel="Disminuir cantidad"
                accessibilityRole="button"
                disabled={cantidad <= 1}
                onPress={() => setCantidad((valor) => Math.max(1, valor - 1))}
                style={[estilos.botonCantidad, cantidad <= 1 ? estilos.deshabilitado : null]}
              >
                <Text style={estilos.textoCantidad}>−</Text>
              </Pressable>
              <Text accessibilityLiveRegion="polite" style={estilos.numeroCantidad}>
                {cantidad}
              </Text>
              <Pressable
                accessibilityLabel="Aumentar cantidad"
                accessibilityRole="button"
                disabled={cantidad + cantidadActualEnCarrito >= producto.stock}
                onPress={() =>
                  setCantidad((valor) =>
                    Math.min(producto.stock - cantidadActualEnCarrito, valor + 1),
                  )
                }
                style={[
                  estilos.botonCantidad,
                  cantidad + cantidadActualEnCarrito >= producto.stock
                    ? estilos.deshabilitado
                    : null,
                ]}
              >
                <Text style={estilos.textoCantidad}>+</Text>
              </Pressable>
            </View>
          </View>
          <Pressable
            accessibilityRole="button"
            disabled={cantidadActualEnCarrito >= producto.stock}
            onPress={() => {
              agregarProducto(producto, cantidad);
              setAgregado(true);
              setCantidad(1);
            }}
            style={[
              estilos.boton,
              cantidadActualEnCarrito >= producto.stock ? estilos.deshabilitado : null,
            ]}
          >
            <Text style={estilos.textoBoton}>Agregar al carrito</Text>
          </Pressable>
          {cantidadActualEnCarrito > 0 ? (
            <Text style={estilos.enCarrito}>En el carrito: {cantidadActualEnCarrito}</Text>
          ) : null}
          {agregado ? (
            <Pressable accessibilityRole="button" onPress={() => router.push("/(cliente)/carrito")}>
              <Text accessibilityLiveRegion="polite" style={estilos.enlaceCarrito}>
                Producto agregado. Ver carrito
              </Text>
            </Pressable>
          ) : null}
        </View>
      ) : null}

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
  seccionCarrito: { gap: 12 },
  filaCantidad: {
    alignItems: "center",
    flexDirection: "row",
    justifyContent: "space-between",
  },
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
  deshabilitado: { opacity: 0.4 },
  enCarrito: { color: "#4b5563", textAlign: "center" },
  enlaceCarrito: { color: "#075985", fontWeight: "700", minHeight: 44, textAlign: "center" },
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
