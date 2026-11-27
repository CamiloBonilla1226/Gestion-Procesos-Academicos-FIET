package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorradorResolucion {
    private String uuidAsignaturaSolicitud;
    private String asignatura;
    private Boolean aceptada;
    private String prefijo;
    private String numero;
    private LocalDate fecha;
    private String asunto;
    private String preambulo;
    private String considerandos;
    private String articuloPrimero;
    @Builder.Default
    private List<FilaResolucion> filas = new ArrayList<>();
    private String articulosSiguientes;
    private String firmante;
    private String cargoFirmante;
    private String diligencia;
    private String proyecto;
    private String reviso;
}
