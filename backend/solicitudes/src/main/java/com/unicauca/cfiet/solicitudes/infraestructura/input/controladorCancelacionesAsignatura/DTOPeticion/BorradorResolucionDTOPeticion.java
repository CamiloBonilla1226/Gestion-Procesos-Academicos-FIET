package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesAsignatura.DTOPeticion;

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
public class BorradorResolucionDTOPeticion {
    private String asignaturaSolicitudUuid;
    private String numero;
    private LocalDate fecha;
    private String asunto;
    private String preambulo;
    private String considerandos;
    private String articuloPrimero;
    private List<FilaResolucionDTOPeticion> filas;
    private String articulosSiguientes;
    private String firmante;
    private String cargoFirmante;
    private String diligencia;
    private String proyecto;
    private String reviso;
}
