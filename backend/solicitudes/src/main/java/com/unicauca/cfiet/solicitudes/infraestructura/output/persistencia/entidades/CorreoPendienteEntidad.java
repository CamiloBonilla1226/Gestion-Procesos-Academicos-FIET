package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "CORREO_PENDIENTE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorreoPendienteEntidad {
    public static final String PENDIENTE = "PENDIENTE";
    public static final String ENVIANDO = "ENVIANDO";
    public static final String ENVIADO = "ENVIADO";
    public static final String ABANDONADO = "ABANDONADO";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCorreo")
    private Long idCorreo;
    @Column(name = "destinatario", length = 255, nullable = false)
    private String destinatario;
    @Column(name = "asunto", length = 500, nullable = false)
    private String asunto;
    @Column(name = "texto", columnDefinition = "longtext", nullable = false)
    private String texto;
    @Column(name = "adjuntos", columnDefinition = "longtext")
    private String adjuntos;
    @Column(name = "estado", length = 20, nullable = false)
    private String estado;
    @Column(name = "intentos", nullable = false)
    private int intentos;
    @Column(name = "ultimoError", length = 1000)
    private String ultimoError;
    @Column(name = "fechaCreacion", nullable = false, columnDefinition = "datetime")
    private LocalDateTime fechaCreacion;
    @Column(name = "fechaReclamo", columnDefinition = "datetime")
    private LocalDateTime fechaReclamo;
    @Column(name = "fechaEnvio", columnDefinition = "datetime")
    private LocalDateTime fechaEnvio;
}
