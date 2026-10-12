package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.EstudianteCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.UsuarioCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.AsignaturaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.DatosPersonalesGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.EstudianteGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.ExcepcionesFormateadorIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.IJwtServicio;
import com.unicauca.cfiet.solicitudes.aplicacion.output.ProgramaAcademicoGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.RolGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.SesionGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.UsuarioGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.ApplicationConstantes;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EstadoAsignaturaMatriculadaConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Asignatura;
import com.unicauca.cfiet.solicitudes.dominio.modelos.AsignaturaMatriculada;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Estudiante;
import com.unicauca.cfiet.solicitudes.dominio.modelos.ProgramaAcademico;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Rol;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoUsuario;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Usuario;
import com.unicauca.cfiet.solicitudes.dominio.servicios.DatosPersonales;
import com.unicauca.cfiet.solicitudes.dominio.servicios.ReglasAsignatura;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

public class EstudianteCUImplAdaptador implements EstudianteCUIntPuerto {
    private final UsuarioCUIntPuerto usuarioCU;
    private final EstudianteGatewayIntPuerto gateway;
    private final AsignaturaGatewayIntPuerto asignaturaGateway;
    private final ProgramaAcademicoGatewayIntPuerto programaGateway;
    private final RolGatewayIntPuerto rolGateway;
    private final UsuarioGatewayIntPuerto usuarioGateway;
    private final SesionGatewayIntPuerto sesionGateway;
    private final IJwtServicio jwtServicio;
    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;
    private final LogCUIntPuerto log;
    private final DatosPersonales datosPersonales;
    private static final String FACULTAD_ESTUDIANTE = "FIET";
    private static final String ESTUDIANTE = "Estudiante";
    private static final String ESTUDIANTES = "estudiantes";
    private static final String USUARIO = "Usuario";
    private static final String ROL = "Rol";
    private static final String TIPO_USUARIO = "Tipo de usuario";
    private static final String ASIGNATURA = "Asignatura";
    private static final String ASIGNATURA_MATRICULADA = "Asignatura matriculada";
    private static final String PROGRAMA_ACADEMICO = "Programa académico";
    private static final String CODIGO = "codigo";
    private static final String NOMBRE = "nombre";
    private static final String USERNAME = "username";
    private static final String CODIGO_ESTUDIANTIL = "codigo estudiantil";
    private static final String NUMERO_DOCUMENTO = "numero de documento";
    private static final String CORREO_ELECTRONICO = "correo electronico";
    private static final String ASIGNATURA_Y_GRUPO = "la asignatura y grupo";
    private static final String TIPO_USUARIO_ESTUDIANTE = "Estudiante";
    private static final String INSTANCIA_ESTUDIANTE = "ESTUDIANTE";

    public EstudianteCUImplAdaptador(UsuarioCUIntPuerto usuarioCU,
                                     EstudianteGatewayIntPuerto gateway,
                                     AsignaturaGatewayIntPuerto asignaturaGateway,
                                     ProgramaAcademicoGatewayIntPuerto programaGateway,
                                     RolGatewayIntPuerto rolGateway,
                                     UsuarioGatewayIntPuerto usuarioGateway,
                                     DatosPersonalesGatewayIntPuerto datosPersonalesGateway,
                                     SesionGatewayIntPuerto sesionGateway,
                                     IJwtServicio jwtServicio,
                                     ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                     LogCUIntPuerto log) {
        this.usuarioCU = usuarioCU;
        this.gateway = gateway;
        this.asignaturaGateway = asignaturaGateway;
        this.programaGateway = programaGateway;
        this.rolGateway = rolGateway;
        this.usuarioGateway = usuarioGateway;
        this.sesionGateway = sesionGateway;
        this.jwtServicio = jwtServicio;
        this.formateadorExcepciones = formateadorExcepciones;
        this.log = log;
        this.datosPersonales = new DatosPersonales(datosPersonalesGateway, formateadorExcepciones);
    }

    @Override
    public Estudiante crearEstudiante(Estudiante estudiante, String token) {
        validarEstudiante(estudiante);
        Rol rol = obtenerRolEstudiante();
        TipoUsuario tipoUsuario = obtenerTipoUsuarioEstudiante();
        return registrar(estudiante, rol, tipoUsuario, token);
    }

