package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudesAcademicas.mapeador;

import com.unicauca.cfiet.solicitudes.aplicacion.output.ExcepcionesFormateadorIntPuerto;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudesAcademicas.DTORespuesta.EliminacionRespuestasDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Component
@RequiredArgsConstructor
public class MapperEliminacionRespuestasInfraestructuraDominio {
    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;

    public LocalDate mapearFecha(String valor, String dato) {
        if (valor == null || valor.isBlank()) return null;
        try {
            return LocalDate.parse(valor.trim());
        } catch (DateTimeParseException error) {
            formateadorExcepciones.lanzarMalFormato(String.format(MensajesError.FECHA_MAL_FORMADA, valor, dato));
            return null;
        }
    }

    public EliminacionRespuestasDTORespuesta mapearARespuesta(int cantidad, LocalDate desde, LocalDate hasta) {
        return EliminacionRespuestasDTORespuesta.builder()
                .cantidad(cantidad)
                .desde(desde)
                .hasta(hasta)
                .build();
    }
}
