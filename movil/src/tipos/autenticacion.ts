export interface TokenResponse {
  token: string;
  tipo: string;
  expiraEn: string;
}

export interface UsuarioResponse {
  id: number;
  nombre: string;
  email: string;
  telefono: string;
  rol: "CLIENTE" | "ADMIN";
  conjuntoResidencial: string | null;
  apartamento: string | null;
}

export interface ProblemaHttp {
  type?: string;
  title?: string;
  status?: number;
  detail?: string;
  instance?: string;
  codigo?: string;
  campos?: Record<string, string>;
}
