package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorreoPreparado {
    private String destinatario;
    private String asunto;
    private String texto;
    private String html;
    @Builder.Default
    private List<List<FragmentoTexto>> parrafos = new ArrayList<>();
    @Builder.Default
    private List<AdjuntoCorreo> adjuntos = new ArrayList<>();
}
