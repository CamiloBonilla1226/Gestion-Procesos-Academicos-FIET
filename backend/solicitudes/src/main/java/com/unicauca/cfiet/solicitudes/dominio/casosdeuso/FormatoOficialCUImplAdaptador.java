package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.AnexoAcademicoCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.ConsultaCancelacionAsignaturaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.ConsultaCancelacionMatriculaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.ConsultaExamenSupletorioCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.ConsultaSolicitudAcademicaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.FormatoOficialCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.SolicitudAcademicaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.AlmacenamientoAnexosIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.ComposicionFormatoOficialIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.EliminacionRespuestasGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.ExcepcionesFormateadorIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.SituacionAcademicaAsignaturaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.SolicitudAcademicaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.TipoAnexoAcademicoGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.dominio.servicios.MaquinaEtapas;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.unicauca.cfiet.solicitudes.dominio.helper.constantes.AnexoAcademicoConstantes.FORMATO_FIRMADO_ESTUDIANTE;
import static com.unicauca.cfiet.solicitudes.dominio.helper.constantes.AnexoAcademicoConstantes.SOPORTE_JUSTIFICACION;
import static com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EtapaSolicitudAcademicaConstantes.*;

public class FormatoOficialCUImplAdaptador implements FormatoOficialCUIntPuerto {
    private static final String SOLICITUD_ACADEMICA = "Solicitud académica";
    private static final String TIPO_ANEXO_ACADEMICO = "Tipo de anexo académico";
    private static final String FACULTAD = "Ingeniería Electrónica y Telecomunicaciones";
    private static final String NEGADA = "Negada";
    private static final String TIPO_PDF = "application/pdf";
    private static final int AJUSTE_MAXIMO = 15;
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Set<String> ETAPAS_CON_DECISION =
            Set.of(APROBADA_POR_DECANO, RECHAZADA_POR_DECANO, PENDIENTE_PAGO, EN_VERIFICACION_PAGO, APROBADA, RECHAZADA, APROBADA_POR_CONSEJO,
                    NO_APROBADA_POR_CONSEJO);
    private static final Set<String> ACCIONES_DECANO = Set.of(AccionEtapa.APROBAR_DECANO.name(), AccionEtapa.RECHAZAR_DECANO.name(),
            AccionEtapa.APROBAR_CONSEJO.name(), AccionEtapa.NO_APROBAR_CONSEJO.name());

    private final ConsultaSolicitudAcademicaCUIntPuerto consultaCU;
    private final ConsultaCancelacionMatriculaCUIntPuerto consultaMatriculaCU;
    private final ConsultaCancelacionAsignaturaCUIntPuerto consultaAsignaturaCU;
    private final ConsultaExamenSupletorioCUIntPuerto consultaSupletorioCU;
    private final SolicitudAcademicaCUIntPuerto solicitudCU;
    private final AnexoAcademicoCUIntPuerto anexoCU;
    private final SolicitudAcademicaGatewayIntPuerto solicitudGateway;
    private final TipoAnexoAcademicoGatewayIntPuerto tipoAnexoGateway;
    private final SituacionAcademicaAsignaturaGatewayIntPuerto situacionGateway;
    private final EliminacionRespuestasGatewayIntPuerto eliminacionGateway;
    private final AlmacenamientoAnexosIntPuerto almacenamiento;
    private final ComposicionFormatoOficialIntPuerto composicion;
    private final MaquinaEtapas maquinaEtapas;
    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;
    private final LogCUIntPuerto log;

