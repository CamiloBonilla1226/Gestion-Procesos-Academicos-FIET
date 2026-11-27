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
    public static final int MAXIMO_TEXTO = 4000;
    public static final String APRUEBA = "aprueba la";
    public static final String RECHAZA = "niega la";
    private static final String DECISION_PARCIAL = "aprueba la cancelación de %s y niega la cancelación de %s, en la solicitud de";
    public static final String ASUNTO_COMUNICACION = "Notificación de Cancelación de asignatura";
    private static final String ASUNTO_MENSAJE_INICIAL = "Respuesta a su solicitud de cancelación de asignatura - Radicado %s";
    private static final String FIRMA = "[FIRMA]";
    private static final String MENSAJE_INICIAL = """
            Respetado(a) estudiante:

            Reciba un cordial saludo.

            En atención a la solicitud de cancelación de asignatura radicada por usted, me permito informarle que el señor Decano de la Facultad ya emitió respuesta a la misma. Por tal motivo, le solicito comedidamente acercarse a la Decanatura de la Facultad de Ingeniería Electrónica y Telecomunicaciones para conocer la decisión y adelantar el trámite correspondiente.

            Quedo atento a cualquier inquietud.

            Atentamente,

            [FIRMA]""";
    private static final String COMUNICACION_ESTUDIANTE = """
            Estimado (a) estudiante reciba un cordial saludo, de conformidad con la solicitud, me permito adjuntar la Resolución correspondiente.

            COMUNICACIÓN ELECTRÓNICA - RESOLUCIÓN No. {prefijo}{numero} de {anio}

            Se comunica al estudiante, {nombre}, del texto de la resolución No. {prefijo}{numero} de {anio}, la misma por la cual se {decision} cancelación de asignatura, Resolución la cual se adjunta a este correo electrónico y se comunica conforme a los parámetros normativos de la Ley 1437 de 2011.

            Cordialmente,

            {firma}""";
    private static final Set<String> RESALTADOS = Set.of("numero", "anio", "nombre", "decision");
    private static final Pattern MARCADOR = Pattern.compile("\\{(\\w+)}");
    private static final String PARRAFO_HTML = "<p style=\"margin:0 0 12px 0\">%s</p>";
    private static final String PARRAFO_ESTILO_HTML = "<p style=\"%s\">%s</p>";
    private static final String CUERPO_HTML = "<div style=\"font-family:Georgia,'Times New Roman',serif;font-size:14px;line-height:1.5;color:#222222\">%s</div>";
    private static final String ESTILO_PARRAFO = "margin:0 0 16px 0";
    private static final String ESTILO_TITULO = "margin:24px 0 16px 0;text-align:center;font-family:Arial,Helvetica,sans-serif;font-size:15px";
    private static final String ESTILO_CENTRADO = "margin:0 0 20px 0;text-align:center";
    private static final String ESTILO_DESPEDIDA = "margin:0 0 16px 0;font-style:italic";
    private static final String ESTILO_FIRMA = "margin:0;font-family:Arial,Helvetica,sans-serif;font-weight:bold";
    private static final String INICIO_TITULO = "COMUNICACIÓN ELECTRÓNICA";
    private static final String DESPEDIDA = "Cordialmente,";
    private static final String MARCA_NUMERO = "{numero}";
    private static final String MARCA_FIRMA = "{firma}";

    private final FirmaCorreo firma;
    private final String prefijo;

    public ComunicacionResolucion(FirmaCorreo firma, String prefijo) {
        this.firma = firma;
        this.prefijo = prefijo == null ? "" : prefijo;
    }

    public String getPrefijo() {
        return prefijo;
    }

    public String plantillaMensajeInicial() {
        return MENSAJE_INICIAL.replace(FIRMA, firma());
    }

    public String plantillaComunicacion() {
        return COMUNICACION_ESTUDIANTE.replace("{prefijo}", prefijo).replace("{firma}", firma());
    }

    public String asuntoMensajeInicial(String radicado) {
        return String.format(ASUNTO_MENSAJE_INICIAL, radicado);
    }

    public CorreoPreparado comunicacionEstudiante(String destinatario, String nombreCompleto, NotificacionResolucion notificacion) {
        Map<String, String> valores = Map.of(
                "numero", valor(notificacion.getNumeroResolucion()),
                "prefijo", prefijo,
                "anio", notificacion.getAnioResolucion() == null ? "" : String.valueOf(notificacion.getAnioResolucion()),
                "nombre", valor(nombreCompleto),
                "decision", valor(notificacion.getDecision()),
                "firma", firma());
        String plantilla = notificacion.getTextoComunicacion() == null || notificacion.getTextoComunicacion().isBlank()
                ? COMUNICACION_ESTUDIANTE : notificacion.getTextoComunicacion();
        List<List<FragmentoTexto>> parrafos = parrafos(plantilla, valores);
        return CorreoPreparado.builder()
                .destinatario(destinatario)
                .asunto(ASUNTO_COMUNICACION)
                .parrafos(parrafos)
                .texto(texto(parrafos))
                .html(htmlComunicacion(bloques(plantilla), parrafos))
                .build();
    }

    private String htmlComunicacion(List<String> bloques, List<List<FragmentoTexto>> parrafos) {
        StringBuilder html = new StringBuilder();
        for (int i = 0; i < parrafos.size(); i++) {
            String contenido = parrafos.get(i).stream().map(ComunicacionResolucion::htmlDe).collect(Collectors.joining());
            html.append(String.format(PARRAFO_ESTILO_HTML, estiloDe(bloques.get(i)), contenido));
        }
        return String.format(CUERPO_HTML, html);
    }

    private String estiloDe(String bloque) {
        String limpio = bloque.replace("\r", "").strip();
        if (limpio.toUpperCase().startsWith(INICIO_TITULO)) return ESTILO_TITULO;
        if (limpio.contains(MARCA_NUMERO)) return ESTILO_CENTRADO;
        if (limpio.equalsIgnoreCase(DESPEDIDA)) return ESTILO_DESPEDIDA;
        if (limpio.equals(MARCA_FIRMA) || (!firma().isEmpty() && limpio.equals(firma()))) return ESTILO_FIRMA;
        return ESTILO_PARRAFO;
    }

    private List<String> bloques(String plantilla) {
        return List.of(plantilla.strip().split("\n\\s*\n"));
    }

    public String decisionPropuesta(String etapaCodigo, List<AsignaturaSolicitudAcademica> asignaturas) {
        if (RECHAZADA_POR_DECANO.equals(etapaCodigo))
            return RECHAZA;
        List<AsignaturaSolicitudAcademica> todas = asignaturas == null ? List.of() : asignaturas;
        long aprobadas = todas.stream().filter(asignatura -> Boolean.TRUE.equals(asignatura.getAprobadaPorDecano())).count();
        if (aprobadas == 0)
            return RECHAZA;
        if (aprobadas == todas.size())
            return APRUEBA;
        return String.format(DECISION_PARCIAL, asignaturasDe(todas, true), asignaturasDe(todas, false));
    }

    private String asignaturasDe(List<AsignaturaSolicitudAcademica> asignaturas, boolean aprobadas) {
        List<String> nombres = asignaturas.stream()
                .filter(asignatura -> Boolean.TRUE.equals(asignatura.getAprobadaPorDecano()) == aprobadas)
                .map(this::nombreDe)
                .toList();
        String lista = nombres.size() == 1 ? nombres.get(0)
                : String.join(", ", nombres.subList(0, nombres.size() - 1)) + " y " + nombres.get(nombres.size() - 1);
        return (nombres.size() == 1 ? "la asignatura " : "las asignaturas ") + lista;
    }

    private String nombreDe(AsignaturaSolicitudAcademica asignatura) {
        if (asignatura.getAsignaturaMatriculada() == null || asignatura.getAsignaturaMatriculada().getAsignatura() == null)
            return "";
        return valor(asignatura.getAsignaturaMatriculada().getAsignatura().getNombreAsignatura());
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
        for (String bloque : bloques(plantilla)) {
            List<FragmentoTexto> fragmentos = new ArrayList<>();
            Matcher marcador = MARCADOR.matcher(bloque);
            int desde = 0;
            while (marcador.find()) {
                agregar(fragmentos, bloque.substring(desde, marcador.start()), false);
                String clave = marcador.group(1);
                boolean conocida = valores.containsKey(clave);
                agregar(fragmentos, conocida ? valores.get(clave) : marcador.group(), conocida && RESALTADOS.contains(clave));
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
