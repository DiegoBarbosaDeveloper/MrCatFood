import { Redirect } from "expo-router";
import { useSesion } from "../src/funcionalidades/sesion/contextoSesion";

export default function Raiz() {
  const { usuario, cargando } = useSesion();
  if (cargando) return null;
  if (!usuario) return <Redirect href="/(auth)/acceso" />;
  if (usuario.rol === "ADMIN") return <Redirect href="/(admin)" />;
  return <Redirect href="/(cliente)" />;
}
