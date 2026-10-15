package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.gateway;

import com.unicauca.cfiet.solicitudes.aplicacion.output.DocenteGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.ApplicationConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Docente;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.UsuarioEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper.DocenteOwnMapper;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.DocenteRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DocenteGatewayImplAdaptador implements DocenteGatewayIntPuerto {
    private final DocenteRepositorio repositorio;
    private final DocenteOwnMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public PaginacionRespuestaDTO<Docente> buscar(String nombre, String numeroDocumento, String username, int pagina, int tamanio) {
        Page<UsuarioEntidad> page = repositorio.buscarActivosPorRol(ApplicationConstantes.DOCENTE_ROL, nombre, numeroDocumento, username,
                PageRequest.of(pagina, tamanio, Sort.by("apellidos", "nombres").ascending()));
        return new PaginacionRespuestaDTO<>(page.getContent().stream().map(mapper::toDominio).toList(), page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Docente getActivo(String uuidUsuario) {
        return repositorio.buscarActivoPorRol(uuidUsuario, ApplicationConstantes.DOCENTE_ROL).map(mapper::toDominio).orElse(null);
    }
}
