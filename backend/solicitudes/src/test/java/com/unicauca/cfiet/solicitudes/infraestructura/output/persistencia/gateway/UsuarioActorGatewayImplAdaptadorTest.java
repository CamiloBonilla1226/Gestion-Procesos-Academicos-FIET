package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.gateway;

import com.unicauca.cfiet.solicitudes.dominio.modelos.Rol;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Usuario;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.UsuarioActorRepositorio;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.UsuarioActorRepositorio.FilaActor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioActorGatewayImplAdaptadorTest {

    @Mock
    private UsuarioActorRepositorio repositorio;

    private UsuarioActorGatewayImplAdaptador gateway;

    @BeforeEach
    void setUp() {
        gateway = new UsuarioActorGatewayImplAdaptador(repositorio);
    }

    private FilaActor fila(String uuidRol, String nombreRol) {
        return new FilaActor() {
            public String getUuidUsuario() { return "usr-1"; }
            public String getUsername() { return "alumno"; }
            public String getUuidRol() { return uuidRol; }
            public String getNombreRol() { return nombreRol; }
            public String getDescripcionRol() { return uuidRol == null ? null : "desc " + nombreRol; }
            public Boolean getEstadoRol() { return uuidRol == null ? null : true; }
        };
    }

    @Test
    void getUsuarioArmaUsuarioConTodosSusRoles() {
        when(repositorio.buscarPorUuid("usr-1")).thenReturn(List.of(fila("rol-1", "Estudiante"), fila("rol-2", "Decano")));

        Usuario usuario = gateway.getUsuario("usr-1");

        assertEquals("usr-1", usuario.getUuidUsuario());
        assertEquals("alumno", usuario.getUsername());
        assertEquals(List.of("Estudiante", "Decano"), usuario.getRoles().stream().map(Rol::getNombre).toList());
        Rol primero = usuario.getRoles().get(0);
        assertEquals("rol-1", primero.getUuidRol());
        assertEquals("desc Estudiante", primero.getDescripcion());
        assertTrue(primero.getEstado());
        assertTrue(usuario.getLogs().isEmpty());
    }

    @Test
    void getUsuarioPorUsernameUsaLaConsultaPorUsername() {
        when(repositorio.buscarPorUsername("alumno")).thenReturn(List.of(fila("rol-1", "Estudiante")));

        Usuario usuario = gateway.getUsuarioPorUsername("alumno");

        assertEquals("usr-1", usuario.getUuidUsuario());
        assertEquals(1, usuario.getRoles().size());
        verify(repositorio, never()).buscarPorUuid(any());
    }

    @Test
    void usuarioSinRolesQuedaConListaVacia() {
        when(repositorio.buscarPorUuid("usr-1")).thenReturn(List.of(fila(null, null)));

        Usuario usuario = gateway.getUsuario("usr-1");

        assertNotNull(usuario.getRoles());
        assertTrue(usuario.getRoles().isEmpty());
    }

    @Test
    void usuarioInexistenteDevuelveNulo() {
        when(repositorio.buscarPorUuid("nadie")).thenReturn(List.of());
        when(repositorio.buscarPorUsername("nadie")).thenReturn(List.of());

        assertNull(gateway.getUsuario("nadie"));
        assertNull(gateway.getUsuarioPorUsername("nadie"));
    }
}
