import * as SecureStore from "expo-secure-store";
import { Platform } from "react-native";

const CLAVE = "mrcatfood_token";

export const almacenToken = {
  async guardar(token: string): Promise<void> {
    if (Platform.OS === "web") {
      window.localStorage.setItem(CLAVE, token);
      return;
    }
    await SecureStore.setItemAsync(CLAVE, token);
  },
  async leer(): Promise<string | null> {
    if (Platform.OS === "web") {
      return window.localStorage.getItem(CLAVE);
    }
    return SecureStore.getItemAsync(CLAVE);
  },
  async borrar(): Promise<void> {
    if (Platform.OS === "web") {
      window.localStorage.removeItem(CLAVE);
      return;
    }
    await SecureStore.deleteItemAsync(CLAVE);
  },
};
