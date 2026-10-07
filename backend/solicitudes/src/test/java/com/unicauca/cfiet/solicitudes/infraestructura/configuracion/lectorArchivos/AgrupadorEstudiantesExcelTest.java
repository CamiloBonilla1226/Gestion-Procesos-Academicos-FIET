package com.unicauca.cfiet.solicitudes.infraestructura.configuracion.lectorArchivos;

import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorEstudiantes.DTOPeticion.AsignaturaMatriculadaDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorEstudiantes.DTOPeticion.EstudianteDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorEstudiantes.DTOPeticion.FilaEstudianteExcelDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.formateador.ExcepcionesFormateadorImplAdaptador;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorMalFormatoExcepcion;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class AgrupadorEstudiantesExcelTest {

    private final AgrupadorEstudiantesExcel agrupador = new AgrupadorEstudiantesExcel(new ExcepcionesFormateadorImplAdaptador());

    private FilaEstudianteExcelDTOPeticion fila(int numero, String documento, String codigoAsignatura, String grupo) {
        return fila(numero, documento, codigoAsignatura, grupo, peticion -> {
        });
    }

    private FilaEstudianteExcelDTOPeticion fila(int numero, String documento, String codigoAsignatura, String grupo,
                                                Consumer<EstudianteDTOPeticion> ajuste) {
        EstudianteDTOPeticion peticion = new EstudianteDTOPeticion("Ana", "Pérez", "Cédula de ciudadanía", documento, "3001234567",
                "est" + documento + "@unicauca.edu.co", "est" + documento, "Clave12345", "COD" + documento,
                "Ingeniería de Sistemas", "5", "FIET",
                new ArrayList<>(List.of(new AsignaturaMatriculadaDTOPeticion(codigoAsignatura, "Asignatura " + codigoAsignatura, grupo))));
        ajuste.accept(peticion);
        return new FilaEstudianteExcelDTOPeticion(numero, peticion);
    }

    private List<String> materias(EstudianteDTOPeticion estudiante) {
        return estudiante.getAsignaturas().stream().map(a -> a.getCodigoAsignatura() + "-" + a.getGrupo()).toList();
    }

    private String error(List<FilaEstudianteExcelDTOPeticion> filas) {
        return assertThrows(ErrorMalFormatoExcepcion.class, () -> agrupador.agrupar(filas)).getMessage();
    }

    @Test
    void juntaLasFilasDelMismoDocumentoEnUnEstudianteConTodasSusMaterias() {
        List<EstudianteDTOPeticion> estudiantes = agrupador.agrupar(List.of(
                fila(2, "1001", "MAT1", "A"),
                fila(3, "2002", "FIS1", "B"),
                fila(4, "1001", "FIS1", "A"),
                fila(5, "1001", "MAT1", "B")));

        assertEquals(2, estudiantes.size());
        assertEquals("1001", estudiantes.get(0).getNumeroDocumento());
        assertEquals(List.of("MAT1-A", "FIS1-A", "MAT1-B"), materias(estudiantes.get(0)));
        assertEquals("2002", estudiantes.get(1).getNumeroDocumento());
        assertEquals(List.of("FIS1-B"), materias(estudiantes.get(1)));
    }

    @Test
    void rechazaUnaFilaDelMismoEstudianteConUnDatoDistinto() {
        assertEquals(String.format(MensajesError.EXCEL_FILA_DATO_DISTINTO, 4, "A", "nombres", "1001", 2),
                error(List.of(fila(2, "1001", "MAT1", "A"), fila(3, "2002", "MAT1", "A"),
                        fila(4, "1001", "FIS1", "A", p -> p.setNombres("Andrea")))));
    }

    @Test
    void comparaTambienLosDatosAcademicosDelEstudiante() {
        assertEquals(String.format(MensajesError.EXCEL_FILA_DATO_DISTINTO, 3, "K", "semestre", "1001", 2),
                error(List.of(fila(2, "1001", "MAT1", "A"), fila(3, "1001", "FIS1", "A", p -> p.setSemestre("6")))));
    }

    @Test
    void rechazaLaMismaMateriaYGrupoDosVecesParaElMismoEstudiante() {
        assertEquals(String.format(MensajesError.EXCEL_FILA_MATERIA_REPETIDA, 4, "M", "codigoAsignatura", "mat1", " a ", 2),
                error(List.of(fila(2, "1001", "MAT1", "A"), fila(3, "1001", "FIS1", "A"), fila(4, "1001", "mat1", " a "))));
    }

    @Test
    void permiteLaMismaMateriaEnOtroGrupoOEnOtroEstudiante() {
        List<EstudianteDTOPeticion> estudiantes = agrupador.agrupar(List.of(
                fila(2, "1001", "MAT1", "A"), fila(3, "1001", "MAT1", "B"), fila(4, "2002", "MAT1", "A")));

        assertEquals(List.of("MAT1-A", "MAT1-B"), materias(estudiantes.get(0)));
        assertEquals(List.of("MAT1-A"), materias(estudiantes.get(1)));
    }

    @Test
    void rechazaElMismoCodigoEstudiantilEnDosEstudiantes() {
        assertEquals(String.format(MensajesError.EXCEL_FILA_VALOR_REPETIDO, 3, "I", "codigoEstudiantil", "cod1001", 2),
                error(List.of(fila(2, "1001", "MAT1", "A"), fila(3, "2002", "MAT1", "A", p -> p.setCodigoEstudiantil("cod1001")))));
    }

    @Test
    void rechazaElMismoCorreoEnDosEstudiantesSinImportarMayusculas() {
        assertEquals(String.format(MensajesError.EXCEL_FILA_VALOR_REPETIDO, 3, "F", "correoElectronico", "EST1001@unicauca.edu.co", 2),
                error(List.of(fila(2, "1001", "MAT1", "A"), fila(3, "2002", "MAT1", "A",
                        p -> p.setCorreoElectronico("EST1001@unicauca.edu.co")))));
    }

    @Test
    void rechazaElMismoUsernameEnDosEstudiantes() {
        assertEquals(String.format(MensajesError.EXCEL_FILA_VALOR_REPETIDO, 3, "G", "username", "est1001 ", 2),
                error(List.of(fila(2, "1001", "MAT1", "A"), fila(3, "2002", "MAT1", "A", p -> p.setUsername("est1001 ")))));
    }

    @Test
    void unArchivoConUnSoloEstudianteNoTieneRepetidos() {
        List<EstudianteDTOPeticion> estudiantes = agrupador.agrupar(List.of(fila(2, "1001", "MAT1", "A")));

        assertEquals(1, estudiantes.size());
        assertEquals(List.of("MAT1-A"), materias(estudiantes.get(0)));
    }
}
