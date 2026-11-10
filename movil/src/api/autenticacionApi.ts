import { clienteHttp } from "./clienteHttp";
import type { TokenResponse, UsuarioResponse } from "../tipos/autenticacion";

export const autenticacionApi = {
  registrar: (cuerpo: {
    nombre: string;
    email: string;
    telefono: string;
    password: string;
  }) => clienteHttp<TokenResponse>("/api/auth/registro", {
    method: "POST",
    body: JSON.stringify(cuerpo),
  }),

  login: (cuerpo: { email: string; password: string }) =>
    clienteHttp<TokenResponse>("/api/auth/login", {
      method: "POST",
      body: JSON.stringify(cuerpo),
    }),

  yo: () => clienteHttp<UsuarioResponse>("/api/auth/yo"),

  logout: () =>
    clienteHttp<void>("/api/auth/logout", { method: "POST" }),
};
