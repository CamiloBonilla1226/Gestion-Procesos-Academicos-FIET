package com.unicauca.cfiet.solicitudes.infraestructura.configuracion.lectorArchivos.validadoresArchivos;

import com.unicauca.cfiet.solicitudes.infraestructura.configuracion.lectorArchivos.ColumnasExcelEstudiantes;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorEstudiantes.DTOPeticion.EstudianteDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorEstudiantes.DTOPeticion.FilaEstudianteExcelDTOPeticion;
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

@Service("validador-estudiantes")
@RequiredArgsConstructor
public class EstudianteExcelService implements ValidadorPeticionesExcel<FilaEstudianteExcelDTOPeticion> {
    private final Validator validator;

    @Override
    public Map<String, String> validar(FilaEstudianteExcelDTOPeticion fila) {
        Set<ConstraintViolation<EstudianteDTOPeticion>> violaciones = validator.validate(fila.getPeticion());
        if (violaciones.isEmpty())
            return null;

        List<ConstraintViolation<EstudianteDTOPeticion>> ordenadas = violaciones.stream()
                .sorted(Comparator.comparingInt((ConstraintViolation<EstudianteDTOPeticion> v) ->
                                ColumnasExcelEstudiantes.COLUMNAS.indexOf(columna(v)))
                        .thenComparing(ConstraintViolation::getMessage))
                .toList();

        Map<String, String> errores = new LinkedHashMap<>();
        for (ConstraintViolation<EstudianteDTOPeticion> violacion : ordenadas) {
            String columna = columna(violacion);
            String mensaje = String.format(MensajesError.EXCEL_FILA_ERROR, fila.getNumeroFila(),
                    ColumnasExcelEstudiantes.letra(columna), columna, violacion.getMessage());
            errores.merge(columna, mensaje, (anterior, nuevo) -> anterior + " " + violacion.getMessage());
        }
        return errores;
    }

    private String columna(ConstraintViolation<EstudianteDTOPeticion> violacion) {
        String ruta = violacion.getPropertyPath().toString();
        return ruta.substring(ruta.lastIndexOf('.') + 1);
    }
}
