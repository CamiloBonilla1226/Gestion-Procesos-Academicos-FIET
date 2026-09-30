package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.RolCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.ExcepcionesFormateadorIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.RolGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Rol;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;
import java.util.List;

/**
 * Implementación de la interfaz de los casos de uso.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
public class RolCUImplAdaptador implements RolCUIntPuerto {
    private final RolGatewayIntPuerto gateway;
    private  final ExcepcionesFormateadorIntPuerto formateadorExcepciones;
    private final LogCUIntPuerto log;
    /* Constantes */
    private static final String ROLES = "roles";
    private static final String ROL = "Rol";

    public RolCUImplAdaptador(RolGatewayIntPuerto gateway, ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                              LogCUIntPuerto log){
        this.gateway = gateway;
        this.formateadorExcepciones = formateadorExcepciones;
        this.log = log;
    }

    @Override
    public List<Rol> getRoles() {
        List<Rol> roles = gateway.getRoles();
        if(roles.isEmpty())
            formateadorExcepciones.lanzarSinInformacion(String.format(MensajesError.SIN_REGISTROS, ROLES));
        return  roles;
    }

    @Override
    public List<Rol> getRoles(int pagina, int tamanio) {
        if(pagina < 0 || tamanio < 0)
            formateadorExcepciones.lanzarMalFormato(MensajesError.PAGINACION_ERROR);
        List<Rol> roles = gateway.getRoles(pagina, tamanio);
        if(roles.isEmpty())
            formateadorExcepciones.lanzarSinInformacion(String.format(MensajesError.SIN_REGISTROS, ROLES));
        return  roles;
    }

    @Override
    public Rol getRol(String uuidRol) {
        Rol rol = gateway.getRol(uuidRol);
        if(rol == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, ROL, uuidRol));
        return rol;
    }

    @Override
    public Rol actualizarRol(String uuidRol, Rol rol, String token) {
        Rol rolObtenido = gateway.getRol(uuidRol);
        if(rolObtenido == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, ROL, uuidRol));

        if(rol.getDescripcion() != null && !rol.getDescripcion().isBlank())
            rolObtenido.setDescripcion(rol.getDescripcion());

        if(rol.getEstado() != null)
            rolObtenido.setEstado(rol.getEstado());

        log.crearLog("Actualizar rol", String.format("Rol %s actualizado", rolObtenido.getNombre()), token);
        return gateway.guardarRol(rolObtenido);
    }
}
