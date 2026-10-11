package com.unicauca.cfiet.solicitudes.infraestructura.configuracion.lectorArchivos;

import com.unicauca.cfiet.solicitudes.aplicacion.input.AsignaturaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.ProgramaAcademicoCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.ExcepcionesFormateadorIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Asignatura;
import com.unicauca.cfiet.solicitudes.dominio.modelos.ProgramaAcademico;
import com.unicauca.cfiet.solicitudes.dominio.servicios.ReglasAsignatura;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorAsignaturas.DTOPeticion.AsignaturaDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorAsignaturas.DTOPeticion.FilaAsignaturaExcelDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorEstudiantes.DTOPeticion.AsignaturaMatriculadaDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorEstudiantes.DTOPeticion.EstudianteDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorEstudiantes.DTOPeticion.FilaEstudianteExcelDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CatalogoAcademicoExcel {
    private static final String PROGRAMA = "programaAcademico";
    private static final String NOMBRE_ASIGNATURA = "nombreAsignatura";
    private static final String CODIGO_ASIGNATURA = "codigoAsignatura";

    private final ProgramaAcademicoCUIntPuerto programaCU;
    private final AsignaturaCUIntPuerto asignaturaCU;
    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;

    public void resolverProgramasAsignaturas(List<FilaAsignaturaExcelDTOPeticion> filas) {
        Map<String, ProgramaAcademico> programas = programasPorNombre();
        for (FilaAsignaturaExcelDTOPeticion fila : filas)
            fila.getPeticion().setUuidProgramaAcademico(uuidPrograma(programas, fila.getProgramaAcademico(),
                    fila.getNumeroFila(), ColumnasExcelAsignaturas.letra(PROGRAMA)));
    }

    public void resolverProgramasEstudiantes(List<FilaEstudianteExcelDTOPeticion> filas) {
        Map<String, ProgramaAcademico> programas = programasPorNombre();
        for (FilaEstudianteExcelDTOPeticion fila : filas)
            fila.getPeticion().setUuidProgramaAcademico(uuidPrograma(programas, fila.getProgramaAcademico(),
                    fila.getNumeroFila(), ColumnasExcelEstudiantes.letra(PROGRAMA)));
    }

    public void verificarNombresAsignaturas(List<FilaAsignaturaExcelDTOPeticion> filas) {
        Map<String, ProgramaAcademico> programas = programasPorUuid();
        Map<String, Integer> vistos = new HashMap<>();
        Map<String, Map<String, Asignatura>> existentes = new HashMap<>();
        String letra = ColumnasExcelAsignaturas.letra(NOMBRE_ASIGNATURA);

        for (FilaAsignaturaExcelDTOPeticion fila : filas) {
            AsignaturaDTOPeticion peticion = fila.getPeticion();
            String nombre = ReglasAsignatura.normalizarNombre(peticion.getNombreAsignatura());
            String clave = ReglasAsignatura.claveNombre(nombre);
            ProgramaAcademico programa = programas.get(peticion.getUuidProgramaAcademico());

            Integer filaPrevia = vistos.putIfAbsent(programa.getUuidProgramaAcademico() + "|" + clave, fila.getNumeroFila());
            if (filaPrevia != null)
                formateadorExcepciones.lanzarMalFormato(String.format(MensajesError.EXCEL_FILA_ASIGNATURA_REPETIDA,
                        fila.getNumeroFila(), letra, NOMBRE_ASIGNATURA, nombre, filaPrevia, programa.getNombre()));

            Asignatura existente = existentes
                    .computeIfAbsent(programa.getUuidProgramaAcademico(), this::asignaturasPorNombre)
                    .get(clave);
            if (existente != null)
                formateadorExcepciones.lanzarEntidadExiste(String.format(MensajesError.EXCEL_FILA_ASIGNATURA_EXISTE,
                        fila.getNumeroFila(), letra, NOMBRE_ASIGNATURA, nombre, programa.getNombre(), existente.getCodigoAsignatura()));
        }
    }

    public void verificarAsignaturasEstudiantes(List<FilaEstudianteExcelDTOPeticion> filas) {
        Map<String, ProgramaAcademico> programas = programasPorUuid();
        Map<String, Asignatura> catalogo = new HashMap<>();
        for (ProgramaAcademico programa : programas.values())
            for (Asignatura asignatura : asignaturaCU.getAsignaturasPorPrograma(programa.getUuidProgramaAcademico()))
                catalogo.put(ReglasAsignatura.normalizarCodigo(asignatura.getCodigoAsignatura()), asignatura);
        String letra = ColumnasExcelEstudiantes.letra(CODIGO_ASIGNATURA);

        for (FilaEstudianteExcelDTOPeticion fila : filas) {
            EstudianteDTOPeticion peticion = fila.getPeticion();
            AsignaturaMatriculadaDTOPeticion materia = peticion.getAsignaturas().get(0);
            Asignatura asignatura = catalogo.get(ReglasAsignatura.normalizarCodigo(materia.getCodigoAsignatura()));
            if (asignatura == null)
                formateadorExcepciones.lanzarMalFormato(String.format(MensajesError.EXCEL_FILA_ASIGNATURA_NO_EXISTE,
                        fila.getNumeroFila(), letra, CODIGO_ASIGNATURA, materia.getCodigoAsignatura()));

            String uuidPrograma = asignatura.getProgramaAcademico().getUuidProgramaAcademico();
            if (!uuidPrograma.equals(peticion.getUuidProgramaAcademico()))
                formateadorExcepciones.lanzarMalFormato(String.format(MensajesError.EXCEL_FILA_ASIGNATURA_OTRO_PROGRAMA,
                        fila.getNumeroFila(), letra, CODIGO_ASIGNATURA, asignatura.getCodigoAsignatura(),
                        asignatura.getProgramaAcademico().getNombre(), programas.get(peticion.getUuidProgramaAcademico()).getNombre()));
        }
    }

    private String uuidPrograma(Map<String, ProgramaAcademico> programas, String nombre, int fila, String letra) {
        if (nombre == null || nombre.isBlank())
            return null;
        ProgramaAcademico programa = programas.get(clavePrograma(nombre));
        if (programa == null)
            formateadorExcepciones.lanzarMalFormato(String.format(MensajesError.EXCEL_FILA_PROGRAMA_DESCONOCIDO,
                    fila, letra, PROGRAMA, nombre, String.join(", ", programas.values().stream().map(ProgramaAcademico::getNombre).toList())));
        return programa.getUuidProgramaAcademico();
    }

    private Map<String, ProgramaAcademico> programasPorNombre() {
        Map<String, ProgramaAcademico> programas = new LinkedHashMap<>();
        for (ProgramaAcademico programa : programaCU.getProgramasAcademicos())
            programas.put(clavePrograma(programa.getNombre()), programa);
        return programas;
    }

    private Map<String, ProgramaAcademico> programasPorUuid() {
        Map<String, ProgramaAcademico> programas = new LinkedHashMap<>();
        for (ProgramaAcademico programa : programaCU.getProgramasAcademicos())
            programas.put(programa.getUuidProgramaAcademico(), programa);
        return programas;
    }

    private Map<String, Asignatura> asignaturasPorNombre(String uuidPrograma) {
        Map<String, Asignatura> asignaturas = new HashMap<>();
        for (Asignatura asignatura : asignaturaCU.getAsignaturasPorPrograma(uuidPrograma))
            asignaturas.put(ReglasAsignatura.claveNombre(asignatura.getNombreAsignatura()), asignatura);
        return asignaturas;
    }

    private String clavePrograma(String nombre) {
        return nombre.trim().toLowerCase(Locale.ROOT);
    }
}
