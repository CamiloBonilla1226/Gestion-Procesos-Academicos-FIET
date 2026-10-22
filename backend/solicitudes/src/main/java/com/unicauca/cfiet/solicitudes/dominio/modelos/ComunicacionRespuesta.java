package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComunicacionRespuesta {
    private Boolean enviarMensaje;
    private String mensaje;
    private String textoComunicacion;
    private String textoDarca;
    private ProgramacionResolucion programacion;
}
