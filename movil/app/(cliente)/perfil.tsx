import { Text, View } from "react-native";
import { useSesion } from "../../src/funcionalidades/sesion/contextoSesion";

export default function Perfil() {
  const { usuario } = useSesion();
  return (
    <View style={{ flex: 1, padding: 24, gap: 8 }}>
      <Text style={{ fontSize: 20, fontWeight: "700" }}>Mi perfil</Text>
      <Text>Nombre: {usuario?.nombre}</Text>
      <Text>Correo: {usuario?.email}</Text>
      <Text>Rol: {usuario?.rol}</Text>
    </View>
  );
}
