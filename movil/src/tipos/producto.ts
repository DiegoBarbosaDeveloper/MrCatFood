export interface ProductoResponse {
  id: number;
  nombre: string;
  descripcion: string;
  ingredientes: string[];
  precio: number;
  stock: number;
  activo: boolean;
  disponible: boolean;
}
