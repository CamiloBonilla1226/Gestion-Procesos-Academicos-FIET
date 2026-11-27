package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesAsignatura.DTORespuesta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RemisionConsejoDTORespuesta {
    private String uuidSolicitud;
    private String uuidSolicitudAcademica;
    private String radicado;
    private String etapaCodigo;
    private String nombresEstudiante;
    private String apellidosEstudiante;
    private String codigoEstudiantil;
    private String motivo;
    private OrdenDelDia ordenDelDia;
    private List<Asignatura> asignaturas;
    private Respuesta respuesta;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrdenDelDia {
        private String uuidOrdenDelDia;
        private String nombre;
        private String fecha;
        private String numeroActa;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Asignatura {
        private String asignaturaSolicitudUuid;
        private String codigoAsignatura;
        private String nombreAsignatura;
        private String numeroFaltas;
        private String nota;
        private Boolean aprobada;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Respuesta {
        private String uuidRespuesta;
        private String tipoRespuesta;
        private String consecutivoFiet;
        private String respuestaConsejo;
        private String indicaciones;
    }
}
