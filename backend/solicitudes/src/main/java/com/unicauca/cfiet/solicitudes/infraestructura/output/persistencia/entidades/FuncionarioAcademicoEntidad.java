package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "FUNCIONARIO_ACADEMICO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FuncionarioAcademicoEntidad implements Persistable<String> {
    @Id
    @Column(name = "Usuario_uuid", length = 100)
    private String uuidUsuario;
    @MapsId
    @OneToOne(optional = false)
    @JoinColumn(
            name = "Usuario_uuid",
            columnDefinition = "varchar(100)",
            foreignKey = @ForeignKey(name = "fk_funcionarioacademico_usuario")
    )
    private UsuarioEntidad usuario;
    @Column(name = "dependencia", length = 100, nullable = false)
    private String dependencia;
    @Transient
    private boolean nuevo;

    @Override
    public String getId() {
        return uuidUsuario;
    }

    @Override
    public boolean isNew() {
        return nuevo;
    }
}
