package com.diegodev.mrcatfood.usuario;

/** Respuesta pública de un usuario: nunca incluye el hash de la contraseña. */
public record UsuarioResponse(
        Long id,
        String nombre,
        String email,
        String telefono,
        Rol rol,
        String conjuntoResidencial,
        String apartamento) {

    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getTelefono(),
                usuario.getRol(),
                usuario.getConjuntoResidencial(),
                usuario.getApartamento());
    }
}
