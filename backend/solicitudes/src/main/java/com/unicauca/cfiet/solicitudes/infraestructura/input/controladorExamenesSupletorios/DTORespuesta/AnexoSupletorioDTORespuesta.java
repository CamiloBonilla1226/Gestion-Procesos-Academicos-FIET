package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorExamenesSupletorios.DTORespuesta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnexoSupletorioDTORespuesta {
    private String uuidTipoAnexoAcademico;
    private String nombre;
    private String formatosPermitidos;
    private Long tamanioMaximoBytes;
    @Builder.Default
    private List<String> causas = new ArrayList<>();
}
