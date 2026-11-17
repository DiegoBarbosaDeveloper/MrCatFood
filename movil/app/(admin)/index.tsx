import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
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
import { formatearPrecio } from "../../src/utilidades/formatearPrecio";

export default function AdminProductos() {
  const clienteConsultas = useQueryClient();
  const { usuario, cerrarSesion } = useSesion();
  const productos = useQuery({
    queryKey: ["productos", "administracion"],
    queryFn: productosApi.listarAdministracion,
  });
  const disponibilidad = useMutation({
    mutationFn: ({
      productoId,
      activo,
      stock,
    }: {
      productoId: number;
      activo: boolean;
      stock: number;
    }) =>
      productosApi.actualizarDisponibilidad(productoId, { activo: !activo, stock }),
    onSuccess: async () => {
      await clienteConsultas.invalidateQueries({ queryKey: ["productos"] });
    },
  });

  if (productos.isPending) {
    return (
      <View accessibilityLiveRegion="polite" style={estilos.estado}>
        <ActivityIndicator size="large" />
        <Text>Cargando productos…</Text>
      </View>
    );
  }

  if (productos.isError) {
    return (
      <View accessibilityLiveRegion="polite" style={estilos.estado}>
        <Text style={estilos.error}>No se pudieron cargar los productos.</Text>
        <Pressable
          accessibilityRole="button"
          onPress={() => productos.refetch()}
          style={estilos.botonPrincipal}
        >
          <Text style={estilos.textoBotonPrincipal}>Reintentar</Text>
        </Pressable>
      </View>
    );
  }

  function editarProducto(productoId: number) {
    router.push({
      pathname: "/(admin)/editar-producto",
      params: { productoId: productoId.toString() },
    });
  }

  return (
    <FlatList
      contentContainerStyle={estilos.lista}
      data={productos.data}
      keyExtractor={(producto) => producto.id.toString()}
      ListHeaderComponent={
        <View style={estilos.encabezado}>
          <View style={estilos.filaEncabezado}>
            <View style={estilos.presentacion}>
              <Text style={estilos.sesion}>Sesión: {usuario?.email}</Text>
              <Text style={estilos.titulo}>Productos</Text>
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
          <Pressable
            accessibilityRole="button"
            onPress={() => router.push("/(admin)/editar-producto")}
            style={estilos.botonPrincipal}
          >
            <Text style={estilos.textoBotonPrincipal}>Crear producto</Text>
          </Pressable>
          {disponibilidad.isError ? (
            <Text accessibilityLiveRegion="polite" style={estilos.error}>
              No se pudo cambiar la disponibilidad. Intenta nuevamente.
            </Text>
          ) : null}
        </View>
      }
      ListEmptyComponent={
        <View style={estilos.estadoVacio}>
          <Text style={estilos.nombre}>No hay productos registrados.</Text>
        </View>
      }
      renderItem={({ item }) => {
        const cambiandoEsteProducto =
          disponibilidad.isPending && disponibilidad.variables?.productoId === item.id;
        return (
          <View style={estilos.tarjeta}>
            <View style={estilos.filaTarjeta}>
              <Text style={estilos.nombre}>{item.nombre}</Text>
              <View style={[estilos.badge, item.activo ? estilos.activo : estilos.inactivo]}>
                <Text style={item.activo ? estilos.textoActivo : estilos.textoInactivo}>
                  {item.activo ? "Activo" : "Inactivo"}
                </Text>
              </View>
            </View>
            <Text style={estilos.precio}>{formatearPrecio(item.precio)}</Text>
            <Text style={estilos.detalle}>Stock: {item.stock}</Text>
            <Text numberOfLines={2} style={estilos.detalle}>
              {item.ingredientes.join(" · ")}
            </Text>
            <View style={estilos.acciones}>
              <Pressable
                accessibilityRole="button"
                onPress={() => editarProducto(item.id)}
                style={estilos.botonEditar}
              >
                <Text style={estilos.textoEditar}>Editar</Text>
              </Pressable>
              <Pressable
                accessibilityRole="button"
                disabled={disponibilidad.isPending}
                onPress={() =>
                  disponibilidad.mutate({
                    productoId: item.id,
                    activo: item.activo,
                    stock: item.stock,
                  })
                }
                style={[estilos.botonEstado, cambiandoEsteProducto ? estilos.deshabilitado : null]}
              >
                {cambiandoEsteProducto ? (
                  <ActivityIndicator color="#374151" />
                ) : (
                  <Text style={estilos.textoEstado}>{item.activo ? "Desactivar" : "Activar"}</Text>
                )}
              </Pressable>
            </View>
          </View>
        );
      }}
    />
  );
}

const estilos = StyleSheet.create({
  lista: { flexGrow: 1, gap: 14, padding: 20 },
  encabezado: { gap: 14, marginBottom: 8 },
  filaEncabezado: {
    alignItems: "center",
    flexDirection: "row",
    gap: 12,
    justifyContent: "space-between",
  },
  presentacion: { flex: 1 },
  sesion: { color: "#4b5563", fontSize: 13 },
  titulo: { fontSize: 26, fontWeight: "700" },
  tarjeta: {
    borderColor: "#d1d5db",
    borderRadius: 12,
    borderWidth: 1,
    gap: 9,
    padding: 16,
  },
  filaTarjeta: {
    alignItems: "center",
    flexDirection: "row",
    gap: 10,
    justifyContent: "space-between",
  },
  nombre: { flex: 1, fontSize: 17, fontWeight: "700" },
  precio: { color: "#075985", fontSize: 17, fontWeight: "700" },
  detalle: { color: "#4b5563" },
  badge: { borderRadius: 999, paddingHorizontal: 10, paddingVertical: 5 },
  activo: { backgroundColor: "#dcfce7" },
  inactivo: { backgroundColor: "#e5e7eb" },
  textoActivo: { color: "#166534", fontWeight: "700" },
  textoInactivo: { color: "#374151", fontWeight: "700" },
  acciones: { flexDirection: "row", gap: 10, marginTop: 6 },
  botonEditar: {
    alignItems: "center",
    backgroundColor: "#0a7ea4",
    borderRadius: 8,
    flex: 1,
    justifyContent: "center",
    minHeight: 44,
  },
  textoEditar: { color: "#fff", fontWeight: "700" },
  botonEstado: {
    alignItems: "center",
    borderColor: "#9ca3af",
    borderRadius: 8,
    borderWidth: 1,
    flex: 1,
    justifyContent: "center",
    minHeight: 44,
  },
  textoEstado: { color: "#374151", fontWeight: "700" },
  botonPrincipal: {
    alignItems: "center",
    backgroundColor: "#0a7ea4",
    borderRadius: 8,
    justifyContent: "center",
    minHeight: 48,
    paddingHorizontal: 18,
  },
  textoBotonPrincipal: { color: "#fff", fontWeight: "700" },
  botonSecundario: {
    alignItems: "center",
    borderColor: "#9ca3af",
    borderRadius: 8,
    borderWidth: 1,
    justifyContent: "center",
    minHeight: 44,
    paddingHorizontal: 14,
  },
  textoBotonSecundario: { color: "#374151", fontWeight: "600" },
  deshabilitado: { opacity: 0.6 },
  estado: {
    alignItems: "center",
    flex: 1,
    gap: 16,
    justifyContent: "center",
    padding: 24,
  },
  estadoVacio: { alignItems: "center", paddingVertical: 60 },
  error: { color: "#b91c1c", textAlign: "center" },
});
