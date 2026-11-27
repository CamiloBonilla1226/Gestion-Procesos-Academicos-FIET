package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.TipoSolicitudConsejoCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.ExcepcionesFormateadorIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.FuncionarioAcademicoGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.TipoSolicitudAcademicaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.TipoSolicitudConsejoGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.UsuarioGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.RemisionConsejoConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.EncargadoTipoSolicitud;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Funcionario;
import com.unicauca.cfiet.solicitudes.dominio.modelos.FuncionarioAcademico;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoProcesoAcademico;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoSolicitudAcademica;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoSolicitudConsejo;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Usuario;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;

import java.util.ArrayList;
import java.util.List;

public class TipoSolicitudConsejoCUImplAdaptador implements TipoSolicitudConsejoCUIntPuerto {
    private static final String PROCESO_ACADEMICO = "Proceso académico";

    private final TipoSolicitudConsejoGatewayIntPuerto gateway;
    private final TipoSolicitudAcademicaGatewayIntPuerto tipoAcademicoGateway;
    private final FuncionarioAcademicoGatewayIntPuerto funcionarioAcademicoGateway;
    private final UsuarioGatewayIntPuerto usuarioGateway;
    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;
    private final LogCUIntPuerto log;

    public TipoSolicitudConsejoCUImplAdaptador(TipoSolicitudConsejoGatewayIntPuerto gateway,
                                               TipoSolicitudAcademicaGatewayIntPuerto tipoAcademicoGateway,
                                               FuncionarioAcademicoGatewayIntPuerto funcionarioAcademicoGateway,
                                               UsuarioGatewayIntPuerto usuarioGateway,
                                               ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                               LogCUIntPuerto log) {
        this.gateway = gateway;
        this.tipoAcademicoGateway = tipoAcademicoGateway;
        this.funcionarioAcademicoGateway = funcionarioAcademicoGateway;
        this.usuarioGateway = usuarioGateway;
        this.formateadorExcepciones = formateadorExcepciones;
        this.log = log;
    }

    @Override
    public TipoSolicitudConsejo prepararEnlace(String uuidTipoSolicitud, String uuidProcesoAcademico, String uuidEncargado) {
        String uuidProceso = uuidProcesoAcademico;
        if (uuidProceso == null) {
            TipoSolicitudConsejo actual = uuidTipoSolicitud == null ? null : gateway.getPorTipoSolicitud(uuidTipoSolicitud);
            if (actual == null)
                return null;
            uuidProceso = actual.getTipoSolicitudAcademica().getUuidTipoSolicitudAcademica();
        }
        if (uuidProceso.isBlank())
            return null;
        TipoSolicitudAcademica proceso = procesoConConsejo(uuidProceso.trim());
        TipoSolicitudConsejo existente = gateway.getPorTipoAcademico(proceso.getUuidTipoSolicitudAcademica());
        if (existente != null && !existente.getUuidTipoSolicitud().equals(uuidTipoSolicitud))
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.TIPO_CONSEJO_YA_ENLAZADO, proceso.getNombre()));
        FuncionarioAcademico encargado = uuidEncargado == null || uuidEncargado.isBlank()
                ? null : funcionarioAcademicoGateway.getPorUuid(uuidEncargado.trim());
        return TipoSolicitudConsejo.builder()
                .uuidTipoSolicitud(uuidTipoSolicitud)
                .tipoSolicitudAcademica(proceso)
                .encargadoAcademico(encargado)
                .build();
    }

    @Override
    public TipoSolicitudConsejo guardarEnlace(String uuidTipoSolicitud, TipoSolicitudConsejo enlace, String token) {
        if (enlace == null) {
            if (gateway.getPorTipoSolicitud(uuidTipoSolicitud) != null) {
                gateway.eliminarPorTipoSolicitud(uuidTipoSolicitud);
                log.crearLog("Desenlazar tipo de solicitud del Consejo", String.format("Tipo de solicitud %s sin proceso académico", uuidTipoSolicitud), token);
            }
            return null;
        }
        enlace.setUuidTipoSolicitud(uuidTipoSolicitud);
        TipoSolicitudConsejo guardado = gateway.guardar(enlace);
        log.crearLog("Enlazar tipo de solicitud al Consejo", String.format("Tipo de solicitud %s enlazado a %s, encargado académico %s",
                uuidTipoSolicitud, enlace.getTipoSolicitudAcademica().getNombre(),
                enlace.getEncargadoAcademico() == null ? "ninguno" : enlace.getEncargadoAcademico().getUuidUsuario()), token);
        return guardado;
    }

    @Override
    public TipoSolicitudConsejo getEnlace(String uuidTipoSolicitud) {
        return gateway.getPorTipoSolicitud(uuidTipoSolicitud);
    }

    @Override
    public List<EncargadoTipoSolicitud> getEncargados(String uuidProcesoAcademico) {
        List<EncargadoTipoSolicitud> encargados = new ArrayList<>();
        List<Funcionario> funcionarios = usuarioGateway.getFuncionarios();
        if (funcionarios != null)
            funcionarios.forEach(funcionario -> encargados.add(encargado(funcionario, RemisionConsejoConstantes.ENCARGADO_FUNCIONARIO)));
        if (uuidProcesoAcademico == null || uuidProcesoAcademico.isBlank())
            return encargados;
        procesoConConsejo(uuidProcesoAcademico.trim());
        for (FuncionarioAcademico academico : funcionarioAcademicoGateway.getTodos()) {
            Usuario usuario = academico.getUsuario();
            if (usuario != null && !Boolean.FALSE.equals(usuario.getEstado()))
                encargados.add(encargado(usuario, RemisionConsejoConstantes.ENCARGADO_FUNCIONARIO_ACADEMICO));
        }
        return encargados;
    }

    private TipoSolicitudAcademica procesoConConsejo(String uuidProceso) {
        TipoSolicitudAcademica proceso = tipoAcademicoGateway.getPorUuid(uuidProceso);
        if (proceso == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, PROCESO_ACADEMICO, uuidProceso));
        if (TipoProcesoAcademico.porNombre(proceso.getNombre()) != TipoProcesoAcademico.CANCELACION_ASIGNATURA)
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.PROCESO_SIN_CONSEJO, proceso.getNombre()));
        return proceso;
    }

    private EncargadoTipoSolicitud encargado(Usuario usuario, String tipo) {
        return EncargadoTipoSolicitud.builder()
                .uuidUsuario(usuario.getUuidUsuario())
                .nombres(usuario.getNombres())
                .apellidos(usuario.getApellidos())
                .tipo(tipo)
                .build();
    }
}
