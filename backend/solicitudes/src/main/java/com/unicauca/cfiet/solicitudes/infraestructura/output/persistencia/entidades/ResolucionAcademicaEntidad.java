package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;

@Entity
@Table(name = "RESOLUCION_ACADEMICA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResolucionAcademicaEntidad implements Persistable<String> {
    @Id
    @Column(name = "SolicitudAcademica_uuid", length = 100)
    private String uuidSolicitudAcademica;
    @MapsId
    @OneToOne(optional = false)
    @JoinColumn(
            name = "SolicitudAcademica_uuid",
            columnDefinition = "varchar(100)",
            foreignKey = @ForeignKey(name = "fk_resolucion_solacad")
    )
    private SolicitudAcademicaEntidad solicitudAcademica;
    @Column(name = "urlArchivo", length = 400, nullable = false)
    private String urlArchivo;
    @Column(name = "nombreArchivo", length = 255, nullable = false)
    private String nombreArchivo;
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "fechaSubida", nullable = false, columnDefinition = "datetime")
    private LocalDateTime fechaSubida;
    @ManyToOne(optional = false)
    @JoinColumn(
            name = "FuncionarioAcademico_uuid",
            nullable = false,
            columnDefinition = "varchar(100)",
            foreignKey = @ForeignKey(name = "fk_resolucion_funcionario")
    )
    private FuncionarioAcademicoEntidad funcionarioAcademico;
    @Transient
    private boolean nuevo;

    @Override
    public String getId() {
        return uuidSolicitudAcademica;
    }

    @Override
    public boolean isNew() {
        return nuevo;
    }
}
