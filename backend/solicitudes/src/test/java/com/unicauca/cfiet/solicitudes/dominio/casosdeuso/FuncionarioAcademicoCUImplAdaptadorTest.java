package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.UsuarioCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.DatosPersonalesGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.FuncionarioAcademicoGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.RolGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.UsuarioGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.modelos.FuncionarioAcademico;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Rol;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoUsuario;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Usuario;
import com.unicauca.cfiet.solicitudes.infraestructura.output.formateador.ExcepcionesFormateadorImplAdaptador;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorEntidadExisteExcepcion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorEntidadNoExisteExcepcion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorMalFormatoExcepcion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorNoInformacionExcepcion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FuncionarioAcademicoCUImplAdaptadorTest {

    private static final String TOKEN = "token";
    private static final String UUID_CREADO = "uuid-usuario-creado";
    private static final String UUID_FUNCIONARIO = "uuid-funcionario";
    private static final String NOMBRE_FUNCIONARIO_ACADEMICO = "Funcionario Académico";
    private static final String TIPO_EMPLEADO_FUNCIONARIO = "Empleado FIET - Funcionario";

    @Mock
    private UsuarioCUIntPuerto usuarioCU;

    @Mock
    private FuncionarioAcademicoGatewayIntPuerto gateway;

    @Mock
    private RolGatewayIntPuerto rolGateway;

    @Mock
    private UsuarioGatewayIntPuerto usuarioGateway;

    @Mock
    private DatosPersonalesGatewayIntPuerto datosPersonalesGateway;

    @Mock
    private LogCUIntPuerto log;

    private FuncionarioAcademicoCUImplAdaptador casoDeUso;

    private final Rol rolFuncionarioAcademico = Rol.builder().uuidRol("rol-fa").nombre(NOMBRE_FUNCIONARIO_ACADEMICO).estado(true).build();
    private final Rol rolDecano = Rol.builder().uuidRol("rol-dec").nombre("Decano").estado(true).build();
    private final TipoUsuario tipoFuncionarioAcademico = TipoUsuario.builder().uuidTipoUsuario("tipo-fa").nombre(TIPO_EMPLEADO_FUNCIONARIO).build();

    @BeforeEach
    void setUp() {
        casoDeUso = new FuncionarioAcademicoCUImplAdaptador(usuarioCU, gateway, rolGateway, usuarioGateway, datosPersonalesGateway,
                new ExcepcionesFormateadorImplAdaptador(), log);
    }

    private FuncionarioAcademico funcionario(String sufijo, String cargo) {
        Usuario usuario = new Usuario();
        usuario.setNombres("Carlos");
        usuario.setApellidos("Lopez");
        usuario.setNumeroDocumento("D" + sufijo);
        usuario.setCorreoElectronico("f" + sufijo + "@unicauca.edu.co");
        usuario.setUsername("u" + sufijo);
        usuario.setRoles(new ArrayList<>(List.of(rolDecano)));
        return FuncionarioAcademico.builder()
                .usuario(usuario)
                .cargo(cargo)
                .build();
    }

    private FuncionarioAcademico registrado(String cargo) {
        FuncionarioAcademico funcionario = funcionario("100", cargo);
        funcionario.setUuidUsuario(UUID_FUNCIONARIO);
        funcionario.getUsuario().setUuidUsuario(UUID_FUNCIONARIO);
        return funcionario;
    }

    private void prepararCreacion() {
        when(rolGateway.getRoles()).thenReturn(List.of(rolDecano, rolFuncionarioAcademico));
        when(usuarioGateway.getTipoUsuarioPorNombre(TIPO_EMPLEADO_FUNCIONARIO)).thenReturn(tipoFuncionarioAcademico);
        when(usuarioCU.crearUsuario(any(Usuario.class), eq("FUNCIONARIOACADEMICO"), eq(TOKEN))).thenAnswer(invocacion -> {
            Usuario usuario = invocacion.getArgument(0);
            usuario.setUuidUsuario(UUID_CREADO);
            return usuario;
        });
        when(gateway.guardar(any(FuncionarioAcademico.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
    }

    private void verificarQueNoSeCreoNada() {
        verifyNoInteractions(usuarioCU, gateway, log);
    }

    @Test
    void crearFuncionarioAcademicoCreaUsuarioAntesDeGuardarConRolYTipoFuncionarioAcademico() {
        prepararCreacion();

        FuncionarioAcademico creado = casoDeUso.crearFuncionarioAcademico(funcionario("1", "Division de Admisiones"), TOKEN);

        InOrder orden = inOrder(usuarioCU, gateway);
        orden.verify(usuarioCU).crearUsuario(any(Usuario.class), eq("FUNCIONARIOACADEMICO"), eq(TOKEN));
        orden.verify(gateway).guardar(any(FuncionarioAcademico.class));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioCU).crearUsuario(captor.capture(), eq("FUNCIONARIOACADEMICO"), eq(TOKEN));
        assertEquals(List.of(rolFuncionarioAcademico), captor.getValue().getRoles());
        assertSame(tipoFuncionarioAcademico, captor.getValue().getObjTipoUsuario());

        assertEquals(UUID_CREADO, creado.getUuidUsuario());
        assertEquals("Division de Admisiones", creado.getCargo());
        verify(log).crearLog(eq("Crear funcionario académico"), anyString(), eq(TOKEN));
    }

    @Test
    void crearFuncionarioAcademicoConRolDecanoEnLaPeticionLoReemplazaPorFuncionarioAcademico() {
        prepararCreacion();

        casoDeUso.crearFuncionarioAcademico(funcionario("1", "Vicerrectoria"), TOKEN);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioCU).crearUsuario(captor.capture(), anyString(), anyString());
        assertTrue(captor.getValue().getRoles().stream().noneMatch(rol -> "Decano".equals(rol.getNombre())));
    }

    @Test
    void crearFuncionarioAcademicoConDocumentoRepetidoNoCreaNada() {
        when(usuarioGateway.existeUsuarioNumeroDocumento("D1")).thenReturn(true);

        assertThrows(ErrorEntidadExisteExcepcion.class,
                () -> casoDeUso.crearFuncionarioAcademico(funcionario("1", "Admisiones"), TOKEN));

        verificarQueNoSeCreoNada();
    }

    @Test
    void crearFuncionarioAcademicoConCorreoRepetidoNoCreaNada() {
        when(usuarioGateway.existeUsuarioCorreo("f1@unicauca.edu.co")).thenReturn(true);

        assertThrows(ErrorEntidadExisteExcepcion.class,
                () -> casoDeUso.crearFuncionarioAcademico(funcionario("1", "Admisiones"), TOKEN));

        verificarQueNoSeCreoNada();
    }

    @Test
    void crearFuncionarioAcademicoConUsernameRepetidoNoCreaNada() {
        when(usuarioGateway.existeUsuarioUsername("u1")).thenReturn(true);

        assertThrows(ErrorEntidadExisteExcepcion.class,
                () -> casoDeUso.crearFuncionarioAcademico(funcionario("1", "Admisiones"), TOKEN));

        verificarQueNoSeCreoNada();
    }

    @Test
    void crearFuncionarioAcademicoSinRolLanzaEntidadNoExisteYNoCreaNada() {
        when(rolGateway.getRoles()).thenReturn(List.of(rolDecano));

        assertThrows(ErrorEntidadNoExisteExcepcion.class,
                () -> casoDeUso.crearFuncionarioAcademico(funcionario("1", "Admisiones"), TOKEN));

        verificarQueNoSeCreoNada();
    }

    @Test
    void crearFuncionarioAcademicoSinTipoUsuarioLanzaEntidadNoExisteYNoCreaNada() {
        when(rolGateway.getRoles()).thenReturn(List.of(rolFuncionarioAcademico));
        when(usuarioGateway.getTipoUsuarioPorNombre(TIPO_EMPLEADO_FUNCIONARIO)).thenReturn(null);

        assertThrows(ErrorEntidadNoExisteExcepcion.class,
                () -> casoDeUso.crearFuncionarioAcademico(funcionario("1", "Admisiones"), TOKEN));

        verificarQueNoSeCreoNada();
    }

    @Test
    void crearFuncionariosAcademicosCreaCadaUno() {
        prepararCreacion();

        List<FuncionarioAcademico> creados = casoDeUso.crearFuncionariosAcademicos(
                List.of(funcionario("1", "Admisiones"), funcionario("2", "Registro")), TOKEN);

        assertEquals(2, creados.size());
        verify(usuarioCU, times(2)).crearUsuario(any(Usuario.class), eq("FUNCIONARIOACADEMICO"), eq(TOKEN));
        verify(gateway, times(2)).guardar(any(FuncionarioAcademico.class));
        verify(log, times(2)).crearLog(eq("Crear funcionario académico"), anyString(), eq(TOKEN));
    }

    @Test
    void crearFuncionariosAcademicosConDocumentoRepetidoEnElLoteNoCreaNada() {
        FuncionarioAcademico segundo = funcionario("2", "Registro");
        segundo.getUsuario().setNumeroDocumento("d1 ");

        assertThrows(ErrorMalFormatoExcepcion.class,
                () -> casoDeUso.crearFuncionariosAcademicos(List.of(funcionario("1", "Admisiones"), segundo), TOKEN));

        verificarQueNoSeCreoNada();
    }

    @Test
    void crearFuncionariosAcademicosConCorreoRepetidoEnElLoteNoCreaNada() {
        FuncionarioAcademico segundo = funcionario("2", "Registro");
        segundo.getUsuario().setCorreoElectronico("F1@unicauca.edu.co");

        assertThrows(ErrorMalFormatoExcepcion.class,
                () -> casoDeUso.crearFuncionariosAcademicos(List.of(funcionario("1", "Admisiones"), segundo), TOKEN));

        verificarQueNoSeCreoNada();
    }

    @Test
    void crearFuncionariosAcademicosConUsernameRepetidoEnElLoteNoCreaNada() {
        FuncionarioAcademico segundo = funcionario("2", "Registro");
        segundo.getUsuario().setUsername("u1");

        assertThrows(ErrorMalFormatoExcepcion.class,
                () -> casoDeUso.crearFuncionariosAcademicos(List.of(funcionario("1", "Admisiones"), segundo), TOKEN));

        verificarQueNoSeCreoNada();
    }

    @Test
    void crearFuncionariosAcademicosConDocumentoYaRegistradoEnElSegundoNoCreaAlPrimero() {
        when(usuarioGateway.existeUsuarioNumeroDocumento(anyString()))
                .thenAnswer(invocacion -> "D2".equals(invocacion.getArgument(0)));

        assertThrows(ErrorEntidadExisteExcepcion.class, () -> casoDeUso.crearFuncionariosAcademicos(
                List.of(funcionario("1", "Admisiones"), funcionario("2", "Registro")), TOKEN));

        verificarQueNoSeCreoNada();
    }

    @Test
    void crearFuncionariosAcademicosConListaVaciaLanzaSinInformacion() {
        assertThrows(ErrorNoInformacionExcepcion.class, () -> casoDeUso.crearFuncionariosAcademicos(List.of(), TOKEN));
        verificarQueNoSeCreoNada();
    }

    @Test
    void getFuncionariosAcademicosPaginadoDevuelveLaPaginaDelGateway() {
        PaginacionRespuestaDTO<FuncionarioAcademico> pagina = new PaginacionRespuestaDTO<>(List.of(registrado("Admisiones")), 1L);
        when(gateway.getPaginado(0, 10)).thenReturn(pagina);

        assertSame(pagina, casoDeUso.getFuncionariosAcademicosPaginado(0, 10));
    }

    @Test
    void getFuncionariosAcademicosPaginadoSinResultadosDevuelvePaginaVacia() {
        when(gateway.getPaginado(0, 10)).thenReturn(new PaginacionRespuestaDTO<>(List.of(), 0L));

        assertTrue(casoDeUso.getFuncionariosAcademicosPaginado(0, 10).getContent().isEmpty());
    }

    @ParameterizedTest
    @CsvSource({"-1, 10", "0, 0", "0, -5"})
    void getFuncionariosAcademicosPaginadoConPaginacionInvalidaLanzaMalFormato(int pagina, int tamanio) {
        assertThrows(ErrorMalFormatoExcepcion.class, () -> casoDeUso.getFuncionariosAcademicosPaginado(pagina, tamanio));
        verify(gateway, never()).getPaginado(anyInt(), anyInt());
    }

    @Test
    void getFuncionariosAcademicosPorFiltroPasaLosFiltrosAlGateway() {
        PaginacionRespuestaDTO<FuncionarioAcademico> pagina = new PaginacionRespuestaDTO<>(List.of(registrado("Admisiones")), 1L);
        when(gateway.getPorFiltro("Carlos", "Lopez", "Admisiones", 0, 5)).thenReturn(pagina);

        assertSame(pagina, casoDeUso.getFuncionariosAcademicosPorFiltro("Carlos", "Lopez", "Admisiones", 0, 5));
    }

    @Test
    void getFuncionariosAcademicosPorFiltroSinResultadosDevuelvePaginaVacia() {
        when(gateway.getPorFiltro(null, null, "Inexistente", 0, 5)).thenReturn(new PaginacionRespuestaDTO<>(List.of(), 0L));

        assertTrue(casoDeUso.getFuncionariosAcademicosPorFiltro(null, null, "Inexistente", 0, 5).getContent().isEmpty());
    }

    @Test
    void getFuncionariosAcademicosPorFiltroConPaginacionInvalidaLanzaMalFormato() {
        assertThrows(ErrorMalFormatoExcepcion.class,
                () -> casoDeUso.getFuncionariosAcademicosPorFiltro("Carlos", null, null, -1, 5));
        verifyNoInteractions(gateway);
    }

    @Test
    void getFuncionarioAcademicoExistenteLoDevuelve() {
        FuncionarioAcademico funcionario = registrado("Admisiones");
        when(gateway.getPorUuid(UUID_FUNCIONARIO)).thenReturn(funcionario);

        assertSame(funcionario, casoDeUso.getFuncionarioAcademico(UUID_FUNCIONARIO));
    }

    @Test
    void getFuncionarioAcademicoInexistenteLanzaEntidadNoExiste() {
        when(gateway.getPorUuid("no-existe")).thenReturn(null);

        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.getFuncionarioAcademico("no-existe"));
    }

    private FuncionarioAcademico edicion(String cargo) {
        return FuncionarioAcademico.builder().usuario(new Usuario()).cargo(cargo).build();
    }

    @Test
    void actualizarFuncionarioAcademicoCambiaDatosPersonalesYCargoSinTocarElUsername() {
        FuncionarioAcademico actual = registrado("Admisiones");
        when(gateway.getPorUuid(UUID_FUNCIONARIO)).thenReturn(actual);
        when(gateway.guardar(any(FuncionarioAcademico.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        FuncionarioAcademico cambios = funcionario("999", " Registro Academico ");
        cambios.getUsuario().setNombres(" Laura ");
        cambios.getUsuario().setTipoDocumento("cédula de ciudadanía");
        cambios.getUsuario().setTelefono("3201112233");
        cambios.getUsuario().setUsername("otro-username");

        FuncionarioAcademico actualizado = casoDeUso.actualizarFuncionarioAcademico(UUID_FUNCIONARIO, cambios, TOKEN);

        assertEquals("Registro Academico", actualizado.getCargo());
        assertEquals("Laura", actualizado.getUsuario().getNombres());
        assertEquals("Lopez", actualizado.getUsuario().getApellidos());
        assertEquals("Cédula de ciudadanía", actualizado.getUsuario().getTipoDocumento());
        assertEquals("D999", actualizado.getUsuario().getNumeroDocumento());
        assertEquals("f999@unicauca.edu.co", actualizado.getUsuario().getCorreoElectronico());
        assertEquals("3201112233", actualizado.getUsuario().getTelefono());
        assertEquals("u100", actualizado.getUsuario().getUsername());
        assertEquals(UUID_FUNCIONARIO, actualizado.getUuidUsuario());
        verify(datosPersonalesGateway).existeNumeroDocumentoEnOtro("D999", UUID_FUNCIONARIO);
        verify(datosPersonalesGateway).existeCorreoEnOtro("f999@unicauca.edu.co", UUID_FUNCIONARIO);
        verify(datosPersonalesGateway).actualizar(eq(UUID_FUNCIONARIO), any(Usuario.class));
        verifyNoInteractions(usuarioCU, usuarioGateway, rolGateway);
        verify(log).crearLog(eq("Actualizar funcionario académico"), anyString(), eq(TOKEN));
    }

    @Test
    void actualizarFuncionarioAcademicoConCamposVaciosConservaLoActual() {
        when(gateway.getPorUuid(UUID_FUNCIONARIO)).thenReturn(registrado("Admisiones"));
        when(gateway.guardar(any(FuncionarioAcademico.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
        FuncionarioAcademico cambios = edicion(" ");
        cambios.getUsuario().setNombres("");

        FuncionarioAcademico actualizado = casoDeUso.actualizarFuncionarioAcademico(UUID_FUNCIONARIO, cambios, TOKEN);

        assertEquals("Admisiones", actualizado.getCargo());
        assertEquals("Carlos", actualizado.getUsuario().getNombres());
        verifyNoInteractions(datosPersonalesGateway);
    }

    @Test
    void actualizarFuncionarioAcademicoConDocumentoDeOtroUsuarioLanzaEntidadExiste() {
        when(gateway.getPorUuid(UUID_FUNCIONARIO)).thenReturn(registrado("Admisiones"));
        when(datosPersonalesGateway.existeNumeroDocumentoEnOtro("D555", UUID_FUNCIONARIO)).thenReturn(true);
        FuncionarioAcademico cambios = edicion("Otro cargo");
        cambios.getUsuario().setNumeroDocumento("D555");

        ErrorEntidadExisteExcepcion error = assertThrows(ErrorEntidadExisteExcepcion.class,
                () -> casoDeUso.actualizarFuncionarioAcademico(UUID_FUNCIONARIO, cambios, TOKEN));

        assertEquals(String.format(MensajesError.ATRIBUTO_UNICO_YA_EXISTE, "Usuario", "numero de documento", "D555"), error.getMessage());
        verify(datosPersonalesGateway, never()).actualizar(anyString(), any());
        verify(gateway, never()).guardar(any());
        verifyNoInteractions(log);
    }

    @Test
    void actualizarFuncionarioAcademicoConCorreoDeOtroUsuarioLanzaEntidadExiste() {
        when(gateway.getPorUuid(UUID_FUNCIONARIO)).thenReturn(registrado("Admisiones"));
        when(datosPersonalesGateway.existeCorreoEnOtro("x@unicauca.edu.co", UUID_FUNCIONARIO)).thenReturn(true);
        FuncionarioAcademico cambios = edicion(null);
        cambios.getUsuario().setCorreoElectronico("x@unicauca.edu.co");

        assertThrows(ErrorEntidadExisteExcepcion.class, () -> casoDeUso.actualizarFuncionarioAcademico(UUID_FUNCIONARIO, cambios, TOKEN));

        verify(gateway, never()).guardar(any());
        verifyNoInteractions(log);
    }

    @Test
    void actualizarFuncionarioAcademicoConTipoDeDocumentoInvalidoLanzaMalFormato() {
        when(gateway.getPorUuid(UUID_FUNCIONARIO)).thenReturn(registrado("Admisiones"));
        FuncionarioAcademico cambios = edicion(null);
        cambios.getUsuario().setTipoDocumento("Licencia");

        assertThrows(ErrorMalFormatoExcepcion.class, () -> casoDeUso.actualizarFuncionarioAcademico(UUID_FUNCIONARIO, cambios, TOKEN));

        verify(gateway, never()).guardar(any());
        verifyNoInteractions(log);
    }

    @Test
    void actualizarFuncionarioAcademicoInexistenteLanzaEntidadNoExiste() {
        when(gateway.getPorUuid("no-existe")).thenReturn(null);

        assertThrows(ErrorEntidadNoExisteExcepcion.class,
                () -> casoDeUso.actualizarFuncionarioAcademico("no-existe", funcionario("2", "Registro"), TOKEN));

        verify(gateway, never()).guardar(any());
        verifyNoInteractions(log);
    }
}
