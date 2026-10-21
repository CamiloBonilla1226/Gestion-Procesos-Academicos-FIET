package com.unicauca.cfiet.solicitudes.dominio.servicios;

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
            .facultad("Facultad de Ingeniería Electrónica y Telecomunicaciones").build(), "/178");

    private NotificacionResolucion notificacion(String numero, String decision) {
        return NotificacionResolucion.builder().numeroResolucion(numero).anioResolucion(2026).decision(decision).build();
    }

    private AsignaturaSolicitudAcademica asignatura(Boolean aprobada) {
        return AsignaturaSolicitudAcademica.builder().aprobadaPorDecano(aprobada).build();
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
    void elCorreoAlEstudianteConservaElTextoDelClienteEnTextoPlano() {
        CorreoPreparado correo = comunicacion.comunicacionEstudiante("ana@unicauca.edu.co", "Ana María Muñoz Peña",
                notificacion("123-25", "aprueba la"));

        assertEquals("ana@unicauca.edu.co", correo.getDestinatario());
        assertEquals("Notificación de Cancelación de asignatura", correo.getAsunto());
        assertEquals("Estimado (a) estudiante reciba un cordial saludo, de conformidad con la solicitud, me permito adjuntar la "
                + "Resolución correspondiente.\n\n"
                + "COMUNICACIÓN ELECTRÓNICA - RESOLUCIÓN No. 123-25/178 de 2026\n\n"
                + "Se comunica al estudiante, Ana María Muñoz Peña, del texto de la resolución No. 123-25/178 de 2026, la misma por la cual "
                + "se aprueba la cancelación de asignatura, Resolución la cual se adjunta a este correo electrónico y se comunica conforme "
                + "a los parámetros normativos de la Ley 1437 de 2011.\n\n"
                + "Cordialmente,\n\n" + FIRMA, correo.getTexto());
    }

    @Test
    void enElHtmlLosValoresVanEnNegrillaYElSufijoYLaFirmaNo() {
        CorreoPreparado correo = comunicacion.comunicacionEstudiante("ana@unicauca.edu.co", "Ana María Muñoz Peña",
                notificacion("123-25", "rechaza la"));

        assertTrue(correo.getHtml().contains("COMUNICACIÓN ELECTRÓNICA - RESOLUCIÓN No. <strong>123-25</strong>/178 de <strong>2026</strong>"));
        assertTrue(correo.getHtml().contains("Se comunica al estudiante, <strong>Ana María Muñoz Peña</strong>, del texto"));
        assertTrue(correo.getHtml().contains("se <strong>rechaza la</strong> cancelación de asignatura"));
        assertTrue(correo.getHtml().contains("CRISTIAN ESTIBEN ILLERA OSPITIA<br>Apoyo Administrativo<br>Decanatura<br>"));
        assertFalse(correo.getHtml().contains("<strong>/178"));
        assertFalse(correo.getHtml().contains("<strong>CRISTIAN"));
        assertEquals(5, correo.getHtml().split("<p ").length - 1);
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
        assertEquals("aprueba parcialmente la", comunicacion.decisionPropuesta(APROBADA_POR_DECANO, List.of(asignatura(true), asignatura(false))));
        assertEquals("rechaza la", comunicacion.decisionPropuesta(RECHAZADA_POR_DECANO, List.of(asignatura(true))));
        assertEquals("rechaza la", comunicacion.decisionPropuesta(APROBADA_POR_DECANO, List.of(asignatura(false))));
    }

    @Test
    void elSufijoSaleDeLaConfiguracion() {
        ComunicacionResolucion otra = new ComunicacionResolucion(FirmaCorreo.builder().nombre("X").build(), "/999");

        assertEquals("/999", otra.getSufijo());
        assertTrue(otra.comunicacionEstudiante("a@b.co", "Ana", notificacion("5", "aprueba la")).getTexto().contains("No. 5/999 de 2026"));
        assertTrue(otra.plantillaMensajeInicial().endsWith("Cordialmente,\n\nX"));
    }
}
