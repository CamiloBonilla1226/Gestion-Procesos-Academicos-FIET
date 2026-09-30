package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Representa un anexo asociado a una solicitud.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Anexo {
    private String uuidAnexo;
    private String nombre;
    private MultipartFile anexoFile;
    private String urlAnexo;
    private Solicitud objSolicitud;
}
