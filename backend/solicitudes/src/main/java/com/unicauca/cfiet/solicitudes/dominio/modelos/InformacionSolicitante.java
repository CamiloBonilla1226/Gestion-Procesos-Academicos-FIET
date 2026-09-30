package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

/**
 * Representa la información personal del solicitante publcio de una solicitud.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InformacionSolicitante {
    private String uuidInformacionSolicitante;
    private String tipoDocumento;
    private String numeroDocumento;
    private String nombres;
    private String apellidos;
    private String telefono;
    private String correoElectronico;
    private Solicitud solicitud;
}
