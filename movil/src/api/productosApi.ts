import type {
  DisponibilidadRequest,
  ProductoRequest,
  ProductoResponse,
} from "../tipos/producto";
import { clienteHttp } from "./clienteHttp";

export const productosApi = {
  listar: () => clienteHttp<ProductoResponse[]>("/api/productos"),
  consultar: (productoId: number) =>
    clienteHttp<ProductoResponse>(`/api/productos/${productoId}`),
  listarAdministracion: () =>
    clienteHttp<ProductoResponse[]>("/api/admin/productos"),
  crear: (solicitud: ProductoRequest) =>
    clienteHttp<ProductoResponse>("/api/admin/productos", {
      method: "POST",
      body: JSON.stringify(solicitud),
    }),
  actualizar: (productoId: number, solicitud: ProductoRequest) =>
    clienteHttp<ProductoResponse>(`/api/admin/productos/${productoId}`, {
      method: "PUT",
      body: JSON.stringify(solicitud),
    }),
  actualizarDisponibilidad: (
    productoId: number,
    solicitud: DisponibilidadRequest,
  ) =>
    clienteHttp<ProductoResponse>(
      `/api/admin/productos/${productoId}/disponibilidad`,
      {
        method: "PATCH",
        body: JSON.stringify(solicitud),
      },
    ),
};
