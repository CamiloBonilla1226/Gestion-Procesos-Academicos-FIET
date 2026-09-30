package com.unicauca.cfiet.solicitudes.dominio.modelos;

import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.ApplicationConstantes;
import lombok.*;

/**
 * Representa un tipo de anexo asociado a un tipo de solicitud.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoAnexo {
    private String uuidTipoAnexo;
    private String nombre;
    private String descripcion;
    private String formato;
    private Boolean obligatoriedad;
    private TipoSolicitud objTipoSolicitud;

    /**
     * Verifica si el formato del anexo es válido según los formatos permitidos.
     *
     * @return true si el formato es PDF, DOCX o XLSX; false en caso contrario
     */
    public boolean formatoEsValido(){
        if(!formato.toUpperCase().equals(ApplicationConstantes.FORMATO_PDF)
                && !formato.toUpperCase().equals(ApplicationConstantes.FORMATO_DOCX)
                && !formato.toUpperCase().equals(ApplicationConstantes.FORMATO_XLSX))
            return false;
        return true;
    }
}
