package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.EliminacionRespuestasCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.AlmacenamientoAnexosIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.EliminacionRespuestasGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.ExcepcionesFormateadorIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.IJwtServicio;
import com.unicauca.cfiet.solicitudes.aplicacion.output.SesionGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.ApplicationConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.EstadoNotificacionResolucion;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Usuario;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EtapaSolicitudAcademicaConstantes.APROBADA;
import static com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EtapaSolicitudAcademicaConstantes.RECHAZADA;

public class EliminacionRespuestasCUImplAdaptador implements EliminacionRespuestasCUIntPuerto {
    private static final Set<String> ETAPAS_FINALES = Set.of(APROBADA, RECHAZADA);
    private static final Set<EstadoNotificacionResolucion> COMUNICACION_PENDIENTE = Set.of(EstadoNotificacionResolucion.PROGRAMADA,
            EstadoNotificacionResolucion.EN_PROCESO, EstadoNotificacionResolucion.FALLIDA);
    private static final String USUARIO = "Usuario";
    private static final String USERNAME = "username";
    private static final String FECHA_INICIAL = "La fecha inicial";
    private static final String FECHA_FINAL = "La fecha final";

    private final EliminacionRespuestasGatewayIntPuerto gateway;
    private final SesionGatewayIntPuerto sesionGateway;
    private final IJwtServicio jwtServicio;
    private final AlmacenamientoAnexosIntPuerto almacenamiento;
    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;
    private final LogCUIntPuerto log;
    private final Clock reloj;

    public EliminacionRespuestasCUImplAdaptador(EliminacionRespuestasGatewayIntPuerto gateway,
                                                SesionGatewayIntPuerto sesionGateway,
                                                IJwtServicio jwtServicio,
                                                AlmacenamientoAnexosIntPuerto almacenamiento,
                                                ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                LogCUIntPuerto log,
                                                Clock reloj) {
        this.gateway = gateway;
        this.sesionGateway = sesionGateway;
        this.jwtServicio = jwtServicio;
        this.almacenamiento = almacenamiento;
        this.formateadorExcepciones = formateadorExcepciones;
        this.log = log;
        this.reloj = reloj;
    }

    @Override
    public int contarRespuestas(LocalDate desde, LocalDate hasta, String token) {
        return eliminables(desde, hasta, token).size();
    }

    @Override
    public int eliminarRespuestas(LocalDate desde, LocalDate hasta, String token) {
        List<String> uuids = eliminables(desde, hasta, token);
        if (uuids.isEmpty())
            return 0;
        gateway.eliminar(uuids).forEach(almacenamiento::eliminarTrasConfirmar);
        log.crearLog("Eliminar respuestas de solicitudes académicas",
                String.format("%d solicitudes académicas respondidas entre %s y %s eliminadas con sus anexos, historial y notificaciones",
                        uuids.size(), desde, hasta),
                token);
        return uuids.size();
    }

    private List<String> eliminables(LocalDate desde, LocalDate hasta, String token) {
        validarRango(desde, hasta);
        Usuario funcionario = usuarioDe(token);
        if (!tieneRol(funcionario, ApplicationConstantes.FUNCIONARIO_ACADEMICO_ROL))
            return List.of();
        return gateway.getRespuestasEliminables(funcionario.getUuidUsuario(), ETAPAS_FINALES, COMUNICACION_PENDIENTE,
                desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay());
    }

    private void validarRango(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null)
            formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.RANGO_FECHAS_REQUERIDO);
        LocalDate hoy = LocalDate.now(reloj);
        if (desde.isAfter(hoy))
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.RANGO_FECHAS_FUTURO, FECHA_INICIAL, desde, hoy));
        if (hasta.isAfter(hoy))
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.RANGO_FECHAS_FUTURO, FECHA_FINAL, hasta, hoy));
        if (desde.isAfter(hasta))
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.RANGO_FECHAS_INVERTIDO, desde, hasta));
    }

    private Usuario usuarioDe(String token) {
        String username = jwtServicio.getUsername(token);
        if (username == null || username.isBlank())
            formateadorExcepciones.lanzarErrorGenerico(MensajesError.USERNAME_TOKEN);
        Usuario usuario = sesionGateway.getUsuario(username);
        if (usuario == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA_FILTRO, USUARIO, USERNAME, username));
        return usuario;
    }

    private boolean tieneRol(Usuario usuario, String rol) {
        return usuario.getRoles() != null && usuario.getRoles().stream().anyMatch(actual -> rol.equals(actual.getNombre()));
    }
}
