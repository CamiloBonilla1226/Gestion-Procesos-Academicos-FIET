package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdjuntoCorreo {
    private String nombre;
    private String tipoContenido;
    private String urlArchivo;
    private byte[] contenido;
}
