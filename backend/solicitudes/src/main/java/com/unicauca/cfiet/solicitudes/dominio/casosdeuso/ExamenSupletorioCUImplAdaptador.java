package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.AnexoAcademicoCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.ExamenSupletorioCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.SolicitudAcademicaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.*;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.ApplicationConstantes;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EstadoAsignaturaMatriculadaConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.dominio.servicios.ValidadorArchivoAdjunto;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

import static com.unicauca.cfiet.solicitudes.dominio.helper.constantes.AnexoAcademicoConstantes.*;

public class ExamenSupletorioCUImplAdaptador implements ExamenSupletorioCUIntPuerto {
    private static final String USUARIO = "Usuario";
    private static final String TIPO_SOLICITUD_ACADEMICA = "Tipo de solicitud académica";
    private static final String TIPO_ANEXO_ACADEMICO = "Tipo de anexo académico";
    private static final String NOMBRE = "nombre";
    public static final int DIAS_HABILES_PLAZO = 3;
    private static final int MAXIMO_HORA = 10;
    private static final Pattern FORMATO_HORA = Pattern.compile("([01]\\d|2[0-3]):[0-5]\\d");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final SolicitudExamenSupletorioGatewayIntPuerto gateway;
    private final EstudianteGatewayIntPuerto estudianteGateway;
    private final UsuarioGatewayIntPuerto usuarioGateway;
    private final TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway;
    private final TipoAnexoAcademicoGatewayIntPuerto tipoAnexoGateway;
    private final SolicitudAcademicaCUIntPuerto solicitudCU;
    private final AnexoAcademicoCUIntPuerto anexoCU;
    private final ValidadorArchivoAdjunto validadorArchivo;
    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;
    private final LogCUIntPuerto log;
    private final Clock reloj;

    public ExamenSupletorioCUImplAdaptador(SolicitudExamenSupletorioGatewayIntPuerto gateway,
                                           EstudianteGatewayIntPuerto estudianteGateway,
                                           UsuarioGatewayIntPuerto usuarioGateway,
                                           TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway,
                                           TipoAnexoAcademicoGatewayIntPuerto tipoAnexoGateway,
                                           SolicitudAcademicaCUIntPuerto solicitudCU,
                                           AnexoAcademicoCUIntPuerto anexoCU,
                                           ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                           LogCUIntPuerto log,
                                           Clock reloj) {
        this.gateway = gateway;
        this.estudianteGateway = estudianteGateway;
        this.usuarioGateway = usuarioGateway;
        this.tipoSolicitudGateway = tipoSolicitudGateway;
        this.tipoAnexoGateway = tipoAnexoGateway;
        this.solicitudCU = solicitudCU;
        this.anexoCU = anexoCU;
        this.validadorArchivo = new ValidadorArchivoAdjunto(formateadorExcepciones);
        this.formateadorExcepciones = formateadorExcepciones;
        this.log = log;
        this.reloj = reloj;
    }

    @Override
    public SolicitudExamenSupletorio radicarExamenSupletorio(String uuidEstudiante, String uuidAsignaturaMatriculada, LocalDate fechaExamenNoPresentado,
                                                             String tipoCausa, String uuidAsignaturaCruzada, LocalDate fechaExamenCruzada,
                                                             String horaExamenCruzada, List<AnexoRadicacion> anexos, String token) {
        validarEstudiante(uuidEstudiante);
        TipoSolicitudAcademica tipo = tipoExamenSupletorio();
        solicitudCU.verificarSinSolicitudEnCurso(uuidEstudiante, tipo);
        Map<String, AsignaturaMatriculada> matriculadas = matriculadasDe(uuidEstudiante);
        if (!tieneTexto(uuidAsignaturaMatriculada))
            formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.ASIGNATURA_SUPLETORIO_REQUERIDA);
        AsignaturaMatriculada asignatura = asignaturaActiva(matriculadas, uuidAsignaturaMatriculada, uuidEstudiante);
        CausaSupletorio causa = causaDe(tipoCausa);
        validarPlazo(fechaExamenNoPresentado);
        CruceSupletorio cruce = causa == CausaSupletorio.CRUCE
                ? cruceDe(matriculadas, asignatura, uuidAsignaturaCruzada, fechaExamenCruzada, horaExamenCruzada, uuidEstudiante)
                : sinDatosDeCruce(uuidAsignaturaCruzada, fechaExamenCruzada, horaExamenCruzada);
        List<AnexoRadicacion> entregados = anexos == null ? List.of() : anexos;
        validarAnexos(entregados, tipo, causa);

