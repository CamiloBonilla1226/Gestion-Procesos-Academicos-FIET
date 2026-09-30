package com.unicauca.cfiet.solicitudes.infraestructura.configuracion.lectorArchivos.validadoresArchivos;

import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorTiposSolicitud.DTOPeticion.TipoSolicitudDTOPeticion;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Valida los atributos de la petición creada por medio del archivo excel.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Service("validador-tipos-solicitud")
@RequiredArgsConstructor
public class TipoSolicitudExcelService implements ValidadorPeticionesExcel<TipoSolicitudDTOPeticion>{
    private final Validator validator;

    @Override
    public Map<String, String> validar(TipoSolicitudDTOPeticion peticion) {
        Set<ConstraintViolation<TipoSolicitudDTOPeticion>> violaciones = validator.validate(peticion);
        if (violaciones.isEmpty())
            return null;

        return violaciones.stream()
                .collect(Collectors.toMap(
                        v -> v.getPropertyPath().toString(),
                        ConstraintViolation::getMessage
                ));
    }
}
