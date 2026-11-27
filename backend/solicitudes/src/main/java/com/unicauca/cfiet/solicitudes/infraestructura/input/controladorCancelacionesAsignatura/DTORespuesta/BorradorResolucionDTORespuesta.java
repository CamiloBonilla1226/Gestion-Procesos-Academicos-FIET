package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesAsignatura.DTORespuesta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BorradorResolucionDTORespuesta {
    private String asignaturaSolicitudUuid;
    private String asignatura;
    private Boolean aceptada;
    private String prefijo;
    private String numero;
    private LocalDate fecha;
    private String asunto;
    private String preambulo;
    private String considerandos;
    private String articuloPrimero;
    private List<FilaResolucionDTORespuesta> filas;
    private String articulosSiguientes;
    private String firmante;
    private String cargoFirmante;
    private String diligencia;
    private String proyecto;
    private String reviso;
}
