import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { router, useLocalSearchParams } from "expo-router";
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
import { ErrorHttp } from "../../src/api/clienteHttp";
import { productosApi } from "../../src/api/productosApi";
import type { ProductoRequest } from "../../src/tipos/producto";

const esquemaProducto = z.object({
  nombre: z
    .string()
    .trim()
    .min(1, "El nombre es obligatorio")
    .max(120, "Debe tener máximo 120 caracteres"),
  descripcion: z
    .string()
    .trim()
    .min(1, "La descripción es obligatoria")
    .max(600, "Debe tener máximo 600 caracteres"),
  precio: z
    .string()
    .trim()
    .regex(/^\d+(?:[.,]\d{1,2})?$/, "Ingresa un precio válido con máximo 2 decimales"),
  stock: z.string().trim().regex(/^\d+$/, "Ingresa un número entero no negativo"),
  ingredientesTexto: z
    .string()
    .trim()
    .min(1, "Incluye al menos un ingrediente")
    .refine(
      (valor) =>
        valor
          .split(/[\n,]/)
          .map((ingrediente) => ingrediente.trim())
          .filter(Boolean)
          .every((ingrediente) => ingrediente.length <= 120),
      "Cada ingrediente debe tener máximo 120 caracteres",
    ),
});

type FormularioProducto = z.infer<typeof esquemaProducto>;

