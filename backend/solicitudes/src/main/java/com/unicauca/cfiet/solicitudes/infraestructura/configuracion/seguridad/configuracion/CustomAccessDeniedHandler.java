package com.unicauca.cfiet.solicitudes.infraestructura.configuracion.seguridad.configuracion;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.estructura.Error;

/**
 * Manejador personalizado para denegación de acceso en Spring Security.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        Error error = Error.builder()
                .codigoError("403")
                .mensaje("No tiene permisos para acceder a este recurso")
                .httpCodigo(HttpServletResponse.SC_FORBIDDEN)
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
