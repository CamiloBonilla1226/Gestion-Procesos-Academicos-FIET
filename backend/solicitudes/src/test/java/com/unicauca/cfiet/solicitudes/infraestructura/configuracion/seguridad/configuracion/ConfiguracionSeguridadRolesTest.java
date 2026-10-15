package com.unicauca.cfiet.solicitudes.infraestructura.configuracion.seguridad.configuracion;

import com.unicauca.cfiet.solicitudes.infraestructura.configuracion.seguridad.jwt.JwtFiltroAutenticacion;
import com.unicauca.cfiet.solicitudes.infraestructura.configuracion.seguridad.jwt.JwtServicio;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.RolEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.UsuarioEntidad;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(properties = "server.port=0")
@WebMvcTest(controllers = ConfiguracionSeguridadRolesTest.ControladorComodin.class)
@Import({ConfiguracionSeguridad.class, JwtFiltroAutenticacion.class, JwtServicio.class,
        CustomAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class,
        ConfiguracionSeguridadRolesTest.ControladorComodin.class})
class ConfiguracionSeguridadRolesTest {

    private static final String BASE = "/api/unicauca/fiet/consejo/";
    private static final String UUID = "0b8f3c1e-2a4d-4e6f-8a1b-3c5d7e9f1a2b";
    private static final String SG = "Secretario General";
    private static final String DECANO = "Decano";
    private static final String FUNCIONARIO = "Funcionario";
    private static final String SECRETARIA = "Secretaria Decanatura FIET";
    private static final String ESTUDIANTE = "Estudiante";
    private static final String FUNCIONARIO_ACADEMICO = "Funcionario Académico";
    private static final String DOCENTE = "Docente";
    private static final String DESHABILITADO = "deshabilitado";

