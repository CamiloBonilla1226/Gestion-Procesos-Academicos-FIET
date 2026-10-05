package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.FuncionarioAcademicoCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.UsuarioCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.ExcepcionesFormateadorIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.FuncionarioAcademicoGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.RolGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.UsuarioGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.ApplicationConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.FuncionarioAcademico;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Rol;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoUsuario;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Usuario;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class FuncionarioAcademicoCUImplAdaptador implements FuncionarioAcademicoCUIntPuerto {
    private final UsuarioCUIntPuerto usuarioCU;
    private final FuncionarioAcademicoGatewayIntPuerto gateway;
    private final RolGatewayIntPuerto rolGateway;
    private final UsuarioGatewayIntPuerto usuarioGateway;
    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;
    private final LogCUIntPuerto log;
    private static final String FUNCIONARIO_ACADEMICO = "Funcionario Académico";
    private static final String FUNCIONARIOS_ACADEMICOS = "funcionarios académicos";
    private static final String USUARIO = "Usuario";
    private static final String ROL = "Rol";
    private static final String TIPO_USUARIO = "Tipo de usuario";
    private static final String NOMBRE = "nombre";
    private static final String USERNAME = "username";
    private static final String NUMERO_DOCUMENTO = "numero de documento";
    private static final String CORREO_ELECTRONICO = "correo electronico";
    private static final String TIPO_USUARIO_FUNCIONARIO_ACADEMICO = "Funcionario Académico";
    private static final String INSTANCIA_FUNCIONARIO_ACADEMICO = "FUNCIONARIOACADEMICO";

    public FuncionarioAcademicoCUImplAdaptador(UsuarioCUIntPuerto usuarioCU,
                                               FuncionarioAcademicoGatewayIntPuerto gateway,
                                               RolGatewayIntPuerto rolGateway,
                                               UsuarioGatewayIntPuerto usuarioGateway,
                                               ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                               LogCUIntPuerto log) {
        this.usuarioCU = usuarioCU;
        this.gateway = gateway;
        this.rolGateway = rolGateway;
        this.usuarioGateway = usuarioGateway;
        this.formateadorExcepciones = formateadorExcepciones;
        this.log = log;
    }

    @Override
    public FuncionarioAcademico crearFuncionarioAcademico(FuncionarioAcademico funcionarioAcademico, String token) {
        validarUsuario(funcionarioAcademico.getUsuario());
        Rol rol = obtenerRol();
        TipoUsuario tipoUsuario = obtenerTipoUsuario();
        return registrar(funcionarioAcademico, rol, tipoUsuario, token);
    }

    @Override
    public List<FuncionarioAcademico> crearFuncionariosAcademicos(List<FuncionarioAcademico> funcionariosAcademicos, String token) {
        if (funcionariosAcademicos == null || funcionariosAcademicos.isEmpty())
            formateadorExcepciones.lanzarSinInformacion(String.format(MensajesError.ARCHIVO_EXCEL_VACIO, FUNCIONARIOS_ACADEMICOS));

        for (FuncionarioAcademico funcionarioAcademico : funcionariosAcademicos)
            validarUsuario(funcionarioAcademico.getUsuario());
        validarRepetidosEnLote(funcionariosAcademicos, Usuario::getNumeroDocumento, NUMERO_DOCUMENTO);
        validarRepetidosEnLote(funcionariosAcademicos, Usuario::getCorreoElectronico, CORREO_ELECTRONICO);
        validarRepetidosEnLote(funcionariosAcademicos, Usuario::getUsername, USERNAME);
        Rol rol = obtenerRol();
        TipoUsuario tipoUsuario = obtenerTipoUsuario();

        List<FuncionarioAcademico> creados = new ArrayList<>();
        for (FuncionarioAcademico funcionarioAcademico : funcionariosAcademicos)
            creados.add(registrar(funcionarioAcademico, rol, tipoUsuario, token));
        return creados;
    }

    @Override
    public PaginacionRespuestaDTO<FuncionarioAcademico> getFuncionariosAcademicosPaginado(int pagina, int tamanio) {
        validarPaginacion(pagina, tamanio);
        return gateway.getPaginado(pagina, tamanio);
    }

    @Override
    public PaginacionRespuestaDTO<FuncionarioAcademico> getFuncionariosAcademicosPorFiltro(String nombre, String apellido, String dependencia, int pagina, int tamanio) {
        validarPaginacion(pagina, tamanio);
        return gateway.getPorFiltro(nombre, apellido, dependencia, pagina, tamanio);
    }

    @Override
    public FuncionarioAcademico getFuncionarioAcademico(String uuidUsuario) {
        FuncionarioAcademico funcionarioAcademico = gateway.getPorUuid(uuidUsuario);
        if (funcionarioAcademico == null)
            formateadorExcepciones.lanzarEntidadNoExiste(
                    String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, FUNCIONARIO_ACADEMICO, uuidUsuario));
        return funcionarioAcademico;
    }

    @Override
    public FuncionarioAcademico actualizarFuncionarioAcademico(String uuidUsuario, FuncionarioAcademico funcionarioAcademico, String token) {
        FuncionarioAcademico actual = getFuncionarioAcademico(uuidUsuario);
        if (tieneTexto(funcionarioAcademico.getDependencia()))
            actual.setDependencia(funcionarioAcademico.getDependencia().trim());

        FuncionarioAcademico guardado = gateway.guardar(actual);
        log.crearLog("Actualizar funcionario académico",
                String.format("Funcionario académico %s actualizado", guardado.getUuidUsuario()),
                token);
        return guardado;
    }

    private void validarUsuario(Usuario usuario) {
        if (usuarioGateway.existeUsuarioNumeroDocumento(usuario.getNumeroDocumento()))
            formateadorExcepciones.lanzarEntidadExiste(String.format(
                    MensajesError.ATRIBUTO_UNICO_YA_EXISTE, USUARIO, NUMERO_DOCUMENTO, usuario.getNumeroDocumento()));
        if (usuarioGateway.existeUsuarioCorreo(usuario.getCorreoElectronico()))
            formateadorExcepciones.lanzarEntidadExiste(String.format(
                    MensajesError.ATRIBUTO_UNICO_YA_EXISTE, USUARIO, CORREO_ELECTRONICO, usuario.getCorreoElectronico()));
        if (usuarioGateway.existeUsuarioUsername(usuario.getUsername()))
            formateadorExcepciones.lanzarEntidadExiste(String.format(
                    MensajesError.ATRIBUTO_UNICO_YA_EXISTE, USUARIO, USERNAME, usuario.getUsername()));
    }

    private void validarRepetidosEnLote(List<FuncionarioAcademico> funcionariosAcademicos, Function<Usuario, String> valor, String campo) {
        Set<String> vistos = new HashSet<>();
        for (FuncionarioAcademico funcionarioAcademico : funcionariosAcademicos) {
            String dato = valor.apply(funcionarioAcademico.getUsuario());
            if (tieneTexto(dato) && !vistos.add(dato.trim().toUpperCase()))
                formateadorExcepciones.lanzarMalFormato(String.format(MensajesError.VALOR_REPETIDO_PETICION, campo, dato));
        }
    }

    private Rol obtenerRol() {
        Rol rol = rolGateway.getRoles().stream()
                .filter(r -> ApplicationConstantes.FUNCIONARIO_ACADEMICO_ROL.equals(r.getNombre()))
                .findFirst()
                .orElse(null);
        if (rol == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(
                    MensajesError.ENTIDAD_NO_ENCONTRADA_FILTRO, ROL, NOMBRE, ApplicationConstantes.FUNCIONARIO_ACADEMICO_ROL));
        return rol;
    }

    private TipoUsuario obtenerTipoUsuario() {
        TipoUsuario tipoUsuario = usuarioGateway.getTipoUsuarioPorNombre(TIPO_USUARIO_FUNCIONARIO_ACADEMICO);
        if (tipoUsuario == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(
                    MensajesError.ENTIDAD_NO_ENCONTRADA_FILTRO, TIPO_USUARIO, NOMBRE, TIPO_USUARIO_FUNCIONARIO_ACADEMICO));
        return tipoUsuario;
    }

    private FuncionarioAcademico registrar(FuncionarioAcademico funcionarioAcademico, Rol rol, TipoUsuario tipoUsuario, String token) {
        Usuario usuario = funcionarioAcademico.getUsuario();
        usuario.setRoles(new ArrayList<>(List.of(rol)));
        usuario.setObjTipoUsuario(tipoUsuario);
        Usuario creado = usuarioCU.crearUsuario(usuario, INSTANCIA_FUNCIONARIO_ACADEMICO, token);

        funcionarioAcademico.setUuidUsuario(creado.getUuidUsuario());
        funcionarioAcademico.setUsuario(creado);
        FuncionarioAcademico guardado = gateway.guardar(funcionarioAcademico);
        log.crearLog("Crear funcionario académico",
                String.format("Funcionario académico %s %s de la dependencia %s creado con uuid %s",
                        creado.getNombres(), creado.getApellidos(), guardado.getDependencia(), guardado.getUuidUsuario()),
                token);
        return guardado;
    }

    private void validarPaginacion(int pagina, int tamanio) {
        if (pagina < 0 || tamanio < 1)
            formateadorExcepciones.lanzarMalFormato(MensajesError.PAGINACION_ERROR);
    }

    private boolean tieneTexto(String valor) {
        return valor != null && !valor.isBlank();
    }
}
