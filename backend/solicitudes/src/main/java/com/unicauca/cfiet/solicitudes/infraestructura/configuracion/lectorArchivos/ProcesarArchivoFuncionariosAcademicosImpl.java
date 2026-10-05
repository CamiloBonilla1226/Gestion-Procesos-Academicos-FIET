package com.unicauca.cfiet.solicitudes.infraestructura.configuracion.lectorArchivos;

import com.unicauca.cfiet.solicitudes.aplicacion.output.ExcepcionesFormateadorIntPuerto;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorFuncionariosAcademicos.DTOPeticion.FilaFuncionarioAcademicoExcelDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorFuncionariosAcademicos.DTOPeticion.FuncionarioAcademicoDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.unicauca.cfiet.solicitudes.infraestructura.configuracion.lectorArchivos.ColumnasExcelFuncionariosAcademicos.COLUMNAS;
import static com.unicauca.cfiet.solicitudes.infraestructura.configuracion.lectorArchivos.ColumnasExcelFuncionariosAcademicos.letra;

@Service("archivos-funcionarios-academicos")
public class ProcesarArchivoFuncionariosAcademicosImpl implements ProcesadorArchivos<FilaFuncionarioAcademicoExcelDTOPeticion> {
    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;
    private final DataFormatter formato = new DataFormatter();

    public ProcesarArchivoFuncionariosAcademicosImpl(ExcepcionesFormateadorIntPuerto formateadorExcepciones) {
        this.formateadorExcepciones = formateadorExcepciones;
    }

    @Override
    public List<FilaFuncionarioAcademicoExcelDTOPeticion> procesarArchivo(MultipartFile file) {
        String nombre = file == null ? null : file.getOriginalFilename();
        if (file == null || file.isEmpty() || nombre == null || !nombre.toLowerCase().endsWith(".xlsx"))
            formateadorExcepciones.lanzarMalFormato(MensajesError.EXCEL_NO_VALIDO);

        try (Workbook workbook = abrir(file)) {
            if (workbook.getNumberOfSheets() == 0)
                formateadorExcepciones.lanzarMalFormato(MensajesError.EXCEL_NO_VALIDO);
            Sheet hoja = workbook.getSheetAt(0);
            validarEncabezado(hoja.getRow(0));

            List<FilaFuncionarioAcademicoExcelDTOPeticion> filas = new ArrayList<>();
            for (int i = 1; i <= hoja.getLastRowNum(); i++) {
                Row fila = hoja.getRow(i);
                if (filaVacia(fila)) break;
                filas.add(new FilaFuncionarioAcademicoExcelDTOPeticion(i + 1, leerFila(fila)));
            }
            if (filas.isEmpty())
                formateadorExcepciones.lanzarMalFormato(MensajesError.EXCEL_SIN_DATOS);
            return filas;
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cerrar el archivo de funcionarios académicos", e);
        }
    }

    private Workbook abrir(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            return new XSSFWorkbook(is);
        } catch (Exception e) {
            formateadorExcepciones.lanzarMalFormato(MensajesError.EXCEL_NO_VALIDO);
            return null;
        }
    }

    private void validarEncabezado(Row encabezado) {
        for (int i = 0; i < COLUMNAS.size(); i++) {
            String valor = encabezado == null ? "" : valor(encabezado, i);
            if (!COLUMNAS.get(i).equalsIgnoreCase(valor))
                formateadorExcepciones.lanzarMalFormato(String.format(
                        MensajesError.EXCEL_ENCABEZADO_INVALIDO, letra(i), COLUMNAS.get(i), valor));
        }
    }

    private FuncionarioAcademicoDTOPeticion leerFila(Row fila) {
        return new FuncionarioAcademicoDTOPeticion(
                valor(fila, 0), valor(fila, 1), valor(fila, 2), valor(fila, 3), valor(fila, 4),
                valor(fila, 5), valor(fila, 6), valor(fila, 7), valor(fila, 8));
    }

    private String valor(Row fila, int columna) {
        Cell celda = fila.getCell(columna);
        return celda == null ? "" : formato.formatCellValue(celda).trim();
    }

    private boolean filaVacia(Row fila) {
        if (fila == null) return true;
        for (int i = 0; i < Math.max(fila.getLastCellNum(), COLUMNAS.size()); i++) {
            if (!valor(fila, i).isEmpty())
                return false;
        }
        return true;
    }
}