    @Override
    public List<Estudiante> crearEstudiantes(List<Estudiante> estudiantes, String token) {
        if (estudiantes == null || estudiantes.isEmpty())
            formateadorExcepciones.lanzarSinInformacion(String.format(MensajesError.ARCHIVO_EXCEL_VACIO, ESTUDIANTES));

        for (Estudiante estudiante : estudiantes)
            validarEstudiante(estudiante);
        validarRepetidosEnLote(estudiantes, Estudiante::getCodigoEstudiantil, CODIGO_ESTUDIANTIL);
        validarRepetidosEnLote(estudiantes, e -> e.getUsuario().getNumeroDocumento(), NUMERO_DOCUMENTO);
        validarRepetidosEnLote(estudiantes, e -> e.getUsuario().getCorreoElectronico(), CORREO_ELECTRONICO);
        validarRepetidosEnLote(estudiantes, e -> e.getUsuario().getUsername(), USERNAME);
        Rol rol = obtenerRolEstudiante();
        TipoUsuario tipoUsuario = obtenerTipoUsuarioEstudiante();

        List<Estudiante> creados = new ArrayList<>();
        for (Estudiante estudiante : estudiantes)
            creados.add(registrar(estudiante, rol, tipoUsuario, token));
        return creados;
    }

    @Override
    public PaginacionRespuestaDTO<Estudiante> getEstudiantesPaginado(int pagina, int tamanio) {
        validarPaginacion(pagina, tamanio);
        return gateway.getPaginado(pagina, tamanio);
    }

    @Override
    public PaginacionRespuestaDTO<Estudiante> getEstudiantesPorFiltro(String nombre, String apellido, String codigo, int pagina, int tamanio) {
        validarPaginacion(pagina, tamanio);
        return gateway.getPorFiltro(nombre, apellido, codigo, pagina, tamanio);
    }

