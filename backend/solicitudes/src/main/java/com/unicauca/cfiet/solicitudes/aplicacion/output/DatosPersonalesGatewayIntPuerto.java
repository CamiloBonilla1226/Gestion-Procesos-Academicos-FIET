package com.unicauca.cfiet.solicitudes.aplicacion.output;

import com.unicauca.cfiet.solicitudes.dominio.modelos.Usuario;

public interface DatosPersonalesGatewayIntPuerto {

    boolean existeNumeroDocumentoEnOtro(String numeroDocumento, String uuidUsuario);

    boolean existeCorreoEnOtro(String correoElectronico, String uuidUsuario);

    void actualizar(String uuidUsuario, Usuario cambios);
}