    public FormatoOficialCUImplAdaptador(ConsultaSolicitudAcademicaCUIntPuerto consultaCU,
                                         ConsultaCancelacionMatriculaCUIntPuerto consultaMatriculaCU,
                                         ConsultaCancelacionAsignaturaCUIntPuerto consultaAsignaturaCU,
                                         ConsultaExamenSupletorioCUIntPuerto consultaSupletorioCU,
                                         SolicitudAcademicaCUIntPuerto solicitudCU,
                                         AnexoAcademicoCUIntPuerto anexoCU,
                                         SolicitudAcademicaGatewayIntPuerto solicitudGateway,
                                         TipoAnexoAcademicoGatewayIntPuerto tipoAnexoGateway,
                                         SituacionAcademicaAsignaturaGatewayIntPuerto situacionGateway,
                                         EliminacionRespuestasGatewayIntPuerto eliminacionGateway,
                                         AlmacenamientoAnexosIntPuerto almacenamiento,
                                         ComposicionFormatoOficialIntPuerto composicion,
                                         MaquinaEtapas maquinaEtapas,
                                         ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                         LogCUIntPuerto log) {
        this.consultaCU = consultaCU;
        this.consultaMatriculaCU = consultaMatriculaCU;
        this.consultaAsignaturaCU = consultaAsignaturaCU;
        this.consultaSupletorioCU = consultaSupletorioCU;
        this.solicitudCU = solicitudCU;
        this.anexoCU = anexoCU;
        this.solicitudGateway = solicitudGateway;
        this.tipoAnexoGateway = tipoAnexoGateway;
        this.situacionGateway = situacionGateway;
        this.eliminacionGateway = eliminacionGateway;
        this.almacenamiento = almacenamiento;
        this.composicion = composicion;
        this.maquinaEtapas = maquinaEtapas;
        this.formateadorExcepciones = formateadorExcepciones;
        this.log = log;
    }

    @Override
    public ArchivoAdjunto descargarFormato(String uuidSolicitudAcademica, String token) {
        DetalleSolicitudAcademica detalle = consultaCU.getDetalle(uuidSolicitudAcademica, token);
        DatosFormatoOficial datos = datos(detalle, token, false, List.of());
        return pdf("Formato-" + datos.getRadicado() + "-sin-firmar.pdf", generar(datos));
    }

    @Override
    public SolicitudAcademica radicar(String uuidSolicitudAcademica, ArchivoAdjunto archivo, String token) {
        ActorSolicitud actor = actorConRol(uuidSolicitudAcademica, token, Set.of(RolEtiquetaEtapa.ESTUDIANTE));
        DetalleSolicitudAcademica detalle = consultaCU.getDetalle(uuidSolicitudAcademica, token);
        SolicitudAcademica solicitud = detalle.getSolicitudAcademica();
        maquinaEtapas.siguienteEtapa(procesoDe(solicitud), solicitud.getEtapa().getCodigo(), AccionEtapa.RADICAR, RolEtiquetaEtapa.ESTUDIANTE);
        DatosFormatoOficial datos = datos(detalle, token, false, List.of());

        anexoCU.adjuntarAnexo(uuidSolicitudAcademica, uuidTipoFormato(solicitud, FORMATO_FIRMADO_ESTUDIANTE), archivo, actor, token);
        int paginas = composicion.contarPaginas(archivo.getContenido());
        if (paginas < 0)
            formateadorExcepciones.lanzarMalFormato(MensajesError.FORMATO_NO_LEGIBLE);
        int minimo = composicion.paginasMinimas(datos);
        if (paginas < minimo)
            formateadorExcepciones.lanzarMalFormato(String.format(MensajesError.FORMATO_PAGINAS_INSUFICIENTES, minimo));
        return solicitudCU.cambiarEtapa(uuidSolicitudAcademica, AccionEtapa.RADICAR, actor, null, token);
    }

    @Override
    public void descartar(String uuidSolicitudAcademica, String token) {
        actorConRol(uuidSolicitudAcademica, token, Set.of(RolEtiquetaEtapa.ESTUDIANTE));
        SolicitudAcademica solicitud = consultaCU.getDetalle(uuidSolicitudAcademica, token).getSolicitudAcademica();
        maquinaEtapas.siguienteEtapa(procesoDe(solicitud), solicitud.getEtapa().getCodigo(), AccionEtapa.DESCARTAR, RolEtiquetaEtapa.ESTUDIANTE);
        List<String> rutas = eliminacionGateway.eliminar(List.of(solicitud.getUuidSolicitudAcademica()));
        rutas.forEach(almacenamiento::eliminarTrasConfirmar);
        log.crearLog("Descartar solicitud académica",
                String.format("Solicitud académica %s, acción %s: se borró antes de radicarse", solicitud.getRadicado(), AccionEtapa.DESCARTAR),
                token);
    }

