package com.diegodev.mrcatfood.usuario;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Datos de registro. No incluye rol: el registro público siempre crea un {@code CLIENTE}; si el
 * cuerpo trae {@code rol} se rechaza (prueba de escalada de privilegios, M2).
 */
@JsonIgnoreProperties(ignoreUnknown = false)
public record RegistroRequest(
        @NotBlank String nombre,
        @NotBlank @Email String email,
        @NotBlank @Pattern(regexp = "^[0-9]{7,15}$", message = "debe tener entre 7 y 15 dígitos")
                String telefono,
        @NotBlank @Size(min = 8, message = "debe tener al menos 8 caracteres") String password,
        @jakarta.validation.constraints.Null String rol) {}
