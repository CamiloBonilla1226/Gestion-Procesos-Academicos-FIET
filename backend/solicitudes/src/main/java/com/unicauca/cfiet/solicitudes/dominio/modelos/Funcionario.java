package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Representa un usuario del sistema con rol de funcionario.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Getter
@Setter
@SuperBuilder
public class Funcionario extends Usuario{

    public Funcionario(){
        super();
    }
}
