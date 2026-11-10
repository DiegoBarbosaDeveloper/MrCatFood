import type { UsuarioResponse } from "../tipos/autenticacion";
import { clienteHttp } from "./clienteHttp";

export interface ActualizarUbicacionRequest {
  conjuntoResidencial: string;
  apartamento: string;
}

export const usuariosApi = {
  actualizarUbicacion: (solicitud: ActualizarUbicacionRequest) =>
    clienteHttp<UsuarioResponse>("/api/usuarios/yo/ubicacion", {
      method: "PUT",
      body: JSON.stringify(solicitud),
    }),
};
