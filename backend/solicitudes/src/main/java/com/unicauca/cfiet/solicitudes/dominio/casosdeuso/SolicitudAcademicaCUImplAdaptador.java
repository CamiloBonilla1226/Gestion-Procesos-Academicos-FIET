package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.SolicitudAcademicaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.AnexoAcademicoGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.EstudianteGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.EtapaSolicitudAcademicaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.ExcepcionesFormateadorIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.ResolucionAcademicaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.SolicitudAcademicaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.TipoSolicitudAcademicaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.UsuarioGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.dominio.servicios.MaquinaEtapas;
import com.unicauca.cfiet.solicitudes.dominio.servicios.ValidadorActorSolicitud;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.unicauca.cfiet.solicitudes.dominio.helper.constantes.AnexoAcademicoConstantes.COMPROBANTE_PAGO;
import static com.unicauca.cfiet.solicitudes.dominio.helper.constantes.AnexoAcademicoConstantes.RECIBO_PAGO;

public class SolicitudAcademicaCUImplAdaptador implements SolicitudAcademicaCUIntPuerto {
    private static final String SOLICITUD_ACADEMICA = "Solicitud académica";
    private static final String TIPO_SOLICITUD_ACADEMICA = "Tipo de solicitud académica";
    private static final String ESTUDIANTE = "Estudiante";
    private static final String ETAPA = "Etapa de solicitud académica";
    private static final String CODIGO = "codigo";
    private static final int MAXIMO_OBSERVACION = 500;

    private final SolicitudAcademicaGatewayIntPuerto gateway;
    private final EstudianteGatewayIntPuerto estudianteGateway;
    private final TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway;
    private final EtapaSolicitudAcademicaGatewayIntPuerto etapaGateway;
    private final ResolucionAcademicaGatewayIntPuerto resolucionGateway;
    private final AnexoAcademicoGatewayIntPuerto anexoGateway;
    private final ValidadorActorSolicitud validadorActor;
    private final MaquinaEtapas maquinaEtapas;
    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;
    private final Clock reloj;

    public SolicitudAcademicaCUImplAdaptador(SolicitudAcademicaGatewayIntPuerto gateway,
                                             EstudianteGatewayIntPuerto estudianteGateway,
                                             TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway,
                                             EtapaSolicitudAcademicaGatewayIntPuerto etapaGateway,
                                             ResolucionAcademicaGatewayIntPuerto resolucionGateway,
                                             AnexoAcademicoGatewayIntPuerto anexoGateway,
                                             UsuarioGatewayIntPuerto usuarioGateway,
                                             MaquinaEtapas maquinaEtapas,
                                             ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                             Clock reloj) {
        this.gateway = gateway;
        this.estudianteGateway = estudianteGateway;
        this.tipoSolicitudGateway = tipoSolicitudGateway;
        this.etapaGateway = etapaGateway;
        this.resolucionGateway = resolucionGateway;
        this.anexoGateway = anexoGateway;
        this.validadorActor = new ValidadorActorSolicitud(usuarioGateway, formateadorExcepciones);
        this.maquinaEtapas = maquinaEtapas;
        this.formateadorExcepciones = formateadorExcepciones;
        this.reloj = reloj;
    }

    @Override
    public SolicitudAcademica crearSolicitud(String uuidEstudiante, String uuidTipoSolicitudAcademica) {
        Estudiante estudiante = tieneTexto(uuidEstudiante) ? estudianteGateway.getPorUuid(uuidEstudiante) : null;
        if (estudiante == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, ESTUDIANTE, uuidEstudiante));
        TipoSolicitudAcademica tipo = tieneTexto(uuidTipoSolicitudAcademica) ? tipoSolicitudGateway.getPorUuid(uuidTipoSolicitudAcademica) : null;
        if (tipo == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(
                    MensajesError.ENTIDAD_NO_ENCONTRADA, TIPO_SOLICITUD_ACADEMICA, uuidTipoSolicitudAcademica));

        TipoProcesoAcademico proceso = procesoDe(tipo);
        ResultadoTransicion resultado = maquinaEtapas.siguienteEtapa(proceso, null, AccionEtapa.RADICAR, RolEtiquetaEtapa.ESTUDIANTE);
        EtapaSolicitudAcademica etapa = etapaDe(tipo, resultado.getEtapaSiguiente());
        LocalDateTime ahora = LocalDateTime.now(reloj);

