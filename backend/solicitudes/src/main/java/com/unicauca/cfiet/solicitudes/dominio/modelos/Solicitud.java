package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;
import java.util.List;

/**
 * Representa una solicitud registrada en el sistema.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Solicitud {
    private String uuidSolicitud;
    private String consecutivo;
    private String nombre;
    private String descripcion;
    private String estado;
    private TipoSolicitud objTipoSolicitud;
    private String uuidTipoSolicitud;
    private List<Anexo> anexos;
    private OrdenDelDia objOrdenDelDia;
    private String uuidOrdenDelDia;
    private InformacionSolicitante informacionSolicitante;
    private Funcionario objFuncionario;
    private String uuidFuncionario;
    private Respuesta respuesta;

    /**
     * Actualiza los campos básicos de la solicitud con los valores de otra instancia.
     *
     * @param other otra instancia de Solicitud cuyos valores se copiarán
     */
    public void actualizar(Solicitud other){
        if (other == null) return;
        this.consecutivo = other.consecutivo;
        this.nombre = other.nombre;
        this.descripcion = other.descripcion;
        this.estado = other.estado;
    }
}
