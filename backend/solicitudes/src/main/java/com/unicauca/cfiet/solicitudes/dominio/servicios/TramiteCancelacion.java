package com.unicauca.cfiet.solicitudes.dominio.servicios;

import com.unicauca.cfiet.solicitudes.aplicacion.output.*;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EstadoAsignaturaMatriculadaConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;

public class TramiteCancelacion {
    private static final String SITUACION_ACADEMICA = "Situación académica de asignatura";
    private static final String NUMERO_FALTAS = "el número de faltas";
    private static final String NOTA = "la nota";
    private static final BigDecimal NOTA_MINIMA = BigDecimal.ZERO;
    private static final BigDecimal NOTA_MAXIMA = new BigDecimal("5.0");

    private final SolicitudCancelacionMatriculaGatewayIntPuerto asignaturaGateway;
    private final SituacionAcademicaAsignaturaGatewayIntPuerto situacionGateway;
    private final TramiteSolicitud tramiteSolicitud;
    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;

    public TramiteCancelacion(SolicitudCancelacionMatriculaGatewayIntPuerto asignaturaGateway,
                              SolicitudAcademicaGatewayIntPuerto solicitudGateway,
                              SituacionAcademicaAsignaturaGatewayIntPuerto situacionGateway,
                              UsuarioGatewayIntPuerto usuarioGateway,
                              SesionGatewayIntPuerto sesionGateway,
                              IJwtServicio jwtServicio,
                              MaquinaEtapas maquinaEtapas,
                              ExcepcionesFormateadorIntPuerto formateadorExcepciones) {
        this.asignaturaGateway = asignaturaGateway;
        this.situacionGateway = situacionGateway;
        this.tramiteSolicitud = new TramiteSolicitud(solicitudGateway, usuarioGateway, sesionGateway, jwtServicio, maquinaEtapas,
                formateadorExcepciones);
        this.formateadorExcepciones = formateadorExcepciones;
    }

    public ActorSolicitud actorDe(String token, RolEtiquetaEtapa rol) {
        return tramiteSolicitud.actorDe(token, rol);
    }

    public SolicitudAcademica solicitudValidada(TipoProcesoAcademico proceso, String uuidSolicitudAcademica, AccionEtapa accion,
                                                ActorSolicitud actor) {
        return tramiteSolicitud.solicitudValidada(proceso, uuidSolicitudAcademica, accion, actor);
    }

    public Map<String, SituacionAcademicaAsignatura> catalogoSituaciones() {
        Map<String, SituacionAcademicaAsignatura> catalogo = new HashMap<>();
        for (SituacionAcademicaAsignatura situacion : situacionGateway.getTodas())
            catalogo.put(situacion.getUuidSituacionAcademica(), situacion);
        return catalogo;
    }

    public <T> Map<String, T> cobertura(List<AsignaturaSolicitudAcademica> asignaturas, String radicado, List<T> recibidas,
                                        Function<T, String> llave) {
        Map<String, AsignaturaSolicitudAcademica> propias = new LinkedHashMap<>();
        for (AsignaturaSolicitudAcademica asignatura : asignaturas)
            propias.put(asignatura.getUuidAsignaturaSolicitud(), asignatura);

        Map<String, T> porAsignatura = new HashMap<>();
        for (T recibida : recibidas == null ? List.<T>of() : recibidas) {
            String uuid = recibida == null ? null : llave.apply(recibida);
            if (uuid == null || !propias.containsKey(uuid))
                formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.ASIGNATURA_AJENA, uuid, radicado));
            if (porAsignatura.put(uuid, recibida) != null)
                formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.ASIGNATURA_REPETIDA, nombreDe(propias.get(uuid))));
        }
        List<String> faltantes = propias.values().stream()
                .filter(asignatura -> !porAsignatura.containsKey(asignatura.getUuidAsignaturaSolicitud()))
                .map(this::nombreDe)
                .toList();
        if (!faltantes.isEmpty())
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.ASIGNATURAS_SIN_EVALUAR, String.join("; ", faltantes)));
        return porAsignatura;
    }

    public Integer faltasDe(Integer numeroFaltas, String asignatura) {
        if (numeroFaltas == null)
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.DATO_ASIGNATURA_FALTANTE, NUMERO_FALTAS, asignatura));
        if (numeroFaltas < 0)
            formateadorExcepciones.lanzarMalFormato(String.format(MensajesError.NUMERO_FALTAS_NO_VALIDO, asignatura));
        return numeroFaltas;
    }

    public BigDecimal notaDe(BigDecimal nota, String asignatura) {
        if (nota == null)
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.DATO_ASIGNATURA_FALTANTE, NOTA, asignatura));
        if (nota.compareTo(NOTA_MINIMA) < 0 || nota.compareTo(NOTA_MAXIMA) > 0 || nota.stripTrailingZeros().scale() > 1)
            formateadorExcepciones.lanzarMalFormato(String.format(MensajesError.NOTA_NO_VALIDA, asignatura));
        return nota.setScale(1);
    }

    public SituacionAcademicaAsignatura situacionDe(Map<String, SituacionAcademicaAsignatura> catalogo, String uuidSituacion,
                                                    String dato, String asignatura) {
        if (!tieneTexto(uuidSituacion))
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.DATO_ASIGNATURA_FALTANTE, dato, asignatura));
        SituacionAcademicaAsignatura situacion = catalogo.get(uuidSituacion);
        if (situacion == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, SITUACION_ACADEMICA, uuidSituacion));
        return situacion;
    }

    public List<AsignaturaSolicitudAcademica> guardarAsignaturas(List<AsignaturaSolicitudAcademica> asignaturas) {
        return asignaturaGateway.actualizarAsignaturas(asignaturas);
    }

    public int cancelarActivas(List<AsignaturaSolicitudAcademica> asignaturas) {
        List<AsignaturaMatriculada> activas = asignaturas.stream()
                .map(AsignaturaSolicitudAcademica::getAsignaturaMatriculada)
                .filter(matriculada -> EstadoAsignaturaMatriculadaConstantes.ACTIVA.equals(matriculada.getEstado()))
                .toList();
        List<String> matriculadas = activas.stream().map(AsignaturaMatriculada::getUuidAsignaturaMatriculada).toList();
        if (!matriculadas.isEmpty())
            asignaturaGateway.cambiarEstadoAsignaturasMatriculadas(matriculadas, EstadoAsignaturaMatriculadaConstantes.CANCELADA);
        activas.forEach(matriculada -> matriculada.setEstado(EstadoAsignaturaMatriculadaConstantes.CANCELADA));
        return matriculadas.size();
    }

    public String nombreDe(AsignaturaSolicitudAcademica asignatura) {
        AsignaturaMatriculada matriculada = asignatura.getAsignaturaMatriculada();
        if (matriculada == null || matriculada.getAsignatura() == null)
            return asignatura.getUuidAsignaturaSolicitud();
        return matriculada.getAsignatura().getNombreAsignatura();
    }

    public String textoAcotado(String valor, int maximo, String dato, String asignatura) {
        if (!tieneTexto(valor)) return null;
        String texto = valor.trim();
        if (texto.length() > maximo)
            formateadorExcepciones.lanzarMalFormato(String.format(MensajesError.OBSERVACION_ASIGNATURA_MUY_LARGA, dato, asignatura, maximo));
        return texto;
    }

    public boolean tieneTexto(String valor) {
        return tramiteSolicitud.tieneTexto(valor);
    }
}
