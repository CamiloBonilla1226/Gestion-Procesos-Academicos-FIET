package com.unicauca.cfiet.solicitudes.infraestructura.configuracion.seguridad.configuracion;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.estructura.CodigoError;
import io.jsonwebtoken.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.estructura.Error;

/**
 * Punto de entrada personalizado para manejar intentos de acceso no autenticado.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        Error error = Error.builder()
                .codigoError("401")
                .mensaje(CodigoError.ERROR_SIN_ACCESO.getMensaje())
                .httpCodigo(HttpServletResponse.SC_UNAUTHORIZED)
                .url(request.getRequestURL().toString())
                .metodo(request.getMethod())
                .build();

        ObjectMapper mapper = new ObjectMapper();
        try {
            response.getWriter().write(mapper.writeValueAsString(error));
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }
}

