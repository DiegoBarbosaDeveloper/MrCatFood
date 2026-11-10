import { Pressable, Text, View } from "react-native";
import { useSesion } from "../../src/funcionalidades/sesion/contextoSesion";

export default function AdminInicio() {
  const { usuario, cerrarSesion } = useSesion();
  return (
    <View style={{ flex: 1, padding: 24, justifyContent: "center", gap: 12 }}>
      <Text style={{ fontSize: 20, fontWeight: "700" }}>Panel de administración</Text>
      <Text>Sesión: {usuario?.email}</Text>
      <Text>Gestión de productos y pedidos próximamente.</Text>
      <Pressable
        onPress={cerrarSesion}
        style={{ backgroundColor: "#c0392b", padding: 12, borderRadius: 8, alignItems: "center" }}
      >
        <Text style={{ color: "#fff" }}>Cerrar sesión</Text>
      </Pressable>
    </View>
  );
}
