package com.unicauca.cfiet.solicitudes.dominio.modelos;

import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.ApplicationConstantes;
import lombok.*;
import java.util.List;
import java.util.UUID;

/**
 * Representa un tipo de solicitud dentro del sistema, incluyendo su sección, perfil solicitante
 * y los anexos asociados.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoSolicitud {
    private String uuidTipoSolicitud;
    private String nombre;
    private String descripcion;
    private String seccion;
    private String perfilSolicitante;
    private List<TipoAnexo> anexos;
    private Funcionario objFuncionarioEncargado;
    private String uuidFuncionario;

    /**
     * Verifica que la sección de la solicitud sea una de las secciones válidas definidas
     * en la aplicación.
     *
     * @return true si la sección es válida; false en caso contrario
     */
    public boolean revisarSeccion() {
        String seccion = getSeccion();
        if (seccion == null) return false;
        switch (seccion.trim().toLowerCase()) {
            case ApplicationConstantes.ASUNTOS_DECANO:
            case ApplicationConstantes.ASUNTOS_PREGRADO:
            case ApplicationConstantes.ASUNTOS_POSGRADOS:
            case ApplicationConstantes.ASUNTOS_DELEGADOS_EN_DECANO:
            case ApplicationConstantes.SOLICITUD_COMISION_ACADEMICA_INTERIOR_PAIS:
            case ApplicationConstantes.SOLICITUD_COMISION_ACADEMICA_EXTERIOR_PAIS:
            case ApplicationConstantes.INFORME_COMISION_ACADEMICA:
            case ApplicationConstantes.ASUNTOS_VARIOS:
                setSeccion(seccion.trim());
                return true;
            default:
                return false;
        }
    }

    /**
     * Verifica si el perfil solicitante es válido según la lista de roles disponibles.
     *
     * @param roles lista de roles existentes en el sistema
     * @return true si el perfil es válido; false en caso contrario
     */
    public boolean revisarPerfilSolicitante(List<Rol> roles) {
        return roles != null && roles.stream()
                .anyMatch(rol -> perfilSolicitante.equals(rol.getNombre()));
    }

    /**
     * Verifica que todos los anexos del tipo de solicitud tengan un formato válido.
     *
     * @return true si todos los anexos son válidos; false en caso contrario
     */
    public boolean revisarAnexos(){
        for(TipoAnexo anexo: anexos){
            if(!anexo.formatoEsValido())
                return false;
        }
        return true;
    }

    /**
     * Actualiza los campos de la instancia actual con los valores proporcionados
     * en otro TipoSolicitud.
     *
     * @param tipoSolicitud instancia con los datos actualizados
     */
    public void actualizarTipoSolicitud(TipoSolicitud tipoSolicitud) {
        if (tipoSolicitud.getNombre() != null && !tipoSolicitud.getNombre().isBlank())
            this.nombre = tipoSolicitud.getNombre();
        if (tipoSolicitud.getDescripcion() != null && !tipoSolicitud.getDescripcion().isBlank())
            this.descripcion = tipoSolicitud.getDescripcion();
        if (tipoSolicitud.getSeccion() != null && !tipoSolicitud.getSeccion().isBlank())
            this.seccion = tipoSolicitud.getSeccion();
        if (tipoSolicitud.getAnexos() != null && !tipoSolicitud.getAnexos().isEmpty()) {
            for(TipoAnexo anexo : tipoSolicitud.getAnexos()) {
                anexo.setUuidTipoAnexo(UUID.randomUUID().toString());
                anexo.setObjTipoSolicitud(this);
            }
            this.anexos = tipoSolicitud.getAnexos();
        }
        if(tipoSolicitud.getPerfilSolicitante() != null && !tipoSolicitud.getPerfilSolicitante().isBlank())
            this.perfilSolicitante = tipoSolicitud.getPerfilSolicitante();
        if (tipoSolicitud.getObjFuncionarioEncargado() != null)
            this.objFuncionarioEncargado = tipoSolicitud.getObjFuncionarioEncargado();
        if(tipoSolicitud.getUuidFuncionario() != null && !tipoSolicitud.getUuidFuncionario().isBlank())
            this.uuidFuncionario = tipoSolicitud.getUuidFuncionario();
    }

}
