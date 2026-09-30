package com.unicauca.cfiet.solicitudes.infraestructura.output.exportador;

import com.unicauca.cfiet.solicitudes.aplicacion.output.OrdenDelDiaExportador;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Servicio para exportar la información de una Orden del Día a un documento Word
 * utilizando una plantilla predefinida.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Service
public class OrdenDelDiaExportadorImpl implements OrdenDelDiaExportador {

    @Override
    public byte[] exportarOrdenDelDia(HashMap<String, String> data, String plantilla) {
        try (
                InputStream is = new ClassPathResource(plantilla).getInputStream();
                XWPFDocument document = new XWPFDocument(is);
                ByteArrayOutputStream baos = new ByteArrayOutputStream()
        ) {

            for (XWPFParagraph paragraph : document.getParagraphs())
                reemplazarTexto(paragraph, data);

            for (XWPFTable table : document.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        for (XWPFParagraph paragraph : cell.getParagraphs())
                            reemplazarTexto(paragraph, data);
                    }
                }
            }

            document.write(baos);
            return baos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Error generando el documento Word", e);
        }
    }

    private void reemplazarTexto(XWPFParagraph paragraph, Map<String, String> datos) {
        for (XWPFRun run : paragraph.getRuns()) {
            String texto = run.getText(0);
            if (texto == null) continue;

            for (Map.Entry<String, String> entry : datos.entrySet()) {
                if (!texto.contains(entry.getKey())) continue;

                run.setText("", 0);

                String valor = entry.getValue();
                if (valor == null || valor.isEmpty()) return;

                String[] lineas = valor.split("\n");

                for (int i = 0; i < lineas.length; i++) {
                    run.setText(lineas[i]);
                    if (i < lineas.length - 1)
                        run.addBreak();
                }
                return;
            }
        }
    }

}