export default function EditarProducto() {
  const parametros = useLocalSearchParams<{ productoId?: string }>();
  const productoId = parametros.productoId ? Number(parametros.productoId) : null;
  const editando = productoId !== null;
  const identificadorValido = productoId === null || (Number.isInteger(productoId) && productoId > 0);
  const clienteConsultas = useQueryClient();
  const [mensajeGeneral, setMensajeGeneral] = useState<string | null>(null);
  const productos = useQuery({
    queryKey: ["productos", "administracion"],
    queryFn: productosApi.listarAdministracion,
    enabled: editando && identificadorValido,
  });
  const producto = editando
    ? productos.data?.find((elemento) => elemento.id === productoId)
    : undefined;
  const {
    control,
    handleSubmit,
    reset,
    setError: establecerErrorCampo,
  } = useForm<FormularioProducto>({
    resolver: zodResolver(esquemaProducto),
    defaultValues: {
      nombre: "",
      descripcion: "",
      precio: "",
      stock: "",
      ingredientesTexto: "",
    },
  });

  useEffect(() => {
    if (producto) {
      reset({
        nombre: producto.nombre,
        descripcion: producto.descripcion,
        precio: producto.precio.toString(),
        stock: producto.stock.toString(),
        ingredientesTexto: producto.ingredientes.join("\n"),
      });
    }
  }, [producto, reset]);

  const guardado = useMutation({
    mutationFn: (solicitud: ProductoRequest) =>
      editando && productoId !== null
        ? productosApi.actualizar(productoId, solicitud)
        : productosApi.crear(solicitud),
    onSuccess: async () => {
      await clienteConsultas.invalidateQueries({ queryKey: ["productos"] });
      router.back();
    },
  });

  async function guardarProducto(valores: FormularioProducto) {
    setMensajeGeneral(null);
    const ingredientes = valores.ingredientesTexto
      .split(/[\n,]/)
      .map((ingrediente) => ingrediente.trim())
      .filter(Boolean);
    const solicitud: ProductoRequest = {
      nombre: valores.nombre.trim(),
      descripcion: valores.descripcion.trim(),
      precio: Number(valores.precio.replace(",", ".")),
      stock: Number(valores.stock),
      ingredientes,
    };

    try {
      await guardado.mutateAsync(solicitud);
    } catch (error) {
      if (error instanceof ErrorHttp && error.campos) {
        const campos: (keyof FormularioProducto)[] = [
          "nombre",
          "descripcion",
          "precio",
          "stock",
        ];
        campos.forEach((campo) => {
          const mensaje = error.campos?.[campo];
          if (mensaje) establecerErrorCampo(campo, { message: mensaje });
        });
        const errorIngredientes = error.campos.ingredientes;
        if (errorIngredientes) {
          establecerErrorCampo("ingredientesTexto", { message: errorIngredientes });
        }
      }
      setMensajeGeneral(
        error instanceof Error ? error.message : "No se pudo guardar el producto",
      );
    }
  }

  if (!identificadorValido) {
    return (
      <View style={estilos.estado}>
        <Text style={estilos.error}>El producto solicitado no es válido.</Text>
      </View>
    );
  }

  if (editando && productos.isPending) {
    return (
      <View accessibilityLiveRegion="polite" style={estilos.estado}>
        <ActivityIndicator size="large" />
        <Text>Cargando producto…</Text>
      </View>
    );
  }

  if (editando && (productos.isError || !producto)) {
    return (
      <View accessibilityLiveRegion="polite" style={estilos.estado}>
        <Text style={estilos.error}>No se pudo encontrar el producto.</Text>
        <Pressable
          accessibilityRole="button"
          onPress={() => productos.refetch()}
          style={estilos.botonPrincipal}
        >
          <Text style={estilos.textoBoton}>Reintentar</Text>
        </Pressable>
      </View>
    );
  }

  return (
    <ScrollView contentContainerStyle={estilos.contenedor} keyboardShouldPersistTaps="handled">
      <Text style={estilos.titulo}>{editando ? "Editar producto" : "Crear producto"}</Text>

      <Controller
        control={control}
        name="nombre"
        render={({ field, fieldState }) => (
          <View style={estilos.grupoCampo}>
            <Text style={estilos.etiqueta}>Nombre</Text>
            <TextInput
              accessibilityLabel="Nombre del producto"
              maxLength={120}
              onBlur={field.onBlur}
              onChangeText={field.onChange}
              style={[estilos.campo, fieldState.error ? estilos.campoError : null]}
              value={field.value}
            />
            {fieldState.error ? <Text style={estilos.error}>{fieldState.error.message}</Text> : null}
          </View>
        )}
      />

      <Controller
        control={control}
        name="descripcion"
        render={({ field, fieldState }) => (
          <View style={estilos.grupoCampo}>
            <Text style={estilos.etiqueta}>Descripción</Text>
            <TextInput
              accessibilityLabel="Descripción del producto"
              maxLength={600}
              multiline
              numberOfLines={4}
              onBlur={field.onBlur}
              onChangeText={field.onChange}
              style={[estilos.campo, estilos.campoMultilinea, fieldState.error ? estilos.campoError : null]}
              textAlignVertical="top"
              value={field.value}
            />
            {fieldState.error ? <Text style={estilos.error}>{fieldState.error.message}</Text> : null}
          </View>
        )}
      />

      <View style={estilos.filaCampos}>
        <Controller
          control={control}
          name="precio"
          render={({ field, fieldState }) => (
            <View style={[estilos.grupoCampo, estilos.campoMitad]}>
              <Text style={estilos.etiqueta}>Precio</Text>
              <TextInput
                accessibilityLabel="Precio del producto"
                inputMode="decimal"
                onBlur={field.onBlur}
                onChangeText={field.onChange}
                placeholder="15000"
                style={[estilos.campo, fieldState.error ? estilos.campoError : null]}
                value={field.value}
              />
              {fieldState.error ? <Text style={estilos.error}>{fieldState.error.message}</Text> : null}
            </View>
          )}
        />
        <Controller
          control={control}
          name="stock"
          render={({ field, fieldState }) => (
            <View style={[estilos.grupoCampo, estilos.campoMitad]}>
              <Text style={estilos.etiqueta}>Stock</Text>
              <TextInput
                accessibilityLabel="Stock del producto"
                inputMode="numeric"
                onBlur={field.onBlur}
                onChangeText={field.onChange}
                placeholder="0"
                style={[estilos.campo, fieldState.error ? estilos.campoError : null]}
                value={field.value}
              />
              {fieldState.error ? <Text style={estilos.error}>{fieldState.error.message}</Text> : null}
            </View>
          )}
        />
      </View>

      <Controller
        control={control}
        name="ingredientesTexto"
        render={({ field, fieldState }) => (
          <View style={estilos.grupoCampo}>
            <Text style={estilos.etiqueta}>Ingredientes</Text>
            <Text style={estilos.ayuda}>Escribe uno por línea o sepáralos con comas.</Text>
            <TextInput
              accessibilityLabel="Ingredientes del producto"
              multiline
              numberOfLines={6}
              onBlur={field.onBlur}
              onChangeText={field.onChange}
              style={[estilos.campo, estilos.campoIngredientes, fieldState.error ? estilos.campoError : null]}
              textAlignVertical="top"
              value={field.value}
            />
            {fieldState.error ? <Text style={estilos.error}>{fieldState.error.message}</Text> : null}
          </View>
        )}
      />

      {mensajeGeneral ? (
        <Text accessibilityLiveRegion="polite" style={estilos.error}>
          {mensajeGeneral}
        </Text>
      ) : null}

      <Pressable
        accessibilityRole="button"
        disabled={guardado.isPending}
        onPress={() => void handleSubmit(guardarProducto)()}
        style={[estilos.botonPrincipal, guardado.isPending ? estilos.deshabilitado : null]}
      >
        {guardado.isPending ? (
          <ActivityIndicator color="#fff" />
        ) : (
          <Text style={estilos.textoBoton}>Guardar producto</Text>
        )}
      </Pressable>
    </ScrollView>
  );
}

const estilos = StyleSheet.create({
  contenedor: { flexGrow: 1, gap: 18, padding: 24 },
  titulo: { fontSize: 25, fontWeight: "700" },
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
  campoMultilinea: { minHeight: 100 },
  campoIngredientes: { minHeight: 130 },
  campoError: { borderColor: "#b91c1c" },
  filaCampos: { flexDirection: "row", gap: 12 },
  campoMitad: { flex: 1 },
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
  deshabilitado: { opacity: 0.6 },
  estado: {
    alignItems: "center",
    flex: 1,
    gap: 16,
    justifyContent: "center",
    padding: 24,
  },
});
