import { almacenToken } from "../funcionalidades/sesion/almacenToken";
import type { ProblemaHttp } from "../tipos/autenticacion";

const URL_BASE = process.env.EXPO_PUBLIC_API_URL ?? "http://127.0.0.1:8080";

export class ErrorHttp extends Error {
  constructor(
    public readonly estado: number,
    public readonly codigo: string,
    public readonly detalle: string,
    public readonly campos?: Record<string, string>,
  ) {
    super(detalle);
  }
}

export async function clienteHttp<T>(
  ruta: string,
  opciones: RequestInit = {},
): Promise<T> {
  const token = await almacenToken.leer();
  const controlador = new AbortController();
  const temporizador = setTimeout(() => controlador.abort(), 15000);

  let respuesta: Response;
  try {
    respuesta = await fetch(`${URL_BASE}${ruta}`, {
      ...opciones,
      headers: {
        "Content-Type": "application/json",
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...(opciones.headers ?? {}),
      },
      signal: controlador.signal,
    });
  } finally {
    clearTimeout(temporizador);
  }

  if (respuesta.status === 204) {
    return undefined as T;
  }

  const texto = await respuesta.text();
  let datos: unknown = null;
  if (texto) {
    try {
      datos = JSON.parse(texto);
    } catch {
      datos = texto;
    }
  }

  if (!respuesta.ok) {
    const problema = (datos ?? {}) as ProblemaHttp;
    throw new ErrorHttp(
      respuesta.status,
      problema.codigo ?? "ERROR_INTERNO",
      problema.detail ?? respuesta.statusText,
      problema.campos,
    );
  }

  return datos as T;
}
