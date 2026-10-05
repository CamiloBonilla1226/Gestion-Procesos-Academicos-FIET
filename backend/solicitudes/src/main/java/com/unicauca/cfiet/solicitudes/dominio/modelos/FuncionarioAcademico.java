package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FuncionarioAcademico {
    private String uuidUsuario;
    private Usuario usuario;
    private String dependencia;
    @Builder.Default
    private List<TipoSolicitudAcademica> tiposSolicitud = new ArrayList<>();
}
