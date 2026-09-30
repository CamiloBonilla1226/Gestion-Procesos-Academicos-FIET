package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.UsuarioCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.ExcepcionesFormateadorIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.PasswordEncoderGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.UsuarioGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Funcionario;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoUsuario;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Usuario;
import com.unicauca.cfiet.solicitudes.dominio.modelos.UsuarioLiviano;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implementación de la interfaz de los casos de uso para la gestión de usuarios.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
public class UsuarioCUImplAdaptador implements UsuarioCUIntPuerto {
    private final UsuarioGatewayIntPuerto gateway;
    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;
    private final LogCUIntPuerto log;
    private final PasswordEncoderGatewayIntPuerto encoder;
    /* Constantes */
    private static final String USUARIOS = "usuarios";
    private static final String USUARIO = "Usuario";
    private static final String NUMERO_DOCUMENTO = "Numero de documento";
    private static final String CORREO_ELECTRONICO = "Correo Electrónico";
    private  static final String USERNAME = "username";
    private static final String TIPO_USUARIO = "Tipo de usuario";
    private static final String TIPOS_USUARIO = "Tipos de usuario";
    private static final String NOMBRE = "nombre";

    public UsuarioCUImplAdaptador(UsuarioGatewayIntPuerto gateway,
                                  ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                  LogCUIntPuerto log,
                                  PasswordEncoderGatewayIntPuerto encoder){
        this.gateway = gateway;
        this.formateadorExcepciones = formateadorExcepciones;
        this.log = log;
        this.encoder = encoder;
    }

    @Override
    public List<UsuarioLiviano> getUsuarios() {
        List<UsuarioLiviano> usuarios = gateway.getUsuarios();
        if(usuarios.isEmpty())
            formateadorExcepciones.lanzarSinInformacion(String.format(MensajesError.SIN_REGISTROS, USUARIOS));
        return  usuarios;
    }

    @Override
    public List<Funcionario> getFuncionarios(){
        return gateway.getFuncionarios();
    }

    @Override
    public PaginacionRespuestaDTO<UsuarioLiviano> getUsuarios(int pagina, int tamanio) {
        if (pagina < 0 || tamanio < 0)
            formateadorExcepciones.lanzarMalFormato(MensajesError.PAGINACION_ERROR);
        PaginacionRespuestaDTO<UsuarioLiviano> respuesta = gateway.getUsuarios(pagina, tamanio);
        if (respuesta.getContent().isEmpty())
            formateadorExcepciones.lanzarSinInformacion(String.format(MensajesError.SIN_REGISTROS, USUARIOS));
        return respuesta;
    }

    @Override
    public PaginacionRespuestaDTO<UsuarioLiviano> getUsuariosByNombreCompleto(String nombreCompleto, int pagina, int tamanio) {
        if (pagina < 0 || tamanio < 0)
            formateadorExcepciones.lanzarMalFormato(MensajesError.PAGINACION_ERROR);
        PaginacionRespuestaDTO<UsuarioLiviano> respuesta = gateway.getUsuariosByNombreCompleto(nombreCompleto, pagina, tamanio);
        if (respuesta.getContent().isEmpty())
            formateadorExcepciones.lanzarSinInformacion(String.format(MensajesError.SIN_REGISTROS, USUARIOS));
        return respuesta;
    }

