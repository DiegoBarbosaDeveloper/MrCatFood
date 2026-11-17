import {
  formatearEstadoPedido,
  formatearFechaEntrega,
  formatearHoraEntrega,
  obtenerTransicionesPedido,
} from "../src/utilidades/formatearPedido";
import { formatearPrecio } from "../src/utilidades/formatearPrecio";

describe("formatearFechaEntrega", () => {
  it("formatea la fecha en español con la zona de Bogotá", () => {
    expect(formatearFechaEntrega("2026-11-17")).toContain("2026");
    expect(formatearFechaEntrega("2026-11-17")).toContain("17");
  });
});

describe("formatearHoraEntrega", () => {
  it("muestra la hora en formato de doce horas", () => {
    expect(formatearHoraEntrega("12:30")).toMatch(/12:30/);
    expect(formatearHoraEntrega("18:00")).toMatch(/6:00/);
  });
});

describe("formatearEstadoPedido", () => {
  it("devuelve el estado legible en español", () => {
    expect(formatearEstadoPedido("PENDIENTE")).toBe("Pendiente");
    expect(formatearEstadoPedido("EN_PREPARACION")).toBe("En preparación");
    expect(formatearEstadoPedido("CANCELADO")).toBe("Cancelado");
  });
});

describe("obtenerTransicionesPedido", () => {
  it("devuelve solo las transiciones permitidas hacia adelante", () => {
    expect(obtenerTransicionesPedido("PENDIENTE")).toEqual(["CONFIRMADO", "CANCELADO"]);
    expect(obtenerTransicionesPedido("EN_PREPARACION")).toEqual(["ENTREGADO", "CANCELADO"]);
  });

  it("devuelve una lista vacía para los estados finales", () => {
    expect(obtenerTransicionesPedido("ENTREGADO")).toEqual([]);
    expect(obtenerTransicionesPedido("CANCELADO")).toEqual([]);
  });
});

describe("formatearPrecio", () => {
  it("muestra el símbolo de pesos colombianos y separador de miles", () => {
    const resultado = formatearPrecio(43000);

    expect(resultado).toContain("$");
    expect(resultado).toMatch(/43\.?\s?000/);
  });
});