    @Override
    public ArchivoAdjunto componer(String uuidSolicitudAcademica, List<ValorProvisionalFormato> valoresProvisionales, String token) {
        ActorSolicitud actor = actorConRol(uuidSolicitudAcademica, token, Set.of(RolEtiquetaEtapa.FUNCIONARIO, RolEtiquetaEtapa.DECANO));
        DetalleSolicitudAcademica detalle = consultaCU.getDetalle(uuidSolicitudAcademica, token);
        List<ValorProvisionalFormato> provisionales = provisionalesDe(actor.getRol(), valoresProvisionales);
        DatosFormatoOficial datos = datos(detalle, token, true, provisionales);
        byte[] escaneo = detalle.getFormatoFirmadoEstudianteUuid() == null ? null
                : consultaCU.descargarAnexo(uuidSolicitudAcademica, detalle.getFormatoFirmadoEstudianteUuid(), token).getContenido();
        SolicitudAcademica solicitud = detalle.getSolicitudAcademica();
        byte[] contenido = null;
        try {
            contenido = composicion.componer(escaneo, datos, milimetros(solicitud.getAjusteFormatoXmm()), milimetros(solicitud.getAjusteFormatoYmm()));
        } catch (RuntimeException error) {
            formateadorExcepciones.lanzarErrorGenerico(String.format(MensajesError.FORMATO_NO_GENERADO, solicitud.getRadicado()));
        }
        return pdf("Formato-" + solicitud.getRadicado() + "-para-firma.pdf", contenido);
    }

