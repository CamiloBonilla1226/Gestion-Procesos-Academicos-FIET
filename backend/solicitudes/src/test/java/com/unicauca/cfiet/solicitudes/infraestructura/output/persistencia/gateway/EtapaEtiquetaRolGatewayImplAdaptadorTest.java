package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.gateway;

import com.unicauca.cfiet.solicitudes.dominio.modelos.EtapaEtiquetaRol;
import com.unicauca.cfiet.solicitudes.dominio.modelos.EtapaSolicitudAcademica;
import com.unicauca.cfiet.solicitudes.dominio.modelos.RolEtiquetaEtapa;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.EtapaEtiquetaRolEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper.EtapaEtiquetaRolOwnMapper;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.EtapaEtiquetaRolRepositorio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EtapaEtiquetaRolGatewayImplAdaptadorTest {

    @Mock
    private EtapaEtiquetaRolRepositorio repositorio;

    @Mock
    private EtapaEtiquetaRolOwnMapper mapper;

    @Test
    void getPorRolUsaLaConsultaConEtapaYConservaElOrden() {
        EtapaEtiquetaRolEntidad aprobada = new EtapaEtiquetaRolEntidad();
        EtapaEtiquetaRolEntidad radicada = new EtapaEtiquetaRolEntidad();
        EtapaEtiquetaRol primera = EtapaEtiquetaRol.builder()
                .etapa(EtapaSolicitudAcademica.builder().codigo("APROBADA").build()).etiqueta("Respondida").build();
        EtapaEtiquetaRol segunda = EtapaEtiquetaRol.builder()
                .etapa(EtapaSolicitudAcademica.builder().codigo("RADICADA").build()).etiqueta("Por Revisar").build();
        when(repositorio.buscarPorRolConEtapa(RolEtiquetaEtapa.FUNCIONARIO)).thenReturn(List.of(aprobada, radicada));
        when(mapper.toDominio(aprobada)).thenReturn(primera);
        when(mapper.toDominio(radicada)).thenReturn(segunda);

        List<EtapaEtiquetaRol> etiquetas = new EtapaEtiquetaRolGatewayImplAdaptador(repositorio, mapper).getPorRol(RolEtiquetaEtapa.FUNCIONARIO);

        assertEquals(List.of(primera, segunda), etiquetas);
        verify(repositorio).buscarPorRolConEtapa(RolEtiquetaEtapa.FUNCIONARIO);
        verifyNoMoreInteractions(repositorio);
    }

    @Test
    void getPorRolSinFilasDevuelveListaVacia() {
        when(repositorio.buscarPorRolConEtapa(RolEtiquetaEtapa.DECANO)).thenReturn(List.of());

        assertTrue(new EtapaEtiquetaRolGatewayImplAdaptador(repositorio, mapper).getPorRol(RolEtiquetaEtapa.DECANO).isEmpty());
    }
}
