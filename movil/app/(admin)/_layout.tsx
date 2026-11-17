import { Tabs } from "expo-router";

export default function DisposicionAdmin() {
  return (
    <Tabs screenOptions={{ headerShown: true }}>
      <Tabs.Screen name="index" options={{ title: "Productos" }} />
      <Tabs.Screen name="editar-producto" options={{ title: "Editar producto", href: null }} />
    </Tabs>
  );
}