    @Override
    public void guardarAjuste(String uuidSolicitudAcademica, BigDecimal ajusteXmm, BigDecimal ajusteYmm, String token) {
        actorConRol(uuidSolicitudAcademica, token, Set.of(RolEtiquetaEtapa.FUNCIONARIO));
        SolicitudAcademica solicitud = consultaCU.getDetalle(uuidSolicitudAcademica, token).getSolicitudAcademica();
        if (maquinaEtapas.esFinal(solicitud.getEtapa().getCodigo()))
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.AJUSTE_FORMATO_ETAPA_FINAL, solicitud.getRadicado()));
        BigDecimal x = ajusteValido(ajusteXmm, "El ajuste horizontal");
        BigDecimal y = ajusteValido(ajusteYmm, "El ajuste vertical");
        solicitudGateway.guardarAjusteFormato(solicitud.getUuidSolicitudAcademica(), x, y);
        log.crearLog("Ajustar formato oficial",
                String.format("Solicitud académica %s: ajuste del formato X=%s mm, Y=%s mm", solicitud.getRadicado(), x, y), token);
    }

    private List<ValorProvisionalFormato> provisionalesDe(RolEtiquetaEtapa rol, List<ValorProvisionalFormato> valores) {
        if (valores == null) return List.of();
        return valores.stream()
                .filter(valor -> valor != null && valor.getUuidAsignaturaSolicitud() != null)
                .map(valor -> rol == RolEtiquetaEtapa.DECANO
                        ? ValorProvisionalFormato.builder().uuidAsignaturaSolicitud(valor.getUuidAsignaturaSolicitud()).aprobada(valor.getAprobada()).build()
                        : ValorProvisionalFormato.builder().uuidAsignaturaSolicitud(valor.getUuidAsignaturaSolicitud())
                        .uuidSituacionMatricula(valor.getUuidSituacionMatricula()).uuidSituacionCancelar(valor.getUuidSituacionCancelar())
                        .faltas(valor.getFaltas()).nota(valor.getNota()).build())
                .toList();
    }

    private BigDecimal ajusteValido(BigDecimal valor, String nombre) {
        BigDecimal ajuste = valor == null ? BigDecimal.ZERO : valor.setScale(1, RoundingMode.HALF_UP);
        if (ajuste.abs().compareTo(BigDecimal.valueOf(AJUSTE_MAXIMO)) > 0)
            formateadorExcepciones.lanzarMalFormato(String.format(MensajesError.AJUSTE_FORMATO_FUERA_DE_RANGO, nombre, -AJUSTE_MAXIMO, AJUSTE_MAXIMO));
        return ajuste;
    }

    private ActorSolicitud actorConRol(String uuidSolicitudAcademica, String token, Set<RolEtiquetaEtapa> roles) {
        ActorSolicitud actor = consultaCU.resolverActor(uuidSolicitudAcademica, token);
        if (!roles.contains(actor.getRol()))
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, SOLICITUD_ACADEMICA, uuidSolicitudAcademica));
        return actor;
    }

    private DatosFormatoOficial datos(DetalleSolicitudAcademica detalle, String token, boolean conFacultad,
                                      List<ValorProvisionalFormato> provisionales) {
        SolicitudAcademica solicitud = detalle.getSolicitudAcademica();
        Estudiante estudiante = solicitud.getEstudiante();
        Usuario usuario = estudiante == null ? null : estudiante.getUsuario();
        TipoProcesoAcademico proceso = procesoDe(solicitud);
        DatosFormatoOficial datos = DatosFormatoOficial.builder()
                .proceso(proceso)
                .radicado(solicitud.getRadicado())
                .fecha(solicitud.getFechaCreacion() == null ? null : solicitud.getFechaCreacion().toLocalDate())
                .nombre(nombreCompleto(usuario))
                .codigo(estudiante == null ? null : estudiante.getCodigoEstudiantil())
                .programa(estudiante == null || estudiante.getProgramaAcademico() == null ? null : estudiante.getProgramaAcademico().getNombre())
                .facultad(FACULTAD)
                .semestre(estudiante == null ? null : estudiante.getSemestre())
                .cedula(usuario == null ? null : usuario.getNumeroDocumento())
                .correo(usuario == null ? null : usuario.getCorreoElectronico())
                .telefono(usuario == null ? null : usuario.getTelefono())
                .datosFacultad(conFacultad)
                .build();
        String uuid = solicitud.getUuidSolicitudAcademica();
        List<AsignaturaSolicitudAcademica> asignaturas = List.of();
        if (proceso == TipoProcesoAcademico.CANCELACION_MATRICULA) {
            DetalleCancelacionMatricula matricula = consultaMatriculaCU.getDetalle(uuid, token);
            datos.setMotivo(matricula.getMotivoCancelacion());
            asignaturas = matricula.getAsignaturas();
        } else if (proceso == TipoProcesoAcademico.CANCELACION_ASIGNATURA) {
            DetalleCancelacionAsignatura cancelacion = consultaAsignaturaCU.getDetalle(uuid, token);
            datos.setMotivo(cancelacion.getMotivoCancelacion());
            asignaturas = cancelacion.getAsignaturas();
        } else {
            completarSupletorio(datos, consultaSupletorioCU.getDetalle(uuid, token).getSupletorio(), detalle.getAnexos());
        }
        if (conFacultad) {
            datos.setRevisionAcademica(funcionarioDe(solicitud));
            Boolean decision = decision(proceso, solicitud, asignaturas, token);
            boolean provisional = decision == null && decisionProvisionalCompleta(proceso, solicitud, asignaturas, provisionales);
            datos.setDecisionDecano(provisional ? Boolean.valueOf(provisionales.stream().anyMatch(valor -> Boolean.TRUE.equals(valor.getAprobada()))) : decision);
        }
        datos.setAsignaturas(filas(asignaturas, datos.getDecisionDecano() != null, provisionales));
        return datos;
    }

    private boolean decisionProvisionalCompleta(TipoProcesoAcademico proceso, SolicitudAcademica solicitud,
                                                List<AsignaturaSolicitudAcademica> asignaturas, List<ValorProvisionalFormato> provisionales) {
        if (proceso != TipoProcesoAcademico.CANCELACION_ASIGNATURA || !EN_REVISION_DECANO.equals(solicitud.getEtapa().getCodigo())
                || asignaturas.isEmpty()) return false;
        Set<String> decididas = provisionales.stream().filter(valor -> valor.getAprobada() != null)
                .map(ValorProvisionalFormato::getUuidAsignaturaSolicitud).collect(Collectors.toSet());
        return asignaturas.stream().allMatch(fila -> decididas.contains(fila.getUuidAsignaturaSolicitud()));
    }

    private void completarSupletorio(DatosFormatoOficial datos, SolicitudExamenSupletorio supletorio, List<AnexoAcademico> anexos) {
        if (supletorio == null) return;
        Asignatura asignatura = asignaturaDe(supletorio.getAsignaturaMatriculada());
        datos.setAsignatura(asignatura == null ? null : asignatura.getNombreAsignatura());
        datos.setCodigoAsignatura(asignatura == null ? null : asignatura.getCodigoAsignatura());
        datos.setGrupoAsignatura(supletorio.getAsignaturaMatriculada() == null ? null : supletorio.getAsignaturaMatriculada().getGrupo());
        datos.setFechaExamen(supletorio.getFechaExamenNoPresentado() == null ? null : supletorio.getFechaExamenNoPresentado().format(FECHA));
        if (supletorio.getTipoCausa() == CausaSupletorio.OTRA)
            datos.setDocumentos(anexos.stream()
                    .filter(anexo -> anexo.getTipoAnexoAcademico() != null && SOPORTE_JUSTIFICACION.equals(anexo.getTipoAnexoAcademico().getNombre()))
                    .map(AnexoAcademico::getNombreArchivo)
                    .collect(Collectors.joining(", ")));
        CruceSupletorio cruce = supletorio.getCruce();
        if (cruce == null) return;
        Asignatura cruzada = asignaturaDe(cruce.getAsignaturaMatriculadaCruzada());
        datos.setCruceAsignatura(cruzada == null ? null : cruzada.getNombreAsignatura());
        datos.setCruceCodigo(cruzada == null ? null : cruzada.getCodigoAsignatura());
        datos.setCruceGrupo(cruce.getAsignaturaMatriculadaCruzada() == null ? null : cruce.getAsignaturaMatriculadaCruzada().getGrupo());
        datos.setCruceFecha(cruce.getFechaExamenCruzada() == null ? null : cruce.getFechaExamenCruzada().format(FECHA));
        datos.setCruceHora(cruce.getHoraExamenCruzada());
    }

    private List<FilaFormatoOficial> filas(List<AsignaturaSolicitudAcademica> asignaturas, boolean conDecision,
                                           List<ValorProvisionalFormato> provisionales) {
        Map<String, ValorProvisionalFormato> porFila = new HashMap<>();
        provisionales.stream().filter(valor -> valor != null && valor.getUuidAsignaturaSolicitud() != null)
                .forEach(valor -> porFila.put(valor.getUuidAsignaturaSolicitud(), valor));
        Map<String, String> situaciones = porFila.isEmpty() ? Map.of() : situacionGateway.getTodas().stream()
                .collect(Collectors.toMap(SituacionAcademicaAsignatura::getUuidSituacionAcademica, SituacionAcademicaAsignatura::getNombre));
        return asignaturas.stream().map(fila -> {
            Asignatura asignatura = asignaturaDe(fila.getAsignaturaMatriculada());
            ValorProvisionalFormato provisional = porFila.get(fila.getUuidAsignaturaSolicitud());
            String matricula = nombreDe(fila.getSituacionMatricula());
            String cancelar = nombreDe(fila.getSituacionCancelar());
            if (provisional != null && situaciones.containsKey(provisional.getUuidSituacionMatricula()))
                matricula = situaciones.get(provisional.getUuidSituacionMatricula());
            if (provisional != null && situaciones.containsKey(provisional.getUuidSituacionCancelar()))
                cancelar = situaciones.get(provisional.getUuidSituacionCancelar());
            Boolean aprobada = provisional != null && provisional.getAprobada() != null ? provisional.getAprobada() : fila.getAprobadaPorDecano();
            if (conDecision && Boolean.FALSE.equals(aprobada))
                cancelar = NEGADA;
            String faltas = provisional != null && provisional.getFaltas() != null ? textoOVacio(provisional.getFaltas()) : fila.getNumeroFaltas();
            String nota = provisional != null && provisional.getNota() != null ? textoOVacio(provisional.getNota()) : fila.getNota();
            return FilaFormatoOficial.builder()
                    .uuidAsignaturaSolicitud(fila.getUuidAsignaturaSolicitud())
                    .codigo(asignatura == null ? null : asignatura.getCodigoAsignatura())
                    .nombre(asignatura == null ? null : asignatura.getNombreAsignatura())
                    .faltas(faltas)
                    .nota(nota)
                    .situacionMatricula(matricula)
                    .situacionCancelar(cancelar)
                    .build();
        }).toList();
    }

    private Boolean decision(TipoProcesoAcademico proceso, SolicitudAcademica solicitud, List<AsignaturaSolicitudAcademica> asignaturas,
                             String token) {
        if (!ETAPAS_CON_DECISION.contains(solicitud.getEtapa().getCodigo())) return null;
        List<HistorialSolicitudAcademica> historial = consultaCU.getHistorial(solicitud.getUuidSolicitudAcademica(), token);
        String ultima = historial == null ? null : historial.stream()
                .map(HistorialSolicitudAcademica::getAccion)
                .filter(ACCIONES_DECANO::contains)
                .reduce((primera, segunda) -> segunda)
                .orElse(null);
        if (ultima == null) return null;
        if (proceso == TipoProcesoAcademico.CANCELACION_ASIGNATURA)
            return asignaturas.stream().anyMatch(fila -> Boolean.TRUE.equals(fila.getAprobadaPorDecano()));
        return AccionEtapa.APROBAR_DECANO.name().equals(ultima) || AccionEtapa.APROBAR_CONSEJO.name().equals(ultima);
    }

    private String textoOVacio(String valor) {
        String texto = valor.trim();
        return texto.isEmpty() ? null : texto;
    }

    private String uuidTipoFormato(SolicitudAcademica solicitud, String nombre) {
        String uuidTipo = solicitud.getTipoSolicitudAcademica().getUuidTipoSolicitudAcademica();
        return tipoAnexoGateway.getPorTipo(uuidTipo).stream()
                .filter(tipo -> nombre.equals(tipo.getNombre()))
                .map(TipoAnexoAcademico::getUuidTipoAnexoAcademico)
                .findFirst()
                .orElseGet(() -> {
                    formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA_FILTRO,
                            TIPO_ANEXO_ACADEMICO, "nombre", nombre));
                    return null;
                });
    }

    private byte[] generar(DatosFormatoOficial datos) {
        try {
            return composicion.generar(datos);
        } catch (RuntimeException error) {
            formateadorExcepciones.lanzarErrorGenerico(String.format(MensajesError.FORMATO_NO_GENERADO, datos.getRadicado()));
            return null;
        }
    }

    private String funcionarioDe(SolicitudAcademica solicitud) {
        TipoSolicitudAcademica tipo = solicitud.getTipoSolicitudAcademica();
        FuncionarioAcademico funcionario = tipo == null ? null : tipo.getFuncionarioAcademico();
        return funcionario == null ? null : nombreCompleto(funcionario.getUsuario());
    }

    private TipoProcesoAcademico procesoDe(SolicitudAcademica solicitud) {
        TipoProcesoAcademico proceso = TipoProcesoAcademico.porNombre(solicitud.getTipoSolicitudAcademica().getNombre());
        if (proceso == null)
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.TIPO_SOLICITUD_SIN_PROCESO,
                    solicitud.getTipoSolicitudAcademica().getNombre()));
        return proceso;
    }

    private Asignatura asignaturaDe(AsignaturaMatriculada matriculada) {
        return matriculada == null ? null : matriculada.getAsignatura();
    }

    private String nombreDe(SituacionAcademicaAsignatura situacion) {
        return situacion == null ? null : situacion.getNombre();
    }

    private String nombreCompleto(Usuario usuario) {
        if (usuario == null) return null;
        String nombres = usuario.getNombres() == null ? "" : usuario.getNombres().trim();
        String apellidos = usuario.getApellidos() == null ? "" : usuario.getApellidos().trim();
        return (nombres + " " + apellidos).trim();
    }

    private double milimetros(BigDecimal valor) {
        return valor == null ? 0 : valor.doubleValue();
    }

    private ArchivoAdjunto pdf(String nombre, byte[] contenido) {
        return ArchivoAdjunto.builder().nombreOriginal(nombre).tipoContenido(TIPO_PDF).contenido(contenido).build();
    }
}
