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
public class DatosFormatoOficial {
    private TipoProcesoAcademico proceso;
    private String radicado;
    private LocalDate fecha;
    private String nombre;
    private String codigo;
    private String programa;
    private String facultad;
    private String semestre;
    private String cedula;
    private String correo;
    private String telefono;
    private String motivo;
    @Builder.Default
    private List<FilaFormatoOficial> asignaturas = new ArrayList<>();
    private String asignatura;
    private String codigoAsignatura;
    private String grupoAsignatura;
    private String fechaExamen;
    private String documentos;
    private String cruceAsignatura;
    private String cruceCodigo;
    private String cruceGrupo;
    private String cruceFecha;
    private String cruceHora;
    private boolean datosFacultad;
    private String revisionAcademica;
    private Boolean decisionDecano;
}
