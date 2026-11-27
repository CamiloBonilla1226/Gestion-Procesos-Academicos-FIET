package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorTiposSolicitudConsejo.controlador;

import com.unicauca.cfiet.solicitudes.aplicacion.input.TipoSolicitudConsejoCUIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.ApplicationConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoSolicitudConsejo;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorTiposSolicitudConsejo.DTORespuesta.EncargadoTipoSolicitudDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorTiposSolicitudConsejo.DTORespuesta.TipoSolicitudConsejoDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorTiposSolicitudConsejo.mapeador.MapperTipoSolicitudConsejoInfraestructuraDominio;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${url.application}catalogos-academicos/tipos-solicitud-consejo")
@CrossOrigin(origins = "${url.frontend}")
@RequiredArgsConstructor
@Tag(name = "Tipo de solicitud del Consejo", description = "Enlace del tipo de solicitud de la orden del día con Cancelación de Asignatura y candidatos a encargado.")
public class TipoSolicitudConsejoRestController {
    private final TipoSolicitudConsejoCUIntPuerto casoDeUso;
    private final MapperTipoSolicitudConsejoInfraestructuraDominio mapper;

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_ACCESO)
    @Transactional(readOnly = true)
    @GetMapping("/encargados")
    public ResponseEntity<List<EncargadoTipoSolicitudDTORespuesta>> getEncargados(
            @RequestParam(value = "procesoAcademico", required = false) String procesoAcademico) {
        return new ResponseEntity<>(mapper.mapearEncargados(casoDeUso.getEncargados(procesoAcademico)), HttpStatus.OK);
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_ACCESO)
    @Transactional(readOnly = true)
    @GetMapping("/{uuidTipoSolicitud:[0-9a-fA-F\\-]{36}}")
    public ResponseEntity<TipoSolicitudConsejoDTORespuesta> getEnlace(@PathVariable String uuidTipoSolicitud) {
        TipoSolicitudConsejo enlace = casoDeUso.getEnlace(uuidTipoSolicitud);
        if (enlace == null)
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        return new ResponseEntity<>(mapper.mapearEnlace(enlace), HttpStatus.OK);
    }
}
