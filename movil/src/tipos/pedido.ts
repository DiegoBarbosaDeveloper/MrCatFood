export interface HorarioResponse {
  horarioRecepcionInicio: string;
  horarioRecepcionFin: string;
  ventanaEntregaInicio: string;
  ventanaEntregaFin: string;
  zona: string;
}

export interface ProductoCantidadRequest {
  productoId: number;
  cantidad: number;
}

export interface PedidoRequest {
  productos: ProductoCantidadRequest[];
  fechaEntrega: string;
  horaEntrega: string;
  notas: string | null;
}

export interface DetallePedidoResponse {
  productoId: number | null;
  nombreProducto: string;
  precioUnitario: number;
  cantidad: number;
  subtotal: number;
}

export interface PrevisualizacionPedidoResponse {
  detalles: DetallePedidoResponse[];
  subtotal: number;
  total: number;
  conjuntoResidencial: string;
  apartamento: string;
  fechaEntrega: string;
  horaEntrega: string;
  notas: string | null;
}

export interface ClientePedidoResponse {
  id: number;
  nombre: string;
  telefono: string;
}

export type EstadoPedido =
  | "PENDIENTE"
  | "CONFIRMADO"
  | "EN_PREPARACION"
  | "ENTREGADO"
  | "CANCELADO";

export interface PedidoResponse extends PrevisualizacionPedidoResponse {
  id: number;
  codigo: string;
  estado: EstadoPedido;
  cliente: ClientePedidoResponse;
  creadoEn: string;
}

export interface PedidoResumenResponse {
  id: number;
  codigo: string;
  estado: EstadoPedido;
  cliente: ClientePedidoResponse;
  conjuntoResidencial: string;
  apartamento: string;
  fechaEntrega: string;
  horaEntrega: string;
  total: number;
  creadoEn: string;
}

export interface PaginaResponse<T> {
  contenido: T[];
  pagina: number;
  tamano: number;
  totalElementos: number;
  totalPaginas: number;
}

export interface FiltrosPedidosAdmin {
  estado?: EstadoPedido;
  fechaDesde?: string;
  fechaHasta?: string;
  pagina: number;
  tamano?: number;
}

export interface EnlaceWhatsAppResponse {
  enlace: string;
}
