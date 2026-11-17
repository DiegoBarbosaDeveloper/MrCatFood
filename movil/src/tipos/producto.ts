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

export interface ProductoRequest {
  nombre: string;
  descripcion: string;
  ingredientes: string[];
  precio: number;
  stock: number;
}

export interface DisponibilidadRequest {
  activo: boolean;
  stock: number;
}
