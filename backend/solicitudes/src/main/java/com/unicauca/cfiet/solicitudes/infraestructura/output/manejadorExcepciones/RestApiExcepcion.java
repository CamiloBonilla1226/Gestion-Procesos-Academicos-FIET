package com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones;

import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.estructura.CodigoError;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.estructura.UtilidadesError;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.*;
import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.estructura.Error;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 *  Interceptador global de excepciones
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@ControllerAdvice
public class RestApiExcepcion {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Error> manejarExcepcionGenerica(final HttpServletRequest req,
                                                        final Exception ex, final Locale locale) {
        final Error error = UtilidadesError
                .crearError(CodigoError.ERROR_GENERICO.getCodigo(),
                        ex.getMessage(),
                        HttpStatus.INTERNAL_SERVER_ERROR.value())
                .setUrl(req.getRequestURL().toString()).setMetodo(req.getMethod());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ErrorEntidadNoExisteExcepcion.class)
    public ResponseEntity<Error> manejarEntidadNoExiste(final HttpServletRequest req,
                                                        final Exception ex, final Locale locale) {
        final Error error = UtilidadesError
                .crearError(CodigoError.ERROR_ENTIDAD_NO_EXISTE.getCodigo(),
                        ex.getMessage(),
                        HttpStatus.INTERNAL_SERVER_ERROR.value())
                .setUrl(req.getRequestURL().toString()).setMetodo(req.getMethod());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ErrorEntidadExisteExcepcion.class)
    public ResponseEntity<Error> manejarEntidadExiste(final HttpServletRequest req,
                                                        final Exception ex, final Locale locale) {
        final Error error = UtilidadesError
                .crearError(CodigoError.ERROR_ENTIDAD_EXISTE.getCodigo(),
                        ex.getMessage(),
                        HttpStatus.INTERNAL_SERVER_ERROR.value())
                .setUrl(req.getRequestURL().toString()).setMetodo(req.getMethod());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ErrorReglaNegocioVioladaExcepcion.class)
    public ResponseEntity<Error> manejarReglaNegocioViolada(final HttpServletRequest req,
                                                      final Exception ex, final Locale locale) {
        final Error error = UtilidadesError
                .crearError(CodigoError.ERROR_REGLA_DE_NEGOCIO_VIOLADA.getCodigo(),
                        ex.getMessage(),
                        HttpStatus.INTERNAL_SERVER_ERROR.value())
                .setUrl(req.getRequestURL().toString()).setMetodo(req.getMethod());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Error> manejarCredencialesIncorrectas(final HttpServletRequest req, final Exception ex, final Locale locale) {
        final Error error = UtilidadesError
                .crearError(CodigoError.ERROR_CREDENCIALES.getCodigo(),
                        CodigoError.ERROR_CREDENCIALES.getMensaje(),
                        HttpStatus.INTERNAL_SERVER_ERROR.value())
                .setUrl(req.getRequestURL().toString()).setMetodo(req.getMethod());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Error> manejarNoAutenticacion(final HttpServletRequest req, final Exception ex, final Locale locale) {
        final Error error = UtilidadesError
                .crearError(CodigoError.ERROR_SIN_ACCESO.getCodigo(),
                        CodigoError.ERROR_SIN_ACCESO.getMensaje(),
                        HttpStatus.FORBIDDEN.value())
                .setUrl(req.getRequestURL().toString()).setMetodo(req.getMethod());
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Error> manejarNoAAcceso(final HttpServletRequest req, final Exception ex, final Locale locale) {
        final Error error = UtilidadesError
                .crearError(CodigoError.ERROR_SIN_ACCESO.getCodigo(),
                        CodigoError.ERROR_SIN_ACCESO.getMensaje(),
                        HttpStatus.FORBIDDEN.value())
                .setUrl(req.getRequestURL().toString()).setMetodo(req.getMethod());
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ErrorMalFormatoExcepcion.class)
    public ResponseEntity<Error> manejarMalFormato(final HttpServletRequest req,
                                                                final Exception ex, final Locale locale) {
        final Error error = UtilidadesError
                .crearError(CodigoError.ERROR_MAL_FORMATO.getCodigo(),
                        ex.getMessage(),
                        HttpStatus.INTERNAL_SERVER_ERROR.value())
                .setUrl(req.getRequestURL().toString()).setMetodo(req.getMethod());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ErrorNoInformacionExcepcion.class)
    public ResponseEntity<Error> manejarSinInformacion(final HttpServletRequest req,
                                                   final Exception ex, final Locale locale) {
        final Error error = UtilidadesError
                .crearError(CodigoError.ERROR_NO_INFORMACION.getCodigo(),
                        ex.getMessage(),
                        HttpStatus.INTERNAL_SERVER_ERROR.value())
                .setUrl(req.getRequestURL().toString()).setMetodo(req.getMethod());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ErrorSinAccesoExcepcion.class)
    public ResponseEntity<Error> manejarSinAcceso(final HttpServletRequest req,
                                                       final Exception ex, final Locale locale) {
        final Error error = UtilidadesError
                .crearError(CodigoError.ERROR_SIN_ACCESO.getCodigo(),
                        ex.getMessage(),
                        HttpStatus.INTERNAL_SERVER_ERROR.value())
                .setUrl(req.getRequestURL().toString()).setMetodo(req.getMethod());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ErrorTokenExpiradoExcepcion.class)
    public ResponseEntity<Error> manejarTokenExpirado(final HttpServletRequest req, final Exception ex, final Locale locale) {
        final Error error = UtilidadesError
                .crearError(CodigoError.ERROR_TOKEN_EXPIRADO.getCodigo(),
                        ex.getMessage(),
                        HttpStatus.INTERNAL_SERVER_ERROR.value())
                .setUrl(req.getRequestURL().toString()).setMetodo(req.getMethod());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String campo = ((FieldError) error).getField();
            String mensajeDeError = error.getDefaultMessage();
            errores.put(campo, mensajeDeError);
        });

        return new ResponseEntity<Map<String, String>>(errores, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<String> handleConstraintViolationException(ConstraintViolationException e) {
        return new ResponseEntity<>(e.getMessage(),
                HttpStatus.BAD_REQUEST);
    }
}
