import React, {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from "react";
import { almacenToken } from "./almacenToken";
import { autenticacionApi } from "../../api/autenticacionApi";
import type { UsuarioResponse } from "../../tipos/autenticacion";

interface Sesion {
  usuario: UsuarioResponse | null;
  token: string | null;
  cargando: boolean;
  iniciarSesion: (email: string, password: string) => Promise<UsuarioResponse>;
  registrarse: (datos: {
    nombre: string;
    email: string;
    telefono: string;
    password: string;
  }) => Promise<UsuarioResponse>;
  cerrarSesion: () => Promise<void>;
  actualizarUsuario: (usuario: UsuarioResponse) => void;
}

const ContextoSesion = createContext<Sesion | null>(null);

export function ProveedorSesion({ children }: { children: React.ReactNode }) {
  const [usuario, setUsuario] = useState<UsuarioResponse | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [cargando, setCargando] = useState(true);

  useEffect(() => {
    (async () => {
      const guardado = await almacenToken.leer();
      if (guardado) {
        try {
          const yo = await autenticacionApi.yo();
          setToken(guardado);
          setUsuario(yo);
        } catch {
          await almacenToken.borrar();
        }
      }
      setCargando(false);
    })();
  }, []);

  const iniciarSesion = useCallback(async (email: string, password: string) => {
    const respuesta = await autenticacionApi.login({ email, password });
    await almacenToken.guardar(respuesta.token);
    const yo = await autenticacionApi.yo();
    setToken(respuesta.token);
    setUsuario(yo);
    return yo;
  }, []);

  const registrarse = useCallback(
    async (datos: { nombre: string; email: string; telefono: string; password: string }) => {
      const respuesta = await autenticacionApi.registrar(datos);
      await almacenToken.guardar(respuesta.token);
      const yo = await autenticacionApi.yo();
      setToken(respuesta.token);
      setUsuario(yo);
      return yo;
    },
    [],
  );

  const cerrarSesion = useCallback(async () => {
    try {
      await autenticacionApi.logout();
    } catch {
      // aunque falle, limpiamos localmente
    }
    await almacenToken.borrar();
    setToken(null);
    setUsuario(null);
  }, []);

  const actualizarUsuario = useCallback((usuarioActualizado: UsuarioResponse) => {
    setUsuario(usuarioActualizado);
  }, []);

  const valor = useMemo<Sesion>(
    () => ({
      usuario,
      token,
      cargando,
      iniciarSesion,
      registrarse,
      cerrarSesion,
      actualizarUsuario,
    }),
    [
      usuario,
      token,
      cargando,
      iniciarSesion,
      registrarse,
      cerrarSesion,
      actualizarUsuario,
    ],
  );

  return <ContextoSesion.Provider value={valor}>{children}</ContextoSesion.Provider>;
}

export function useSesion(): Sesion {
  const ctx = useContext(ContextoSesion);
  if (!ctx) {
    throw new Error("useSesion debe usarse dentro de ProveedorSesion");
  }
  return ctx;
}
