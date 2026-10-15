package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorNotificacionesDocente.controlador;

import com.unicauca.cfiet.solicitudes.aplicacion.input.NotificacionDocenteCUIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.ApplicationConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.ArchivoAdjunto;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorNotificacionesDocente.DTORespuesta.NotificacionDocenteDetalleDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorNotificacionesDocente.DTORespuesta.NotificacionDocenteResumenDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorNotificacionesDocente.mapeador.MapperNotificacionDocenteInfraestructuraDominio;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("${url.application}notificaciones-docente")
@CrossOrigin(origins = "${url.frontend}")
@RequiredArgsConstructor
@Tag(name = "Notificaciones del docente", description = "Exámenes supletorios asignados al docente autenticado, solo lectura.")
public class NotificacionDocenteRestController {
    private static final String UUID_NOTIFICACION = "/{uuidNotificacion:[0-9a-fA-F\\-]{36}}";
    private static final String UUID_ANEXO = "/{uuidAnexo:[0-9a-fA-F\\-]{36}}";

    private final NotificacionDocenteCUIntPuerto casoDeUso;
    private final MapperNotificacionDocenteInfraestructuraDominio mapper;

    @PreAuthorize(ApplicationConstantes.DOCENTE_ACCESO)
    @Transactional(readOnly = true)
    @GetMapping
    public ResponseEntity<List<NotificacionDocenteResumenDTORespuesta>> getMisNotificaciones(@RequestHeader("Authorization") String token) {
        return new ResponseEntity<>(mapper.mapearListaARespuesta(casoDeUso.getMisNotificaciones(token.substring(7))), HttpStatus.OK);
    }

    @PreAuthorize(ApplicationConstantes.DOCENTE_ACCESO)
    @Transactional(readOnly = true)
    @GetMapping(UUID_NOTIFICACION)
    public ResponseEntity<NotificacionDocenteDetalleDTORespuesta> getNotificacion(@PathVariable String uuidNotificacion,
                                                                                 @RequestHeader("Authorization") String token) {
        return new ResponseEntity<>(mapper.mapearDetalleARespuesta(casoDeUso.getNotificacion(uuidNotificacion, token.substring(7))), HttpStatus.OK);
    }

    @PreAuthorize(ApplicationConstantes.DOCENTE_ACCESO)
    @Transactional
    @PatchMapping(UUID_NOTIFICACION + "/leida")
    public ResponseEntity<NotificacionDocenteDetalleDTORespuesta> marcarLeida(@PathVariable String uuidNotificacion,
                                                                             @RequestHeader("Authorization") String token) {
        return new ResponseEntity<>(mapper.mapearDetalleARespuesta(casoDeUso.marcarLeida(uuidNotificacion, token.substring(7))), HttpStatus.OK);
    }

    @PreAuthorize(ApplicationConstantes.DOCENTE_ACCESO)
    @Transactional(readOnly = true)
    @GetMapping(UUID_NOTIFICACION + "/anexos" + UUID_ANEXO)
    public ResponseEntity<ByteArrayResource> descargarAnexo(@PathVariable String uuidNotificacion,
                                                            @PathVariable String uuidAnexo,
                                                            @RequestHeader("Authorization") String token) {
        ArchivoAdjunto archivo = casoDeUso.descargarAnexo(uuidNotificacion, uuidAnexo, token.substring(7));
        ByteArrayResource recurso = new ByteArrayResource(archivo.getContenido());
        String tipo = archivo.getTipoContenido() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : archivo.getTipoContenido();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(archivo.getNombreOriginal(), StandardCharsets.UTF_8).build().toString())
                .contentType(MediaType.parseMediaType(tipo))
                .contentLength(recurso.contentLength())
                .body(recurso);
    }
}
