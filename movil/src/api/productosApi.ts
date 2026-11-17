import type { ProductoResponse } from "../tipos/producto";
import { clienteHttp } from "./clienteHttp";

export const productosApi = {
  listar: () => clienteHttp<ProductoResponse[]>("/api/productos"),
  consultar: (productoId: number) =>
    clienteHttp<ProductoResponse>(`/api/productos/${productoId}`),
};
