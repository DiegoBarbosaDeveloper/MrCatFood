import { useQuery } from "@tanstack/react-query";
import { router } from "expo-router";
import { useState } from "react";
import {
  ActivityIndicator,
  FlatList,
  Pressable,
  StyleSheet,
  Text,
  TextInput,
  View,
} from "react-native";
import { pedidosApi } from "../../src/api/pedidosApi";
import type { EstadoPedido } from "../../src/tipos/pedido";
import {
  formatearEstadoPedido,
  formatearFechaEntrega,
  formatearHoraEntrega,
} from "../../src/utilidades/formatearPedido";
import { formatearPrecio } from "../../src/utilidades/formatearPrecio";

const ESTADOS: EstadoPedido[] = [
  "PENDIENTE",
  "CONFIRMADO",
  "EN_PREPARACION",
  "ENTREGADO",
  "CANCELADO",
];
const FORMATO_FECHA = /^\d{4}-\d{2}-\d{2}$/;

function esFechaValida(fecha: string): boolean {
  if (!FORMATO_FECHA.test(fecha)) return false;
  const [ano, mes, dia] = fecha.split("-").map(Number);
  const fechaUtc = new Date(Date.UTC(ano, mes - 1, dia));
  return (
    fechaUtc.getUTCFullYear() === ano &&
    fechaUtc.getUTCMonth() === mes - 1 &&
    fechaUtc.getUTCDate() === dia
  );
}

