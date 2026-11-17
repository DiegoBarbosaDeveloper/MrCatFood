import type { EstadoPedido } from "../tipos/pedido";

const formateadorFecha = new Intl.DateTimeFormat("es-CO", {
  dateStyle: "medium",
  timeZone: "America/Bogota",
});

export function formatearFechaEntrega(fecha: string): string {
  return formateadorFecha.format(new Date(`${fecha}T12:00:00-05:00`));
}

export function formatearHoraEntrega(hora: string): string {
  const [horas, minutos] = hora.split(":").map(Number);
  const fecha = new Date(2026, 0, 1, horas, minutos);
  return new Intl.DateTimeFormat("es-CO", {
    hour: "numeric",
    minute: "2-digit",
  }).format(fecha);
}

export function formatearEstadoPedido(estado: EstadoPedido): string {
  const nombres: Record<EstadoPedido, string> = {
    PENDIENTE: "Pendiente",
    CONFIRMADO: "Confirmado",
    EN_PREPARACION: "En preparación",
    ENTREGADO: "Entregado",
    CANCELADO: "Cancelado",
  };
  return nombres[estado];
}
