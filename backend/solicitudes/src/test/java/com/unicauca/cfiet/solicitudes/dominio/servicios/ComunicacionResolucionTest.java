package com.unicauca.cfiet.solicitudes.dominio.servicios;

import com.unicauca.cfiet.solicitudes.dominio.modelos.Asignatura;
import com.unicauca.cfiet.solicitudes.dominio.modelos.AsignaturaMatriculada;
import com.unicauca.cfiet.solicitudes.dominio.modelos.AsignaturaSolicitudAcademica;
import com.unicauca.cfiet.solicitudes.dominio.modelos.CorreoPreparado;
import com.unicauca.cfiet.solicitudes.dominio.modelos.FirmaCorreo;
import com.unicauca.cfiet.solicitudes.dominio.modelos.FragmentoTexto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.NotificacionResolucion;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EtapaSolicitudAcademicaConstantes.APROBADA_POR_DECANO;
import static com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EtapaSolicitudAcademicaConstantes.RECHAZADA_POR_DECANO;
import static org.junit.jupiter.api.Assertions.*;

class ComunicacionResolucionTest {

    private static final String FIRMA = "CRISTIAN ESTIBEN ILLERA OSPITIA\nApoyo Administrativo\nDecanatura\n"
            + "Facultad de Ingeniería Electrónica y Telecomunicaciones";

    private final ComunicacionResolucion comunicacion = new ComunicacionResolucion(FirmaCorreo.builder()
            .nombre("CRISTIAN ESTIBEN ILLERA OSPITIA").cargo("Apoyo Administrativo").dependencia("Decanatura")
            .facultad("Facultad de Ingeniería Electrónica y Telecomunicaciones").build(), "8.4.2-4.10/");

    private NotificacionResolucion notificacion(String numero, String decision) {
        return NotificacionResolucion.builder().numeroResolucion(numero).anioResolucion(2026).decision(decision).build();
    }

    private AsignaturaSolicitudAcademica asignatura(Boolean aprobada) {
        return AsignaturaSolicitudAcademica.builder().aprobadaPorDecano(aprobada).build();
    }

    private AsignaturaSolicitudAcademica asignatura(String nombre, Boolean aprobada) {
        return AsignaturaSolicitudAcademica.builder()
                .asignaturaMatriculada(AsignaturaMatriculada.builder().asignatura(Asignatura.builder().nombreAsignatura(nombre).build()).build())
                .aprobadaPorDecano(aprobada)
                .build();
    }

    @Test
    void elMensajeInicialEsLaPlantillaDelClienteConLaFirmaDeCuatroLineas() {
        assertEquals("Buen día, estudiante,\n\n"
                + "Dando respuesta a la solicitud hecha por parte de usted, le recuerdo que debe acercarse a la Decanatura para revisar la "
                + "respuesta que emitió el señor Decano, frente a la solicitud de cancelación de asignatura hecha por usted, quedo atento a "
                + "cualquier inquietud.\n\nCordialmente,\n\n" + FIRMA, comunicacion.plantillaMensajeInicial());
        assertEquals("Respuesta a su solicitud de cancelación de asignatura - Radicado 2026-CA-0001",
                comunicacion.asuntoMensajeInicial("2026-CA-0001"));
    }

    @Test
    void laPlantillaEditableTraePrefijoYFirmaYDejaLasMarcasDeLosDatos() {
        String plantilla = comunicacion.plantillaComunicacion();

        assertTrue(plantilla.contains("COMUNICACIÓN ELECTRÓNICA - RESOLUCIÓN No. 8.4.2-4.10/{numero} de {anio}"));
        assertTrue(plantilla.contains("Se comunica al estudiante, {nombre}, del texto de la resolución No. 8.4.2-4.10/{numero} de {anio}"));
        assertTrue(plantilla.contains("la misma por la cual se {decision} cancelación de asignatura"));
        assertTrue(plantilla.endsWith("Cordialmente,\n\n" + FIRMA));
        assertFalse(plantilla.contains("{prefijo}"));
        assertFalse(plantilla.contains("{firma}"));
    }

