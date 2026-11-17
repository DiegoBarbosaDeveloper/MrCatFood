package com.diegodev.mrcatfood.seguridad;

import com.diegodev.mrcatfood.usuario.Rol;
import com.diegodev.mrcatfood.usuario.Usuario;
import com.diegodev.mrcatfood.usuario.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Crea la cuenta única de la administradora al arrancar, si no existe. Es idempotente: si el correo
 * ya está registrado, no duplica ni sobrescribe la contraseña (HU-03, M2.6).
 */
@Component
public class CreadorCuentaAdministradora implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CreadorCuentaAdministradora.class);

    private final UsuarioRepository usuarios;
    private final PasswordEncoder codificador;
    private final String correoAdmin;
    private final String contrasenaAdmin;

    public CreadorCuentaAdministradora(
            UsuarioRepository usuarios,
            PasswordEncoder codificador,
            @Value("${ADMIN_EMAIL}") String correoAdmin,
            @Value("${ADMIN_PASSWORD}") String contrasenaAdmin) {
        this.usuarios = usuarios;
        this.codificador = codificador;
        this.correoAdmin = correoAdmin;
        this.contrasenaAdmin = contrasenaAdmin;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String correo = Usuario.normalizarEmail(correoAdmin);
        if (correo == null || correo.isBlank()) {
            throw new IllegalStateException("ADMIN_EMAIL es obligatorio");
        }
        if (contrasenaAdmin == null || contrasenaAdmin.isBlank()) {
            throw new IllegalStateException("ADMIN_PASSWORD es obligatorio");
        }

        if (usuarios.existsByEmailIgnoreCase(correo)) {
            log.info("La cuenta de administradora ya existe; no se modifica");
            return;
        }

        var administradora =
                new Usuario(
                        "Administradora",
                        correo,
                        "",
                        codificador.encode(contrasenaAdmin),
                        Rol.ADMIN);
        usuarios.save(administradora);
        log.info("Cuenta de administradora creada para {}", correo);
    }
}
