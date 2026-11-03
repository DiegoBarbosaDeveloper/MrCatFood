package com.diegodev.mrcatfood.config;

import com.diegodev.mrcatfood.comun.RespuestaProblema;
import com.diegodev.mrcatfood.seguridad.FiltroJwt;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/** Cadena de seguridad stateless con JWT y roles. */
@Configuration
@EnableWebSecurity
public class SeguridadConfig {

    @Bean
    SecurityFilterChain cadenaSeguridad(HttpSecurity http, FiltroJwt filtroJwt) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        autorizacion ->
                                autorizacion
                                        .requestMatchers("/api/auth/registro", "/api/auth/login")
                                        .permitAll()
                                        .requestMatchers(
                                                HttpMethod.GET,
                                                "/api/productos/**",
                                                "/api/configuracion/horario")
                                        .permitAll()
                                        .requestMatchers("/api/admin/**")
                                        .hasRole("ADMIN")
                                        .requestMatchers("/api/**")
                                        .authenticated()
                                        .anyRequest()
                                        .permitAll())
                .addFilterBefore(filtroJwt, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(
                        manejo ->
                                manejo.authenticationEntryPoint(
                                                (peticion, respuesta, excepcion) ->
                                                        RespuestaProblema.escribir(
                                                                respuesta,
                                                                401,
                                                                "NO_AUTENTICADO",
                                                                "No autenticado",
                                                                "Se requiere un token válido",
                                                                peticion.getRequestURI()))
                                        .accessDeniedHandler(
                                                (peticion, respuesta, excepcion) ->
                                                        RespuestaProblema.escribir(
                                                                respuesta,
                                                                403,
                                                                "ACCESO_DENEGADO",
                                                                "Acceso denegado",
                                                                "No tienes permisos para esta operación",
                                                                peticion.getRequestURI())));
        return http.build();
    }

    @Bean
    PasswordEncoder codificadorContrasenas() {
        return new BCryptPasswordEncoder(12);
    }
}
