package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper;

import com.unicauca.cfiet.solicitudes.dominio.modelos.InformacionSolicitante;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Respuesta;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Solicitud;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.RespuestaEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.SolicitudEntidad;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Service
@RequiredArgsConstructor
public class RespuestaOwnMapper implements OwnMapper<Respuesta, RespuestaEntidad> {
    @Override
    public Respuesta toDominio(RespuestaEntidad source) {
        Respuesta respuesta = Respuesta.builder()
                .uuidRespuesta(source.getUuidRespuesta())
                .tipoRespuesta(source.getTipoRespuesta())
                .consecutivoFiet(source.getConsecutivoFiet())
                .respuestaConsejo(source.getRespuestaConsejo())
                .indicaciones(source.getIndicaciones())
                .urlRespuesta(source.getUrlRespuesta())
                .build();

        Solicitud solicitud = toDominio(source.getSolicitud(), respuesta);
        respuesta.setSolicitud(solicitud);
        return respuesta;
    }

    @Override
    public RespuestaEntidad toEntidad(Respuesta source) {
        RespuestaEntidad respuesta = RespuestaEntidad.builder()
                .uuidRespuesta(source.getUuidRespuesta())
                .tipoRespuesta(source.getTipoRespuesta())
                .consecutivoFiet(source.getConsecutivoFiet())
                .respuestaConsejo(source.getRespuestaConsejo())
                .indicaciones(source.getIndicaciones())
                .urlRespuesta(source.getUrlRespuesta())
                .build();

        SolicitudEntidad solicitud = toEntidad(source.getSolicitud());
        respuesta.setSolicitud(solicitud);
        return respuesta;
    }

    private Solicitud toDominio(SolicitudEntidad source, Respuesta respuesta){
        return Solicitud.builder()
                .uuidSolicitud(source.getUuidSolicitud())
                .consecutivo(source.getConsecutivo())
                .nombre(source.getNombre())
                .descripcion(source.getDescripcion())
                .estado(source.getEstado())
                .informacionSolicitante(
                        InformacionSolicitante.builder()
                                .uuidInformacionSolicitante(source.getInformacionSolicitante().getUuidInformacionSolicitante())
                                .tipoDocumento(source.getInformacionSolicitante().getTipoDocumento())
                                .numeroDocumento(source.getInformacionSolicitante().getNumeroDocumento())
                                .nombres(source.getInformacionSolicitante().getNombres())
                                .apellidos(source.getInformacionSolicitante().getApellidos())
                                .correoElectronico(source.getInformacionSolicitante().getCorreoElectronico())
                                .telefono(source.getInformacionSolicitante().getTelefono())
                                .build()
                )
                .respuesta(respuesta)
                .build();
    }

    private SolicitudEntidad toEntidad(Solicitud source) {
        return SolicitudEntidad.builder()
                .uuidSolicitud(source.getUuidSolicitud())
                .build();
    }

}
