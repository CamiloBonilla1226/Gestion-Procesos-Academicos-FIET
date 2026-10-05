package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.AsignacionFuncionarioAcademicoCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.ExcepcionesFormateadorIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.FuncionarioAcademicoGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.TipoSolicitudAcademicaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.FuncionarioAcademico;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoSolicitudAcademica;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;

public class AsignacionFuncionarioAcademicoCUImplAdaptador implements AsignacionFuncionarioAcademicoCUIntPuerto {
    private static final String TIPO_SOLICITUD_ACADEMICA = "Tipo de solicitud académica";
    private static final String FUNCIONARIO_ACADEMICO = "Funcionario Académico";

    private final TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway;
    private final FuncionarioAcademicoGatewayIntPuerto funcionarioGateway;
    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;
    private final LogCUIntPuerto log;

    public AsignacionFuncionarioAcademicoCUImplAdaptador(TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway,
                                                         FuncionarioAcademicoGatewayIntPuerto funcionarioGateway,
                                                         ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                         LogCUIntPuerto log) {
        this.tipoSolicitudGateway = tipoSolicitudGateway;
        this.funcionarioGateway = funcionarioGateway;
        this.formateadorExcepciones = formateadorExcepciones;
        this.log = log;
    }

    @Override
    public TipoSolicitudAcademica asignarFuncionarioAcademico(String uuidTipoSolicitudAcademica, String uuidFuncionarioAcademico, String token) {
        TipoSolicitudAcademica tipo = tieneTexto(uuidTipoSolicitudAcademica) ? tipoSolicitudGateway.getPorUuid(uuidTipoSolicitudAcademica) : null;
        if (tipo == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(
                    MensajesError.ENTIDAD_NO_ENCONTRADA, TIPO_SOLICITUD_ACADEMICA, uuidTipoSolicitudAcademica));

        FuncionarioAcademico funcionario = tieneTexto(uuidFuncionarioAcademico) ? funcionarioGateway.getPorUuid(uuidFuncionarioAcademico) : null;
        if (funcionario == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(
                    MensajesError.ENTIDAD_NO_ENCONTRADA, FUNCIONARIO_ACADEMICO, uuidFuncionarioAcademico));

        String anterior = tipo.getFuncionarioAcademico() == null ? null : tipo.getFuncionarioAcademico().getUuidUsuario();
        TipoSolicitudAcademica asignado = tipoSolicitudGateway.asignarFuncionarioAcademico(uuidTipoSolicitudAcademica, uuidFuncionarioAcademico);
        log.crearLog("Asignar funcionario académico a tipo de solicitud",
                String.format("Tipo de solicitud académica %s (%s) asignado al funcionario académico %s; antes lo atendía %s",
                        tipo.getNombre(), uuidTipoSolicitudAcademica, uuidFuncionarioAcademico, anterior),
                token);
        return asignado;
    }

    private boolean tieneTexto(String valor) {
        return valor != null && !valor.isBlank();
    }
}
