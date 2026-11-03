import { Stack } from "expo-router";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { ProveedorSesion } from "../src/funcionalidades/sesion/contextoSesion";

const cliente = new QueryClient();

export default function DisposicionRaiz() {
  return (
    <QueryClientProvider client={cliente}>
      <ProveedorSesion>
        <Stack screenOptions={{ headerShown: false }} />
      </ProveedorSesion>
    </QueryClientProvider>
  );
}
