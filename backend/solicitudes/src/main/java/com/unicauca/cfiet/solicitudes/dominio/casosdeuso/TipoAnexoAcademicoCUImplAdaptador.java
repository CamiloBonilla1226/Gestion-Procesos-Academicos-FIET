package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.TipoAnexoAcademicoCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.ExcepcionesFormateadorIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.TipoAnexoAcademicoGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.TipoSolicitudAcademicaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoAnexoAcademico;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;

import java.util.List;

public class TipoAnexoAcademicoCUImplAdaptador implements TipoAnexoAcademicoCUIntPuerto {
    private final TipoAnexoAcademicoGatewayIntPuerto gateway;
    private final TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway;
    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;
    private static final String TIPO_SOLICITUD_ACADEMICA = "Tipo de solicitud académica";

    public TipoAnexoAcademicoCUImplAdaptador(TipoAnexoAcademicoGatewayIntPuerto gateway,
                                             TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway,
                                             ExcepcionesFormateadorIntPuerto formateadorExcepciones) {
        this.gateway = gateway;
        this.tipoSolicitudGateway = tipoSolicitudGateway;
        this.formateadorExcepciones = formateadorExcepciones;
    }

    @Override
    public List<TipoAnexoAcademico> getTiposAnexoPorTipo(String uuidTipoSolicitudAcademica) {
        if (!tipoSolicitudGateway.existePorUuid(uuidTipoSolicitudAcademica))
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(
                    MensajesError.ENTIDAD_NO_ENCONTRADA, TIPO_SOLICITUD_ACADEMICA, uuidTipoSolicitudAcademica));
        return gateway.getPorTipo(uuidTipoSolicitudAcademica);
    }
}
