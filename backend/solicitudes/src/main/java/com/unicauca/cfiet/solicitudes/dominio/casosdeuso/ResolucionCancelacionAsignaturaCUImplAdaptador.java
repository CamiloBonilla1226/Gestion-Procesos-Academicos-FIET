package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.ConsultaCancelacionAsignaturaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.RemisionConsejoCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.ConsultaSolicitudAcademicaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.ResolucionCancelacionAsignaturaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.ExcepcionesFormateadorIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.GeneradorResolucionIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.SituacionAcademicaAsignaturaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EtapaSolicitudAcademicaConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.dominio.servicios.RedaccionResolucion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class ResolucionCancelacionAsignaturaCUImplAdaptador implements ResolucionCancelacionAsignaturaCUIntPuerto {
    private static final String SOLICITUD_ACADEMICA = "Solicitud académica";
    private static final String TIPO_PDF = "application/pdf";
    private static final String NO_REGISTRA = "No registra";
    private static final String SIN_NUMERO = "____";
    private static final int MAXIMO_TEXTO = 20000;
    private static final int MAXIMO_CAMPO = 300;
    private static final Set<String> ETAPAS_PERMITIDAS = EtapaSolicitudAcademicaConstantes.CON_DECISION;
    private static final Set<String> ACCIONES_DECANO = Set.of(AccionEtapa.APROBAR_DECANO.name(), AccionEtapa.RECHAZAR_DECANO.name(),
            AccionEtapa.APROBAR_CONSEJO.name(), AccionEtapa.NO_APROBAR_CONSEJO.name());

    private final ConsultaCancelacionAsignaturaCUIntPuerto consultaAsignaturaCU;
    private final ConsultaSolicitudAcademicaCUIntPuerto consultaCU;
    private final SituacionAcademicaAsignaturaGatewayIntPuerto situacionGateway;
    private final GeneradorResolucionIntPuerto generador;
    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;
    private final Clock reloj;
    private final String prefijo;
    private final String firmante;
    private final String reviso;
    private final RemisionConsejoCUIntPuerto remisionConsejoCU;

    public ResolucionCancelacionAsignaturaCUImplAdaptador(ConsultaCancelacionAsignaturaCUIntPuerto consultaAsignaturaCU,
                                                          ConsultaSolicitudAcademicaCUIntPuerto consultaCU,
                                                          SituacionAcademicaAsignaturaGatewayIntPuerto situacionGateway,
                                                          GeneradorResolucionIntPuerto generador,
                                                          ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                          Clock reloj,
                                                          String prefijo,
                                                          String firmante,
                                                          String reviso,
                                                          RemisionConsejoCUIntPuerto remisionConsejoCU) {
        this.consultaAsignaturaCU = consultaAsignaturaCU;
        this.consultaCU = consultaCU;
        this.situacionGateway = situacionGateway;
        this.generador = generador;
        this.formateadorExcepciones = formateadorExcepciones;
        this.reloj = reloj;
        this.prefijo = prefijo == null ? "" : prefijo;
        this.firmante = firmante == null ? "" : firmante;
        this.reviso = reviso == null ? "" : reviso;
        this.remisionConsejoCU = remisionConsejoCU;
    }

    @Override
    public List<BorradorResolucion> getBorradores(String uuidSolicitudAcademica, List<AjusteAsignatura> ajustes, String token) {
        DetalleCancelacionAsignatura cancelacion = cancelacionDelFuncionario(uuidSolicitudAcademica, token);
        SolicitudAcademica solicitud = cancelacion.getDetalle().getSolicitudAcademica();
        boolean rechazadaCompleta = EtapaSolicitudAcademicaConstantes.NO_APROBADAS_CON_DECISION.contains(solicitud.getEtapa().getCodigo());
        Map<String, AjusteAsignatura> porAsignatura = new HashMap<>();
        if (ajustes != null)
            ajustes.stream().filter(ajuste -> ajuste != null && ajuste.getUuidAsignaturaSolicitud() != null)
                    .forEach(ajuste -> porAsignatura.put(ajuste.getUuidAsignaturaSolicitud(), ajuste));
        Map<String, String> situaciones = new HashMap<>();
        for (SituacionAcademicaAsignatura situacion : situacionGateway.getTodas())
            if (situacion != null && situacion.getUuidSituacionAcademica() != null && situacion.getNombre() != null)
                situaciones.put(situacion.getUuidSituacionAcademica(), situacion.getNombre());
        String observacionDecano = observacionDelDecano(uuidSolicitudAcademica, token);
        LocalDate hoy = LocalDate.now(reloj);
        String proyecto = funcionarioDe(solicitud);
        SesionConsejo sesion = sesionDe(uuidSolicitudAcademica, solicitud.getEtapa().getCodigo());
        List<BorradorResolucion> borradores = new ArrayList<>();
        for (AsignaturaSolicitudAcademica fila : cancelacion.getAsignaturas()) {
            boolean aceptada = !rechazadaCompleta && Boolean.TRUE.equals(fila.getAprobadaPorDecano());
            Datos datos = datos(solicitud, cancelacion, fila);
            BorradorResolucion borrador = BorradorResolucion.builder()
                    .uuidAsignaturaSolicitud(fila.getUuidAsignaturaSolicitud())
                    .asignatura(datos.asignatura())
                    .aceptada(aceptada)
                    .prefijo(prefijo)
                    .numero(sesion == null || sesion.getConsecutivo() == null ? "" : sesion.getConsecutivo())
                    .fecha(hoy)
                    .asunto(aceptada ? RedaccionResolucion.ASUNTO_ACEPTA : RedaccionResolucion.ASUNTO_NO_ACEPTA)
                    .preambulo(sesion == null ? RedaccionResolucion.PREAMBULO : RedaccionResolucion.PREAMBULO_CONSEJO)
                    .considerandos(aceptada ? considerandosAcepta(datos, fila, porAsignatura.get(fila.getUuidAsignaturaSolicitud()), situaciones, sesion)
                            : considerandosNoAcepta(datos, observacionDecano, sesion))
                    .articuloPrimero(aceptada ? articuloPrimeroAcepta(datos) : articuloPrimeroNoAcepta(datos))
                    .filas(aceptada ? List.of(filaDe(datos, fila, porAsignatura.get(fila.getUuidAsignaturaSolicitud()), situaciones)) : List.of())
                    .articulosSiguientes(aceptada ? articulosAcepta(datos, sesion) : articulosNoAcepta(datos, sesion))
                    .firmante(firmante)
                    .cargoFirmante(RedaccionResolucion.CARGO_FIRMANTE)
                    .diligencia(RedaccionResolucion.diligencia(hoy))
                    .proyecto(proyecto)
                    .reviso(reviso)
                    .build();
            borradores.add(borrador);
        }
        return borradores;
    }

    @Override
    public ArchivoAdjunto generarPdf(String uuidSolicitudAcademica, BorradorResolucion borrador, String token) {
        DetalleCancelacionAsignatura cancelacion = cancelacionDelFuncionario(uuidSolicitudAcademica, token);
        SolicitudAcademica solicitud = cancelacion.getDetalle().getSolicitudAcademica();
        if (borrador == null)
            formateadorExcepciones.lanzarMalFormato(MensajesError.RESOLUCION_BORRADOR_REQUERIDO);
        AsignaturaSolicitudAcademica fila = cancelacion.getAsignaturas().stream()
                .filter(asignatura -> asignatura.getUuidAsignaturaSolicitud().equals(borrador.getUuidAsignaturaSolicitud()))
                .findFirst()
                .orElse(null);
        if (fila == null)
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.ASIGNATURA_AJENA,
                    borrador.getUuidAsignaturaSolicitud(), solicitud.getRadicado()));
        validarLargos(borrador);
        borrador.setPrefijo(prefijo);
        if (borrador.getFecha() == null)
            borrador.setFecha(LocalDate.now(reloj));
        if (borrador.getFilas() == null)
            borrador.setFilas(List.of());
        byte[] contenido = null;
        try {
            contenido = generador.generar(borrador);
        } catch (RuntimeException error) {
            formateadorExcepciones.lanzarErrorGenerico(String.format(MensajesError.RESOLUCION_NO_GENERADA, solicitud.getRadicado()));
        }
        String numero = tieneTexto(borrador.getNumero()) ? borrador.getNumero().trim().replaceAll("[^0-9A-Za-z-]", "") : SIN_NUMERO;
        String nombre = "Resolucion-" + solicitud.getRadicado() + "-" + (numero.isEmpty() ? SIN_NUMERO : numero) + ".pdf";
        return ArchivoAdjunto.builder().nombreOriginal(nombre).tipoContenido(TIPO_PDF).contenido(contenido).build();
    }

    private DetalleCancelacionAsignatura cancelacionDelFuncionario(String uuidSolicitudAcademica, String token) {
        ActorSolicitud actor = consultaCU.resolverActor(uuidSolicitudAcademica, token);
        if (actor.getRol() != RolEtiquetaEtapa.FUNCIONARIO)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, SOLICITUD_ACADEMICA, uuidSolicitudAcademica));
        DetalleCancelacionAsignatura cancelacion = consultaAsignaturaCU.getDetalle(uuidSolicitudAcademica, token);
        SolicitudAcademica solicitud = cancelacion.getDetalle().getSolicitudAcademica();
        String etapa = solicitud.getEtapa() == null ? null : solicitud.getEtapa().getCodigo();
        if (!ETAPAS_PERMITIDAS.contains(etapa))
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.RESOLUCION_GENERADA_ETAPA, solicitud.getRadicado()));
        return cancelacion;
    }

    private void validarLargos(BorradorResolucion borrador) {
        Map<String, String> obligatorios = Map.of("El asunto", valor(borrador.getAsunto()), "El preámbulo", valor(borrador.getPreambulo()),
                "Los considerandos", valor(borrador.getConsiderandos()), "El artículo primero", valor(borrador.getArticuloPrimero()),
                "Los demás artículos", valor(borrador.getArticulosSiguientes()), "El nombre de quien firma", valor(borrador.getFirmante()),
                "El cargo de quien firma", valor(borrador.getCargoFirmante()));
        obligatorios.forEach((campo, texto) -> {
            if (texto.isEmpty())
                formateadorExcepciones.lanzarMalFormato(String.format(MensajesError.DATO_RESOLUCION_REQUERIDO, campo));
        });
        if (!valor(borrador.getNumero()).matches("[0-9A-Za-z-]*"))
            formateadorExcepciones.lanzarMalFormato(MensajesError.NUMERO_RESOLUCION_INVALIDO);
        if (borrador.getFecha() != null && (borrador.getFecha().getYear() < 2000 || borrador.getFecha().getYear() > 2099))
            formateadorExcepciones.lanzarMalFormato(MensajesError.FECHA_RESOLUCION_INVALIDA);
        Map<String, String> textos = Map.of("El preámbulo", valor(borrador.getPreambulo()), "Los considerandos", valor(borrador.getConsiderandos()),
                "El artículo primero", valor(borrador.getArticuloPrimero()), "Los demás artículos", valor(borrador.getArticulosSiguientes()),
                "La diligencia de notificación", valor(borrador.getDiligencia()));
        textos.forEach((campo, texto) -> {
            if (texto.length() > MAXIMO_TEXTO)
                formateadorExcepciones.lanzarMalFormato(String.format(MensajesError.DATO_RESOLUCION_MUY_LARGO, campo, MAXIMO_TEXTO));
        });
        Map<String, String> campos = Map.of("El número", valor(borrador.getNumero()), "El asunto", valor(borrador.getAsunto()),
                "El firmante", valor(borrador.getFirmante()), "El cargo del firmante", valor(borrador.getCargoFirmante()),
                "Quien proyectó", valor(borrador.getProyecto()), "Quien revisó", valor(borrador.getReviso()));
        campos.forEach((campo, texto) -> {
            if (texto.length() > MAXIMO_CAMPO)
                formateadorExcepciones.lanzarMalFormato(String.format(MensajesError.DATO_RESOLUCION_MUY_LARGO, campo, MAXIMO_CAMPO));
        });
    }

    private Datos datos(SolicitudAcademica solicitud, DetalleCancelacionAsignatura cancelacion, AsignaturaSolicitudAcademica fila) {
        Estudiante estudiante = solicitud.getEstudiante();
        Usuario usuario = estudiante == null ? null : estudiante.getUsuario();
        Asignatura asignatura = fila.getAsignaturaMatriculada() == null ? null : fila.getAsignaturaMatriculada().getAsignatura();
        LocalDate fechaSolicitud = solicitud.getFechaCreacion() == null ? null : solicitud.getFechaCreacion().toLocalDate();
        return new Datos(
                nombreCompleto(usuario),
                usuario == null ? "" : valor(usuario.getNumeroDocumento()),
                estudiante == null ? "" : valor(estudiante.getCodigoEstudiantil()),
                estudiante == null || estudiante.getProgramaAcademico() == null ? "" : valor(estudiante.getProgramaAcademico().getNombre()),
                usuario == null ? "" : valor(usuario.getCorreoElectronico()),
                asignatura == null ? "" : valor(asignatura.getNombreAsignatura()),
                RedaccionResolucion.fechaCorta(fechaSolicitud),
                RedaccionResolucion.periodo(fechaSolicitud),
                motivo(cancelacion.getMotivoCancelacion()));
    }

    private SesionConsejo sesionDe(String uuidSolicitudAcademica, String etapa) {
        if (!EtapaSolicitudAcademicaConstantes.APROBADA_POR_CONSEJO.equals(etapa) && !EtapaSolicitudAcademicaConstantes.NO_APROBADA_POR_CONSEJO.equals(etapa))
            return null;
        SesionConsejo sesion = remisionConsejoCU == null ? null : remisionConsejoCU.getSesion(uuidSolicitudAcademica);
        return sesion == null ? new SesionConsejo() : sesion;
    }

    private String enSesion(SesionConsejo sesion) {
        String texto = "";
        if (tieneTexto(sesion.getFecha()))
            texto += " en la sesión del " + sesion.getFecha();
        if (tieneTexto(sesion.getNumeroActa()))
            texto += ", según consta en el Acta N° " + sesion.getNumeroActa();
        return texto.isEmpty() ? "" : texto + ",";
    }

    private String considerandosAcepta(Datos datos, AsignaturaSolicitudAcademica fila, AjusteAsignatura ajuste, Map<String, String> situaciones,
                                       SesionConsejo sesion) {
        String cancelar = situacion(fila.getSituacionCancelar(), ajuste == null ? null : ajuste.getUuidSituacionCancelar(), situaciones);
        String como = cancelar == null ? "" : ", " + comoTexto(cancelar);
        return RedaccionResolucion.CONSIDERANDOS_COMUNES + "\n\n"
                + "6. El estudiante del Programa " + datos.programa() + ", **" + datos.nombre() + "**, identificado con Cédula de Ciudadanía N° "
                + datos.cedula() + " y código estudiantil N° " + datos.codigo() + ", solicitó el día " + datos.fechaSolicitud()
                + " la cancelación de la(s) asignatura(s) **" + datos.asignatura() + "** matriculada en el " + datos.periodo() + ", argumentando: \""
                + datos.motivo() + "\".\n\n"
                + "De conformidad con los artículos antes mencionados y atendiendo la solicitud del estudiante, "
                + (sesion == null ? "el Decano de la Facultad de Ingeniería Electrónica y Telecomunicaciones"
                        : RedaccionResolucion.CONSEJO + enSesion(sesion))
                + " decide Aceptar la petición de cancelación de la asignatura" + como + ".";
    }

    private String considerandosNoAcepta(Datos datos, String observacionDecano, SesionConsejo sesion) {
        String razones = tieneTexto(observacionDecano) ? observacionDecano.trim()
                : sesion != null ? "Revisada la solicitud de cancelación del estudiante, " + RedaccionResolucion.CONSEJO + enSesion(sesion)
                        + " decidió no aprobar la cancelación de la(s) asignatura(s) **" + datos.asignatura() + "**."
                : "Revisada la solicitud de cancelación del estudiante, [escriba aquí las razones por las que no se acepta la cancelación].";
        return RedaccionResolucion.CONSIDERANDOS_COMUNES + "\n\n" + RedaccionResolucion.CONSIDERANDO_MATRICULA + "\n\n"
                + "7. El estudiante del Programa " + datos.programa() + ", **" + datos.nombre() + "**, identificado con cédula de ciudadanía N° "
                + datos.cedula() + " y código estudiantil N° " + datos.codigo() + ", el día " + datos.fechaSolicitud()
                + ", solicitó la cancelación de la(s) asignatura(s) **" + datos.asignatura() + "**, matriculada(s) en el " + datos.periodo()
                + ", argumentando: \"" + datos.motivo() + "\".\n\n"
                + "8. " + razones;
    }

    private String articuloPrimeroAcepta(Datos datos) {
        return "**ARTÍCULO PRIMERO:** Aceptar la cancelación de la(s) asignatura(s) **" + datos.asignatura() + "**, solicitada por el estudiante, **"
                + datos.nombre() + "**, identificado con Cédula de Ciudadanía N° " + datos.cedula() + " y código estudiantil N° " + datos.codigo()
                + ", matriculado en el programa " + datos.programa() + ", correspondiente al " + datos.periodo() + ", teniendo en cuenta el número "
                + "de faltas y el estado de la cancelación de cada una de las asignaturas que se enlista a continuación:";
    }

    private String articuloPrimeroNoAcepta(Datos datos) {
        return "**ARTÍCULO PRIMERO:** NO Aceptar la cancelación de la(s) asignatura(s) **" + datos.asignatura() + "** del Programa " + datos.programa()
                + ", solicitada por el estudiante **" + datos.nombre() + "** identificado con cédula de ciudadanía N° " + datos.cedula()
                + " y código estudiantil N° " + datos.codigo() + ", correspondiente al " + datos.periodo() + ".";
    }

    private String articulosAcepta(Datos datos, SesionConsejo sesion) {
        return "**ARTÍCULO SEGUNDO:** Informar al estudiante que teniendo en cuenta lo dispuesto en el artículo 12 del Reglamento Estudiantil, "
                + "deberá matricular obligatoriamente en el siguiente periodo académico la o las asignaturas que se le cancelen como perdida y demás "
                + "que haya perdido: \"12. Los alumnos que tuvieren asignaturas pendientes por cursar o repetir, de períodos de carreras anteriores\".\n\n"
                + notificacion("a " + datos.correo(), "TERCERO", sesion) + "\n\n"
                + "**ARTÍCULO CUARTO:** Enviar la presente resolución a la División de Admisiones, Registro y Control Académico – DARCA, para que "
                + "sea registrado en la historia académica del estudiante.";
    }

    private String articulosNoAcepta(Datos datos, SesionConsejo sesion) {
        return notificacion("al estudiante **" + datos.nombre() + "**", "SEGUNDO", sesion) + "\n\n"
                + "**ARTÍCULO TERCERO:** Enviar copia de esta resolución a la División de Admisiones, Registro y Control Académico – DARCA, para "
                + "que sea registrado en la hoja de vida académica del estudiante.";
    }

    private String notificacion(String destinatario, String ordinal, SesionConsejo sesion) {
        return "**ARTÍCULO " + ordinal + ":** Notificar personalmente o por aviso mediante correo electrónico " + destinatario
                + ", del contenido de la presente resolución, advirtiéndole que contra el presente Acto Administrativo procede el recurso de "
                + (sesion == null ? "reposición ante el Decano de la Facultad de Ingeniería Electrónica y Telecomunicaciones y de apelación ante el Consejo de "
                        + "Facultad" : "reposición ante " + RedaccionResolucion.CONSEJO) + ", el cual deberá ser interpuesto en la diligencia de notificación o dentro de los diez (10) días hábiles siguientes a "
                + "la notificación personal o por aviso.";
    }

    private FilaResolucion filaDe(Datos datos, AsignaturaSolicitudAcademica fila, AjusteAsignatura ajuste, Map<String, String> situaciones) {
        return FilaResolucion.builder()
                .asignatura(datos.asignatura())
                .faltas(tieneTexto(fila.getNumeroFaltas()) ? fila.getNumeroFaltas().trim() : NO_REGISTRA)
                .nota(tieneTexto(fila.getNota()) ? fila.getNota().trim() : NO_REGISTRA)
                .estadoAlSolicitar(valor(situacion(fila.getSituacionMatricula(), ajuste == null ? null : ajuste.getUuidSituacionMatricula(), situaciones)))
                .estadoCancelacion(valor(situacion(fila.getSituacionCancelar(), ajuste == null ? null : ajuste.getUuidSituacionCancelar(), situaciones)))
                .estadoNuevaMatricula(valor(situacion(fila.getSituacionNuevaMatricula(),
                        ajuste == null ? null : ajuste.getUuidSituacionNuevaMatricula(), situaciones)))
                .build();
    }

    private String situacion(SituacionAcademicaAsignatura guardada, String uuidAjuste, Map<String, String> situaciones) {
        if (tieneTexto(uuidAjuste) && situaciones.containsKey(uuidAjuste))
            return situaciones.get(uuidAjuste);
        return guardada == null ? null : guardada.getNombre();
    }

    private String comoTexto(String situacionCancelar) {
        String texto = situacionCancelar.trim().toLowerCase();
        return texto.startsWith("como ") ? texto : "como " + texto;
    }

    private String observacionDelDecano(String uuidSolicitudAcademica, String token) {
        List<HistorialSolicitudAcademica> historial = consultaCU.getHistorial(uuidSolicitudAcademica, token);
        if (historial == null) return null;
        String ultima = null;
        for (HistorialSolicitudAcademica fila : historial)
            if (fila != null && ACCIONES_DECANO.contains(fila.getAccion()))
                ultima = fila.getObservaciones();
        return ultima;
    }

    private String funcionarioDe(SolicitudAcademica solicitud) {
        TipoSolicitudAcademica tipo = solicitud.getTipoSolicitudAcademica();
        FuncionarioAcademico funcionario = tipo == null ? null : tipo.getFuncionarioAcademico();
        return funcionario == null ? "" : nombreCompleto(funcionario.getUsuario());
    }

    private String motivo(String motivo) {
        String texto = valor(motivo).replaceAll("\\s+", " ");
        while (texto.endsWith(".")) texto = texto.substring(0, texto.length() - 1).trim();
        return texto;
    }

    private String nombreCompleto(Usuario usuario) {
        if (usuario == null) return "";
        return (valor(usuario.getNombres()) + " " + valor(usuario.getApellidos())).trim();
    }

    private String valor(String texto) {
        return texto == null ? "" : texto.trim();
    }

    private boolean tieneTexto(String valor) {
        return valor != null && !valor.isBlank();
    }

    private record Datos(String nombre, String cedula, String codigo, String programa, String correo, String asignatura,
                         String fechaSolicitud, String periodo, String motivo) {
    }
}
