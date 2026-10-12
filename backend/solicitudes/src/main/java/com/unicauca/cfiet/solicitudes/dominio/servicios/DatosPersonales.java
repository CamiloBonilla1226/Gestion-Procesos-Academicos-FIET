package com.unicauca.cfiet.solicitudes.dominio.servicios;

import com.unicauca.cfiet.solicitudes.aplicacion.output.DatosPersonalesGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.ExcepcionesFormateadorIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Usuario;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;

public class DatosPersonales {
    private static final String USUARIO = "Usuario";
    private static final String NUMERO_DOCUMENTO = "numero de documento";
    private static final String CORREO_ELECTRONICO = "correo electronico";

    private final DatosPersonalesGatewayIntPuerto gateway;
    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;

    public DatosPersonales(DatosPersonalesGatewayIntPuerto gateway, ExcepcionesFormateadorIntPuerto formateadorExcepciones) {
        this.gateway = gateway;
        this.formateadorExcepciones = formateadorExcepciones;
    }

    public Usuario cambiosValidados(String uuidUsuario, Usuario recibido) {
        Usuario cambios = new Usuario();
        if (recibido == null) return cambios;
        cambios.setNombres(texto(recibido.getNombres()));
        cambios.setApellidos(texto(recibido.getApellidos()));
        cambios.setTelefono(texto(recibido.getTelefono()));

        String tipoDocumento = texto(recibido.getTipoDocumento());
        if (tipoDocumento != null) {
            cambios.setTipoDocumento(tipoDocumento);
            if (!cambios.revisarTipoDocumento())
                formateadorExcepciones.lanzarMalFormato(MensajesError.TIPO_DOCUMENTO_ERRONEO);
        }

        String numeroDocumento = texto(recibido.getNumeroDocumento());
        if (numeroDocumento != null && gateway.existeNumeroDocumentoEnOtro(numeroDocumento, uuidUsuario))
            formateadorExcepciones.lanzarEntidadExiste(String.format(
                    MensajesError.ATRIBUTO_UNICO_YA_EXISTE, USUARIO, NUMERO_DOCUMENTO, numeroDocumento));
        cambios.setNumeroDocumento(numeroDocumento);

        String correo = texto(recibido.getCorreoElectronico());
        if (correo != null && gateway.existeCorreoEnOtro(correo, uuidUsuario))
            formateadorExcepciones.lanzarEntidadExiste(String.format(
                    MensajesError.ATRIBUTO_UNICO_YA_EXISTE, USUARIO, CORREO_ELECTRONICO, correo));
        cambios.setCorreoElectronico(correo);
        return cambios;
    }

    public void guardar(String uuidUsuario, Usuario actual, Usuario cambios) {
        if (!hayCambios(cambios)) return;
        gateway.actualizar(uuidUsuario, cambios);
        if (actual == null) return;
        if (cambios.getNombres() != null) actual.setNombres(cambios.getNombres());
        if (cambios.getApellidos() != null) actual.setApellidos(cambios.getApellidos());
        if (cambios.getTipoDocumento() != null) actual.setTipoDocumento(cambios.getTipoDocumento());
        if (cambios.getNumeroDocumento() != null) actual.setNumeroDocumento(cambios.getNumeroDocumento());
        if (cambios.getTelefono() != null) actual.setTelefono(cambios.getTelefono());
        if (cambios.getCorreoElectronico() != null) actual.setCorreoElectronico(cambios.getCorreoElectronico());
    }

    private boolean hayCambios(Usuario cambios) {
        return cambios.getNombres() != null || cambios.getApellidos() != null || cambios.getTipoDocumento() != null
                || cambios.getNumeroDocumento() != null || cambios.getTelefono() != null || cambios.getCorreoElectronico() != null;
    }

    private String texto(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
