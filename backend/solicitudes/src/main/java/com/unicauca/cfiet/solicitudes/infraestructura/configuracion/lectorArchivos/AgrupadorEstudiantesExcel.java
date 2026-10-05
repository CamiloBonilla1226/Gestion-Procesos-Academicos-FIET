package com.unicauca.cfiet.solicitudes.infraestructura.configuracion.lectorArchivos;

import com.unicauca.cfiet.solicitudes.aplicacion.output.ExcepcionesFormateadorIntPuerto;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorEstudiantes.DTOPeticion.AsignaturaMatriculadaDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorEstudiantes.DTOPeticion.EstudianteDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorEstudiantes.DTOPeticion.FilaEstudianteExcelDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static com.unicauca.cfiet.solicitudes.infraestructura.configuracion.lectorArchivos.ColumnasExcelEstudiantes.COLUMNAS;
import static com.unicauca.cfiet.solicitudes.infraestructura.configuracion.lectorArchivos.ColumnasExcelEstudiantes.letra;

@Service
@RequiredArgsConstructor
public class AgrupadorEstudiantesExcel {
    private static final List<Function<EstudianteDTOPeticion, String>> DATOS_ESTUDIANTE = List.of(
            EstudianteDTOPeticion::getNombres,
            EstudianteDTOPeticion::getApellidos,
            EstudianteDTOPeticion::getTipoDocumento,
            EstudianteDTOPeticion::getNumeroDocumento,
            EstudianteDTOPeticion::getTelefono,
            EstudianteDTOPeticion::getCorreoElectronico,
            EstudianteDTOPeticion::getUsername,
            EstudianteDTOPeticion::getPassword,
            EstudianteDTOPeticion::getCodigoEstudiantil,
            EstudianteDTOPeticion::getProgramaAcademico,
            EstudianteDTOPeticion::getSemestre,
            EstudianteDTOPeticion::getFacultad);
    private static final List<Integer> COLUMNAS_UNICAS = List.of(
            COLUMNAS.indexOf("codigoEstudiantil"),
            COLUMNAS.indexOf("correoElectronico"),
            COLUMNAS.indexOf("username"));

    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;

    public List<EstudianteDTOPeticion> agrupar(List<FilaEstudianteExcelDTOPeticion> filas) {
        Map<String, FilaEstudianteExcelDTOPeticion> primeraFila = new LinkedHashMap<>();
        Map<String, Integer> materias = new HashMap<>();

        for (FilaEstudianteExcelDTOPeticion fila : filas) {
            EstudianteDTOPeticion peticion = fila.getPeticion();
            String documento = normalizar(peticion.getNumeroDocumento());
            AsignaturaMatriculadaDTOPeticion materia = peticion.getAsignaturas().get(0);

            FilaEstudianteExcelDTOPeticion primera = primeraFila.putIfAbsent(documento, fila);
            if (primera != null)
                compararDatos(primera, fila);

            String claveMateria = documento + "|" + normalizar(materia.getCodigoAsignatura()) + "|" + normalizar(materia.getGrupo());
            Integer filaMateria = materias.putIfAbsent(claveMateria, fila.getNumeroFila());
            if (filaMateria != null)
                formateadorExcepciones.lanzarMalFormato(String.format(MensajesError.EXCEL_FILA_MATERIA_REPETIDA,
                        fila.getNumeroFila(), letra("codigoAsignatura"), "codigoAsignatura",
                        materia.getCodigoAsignatura(), materia.getGrupo(), filaMateria));

            if (primera != null)
                primera.getPeticion().getAsignaturas().add(materia);
        }

        for (int columna : COLUMNAS_UNICAS)
            validarUnico(primeraFila.values(), columna);

        List<EstudianteDTOPeticion> estudiantes = new ArrayList<>();
        for (FilaEstudianteExcelDTOPeticion fila : primeraFila.values())
            estudiantes.add(fila.getPeticion());
        return estudiantes;
    }

    private void compararDatos(FilaEstudianteExcelDTOPeticion primera, FilaEstudianteExcelDTOPeticion fila) {
        for (int i = 0; i < DATOS_ESTUDIANTE.size(); i++) {
            String esperado = DATOS_ESTUDIANTE.get(i).apply(primera.getPeticion());
            String recibido = DATOS_ESTUDIANTE.get(i).apply(fila.getPeticion());
            if (!Objects.equals(esperado, recibido))
                formateadorExcepciones.lanzarMalFormato(String.format(MensajesError.EXCEL_FILA_DATO_DISTINTO,
                        fila.getNumeroFila(), letra(i), COLUMNAS.get(i),
                        primera.getPeticion().getNumeroDocumento(), primera.getNumeroFila()));
        }
    }

    private void validarUnico(Iterable<FilaEstudianteExcelDTOPeticion> filas, int columna) {
        Map<String, Integer> vistos = new HashMap<>();
        for (FilaEstudianteExcelDTOPeticion fila : filas) {
            String valor = DATOS_ESTUDIANTE.get(columna).apply(fila.getPeticion());
            Integer filaPrevia = vistos.putIfAbsent(normalizar(valor), fila.getNumeroFila());
            if (filaPrevia != null)
                formateadorExcepciones.lanzarMalFormato(String.format(MensajesError.EXCEL_FILA_VALOR_REPETIDO,
                        fila.getNumeroFila(), letra(columna), COLUMNAS.get(columna), valor, filaPrevia));
        }
    }

    private String normalizar(String valor) {
        return valor == null ? "" : valor.trim().toUpperCase();
    }
}
