import type {
  EstadoPedido,
  FiltrosPedidosAdmin,
  HorarioResponse,
  PaginaResponse,
  PedidoRequest,
  PedidoResponse,
  PedidoResumenResponse,
  PrevisualizacionPedidoResponse,
} from "../tipos/pedido";
import { clienteHttp } from "./clienteHttp";

function crearConsultaAdministrativa(filtros: FiltrosPedidosAdmin): string {
  const parametros = [
    filtros.estado ? `estado=${encodeURIComponent(filtros.estado)}` : null,
    filtros.fechaDesde ? `fechaDesde=${encodeURIComponent(filtros.fechaDesde)}` : null,
    filtros.fechaHasta ? `fechaHasta=${encodeURIComponent(filtros.fechaHasta)}` : null,
    `pagina=${filtros.pagina}`,
    `tamano=${filtros.tamano ?? 20}`,
  ].filter(Boolean);
  return `/api/admin/pedidos?${parametros.join("&")}`;
}

export const pedidosApi = {
  consultarHorario: () => clienteHttp<HorarioResponse>("/api/configuracion/horario"),
  previsualizar: (solicitud: PedidoRequest) =>
    clienteHttp<PrevisualizacionPedidoResponse>("/api/pedidos/preview", {
      method: "POST",
      body: JSON.stringify(solicitud),
    }),
  crear: (solicitud: PedidoRequest) =>
    clienteHttp<PedidoResponse>("/api/pedidos", {
      method: "POST",
      body: JSON.stringify(solicitud),
    }),
  listarMios: () => clienteHttp<PedidoResponse[]>("/api/pedidos/mios"),
  consultar: (pedidoId: number) =>
    clienteHttp<PedidoResponse>(`/api/pedidos/${pedidoId}`),
  listarAdministracion: (filtros: FiltrosPedidosAdmin) =>
    clienteHttp<PaginaResponse<PedidoResumenResponse>>(
      crearConsultaAdministrativa(filtros),
    ),
  consultarAdministracion: (pedidoId: number) =>
    clienteHttp<PedidoResponse>(`/api/admin/pedidos/${pedidoId}`),
  cambiarEstado: (pedidoId: number, estado: EstadoPedido) =>
    clienteHttp<PedidoResponse>(`/api/admin/pedidos/${pedidoId}/estado`, {
      method: "PATCH",
      body: JSON.stringify({ estado }),
    }),
};
