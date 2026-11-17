import { Tabs } from "expo-router";
import {
  calcularCantidadTotal,
  useCarrito,
} from "../../src/funcionalidades/carrito/tiendaCarrito";

export default function DisposicionCliente() {
  const cantidadCarrito = useCarrito((estado) => calcularCantidadTotal(estado.items));
  return (
    <Tabs screenOptions={{ headerShown: true }}>
      <Tabs.Screen name="index" options={{ title: "Catálogo" }} />
      <Tabs.Screen
        name="carrito"
        options={{ title: cantidadCarrito > 0 ? `Carrito (${cantidadCarrito})` : "Carrito" }}
      />
      <Tabs.Screen name="perfil" options={{ title: "Perfil" }} />
      <Tabs.Screen name="mi-ubicacion" options={{ title: "Mi ubicación", href: null }} />
      <Tabs.Screen name="detalle-producto" options={{ title: "Detalle", href: null }} />
    </Tabs>
  );
}