    @Test
    void unTextoEditadoReemplazaLasMarcasYResaltaLosDatos() {
        NotificacionResolucion editada = notificacion("45", "niega la");
        editada.setTextoComunicacion("Hola {nombre}.\n \nResolución {numero}/178 de {anio}: se {decision} cancelación. {otra}");

        CorreoPreparado correo = comunicacion.comunicacionEstudiante("ana@unicauca.edu.co", "Ana Muñoz", editada);

        assertEquals("Hola Ana Muñoz.\n\nResolución 45/178 de 2026: se niega la cancelación. {otra}", correo.getTexto());
        assertEquals(2, correo.getParrafos().size());
        assertTrue(correo.getHtml().contains("Hola <strong>Ana Muñoz</strong>."));
        assertTrue(correo.getHtml().contains("Resolución <strong>45</strong>/178 de <strong>2026</strong>: se <strong>niega la</strong>"));
    }

    @Test
    void elCorreoAlEstudianteConservaElTextoDelClienteEnTextoPlano() {
        CorreoPreparado correo = comunicacion.comunicacionEstudiante("ana@unicauca.edu.co", "Ana María Muñoz Peña",
                notificacion("123-25", "aprueba la"));

        assertEquals("ana@unicauca.edu.co", correo.getDestinatario());
        assertEquals("Notificación de Cancelación de asignatura", correo.getAsunto());
        assertEquals("Estimado (a) estudiante reciba un cordial saludo, de conformidad con la solicitud, me permito adjuntar la "
                + "Resolución correspondiente.\n\n"
                + "COMUNICACIÓN ELECTRÓNICA - RESOLUCIÓN No. 8.4.2-4.10/123-25 de 2026\n\n"
                + "Se comunica al estudiante, Ana María Muñoz Peña, del texto de la resolución No. 8.4.2-4.10/123-25 de 2026, la misma por la cual "
                + "se aprueba la cancelación de asignatura, Resolución la cual se adjunta a este correo electrónico y se comunica conforme "
                + "a los parámetros normativos de la Ley 1437 de 2011.\n\n"
                + "Cordialmente,\n\n" + FIRMA, correo.getTexto());
    }

    @Test
    void enElHtmlLosValoresVanEnNegrillaYElPrefijoYLaFirmaNo() {
        CorreoPreparado correo = comunicacion.comunicacionEstudiante("ana@unicauca.edu.co", "Ana María Muñoz Peña",
                notificacion("123-25", "niega la"));

        assertTrue(correo.getHtml().contains("COMUNICACIÓN ELECTRÓNICA - RESOLUCIÓN No. 8.4.2-4.10/<strong>123-25</strong> de <strong>2026</strong>"));
        assertTrue(correo.getHtml().contains("Se comunica al estudiante, <strong>Ana María Muñoz Peña</strong>, del texto"));
        assertTrue(correo.getHtml().contains("se <strong>niega la</strong> cancelación de asignatura"));
        assertTrue(correo.getHtml().contains("CRISTIAN ESTIBEN ILLERA OSPITIA<br>Apoyo Administrativo<br>Decanatura<br>"));
        assertFalse(correo.getHtml().contains("<strong>8.4.2"));
        assertFalse(correo.getHtml().contains("<strong>CRISTIAN"));
        assertEquals(5, correo.getHtml().split("<p ").length - 1);
    }

    @Test
    void enElHtmlElTituloYLaComunicacionVanCentradosYLaFirmaEnNegrilla() {
        CorreoPreparado correo = comunicacion.comunicacionEstudiante("ana@unicauca.edu.co", "Ana", notificacion("7", "aprueba la"));
        String html = correo.getHtml();

        assertTrue(html.startsWith("<div style=\"font-family:Georgia"));
        assertTrue(html.contains("<p style=\"margin:0 0 16px 0\">Estimado (a) estudiante"));
        assertTrue(html.contains("text-align:center;font-family:Arial,Helvetica,sans-serif;font-size:15px\">COMUNICACIÓN ELECTRÓNICA"));
        assertTrue(html.contains("<p style=\"margin:0 0 20px 0;text-align:center\">Se comunica al estudiante"));
        assertTrue(html.contains("<p style=\"margin:0 0 16px 0;font-style:italic\">Cordialmente,</p>"));
        assertTrue(html.contains("font-weight:bold\">CRISTIAN ESTIBEN ILLERA OSPITIA<br>"));
    }

