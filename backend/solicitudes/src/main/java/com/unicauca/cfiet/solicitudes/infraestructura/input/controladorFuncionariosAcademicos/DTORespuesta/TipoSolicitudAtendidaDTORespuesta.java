package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorFuncionariosAcademicos.DTORespuesta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TipoSolicitudAtendidaDTORespuesta {
    private String uuidTipoSolicitudAcademica;
    private String nombre;
}
