package com.unicauca.cfiet.solicitudes.aplicacion.output;

import com.unicauca.cfiet.solicitudes.dominio.modelos.AdjuntoCorreo;

import java.util.List;

public interface CorreoIntPuerto {
    void enviar(String destinatario, String asunto, String texto, List<AdjuntoCorreo> adjuntos);
}
