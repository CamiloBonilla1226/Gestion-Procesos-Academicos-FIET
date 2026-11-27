package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorTiposSolicitudConsejo.mapeador;

import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.RemisionConsejoConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.EncargadoTipoSolicitud;
import com.unicauca.cfiet.solicitudes.dominio.modelos.FuncionarioAcademico;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoSolicitudConsejo;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorTiposSolicitudConsejo.DTORespuesta.EncargadoTipoSolicitudDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorTiposSolicitudConsejo.DTORespuesta.TipoSolicitudConsejoDTORespuesta;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MapperTipoSolicitudConsejoInfraestructuraDominio {

    public List<EncargadoTipoSolicitudDTORespuesta> mapearEncargados(List<EncargadoTipoSolicitud> encargados) {
        return encargados.stream().map(this::mapearEncargado).toList();
    }

    public TipoSolicitudConsejoDTORespuesta mapearEnlace(TipoSolicitudConsejo enlace) {
        return TipoSolicitudConsejoDTORespuesta.builder()
                .uuidTipoSolicitud(enlace.getUuidTipoSolicitud())
                .procesoAcademicoUuid(enlace.getTipoSolicitudAcademica() == null ? null : enlace.getTipoSolicitudAcademica().getUuidTipoSolicitudAcademica())
                .procesoAcademicoNombre(enlace.getTipoSolicitudAcademica() == null ? null : enlace.getTipoSolicitudAcademica().getNombre())
                .encargadoAcademico(mapearAcademico(enlace.getEncargadoAcademico()))
                .build();
    }

    private EncargadoTipoSolicitudDTORespuesta mapearEncargado(EncargadoTipoSolicitud encargado) {
        return EncargadoTipoSolicitudDTORespuesta.builder()
                .uuidUsuario(encargado.getUuidUsuario())
                .nombres(encargado.getNombres())
                .apellidos(encargado.getApellidos())
                .tipo(encargado.getTipo())
                .build();
    }

    private EncargadoTipoSolicitudDTORespuesta mapearAcademico(FuncionarioAcademico academico) {
        if (academico == null)
            return null;
        return EncargadoTipoSolicitudDTORespuesta.builder()
                .uuidUsuario(academico.getUuidUsuario())
                .nombres(academico.getUsuario() == null ? null : academico.getUsuario().getNombres())
                .apellidos(academico.getUsuario() == null ? null : academico.getUsuario().getApellidos())
                .tipo(RemisionConsejoConstantes.ENCARGADO_FUNCIONARIO_ACADEMICO)
                .build();
    }
}