    @Override
    public Estudiante getEstudiante(String uuidUsuario) {
        Estudiante estudiante = gateway.getPorUuid(uuidUsuario);
        if (estudiante == null)
            formateadorExcepciones.lanzarEntidadNoExiste(
                    String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, ESTUDIANTE, uuidUsuario));
        return estudiante;
    }

    @Override
    public List<AsignaturaMatriculada> getMisAsignaturas(String token) {
        String username = jwtServicio.getUsername(token);
        if (username == null || username.isBlank())
            formateadorExcepciones.lanzarErrorGenerico(MensajesError.USERNAME_TOKEN);

        Usuario usuario = sesionGateway.getUsuario(username);
        if (usuario == null || !gateway.existePorUuid(usuario.getUuidUsuario()))
            formateadorExcepciones.lanzarEntidadNoExiste(
                    String.format(MensajesError.ENTIDAD_NO_ENCONTRADA_FILTRO, ESTUDIANTE, USERNAME, username));

        return gateway.getAsignaturasMatriculadas(usuario.getUuidUsuario()).stream()
                .filter(materia -> EstadoAsignaturaMatriculadaConstantes.ACTIVA.equals(materia.getEstado()))
                .toList();
    }

    @Override
    public Estudiante actualizarEstudiante(String uuidUsuario, Estudiante estudiante, String token) {
        Estudiante actual = getEstudiante(uuidUsuario);

        String nuevoCodigo = tieneTexto(estudiante.getCodigoEstudiantil()) ? estudiante.getCodigoEstudiantil().trim() : null;
        if (nuevoCodigo != null && gateway.existePorCodigoEstudiantilEnOtro(nuevoCodigo, uuidUsuario))
            formateadorExcepciones.lanzarEntidadExiste(String.format(
                    MensajesError.ATRIBUTO_UNICO_YA_EXISTE, ESTUDIANTE, CODIGO_ESTUDIANTIL, nuevoCodigo));
        Usuario cambios = datosPersonales.cambiosValidados(uuidUsuario, estudiante.getUsuario());

        datosPersonales.guardar(uuidUsuario, actual.getUsuario(), cambios);
        if (nuevoCodigo != null)
            actual.setCodigoEstudiantil(nuevoCodigo);
        if (tieneTexto(estudiante.getSemestre()))
            actual.setSemestre(estudiante.getSemestre().trim());
        actual.setFacultad(FACULTAD_ESTUDIANTE);

        Estudiante guardado = gateway.guardar(actual);
        log.crearLog("Actualizar estudiante",
                String.format("Estudiante %s actualizado", guardado.getUuidUsuario()),
                token);
        return guardado;
    }

    @Override
    public AsignaturaMatriculada agregarAsignaturaMatriculada(String uuidUsuario, AsignaturaMatriculada asignaturaMatriculada, String token) {
        Estudiante estudiante = getEstudiante(uuidUsuario);

        String codigo = ReglasAsignatura.normalizarCodigo(asignaturaMatriculada.getAsignatura().getCodigoAsignatura());
        String grupo = asignaturaMatriculada.getGrupo();
        boolean yaActiva = gateway.getAsignaturasMatriculadas(uuidUsuario).stream()
                .anyMatch(materia -> EstadoAsignaturaMatriculadaConstantes.ACTIVA.equals(materia.getEstado())
                        && normalizar(materia.getAsignatura().getCodigoAsignatura()).equals(normalizar(codigo))
                        && normalizar(materia.getGrupo()).equals(normalizar(grupo)));
        if (yaActiva)
            formateadorExcepciones.lanzarMalFormato(String.format(MensajesError.ASIGNATURA_YA_MATRICULADA, codigo, grupo));

        asignaturaMatriculada.setAsignatura(asignaturaDelPrograma(codigo, estudiante.getProgramaAcademico()));
        asignaturaMatriculada.setUuidAsignaturaMatriculada(UUID.randomUUID().toString());
        asignaturaMatriculada.setEstado(EstadoAsignaturaMatriculadaConstantes.ACTIVA);
        AsignaturaMatriculada guardada = gateway.guardarAsignaturaMatriculada(uuidUsuario, asignaturaMatriculada);
        log.crearLog("Agregar asignatura matriculada",
                String.format("Asignatura %s grupo %s matriculada al estudiante %s con uuid %s",
                        guardada.getAsignatura().getCodigoAsignatura(), guardada.getGrupo(), uuidUsuario,
                        guardada.getUuidAsignaturaMatriculada()),
                token);
        return guardada;
    }

    @Override
    public AsignaturaMatriculada cambiarEstadoAsignatura(String uuidUsuario, String uuidAsignaturaMatriculada, String estado, String token) {
        String nuevoEstado = estado == null ? "" : estado.trim().toLowerCase();
        if (!EstadoAsignaturaMatriculadaConstantes.ESTADOS_VALIDOS.contains(nuevoEstado))
            formateadorExcepciones.lanzarMalFormato(String.format(MensajesError.ESTADO_ASIGNATURA_NO_VALIDO,
                    estado, String.join(", ", EstadoAsignaturaMatriculadaConstantes.ESTADOS_VALIDOS)));

        AsignaturaMatriculada matricula = gateway.getAsignaturaMatriculada(uuidUsuario, uuidAsignaturaMatriculada);
        if (matricula == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(
                    MensajesError.ENTIDAD_NO_ENCONTRADA, ASIGNATURA_MATRICULADA, uuidAsignaturaMatriculada));

        String estadoActual = matricula.getEstado();
        if (!EstadoAsignaturaMatriculadaConstantes.ACTIVA.equals(estadoActual)
                || EstadoAsignaturaMatriculadaConstantes.ACTIVA.equals(nuevoEstado))
            formateadorExcepciones.lanzarMalFormato(String.format(MensajesError.CAMBIO_ESTADO_NO_PERMITIDO, estadoActual, nuevoEstado));

        matricula.setEstado(nuevoEstado);
        AsignaturaMatriculada guardada = gateway.guardarAsignaturaMatriculada(uuidUsuario, matricula);
        log.crearLog("Cambiar estado asignatura matriculada",
                String.format("Asignatura matriculada %s del estudiante %s paso de %s a %s",
                        uuidAsignaturaMatriculada, uuidUsuario, estadoActual, nuevoEstado),
                token);
        return guardada;
    }

    private void validarEstudiante(Estudiante estudiante) {
        if (gateway.existePorCodigoEstudiantil(estudiante.getCodigoEstudiantil()))
            formateadorExcepciones.lanzarEntidadExiste(String.format(
                    MensajesError.ATRIBUTO_UNICO_YA_EXISTE, ESTUDIANTE, CODIGO_ESTUDIANTIL, estudiante.getCodigoEstudiantil()));

        Usuario usuario = estudiante.getUsuario();
        if (usuarioGateway.existeUsuarioNumeroDocumento(usuario.getNumeroDocumento()))
            formateadorExcepciones.lanzarEntidadExiste(String.format(
                    MensajesError.ATRIBUTO_UNICO_YA_EXISTE, USUARIO, NUMERO_DOCUMENTO, usuario.getNumeroDocumento()));
        if (usuarioGateway.existeUsuarioCorreo(usuario.getCorreoElectronico()))
            formateadorExcepciones.lanzarEntidadExiste(String.format(
                    MensajesError.ATRIBUTO_UNICO_YA_EXISTE, USUARIO, CORREO_ELECTRONICO, usuario.getCorreoElectronico()));
        if (usuarioGateway.existeUsuarioUsername(usuario.getUsername()))
            formateadorExcepciones.lanzarEntidadExiste(String.format(
                    MensajesError.ATRIBUTO_UNICO_YA_EXISTE, USUARIO, USERNAME, usuario.getUsername()));

        Set<String> materias = new HashSet<>();
        for (AsignaturaMatriculada materia : estudiante.getAsignaturasMatriculadas()) {
            String codigo = materia.getAsignatura().getCodigoAsignatura();
            if (!materias.add(normalizar(codigo) + "|" + normalizar(materia.getGrupo())))
                formateadorExcepciones.lanzarMalFormato(String.format(
                        MensajesError.VALOR_REPETIDO_PETICION, ASIGNATURA_Y_GRUPO, codigo + " - " + materia.getGrupo()));
        }

        ProgramaAcademico programa = programaExistente(estudiante.getProgramaAcademico());
        estudiante.setProgramaAcademico(programa);
        for (AsignaturaMatriculada materia : estudiante.getAsignaturasMatriculadas())
            materia.setAsignatura(asignaturaDelPrograma(
                    ReglasAsignatura.normalizarCodigo(materia.getAsignatura().getCodigoAsignatura()), programa));
    }

    private ProgramaAcademico programaExistente(ProgramaAcademico recibido) {
        String uuid = recibido == null ? null : recibido.getUuidProgramaAcademico();
        if (!tieneTexto(uuid))
            formateadorExcepciones.lanzarMalFormato(MensajesError.PROGRAMA_ACADEMICO_REQUERIDO);
        ProgramaAcademico programa = programaGateway.getPorUuid(uuid.trim());
        if (programa == null)
            formateadorExcepciones.lanzarEntidadNoExiste(
                    String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, PROGRAMA_ACADEMICO, uuid));
        return programa;
    }

    private Asignatura asignaturaDelPrograma(String codigo, ProgramaAcademico programa) {
        Asignatura asignatura = asignaturaGateway.getPorCodigo(codigo);
        if (asignatura == null)
            formateadorExcepciones.lanzarEntidadNoExiste(
                    String.format(MensajesError.ENTIDAD_NO_ENCONTRADA_FILTRO, ASIGNATURA, CODIGO, codigo));
        ProgramaAcademico programaAsignatura = asignatura.getProgramaAcademico();
        if (programaAsignatura == null || !programa.getUuidProgramaAcademico().equals(programaAsignatura.getUuidProgramaAcademico()))
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.ASIGNATURA_OTRO_PROGRAMA,
                    asignatura.getCodigoAsignatura(), asignatura.getNombreAsignatura(),
                    programaAsignatura == null ? "" : programaAsignatura.getNombre(), programa.getNombre()));
        return asignatura;
    }

    private void validarRepetidosEnLote(List<Estudiante> estudiantes, Function<Estudiante, String> valor, String campo) {
        Set<String> vistos = new HashSet<>();
        for (Estudiante estudiante : estudiantes) {
            String dato = valor.apply(estudiante);
            if (tieneTexto(dato) && !vistos.add(normalizar(dato)))
                formateadorExcepciones.lanzarMalFormato(String.format(MensajesError.VALOR_REPETIDO_PETICION, campo, dato));
        }
    }

    private Rol obtenerRolEstudiante() {
        Rol rol = rolGateway.getRoles().stream()
                .filter(r -> ApplicationConstantes.ESTUDIANTE_ROL.equals(r.getNombre()))
                .findFirst()
                .orElse(null);
        if (rol == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(
                    MensajesError.ENTIDAD_NO_ENCONTRADA_FILTRO, ROL, NOMBRE, ApplicationConstantes.ESTUDIANTE_ROL));
        return rol;
    }

    private TipoUsuario obtenerTipoUsuarioEstudiante() {
        TipoUsuario tipoUsuario = usuarioGateway.getTipoUsuarioPorNombre(TIPO_USUARIO_ESTUDIANTE);
        if (tipoUsuario == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(
                    MensajesError.ENTIDAD_NO_ENCONTRADA_FILTRO, TIPO_USUARIO, NOMBRE, TIPO_USUARIO_ESTUDIANTE));
        return tipoUsuario;
    }

    private Estudiante registrar(Estudiante estudiante, Rol rol, TipoUsuario tipoUsuario, String token) {
        Usuario usuario = estudiante.getUsuario();
        usuario.setRoles(new ArrayList<>(List.of(rol)));
        usuario.setObjTipoUsuario(tipoUsuario);
        Usuario creado = usuarioCU.crearUsuario(usuario, INSTANCIA_ESTUDIANTE, token);

        estudiante.setUuidUsuario(creado.getUuidUsuario());
        estudiante.setUsuario(creado);
        estudiante.setFacultad(FACULTAD_ESTUDIANTE);
        for (AsignaturaMatriculada materia : estudiante.getAsignaturasMatriculadas()) {
            materia.setUuidAsignaturaMatriculada(UUID.randomUUID().toString());
            materia.setEstado(EstadoAsignaturaMatriculadaConstantes.ACTIVA);
        }
        Estudiante guardado = gateway.guardar(estudiante);
        log.crearLog("Crear estudiante",
                String.format("Estudiante %s con codigo %s creado con uuid %s y %d asignaturas matriculadas",
                        creado.getNombres() + " " + creado.getApellidos(), guardado.getCodigoEstudiantil(),
                        guardado.getUuidUsuario(), estudiante.getAsignaturasMatriculadas().size()),
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

    private String normalizar(String valor) {
        return valor == null ? "" : valor.trim().toUpperCase();
    }
}
