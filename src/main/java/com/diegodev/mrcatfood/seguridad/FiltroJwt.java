package com.diegodev.mrcatfood.seguridad;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Extrae el token del encabezado {@code Authorization: Bearer ...} y, si es válido, deja la
 * identidad autenticada en el contexto de seguridad. Un token inválido simplemente no autentica: la
 * cadena de filtros responde 401 a través del punto de entrada configurado.
 */
@Component
public class FiltroJwt extends OncePerRequestFilter {

    private final ServicioJwt servicioJwt;

    public FiltroJwt(ServicioJwt servicioJwt) {
        this.servicioJwt = servicioJwt;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest peticion, HttpServletResponse respuesta, FilterChain cadena)
            throws ServletException, IOException {
        String encabezado = peticion.getHeader("Authorization");
        if (encabezado != null && encabezado.startsWith("Bearer ")) {
            String token = encabezado.substring("Bearer ".length()).trim();
            try {
                UsuarioAutenticado principal = servicioJwt.validar(token);
                var autoridad = new SimpleGrantedAuthority("ROLE_" + principal.rol().name());
                var autenticacion =
                        new UsernamePasswordAuthenticationToken(
                                principal, null, List.of(autoridad));
                SecurityContextHolder.getContext().setAuthentication(autenticacion);
            } catch (TokenInvalidoException ignorada) {
                SecurityContextHolder.clearContext();
            }
        }
        cadena.doFilter(peticion, respuesta);
    }
}
