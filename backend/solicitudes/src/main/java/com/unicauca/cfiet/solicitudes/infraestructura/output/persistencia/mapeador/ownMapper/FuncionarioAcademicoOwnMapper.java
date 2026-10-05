package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper;

import com.unicauca.cfiet.solicitudes.dominio.modelos.FuncionarioAcademico;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Usuario;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.FuncionarioAcademicoEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.UsuarioEntidad;
import org.springframework.stereotype.Service;

@Service
public class FuncionarioAcademicoOwnMapper implements OwnMapper<FuncionarioAcademico, FuncionarioAcademicoEntidad> {

    @Override
    public FuncionarioAcademico toDominio(FuncionarioAcademicoEntidad source) {
        return FuncionarioAcademico.builder()
                .uuidUsuario(source.getUuidUsuario())
                .usuario(usuarioDominio(source.getUsuario()))
                .dependencia(source.getDependencia())
                .build();
    }

    @Override
    public FuncionarioAcademicoEntidad toEntidad(FuncionarioAcademico source) {
        return FuncionarioAcademicoEntidad.builder()
                .uuidUsuario(source.getUuidUsuario())
                .dependencia(source.getDependencia())
                .build();
    }

    private Usuario usuarioDominio(UsuarioEntidad source) {
        if (source == null) return null;
        return Usuario.builder()
                .uuidUsuario(source.getUuidUsuario())
                .nombres(source.getNombres())
                .apellidos(source.getApellidos())
                .estado(source.getEstado())
                .tipoDocumento(source.getTipoDocumento())
                .numeroDocumento(source.getNumeroDocumento())
                .telefono(source.getTelefono())
                .correoElectronico(source.getCorreoElectronico())
                .username(source.getUsername())
                .build();
    }
}