    @Test
    void unTextoEditadoCentraLosParrafosQueLlevanElNumero() {
        NotificacionResolucion editada = notificacion("9", "aprueba la");
        editada.setTextoComunicacion("Hola {nombre}.\n\nResolución {numero} de {anio}.\n\nGracias.");

        String html = comunicacion.comunicacionEstudiante("ana@unicauca.edu.co", "Ana", editada).getHtml();

        assertTrue(html.contains("<p style=\"margin:0 0 16px 0\">Hola "));
        assertTrue(html.contains("<p style=\"margin:0 0 20px 0;text-align:center\">Resolución "));
        assertTrue(html.contains("<p style=\"margin:0 0 16px 0\">Gracias.</p>"));
    }

    @Test
    void losValoresSeEscapanEnElHtml() {
        CorreoPreparado correo = comunicacion.comunicacionEstudiante("ana@unicauca.edu.co", "Ana <script>alert(1)</script> & \"Pérez\"",
                notificacion("1<2", "aprueba la"));

        assertTrue(correo.getHtml().contains("<strong>Ana &lt;script&gt;alert(1)&lt;/script&gt; &amp; &quot;Pérez&quot;</strong>"));
        assertTrue(correo.getHtml().contains("<strong>1&lt;2</strong>"));
        assertFalse(correo.getHtml().contains("<script>"));
        assertTrue(correo.getTexto().contains("Ana <script>alert(1)</script> & \"Pérez\""));
    }

    @Test
    void losFragmentosMarcanSoloLosCuatroValores() {
        CorreoPreparado correo = comunicacion.comunicacionEstudiante("ana@unicauca.edu.co", "Ana", notificacion("7", "aprueba la"));

        List<String> resaltados = correo.getParrafos().stream().flatMap(List::stream)
                .filter(FragmentoTexto::isResaltado).map(FragmentoTexto::getTexto).toList();
        assertEquals(List.of("7", "2026", "Ana", "7", "2026", "aprueba la"), resaltados);
    }

    @Test
    void laDecisionPropuestaSaleDeLaDecisionDelDecanoPorAsignatura() {
        assertEquals("aprueba la", comunicacion.decisionPropuesta(APROBADA_POR_DECANO, List.of(asignatura(true), asignatura(true))));
        assertEquals("aprueba la cancelación de la asignatura Cálculo I y niega la cancelación de la asignatura Física I, en la solicitud de",
                comunicacion.decisionPropuesta(APROBADA_POR_DECANO, List.of(asignatura("Cálculo I", true), asignatura("Física I", false))));
        assertEquals("aprueba la cancelación de las asignaturas Cálculo I, Dibujo y Inglés y niega la cancelación de la asignatura Física I, "
                + "en la solicitud de", comunicacion.decisionPropuesta(APROBADA_POR_DECANO, List.of(asignatura("Cálculo I", true),
                asignatura("Física I", false), asignatura("Dibujo", true), asignatura("Inglés", true))));
        assertEquals("aprueba la cancelación de la asignatura Dibujo y niega la cancelación de las asignaturas Cálculo I y Física I, "
                + "en la solicitud de", comunicacion.decisionPropuesta(APROBADA_POR_DECANO, List.of(asignatura("Cálculo I", false),
                asignatura("Física I", false), asignatura("Dibujo", true))));
        assertEquals("niega la", comunicacion.decisionPropuesta(RECHAZADA_POR_DECANO, List.of(asignatura(true))));
        assertEquals("niega la", comunicacion.decisionPropuesta(APROBADA_POR_DECANO, List.of(asignatura(false))));
    }

    @Test
    void elPrefijoSaleDeLaConfiguracion() {
        ComunicacionResolucion otra = new ComunicacionResolucion(FirmaCorreo.builder().nombre("X").build(), "1.2-3/");

        assertEquals("1.2-3/", otra.getPrefijo());
        assertTrue(otra.comunicacionEstudiante("a@b.co", "Ana", notificacion("5", "aprueba la")).getTexto().contains("No. 1.2-3/5 de 2026"));
        assertTrue(otra.plantillaMensajeInicial().endsWith("Cordialmente,\n\nX"));
    }
}
