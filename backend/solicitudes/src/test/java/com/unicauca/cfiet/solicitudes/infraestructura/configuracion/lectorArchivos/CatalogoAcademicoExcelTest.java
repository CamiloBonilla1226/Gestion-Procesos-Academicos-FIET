package com.unicauca.cfiet.solicitudes.infraestructura.configuracion.lectorArchivos;

import com.unicauca.cfiet.solicitudes.aplicacion.input.AsignaturaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.ProgramaAcademicoCUIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Asignatura;
import com.unicauca.cfiet.solicitudes.dominio.modelos.ProgramaAcademico;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorAsignaturas.DTOPeticion.AsignaturaDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorAsignaturas.DTOPeticion.FilaAsignaturaExcelDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorEstudiantes.DTOPeticion.AsignaturaMatriculadaDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorEstudiantes.DTOPeticion.EstudianteDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorEstudiantes.DTOPeticion.FilaEstudianteExcelDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.formateador.ExcepcionesFormateadorImplAdaptador;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorEntidadExisteExcepcion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorMalFormatoExcepcion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogoAcademicoExcelTest {

    private static final ProgramaAcademico SISTEMAS = ProgramaAcademico.builder()
            .uuidProgramaAcademico("uuid-isi").sigla("ISI").nombre("Ingeniería de Sistemas").build();
    private static final ProgramaAcademico TELEMATICA = ProgramaAcademico.builder()
            .uuidProgramaAcademico("uuid-tte").sigla("TTE").nombre("Tecnología en Telemática").build();

    @Mock
    private ProgramaAcademicoCUIntPuerto programaCU;

    @Mock
    private AsignaturaCUIntPuerto asignaturaCU;

    private CatalogoAcademicoExcel catalogo;

    @BeforeEach
    void setUp() {
        catalogo = new CatalogoAcademicoExcel(programaCU, asignaturaCU, new ExcepcionesFormateadorImplAdaptador());
        when(programaCU.getProgramasAcademicos()).thenReturn(List.of(SISTEMAS, TELEMATICA));
        lenient().when(asignaturaCU.getAsignaturasPorPrograma("uuid-isi")).thenReturn(List.of(
                asignatura("ISI-001", "Calculo I", SISTEMAS)));
        lenient().when(asignaturaCU.getAsignaturasPorPrograma("uuid-tte")).thenReturn(List.of(
                asignatura("TTE-001", "Redes I", TELEMATICA)));
    }

    private Asignatura asignatura(String codigo, String nombre, ProgramaAcademico programa) {
        return Asignatura.builder().uuidAsignatura("a-" + codigo).codigoAsignatura(codigo)
                .nombreAsignatura(nombre).programaAcademico(programa).build();
    }

    private FilaAsignaturaExcelDTOPeticion filaAsignatura(int numero, String nombre, String programa) {
        return new FilaAsignaturaExcelDTOPeticion(numero, programa, new AsignaturaDTOPeticion(nombre, null));
    }

    private FilaEstudianteExcelDTOPeticion filaEstudiante(int numero, String programa, String codigoAsignatura) {
        EstudianteDTOPeticion peticion = new EstudianteDTOPeticion("Ana", "Perez", "Cedula", "1001", "3001234567",
                "a@unicauca.edu.co", "ana1001", "Clave12345", "COD1001", null, "5", "FIET",
                new ArrayList<>(List.of(new AsignaturaMatriculadaDTOPeticion(codigoAsignatura, "A"))));
        return new FilaEstudianteExcelDTOPeticion(numero, programa, peticion);
    }

    @Test
    void resuelveElProgramaPorNombreSinDistinguirMayusculasNiEspacios() {
        List<FilaAsignaturaExcelDTOPeticion> filas = List.of(filaAsignatura(2, "Fisica", "  ingeniería DE sistemas "));

        catalogo.resolverProgramasAsignaturas(filas);

        assertEquals("uuid-isi", filas.get(0).getPeticion().getUuidProgramaAcademico());
    }

    @Test
    void unProgramaVacioQuedaParaElValidador() {
        List<FilaAsignaturaExcelDTOPeticion> filas = List.of(filaAsignatura(2, "Fisica", " "));

        catalogo.resolverProgramasAsignaturas(filas);

        assertNull(filas.get(0).getPeticion().getUuidProgramaAcademico());
    }

    @Test
    void unProgramaDesconocidoDaErrorConFilaYColumna() {
        ErrorMalFormatoExcepcion error = assertThrows(ErrorMalFormatoExcepcion.class,
                () -> catalogo.resolverProgramasAsignaturas(List.of(filaAsignatura(2, "Fisica", "Ingeniería de Sistemas"),
                        filaAsignatura(3, "Quimica", "Medicina"))));

        assertEquals(String.format(MensajesError.EXCEL_FILA_PROGRAMA_DESCONOCIDO, 3, "B", "programaAcademico", "Medicina",
                "Ingeniería de Sistemas, Tecnología en Telemática"), error.getMessage());
    }

    @Test
    void unProgramaDesconocidoEnEstudiantesUsaLaColumnaJ() {
        ErrorMalFormatoExcepcion error = assertThrows(ErrorMalFormatoExcepcion.class,
                () -> catalogo.resolverProgramasEstudiantes(List.of(filaEstudiante(4, "Sistemas", "ISI-001"))));

        assertTrue(error.getMessage().startsWith("Fila 4, columna J (programaAcademico)"));
    }

    @Test
    void unNombreRepetidoEnElArchivoParaElMismoProgramaDaErrorConLaFilaPrevia() {
        List<FilaAsignaturaExcelDTOPeticion> filas = List.of(filaAsignatura(2, "Fisica I", "Ingeniería de Sistemas"),
                filaAsignatura(3, "Fisica I", "Tecnología en Telemática"),
                filaAsignatura(4, " FISICA   i", "Ingeniería de Sistemas"));
        catalogo.resolverProgramasAsignaturas(filas);

        ErrorMalFormatoExcepcion error = assertThrows(ErrorMalFormatoExcepcion.class, () -> catalogo.verificarNombresAsignaturas(filas));

        assertEquals(String.format(MensajesError.EXCEL_FILA_ASIGNATURA_REPETIDA, 4, "A", "nombreAsignatura", "FISICA i", 2,
                "Ingeniería de Sistemas"), error.getMessage());
    }

    @Test
    void unNombreQueYaExisteEnElProgramaDaEntidadExisteConFilaYColumna() {
        List<FilaAsignaturaExcelDTOPeticion> filas = List.of(filaAsignatura(2, "Redes I", "Ingeniería de Sistemas"),
                filaAsignatura(3, "calculo i", "Ingeniería de Sistemas"));
        catalogo.resolverProgramasAsignaturas(filas);

        ErrorEntidadExisteExcepcion error = assertThrows(ErrorEntidadExisteExcepcion.class, () -> catalogo.verificarNombresAsignaturas(filas));

        assertEquals(String.format(MensajesError.EXCEL_FILA_ASIGNATURA_EXISTE, 3, "A", "nombreAsignatura", "calculo i",
                "Ingeniería de Sistemas", "ISI-001"), error.getMessage());
    }

    @Test
    void nombresNuevosNoDanError() {
        List<FilaAsignaturaExcelDTOPeticion> filas = List.of(filaAsignatura(2, "Fisica I", "Ingeniería de Sistemas"),
                filaAsignatura(3, "Calculo I", "Tecnología en Telemática"));
        catalogo.resolverProgramasAsignaturas(filas);

        assertDoesNotThrow(() -> catalogo.verificarNombresAsignaturas(filas));
    }

    @Test
    void unaAsignaturaInexistenteEnEstudiantesDaErrorEnLaColumnaM() {
        List<FilaEstudianteExcelDTOPeticion> filas = List.of(filaEstudiante(2, "Ingeniería de Sistemas", "ISI-001"),
                filaEstudiante(3, "Ingeniería de Sistemas", "ISI-404"));
        catalogo.resolverProgramasEstudiantes(filas);

        ErrorMalFormatoExcepcion error = assertThrows(ErrorMalFormatoExcepcion.class, () -> catalogo.verificarAsignaturasEstudiantes(filas));

        assertEquals(String.format(MensajesError.EXCEL_FILA_ASIGNATURA_NO_EXISTE, 3, "M", "codigoAsignatura", "ISI-404"), error.getMessage());
    }

    @Test
    void unaAsignaturaDeOtroProgramaEnEstudiantesDaErrorEnLaColumnaM() {
        List<FilaEstudianteExcelDTOPeticion> filas = List.of(filaEstudiante(2, "Ingeniería de Sistemas", "tte-001"));
        catalogo.resolverProgramasEstudiantes(filas);

        ErrorMalFormatoExcepcion error = assertThrows(ErrorMalFormatoExcepcion.class, () -> catalogo.verificarAsignaturasEstudiantes(filas));

        assertEquals(String.format(MensajesError.EXCEL_FILA_ASIGNATURA_OTRO_PROGRAMA, 2, "M", "codigoAsignatura", "TTE-001",
                "Tecnología en Telemática", "Ingeniería de Sistemas"), error.getMessage());
    }

    @Test
    void asignaturasDelProgramaDelEstudianteNoDanError() {
        List<FilaEstudianteExcelDTOPeticion> filas = List.of(filaEstudiante(2, "Tecnología en Telemática", "TTE-001"));
        catalogo.resolverProgramasEstudiantes(filas);

        assertDoesNotThrow(() -> catalogo.verificarAsignaturasEstudiantes(filas));
    }
}