        SolicitudAcademica solicitud = SolicitudAcademica.builder()
                .uuidSolicitudAcademica(UUID.randomUUID().toString())
                .radicado(siguienteRadicado(proceso, ahora.getYear()))
                .estudiante(estudiante)
                .tipoSolicitudAcademica(tipo)
                .etapa(etapa)
                .fechaCreacion(ahora)
                .build();
        return gateway.crear(solicitud, historial(solicitud, estudiante.getUuidUsuario(), AccionEtapa.RADICAR, null, ahora));
    }

    @Override
    public SolicitudAcademica cambiarEtapa(String uuidSolicitudAcademica, AccionEtapa accion, ActorSolicitud actor, String observacionRecibida) {
        SolicitudAcademica solicitud = tieneTexto(uuidSolicitudAcademica) ? gateway.getPorUuid(uuidSolicitudAcademica) : null;
        if (solicitud == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(
                    MensajesError.ENTIDAD_NO_ENCONTRADA, SOLICITUD_ACADEMICA, uuidSolicitudAcademica));
        if (accion == null)
            formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.DATOS_TRANSICION_INCOMPLETOS);

        validadorActor.validar(solicitud, actor);
        String observacion = observacionDe(observacionRecibida);

        TipoProcesoAcademico proceso = procesoDe(solicitud.getTipoSolicitudAcademica());
        ResultadoTransicion resultado = maquinaEtapas.transicionar(
                proceso, solicitud.getEtapa().getCodigo(), accion, actor.getRol(), evidencias(solicitud, observacion));
        solicitud.setEtapa(etapaDe(solicitud.getTipoSolicitudAcademica(), resultado.getEtapaSiguiente()));

        LocalDateTime ahora = LocalDateTime.now(reloj);
        return gateway.actualizarEtapa(solicitud, historial(solicitud, actor.getUuidUsuario(), accion, observacion, ahora));
    }

    private EntregaTransicion evidencias(SolicitudAcademica solicitud, String observacion) {
        String uuidSolicitud = solicitud.getUuidSolicitudAcademica();
        Set<String> anexos = anexoGateway.getPorSolicitud(uuidSolicitud).stream()
                .filter(anexo -> anexo.getTipoAnexoAcademico() != null)
                .map(anexo -> anexo.getTipoAnexoAcademico().getNombre())
                .collect(Collectors.toSet());
        return EntregaTransicion.builder()
                .observacion(observacion)
                .resolucion(resolucionGateway.existePorSolicitud(uuidSolicitud))
                .recibo(anexos.contains(RECIBO_PAGO))
                .comprobante(anexos.contains(COMPROBANTE_PAGO))
                .build();
    }

    private String observacionDe(String observacionRecibida) {
        if (!tieneTexto(observacionRecibida)) return null;
        String observacion = observacionRecibida.trim();
        if (observacion.length() > MAXIMO_OBSERVACION)
            formateadorExcepciones.lanzarMalFormato(String.format(MensajesError.OBSERVACION_MUY_LARGA, MAXIMO_OBSERVACION));
        return observacion;
    }

    private TipoProcesoAcademico procesoDe(TipoSolicitudAcademica tipo) {
        TipoProcesoAcademico proceso = TipoProcesoAcademico.porNombre(tipo.getNombre());
        if (proceso == null)
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.TIPO_SOLICITUD_SIN_PROCESO, tipo.getNombre()));
        return proceso;
    }

    private EtapaSolicitudAcademica etapaDe(TipoSolicitudAcademica tipo, String codigo) {
        EtapaSolicitudAcademica etapa = etapaGateway.getPorTipoIncluyendoUniversales(tipo.getUuidTipoSolicitudAcademica()).stream()
                .filter(e -> codigo.equals(e.getCodigo()))
                .findFirst()
                .orElse(null);
        if (etapa == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA_FILTRO, ETAPA, CODIGO, codigo));
        return etapa;
    }

    private String siguienteRadicado(TipoProcesoAcademico proceso, int anio) {
        String prefijo = String.format("%d-%s-", anio, proceso.getCodigo());
        String ultimo = gateway.getUltimoRadicado(prefijo);
        int consecutivo = ultimo == null ? 1 : Integer.parseInt(ultimo.substring(prefijo.length())) + 1;
        return prefijo + String.format("%04d", consecutivo);
    }

    private HistorialSolicitudAcademica historial(SolicitudAcademica solicitud, String uuidUsuario, AccionEtapa accion,
                                                  String observacion, LocalDateTime fecha) {
        return HistorialSolicitudAcademica.builder()
                .uuidHistorial(UUID.randomUUID().toString())
                .solicitudAcademica(solicitud)
                .usuario(Usuario.builder().uuidUsuario(uuidUsuario).build())
                .accion(accion.name())
                .observaciones(observacion)
                .fecha(fecha)
                .build();
    }

    private boolean tieneTexto(String valor) {
        return valor != null && !valor.isBlank();
    }
}
