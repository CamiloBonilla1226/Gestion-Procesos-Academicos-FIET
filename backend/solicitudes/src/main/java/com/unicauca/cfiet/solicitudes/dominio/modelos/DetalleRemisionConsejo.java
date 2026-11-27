package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleRemisionConsejo {
    private String uuidSolicitud;
    private SolicitudCancelacionAsignatura cancelacion;
    private OrdenDelDia ordenDelDia;
    private Respuesta respuesta;
}
