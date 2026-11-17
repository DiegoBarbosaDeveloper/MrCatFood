const formateadorPesos = new Intl.NumberFormat("es-CO", {
  style: "currency",
  currency: "COP",
  maximumFractionDigits: 0,
});

export function formatearPrecio(precio: number): string {
  return formateadorPesos.format(precio);
}
