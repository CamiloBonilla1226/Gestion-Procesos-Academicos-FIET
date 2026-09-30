package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudes.DTORespuesta;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Getter
@Setter
@NoArgsConstructor
public class OrdenDelDiaDTORespuesta {
    private String uuidOrdenDelDia;
    private String nombre;
    private String descripcion;
    private String ciudad;
    private String fecha;
    private String horaInicio;
    private String horaFin;
    private String lugarReunion;
    private String numeroActa;
    private boolean estado;
}