export default function AdminPedidos() {
  const [estado, setEstado] = useState<EstadoPedido | undefined>();
  const [fechaDesdeEntrada, setFechaDesdeEntrada] = useState("");
  const [fechaHastaEntrada, setFechaHastaEntrada] = useState("");
  const [fechaDesde, setFechaDesde] = useState("");
  const [fechaHasta, setFechaHasta] = useState("");
  const [pagina, setPagina] = useState(0);
  const [errorFechas, setErrorFechas] = useState<string | null>(null);
  const pedidos = useQuery({
    queryKey: ["pedidos", "administracion", estado, fechaDesde, fechaHasta, pagina],
    queryFn: () =>
      pedidosApi.listarAdministracion({
        estado,
        fechaDesde: fechaDesde || undefined,
        fechaHasta: fechaHasta || undefined,
        pagina,
      }),
  });

  function seleccionarEstado(nuevoEstado?: EstadoPedido) {
    setEstado(nuevoEstado);
    setPagina(0);
  }

  function aplicarFechas() {
    if (
      (fechaDesdeEntrada && !esFechaValida(fechaDesdeEntrada)) ||
      (fechaHastaEntrada && !esFechaValida(fechaHastaEntrada))
    ) {
      setErrorFechas("Usa el formato AAAA-MM-DD.");
      return;
    }
    if (fechaDesdeEntrada && fechaHastaEntrada && fechaDesdeEntrada > fechaHastaEntrada) {
      setErrorFechas("La fecha inicial no puede ser posterior a la final.");
      return;
    }
    setErrorFechas(null);
    setFechaDesde(fechaDesdeEntrada);
    setFechaHasta(fechaHastaEntrada);
    setPagina(0);
  }

  const encabezado = (
    <View style={estilos.encabezado}>
      <Text style={estilos.titulo}>Pedidos</Text>
      <Text style={estilos.subtitulo}>Filtrar por estado</Text>
      <View style={estilos.filtrosEstado}>
        <BotonFiltro
          activo={!estado}
          etiqueta="Todos"
          onPress={() => seleccionarEstado(undefined)}
        />
        {ESTADOS.map((opcion) => (
          <BotonFiltro
            activo={estado === opcion}
            etiqueta={formatearEstadoPedido(opcion)}
            key={opcion}
            onPress={() => seleccionarEstado(opcion)}
          />
        ))}
      </View>
      <Text style={estilos.subtitulo}>Rango de entrega</Text>
      <View style={estilos.filaFechas}>
        <TextInput
          accessibilityLabel="Fecha inicial"
          autoCapitalize="none"
          inputMode="numeric"
          onChangeText={setFechaDesdeEntrada}
          placeholder="Desde: AAAA-MM-DD"
          style={estilos.campo}
          value={fechaDesdeEntrada}
        />
        <TextInput
          accessibilityLabel="Fecha final"
          autoCapitalize="none"
          inputMode="numeric"
          onChangeText={setFechaHastaEntrada}
          placeholder="Hasta: AAAA-MM-DD"
          style={estilos.campo}
          value={fechaHastaEntrada}
        />
      </View>
      {errorFechas ? <Text style={estilos.error}>{errorFechas}</Text> : null}
      <Pressable accessibilityRole="button" onPress={aplicarFechas} style={estilos.botonAplicar}>
        <Text style={estilos.textoBotonAplicar}>Aplicar fechas</Text>
      </Pressable>
      {pedidos.data ? (
        <Text style={estilos.resultados}>{pedidos.data.totalElementos} pedido(s)</Text>
      ) : null}
    </View>
  );

  if (pedidos.isPending) {
    return (
      <View style={estilos.pantalla}>
        {encabezado}
        <View accessibilityLiveRegion="polite" style={estilos.estadoPantalla}>
          <ActivityIndicator size="large" />
          <Text>Cargando pedidos…</Text>
        </View>
      </View>
    );
  }

  if (pedidos.isError) {
    return (
      <View style={estilos.pantalla}>
        {encabezado}
        <View accessibilityLiveRegion="polite" style={estilos.estadoPantalla}>
          <Text style={estilos.error}>No se pudieron cargar los pedidos.</Text>
          <Pressable
            accessibilityRole="button"
            onPress={() => pedidos.refetch()}
            style={estilos.botonPrincipal}
          >
            <Text style={estilos.textoBotonPrincipal}>Reintentar</Text>
          </Pressable>
        </View>
      </View>
    );
  }

  return (
    <FlatList
      contentContainerStyle={estilos.lista}
      data={pedidos.data.contenido}
      keyExtractor={(pedido) => pedido.id.toString()}
      ListHeaderComponent={encabezado}
      ListEmptyComponent={<Text style={estilos.vacio}>No hay pedidos con estos filtros.</Text>}
      ListFooterComponent={
        pedidos.data.totalPaginas > 0 ? (
          <View style={estilos.paginacion}>
            <Pressable
              accessibilityRole="button"
              disabled={pagina === 0}
              onPress={() => setPagina((valor) => Math.max(0, valor - 1))}
              style={[estilos.botonPagina, pagina === 0 ? estilos.deshabilitado : null]}
            >
              <Text style={estilos.textoPagina}>Anterior</Text>
            </Pressable>
            <Text>
              Página {pagina + 1} de {pedidos.data.totalPaginas}
            </Text>
            <Pressable
              accessibilityRole="button"
              disabled={pagina + 1 >= pedidos.data.totalPaginas}
              onPress={() => setPagina((valor) => valor + 1)}
              style={[
                estilos.botonPagina,
                pagina + 1 >= pedidos.data.totalPaginas ? estilos.deshabilitado : null,
              ]}
            >
              <Text style={estilos.textoPagina}>Siguiente</Text>
            </Pressable>
          </View>
        ) : undefined
      }
      onRefresh={() => pedidos.refetch()}
      refreshing={pedidos.isRefetching}
      renderItem={({ item }) => (
        <Pressable
          accessibilityHint="Abre el pedido y permite cambiar su estado"
          accessibilityLabel={`${item.codigo}, ${formatearEstadoPedido(item.estado)}`}
          accessibilityRole="button"
          onPress={() =>
            router.push({
              pathname: "/(admin)/detalle-pedido",
              params: { pedidoId: item.id.toString() },
            })
          }
          style={({ pressed }) => [estilos.tarjeta, pressed ? estilos.presionado : null]}
        >
          <View style={estilos.filaTarjeta}>
            <View style={estilos.datosPedido}>
              <Text style={estilos.codigo}>{item.codigo}</Text>
              <Text style={estilos.cliente}>{item.cliente.nombre}</Text>
            </View>
            <View style={estilos.badge}>
              <Text style={estilos.textoBadge}>{formatearEstadoPedido(item.estado)}</Text>
            </View>
          </View>
          <Text style={estilos.detalle}>
            {item.conjuntoResidencial} · {item.apartamento}
          </Text>
          <Text style={estilos.detalle}>
            {formatearFechaEntrega(item.fechaEntrega)}, {formatearHoraEntrega(item.horaEntrega)}
          </Text>
          <Text style={estilos.total}>{formatearPrecio(item.total)}</Text>
        </Pressable>
      )}
    />
  );
}

