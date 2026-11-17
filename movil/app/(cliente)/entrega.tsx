import { zodResolver } from "@hookform/resolvers/zod";
import { useQuery } from "@tanstack/react-query";
import { router } from "expo-router";
import { Controller, useForm } from "react-hook-form";
import {
  ActivityIndicator,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from "react-native";
import { pedidosApi } from "../../src/api/pedidosApi";
import { useCarrito } from "../../src/funcionalidades/carrito/tiendaCarrito";
import {
  esquemaEntrega,
  type FormularioEntrega,
} from "../../src/validacion/esquemasFormularios";

export default function Entrega() {
  const items = useCarrito((estado) => estado.items);
  const entrega = useCarrito((estado) => estado.entrega);
  const actualizarEntrega = useCarrito((estado) => estado.actualizarEntrega);
  const horario = useQuery({
    queryKey: ["configuracion", "horario"],
    queryFn: pedidosApi.consultarHorario,
  });
  const { control, handleSubmit, setError } = useForm<FormularioEntrega>({
    resolver: zodResolver(esquemaEntrega),
    defaultValues: entrega,
  });

  if (items.length === 0) {
    return (
      <View style={estilos.estado}>
        <Text style={estilos.error}>Agrega productos antes de configurar la entrega.</Text>
        <Pressable
          accessibilityRole="button"
          onPress={() => router.replace("/(cliente)/carrito")}
          style={estilos.botonPrincipal}
        >
          <Text style={estilos.textoBoton}>Volver al carrito</Text>
        </Pressable>
      </View>
    );
  }

  if (horario.isPending) {
    return (
      <View accessibilityLiveRegion="polite" style={estilos.estado}>
        <ActivityIndicator size="large" />
        <Text>Cargando horarios…</Text>
      </View>
    );
  }

  if (horario.isError) {
    return (
      <View accessibilityLiveRegion="polite" style={estilos.estado}>
        <Text style={estilos.error}>No se pudo consultar el horario de entrega.</Text>
        <Pressable
          accessibilityRole="button"
          onPress={() => horario.refetch()}
          style={estilos.botonPrincipal}
        >
          <Text style={estilos.textoBoton}>Reintentar</Text>
        </Pressable>
      </View>
    );
  }

  function continuar(valores: FormularioEntrega) {
    const configuracion = horario.data;
    if (!configuracion) return;
    const fechaHora = new Date(`${valores.fechaEntrega}T${valores.horaEntrega}:00-05:00`);
    if (fechaHora.getTime() <= Date.now()) {
      setError("fechaEntrega", { message: "La entrega debe programarse para una fecha futura" });
      return;
    }
    if (
      valores.horaEntrega < configuracion.ventanaEntregaInicio ||
      valores.horaEntrega > configuracion.ventanaEntregaFin
    ) {
      setError("horaEntrega", {
        message: `El horario válido es de ${configuracion.ventanaEntregaInicio} a ${configuracion.ventanaEntregaFin}`,
      });
      return;
    }
    actualizarEntrega({
      fechaEntrega: valores.fechaEntrega.trim(),
      horaEntrega: valores.horaEntrega.trim(),
      notas: valores.notas.trim(),
    });
    router.push("/(cliente)/confirmacion");
  }

  return (
    <ScrollView contentContainerStyle={estilos.contenedor} keyboardShouldPersistTaps="handled">
      <Text style={estilos.titulo}>Datos de entrega</Text>
      <Text style={estilos.descripcion}>
        Programa la entrega entre {horario.data.ventanaEntregaInicio} y{" "}
        {horario.data.ventanaEntregaFin} ({horario.data.zona}).
      </Text>

      <Controller
        control={control}
        name="fechaEntrega"
        render={({ field, fieldState }) => (
          <View style={estilos.grupoCampo}>
            <Text style={estilos.etiqueta}>Fecha</Text>
            <TextInput
              accessibilityLabel="Fecha de entrega"
              autoCapitalize="none"
              inputMode="numeric"
              onBlur={field.onBlur}
              onChangeText={field.onChange}
              placeholder="2026-11-20"
              style={[estilos.campo, fieldState.error ? estilos.campoError : null]}
              value={field.value}
            />
            <Text style={estilos.ayuda}>Formato: AAAA-MM-DD</Text>
            {fieldState.error ? <Text style={estilos.error}>{fieldState.error.message}</Text> : null}
          </View>
        )}
      />

      <Controller
        control={control}
        name="horaEntrega"
        render={({ field, fieldState }) => (
          <View style={estilos.grupoCampo}>
            <Text style={estilos.etiqueta}>Hora</Text>
            <TextInput
              accessibilityLabel="Hora de entrega"
              autoCapitalize="none"
              inputMode="numeric"
              onBlur={field.onBlur}
              onChangeText={field.onChange}
              placeholder="18:30"
              style={[estilos.campo, fieldState.error ? estilos.campoError : null]}
              value={field.value}
            />
            <Text style={estilos.ayuda}>Formato de 24 horas: HH:mm</Text>
            {fieldState.error ? <Text style={estilos.error}>{fieldState.error.message}</Text> : null}
          </View>
        )}
      />

      <Controller
        control={control}
        name="notas"
        render={({ field, fieldState }) => (
          <View style={estilos.grupoCampo}>
            <Text style={estilos.etiqueta}>Notas (opcional)</Text>
            <TextInput
              accessibilityLabel="Notas de entrega"
              maxLength={500}
              multiline
              numberOfLines={5}
              onBlur={field.onBlur}
              onChangeText={field.onChange}
              placeholder="Indicaciones para la entrega"
              style={[
                estilos.campo,
                estilos.campoMultilinea,
                fieldState.error ? estilos.campoError : null,
              ]}
              textAlignVertical="top"
              value={field.value}
            />
            {fieldState.error ? <Text style={estilos.error}>{fieldState.error.message}</Text> : null}
          </View>
        )}
      />

      <Pressable
        accessibilityRole="button"
        onPress={() => void handleSubmit(continuar)()}
        style={estilos.botonPrincipal}
      >
        <Text style={estilos.textoBoton}>Revisar pedido</Text>
      </Pressable>
    </ScrollView>
  );
}

const estilos = StyleSheet.create({
  contenedor: { flexGrow: 1, gap: 20, padding: 24 },
  titulo: { fontSize: 25, fontWeight: "700" },
  descripcion: { color: "#4b5563", lineHeight: 21 },
  grupoCampo: { gap: 6 },
  etiqueta: { fontSize: 16, fontWeight: "600" },
  ayuda: { color: "#6b7280", fontSize: 13 },
  campo: {
    borderColor: "#6b7280",
    borderRadius: 8,
    borderWidth: 1,
    minHeight: 48,
    paddingHorizontal: 12,
    paddingVertical: 10,
  },
  campoMultilinea: { minHeight: 120 },
  campoError: { borderColor: "#b91c1c" },
  error: { color: "#b91c1c" },
  botonPrincipal: {
    alignItems: "center",
    backgroundColor: "#0a7ea4",
    borderRadius: 8,
    justifyContent: "center",
    minHeight: 50,
    paddingHorizontal: 18,
  },
  textoBoton: { color: "#fff", fontWeight: "700" },
  estado: {
    alignItems: "center",
    flex: 1,
    gap: 16,
    justifyContent: "center",
    padding: 24,
  },
});
