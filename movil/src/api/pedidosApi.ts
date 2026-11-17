import type {
  HorarioResponse,
  PedidoRequest,
  PedidoResponse,
  PrevisualizacionPedidoResponse,
} from "../tipos/pedido";
import { clienteHttp } from "./clienteHttp";

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
};
