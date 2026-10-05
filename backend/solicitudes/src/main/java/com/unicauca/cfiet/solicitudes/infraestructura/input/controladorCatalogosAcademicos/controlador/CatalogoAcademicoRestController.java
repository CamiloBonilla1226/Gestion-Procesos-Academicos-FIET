package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCatalogosAcademicos.controlador;

import com.unicauca.cfiet.solicitudes.aplicacion.input.EtapaSolicitudAcademicaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.SituacionAcademicaAsignaturaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.TipoAnexoAcademicoCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.TipoSolicitudAcademicaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.ApplicationConstantes;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCatalogosAcademicos.DTORespuesta.EtapaEtiquetaRolDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCatalogosAcademicos.DTORespuesta.EtapaSolicitudAcademicaDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCatalogosAcademicos.DTORespuesta.SituacionAcademicaAsignaturaDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCatalogosAcademicos.DTORespuesta.TipoAnexoAcademicoDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCatalogosAcademicos.DTORespuesta.TipoSolicitudAcademicaDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCatalogosAcademicos.mapeador.MapperCatalogoAcademicoInfraestructuraDominio;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${url.application}catalogos-academicos")
@CrossOrigin(origins = "${url.frontend}")
@RequiredArgsConstructor
@Tag(name = "Catálogos académicos", description = "Consulta de tipos de solicitud académica, etapas, etiquetas por rol, tipos de anexo y situaciones académicas.")
public class CatalogoAcademicoRestController {
    private static final String UUID_TIPO = "/{uuidTipo:[0-9a-fA-F\\-]{36}}";

    private final TipoSolicitudAcademicaCUIntPuerto tipoSolicitudCU;
    private final EtapaSolicitudAcademicaCUIntPuerto etapaCU;
    private final TipoAnexoAcademicoCUIntPuerto tipoAnexoCU;
    private final SituacionAcademicaAsignaturaCUIntPuerto situacionCU;
    private final MapperCatalogoAcademicoInfraestructuraDominio mapper;

    @PreAuthorize(ApplicationConstantes.AUTHENTICATED)
    @GetMapping("/tipos-solicitud")
    public ResponseEntity<List<TipoSolicitudAcademicaDTORespuesta>> getTiposSolicitud() {
        return new ResponseEntity<>(mapper.mapearTiposSolicitud(tipoSolicitudCU.getTiposSolicitudAcademica()), HttpStatus.OK);
    }

    @PreAuthorize(ApplicationConstantes.AUTHENTICATED)
    @GetMapping("/tipos-solicitud" + UUID_TIPO + "/etapas")
    public ResponseEntity<List<EtapaSolicitudAcademicaDTORespuesta>> getEtapasPorTipo(@PathVariable String uuidTipo) {
        return new ResponseEntity<>(mapper.mapearEtapas(etapaCU.getEtapasPorTipo(uuidTipo)), HttpStatus.OK);
    }

    @PreAuthorize(ApplicationConstantes.AUTHENTICATED)
    @GetMapping("/tipos-solicitud" + UUID_TIPO + "/tipos-anexo")
    public ResponseEntity<List<TipoAnexoAcademicoDTORespuesta>> getTiposAnexoPorTipo(@PathVariable String uuidTipo) {
        return new ResponseEntity<>(mapper.mapearTiposAnexo(tipoAnexoCU.getTiposAnexoPorTipo(uuidTipo)), HttpStatus.OK);
    }

    @PreAuthorize(ApplicationConstantes.AUTHENTICATED)
    @GetMapping("/etiquetas")
    public ResponseEntity<List<EtapaEtiquetaRolDTORespuesta>> getEtiquetasPorRol(@RequestParam("rol") String rol) {
        return new ResponseEntity<>(mapper.mapearEtiquetas(etapaCU.getEtiquetasPorRol(rol)), HttpStatus.OK);
    }

    @PreAuthorize(ApplicationConstantes.AUTHENTICATED)
    @GetMapping("/situaciones")
    public ResponseEntity<List<SituacionAcademicaAsignaturaDTORespuesta>> getSituaciones() {
        return new ResponseEntity<>(mapper.mapearSituaciones(situacionCU.getSituaciones()), HttpStatus.OK);
    }
}
