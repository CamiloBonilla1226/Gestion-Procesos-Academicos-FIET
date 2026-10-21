package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCatalogosAcademicos;

import com.unicauca.cfiet.solicitudes.dominio.modelos.AmbitoSituacionAcademica;
import com.unicauca.cfiet.solicitudes.dominio.modelos.SituacionAcademicaAsignatura;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCatalogosAcademicos.DTORespuesta.SituacionAcademicaAsignaturaDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCatalogosAcademicos.mapeador.MapperCatalogoAcademicoInfraestructuraDominio;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.SituacionAcademicaAsignaturaEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper.SituacionAcademicaAsignaturaOwnMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MapperCatalogoAcademicoInfraestructuraDominioTest {

    @Test
    void lasSituacionesSalenConSuListaYLaRetiradaSinLista() {
        List<SituacionAcademicaAsignaturaDTORespuesta> dtos = new MapperCatalogoAcademicoInfraestructuraDominio().mapearSituaciones(List.of(
                SituacionAcademicaAsignatura.builder().uuidSituacionAcademica("s1").codigo("R1").nombre("Cursada por primera vez")
                        .ambito(AmbitoSituacionAcademica.MATRICULA).build(),
                SituacionAcademicaAsignatura.builder().uuidSituacionAcademica("s2").codigo("C2").nombre("Perdida")
                        .ambito(AmbitoSituacionAcademica.CANCELACION).build(),
                SituacionAcademicaAsignatura.builder().uuidSituacionAcademica("s0").codigo("R0").nombre("No cursada").build()));

        assertEquals(List.of("MATRICULA", "CANCELACION"), dtos.stream().limit(2).map(SituacionAcademicaAsignaturaDTORespuesta::getAmbito).toList());
        assertNull(dtos.get(2).getAmbito());
        assertEquals("Perdida", dtos.get(1).getNombre());
    }

    @Test
    void elMapperDePersistenciaConservaLaLista() {
        SituacionAcademicaAsignaturaOwnMapper mapper = new SituacionAcademicaAsignaturaOwnMapper();
        SituacionAcademicaAsignaturaEntidad entidad = SituacionAcademicaAsignaturaEntidad.builder().uuidSituacionAcademica("s3")
                .codigo("R1R").nombre("Recursada por primera vez").ambito(AmbitoSituacionAcademica.MATRICULA).build();

        SituacionAcademicaAsignatura dominio = mapper.toDominio(entidad);

        assertEquals(AmbitoSituacionAcademica.MATRICULA, dominio.getAmbito());
        assertEquals(AmbitoSituacionAcademica.MATRICULA, mapper.toEntidad(dominio).getAmbito());
        assertNull(mapper.toDominio(SituacionAcademicaAsignaturaEntidad.builder().uuidSituacionAcademica("s0").codigo("R0").nombre("No cursada").build())
                .getAmbito());
    }
}
