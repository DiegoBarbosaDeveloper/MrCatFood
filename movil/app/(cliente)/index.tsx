import { Pressable, Text, View } from "react-native";
import { useSesion } from "../../src/funcionalidades/sesion/contextoSesion";

export default function ClienteInicio() {
  const { usuario, cerrarSesion } = useSesion();
  return (
    <View style={{ flex: 1, padding: 24, justifyContent: "center", gap: 12 }}>
      <Text style={{ fontSize: 20, fontWeight: "700" }}>Hola, {usuario?.nombre}</Text>
      <Text>Catálogo disponible próximamente.</Text>
      <Pressable
        onPress={cerrarSesion}
        style={{ backgroundColor: "#c0392b", padding: 12, borderRadius: 8, alignItems: "center" }}
      >
        <Text style={{ color: "#fff" }}>Cerrar sesión</Text>
      </Pressable>
    </View>
  );
}
