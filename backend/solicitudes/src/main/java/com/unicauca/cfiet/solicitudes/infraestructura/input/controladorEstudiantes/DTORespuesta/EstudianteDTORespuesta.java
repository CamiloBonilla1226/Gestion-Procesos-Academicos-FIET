package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorEstudiantes.DTORespuesta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EstudianteDTORespuesta {
    private String uuidUsuario;
    private String nombres;
    private String apellidos;
    private Boolean estado;
    private String tipoDocumento;
    private String numeroDocumento;
    private String telefono;
    private String correoElectronico;
    private String username;
    private String codigoEstudiantil;
    private String programaAcademico;
    private String semestre;
    private String facultad;
    @Builder.Default
    private List<AsignaturaMatriculadaDTORespuesta> asignaturasMatriculadas = new ArrayList<>();
}
