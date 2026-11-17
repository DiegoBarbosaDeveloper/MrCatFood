import { z } from "zod";

export const esquemaAcceso = z.object({
  email: z.string().email("Correo inválido"),
  password: z.string().min(8, "Mínimo 8 caracteres"),
});

export const esquemaRegistro = z.object({
  nombre: z.string().min(2, "Nombre muy corto"),
  email: z.string().email("Correo inválido"),
  telefono: z.string().regex(/^[0-9]{7,15}$/, "Entre 7 y 15 dígitos"),
  password: z.string().min(8, "Mínimo 8 caracteres"),
});

export const esquemaUbicacion = z.object({
  conjuntoResidencial: z
    .string()
    .trim()
    .min(1, "El conjunto residencial es obligatorio")
    .max(120, "Debe tener máximo 120 caracteres"),
  apartamento: z
    .string()
    .trim()
    .min(1, "El apartamento es obligatorio")
    .max(60, "Debe tener máximo 60 caracteres"),
});

export const esquemaEntrega = z.object({
  fechaEntrega: z
    .string()
    .trim()
    .regex(/^\d{4}-\d{2}-\d{2}$/, "Usa el formato AAAA-MM-DD")
    .refine((fecha) => !Number.isNaN(new Date(`${fecha}T00:00:00-05:00`).getTime()), {
      message: "Ingresa una fecha válida",
    }),
  horaEntrega: z
    .string()
    .trim()
    .regex(/^([01]\d|2[0-3]):[0-5]\d$/, "Usa el formato HH:mm"),
  notas: z.string().max(500, "Las notas deben tener máximo 500 caracteres"),
});

export type FormularioAcceso = z.infer<typeof esquemaAcceso>;
export type FormularioRegistro = z.infer<typeof esquemaRegistro>;
export type FormularioUbicacion = z.infer<typeof esquemaUbicacion>;
export type FormularioEntrega = z.infer<typeof esquemaEntrega>;