function BotonFiltro({
  activo,
  etiqueta,
  onPress,
}: {
  activo: boolean;
  etiqueta: string;
  onPress: () => void;
}) {
  return (
    <Pressable
      accessibilityRole="button"
      accessibilityState={{ selected: activo }}
      onPress={onPress}
      style={[estilos.filtroEstado, activo ? estilos.filtroActivo : null]}
    >
      <Text style={[estilos.textoFiltro, activo ? estilos.textoFiltroActivo : null]}>
        {etiqueta}
      </Text>
    </Pressable>
  );
}

const estilos = StyleSheet.create({
  pantalla: { flex: 1, padding: 20 },
  lista: { flexGrow: 1, gap: 14, padding: 20 },
  encabezado: { gap: 12, marginBottom: 8 },
  titulo: { fontSize: 26, fontWeight: "700" },
  subtitulo: { fontWeight: "700" },
  filtrosEstado: { flexDirection: "row", flexWrap: "wrap", gap: 8 },
  filtroEstado: {
    borderColor: "#9ca3af",
    borderRadius: 999,
    borderWidth: 1,
    minHeight: 40,
    paddingHorizontal: 12,
    justifyContent: "center",
  },
  filtroActivo: { backgroundColor: "#0a7ea4", borderColor: "#0a7ea4" },
  textoFiltro: { color: "#374151", fontWeight: "600" },
  textoFiltroActivo: { color: "#fff" },
  filaFechas: { flexDirection: "row", gap: 8 },
  campo: {
    borderColor: "#9ca3af",
    borderRadius: 8,
    borderWidth: 1,
    flex: 1,
    minHeight: 46,
    paddingHorizontal: 10,
  },
  botonAplicar: {
    alignItems: "center",
    borderColor: "#0a7ea4",
    borderRadius: 8,
    borderWidth: 1,
    justifyContent: "center",
    minHeight: 44,
  },
  textoBotonAplicar: { color: "#075985", fontWeight: "700" },
  resultados: { color: "#4b5563" },
  tarjeta: {
    borderColor: "#d1d5db",
    borderRadius: 12,
    borderWidth: 1,
    gap: 8,
    padding: 16,
  },
  presionado: { opacity: 0.7 },
  filaTarjeta: { alignItems: "center", flexDirection: "row", gap: 10 },
  datosPedido: { flex: 1, gap: 3 },
  codigo: { fontSize: 17, fontWeight: "700" },
  cliente: { color: "#374151" },
  detalle: { color: "#4b5563" },
  total: { color: "#075985", fontSize: 18, fontWeight: "700" },
  badge: { backgroundColor: "#dbeafe", borderRadius: 999, paddingHorizontal: 10, paddingVertical: 5 },
  textoBadge: { color: "#1e40af", fontSize: 12, fontWeight: "700" },
  paginacion: {
    alignItems: "center",
    flexDirection: "row",
    gap: 12,
    justifyContent: "space-between",
    paddingVertical: 16,
  },
  botonPagina: {
    borderColor: "#9ca3af",
    borderRadius: 8,
    borderWidth: 1,
    justifyContent: "center",
    minHeight: 44,
    paddingHorizontal: 12,
  },
  textoPagina: { color: "#374151", fontWeight: "700" },
  deshabilitado: { opacity: 0.4 },
  estadoPantalla: { alignItems: "center", flex: 1, gap: 16, justifyContent: "center" },
  vacio: { color: "#4b5563", paddingVertical: 48, textAlign: "center" },
  error: { color: "#b91c1c", textAlign: "center" },
  botonPrincipal: {
    alignItems: "center",
    backgroundColor: "#0a7ea4",
    borderRadius: 8,
    justifyContent: "center",
    minHeight: 48,
    paddingHorizontal: 18,
  },
  textoBotonPrincipal: { color: "#fff", fontWeight: "700" },
});
