import { Link } from "expo-router";
import { Pressable, StyleSheet, Text, View } from "react-native";
import { useSesion } from "../../src/funcionalidades/sesion/contextoSesion";

export default function Perfil() {
  const { usuario } = useSesion();
  return (
    <View style={estilos.contenedor}>
      <Text style={estilos.titulo}>Mi perfil</Text>
      <Text>Nombre: {usuario?.nombre}</Text>
      <Text>Correo: {usuario?.email}</Text>
      <Text>Rol: {usuario?.rol}</Text>
      <Text>Conjunto: {usuario?.conjuntoResidencial ?? "Sin registrar"}</Text>
      <Text>Apartamento: {usuario?.apartamento ?? "Sin registrar"}</Text>
      <Link href="/(cliente)/mi-ubicacion" asChild>
        <Pressable accessibilityRole="button" style={estilos.boton}>
          <Text style={estilos.textoBoton}>Editar mi ubicación</Text>
        </Pressable>
      </Link>
    </View>
  );
}

const estilos = StyleSheet.create({
  contenedor: {
    flex: 1,
    gap: 8,
    padding: 24,
  },
  titulo: {
    fontSize: 20,
    fontWeight: "700",
  },
  boton: {
    alignItems: "center",
    backgroundColor: "#0a7ea4",
    borderRadius: 8,
    justifyContent: "center",
    marginTop: 12,
    minHeight: 48,
    paddingHorizontal: 16,
  },
  textoBoton: {
    color: "#fff",
    fontWeight: "700",
  },
});
