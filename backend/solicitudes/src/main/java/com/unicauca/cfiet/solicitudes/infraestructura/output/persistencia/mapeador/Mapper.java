package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Configuration
public class Mapper {

    @Bean("mapeadorSimple")
    public ModelMapper crearMapeadorSimple() {
        return new ModelMapper();
    }
}
