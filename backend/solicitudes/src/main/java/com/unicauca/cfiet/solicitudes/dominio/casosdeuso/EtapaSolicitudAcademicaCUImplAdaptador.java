package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.EtapaSolicitudAcademicaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.EtapaEtiquetaRolGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.EtapaSolicitudAcademicaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.ExcepcionesFormateadorIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.TipoSolicitudAcademicaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.EtapaEtiquetaRol;
import com.unicauca.cfiet.solicitudes.dominio.modelos.EtapaSolicitudAcademica;
import com.unicauca.cfiet.solicitudes.dominio.modelos.RolEtiquetaEtapa;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EtapaSolicitudAcademicaCUImplAdaptador implements EtapaSolicitudAcademicaCUIntPuerto {
    private final EtapaSolicitudAcademicaGatewayIntPuerto gateway;
    private final EtapaEtiquetaRolGatewayIntPuerto etiquetaGateway;
    private final TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway;
    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;
    private static final String TIPO_SOLICITUD_ACADEMICA = "Tipo de solicitud académica";

    public EtapaSolicitudAcademicaCUImplAdaptador(EtapaSolicitudAcademicaGatewayIntPuerto gateway,
                                                  EtapaEtiquetaRolGatewayIntPuerto etiquetaGateway,
                                                  TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway,
                                                  ExcepcionesFormateadorIntPuerto formateadorExcepciones) {
        this.gateway = gateway;
        this.etiquetaGateway = etiquetaGateway;
        this.tipoSolicitudGateway = tipoSolicitudGateway;
        this.formateadorExcepciones = formateadorExcepciones;
    }

    @Override
    public List<EtapaSolicitudAcademica> getEtapasPorTipo(String uuidTipoSolicitudAcademica) {
        if (!tipoSolicitudGateway.existePorUuid(uuidTipoSolicitudAcademica))
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(
                    MensajesError.ENTIDAD_NO_ENCONTRADA, TIPO_SOLICITUD_ACADEMICA, uuidTipoSolicitudAcademica));
        return gateway.getPorTipoIncluyendoUniversales(uuidTipoSolicitudAcademica);
    }

    @Override
    public List<EtapaEtiquetaRol> getEtiquetasPorRol(String rol) {
        return etiquetaGateway.getPorRol(convertirRol(rol));
    }

    private RolEtiquetaEtapa convertirRol(String rol) {
        String valor = rol == null ? "" : rol.trim().toUpperCase();
        for (RolEtiquetaEtapa posible : RolEtiquetaEtapa.values()) {
            if (posible.name().equals(valor))
                return posible;
        }
        formateadorExcepciones.lanzarMalFormato(String.format(MensajesError.ROL_ETIQUETA_NO_VALIDO, rol,
                Arrays.stream(RolEtiquetaEtapa.values()).map(Enum::name).collect(Collectors.joining(", "))));
        return null;
    }
}