    @Override
    public Usuario getUsuario(String uuidUsuario) {
        Usuario usuario = gateway.getUsuario(uuidUsuario);
        if(usuario == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, USUARIO, uuidUsuario));
        return usuario;
    }

    @Override
    public Usuario crearUsuario(Usuario usuario, String tipoUsuario, String token) {
        if(!usuario.revisarTipoDocumento())
            formateadorExcepciones.lanzarMalFormato(MensajesError.TIPO_DOCUMENTO_ERRONEO);
        if(gateway.existeUsuarioNumeroDocumento(usuario.getNumeroDocumento()))
            formateadorExcepciones.lanzarEntidadExiste(String.format(MensajesError.ATRIBUTO_UNICO_YA_EXISTE, USUARIO, NUMERO_DOCUMENTO, usuario.getNumeroDocumento()));
        if(gateway.existeUsuarioCorreo(usuario.getCorreoElectronico()))
            formateadorExcepciones.lanzarEntidadExiste(String.format(MensajesError.ATRIBUTO_UNICO_YA_EXISTE, USUARIO, CORREO_ELECTRONICO, usuario.getCorreoElectronico()));
        if(gateway.existeUsuarioUsername(usuario.getUsername()))
            formateadorExcepciones.lanzarEntidadExiste(String.format(MensajesError.ATRIBUTO_UNICO_YA_EXISTE, USUARIO, USERNAME, usuario.getUsername()));

        TipoUsuario objTipoUsuario = gateway.getTipoUsuarioPorNombre(usuario.getObjTipoUsuario().getNombre());
        if(objTipoUsuario == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA_FILTRO, TIPO_USUARIO,  NOMBRE, usuario.getObjTipoUsuario().getNombre()));

        if(usuario.tieneRolesDuplicados())
            formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.ROLES_DUPLICADOS_USUARIO);
        if(!usuario.rolesSonValidos(gateway.getRoles()))
            formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.ROLES_NO_VALIDOS);

        usuario.setUuidUsuario(UUID.randomUUID().toString());
        usuario.setPassword(encoder.encriptarContraseña(usuario.getPassword()));
        usuario.setObjTipoUsuario(objTipoUsuario);
        Usuario instancia = usuario.crearInstancia(tipoUsuario);
        if(instancia == null)
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.INSTANCIA_NO_VALIDA, USUARIO));

        log.crearLog("Crear Usuario", String.format("Usuario %s %s creado con uuid %s",usuario.getNombres(), usuario.getApellidos(), usuario.getUuidUsuario()), token);
        return gateway.guardarUsuario(instancia);
    }

    @Override
    public List<Usuario> crearUsuarios(List<Usuario> usuarios, String token) {
        if (usuarios.isEmpty())
            formateadorExcepciones.lanzarSinInformacion(String.format(MensajesError.ARCHIVO_EXCEL_VACIO, USUARIOS));

        for (Usuario usuario : usuarios) {
            if(!usuario.revisarTipoDocumento())
                formateadorExcepciones.lanzarMalFormato(MensajesError.TIPO_DOCUMENTO_ERRONEO);
            if (gateway.existeUsuarioNumeroDocumento(usuario.getNumeroDocumento()))
                formateadorExcepciones.lanzarEntidadExiste(String.format(
                        MensajesError.ATRIBUTO_UNICO_YA_EXISTE, USUARIO, NUMERO_DOCUMENTO, usuario.getNumeroDocumento()));

            if (gateway.existeUsuarioCorreo(usuario.getCorreoElectronico()))
                formateadorExcepciones.lanzarEntidadExiste(String.format(
                        MensajesError.ATRIBUTO_UNICO_YA_EXISTE, USUARIO, CORREO_ELECTRONICO, usuario.getCorreoElectronico()));

            if (gateway.existeUsuarioUsername(usuario.getUsername()))
                formateadorExcepciones.lanzarEntidadExiste(String.format(
                        MensajesError.ATRIBUTO_UNICO_YA_EXISTE, USUARIO, USERNAME, usuario.getUsername()));

            TipoUsuario objTipoUsuario = gateway.getTipoUsuarioPorNombre(usuario.getObjTipoUsuario().getNombre());
            if (objTipoUsuario == null)
                formateadorExcepciones.lanzarEntidadNoExiste(String.format(
                        MensajesError.ENTIDAD_NO_ENCONTRADA_FILTRO, TIPO_USUARIO, NOMBRE, usuario.getObjTipoUsuario().getNombre()));

            if (usuario.tieneRolesDuplicados())
                formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.ROLES_DUPLICADOS_USUARIO);

            if (!usuario.rolesSonValidos(gateway.getRoles()))
                formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.ROLES_NO_VALIDOS);

            usuario.setUuidUsuario(UUID.randomUUID().toString());
            usuario.setPassword(encoder.encriptarContraseña(usuario.getPassword()));
            usuario.setObjTipoUsuario(objTipoUsuario);
        }

        List<Usuario> instancias = new ArrayList<>();
        for (Usuario usuario : usuarios) {
            String tipoUsuario = usuario.getRoles().get(0).getNombre().replace(" ", "");
            Usuario instancia = usuario.crearInstancia(tipoUsuario);

            if (instancia == null)
                formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.INSTANCIA_NO_VALIDA, USUARIO));

            instancias.add(instancia);
        }

        List<Usuario> guardados = gateway.guardarUsuarios(instancias);

        for (Usuario usuario : guardados) {
            log.crearLog("Crear Usuario", String.format(
                    "Usuario %s %s creado con uuid %s",
                    usuario.getNombres(), usuario.getApellidos(), usuario.getUuidUsuario()), token);
        }

        return guardados;
    }

    @Override
    public Usuario actualizarUsuario(String uuidUsuario, Usuario usuario, String token) {
        Usuario usuarioObtenido = gateway.getUsuario(uuidUsuario);
        if(usuarioObtenido == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, USUARIO, uuidUsuario));

        if(!usuario.revisarTipoDocumento())
            formateadorExcepciones.lanzarMalFormato(MensajesError.TIPO_DOCUMENTO_ERRONEO);

        if(usuario.tieneRolesDuplicados())
            formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.ROLES_DUPLICADOS_USUARIO);
        if(!usuario.rolesSonValidos(gateway.getRoles()))
            formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.ROLES_NO_VALIDOS);

        if(!usuarioObtenido.getNumeroDocumento().equals(usuario.getNumeroDocumento())){
            if(gateway.existeUsuarioNumeroDocumento(usuario.getNumeroDocumento()))
                formateadorExcepciones.lanzarEntidadExiste(String.format(MensajesError.ATRIBUTO_UNICO_YA_EXISTE, USUARIO, NUMERO_DOCUMENTO, usuario.getNumeroDocumento()));
        }

        if(!usuarioObtenido.getCorreoElectronico().equals(usuario.getCorreoElectronico())){
            if(gateway.existeUsuarioCorreo(usuario.getCorreoElectronico()))
                formateadorExcepciones.lanzarEntidadExiste(String.format(MensajesError.ATRIBUTO_UNICO_YA_EXISTE, USUARIO, CORREO_ELECTRONICO, usuario.getCorreoElectronico()));
        }

        if(!usuarioObtenido.getUsername().equals(usuario.getUsername())) {
            if (gateway.existeUsuarioUsername(usuario.getUsername()))
                formateadorExcepciones.lanzarEntidadExiste(String.format(MensajesError.ATRIBUTO_UNICO_YA_EXISTE, USUARIO, USERNAME, usuario.getUsername()));
        }

        usuarioObtenido.actualizarUsuario(usuario);
        log.crearLog("Actualizar Usuario", "Usuario actualizado con éxito", token);
        return gateway.guardarUsuario(usuarioObtenido);
    }

    @Override
    public Usuario cambiarContraseña(String uuidUsuario, String contraseña, String nuevaContraseña, String token) {
        Usuario usuario = getUsuario(uuidUsuario);
        if(!encoder.contraseñaCoincide(usuario.getPassword(), contraseña))
            formateadorExcepciones.lanzarCredencialesErroneas(MensajesError.CONTRASEÑA_INCORRECTA);
        usuario.setPassword(encoder.encriptarContraseña(nuevaContraseña));
        log.crearLog("Actualizar Usuario", "Usuario actualizo contraseña con éxito", token);
        return gateway.guardarUsuario(usuario);
    }

    @Override
    public List<TipoUsuario> getTiposUsuario() {
        List<TipoUsuario> tiposUsuario = gateway.getTiposUsuario();
        if(tiposUsuario.isEmpty())
            formateadorExcepciones.lanzarSinInformacion(String.format(MensajesError.SIN_REGISTROS, TIPOS_USUARIO));
        return  tiposUsuario;
    }
}