    @RestController
    static class ControladorComodin {
        @RequestMapping("/**")
        public String responder() {
            return "ok";
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtServicio jwtServicio;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private AuthenticationProvider authenticationProvider;

    private final Map<String, String> tokens = new HashMap<>();

    @BeforeEach
    void setUp() {
        for (String rol : List.of(SG, DECANO, FUNCIONARIO, SECRETARIA, ESTUDIANTE, FUNCIONARIO_ACADEMICO, DOCENTE))
            registrar(rol, rol, true);
        registrar(DESHABILITADO, ESTUDIANTE, false);
    }

    private void registrar(String clave, String rol, boolean habilitado) {
        String username = "user-" + clave.replace(' ', '-');
        UsuarioEntidad usuario = UsuarioEntidad.builder()
                .uuidUsuario("uuid-" + username)
                .nombres("n")
                .apellidos("a")
                .estado(habilitado)
                .username(username)
                .password("x")
                .roles(List.of(RolEntidad.builder().uuidRol("rol-" + rol).nombre(rol).estado(true).build()))
                .build();
        when(userDetailsService.loadUserByUsername(username)).thenReturn(usuario);
        tokens.put(clave, jwtServicio.getToken(usuario));
    }

    private MockHttpServletRequestBuilder llamar(String metodo, String ruta, String rol) {
        MockHttpServletRequestBuilder peticion = request(HttpMethod.valueOf(metodo), BASE + ruta.replace("{uuid}", UUID));
        if (rol != null)
            peticion.header("Authorization", "Bearer " + tokens.get(rol));
        return peticion;
    }

    static Stream<Arguments> cerradosParaEstudiante() {
        return Stream.of(
                Arguments.of("GET", "solicitudes"),
                Arguments.of("GET", "solicitudes/paginado"),
                Arguments.of("GET", "solicitudes/{uuid}"),
                Arguments.of("PUT", "solicitudes/{uuid}"),
                Arguments.of("GET", "solicitudes/funcionario/{uuid}"),
                Arguments.of("GET", "solicitudes/buscar"),
                Arguments.of("GET", "solicitudes/buscar/solicitante"),
                Arguments.of("GET", "solicitudes/fun/buscar/"),
                Arguments.of("GET", "solicitudes/anexos/download"),
                Arguments.of("GET", "solicitudes/exportar"),
                Arguments.of("GET", "solicitudes/exportar/respuestas"),
                Arguments.of("GET", "solicitudes/exportar/reunion"),
                Arguments.of("GET", "respuestas/paginado/funcionario"),
                Arguments.of("GET", "respuestas/paginado/funcionario-nombre-solicitud"),
                Arguments.of("PATCH", "respuestas/{uuid}/archivo"),
                Arguments.of("POST", "respuestas/{uuid}"),
                Arguments.of("GET", "solicitudes/orden-del-dia"),
                Arguments.of("GET", "usuarios"),
                Arguments.of("GET", "logs"),
                Arguments.of("GET", "roles"),
                Arguments.of("GET", "tipos/solicitudes")
        );
    }

    @ParameterizedTest
    @MethodSource("cerradosParaEstudiante")
    void estudianteRecibe403EnLosEndpointsCerradosDeJulian(String metodo, String ruta) throws Exception {
        mockMvc.perform(llamar(metodo, ruta, ESTUDIANTE)).andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @MethodSource("cerradosParaEstudiante")
    void sinTokenLosEndpointsCerradosResponden401(String metodo, String ruta) throws Exception {
        mockMvc.perform(llamar(metodo, ruta, null)).andExpect(status().isUnauthorized());
    }

    static Stream<Arguments> cerradosParaFuncionarioAcademicoYDocente() {
        return Stream.of(
                Arguments.of("GET", "solicitudes/paginado"),
                Arguments.of("GET", "solicitudes/{uuid}"),
                Arguments.of("PUT", "solicitudes/{uuid}"),
                Arguments.of("GET", "solicitudes/exportar"),
                Arguments.of("PATCH", "respuestas/{uuid}/archivo"),
                Arguments.of("GET", "respuestas/paginado/funcionario")
        );
    }

    @ParameterizedTest
    @MethodSource("cerradosParaFuncionarioAcademicoYDocente")
    void funcionarioAcademicoYDocenteTambienReciben403(String metodo, String ruta) throws Exception {
        mockMvc.perform(llamar(metodo, ruta, FUNCIONARIO_ACADEMICO)).andExpect(status().isForbidden());
        mockMvc.perform(llamar(metodo, ruta, DOCENTE)).andExpect(status().isForbidden());
    }

    static Stream<Arguments> flujosLegitimos() {
        return Stream.of(
                Arguments.of(ESTUDIANTE, "GET", "solicitudes-academicas/estudiante"),
                Arguments.of(ESTUDIANTE, "GET", "solicitudes-academicas/{uuid}"),
                Arguments.of(ESTUDIANTE, "GET", "cancelaciones-matricula/formulario"),
                Arguments.of(ESTUDIANTE, "POST", "cancelaciones-asignatura"),
                Arguments.of(ESTUDIANTE, "POST", "examenes-supletorios/{uuid}/estudiante/comprobante"),
                Arguments.of(ESTUDIANTE, "GET", "estudiantes/mis-asignaturas"),
                Arguments.of(ESTUDIANTE, "GET", "catalogos-academicos/programas"),
                Arguments.of(ESTUDIANTE, "GET", "usuarios/{uuid}"),
                Arguments.of(ESTUDIANTE, "PATCH", "usuarios/{uuid}"),
                Arguments.of(FUNCIONARIO_ACADEMICO, "GET", "solicitudes-academicas/funcionario"),
                Arguments.of(FUNCIONARIO_ACADEMICO, "POST", "cancelaciones-asignatura/{uuid}/funcionario/remitir"),
                Arguments.of(FUNCIONARIO_ACADEMICO, "POST", "solicitudes-academicas/{uuid}/resolucion"),
                Arguments.of(FUNCIONARIO_ACADEMICO, "GET", "estudiantes/paginado"),
                Arguments.of(FUNCIONARIO_ACADEMICO, "GET", "usuarios/{uuid}"),
                Arguments.of(DECANO, "GET", "solicitudes-academicas/decano"),
                Arguments.of(DECANO, "POST", "cancelaciones-matricula/{uuid}/decano/aprobar"),
                Arguments.of(DECANO, "POST", "examenes-supletorios/{uuid}/decano/rechazar"),
                Arguments.of(DECANO, "POST", "solicitudes"),
                Arguments.of(DECANO, "GET", "solicitudes/orden-del-dia/estado"),
                Arguments.of(DECANO, "GET", "solicitudes/{uuid}"),
                Arguments.of(DECANO, "GET", "usuarios/{uuid}"),
                Arguments.of(FUNCIONARIO, "GET", "solicitudes/funcionario/{uuid}"),
                Arguments.of(FUNCIONARIO, "GET", "solicitudes/fun/buscar/"),
                Arguments.of(FUNCIONARIO, "GET", "solicitudes/{uuid}"),
                Arguments.of(FUNCIONARIO, "PUT", "solicitudes/{uuid}"),
                Arguments.of(FUNCIONARIO, "GET", "respuestas/paginado/funcionario"),
                Arguments.of(FUNCIONARIO, "PATCH", "respuestas/{uuid}/archivo"),
                Arguments.of(FUNCIONARIO, "GET", "respuestas/{uuid}/archivo.pdf"),
                Arguments.of(SECRETARIA, "GET", "respuestas/paginado"),
                Arguments.of(SECRETARIA, "GET", "respuestas/paginado/nombre-solicitud"),
                Arguments.of(SECRETARIA, "GET", "respuestas/{uuid}"),
                Arguments.of(SECRETARIA, "GET", "respuestas/{uuid}/respuesta.pdf"),
                Arguments.of(SG, "GET", "solicitudes/paginado"),
                Arguments.of(SG, "GET", "solicitudes/buscar"),
                Arguments.of(SG, "GET", "solicitudes/estado"),
                Arguments.of(SG, "GET", "solicitudes/anexos/download"),
                Arguments.of(SG, "GET", "solicitudes/anexos/{uuid}/anexo.pdf"),
                Arguments.of(SG, "GET", "solicitudes/exportar"),
                Arguments.of(SG, "GET", "solicitudes/exportar/reunion"),
                Arguments.of(SG, "PATCH", "usuarios/{uuid}"),
                Arguments.of(DOCENTE, "POST", "solicitudes"),
                Arguments.of(DOCENTE, "GET", "solicitudes/orden-del-dia/estado"),
                Arguments.of(DOCENTE, "GET", "tipos/solicitudes/perfil")
        );
    }

    @ParameterizedTest
    @MethodSource("flujosLegitimos")
    void losFlujosLegitimosSiguenPasando(String rol, String metodo, String ruta) throws Exception {
        mockMvc.perform(llamar(metodo, ruta, rol)).andExpect(status().isOk());
    }

    static Stream<Arguments> publicos() {
        return Stream.of(
                Arguments.of("POST", "sesiones"),
                Arguments.of("POST", "solicitudes/public"),
                Arguments.of("GET", "tipos/solicitudes/perfil"),
                Arguments.of("GET", "tipos/solicitudes/perfil/paginado"),
                Arguments.of("GET", "tipos/solicitudes/perfil/filtro"),
                Arguments.of("GET", "tipos/solicitudes/{uuid}"),
                Arguments.of("GET", "tipos/solicitudes/paginado"),
                Arguments.of("GET", "tipos/solicitudes/filtro")
        );
    }

    @ParameterizedTest
    @MethodSource("publicos")
    void losEndpointsPublicosSiguenAbiertos(String metodo, String ruta) throws Exception {
        mockMvc.perform(llamar(metodo, ruta, null)).andExpect(status().isOk());
    }

    static Stream<Arguments> cerradosEntreRoles() {
        return Stream.of(
                Arguments.of(ESTUDIANTE, "POST", "cancelaciones-matricula/{uuid}/funcionario/remitir"),
                Arguments.of(ESTUDIANTE, "POST", "examenes-supletorios/{uuid}/decano/aprobar"),
                Arguments.of(FUNCIONARIO_ACADEMICO, "GET", "cancelaciones-matricula/formulario"),
                Arguments.of(DECANO, "POST", "examenes-supletorios/{uuid}/estudiante/comprobante"),
                Arguments.of(SG, "GET", "solicitudes-academicas/{uuid}"),
                Arguments.of(FUNCIONARIO, "GET", "solicitudes-academicas/funcionario"),
                Arguments.of(SECRETARIA, "PUT", "solicitudes/{uuid}"),
                Arguments.of(SECRETARIA, "GET", "solicitudes/{uuid}"),
                Arguments.of(SECRETARIA, "GET", "respuestas/paginado/funcionario"),
                Arguments.of(SECRETARIA, "GET", "respuestas/paginado/funcionario-nombre-solicitud"),
                Arguments.of(SECRETARIA, "PATCH", "respuestas/{uuid}/archivo")
        );
    }

    @ParameterizedTest
    @MethodSource("cerradosEntreRoles")
    void losRolesSinUsoRealSiguenCerrados(String rol, String metodo, String ruta) throws Exception {
        mockMvc.perform(llamar(metodo, ruta, rol)).andExpect(status().isForbidden());
    }

    @Test
    void unUsuarioDeshabilitadoNoPuedeUsarUnTokenYaEmitido() throws Exception {
        mockMvc.perform(llamar("GET", "usuarios/{uuid}", DESHABILITADO)).andExpect(status().isUnauthorized());
        mockMvc.perform(llamar("GET", "solicitudes-academicas/estudiante", DESHABILITADO)).andExpect(status().isUnauthorized());
    }

    @Test
    void elMismoUsuarioHabilitadoSiPasa() throws Exception {
        mockMvc.perform(llamar("GET", "solicitudes-academicas/estudiante", ESTUDIANTE)).andExpect(status().isOk());
    }
}
