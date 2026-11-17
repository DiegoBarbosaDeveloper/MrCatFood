import type { ProductoResponse } from "../src/tipos/producto";
import {
  calcularCantidadTotal,
  calcularSubtotal,
  useCarrito,
} from "../src/funcionalidades/carrito/tiendaCarrito";

function producto(id: number, precio: number, stock: number, disponible = true): ProductoResponse {
  return {
    id,
    nombre: `Sándwich ${id}`,
    descripcion: "Descripción",
    ingredientes: ["Pan"],
    precio,
    stock,
    activo: true,
    disponible,
  };
}

beforeEach(() => {
  useCarrito.getState().vaciarCarrito();
});

describe("useCarrito", () => {
  it("agrega un producto y acumula la cantidad sin superar el stock", () => {
    useCarrito.getState().agregarProducto(producto(1, 15000, 3), 2);
    useCarrito.getState().agregarProducto(producto(1, 15000, 3), 5);

    const items = useCarrito.getState().items;
    expect(items).toHaveLength(1);
    expect(items[0].cantidad).toBe(3);
    expect(calcularCantidadTotal(items)).toBe(3);
    expect(calcularSubtotal(items)).toBe(45000);
  });

  it("no agrega productos agotados ni no disponibles", () => {
    useCarrito.getState().agregarProducto(producto(2, 10000, 0));
    useCarrito.getState().agregarProducto(producto(3, 10000, 5, false));

    expect(useCarrito.getState().items).toEqual([]);
  });

  it("quita un producto cuando la cantidad llega a cero", () => {
    useCarrito.getState().agregarProducto(producto(4, 12000, 5), 2);
    useCarrito.getState().actualizarCantidad(4, 0);

    expect(useCarrito.getState().items).toEqual([]);
  });

  it("sincroniza el catálogo y descarta los productos que ya no están disponibles", () => {
    useCarrito.getState().agregarProducto(producto(5, 10000, 5), 4);
    useCarrito.getState().agregarProducto(producto(6, 20000, 5), 1);

    useCarrito
      .getState()
      .sincronizarCatalogo([producto(5, 11000, 2), producto(6, 20000, 5, false)]);

    const items = useCarrito.getState().items;
    expect(items).toHaveLength(1);
    expect(items[0].cantidad).toBe(2);
    expect(items[0].precio).toBe(11000);
  });

  it("calcula el subtotal con dos decimales", () => {
    const items = [
      { ...producto(7, 12345.67, 5), cantidad: 2 },
      { ...producto(8, 10.1, 5), cantidad: 1 },
    ].map((productoBase) => ({
      productoId: productoBase.id,
      nombre: productoBase.nombre,
      precio: productoBase.precio,
      stock: productoBase.stock,
      cantidad: productoBase.cantidad,
    }));

    expect(calcularSubtotal(items)).toBe(24701.44);
  });
});
