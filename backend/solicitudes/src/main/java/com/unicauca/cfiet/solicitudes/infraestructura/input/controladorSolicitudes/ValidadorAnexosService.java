package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudes;

import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudes.DTOPeticion.AnexoDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorMalFormatoExcepcion;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ValidadorAnexosService {
    /**
     * Valida los anexos y asigna los archivos a cada anexo según su nombre.
     *
     * @param anexos  Lista de anexos de la solicitud
     * @param archivos Map de archivos subidos, donde la key es el nombre del anexo
     * @throws IllegalArgumentException si hay duplicados o faltan archivos
     */
    public void validarYAsignarArchivos(List<AnexoDTOPeticion> anexos, List<MultipartFile> archivos) {

        if (anexos == null || anexos.isEmpty())
            throw new ErrorMalFormatoExcepcion("La solicitud debe contener al menos un anexo");

        if (archivos == null || archivos.isEmpty())
            throw new ErrorMalFormatoExcepcion("No se han enviado archivos.");

        if (archivos.size() != anexos.size())
            throw new ErrorMalFormatoExcepcion("El número de archivos no coincide con el número de anexos.");

        Set<String> nombresUnicos = new HashSet<>();
        for (AnexoDTOPeticion anexo : anexos) {
            if (!nombresUnicos.add(anexo.getNombre()))
                throw new ErrorMalFormatoExcepcion("Anexo duplicado encontrado: " + anexo.getNombre());
        }

        for (int i = 0; i < anexos.size(); i++)
            anexos.get(i).setAnexoFile(archivos.get(i));
    }
}
