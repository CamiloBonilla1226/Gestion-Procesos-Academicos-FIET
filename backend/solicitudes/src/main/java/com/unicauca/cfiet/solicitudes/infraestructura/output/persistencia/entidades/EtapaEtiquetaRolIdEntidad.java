package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades;

import com.unicauca.cfiet.solicitudes.dominio.modelos.RolEtiquetaEtapa;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class EtapaEtiquetaRolIdEntidad implements Serializable {
    @Column(name = "Etapa_uuid", length = 100)
    private String etapaUuid;
    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false, columnDefinition = "enum('ESTUDIANTE','FUNCIONARIO','DECANO')")
    private RolEtiquetaEtapa rol;
}
