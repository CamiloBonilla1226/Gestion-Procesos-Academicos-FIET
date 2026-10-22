package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesAsignatura.DTORespuesta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlantillaNotificacionResolucionDTORespuesta {
    private String mensajeInicial;
    private Integer maximoMensaje;
    private String decisionPropuesta;
    private Integer anioResolucion;
    private String sufijoResolucion;
    private String textoComunicacion;
    private String textoDarca;
    private Integer maximoTexto;
    private String nombreEstudiante;
}
