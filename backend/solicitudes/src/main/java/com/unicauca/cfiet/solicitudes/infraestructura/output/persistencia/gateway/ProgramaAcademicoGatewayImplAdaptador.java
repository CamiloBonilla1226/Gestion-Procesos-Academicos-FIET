package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.gateway;

import com.unicauca.cfiet.solicitudes.aplicacion.output.ProgramaAcademicoGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.ProgramaAcademico;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.ProgramaAcademicoEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper.ProgramaAcademicoOwnMapper;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.ProgramaAcademicoRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProgramaAcademicoGatewayImplAdaptador implements ProgramaAcademicoGatewayIntPuerto {
    private final ProgramaAcademicoRepositorio repositorio;
    private final ProgramaAcademicoOwnMapper mapper;

    @Override
    public List<ProgramaAcademico> getProgramas() {
        return repositorio.findAllByOrderByNombreAsc().stream()
                .map(mapper::toDominio)
                .toList();
    }

    @Override
    public ProgramaAcademico getPorUuid(String uuidProgramaAcademico) {
        if (uuidProgramaAcademico == null)
            return null;
        return repositorio.findById(uuidProgramaAcademico)
                .map(mapper::toDominio)
                .orElse(null);
    }

    @Override
    @Transactional
    public ProgramaAcademico bloquear(String uuidProgramaAcademico) {
        if (uuidProgramaAcademico == null)
            return null;
        return repositorio.findParaActualizar(uuidProgramaAcademico)
                .map(mapper::toDominio)
                .orElse(null);
    }

    @Override
    @Transactional
    public void guardarUltimoConsecutivo(String uuidProgramaAcademico, int ultimoConsecutivo) {
        ProgramaAcademicoEntidad programa = repositorio.findParaActualizar(uuidProgramaAcademico).orElseThrow();
        programa.setUltimoConsecutivoAsignatura(ultimoConsecutivo);
        repositorio.save(programa);
    }
}
