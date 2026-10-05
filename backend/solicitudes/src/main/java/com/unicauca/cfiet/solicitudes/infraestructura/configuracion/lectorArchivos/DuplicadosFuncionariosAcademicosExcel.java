package com.unicauca.cfiet.solicitudes.infraestructura.configuracion.lectorArchivos;

import com.unicauca.cfiet.solicitudes.aplicacion.output.ExcepcionesFormateadorIntPuerto;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorFuncionariosAcademicos.DTOPeticion.FilaFuncionarioAcademicoExcelDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorFuncionariosAcademicos.DTOPeticion.FuncionarioAcademicoDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.unicauca.cfiet.solicitudes.infraestructura.configuracion.lectorArchivos.ColumnasExcelFuncionariosAcademicos.letra;

@Service
@RequiredArgsConstructor
public class DuplicadosFuncionariosAcademicosExcel {
    private static final Map<String, Function<FuncionarioAcademicoDTOPeticion, String>> COLUMNAS_UNICAS = Map.of(
            "numeroDocumento", FuncionarioAcademicoDTOPeticion::getNumeroDocumento,
            "correoElectronico", FuncionarioAcademicoDTOPeticion::getCorreoElectronico,
            "username", FuncionarioAcademicoDTOPeticion::getUsername);
    private static final List<String> ORDEN = List.of("numeroDocumento", "correoElectronico", "username");

    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;

    public void verificar(List<FilaFuncionarioAcademicoExcelDTOPeticion> filas) {
        for (String columna : ORDEN) {
            Map<String, Integer> vistos = new HashMap<>();
            for (FilaFuncionarioAcademicoExcelDTOPeticion fila : filas) {
                String valor = COLUMNAS_UNICAS.get(columna).apply(fila.getPeticion());
                Integer filaPrevia = vistos.putIfAbsent(valor.trim().toUpperCase(), fila.getNumeroFila());
                if (filaPrevia != null)
                    formateadorExcepciones.lanzarMalFormato(String.format(MensajesError.EXCEL_FILA_VALOR_DUPLICADO,
                            fila.getNumeroFila(), letra(columna), columna, valor, filaPrevia));
            }
        }
    }
}
