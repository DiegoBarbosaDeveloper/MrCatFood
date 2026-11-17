import type { EnlaceWhatsAppResponse } from "../tipos/pedido";
import { clienteHttp } from "./clienteHttp";

export const whatsappApi = {
  generarEnlace: (pedidoId: number) =>
    clienteHttp<EnlaceWhatsAppResponse>(`/api/pedidos/${pedidoId}/whatsapp`, {
      method: "POST",
    }),
};
