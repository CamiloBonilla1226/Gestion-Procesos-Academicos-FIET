package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Entity
@Table(name = "funcionarios")
@Getter
@Setter
@SuperBuilder
public class FuncionarioEntidad extends UsuarioEntidad{
    public FuncionarioEntidad(){
        super();
    }
}
