import {
  esquemaAcceso,
  esquemaEntrega,
  esquemaRegistro,
  esquemaUbicacion,
} from "../src/validacion/esquemasFormularios";

describe("esquemaAcceso", () => {
  it("acepta un correo y una contraseña válidos", () => {
    expect(
      esquemaAcceso.safeParse({ email: "ana@example.com", password: "clave1234" }).success,
    ).toBe(true);
  });

  it("rechaza un correo inválido y una contraseña corta", () => {
    const resultado = esquemaAcceso.safeParse({ email: "no-correo", password: "corta" });

    expect(resultado.success).toBe(false);
    if (!resultado.success) {
      const mensajes = resultado.error.issues.map((issue) => issue.message);
      expect(mensajes).toContain("Correo inválido");
      expect(mensajes).toContain("Mínimo 8 caracteres");
    }
  });
});

describe("esquemaRegistro", () => {
  const valido = {
    nombre: "Ana Pérez",
    email: "ana@example.com",
    telefono: "3001234567",
    password: "clave1234",
  };

  it("acepta un registro válido", () => {
    expect(esquemaRegistro.safeParse(valido).success).toBe(true);
  });

  it("rechaza un nombre corto, un teléfono con letras y una contraseña corta", () => {
    const resultado = esquemaRegistro.safeParse({
      ...valido,
      nombre: "A",
      telefono: "300abc4567",
      password: "corta",
    });

    expect(resultado.success).toBe(false);
    if (!resultado.success) {
      const mensajes = resultado.error.issues.map((issue) => issue.message);
      expect(mensajes).toContain("Nombre muy corto");
      expect(mensajes).toContain("Entre 7 y 15 dígitos");
      expect(mensajes).toContain("Mínimo 8 caracteres");
    }
  });
});

describe("esquemaUbicacion", () => {
  it("recorta los espacios exteriores antes de validar", () => {
    const resultado = esquemaUbicacion.parse({
      conjuntoResidencial: "  Los Pinos  ",
      apartamento: "  4B  ",
    });

    expect(resultado).toEqual({ conjuntoResidencial: "Los Pinos", apartamento: "4B" });
  });

  it("rechaza campos vacíos o demasiado largos", () => {
    expect(
      esquemaUbicacion.safeParse({ conjuntoResidencial: "   ", apartamento: "4B" }).success,
    ).toBe(false);
    expect(
      esquemaUbicacion.safeParse({
        conjuntoResidencial: "a".repeat(121),
        apartamento: "4B",
      }).success,
    ).toBe(false);
  });
});

describe("esquemaEntrega", () => {
  const valido = {
    fechaEntrega: "2026-11-17",
    horaEntrega: "12:30",
    notas: "Sin cebolla",
  };

  it("acepta fecha, hora y notas válidas", () => {
    expect(esquemaEntrega.safeParse(valido).success).toBe(true);
  });

  it("rechaza una hora fuera del formato de 24 horas", () => {
    expect(esquemaEntrega.safeParse({ ...valido, horaEntrega: "25:00" }).success).toBe(false);
    expect(esquemaEntrega.safeParse({ ...valido, horaEntrega: "12:60" }).success).toBe(false);
  });

  it("rechaza una fecha fuera del formato AAAA-MM-DD y notas demasiado largas", () => {
    expect(esquemaEntrega.safeParse({ ...valido, fechaEntrega: "17/11/2026" }).success).toBe(false);
    expect(esquemaEntrega.safeParse({ ...valido, notas: "a".repeat(501) }).success).toBe(false);
  });
});
