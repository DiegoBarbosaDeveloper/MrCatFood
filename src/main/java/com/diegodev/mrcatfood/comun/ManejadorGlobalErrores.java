package com.diegodev.mrcatfood.comun;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/** Traduce las excepciones a ProblemDetail con el código del contrato REST. */
@RestControllerAdvice
public class ManejadorGlobalErrores {

    private static final Logger log = LoggerFactory.getLogger(ManejadorGlobalErrores.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ProblemDetail> manejar(
            ApiException excepcion, HttpServletRequest peticion) {
        ProblemDetail problema =
                ProblemDetail.forStatusAndDetail(
                        excepcion.codigo().estado(), excepcion.getMessage());
        problema.setType(URI.create("about:blank"));
        problema.setTitle(excepcion.codigo().titulo());
        problema.setInstance(URI.create(peticion.getRequestURI()));
        problema.setProperty("codigo", excepcion.codigo().codigo());
        if (!excepcion.campos().isEmpty()) {
            problema.setProperty("campos", excepcion.campos());
        }
        return ResponseEntity.status(excepcion.codigo().estado()).body(problema);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> manejarValidacion(
            MethodArgumentNotValidException excepcion, HttpServletRequest peticion) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError error : excepcion.getBindingResult().getFieldErrors()) {
            campos.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        ProblemDetail problema =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_REQUEST, "Los datos enviados no son válidos");
        problema.setType(URI.create("about:blank"));
        problema.setTitle(CodigoError.DATOS_INVALIDOS.titulo());
        problema.setInstance(URI.create(peticion.getRequestURI()));
        problema.setProperty("codigo", CodigoError.DATOS_INVALIDOS.codigo());
        problema.setProperty("campos", campos);
        return ResponseEntity.badRequest().body(problema);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> manejarCuerpoIlegible(
            HttpMessageNotReadableException excepcion, HttpServletRequest peticion) {
        ProblemDetail problema =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_REQUEST, "El cuerpo de la petición no es válido");
        problema.setType(URI.create("about:blank"));
        problema.setTitle(CodigoError.DATOS_INVALIDOS.titulo());
        problema.setInstance(URI.create(peticion.getRequestURI()));
        problema.setProperty("codigo", CodigoError.DATOS_INVALIDOS.codigo());
        return ResponseEntity.badRequest().body(problema);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ProblemDetail> manejarNoEncontrado(
            NoResourceFoundException excepcion, HttpServletRequest peticion) {
        ProblemDetail problema =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.NOT_FOUND, "El recurso solicitado no existe");
        problema.setType(URI.create("about:blank"));
        problema.setTitle(CodigoError.RECURSO_NO_ENCONTRADO.titulo());
        problema.setInstance(URI.create(peticion.getRequestURI()));
        problema.setProperty("codigo", CodigoError.RECURSO_NO_ENCONTRADO.codigo());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problema);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ProblemDetail> manejarEstadoHttp(
            ResponseStatusException excepcion, HttpServletRequest peticion) {
        HttpStatus estado = HttpStatus.valueOf(excepcion.getStatusCode().value());
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(estado, excepcion.getReason());
        problema.setType(URI.create("about:blank"));
        problema.setTitle(estado.getReasonPhrase());
        problema.setInstance(URI.create(peticion.getRequestURI()));
        problema.setProperty("codigo", CodigoError.RECURSO_NO_ENCONTRADO.codigo());
        return ResponseEntity.status(estado).body(problema);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> manejarInesperado(
            Exception excepcion, HttpServletRequest peticion) {
        log.error("Error no controlado en {}", peticion.getRequestURI(), excepcion);
        ProblemDetail problema =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado");
        problema.setType(URI.create("about:blank"));
        problema.setTitle(CodigoError.ERROR_INTERNO.titulo());
        problema.setInstance(URI.create(peticion.getRequestURI()));
        problema.setProperty("codigo", CodigoError.ERROR_INTERNO.codigo());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problema);
    }
}