        SolicitudAcademica solicitud = solicitudCU.crearSolicitud(uuidEstudiante, tipo.getUuidTipoSolicitudAcademica(), token);
        SolicitudExamenSupletorio guardada = gateway.guardar(SolicitudExamenSupletorio.builder()
                .solicitudAcademica(solicitud)
                .asignaturaMatriculada(asignatura)
                .fechaExamenNoPresentado(fechaExamenNoPresentado)
                .tipoCausa(causa)
                .cruce(cruce)
                .build());

        ActorSolicitud actor = ActorSolicitud.builder().uuidUsuario(uuidEstudiante).rol(RolEtiquetaEtapa.ESTUDIANTE).build();
        for (AnexoRadicacion anexo : entregados)
            anexoCU.adjuntarAnexo(solicitud.getUuidSolicitudAcademica(), anexo.getUuidTipoAnexoAcademico(), anexo.getArchivo(), actor, token);

        log.crearLog("Radicar examen supletorio",
                String.format("Solicitud académica %s, acción RADICAR: examen supletorio de %s por causa %s con %d anexos",
                        solicitud.getRadicado(), nombreDe(asignatura), nombreCausa(causa), entregados.size()),
                token);
        guardada.setSolicitudAcademica(solicitud);
        return guardada;
    }

    private void validarEstudiante(String uuidEstudiante) {
        Usuario usuario = tieneTexto(uuidEstudiante) ? usuarioGateway.getUsuario(uuidEstudiante) : null;
        if (usuario == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, USUARIO, uuidEstudiante));
        boolean esEstudiante = usuario.getRoles() != null && usuario.getRoles().stream()
                .anyMatch(rol -> ApplicationConstantes.ESTUDIANTE_ROL.equals(rol.getNombre()));
        if (!esEstudiante || estudianteGateway.getPorUuid(uuidEstudiante) == null)
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.ACTOR_SIN_ROL, uuidEstudiante, ApplicationConstantes.ESTUDIANTE_ROL));
    }

    private TipoSolicitudAcademica tipoExamenSupletorio() {
        TipoSolicitudAcademica tipo = tipoSolicitudGateway.getTodos().stream()
                .filter(t -> TipoProcesoAcademico.porNombre(t.getNombre()) == TipoProcesoAcademico.EXAMEN_SUPLETORIO)
                .findFirst()
                .orElse(null);
        if (tipo == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA_FILTRO,
                    TIPO_SOLICITUD_ACADEMICA, NOMBRE, TipoProcesoAcademico.EXAMEN_SUPLETORIO.getNombre()));
        return tipo;
    }

    private Map<String, AsignaturaMatriculada> matriculadasDe(String uuidEstudiante) {
        Map<String, AsignaturaMatriculada> matriculadas = new HashMap<>();
        for (AsignaturaMatriculada asignatura : estudianteGateway.getAsignaturasMatriculadas(uuidEstudiante))
            matriculadas.put(asignatura.getUuidAsignaturaMatriculada(), asignatura);
        return matriculadas;
    }

    private AsignaturaMatriculada asignaturaActiva(Map<String, AsignaturaMatriculada> matriculadas, String uuid, String uuidEstudiante) {
        AsignaturaMatriculada asignatura = matriculadas.get(uuid.trim());
        if (asignatura == null)
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.ASIGNATURA_NO_MATRICULADA, uuid, uuidEstudiante));
        if (!EstadoAsignaturaMatriculadaConstantes.ACTIVA.equals(asignatura.getEstado()))
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.ASIGNATURA_SUPLETORIO_NO_ACTIVA, nombreDe(asignatura), asignatura.getEstado()));
        return asignatura;
    }

    private CausaSupletorio causaDe(String tipoCausa) {
        String valor = tipoCausa == null ? "" : tipoCausa.trim();
        for (CausaSupletorio causa : CausaSupletorio.values())
            if (causa.name().equalsIgnoreCase(valor))
                return causa;
        formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.CAUSA_SUPLETORIO_INVALIDA, tipoCausa));
        return null;
    }

    private void validarPlazo(LocalDate fechaExamen) {
        if (fechaExamen == null)
            formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.FECHA_EXAMEN_REQUERIDA);
        LocalDate hoy = LocalDate.now(reloj);
        if (fechaExamen.isAfter(hoy))
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.FECHA_EXAMEN_FUTURA,
                    fechaExamen.format(FORMATO_FECHA), hoy.format(FORMATO_FECHA)));
        if (diasHabilesDespuesDe(fechaExamen, hoy) > DIAS_HABILES_PLAZO)
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.PLAZO_SUPLETORIO_VENCIDO,
                    fechaExamen.format(FORMATO_FECHA), ultimoDiaPermitido(fechaExamen).format(FORMATO_FECHA)));
    }

    private int diasHabilesDespuesDe(LocalDate fechaExamen, LocalDate hasta) {
        int dias = 0;
        for (LocalDate dia = fechaExamen.plusDays(1); !dia.isAfter(hasta); dia = dia.plusDays(1))
            if (esHabil(dia))
                dias++;
        return dias;
    }

    private LocalDate ultimoDiaPermitido(LocalDate fechaExamen) {
        LocalDate dia = fechaExamen;
        int dias = 0;
        while (dias < DIAS_HABILES_PLAZO) {
            dia = dia.plusDays(1);
            if (esHabil(dia))
                dias++;
        }
        return dia;
    }

    private boolean esHabil(LocalDate dia) {
        return dia.getDayOfWeek() != DayOfWeek.SATURDAY && dia.getDayOfWeek() != DayOfWeek.SUNDAY;
    }

    private CruceSupletorio cruceDe(Map<String, AsignaturaMatriculada> matriculadas, AsignaturaMatriculada asignatura, String uuidCruzada,
                                    LocalDate fechaExamenCruzada, String horaExamenCruzada, String uuidEstudiante) {
        if (!tieneTexto(uuidCruzada))
            formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.ASIGNATURA_CRUZADA_REQUERIDA);
        if (asignatura.getUuidAsignaturaMatriculada().equals(uuidCruzada.trim()))
            formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.ASIGNATURA_CRUZADA_IGUAL);
        AsignaturaMatriculada cruzada = asignaturaActiva(matriculadas, uuidCruzada, uuidEstudiante);
        if (fechaExamenCruzada == null || !tieneTexto(horaExamenCruzada))
            formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.DATOS_CRUCE_REQUERIDOS);
        String hora = horaExamenCruzada.trim();
        if (hora.length() > MAXIMO_HORA)
            formateadorExcepciones.lanzarMalFormato(String.format(MensajesError.HORA_CRUCE_MUY_LARGA, MAXIMO_HORA));
        if (!FORMATO_HORA.matcher(hora).matches())
            formateadorExcepciones.lanzarMalFormato(String.format(MensajesError.HORA_CRUCE_MAL_FORMADA, hora));
        return CruceSupletorio.builder()
                .asignaturaMatriculadaCruzada(cruzada)
                .fechaExamenCruzada(fechaExamenCruzada)
                .horaExamenCruzada(hora)
                .build();
    }

    private CruceSupletorio sinDatosDeCruce(String uuidCruzada, LocalDate fechaExamenCruzada, String horaExamenCruzada) {
        if (tieneTexto(uuidCruzada) || fechaExamenCruzada != null || tieneTexto(horaExamenCruzada))
            formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.DATOS_CRUCE_NO_PERMITIDOS);
        return null;
    }

    private void validarAnexos(List<AnexoRadicacion> entregados, TipoSolicitudAcademica tipo, CausaSupletorio causa) {
        Map<String, TipoAnexoAcademico> catalogo = new HashMap<>();
        for (TipoAnexoAcademico tipoAnexo : tipoAnexoGateway.getPorTipo(tipo.getUuidTipoSolicitudAcademica()))
            catalogo.put(tipoAnexo.getUuidTipoAnexoAcademico(), tipoAnexo);
        String propioDeLaCausa = causa == CausaSupletorio.CRUCE ? FORMATO_DOCENTE_CRUCE : SOPORTE_JUSTIFICACION;
        String ajenoALaCausa = causa == CausaSupletorio.CRUCE ? SOPORTE_JUSTIFICACION : FORMATO_DOCENTE_CRUCE;

        Set<String> presentes = new HashSet<>();
        List<TipoAnexoAcademico> tipos = new ArrayList<>();
        for (AnexoRadicacion anexo : entregados) {
            String uuidTipo = anexo == null ? null : anexo.getUuidTipoAnexoAcademico();
            if (!tieneTexto(uuidTipo))
                formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.SOPORTE_LIBRE_NO_PERMITIDO, tipo.getNombre()));
            TipoAnexoAcademico tipoAnexo = catalogo.get(uuidTipo);
            if (tipoAnexo == null)
                anexoDeOtroTipo(uuidTipo, tipo);
            String nombre = tipoAnexo.getNombre();
            if (RECIBO_PAGO.equals(nombre) || COMPROBANTE_PAGO.equals(nombre))
                formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.ANEXO_NO_PERMITIDO_AL_RADICAR, nombre));
            if (ajenoALaCausa.equals(nombre))
                formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.ANEXO_NO_CORRESPONDE_CAUSA, nombre, nombreCausa(causa)));
            if (!presentes.add(nombre))
                formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.ANEXO_REPETIDO, nombre));
            tipos.add(tipoAnexo);
        }
        List<String> faltantes = List.of(FORMATO_FOR_23, propioDeLaCausa).stream().filter(nombre -> !presentes.contains(nombre)).toList();
        if (!faltantes.isEmpty())
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.ANEXOS_OBLIGATORIOS_FALTANTES, String.join("; ", faltantes)));

        for (int i = 0; i < entregados.size(); i++)
            validadorArchivo.validar(entregados.get(i).getArchivo(), tipos.get(i).getNombre(), tipos.get(i).getFormatosPermitidos());
    }

    private void anexoDeOtroTipo(String uuidTipoAnexo, TipoSolicitudAcademica tipo) {
        TipoAnexoAcademico otro = tipoAnexoGateway.getPorUuid(uuidTipoAnexo);
        if (otro == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, TIPO_ANEXO_ACADEMICO, uuidTipoAnexo));
        formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.ANEXO_DE_OTRO_TIPO, otro.getNombre(), tipo.getNombre()));
    }

    private String nombreCausa(CausaSupletorio causa) {
        return causa.name().toLowerCase(Locale.ROOT);
    }

    private String nombreDe(AsignaturaMatriculada asignatura) {
        Asignatura datos = asignatura.getAsignatura();
        if (datos == null)
            return asignatura.getUuidAsignaturaMatriculada();
        return datos.getCodigoAsignatura() + " - " + datos.getNombreAsignatura();
    }

    private boolean tieneTexto(String valor) {
        return valor != null && !valor.isBlank();
    }
}
