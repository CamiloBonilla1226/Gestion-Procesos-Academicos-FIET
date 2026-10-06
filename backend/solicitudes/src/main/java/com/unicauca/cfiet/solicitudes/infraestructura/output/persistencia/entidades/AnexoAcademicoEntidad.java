package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity
@Table(name = "ANEXO_ACADEMICO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnexoAcademicoEntidad {
    @Id
    @Column(name = "uuidAnexoAcademico", length = 100)
    private String uuidAnexoAcademico;
    @ManyToOne(optional = false)
    @JoinColumn(
            name = "SolicitudAcademica_uuid",
            nullable = false,
            columnDefinition = "varchar(100)",
            foreignKey = @ForeignKey(name = "fk_anexo_solacad")
    )
    private SolicitudAcademicaEntidad solicitudAcademica;
    @ManyToOne
    @JoinColumn(
            name = "TipoAnexoAcademico_uuid",
            columnDefinition = "varchar(100)",
            foreignKey = @ForeignKey(name = "fk_anexo_tipoanexo")
    )
    private TipoAnexoAcademicoEntidad tipoAnexoAcademico;
    @Column(name = "nombreArchivo", length = 255, nullable = false)
    private String nombreArchivo;
    @Column(name = "urlArchivo", length = 400, nullable = false)
    private String urlArchivo;
    @Column(name = "tipoArchivo", length = 100, nullable = false)
    private String tipoArchivo;
    @Column(name = "tamanioBytes", nullable = false)
    private Long tamanioBytes;
    @ManyToOne(optional = false)
    @JoinColumn(
            name = "Usuario_uuid",
            nullable = false,
            columnDefinition = "varchar(100)",
            foreignKey = @ForeignKey(name = "fk_anexo_usuario")
    )
    private UsuarioEntidad usuario;
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "fechaSubida", nullable = false, columnDefinition = "datetime")
    private LocalDateTime fechaSubida;
}
