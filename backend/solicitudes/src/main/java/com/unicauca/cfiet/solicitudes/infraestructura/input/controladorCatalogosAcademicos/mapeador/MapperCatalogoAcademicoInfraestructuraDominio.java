package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCatalogosAcademicos.mapeador;

import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.AnexoAcademicoConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.EtapaEtiquetaRol;
import com.unicauca.cfiet.solicitudes.dominio.modelos.EtapaSolicitudAcademica;
import com.unicauca.cfiet.solicitudes.dominio.modelos.FuncionarioAcademico;
import com.unicauca.cfiet.solicitudes.dominio.modelos.ProgramaAcademico;
import com.unicauca.cfiet.solicitudes.dominio.modelos.SituacionAcademicaAsignatura;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoAnexoAcademico;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoSolicitudAcademica;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCatalogosAcademicos.DTORespuesta.EtapaEtiquetaRolDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCatalogosAcademicos.DTORespuesta.EtapaSolicitudAcademicaDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCatalogosAcademicos.DTORespuesta.ProgramaAcademicoDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCatalogosAcademicos.DTORespuesta.SituacionAcademicaAsignaturaDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCatalogosAcademicos.DTORespuesta.TipoAnexoAcademicoDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCatalogosAcademicos.DTORespuesta.TipoSolicitudAcademicaDTORespuesta;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MapperCatalogoAcademicoInfraestructuraDominio {

    public List<TipoSolicitudAcademicaDTORespuesta> mapearTiposSolicitud(List<TipoSolicitudAcademica> tipos) {
        return tipos.stream().map(this::mapearTipoSolicitud).toList();
    }

    public List<EtapaSolicitudAcademicaDTORespuesta> mapearEtapas(List<EtapaSolicitudAcademica> etapas) {
        return etapas.stream()
                .map(etapa -> EtapaSolicitudAcademicaDTORespuesta.builder()
                        .uuidEtapa(etapa.getUuidEtapa())
                        .codigo(etapa.getCodigo())
                        .uuidTipoSolicitudAcademica(uuidTipo(etapa.getTipoSolicitudAcademica()))
                        .build())
                .toList();
    }

    public List<EtapaEtiquetaRolDTORespuesta> mapearEtiquetas(List<EtapaEtiquetaRol> etiquetas) {
        return etiquetas.stream()
                .map(etiqueta -> EtapaEtiquetaRolDTORespuesta.builder()
                        .uuidEtapa(etiqueta.getEtapa() == null ? null : etiqueta.getEtapa().getUuidEtapa())
                        .codigoEtapa(etiqueta.getEtapa() == null ? null : etiqueta.getEtapa().getCodigo())
                        .rol(etiqueta.getRol() == null ? null : etiqueta.getRol().name())
                        .etiqueta(etiqueta.getEtiqueta())
                        .build())
                .toList();
    }

    public List<TipoAnexoAcademicoDTORespuesta> mapearTiposAnexo(List<TipoAnexoAcademico> tiposAnexo) {
        return tiposAnexo.stream()
                .map(tipoAnexo -> TipoAnexoAcademicoDTORespuesta.builder()
                        .uuidTipoAnexoAcademico(tipoAnexo.getUuidTipoAnexoAcademico())
                        .uuidTipoSolicitudAcademica(uuidTipo(tipoAnexo.getTipoSolicitudAcademica()))
                        .nombre(tipoAnexo.getNombre())
                        .formatosPermitidos(tipoAnexo.getFormatosPermitidos())
                        .obligatorio(tipoAnexo.getObligatorio())
                        .vigente(tipoAnexo.estaVigente())
                        .tamanioMaximoBytes(AnexoAcademicoConstantes.tamanioMaximoDe(tipoAnexo.getNombre()))
                        .build())
                .toList();
    }

    public List<ProgramaAcademicoDTORespuesta> mapearProgramas(List<ProgramaAcademico> programas) {
        return programas.stream()
                .map(programa -> ProgramaAcademicoDTORespuesta.builder()
                        .uuidProgramaAcademico(programa.getUuidProgramaAcademico())
                        .nombre(programa.getNombre())
                        .sigla(programa.getSigla())
                        .build())
                .toList();
    }

    public List<SituacionAcademicaAsignaturaDTORespuesta> mapearSituaciones(List<SituacionAcademicaAsignatura> situaciones) {
        return situaciones.stream()
                .map(situacion -> SituacionAcademicaAsignaturaDTORespuesta.builder()
                        .uuidSituacionAcademica(situacion.getUuidSituacionAcademica())
                        .codigo(situacion.getCodigo())
                        .nombre(situacion.getNombre())
                        .build())
                .toList();
    }

    public TipoSolicitudAcademicaDTORespuesta mapearTipoSolicitud(TipoSolicitudAcademica tipo) {
        FuncionarioAcademico funcionario = tipo.getFuncionarioAcademico();
        String nombreFuncionario = funcionario == null || funcionario.getUsuario() == null ? null
                : funcionario.getUsuario().getNombres() + " " + funcionario.getUsuario().getApellidos();
        return TipoSolicitudAcademicaDTORespuesta.builder()
                .uuidTipoSolicitudAcademica(tipo.getUuidTipoSolicitudAcademica())
                .nombre(tipo.getNombre())
                .descripcion(tipo.getDescripcion())
                .uuidFuncionarioAcademico(funcionario == null ? null : funcionario.getUuidUsuario())
                .nombreFuncionarioAcademico(nombreFuncionario)
                .build();
    }

    private String uuidTipo(TipoSolicitudAcademica tipo) {
        return tipo == null ? null : tipo.getUuidTipoSolicitudAcademica();
    }
}
