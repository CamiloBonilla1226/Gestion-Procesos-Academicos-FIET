package com.unicauca.cfiet.solicitudes.dominio.servicios;

import com.unicauca.cfiet.solicitudes.dominio.modelos.AsignaturaSolicitudAcademica;
import com.unicauca.cfiet.solicitudes.dominio.modelos.CorreoPreparado;
import com.unicauca.cfiet.solicitudes.dominio.modelos.FirmaCorreo;
import com.unicauca.cfiet.solicitudes.dominio.modelos.FragmentoTexto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.NotificacionResolucion;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EtapaSolicitudAcademicaConstantes.RECHAZADA_POR_DECANO;

public class ComunicacionResolucion {
    public static final int MAXIMO_MENSAJE = 2000;
    public static final String APRUEBA = "aprueba la";
    public static final String RECHAZA = "rechaza la";
    public static final String APRUEBA_PARCIALMENTE = "aprueba parcialmente la";
    public static final String ASUNTO_COMUNICACION = "Notificación de Cancelación de asignatura";
    private static final String ASUNTO_MENSAJE_INICIAL = "Respuesta a su solicitud de cancelación de asignatura - Radicado %s";
    private static final String FIRMA = "[FIRMA]";
    private static final String MENSAJE_INICIAL = """
            Buen día, estudiante,

            Dando respuesta a la solicitud hecha por parte de usted, le recuerdo que debe acercarse a la Decanatura para revisar la respuesta que emitió el señor Decano, frente a la solicitud de cancelación de asignatura hecha por usted, quedo atento a cualquier inquietud.

            Cordialmente,

            [FIRMA]""";
    private static final String COMUNICACION_ESTUDIANTE = """
            Estimado (a) estudiante reciba un cordial saludo, de conformidad con la solicitud, me permito adjuntar la Resolución correspondiente.

            COMUNICACIÓN ELECTRÓNICA - RESOLUCIÓN No. {numero}{sufijo} de {anio}

            Se comunica al estudiante, {nombre}, del texto de la resolución No. {numero}{sufijo} de {anio}, la misma por la cual se {decision} cancelación de asignatura, Resolución la cual se adjunta a este correo electrónico y se comunica conforme a los parámetros normativos de la Ley 1437 de 2011.

            Cordialmente,

            {firma}""";
    private static final Set<String> RESALTADOS = Set.of("numero", "anio", "nombre", "decision");
    private static final Pattern MARCADOR = Pattern.compile("\\{(\\w+)}");
    private static final String PARRAFO_HTML = "<p style=\"margin:0 0 12px 0\">%s</p>";

    private final FirmaCorreo firma;
    private final String sufijo;

    public ComunicacionResolucion(FirmaCorreo firma, String sufijo) {
        this.firma = firma;
        this.sufijo = sufijo == null ? "" : sufijo;
    }

    public String getSufijo() {
        return sufijo;
    }

    public String plantillaMensajeInicial() {
        return MENSAJE_INICIAL.replace(FIRMA, firma());
    }

    public String asuntoMensajeInicial(String radicado) {
        return String.format(ASUNTO_MENSAJE_INICIAL, radicado);
    }

    public CorreoPreparado comunicacionEstudiante(String destinatario, String nombreCompleto, NotificacionResolucion notificacion) {
        Map<String, String> valores = Map.of(
                "numero", valor(notificacion.getNumeroResolucion()),
                "sufijo", sufijo,
                "anio", notificacion.getAnioResolucion() == null ? "" : String.valueOf(notificacion.getAnioResolucion()),
                "nombre", valor(nombreCompleto),
                "decision", valor(notificacion.getDecision()),
                "firma", firma());
        List<List<FragmentoTexto>> parrafos = parrafos(COMUNICACION_ESTUDIANTE, valores);
        return CorreoPreparado.builder()
                .destinatario(destinatario)
                .asunto(ASUNTO_COMUNICACION)
                .parrafos(parrafos)
                .texto(texto(parrafos))
                .html(html(parrafos))
                .build();
    }

    public String decisionPropuesta(String etapaCodigo, List<AsignaturaSolicitudAcademica> asignaturas) {
        if (RECHAZADA_POR_DECANO.equals(etapaCodigo))
            return RECHAZA;
        List<AsignaturaSolicitudAcademica> todas = asignaturas == null ? List.of() : asignaturas;
        long aprobadas = todas.stream().filter(asignatura -> Boolean.TRUE.equals(asignatura.getAprobadaPorDecano())).count();
        if (aprobadas == 0)
            return RECHAZA;
        return aprobadas == todas.size() ? APRUEBA : APRUEBA_PARCIALMENTE;
    }

    public static String texto(List<List<FragmentoTexto>> parrafos) {
        return parrafos.stream()
                .map(parrafo -> parrafo.stream().map(FragmentoTexto::getTexto).collect(Collectors.joining()))
                .collect(Collectors.joining("\n\n"));
    }

    public static String html(List<List<FragmentoTexto>> parrafos) {
        return parrafos.stream()
                .map(parrafo -> String.format(PARRAFO_HTML, parrafo.stream().map(ComunicacionResolucion::htmlDe).collect(Collectors.joining())))
                .collect(Collectors.joining());
    }

    public static String escapar(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    private static String htmlDe(FragmentoTexto fragmento) {
        String escapado = escapar(fragmento.getTexto()).replace("\n", "<br>");
        return fragmento.isResaltado() ? "<strong>" + escapado + "</strong>" : escapado;
    }

    private List<List<FragmentoTexto>> parrafos(String plantilla, Map<String, String> valores) {
        List<List<FragmentoTexto>> parrafos = new ArrayList<>();
        for (String bloque : plantilla.split("\n\n")) {
            List<FragmentoTexto> fragmentos = new ArrayList<>();
            Matcher marcador = MARCADOR.matcher(bloque);
            int desde = 0;
            while (marcador.find()) {
                agregar(fragmentos, bloque.substring(desde, marcador.start()), false);
                String clave = marcador.group(1);
                agregar(fragmentos, valores.getOrDefault(clave, ""), RESALTADOS.contains(clave));
                desde = marcador.end();
            }
            agregar(fragmentos, bloque.substring(desde), false);
            parrafos.add(fragmentos);
        }
        return parrafos;
    }

    private void agregar(List<FragmentoTexto> fragmentos, String texto, boolean resaltado) {
        if (texto == null || texto.isEmpty()) return;
        fragmentos.add(FragmentoTexto.builder().texto(texto).resaltado(resaltado).build());
    }

    private String firma() {
        return Stream.of(firma.getNombre(), firma.getCargo(), firma.getDependencia(), firma.getFacultad())
                .filter(linea -> linea != null && !linea.isBlank())
                .map(String::trim)
                .collect(Collectors.joining("\n"));
    }

    private String valor(String texto) {
        return texto == null ? "" : texto.trim();
    }
}
