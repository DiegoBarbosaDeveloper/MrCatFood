import AsyncStorage from "@react-native-async-storage/async-storage";
import { create } from "zustand";
import { createJSONStorage, persist } from "zustand/middleware";
import type { ProductoResponse } from "../../tipos/producto";

export interface ItemCarrito {
  productoId: number;
  nombre: string;
  precio: number;
  stock: number;
  cantidad: number;
}

export interface EntregaBorrador {
  fechaEntrega: string;
  horaEntrega: string;
  notas: string;
}

interface EstadoCarrito {
  items: ItemCarrito[];
  entrega: EntregaBorrador;
  agregarProducto: (producto: ProductoResponse, cantidad?: number) => void;
  actualizarCantidad: (productoId: number, cantidad: number) => void;
  quitarProducto: (productoId: number) => void;
  sincronizarCatalogo: (productos: ProductoResponse[]) => void;
  actualizarEntrega: (entrega: EntregaBorrador) => void;
  vaciarCarrito: () => void;
}

function limitarCantidad(cantidad: number, stock: number): number {
  return Math.min(Math.max(Math.trunc(cantidad), 1), stock);
}

export const useCarrito = create<EstadoCarrito>()(
  persist(
    (set) => ({
      items: [],
      entrega: { fechaEntrega: "", horaEntrega: "", notas: "" },
      agregarProducto: (producto, cantidad = 1) =>
        set((estado) => {
          if (!producto.disponible || producto.stock <= 0) return estado;
          const existente = estado.items.find((item) => item.productoId === producto.id);
          const cantidadNueva = limitarCantidad((existente?.cantidad ?? 0) + cantidad, producto.stock);
          const itemActualizado: ItemCarrito = {
            productoId: producto.id,
            nombre: producto.nombre,
            precio: producto.precio,
            stock: producto.stock,
            cantidad: cantidadNueva,
          };
          return {
            items: existente
              ? estado.items.map((item) =>
                  item.productoId === producto.id ? itemActualizado : item,
                )
              : [...estado.items, itemActualizado],
          };
        }),
      actualizarCantidad: (productoId, cantidad) =>
        set((estado) => {
          if (cantidad <= 0) {
            return { items: estado.items.filter((item) => item.productoId !== productoId) };
          }
          return {
            items: estado.items.map((item) =>
              item.productoId === productoId
                ? { ...item, cantidad: limitarCantidad(cantidad, item.stock) }
                : item,
            ),
          };
        }),
      quitarProducto: (productoId) =>
        set((estado) => ({
          items: estado.items.filter((item) => item.productoId !== productoId),
        })),
      sincronizarCatalogo: (productos) =>
        set((estado) => {
          const productosPorId = new Map(productos.map((producto) => [producto.id, producto]));
          return {
            items: estado.items.flatMap((item) => {
              const producto = productosPorId.get(item.productoId);
              if (!producto?.disponible || producto.stock <= 0) return [];
              return [
                {
                  productoId: producto.id,
                  nombre: producto.nombre,
                  precio: producto.precio,
                  stock: producto.stock,
                  cantidad: limitarCantidad(item.cantidad, producto.stock),
                },
              ];
            }),
          };
        }),
      actualizarEntrega: (entrega) => set({ entrega }),
      vaciarCarrito: () =>
        set({ items: [], entrega: { fechaEntrega: "", horaEntrega: "", notas: "" } }),
    }),
    {
      name: "mrcatfood_carrito",
      storage: createJSONStorage(() => AsyncStorage),
      version: 1,
    },
  ),
);

export function calcularSubtotal(items: ItemCarrito[]): number {
  const subtotal = items.reduce((total, item) => total + item.precio * item.cantidad, 0);
  return Math.round((subtotal + Number.EPSILON) * 100) / 100;
}

export function calcularCantidadTotal(items: ItemCarrito[]): number {
  return items.reduce((total, item) => total + item.cantidad, 0);
}
