package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.SesionCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.ExcepcionesFormateadorIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.SesionGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Rol;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Usuario;
import com.unicauca.cfiet.solicitudes.dominio.modelos.UsuarioTokenizado;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;

/**
 * Implementación de la interfaz de los casos de uso para sesiones.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
public class SesionCUImplAdaptador implements SesionCUIntPuerto{
    private final SesionGatewayIntPuerto gateway;
    private final LogCUIntPuerto log;
    private  final ExcepcionesFormateadorIntPuerto formateadorExcepciones;

    public SesionCUImplAdaptador(SesionGatewayIntPuerto gateway,
                                 LogCUIntPuerto log,
                                 ExcepcionesFormateadorIntPuerto formateadorExcepciones){
        this.gateway = gateway;
        this.log = log;
        this.formateadorExcepciones = formateadorExcepciones;
    }

    @Override
    public UsuarioTokenizado login(String username, String contraseña) {
        String token = gateway.login(username, contraseña);
        if(token == null)
            formateadorExcepciones.lanzarCredencialesErroneas(MensajesError.CREDENCIALES_ERRONEAS);
        Usuario usuario = gateway.getUsuario(username);
        if(!usuario.getEstado())
            formateadorExcepciones.lanzarSinAcceso(MensajesError.NO_ACCESO);

        for(Rol rol: usuario.getRoles()){
            if(!rol.getEstado())
                formateadorExcepciones.lanzarSinAcceso(MensajesError.ROL_NO_HABILITADO);
        }

        log.crearLogSesion("Inicio de sesión", String.format("Usuario %s ha iniciado sesión", username), username);
        return new UsuarioTokenizado(usuario.getUuidUsuario(), token);
    }
}
