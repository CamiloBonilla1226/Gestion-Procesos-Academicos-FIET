package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorFuncionariosAcademicos.DTOPeticion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilaFuncionarioAcademicoExcelDTOPeticion {
    private int numeroFila;
    private FuncionarioAcademicoDTOPeticion peticion;
}
