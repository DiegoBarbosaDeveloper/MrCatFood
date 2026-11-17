import { Tabs } from "expo-router";

export default function DisposicionAdmin() {
  return (
    <Tabs screenOptions={{ headerShown: true }}>
      <Tabs.Screen name="index" options={{ title: "Productos" }} />
      <Tabs.Screen name="pedidos" options={{ title: "Pedidos" }} />
      <Tabs.Screen name="editar-producto" options={{ title: "Editar producto", href: null }} />
      <Tabs.Screen name="detalle-pedido" options={{ title: "Detalle del pedido", href: null }} />
    </Tabs>
  );
}
