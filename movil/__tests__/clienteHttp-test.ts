import { almacenToken } from "../src/funcionalidades/sesion/almacenToken";
import { clienteHttp, ErrorHttp } from "../src/api/clienteHttp";

jest.mock("../src/funcionalidades/sesion/almacenToken", () => ({
  almacenToken: { leer: jest.fn() },
}));

const leerToken = almacenToken.leer as jest.Mock;
const fetchMock = jest.fn();

function respuesta(status: number, cuerpo: unknown, statusText = ""): Response {
  return {
    status,
    ok: status >= 200 && status < 300,
    statusText,
    text: async () => (typeof cuerpo === "string" ? cuerpo : JSON.stringify(cuerpo)),
  } as Response;
}

beforeEach(() => {
  jest.clearAllMocks();
  leerToken.mockResolvedValue(null);
  globalThis.fetch = fetchMock as unknown as typeof fetch;
});

describe("clienteHttp", () => {
  it("envía el token Bearer cuando existe", async () => {
    leerToken.mockResolvedValue("token-de-prueba");
    fetchMock.mockResolvedValue(respuesta(200, { ok: true }));

    await clienteHttp("/api/productos");

    expect(fetchMock).toHaveBeenCalledWith(
      expect.stringContaining("/api/productos"),
      expect.objectContaining({
        headers: expect.objectContaining({ Authorization: "Bearer token-de-prueba" }),
      }),
    );
  });

  it("devuelve undefined para una respuesta 204", async () => {
    fetchMock.mockResolvedValue(respuesta(204, ""));

    await expect(clienteHttp("/api/auth/logout", { method: "POST" })).resolves.toBeUndefined();
  });

  it("mapea 401 a ErrorHttp con el código del problema", async () => {
    fetchMock.mockResolvedValue(
      respuesta(401, { codigo: "NO_AUTENTICADO", detail: "Sin token" }, "Unauthorized"),
    );

    await expect(clienteHttp("/api/auth/yo")).rejects.toMatchObject({
      estado: 401,
      codigo: "NO_AUTENTICADO",
      detalle: "Sin token",
    });
  });

  it.each([
    [404, "RECURSO_NO_ENCONTRADO"],
    [409, "STOCK_INSUFICIENTE"],
    [422, "DATOS_INVALIDOS"],
  ])("mapea el estado %i a ErrorHttp con código %s", async (estado, codigo) => {
    fetchMock.mockResolvedValue(respuesta(estado, { codigo, detail: "Problema" }));

    await expect(clienteHttp("/api/pedidos")).rejects.toBeInstanceOf(ErrorHttp);
    await expect(clienteHttp("/api/pedidos")).rejects.toMatchObject({ estado, codigo });
  });

  it("usa ERROR_INTERNO y el statusText cuando el cuerpo no es un problema", async () => {
    fetchMock.mockResolvedValue(respuesta(500, "falló", "Internal Server Error"));

    await expect(clienteHttp("/api/productos")).rejects.toMatchObject({
      estado: 500,
      codigo: "ERROR_INTERNO",
      detalle: "Internal Server Error",
    });
  });
});
