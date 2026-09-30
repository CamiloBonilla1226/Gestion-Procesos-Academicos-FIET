package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorLog.controlador;

import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.ApplicationConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Log;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorLog.DTORespuesta.LogDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorLog.mapeador.MapperLogInfraestructuraDominio;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@RestController
@RequestMapping("${url.application}logs")
@CrossOrigin(origins = "${url.frontend}")
@Validated
@RequiredArgsConstructor
@Tag(name = "Logs", description = "Operaciones relacionadas con la gestión de logs.")
public class LogRestController {
    private final LogCUIntPuerto casoDeUso;
    private final MapperLogInfraestructuraDominio mapper;

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_ACCESO)
    @GetMapping
    public ResponseEntity<List<LogDTORespuesta>> index(){
        List<Log> logs = casoDeUso.getLogs();
        return new ResponseEntity<List<LogDTORespuesta>>(
                mapper.mapearModelosARespuesta(logs), HttpStatus.OK
        );
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_ACCESO)
    @GetMapping("/paginado")
    public ResponseEntity<?> indexPaginado(@RequestParam("pagina") int pagina, @RequestParam("tamanio") int tamanio){
        var respuesta = casoDeUso.getLogs(pagina, tamanio);
        return new ResponseEntity<>(
                new PaginacionRespuestaDTO<>(
                        mapper.mapearModelosARespuesta(respuesta.getContent()),
                        respuesta.getTotalElements()
                ),
                HttpStatus.OK
        );
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_ACCESO)
    @GetMapping("/filtro")
    public ResponseEntity<?> filtrarLogs(
            @RequestParam(value = "responsable", required = false) String responsable,
            @RequestParam(value = "fecha", required = false) String fecha,
            @RequestParam("pagina") int pagina,
            @RequestParam("tamanio") int tamanio) {
        var respuesta = casoDeUso.getLogs(responsable, fecha, pagina, tamanio);
        return new ResponseEntity<>(
                new PaginacionRespuestaDTO<>(
                        mapper.mapearModelosARespuesta(respuesta.getContent()),
                        respuesta.getTotalElements()
                ),
                HttpStatus.OK
        );
    }
}
