package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "NOTIFICACION_DOCENTE",
        uniqueConstraints = @UniqueConstraint(name = "uk_notifdoc_uuid", columnNames = "uuidNotificacion"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificacionDocenteEntidad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idNotificacion")
    private Long idNotificacion;
    @Column(name = "uuidNotificacion", length = 100, nullable = false)
    private String uuidNotificacion;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "SolicitudAcademica_uuid",
            nullable = false,
            columnDefinition = "varchar(100)",
            foreignKey = @ForeignKey(name = "fk_notifdoc_solacad")
    )
    private SolicitudAcademicaEntidad solicitudAcademica;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "Docente_uuid",
            nullable = false,
            columnDefinition = "varchar(100)",
            foreignKey = @ForeignKey(name = "fk_notifdoc_usuario")
    )
    private UsuarioEntidad docente;
    @Column(name = "titulo", length = 255, nullable = false)
    private String titulo;
    @Column(name = "fechaCreacion", nullable = false, columnDefinition = "datetime")
    private LocalDateTime fechaCreacion;
    @Column(name = "fechaLectura", columnDefinition = "datetime")
    private LocalDateTime fechaLectura;
}
