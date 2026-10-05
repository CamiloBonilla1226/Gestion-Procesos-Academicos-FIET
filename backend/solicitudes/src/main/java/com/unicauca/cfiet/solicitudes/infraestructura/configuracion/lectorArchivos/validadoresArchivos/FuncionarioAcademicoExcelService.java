package com.unicauca.cfiet.solicitudes.infraestructura.configuracion.lectorArchivos.validadoresArchivos;

import com.unicauca.cfiet.solicitudes.infraestructura.configuracion.lectorArchivos.ColumnasExcelFuncionariosAcademicos;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorFuncionariosAcademicos.DTOPeticion.FilaFuncionarioAcademicoExcelDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorFuncionariosAcademicos.DTOPeticion.FuncionarioAcademicoDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service("validador-funcionarios-academicos")
@RequiredArgsConstructor
public class FuncionarioAcademicoExcelService implements ValidadorPeticionesExcel<FilaFuncionarioAcademicoExcelDTOPeticion> {
    private final Validator validator;

    @Override
    public Map<String, String> validar(FilaFuncionarioAcademicoExcelDTOPeticion fila) {
        Set<ConstraintViolation<FuncionarioAcademicoDTOPeticion>> violaciones = validator.validate(fila.getPeticion());
        if (violaciones.isEmpty())
            return null;

        List<ConstraintViolation<FuncionarioAcademicoDTOPeticion>> ordenadas = violaciones.stream()
                .sorted(Comparator.comparingInt((ConstraintViolation<FuncionarioAcademicoDTOPeticion> v) ->
                                ColumnasExcelFuncionariosAcademicos.COLUMNAS.indexOf(v.getPropertyPath().toString()))
                        .thenComparing(ConstraintViolation::getMessage))
                .toList();

        Map<String, String> errores = new LinkedHashMap<>();
        for (ConstraintViolation<FuncionarioAcademicoDTOPeticion> violacion : ordenadas) {
            String columna = violacion.getPropertyPath().toString();
            String mensaje = String.format(MensajesError.EXCEL_FILA_ERROR, fila.getNumeroFila(),
                    ColumnasExcelFuncionariosAcademicos.letra(columna), columna, violacion.getMessage());
            errores.merge(columna, mensaje, (anterior, nuevo) -> anterior + " " + violacion.getMessage());
        }
        return errores;
    }
}
