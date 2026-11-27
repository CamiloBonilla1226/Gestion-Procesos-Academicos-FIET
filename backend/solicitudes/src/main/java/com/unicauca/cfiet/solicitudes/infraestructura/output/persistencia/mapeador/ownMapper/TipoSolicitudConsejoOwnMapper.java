package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper;

import com.unicauca.cfiet.solicitudes.dominio.modelos.FuncionarioAcademico;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoSolicitudAcademica;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoSolicitudConsejo;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.TipoSolicitudConsejoEntidad;
import org.springframework.stereotype.Service;

@Service
public class TipoSolicitudConsejoOwnMapper implements OwnMapper<TipoSolicitudConsejo, TipoSolicitudConsejoEntidad> {

    @Override
    public TipoSolicitudConsejo toDominio(TipoSolicitudConsejoEntidad source) {
        if (source == null) return null;
        return TipoSolicitudConsejo.builder()
                .uuidTipoSolicitud(source.getUuidTipoSolicitud())
                .tipoSolicitudAcademica(source.getTipoSolicitudAcademica() == null ? null : TipoSolicitudAcademica.builder()
                        .uuidTipoSolicitudAcademica(source.getTipoSolicitudAcademica().getUuidTipoSolicitudAcademica())
                        .nombre(source.getTipoSolicitudAcademica().getNombre())
                        .build())
                .encargadoAcademico(source.getEncargadoAcademico() == null ? null : FuncionarioAcademico.builder()
                        .uuidUsuario(source.getEncargadoAcademico().getUuidUsuario())
                        .build())
                .build();
    }

    @Override
    public TipoSolicitudConsejoEntidad toEntidad(TipoSolicitudConsejo source) {
        if (source == null) return null;
        return TipoSolicitudConsejoEntidad.builder()
                .uuidTipoSolicitud(source.getUuidTipoSolicitud())
                .build();
    }
}
