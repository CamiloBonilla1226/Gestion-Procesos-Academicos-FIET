package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.TipoSolicitudAcademicaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.TipoSolicitudAcademicaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoSolicitudAcademica;

import java.util.List;

public class TipoSolicitudAcademicaCUImplAdaptador implements TipoSolicitudAcademicaCUIntPuerto {
    private final TipoSolicitudAcademicaGatewayIntPuerto gateway;

    public TipoSolicitudAcademicaCUImplAdaptador(TipoSolicitudAcademicaGatewayIntPuerto gateway) {
        this.gateway = gateway;
    }

    @Override
    public List<TipoSolicitudAcademica> getTiposSolicitudAcademica() {
        return gateway.getTodos();
    }
}
