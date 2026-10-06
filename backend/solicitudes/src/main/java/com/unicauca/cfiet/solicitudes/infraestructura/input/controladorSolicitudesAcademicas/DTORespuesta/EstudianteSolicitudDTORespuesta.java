package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudesAcademicas.DTORespuesta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EstudianteSolicitudDTORespuesta {
    private String uuidUsuario;
    private String nombres;
    private String apellidos;
    private String codigoEstudiantil;
    private String programaAcademico;
    private String semestre;
    private String correoElectronico;
}
