import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useEffect, useState } from "react";
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
import { z } from "zod";
import { autenticacionApi } from "../../src/api/autenticacionApi";
import { ErrorHttp } from "../../src/api/clienteHttp";
import { usuariosApi } from "../../src/api/usuariosApi";
import { useSesion } from "../../src/funcionalidades/sesion/contextoSesion";

const esquemaUbicacion = z.object({
  conjuntoResidencial: z
    .string()
    .trim()
    .min(1, "El conjunto residencial es obligatorio")
    .max(120, "Debe tener máximo 120 caracteres"),
  apartamento: z
    .string()
    .trim()
    .min(1, "El apartamento es obligatorio")
    .max(60, "Debe tener máximo 60 caracteres"),
});

type FormularioUbicacion = z.infer<typeof esquemaUbicacion>;

export default function MiUbicacion() {
  const clienteConsultas = useQueryClient();
  const { actualizarUsuario } = useSesion();
  const [mensajeGeneral, setMensajeGeneral] = useState<string | null>(null);
  const [guardado, setGuardado] = useState(false);
  const perfil = useQuery({
    queryKey: ["usuario", "yo"],
    queryFn: autenticacionApi.yo,
  });
  const {
    control,
    handleSubmit,
    reset,
    setError: establecerErrorCampo,
  } = useForm<FormularioUbicacion>({
    resolver: zodResolver(esquemaUbicacion),
    defaultValues: { conjuntoResidencial: "", apartamento: "" },
  });

  useEffect(() => {
    if (perfil.data) {
      reset({
        conjuntoResidencial: perfil.data.conjuntoResidencial ?? "",
        apartamento: perfil.data.apartamento ?? "",
      });
    }
  }, [perfil.data, reset]);

  const actualizacion = useMutation({
    mutationFn: usuariosApi.actualizarUbicacion,
    onSuccess: (usuarioActualizado) => {
      actualizarUsuario(usuarioActualizado);
      clienteConsultas.setQueryData(["usuario", "yo"], usuarioActualizado);
      reset({
        conjuntoResidencial: usuarioActualizado.conjuntoResidencial ?? "",
        apartamento: usuarioActualizado.apartamento ?? "",
      });
      setGuardado(true);
    },
  });

  async function guardarUbicacion(valores: FormularioUbicacion) {
    setMensajeGeneral(null);
    setGuardado(false);
    try {
      await actualizacion.mutateAsync(valores);
    } catch (error) {
      if (error instanceof ErrorHttp && error.campos) {
        const errorConjunto = error.campos.conjuntoResidencial;
        const errorApartamento = error.campos.apartamento;
        if (errorConjunto) {
          establecerErrorCampo("conjuntoResidencial", { message: errorConjunto });
        }
        if (errorApartamento) {
          establecerErrorCampo("apartamento", { message: errorApartamento });
        }
      }
      setMensajeGeneral(
        error instanceof Error ? error.message : "No se pudo guardar la ubicación",
      );
    }
  }

  if (perfil.isPending) {
    return (
      <View style={estilos.estado} accessibilityLiveRegion="polite">
        <ActivityIndicator size="large" />
        <Text>Cargando tu ubicación…</Text>
      </View>
    );
  }

  if (perfil.isError) {
    return (
      <View style={estilos.estado} accessibilityLiveRegion="polite">
        <Text style={estilos.error}>No se pudo consultar tu ubicación.</Text>
        <Pressable
          accessibilityRole="button"
          onPress={() => perfil.refetch()}
          style={estilos.boton}
        >
          <Text style={estilos.textoBoton}>Reintentar</Text>
        </Pressable>
      </View>
    );
  }

  return (
    <ScrollView contentContainerStyle={estilos.contenedor} keyboardShouldPersistTaps="handled">
      <Text style={estilos.titulo}>Mi ubicación</Text>
      <Text style={estilos.descripcion}>
        Estos datos se usarán como dirección de entrega de tus próximos pedidos.
      </Text>

      <Controller
        control={control}
        name="conjuntoResidencial"
        render={({ field, fieldState }) => (
          <View style={estilos.grupoCampo}>
            <Text style={estilos.etiqueta}>Conjunto residencial</Text>
            <TextInput
              accessibilityLabel="Conjunto residencial"
              autoCapitalize="words"
              maxLength={120}
              onBlur={field.onBlur}
              onChangeText={(valor) => {
                field.onChange(valor);
                setGuardado(false);
              }}
              placeholder="Ej. Torres del Parque"
              style={[estilos.campo, fieldState.error ? estilos.campoError : null]}
              value={field.value}
            />
            {fieldState.error ? (
              <Text style={estilos.error}>{fieldState.error.message}</Text>
            ) : null}
          </View>
        )}
      />

      <Controller
        control={control}
        name="apartamento"
        render={({ field, fieldState }) => (
          <View style={estilos.grupoCampo}>
            <Text style={estilos.etiqueta}>Apartamento</Text>
            <TextInput
              accessibilityLabel="Apartamento"
              autoCapitalize="characters"
              maxLength={60}
              onBlur={field.onBlur}
              onChangeText={(valor) => {
                field.onChange(valor);
                setGuardado(false);
              }}
              placeholder="Ej. 302"
              style={[estilos.campo, fieldState.error ? estilos.campoError : null]}
              value={field.value}
            />
            {fieldState.error ? (
              <Text style={estilos.error}>{fieldState.error.message}</Text>
            ) : null}
          </View>
        )}
      />

      {mensajeGeneral ? (
        <Text accessibilityLiveRegion="polite" style={estilos.error}>
          {mensajeGeneral}
        </Text>
      ) : null}
      {guardado ? (
        <Text accessibilityLiveRegion="polite" style={estilos.exito}>
          Ubicación guardada.
        </Text>
      ) : null}

      <Pressable
        accessibilityRole="button"
        disabled={actualizacion.isPending}
        onPress={() => {
          void handleSubmit(guardarUbicacion)();
        }}
        style={[estilos.boton, actualizacion.isPending ? estilos.botonDeshabilitado : null]}
      >
        {actualizacion.isPending ? (
          <ActivityIndicator color="#fff" />
        ) : (
          <Text style={estilos.textoBoton}>Guardar ubicación</Text>
        )}
      </Pressable>
    </ScrollView>
  );
}

const estilos = StyleSheet.create({
  contenedor: {
    flexGrow: 1,
    gap: 18,
    padding: 24,
  },
  titulo: {
    fontSize: 24,
    fontWeight: "700",
  },
  descripcion: {
    color: "#4b5563",
    lineHeight: 21,
  },
  grupoCampo: {
    gap: 6,
  },
  etiqueta: {
    fontSize: 16,
    fontWeight: "600",
  },
  campo: {
    borderColor: "#6b7280",
    borderRadius: 8,
    borderWidth: 1,
    minHeight: 48,
    paddingHorizontal: 12,
  },
  campoError: {
    borderColor: "#b91c1c",
  },
  error: {
    color: "#b91c1c",
  },
  exito: {
    color: "#166534",
    fontWeight: "600",
  },
  boton: {
    alignItems: "center",
    backgroundColor: "#0a7ea4",
    borderRadius: 8,
    justifyContent: "center",
    minHeight: 48,
    paddingHorizontal: 16,
  },
  botonDeshabilitado: {
    opacity: 0.65,
  },
  textoBoton: {
    color: "#fff",
    fontWeight: "700",
  },
  estado: {
    alignItems: "center",
    flex: 1,
    gap: 16,
    justifyContent: "center",
    padding: 24,
  },
});
