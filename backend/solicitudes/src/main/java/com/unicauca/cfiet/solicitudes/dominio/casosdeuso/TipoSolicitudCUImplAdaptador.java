package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.TipoSolicitudCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.ExcepcionesFormateadorIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.RolGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.TipoSolicitudGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.UsuarioGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;
import java.util.List;
import java.util.UUID;

/**
 * Implementación de la interfaz de los casos de uso para la gestión de Tipos de Solicitud.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
public class TipoSolicitudCUImplAdaptador implements TipoSolicitudCUIntPuerto {
    private final TipoSolicitudGatewayIntPuerto gateway;
    private final UsuarioGatewayIntPuerto gatewayUsuario;
    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;
    private final LogCUIntPuerto log;
    private final RolGatewayIntPuerto rolGateway;
    /* Constantes */
    private static final String TIPOS_SOLICITUD = "tipos de solicitud";
    private static final String TIPO_SOLICITUD = "tipo de solicitud";
    private static final String USUARIO = "usuario";

    public TipoSolicitudCUImplAdaptador(TipoSolicitudGatewayIntPuerto gateway,
                                        UsuarioGatewayIntPuerto gatewayUsuario,
                                        ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                        LogCUIntPuerto log,
                                        RolGatewayIntPuerto rolGateway){
        this.gateway = gateway;
        this.gatewayUsuario = gatewayUsuario;
        this.formateadorExcepciones = formateadorExcepciones;
        this.log = log;
        this.rolGateway = rolGateway;
    }

    @Override
    public List<TipoSolicitud> getTiposSolicitud() {
        List<TipoSolicitud> tipoSolicitudes = gateway.getTiposSolicitudes();
        if(tipoSolicitudes.isEmpty())
            formateadorExcepciones.lanzarSinInformacion(String.format(MensajesError.SIN_REGISTROS, TIPOS_SOLICITUD));
        return  tipoSolicitudes;
    }

    @Override
    public PaginacionRespuestaDTO<TipoSolicitud> getTiposSolicitud(int pagina, int tamanio) {
        if (pagina < 0 || tamanio < 0)
            formateadorExcepciones.lanzarMalFormato(MensajesError.PAGINACION_ERROR);
        PaginacionRespuestaDTO<TipoSolicitud> respuesta = gateway.getTiposSolicitudes(pagina, tamanio);
        if (respuesta.getContent().isEmpty())
            formateadorExcepciones.lanzarSinInformacion(String.format(MensajesError.SIN_REGISTROS, TIPOS_SOLICITUD));
        return respuesta;
    }

    @Override
    public PaginacionRespuestaDTO<TipoSolicitud> getTiposSolicitud(String nombreSolicitud, String funcionario, int pagina, int tamanio){
        if (pagina < 0 || tamanio < 0)
            formateadorExcepciones.lanzarMalFormato(MensajesError.PAGINACION_ERROR);
        PaginacionRespuestaDTO<TipoSolicitud> respuesta = gateway.getTiposSolicitudes(nombreSolicitud, funcionario, pagina, tamanio);
        if (respuesta.getContent().isEmpty())
            formateadorExcepciones.lanzarSinInformacion(String.format(MensajesError.SIN_REGISTROS, TIPOS_SOLICITUD));
        return respuesta;
    }

    @Override
    public TipoSolicitud getTipoSolicitud(String uuidTipoSolicitud) {
        TipoSolicitud tipoSolicitud = gateway.getTipoSolicitud(uuidTipoSolicitud);
        if(tipoSolicitud == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, TIPOS_SOLICITUD, uuidTipoSolicitud));
        return tipoSolicitud;
    }

    @Override
    public TipoSolicitud crearTipoSolicitud(TipoSolicitud tipoSolicitud, String token) {
        asignarFuncionario(tipoSolicitud, tipoSolicitud.getUuidFuncionario());
        tipoSolicitud.setUuidTipoSolicitud(UUID.randomUUID().toString());
        if(tipoSolicitud.getAnexos() != null){
            if(!tipoSolicitud.revisarSeccion())
                formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.SECCION_NO_EXISTENTE);
            if(!tipoSolicitud.revisarPerfilSolicitante(rolGateway.getRoles()))
                formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.PERFIL_SOLICITANTE_NO_VALIDO);
            if(!tipoSolicitud.revisarAnexos())
                formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.MAL_FORMATO_ANEXO);

            for(TipoAnexo anexo : tipoSolicitud.getAnexos()) {
                anexo.setUuidTipoAnexo(UUID.randomUUID().toString());
                anexo.setObjTipoSolicitud(tipoSolicitud);
            }
        }
        log.crearLog("Crear Tipo de Solicitud", String.format("Tipo de Solicitud %s con uuid %s",tipoSolicitud.getNombre(), tipoSolicitud.getUuidTipoSolicitud()),token);
        return gateway.guardarTipoSolicitud(tipoSolicitud);
    }

    @Override
    public TipoSolicitud actualizarTipoSolicitud(String uuidTipoSolicitud, TipoSolicitud tipoSolicitud, String token) {
        TipoSolicitud tipoSolicitudObtenida = gateway.getTipoSolicitud(uuidTipoSolicitud);

        if(tipoSolicitudObtenida == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, TIPO_SOLICITUD, uuidTipoSolicitud));
        if(tipoSolicitudObtenida.getObjFuncionarioEncargado() != null && (tipoSolicitud.getUuidFuncionario() == null ||  tipoSolicitud.getUuidFuncionario().isBlank())){
            tipoSolicitudObtenida.setObjFuncionarioEncargado(null);
        }
        if(tipoSolicitudObtenida.getObjFuncionarioEncargado() == null && tipoSolicitud.getUuidFuncionario() != null && !tipoSolicitud.getUuidFuncionario().isBlank())
            asignarFuncionario(tipoSolicitud, tipoSolicitud.getUuidFuncionario());
        if(tipoSolicitudObtenida.getObjFuncionarioEncargado() != null && !tipoSolicitudObtenida.getObjFuncionarioEncargado().getUuidUsuario().equals(tipoSolicitud.getUuidFuncionario())
                && tipoSolicitud.getUuidFuncionario() != null && !tipoSolicitud.getUuidFuncionario().isBlank())
            asignarFuncionario(tipoSolicitud, tipoSolicitud.getUuidFuncionario());

        tipoSolicitudObtenida.actualizarTipoSolicitud(tipoSolicitud);
        if(!tipoSolicitud.revisarSeccion())
            formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.SECCION_NO_EXISTENTE);
        if(!tipoSolicitud.revisarPerfilSolicitante(rolGateway.getRoles()))
            formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.PERFIL_SOLICITANTE_NO_VALIDO);
        if(!tipoSolicitud.revisarAnexos())
            formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.MAL_FORMATO_ANEXO);
        log.crearLog("Actualizar Tipo Solicitud", "Tipo de Solicitud Actualizado con exito!", token);
        return gateway.guardarTipoSolicitud(tipoSolicitudObtenida);
    }

    @Override
    public List<TipoSolicitud> crearTiposSolicitud(List<TipoSolicitud> tiposSolicitud, String token) {
        for(TipoSolicitud tipoSolicitud: tiposSolicitud){
            asignarFuncionario(tipoSolicitud, tipoSolicitud.getUuidFuncionario());
            tipoSolicitud.setUuidTipoSolicitud(UUID.randomUUID().toString());
            if(tipoSolicitud.getAnexos() != null){
                if(!tipoSolicitud.revisarSeccion())
                    formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.SECCION_NO_EXISTENTE);
                if(!tipoSolicitud.revisarPerfilSolicitante(rolGateway.getRoles()))
                    formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.PERFIL_SOLICITANTE_NO_VALIDO);
                if(!tipoSolicitud.revisarAnexos())
                    formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.MAL_FORMATO_ANEXO);

                for(TipoAnexo anexo : tipoSolicitud.getAnexos()) {
                    anexo.setUuidTipoAnexo(UUID.randomUUID().toString());
                    anexo.setObjTipoSolicitud(tipoSolicitud);
                }
            }
        }

        List<TipoSolicitud> guardados = gateway.guardarTiposSolicitud(tiposSolicitud);
        for(TipoSolicitud tipoSolicitudGuardado: guardados)
            log.crearLog("Crear Tipo de Solicitud", String.format("Tipo de Solicitud %s con uuid %s",tipoSolicitudGuardado.getNombre(), tipoSolicitudGuardado.getUuidTipoSolicitud()),token);
        return guardados;
    }

    @Override
    public List<TipoSolicitud> getTiposSolicitudesPorPerfil(String perfil) {
        List<TipoSolicitud> lista = gateway.getTiposSolicitudesPorPerfil(perfil);
        if (lista.isEmpty())
            formateadorExcepciones.lanzarSinInformacion(String.format("No existen tipos de solicitud cuyo perfil solicitante sea '%s'", perfil));
        return lista;
    }

    @Override
    public PaginacionRespuestaDTO<TipoSolicitud> getTiposSolicitudesPorPerfilSolicitante(String perfil, int pagina, int tamanio) {
        if (pagina < 0 || tamanio < 0)
            formateadorExcepciones.lanzarMalFormato(MensajesError.PAGINACION_ERROR);

        PaginacionRespuestaDTO<TipoSolicitud> respuesta = gateway.getTiposSolicitudesPorPerfilSolicitante(perfil, pagina, tamanio);
        if (respuesta.getContent().isEmpty())
            formateadorExcepciones.lanzarSinInformacion(String.format(MensajesError.SIN_REGISTROS, TIPOS_SOLICITUD));

        return respuesta;
    }

    @Override
    public PaginacionRespuestaDTO<TipoSolicitud> getTiposSolicitudesPorNombreYPerfilSolicitante(String nombre, String perfil, int pagina, int tamanio) {
        if (pagina < 0 || tamanio < 0)
            formateadorExcepciones.lanzarMalFormato(MensajesError.PAGINACION_ERROR);

        PaginacionRespuestaDTO<TipoSolicitud> respuesta = gateway.getTiposSolicitudesPorNombreYPerfilSolicitante(nombre, perfil, pagina, tamanio);
        if (respuesta.getContent().isEmpty())
            formateadorExcepciones.lanzarSinInformacion(String.format(MensajesError.SIN_REGISTROS, TIPOS_SOLICITUD));

        return respuesta;
    }

    /**
     * Asigna un funcionario a un tipo de solicitud basado en el UUID proporcionado.
     *
     * @param tipoSolicitud el tipo de solicitud al que se asignará el funcionario.
     * @param uuidFuncionario el UUID del funcionario a asignar.
     */
    private void asignarFuncionario(TipoSolicitud tipoSolicitud, String uuidFuncionario){
        if(uuidFuncionario != null && !uuidFuncionario.isBlank()) {
            Usuario usuario = gatewayUsuario.getUsuario(uuidFuncionario);
            if (usuario == null)
                formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, USUARIO, uuidFuncionario));
            if(usuario instanceof  Funcionario) {
                Funcionario funcionario = (Funcionario) usuario;
                tipoSolicitud.setObjFuncionarioEncargado(funcionario);
            } else
                formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.MAL_ASIGNACION);
        }
    }

}
