package com.unicauca.cfiet.solicitudes.dominio.servicios;

import com.unicauca.cfiet.solicitudes.aplicacion.output.ExcepcionesFormateadorIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.AccionEtapa;
import com.unicauca.cfiet.solicitudes.dominio.modelos.EntregaTransicion;
import com.unicauca.cfiet.solicitudes.dominio.modelos.ResponsableEtapa;
import com.unicauca.cfiet.solicitudes.dominio.modelos.ResultadoTransicion;
import com.unicauca.cfiet.solicitudes.dominio.modelos.RolEtiquetaEtapa;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoProcesoAcademico;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EtapaSolicitudAcademicaConstantes.*;

public class MaquinaEtapas {
    private static final String ETAPA_NUEVA = "(nueva)";
    private static final String OBSERVACION = "una observación";
    private static final String RESOLUCION = "el escaneo de la Resolución";
    private static final String RECIBO = "el recibo de pago";
    private static final String COMPROBANTE = "el comprobante de pago";

    private static final Set<TipoProcesoAcademico> CANCELACIONES =
            EnumSet.of(TipoProcesoAcademico.CANCELACION_MATRICULA, TipoProcesoAcademico.CANCELACION_ASIGNATURA);
    private static final Set<TipoProcesoAcademico> SUPLETORIO = EnumSet.of(TipoProcesoAcademico.EXAMEN_SUPLETORIO);
    private static final Set<TipoProcesoAcademico> TODOS = EnumSet.allOf(TipoProcesoAcademico.class);

    private static final Map<String, ResponsableEtapa> RESPONSABLES = Map.of(
            RADICADA, ResponsableEtapa.FUNCIONARIO,
            EN_REVISION_DECANO, ResponsableEtapa.DECANO,
            APROBADA_POR_DECANO, ResponsableEtapa.FUNCIONARIO,
            RECHAZADA_POR_DECANO, ResponsableEtapa.FUNCIONARIO,
            PENDIENTE_PAGO, ResponsableEtapa.ESTUDIANTE,
            EN_VERIFICACION_PAGO, ResponsableEtapa.FUNCIONARIO,
            APROBADA, ResponsableEtapa.NINGUNO,
            RECHAZADA, ResponsableEtapa.NINGUNO);

    private static final List<Transicion> TRANSICIONES = List.of(
            new Transicion(TODOS, null, AccionEtapa.RADICAR, RADICADA, RolEtiquetaEtapa.ESTUDIANTE, false, false, false, false),
            new Transicion(CANCELACIONES, RADICADA, AccionEtapa.RECHAZAR_FUNCIONARIO, RECHAZADA, RolEtiquetaEtapa.FUNCIONARIO, true, true, false, false),
            new Transicion(SUPLETORIO, RADICADA, AccionEtapa.RECHAZAR_FUNCIONARIO, RECHAZADA, RolEtiquetaEtapa.FUNCIONARIO, true, false, false, false),
            new Transicion(TODOS, RADICADA, AccionEtapa.REMITIR_DECANO, EN_REVISION_DECANO, RolEtiquetaEtapa.FUNCIONARIO, false, false, false, false),
            new Transicion(TODOS, EN_REVISION_DECANO, AccionEtapa.APROBAR_DECANO, APROBADA_POR_DECANO, RolEtiquetaEtapa.DECANO, false, false, false, false),
            new Transicion(TODOS, EN_REVISION_DECANO, AccionEtapa.RECHAZAR_DECANO, RECHAZADA_POR_DECANO, RolEtiquetaEtapa.DECANO, true, false, false, false),
            new Transicion(CANCELACIONES, APROBADA_POR_DECANO, AccionEtapa.ENVIAR_RESPUESTA, APROBADA, RolEtiquetaEtapa.FUNCIONARIO, false, true, false, false),
            new Transicion(CANCELACIONES, RECHAZADA_POR_DECANO, AccionEtapa.ENVIAR_RESPUESTA, RECHAZADA, RolEtiquetaEtapa.FUNCIONARIO, false, true, false, false),
            new Transicion(SUPLETORIO, RECHAZADA_POR_DECANO, AccionEtapa.ENVIAR_RESPUESTA, RECHAZADA, RolEtiquetaEtapa.FUNCIONARIO, false, false, false, false),
            new Transicion(SUPLETORIO, APROBADA_POR_DECANO, AccionEtapa.ENVIAR_RECIBO, PENDIENTE_PAGO, RolEtiquetaEtapa.FUNCIONARIO, false, false, true, false),
            new Transicion(SUPLETORIO, PENDIENTE_PAGO, AccionEtapa.SUBIR_COMPROBANTE, EN_VERIFICACION_PAGO, RolEtiquetaEtapa.ESTUDIANTE, false, false, false, true),
            new Transicion(SUPLETORIO, EN_VERIFICACION_PAGO, AccionEtapa.APROBAR_COMPROBANTE, APROBADA, RolEtiquetaEtapa.FUNCIONARIO, false, false, false, false),
            new Transicion(SUPLETORIO, EN_VERIFICACION_PAGO, AccionEtapa.RECHAZAR_COMPROBANTE, RECHAZADA, RolEtiquetaEtapa.FUNCIONARIO, true, false, false, false));

    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;

