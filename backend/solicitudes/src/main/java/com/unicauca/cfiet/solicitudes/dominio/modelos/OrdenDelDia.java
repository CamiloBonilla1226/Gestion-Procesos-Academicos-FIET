package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

/**
 * Representa un Orden del Día, que incluye información sobre la reunión.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenDelDia {
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

    /**
     * Actualiza los campos de esta instancia con los valores de otra instancia
     * de OrdenDelDia. No modifica la instancia si el objeto proporcionado es null.
     *
     * @param other la instancia de OrdenDelDia de la cual copiar los valores
     */
    public void actualizar(OrdenDelDia other) {
        if (other == null) return;
        this.nombre = other.nombre;
        this.descripcion = other.descripcion;
        this.ciudad = other.ciudad;
        this.fecha = other.fecha;
        this.horaInicio = other.horaInicio;
        this.horaFin = other.horaFin;
        this.lugarReunion = other.lugarReunion;
        this.numeroActa = other.numeroActa;
        this.estado = other.estado;
    }
}
