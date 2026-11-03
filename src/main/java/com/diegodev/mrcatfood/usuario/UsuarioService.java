package com.diegodev.mrcatfood.usuario;

import com.diegodev.mrcatfood.comun.ApiException;
import com.diegodev.mrcatfood.comun.CodigoError;
import com.diegodev.mrcatfood.seguridad.ServicioJwt;
import com.diegodev.mrcatfood.seguridad.TokenEmitido;
import com.diegodev.mrcatfood.seguridad.UsuarioAutenticado;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder codificador;
    private final ServicioJwt jwt;

    public UsuarioService(
            UsuarioRepository usuarios, PasswordEncoder codificador, ServicioJwt jwt) {
        this.usuarios = usuarios;
        this.codificador = codificador;
        this.jwt = jwt;
    }

    @Transactional
    public TokenEmitido registrar(RegistroRequest solicitud) {
        String correo = Usuario.normalizarEmail(solicitud.email());
        if (usuarios.existsByEmailIgnoreCase(correo)) {
            throw new ApiException(CodigoError.CORREO_EN_USO, "El correo ya está registrado");
        }
        var usuario =
                new Usuario(
                        solicitud.nombre().trim(),
                        correo,
                        solicitud.telefono().trim(),
                        codificador.encode(solicitud.password()),
                        Rol.CLIENTE);
        var guardado = usuarios.save(usuario);
        return jwt.emitir(
                new UsuarioAutenticado(guardado.getId(), guardado.getEmail(), Rol.CLIENTE));
    }

    public TokenEmitido autenticar(LoginRequest solicitud) {
        var usuario =
                usuarios.findByEmailIgnoreCase(Usuario.normalizarEmail(solicitud.email()))
                        .orElseThrow(
                                () ->
                                        new ApiException(
                                                CodigoError.CREDENCIALES_INVALIDAS,
                                                "Credenciales inválidas"));

        if (!codificador.matches(solicitud.password(), usuario.getPasswordHash())
                || !usuario.isActivo()) {
            throw new ApiException(CodigoError.CREDENCIALES_INVALIDAS, "Credenciales inválidas");
        }
        return jwt.emitir(
                new UsuarioAutenticado(usuario.getId(), usuario.getEmail(), usuario.getRol()));
    }
}
