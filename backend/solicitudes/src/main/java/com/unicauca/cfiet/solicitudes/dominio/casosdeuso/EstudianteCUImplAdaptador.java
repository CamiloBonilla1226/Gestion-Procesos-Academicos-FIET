package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.EstudianteCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.UsuarioCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.AsignaturaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.EstudianteGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.ExcepcionesFormateadorIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.RolGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.UsuarioGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.ApplicationConstantes;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EstadoAsignaturaMatriculadaConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Asignatura;
import com.unicauca.cfiet.solicitudes.dominio.modelos.AsignaturaMatriculada;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Estudiante;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Rol;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoUsuario;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Usuario;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class EstudianteCUImplAdaptador implements EstudianteCUIntPuerto {
    private final UsuarioCUIntPuerto usuarioCU;
    private final EstudianteGatewayIntPuerto gateway;
    private final AsignaturaGatewayIntPuerto asignaturaGateway;
    private final RolGatewayIntPuerto rolGateway;
    private final UsuarioGatewayIntPuerto usuarioGateway;
    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;
    private final LogCUIntPuerto log;
    private static final String ESTUDIANTE = "Estudiante";
    private static final String ESTUDIANTES = "estudiantes";
    private static final String ROL = "Rol";
    private static final String TIPO_USUARIO = "Tipo de usuario";
    private static final String NOMBRE = "nombre";
    private static final String CODIGO_ESTUDIANTIL = "codigo estudiantil";
    private static final String ASIGNATURA_Y_GRUPO = "la asignatura y grupo";
    private static final String TIPO_USUARIO_ESTUDIANTE = "Estudiante";
    private static final String INSTANCIA_ESTUDIANTE = "ESTUDIANTE";

    public EstudianteCUImplAdaptador(UsuarioCUIntPuerto usuarioCU,
                                     EstudianteGatewayIntPuerto gateway,
                                     AsignaturaGatewayIntPuerto asignaturaGateway,
                                     RolGatewayIntPuerto rolGateway,
                                     UsuarioGatewayIntPuerto usuarioGateway,
                                     ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                     LogCUIntPuerto log) {
        this.usuarioCU = usuarioCU;
        this.gateway = gateway;
        this.asignaturaGateway = asignaturaGateway;
        this.rolGateway = rolGateway;
        this.usuarioGateway = usuarioGateway;
        this.formateadorExcepciones = formateadorExcepciones;
        this.log = log;
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

        Set<String> codigos = new HashSet<>();
        for (Estudiante estudiante : estudiantes) {
            validarEstudiante(estudiante);
            if (!codigos.add(normalizar(estudiante.getCodigoEstudiantil())))
                formateadorExcepciones.lanzarMalFormato(String.format(
                        MensajesError.VALOR_REPETIDO_PETICION, CODIGO_ESTUDIANTIL, estudiante.getCodigoEstudiantil()));
        }
        Rol rol = obtenerRolEstudiante();
        TipoUsuario tipoUsuario = obtenerTipoUsuarioEstudiante();

        List<Estudiante> creados = new ArrayList<>();
        for (Estudiante estudiante : estudiantes)
            creados.add(registrar(estudiante, rol, tipoUsuario, token));
        return creados;
    }

    @Override
    public PaginacionRespuestaDTO<Estudiante> getEstudiantesPaginado(int pagina, int tamanio) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PaginacionRespuestaDTO<Estudiante> getEstudiantesPorFiltro(String nombre, String apellido, String codigo, int pagina, int tamanio) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Estudiante getEstudiante(String uuidUsuario) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<AsignaturaMatriculada> getMisAsignaturas(String token) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Estudiante actualizarEstudiante(String uuidUsuario, Estudiante estudiante, String token) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AsignaturaMatriculada agregarAsignaturaMatriculada(String uuidUsuario, AsignaturaMatriculada asignaturaMatriculada, String token) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AsignaturaMatriculada cambiarEstadoAsignatura(String uuidUsuario, String uuidAsignaturaMatriculada, String estado, String token) {
        throw new UnsupportedOperationException();
    }

    private void validarEstudiante(Estudiante estudiante) {
        if (gateway.existePorCodigoEstudiantil(estudiante.getCodigoEstudiantil()))
            formateadorExcepciones.lanzarEntidadExiste(String.format(
                    MensajesError.ATRIBUTO_UNICO_YA_EXISTE, ESTUDIANTE, CODIGO_ESTUDIANTIL, estudiante.getCodigoEstudiantil()));

        Set<String> materias = new HashSet<>();
        for (AsignaturaMatriculada materia : estudiante.getAsignaturasMatriculadas()) {
            String codigo = materia.getAsignatura().getCodigoAsignatura();
            if (!materias.add(normalizar(codigo) + "|" + normalizar(materia.getGrupo())))
                formateadorExcepciones.lanzarMalFormato(String.format(
                        MensajesError.VALOR_REPETIDO_PETICION, ASIGNATURA_Y_GRUPO, codigo + " - " + materia.getGrupo()));
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
        resolverAsignaturas(estudiante, token);

        Usuario usuario = estudiante.getUsuario();
        usuario.setRoles(new ArrayList<>(List.of(rol)));
        usuario.setObjTipoUsuario(tipoUsuario);
        Usuario creado = usuarioCU.crearUsuario(usuario, INSTANCIA_ESTUDIANTE, token);

        estudiante.setUuidUsuario(creado.getUuidUsuario());
        estudiante.setUsuario(creado);
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

    private void resolverAsignaturas(Estudiante estudiante, String token) {
        Map<String, Asignatura> resueltas = new HashMap<>();
        for (AsignaturaMatriculada materia : estudiante.getAsignaturasMatriculadas()) {
            Asignatura recibida = materia.getAsignatura();
            String clave = normalizar(recibida.getCodigoAsignatura());
            Asignatura asignatura = resueltas.get(clave);
            if (asignatura == null) {
                asignatura = asignaturaGateway.getPorCodigo(recibida.getCodigoAsignatura().trim());
                if (asignatura == null)
                    asignatura = crearAsignatura(recibida, token);
                resueltas.put(clave, asignatura);
            }
            materia.setAsignatura(asignatura);
        }
    }

    private Asignatura crearAsignatura(Asignatura recibida, String token) {
        Asignatura nueva = Asignatura.builder()
                .uuidAsignatura(UUID.randomUUID().toString())
                .codigoAsignatura(recibida.getCodigoAsignatura().trim())
                .nombreAsignatura(recibida.getNombreAsignatura())
                .build();
        Asignatura guardada = asignaturaGateway.guardar(nueva);
        log.crearLog("Crear asignatura",
                String.format("Asignatura %s - %s creada con uuid %s",
                        guardada.getCodigoAsignatura(), guardada.getNombreAsignatura(), guardada.getUuidAsignatura()),
                token);
        return guardada;
    }

    private String normalizar(String valor) {
        return valor == null ? "" : valor.trim().toUpperCase();
    }
}
