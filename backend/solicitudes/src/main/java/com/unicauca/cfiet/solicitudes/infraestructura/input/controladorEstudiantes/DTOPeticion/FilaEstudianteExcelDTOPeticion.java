package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorEstudiantes.DTOPeticion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilaEstudianteExcelDTOPeticion {
    private int numeroFila;
    private EstudianteDTOPeticion peticion;
}
