import { zodResolver } from "@hookform/resolvers/zod";
import { Link, useRouter } from "expo-router";
import { useState } from "react";
import { Controller, useForm } from "react-hook-form";
import { ActivityIndicator, Pressable, Text, TextInput, View } from "react-native";
import { z } from "zod";
import { useSesion } from "../../src/funcionalidades/sesion/contextoSesion";

const esquema = z.object({
  nombre: z.string().min(2, "Nombre muy corto"),
  email: z.string().email("Correo inválido"),
  telefono: z.string().regex(/^[0-9]{7,15}$/, "Entre 7 y 15 dígitos"),
  password: z.string().min(8, "Mínimo 8 caracteres"),
});

type Formulario = z.infer<typeof esquema>;

export default function Registro() {
  const { registrarse } = useSesion();
  const router = useRouter();
  const [error, setError] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

  const { control, handleSubmit } = useForm<Formulario>({
    resolver: zodResolver(esquema),
    defaultValues: { nombre: "", email: "", telefono: "", password: "" },
  });

  async function enviar(datos: Formulario) {
    setError(null);
    setEnviando(true);
    try {
      const usuario = await registrarse(datos);
      if (usuario.rol === "ADMIN") {
        router.replace("/(admin)");
      } else {
        router.replace("/(cliente)");
      }
    } catch (e) {
      setError(e instanceof Error ? e.message : "No se pudo registrar");
    } finally {
      setEnviando(false);
    }
  }

  const campo = (nombre: keyof Formulario, props: any) => (
    <Controller
      control={control}
      name={nombre}
      render={({ field, fieldState }) => (
        <View style={{ marginBottom: 8 }}>
          <TextInput
            {...props}
            value={field.value as string}
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
  );

  return (
    <View style={{ flex: 1, padding: 24, justifyContent: "center", gap: 4 }}>
      <Text style={{ fontSize: 24, fontWeight: "700", marginBottom: 8 }}>Crear cuenta</Text>

      {campo("nombre", { placeholder: "Nombre completo" })}
      {campo("email", { placeholder: "Correo", autoCapitalize: "none", keyboardType: "email-address" })}
      {campo("telefono", { placeholder: "Teléfono", keyboardType: "phone-pad" })}
      {campo("password", { placeholder: "Contraseña", secureTextEntry: true })}

      {error ? <Text style={{ color: "red" }}>{error}</Text> : null}

      <Pressable
        onPress={handleSubmit(enviar) as any}
        disabled={enviando}
        style={{ backgroundColor: "#0a7ea4", padding: 14, borderRadius: 8, alignItems: "center", marginTop: 8 }}
      >
        {enviando ? <ActivityIndicator color="#fff" /> : <Text style={{ color: "#fff", fontWeight: "600" }}>Registrarme</Text>}
      </Pressable>

      <Link href="/(auth)/acceso" style={{ textAlign: "center", marginTop: 8 }}>
        Ya tengo cuenta
      </Link>
    </View>
  );
}
