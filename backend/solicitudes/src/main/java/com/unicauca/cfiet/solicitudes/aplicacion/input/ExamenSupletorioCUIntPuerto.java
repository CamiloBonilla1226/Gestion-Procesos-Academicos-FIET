package com.unicauca.cfiet.solicitudes.aplicacion.input;

import com.unicauca.cfiet.solicitudes.dominio.modelos.AnexoRadicacion;
import com.unicauca.cfiet.solicitudes.dominio.modelos.SolicitudExamenSupletorio;

import java.time.LocalDate;
import java.util.List;

public interface ExamenSupletorioCUIntPuerto {

    SolicitudExamenSupletorio radicarExamenSupletorio(String uuidEstudiante, String uuidAsignaturaMatriculada, LocalDate fechaExamenNoPresentado,
                                                      String tipoCausa, String uuidAsignaturaCruzada, LocalDate fechaExamenCruzada,
                                                      String horaExamenCruzada, List<AnexoRadicacion> anexos, String token);
}
