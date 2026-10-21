package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConstanciaComunicacion {
    private String radicado;
    private String destinatario;
    private String asunto;
    private LocalDateTime fechaEnvio;
    @Builder.Default
    private List<List<FragmentoTexto>> parrafos = new ArrayList<>();
}
