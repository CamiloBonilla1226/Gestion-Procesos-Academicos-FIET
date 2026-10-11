package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorAsignaturas.DTOPeticion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilaAsignaturaExcelDTOPeticion {
    private int numeroFila;
    private String programaAcademico;
    private AsignaturaDTOPeticion peticion;
}
