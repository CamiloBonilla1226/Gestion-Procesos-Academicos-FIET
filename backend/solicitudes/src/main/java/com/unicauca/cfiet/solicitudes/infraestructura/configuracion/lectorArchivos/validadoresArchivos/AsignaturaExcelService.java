package com.unicauca.cfiet.solicitudes.infraestructura.configuracion.lectorArchivos.validadoresArchivos;

import com.unicauca.cfiet.solicitudes.infraestructura.configuracion.lectorArchivos.ColumnasExcelAsignaturas;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorAsignaturas.DTOPeticion.AsignaturaDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorAsignaturas.DTOPeticion.FilaAsignaturaExcelDTOPeticion;
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

@Service("validador-asignaturas")
@RequiredArgsConstructor
public class AsignaturaExcelService implements ValidadorPeticionesExcel<FilaAsignaturaExcelDTOPeticion> {
    private static final String UUID_PROGRAMA = "uuidProgramaAcademico";
    private static final String PROGRAMA = "programaAcademico";

    private final Validator validator;

    @Override
    public Map<String, String> validar(FilaAsignaturaExcelDTOPeticion fila) {
        Set<ConstraintViolation<AsignaturaDTOPeticion>> violaciones = validator.validate(fila.getPeticion());
        if (violaciones.isEmpty())
            return null;

        List<ConstraintViolation<AsignaturaDTOPeticion>> ordenadas = violaciones.stream()
                .sorted(Comparator.comparingInt((ConstraintViolation<AsignaturaDTOPeticion> v) ->
                                ColumnasExcelAsignaturas.COLUMNAS.indexOf(columna(v)))
                        .thenComparing(ConstraintViolation::getMessage))
                .toList();

        Map<String, String> errores = new LinkedHashMap<>();
        for (ConstraintViolation<AsignaturaDTOPeticion> violacion : ordenadas) {
            String columna = columna(violacion);
            String mensaje = String.format(MensajesError.EXCEL_FILA_ERROR, fila.getNumeroFila(),
                    ColumnasExcelAsignaturas.letra(columna), columna, violacion.getMessage());
            errores.merge(columna, mensaje, (anterior, nuevo) -> anterior + " " + violacion.getMessage());
        }
        return errores;
    }

    private String columna(ConstraintViolation<AsignaturaDTOPeticion> violacion) {
        String campo = violacion.getPropertyPath().toString();
        return UUID_PROGRAMA.equals(campo) ? PROGRAMA : campo;
    }
}
