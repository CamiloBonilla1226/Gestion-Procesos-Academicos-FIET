package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.ExcepcionesFormateadorIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.IJwtServicio;
import com.unicauca.cfiet.solicitudes.aplicacion.output.LogGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Log;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Usuario;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.estructura.CodigoError;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Implementación de la interfaz de los casos de uso para gestión de logs.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
public class LogCUImplAdaptador implements LogCUIntPuerto {
    private final LogGatewayIntPuerto gateway;
    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;
    private final IJwtServicio jwtServicio;
    /*Constantes*/
    private static final String LOGS = "logs";
    private static final String USUARIO = "Usuario";
    private static final String USERNAME = "username";

    public LogCUImplAdaptador(LogGatewayIntPuerto gateway, ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                              IJwtServicio jwtServicio){
        this.gateway = gateway;
        this.formateadorExcepciones = formateadorExcepciones;
        this.jwtServicio = jwtServicio;
    }

    @Override
    public void crearLog(String accion, String resultado, String token) {
        String username = jwtServicio.getUsername(token);
        if(username == null || username.isBlank())
            formateadorExcepciones.lanzarErrorGenerico(MensajesError.USERNAME_TOKEN);

        Usuario usuario = gateway.getUsuarioUsername(username);
        if(usuario == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA_FILTRO, USUARIO, USERNAME, username));

        LocalDateTime ahora = LocalDateTime.now();
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String fecha = ahora.format(formato);

        Log logCreado = gateway.crearLog(
            Log.builder()
                .uuidLog(UUID.randomUUID().toString())
                .fecha(fecha)
                .objUsuarioLog(usuario)
                .accion(accion)
                .resultado(resultado)
                .build()
        );

        if(logCreado == null)
            formateadorExcepciones.lanzarErrorGenerico(CodigoError.ERROR_GENERICO.getMensaje());
    }

    @Override
    public void crearLogSesion(String accion, String resultado, String username) {
        Usuario usuario = gateway.getUsuarioUsername(username);
        if(usuario == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA_FILTRO, USUARIO, USERNAME, username));

        LocalDateTime ahora = LocalDateTime.now();
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String fecha = ahora.format(formato);

        Log logCreado = gateway.crearLog(
                Log.builder()
                        .uuidLog(UUID.randomUUID().toString())
                        .fecha(fecha)
                        .objUsuarioLog(usuario)
                        .accion(accion)
                        .resultado(resultado)
                        .build()
        );

        if(logCreado == null)
            formateadorExcepciones.lanzarErrorGenerico(CodigoError.ERROR_GENERICO.getMensaje());
    }

    @Override
    public PaginacionRespuestaDTO<Log> getLogs(int pagina, int tamanio) {
        if (pagina < 0 || tamanio < 0)
            formateadorExcepciones.lanzarMalFormato(MensajesError.PAGINACION_ERROR);
        PaginacionRespuestaDTO<Log> respuesta = gateway.getLogs(pagina, tamanio);
        if (respuesta.getContent().isEmpty())
            formateadorExcepciones.lanzarSinInformacion(String.format(MensajesError.SIN_REGISTROS, LOGS));
        return respuesta;
    }

    @Override
    public PaginacionRespuestaDTO<Log> getLogs(String responsable, String fecha, int pagina, int tamanio){
        if(pagina < 0 || tamanio < 0)
            formateadorExcepciones.lanzarMalFormato(MensajesError.PAGINACION_ERROR);
        return gateway.getLogs(responsable, fecha, pagina, tamanio);
    }

    @Override
    public List<Log> getLogs() {
        List<Log> logs = gateway.getLogs();
        if(logs.isEmpty())
            formateadorExcepciones.lanzarSinInformacion(String.format(MensajesError.SIN_REGISTROS, LOGS));
        return  logs;
    }
}
