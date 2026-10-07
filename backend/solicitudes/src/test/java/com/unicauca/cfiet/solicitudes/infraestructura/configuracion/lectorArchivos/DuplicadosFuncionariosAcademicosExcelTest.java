package com.unicauca.cfiet.solicitudes.infraestructura.configuracion.lectorArchivos;

import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorFuncionariosAcademicos.DTOPeticion.FilaFuncionarioAcademicoExcelDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorFuncionariosAcademicos.DTOPeticion.FuncionarioAcademicoDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.formateador.ExcepcionesFormateadorImplAdaptador;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorMalFormatoExcepcion;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class DuplicadosFuncionariosAcademicosExcelTest {

    private final DuplicadosFuncionariosAcademicosExcel duplicados =
            new DuplicadosFuncionariosAcademicosExcel(new ExcepcionesFormateadorImplAdaptador());

    private FilaFuncionarioAcademicoExcelDTOPeticion fila(int numero, String id) {
        return fila(numero, id, peticion -> {
        });
    }

    private FilaFuncionarioAcademicoExcelDTOPeticion fila(int numero, String id, Consumer<FuncionarioAcademicoDTOPeticion> ajuste) {
        FuncionarioAcademicoDTOPeticion peticion = new FuncionarioAcademicoDTOPeticion("Luis", "Gómez", "Cédula de ciudadanía",
                "9" + id, "3001234567", "fa" + id + "@unicauca.edu.co", "fa" + id, "Clave12345", "Decanatura FIET");
        ajuste.accept(peticion);
        return new FilaFuncionarioAcademicoExcelDTOPeticion(numero, peticion);
    }

    private String error(List<FilaFuncionarioAcademicoExcelDTOPeticion> filas) {
        return assertThrows(ErrorMalFormatoExcepcion.class, () -> duplicados.verificar(filas)).getMessage();
    }

    @Test
    void aceptaFilasSinValoresRepetidos() {
        assertDoesNotThrow(() -> duplicados.verificar(List.of(fila(2, "001"), fila(3, "002"), fila(4, "003"))));
    }

    @Test
    void aceptaUnArchivoVacio() {
        assertDoesNotThrow(() -> duplicados.verificar(List.of()));
    }

    @Test
    void rechazaUnDocumentoRepetidoNombrandoAmbasFilas() {
        assertEquals(String.format(MensajesError.EXCEL_FILA_VALOR_DUPLICADO, 4, "D", "numeroDocumento", "9001", 2),
                error(List.of(fila(2, "001"), fila(3, "002"), fila(4, "003", p -> p.setNumeroDocumento("9001")))));
    }

    @Test
    void rechazaUnCorreoRepetidoSinImportarMayusculasNiEspacios() {
        assertEquals(String.format(MensajesError.EXCEL_FILA_VALOR_DUPLICADO, 3, "F", "correoElectronico", " FA001@Unicauca.edu.co ", 2),
                error(List.of(fila(2, "001"), fila(3, "002", p -> p.setCorreoElectronico(" FA001@Unicauca.edu.co ")))));
    }

    @Test
    void rechazaUnUsernameRepetido() {
        assertEquals(String.format(MensajesError.EXCEL_FILA_VALOR_DUPLICADO, 3, "G", "username", "FA001", 2),
                error(List.of(fila(2, "001"), fila(3, "002", p -> p.setUsername("FA001")))));
    }

    @Test
    void revisaPrimeroElDocumentoLuegoElCorreoYLuegoElUsername() {
        List<FilaFuncionarioAcademicoExcelDTOPeticion> filas = List.of(fila(2, "001"), fila(3, "002", p -> {
            p.setUsername("fa001");
            p.setCorreoElectronico("fa001@unicauca.edu.co");
            p.setNumeroDocumento("9001");
        }));

        assertEquals(String.format(MensajesError.EXCEL_FILA_VALOR_DUPLICADO, 3, "D", "numeroDocumento", "9001", 2), error(filas));
    }

    @Test
    void permiteDatosNoUnicosRepetidosComoNombreODependencia() {
        assertDoesNotThrow(() -> duplicados.verificar(List.of(fila(2, "001"), fila(3, "002"))));
    }
}