    public MaquinaEtapas(ExcepcionesFormateadorIntPuerto formateadorExcepciones) {
        this.formateadorExcepciones = formateadorExcepciones;
    }

    public ResultadoTransicion siguienteEtapa(TipoProcesoAcademico tipo, String etapaActual, AccionEtapa accion, RolEtiquetaEtapa rol) {
        if (tipo == null || accion == null || rol == null)
            formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.DATOS_TRANSICION_INCOMPLETOS);

        String origen = etapaActual == null || etapaActual.isBlank() ? null : etapaActual.trim();
        if (origen != null && esFinal(origen))
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.ETAPA_FINAL_SIN_ACCIONES, origen));

        Transicion transicion = TRANSICIONES.stream()
                .filter(t -> t.procesos().contains(tipo) && Objects.equals(t.origen(), origen) && t.accion() == accion)
                .findFirst()
                .orElse(null);
        if (transicion == null)
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(
                    MensajesError.TRANSICION_NO_PERMITIDA, accion, nombreEtapa(origen), tipo.getNombre()));
        if (transicion.rol() != rol)
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(
                    MensajesError.ROL_TRANSICION_NO_PERMITIDO, rol, accion, nombreEtapa(origen), transicion.rol()));

        return ResultadoTransicion.builder()
                .etapaSiguiente(transicion.destino())
                .observacionObligatoria(transicion.observacion())
                .resolucionObligatoria(transicion.resolucion())
                .reciboObligatorio(transicion.recibo())
                .comprobanteObligatorio(transicion.comprobante())
                .build();
    }

    public ResultadoTransicion transicionar(TipoProcesoAcademico tipo, String etapaActual, AccionEtapa accion,
                                           RolEtiquetaEtapa rol, EntregaTransicion entrega) {
        ResultadoTransicion resultado = siguienteEtapa(tipo, etapaActual, accion, rol);
        EntregaTransicion recibida = entrega == null ? new EntregaTransicion() : entrega;
        List<String> faltantes = new ArrayList<>();
        if (resultado.isObservacionObligatoria() && (recibida.getObservacion() == null || recibida.getObservacion().isBlank()))
            faltantes.add(OBSERVACION);
        if (resultado.isResolucionObligatoria() && !recibida.isResolucion())
            faltantes.add(RESOLUCION);
        if (resultado.isReciboObligatorio() && !recibida.isRecibo())
            faltantes.add(RECIBO);
        if (resultado.isComprobanteObligatorio() && !recibida.isComprobante())
            faltantes.add(COMPROBANTE);
        if (!faltantes.isEmpty())
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(
                    MensajesError.REQUISITO_TRANSICION_FALTANTE, accion, String.join(" y ", faltantes)));
        return resultado;
    }

    public ResponsableEtapa responsableActual(String codigoEtapa) {
        ResponsableEtapa responsable = codigoEtapa == null ? null : RESPONSABLES.get(codigoEtapa.trim());
        if (responsable == null)
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.ETAPA_NO_VALIDA, codigoEtapa));
        return responsable;
    }

    public boolean esFinal(String codigoEtapa) {
        return responsableActual(codigoEtapa) == ResponsableEtapa.NINGUNO;
    }

    private String nombreEtapa(String etapa) {
        return etapa == null ? ETAPA_NUEVA : etapa;
    }

    private record Transicion(Set<TipoProcesoAcademico> procesos, String origen, AccionEtapa accion, String destino,
                              RolEtiquetaEtapa rol, boolean observacion, boolean resolucion, boolean recibo, boolean comprobante) {
    }
}
