import { Tabs } from "expo-router";

export default function DisposicionAdmin() {
  return (
    <Tabs screenOptions={{ headerShown: true }}>
      <Tabs.Screen name="index" options={{ title: "Panel admin" }} />
    </Tabs>
  );
}
