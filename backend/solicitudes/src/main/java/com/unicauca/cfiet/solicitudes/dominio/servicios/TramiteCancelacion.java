package com.unicauca.cfiet.solicitudes.dominio.servicios;

import com.unicauca.cfiet.solicitudes.aplicacion.output.*;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EstadoAsignaturaMatriculadaConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;

public class TramiteCancelacion {
    private static final String SITUACION_ACADEMICA = "Situación académica de asignatura";
    private static final String NUMERO_FALTAS = "el número de faltas";
    private static final String NOTA = "la nota";
    private static final BigDecimal NOTA_MINIMA = BigDecimal.ZERO;
    private static final BigDecimal NOTA_MAXIMA = new BigDecimal("5.0");
    private static final int MAXIMO_VALOR_ASIGNATURA = 20;
    private static final Pattern NUMERO = Pattern.compile("[+-]?\\d+([.,]\\d+)?");

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

    public String faltasDe(String numeroFaltas, String asignatura) {
        String texto = valorAsignatura(numeroFaltas, NUMERO_FALTAS, asignatura);
        BigDecimal faltas = numero(texto);
        if (faltas == null)
            return texto;
        if (faltas.signum() < 0 || faltas.stripTrailingZeros().scale() > 0)
            formateadorExcepciones.lanzarMalFormato(String.format(MensajesError.NUMERO_FALTAS_NO_VALIDO, asignatura));
        return faltas.toBigInteger().toString();
    }

    public String notaDe(String nota, String asignatura) {
        String texto = valorAsignatura(nota, NOTA, asignatura);
        BigDecimal valor = numero(texto);
        if (valor == null)
            return texto;
        if (valor.compareTo(NOTA_MINIMA) < 0 || valor.compareTo(NOTA_MAXIMA) > 0 || valor.stripTrailingZeros().scale() > 1)
            formateadorExcepciones.lanzarMalFormato(String.format(MensajesError.NOTA_NO_VALIDA, asignatura));
        return valor.setScale(1, RoundingMode.UNNECESSARY).toPlainString();
    }

    public BigDecimal numero(String valor) {
        if (valor == null || !NUMERO.matcher(valor.trim()).matches())
            return null;
        return new BigDecimal(valor.trim().replace(',', '.'));
    }

    private String valorAsignatura(String valor, String dato, String asignatura) {
        if (!tieneTexto(valor))
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.DATO_ASIGNATURA_FALTANTE, dato, asignatura));
        String texto = valor.trim();
        if (texto.length() > MAXIMO_VALOR_ASIGNATURA)
            formateadorExcepciones.lanzarMalFormato(String.format(MensajesError.VALOR_ASIGNATURA_MUY_LARGO, asignatura, dato,
                    MAXIMO_VALOR_ASIGNATURA));
        return texto;
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
