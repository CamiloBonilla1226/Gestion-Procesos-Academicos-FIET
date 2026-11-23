package com.unicauca.cfiet.solicitudes.infraestructura.output.correo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicauca.cfiet.solicitudes.aplicacion.output.FallaTransitoriaCorreoException;
import com.unicauca.cfiet.solicitudes.dominio.modelos.AdjuntoCorreo;
import com.unicauca.cfiet.solicitudes.dominio.modelos.CorreoPreparado;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.CorreoPendienteEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.CorreoPendienteRepositorio;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ColaCorreos {
    private static final int MAXIMO_INTENTOS_RECHAZADOS = 5;
    private static final int MAXIMO_ERROR = 1000;
    private static final Duration ESPERA_ENVIO_INMEDIATO = Duration.ofSeconds(30);
    private static final Duration RECLAMO_VENCIDO = Duration.ofMinutes(15);
    private static final TypeReference<List<AdjuntoCorreo>> TIPO_ADJUNTOS = new TypeReference<>() {};

    private final CorreoPendienteRepositorio repositorio;
    private final ServicioCorreo servicioCorreo;
    private final ObjectMapper mapeador = new ObjectMapper();

    public Long guardar(String destinatario, String asunto, String texto, List<AdjuntoCorreo> adjuntos) {
        return repositorio.save(CorreoPendienteEntidad.builder()
                .destinatario(destinatario)
                .asunto(asunto)
                .texto(texto == null ? "" : texto)
                .adjuntos(serializar(adjuntos))
                .estado(CorreoPendienteEntidad.PENDIENTE)
                .intentos(0)
                .fechaCreacion(LocalDateTime.now())
                .build()).getIdCorreo();
    }

    public int contarPendientes() {
        return (int) repositorio.contarFallidos(List.of(CorreoPendienteEntidad.PENDIENTE, CorreoPendienteEntidad.ENVIANDO));
    }

    @Async
    public void enviarEnSegundoPlano(Long id) {
        intentar(id);
    }

    @Scheduled(fixedDelayString = "${correo.pendientes.intervalo-ms:60000}",
            initialDelayString = "${correo.pendientes.retraso-inicial-ms:20000}")
    public void reintentarPendientes() {
        try {
            LocalDateTime ahora = LocalDateTime.now();
            List<Long> pendientes = repositorio.findPorEnviar(CorreoPendienteEntidad.PENDIENTE, CorreoPendienteEntidad.ENVIANDO,
                    ahora.minus(ESPERA_ENVIO_INMEDIATO), ahora.minus(RECLAMO_VENCIDO));
            for (Long id : pendientes)
                intentar(id);
            if (!pendientes.isEmpty())
                log.info("Correos pendientes revisados: {}", pendientes.size());
        } catch (RuntimeException error) {
            log.error("No se pudieron reintentar los correos pendientes: {}", error.getMessage());
        }
    }

    void intentar(Long id) {
        LocalDateTime ahora = LocalDateTime.now();
        if (repositorio.reclamar(id, CorreoPendienteEntidad.PENDIENTE, CorreoPendienteEntidad.ENVIANDO, ahora, ahora.minus(RECLAMO_VENCIDO)) == 0)
            return;
        CorreoPendienteEntidad fila = repositorio.findById(id).orElse(null);
        if (fila == null)
            return;
        try {
            servicioCorreo.enviarAhora(CorreoPreparado.builder()
                    .destinatario(fila.getDestinatario())
                    .asunto(fila.getAsunto())
                    .texto(fila.getTexto())
                    .adjuntos(deserializar(fila.getAdjuntos()))
                    .build());
            repositorio.marcarEnviado(id, CorreoPendienteEntidad.ENVIADO, CorreoPendienteEntidad.ENVIANDO, LocalDateTime.now());
        } catch (FallaTransitoriaCorreoException error) {
            log.warn("Sin conexión para enviar el correo \"{}\" a {}; se reintentará: {}", fila.getAsunto(), fila.getDestinatario(), error.getMessage());
            repositorio.registrarFallo(id, CorreoPendienteEntidad.PENDIENTE, fila.getIntentos(), recortar(error.getMessage()),
                    CorreoPendienteEntidad.ENVIANDO);
        } catch (RuntimeException error) {
            int intentos = fila.getIntentos() + 1;
            boolean abandonar = intentos >= MAXIMO_INTENTOS_RECHAZADOS;
            log.error("No se pudo enviar el correo \"{}\" a {} (intento {}): {}", fila.getAsunto(), fila.getDestinatario(), intentos, error.getMessage());
            repositorio.registrarFallo(id, abandonar ? CorreoPendienteEntidad.ABANDONADO : CorreoPendienteEntidad.PENDIENTE, intentos,
                    recortar(error.getMessage()), CorreoPendienteEntidad.ENVIANDO);
        }
    }

    private String serializar(List<AdjuntoCorreo> adjuntos) {
        if (adjuntos == null || adjuntos.isEmpty())
            return null;
        try {
            return mapeador.writeValueAsString(adjuntos);
        } catch (JsonProcessingException error) {
            throw new IllegalStateException("No se pudieron guardar los adjuntos del correo", error);
        }
    }

    private List<AdjuntoCorreo> deserializar(String adjuntos) {
        if (adjuntos == null || adjuntos.isBlank())
            return List.of();
        try {
            return mapeador.readValue(adjuntos, TIPO_ADJUNTOS);
        } catch (JsonProcessingException error) {
            throw new IllegalStateException("No se pudieron leer los adjuntos del correo", error);
        }
    }

    private String recortar(String mensaje) {
        if (mensaje == null)
            return null;
        return mensaje.length() <= MAXIMO_ERROR ? mensaje : mensaje.substring(0, MAXIMO_ERROR);
    }
}
