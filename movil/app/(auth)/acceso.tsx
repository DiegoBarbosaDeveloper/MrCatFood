import { zodResolver } from "@hookform/resolvers/zod";
import { Link, useRouter } from "expo-router";
import { useState } from "react";
import { Controller, useForm } from "react-hook-form";
import { ActivityIndicator, Pressable, Text, TextInput, View } from "react-native";
import { z } from "zod";
import { useSesion } from "../../src/funcionalidades/sesion/contextoSesion";

const esquema = z.object({
  email: z.string().email("Correo inválido"),
  password: z.string().min(8, "Mínimo 8 caracteres"),
});

type Formulario = z.infer<typeof esquema>;

export default function Acceso() {
  const { iniciarSesion } = useSesion();
  const router = useRouter();
  const [error, setError] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

  const { control, handleSubmit } = useForm<Formulario>({
    resolver: zodResolver(esquema),
    defaultValues: { email: "", password: "" },
  });

  async function enviar(datos: Formulario) {
    setError(null);
    setEnviando(true);
    try {
      const usuario = await iniciarSesion(datos.email, datos.password);
      if (usuario.rol === "ADMIN") {
        router.replace("/(admin)");
      } else {
        router.replace("/(cliente)");
      }
    } catch (e) {
      setError(e instanceof Error ? e.message : "No se pudo iniciar sesión");
    } finally {
      setEnviando(false);
    }
  }

  return (
    <View style={{ flex: 1, padding: 24, justifyContent: "center", gap: 12 }}>
      <Text style={{ fontSize: 24, fontWeight: "700", marginBottom: 8 }}>Iniciar sesión</Text>

      <Controller
        control={control}
        name="email"
        render={({ field, fieldState }) => (
          <View>
            <TextInput
              placeholder="Correo"
              autoCapitalize="none"
              keyboardType="email-address"
              value={field.value}
              onChangeText={field.onChange}
              onBlur={field.onBlur}
              style={{ borderWidth: 1, borderRadius: 8, padding: 12 }}
            />
            {fieldState.error ? (
              <Text style={{ color: "red", marginTop: 4 }}>{fieldState.error.message}</Text>
            ) : null}
          </View>
        )}
      />

      <Controller
        control={control}
        name="password"
        render={({ field, fieldState }) => (
          <View>
            <TextInput
              placeholder="Contraseña"
              secureTextEntry
              value={field.value}
              onChangeText={field.onChange}
              onBlur={field.onBlur}
              style={{ borderWidth: 1, borderRadius: 8, padding: 12 }}
            />
            {fieldState.error ? (
              <Text style={{ color: "red", marginTop: 4 }}>{fieldState.error.message}</Text>
            ) : null}
          </View>
        )}
      />

      {error ? <Text style={{ color: "red" }}>{error}</Text> : null}

      <Pressable
        onPress={handleSubmit(enviar) as any}
        disabled={enviando}
        style={{ backgroundColor: "#0a7ea4", padding: 14, borderRadius: 8, alignItems: "center" }}
      >
        {enviando ? <ActivityIndicator color="#fff" /> : <Text style={{ color: "#fff", fontWeight: "600" }}>Entrar</Text>}
      </Pressable>

      <Link href="/(auth)/registro" style={{ textAlign: "center", marginTop: 8 }}>
        Crear cuenta
      </Link>
    </View>
  );
}
