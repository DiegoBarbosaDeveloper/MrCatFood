package com.diegodev.mrcatfood.seguridad;

import com.diegodev.mrcatfood.usuario.Rol;

/** Identidad que viaja en el token y que el resto de la aplicación considera autenticada. */
public record UsuarioAutenticado(Long id, String email, Rol rol) {}
