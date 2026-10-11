package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.AsignaturaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.AsignaturaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.ExcepcionesFormateadorIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.ProgramaAcademicoGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Asignatura;
import com.unicauca.cfiet.solicitudes.dominio.modelos.ProgramaAcademico;
import com.unicauca.cfiet.solicitudes.dominio.servicios.ReglasAsignatura;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AsignaturaCUImplAdaptador implements AsignaturaCUIntPuerto {
    private final AsignaturaGatewayIntPuerto gateway;
    private final ProgramaAcademicoGatewayIntPuerto programaGateway;
    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;
    private final LogCUIntPuerto log;
    private static final String ASIGNATURA = "Asignatura";
    private static final String ASIGNATURAS = "asignaturas";
    private static final String PROGRAMA_ACADEMICO = "Programa académico";
    private static final String NOMBRE_ASIGNATURA = "la asignatura";

    public AsignaturaCUImplAdaptador(AsignaturaGatewayIntPuerto gateway,
                                     ProgramaAcademicoGatewayIntPuerto programaGateway,
                                     ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                     LogCUIntPuerto log) {
        this.gateway = gateway;
        this.programaGateway = programaGateway;
        this.formateadorExcepciones = formateadorExcepciones;
        this.log = log;
    }

    @Override
    public Asignatura crearAsignatura(Asignatura asignatura, String token) {
        return crearAsignaturas(List.of(asignatura), token).get(0);
    }

    @Override
    public List<Asignatura> crearAsignaturas(List<Asignatura> asignaturas, String token) {
        if (asignaturas == null || asignaturas.isEmpty())
            formateadorExcepciones.lanzarSinInformacion(String.format(MensajesError.ARCHIVO_EXCEL_VACIO, ASIGNATURAS));

        Map<String, ProgramaAcademico> programas = new HashMap<>();
        Set<String> nombres = new HashSet<>();
        for (Asignatura asignatura : asignaturas) {
            asignatura.setNombreAsignatura(nombreValido(asignatura.getNombreAsignatura()));
            String uuidPrograma = uuidPrograma(asignatura);
            if (!programas.containsKey(uuidPrograma))
                programas.put(uuidPrograma, programaExistente(uuidPrograma));
            if (!nombres.add(uuidPrograma + "|" + ReglasAsignatura.claveNombre(asignatura.getNombreAsignatura())))
                formateadorExcepciones.lanzarMalFormato(String.format(
                        MensajesError.VALOR_REPETIDO_PETICION, NOMBRE_ASIGNATURA, asignatura.getNombreAsignatura()));
        }

        Map<String, Integer> consecutivos = new LinkedHashMap<>();
        List<ProgramaAcademico> ordenados = programas.values().stream()
                .sorted(Comparator.comparing(ProgramaAcademico::getSigla))
                .toList();
        for (ProgramaAcademico programa : ordenados) {
            ProgramaAcademico bloqueado = bloquear(programa.getUuidProgramaAcademico());
            validarNombresLibres(asignaturas, bloqueado);
            consecutivos.put(bloqueado.getUuidProgramaAcademico(), ultimoConsecutivo(bloqueado));
        }

        for (Asignatura asignatura : asignaturas) {
            ProgramaAcademico programa = programas.get(uuidPrograma(asignatura));
            int consecutivo = consecutivos.merge(programa.getUuidProgramaAcademico(), 1, Integer::sum);
            asignatura.setUuidAsignatura(UUID.randomUUID().toString());
            asignatura.setCodigoAsignatura(ReglasAsignatura.generarCodigo(programa.getSigla(), consecutivo));
            asignatura.setProgramaAcademico(programa);
        }
        consecutivos.forEach(programaGateway::guardarUltimoConsecutivo);

        List<Asignatura> creadas = new ArrayList<>();
        for (Asignatura asignatura : asignaturas) {
            Asignatura guardada = gateway.guardar(asignatura);
            log.crearLog("Crear asignatura",
                    String.format("Asignatura %s - %s del programa %s creada con uuid %s",
                            guardada.getCodigoAsignatura(), guardada.getNombreAsignatura(),
                            asignatura.getProgramaAcademico().getSigla(), guardada.getUuidAsignatura()),
                    token);
            creadas.add(guardada);
        }
        return creadas;
    }

    @Override
    public PaginacionRespuestaDTO<Asignatura> getAsignaturasPaginado(int pagina, int tamanio) {
        validarPaginacion(pagina, tamanio);
        return gateway.getPaginado(pagina, tamanio);
    }

    @Override
    public PaginacionRespuestaDTO<Asignatura> getAsignaturasPorFiltro(String texto, String uuidProgramaAcademico, int pagina, int tamanio) {
        validarPaginacion(pagina, tamanio);
        String programa = uuidProgramaAcademico == null || uuidProgramaAcademico.isBlank() ? null : uuidProgramaAcademico.trim();
        if (programa != null)
            programaExistente(programa);
        return gateway.getPorFiltro(texto, programa, pagina, tamanio);
    }

    @Override
    public List<Asignatura> getAsignaturasPorPrograma(String uuidProgramaAcademico) {
        programaExistente(uuidProgramaAcademico);
        return gateway.getPorPrograma(uuidProgramaAcademico);
    }

    @Override
    public Asignatura getAsignatura(String uuidAsignatura) {
        return obtenerExistente(uuidAsignatura);
    }

    @Override
    public Asignatura actualizarAsignatura(String uuidAsignatura, Asignatura asignatura, String token) {
        Asignatura actual = obtenerExistente(uuidAsignatura);
        String nombre = nombreValido(asignatura.getNombreAsignatura());

        ProgramaAcademico programa = bloquear(actual.getProgramaAcademico().getUuidProgramaAcademico());
        String clave = ReglasAsignatura.claveNombre(nombre);
        for (Asignatura otra : gateway.getPorPrograma(programa.getUuidProgramaAcademico())) {
            if (!otra.getUuidAsignatura().equals(uuidAsignatura) && ReglasAsignatura.claveNombre(otra.getNombreAsignatura()).equals(clave))
                lanzarNombreExiste(nombre, programa, otra);
        }

        actual.setNombreAsignatura(nombre);
        Asignatura guardada = gateway.guardar(actual);
        log.crearLog("Actualizar asignatura",
                String.format("Asignatura %s actualizada con nombre %s", guardada.getCodigoAsignatura(), guardada.getNombreAsignatura()),
                token);
        return guardada;
    }

    private void validarNombresLibres(List<Asignatura> asignaturas, ProgramaAcademico programa) {
        Map<String, Asignatura> existentes = new HashMap<>();
        for (Asignatura existente : gateway.getPorPrograma(programa.getUuidProgramaAcademico()))
            existentes.put(ReglasAsignatura.claveNombre(existente.getNombreAsignatura()), existente);

        for (Asignatura asignatura : asignaturas) {
            if (!programa.getUuidProgramaAcademico().equals(uuidPrograma(asignatura)))
                continue;
            Asignatura existente = existentes.get(ReglasAsignatura.claveNombre(asignatura.getNombreAsignatura()));
            if (existente != null)
                lanzarNombreExiste(asignatura.getNombreAsignatura(), programa, existente);
        }
    }

    private String nombreValido(String nombre) {
        String normalizado = ReglasAsignatura.normalizarNombre(nombre);
        if (normalizado.isEmpty())
            formateadorExcepciones.lanzarMalFormato(MensajesError.NOMBRE_ASIGNATURA_REQUERIDO);
        if (normalizado.length() > ReglasAsignatura.LONGITUD_MAXIMA_NOMBRE)
            formateadorExcepciones.lanzarMalFormato(String.format(
                    MensajesError.NOMBRE_ASIGNATURA_MUY_LARGO, ReglasAsignatura.LONGITUD_MAXIMA_NOMBRE));
        return normalizado;
    }

    private String uuidPrograma(Asignatura asignatura) {
        ProgramaAcademico programa = asignatura.getProgramaAcademico();
        String uuid = programa == null ? null : programa.getUuidProgramaAcademico();
        if (uuid == null || uuid.isBlank())
            formateadorExcepciones.lanzarMalFormato(MensajesError.PROGRAMA_ACADEMICO_REQUERIDO);
        return uuid.trim();
    }

    private ProgramaAcademico programaExistente(String uuidPrograma) {
        ProgramaAcademico programa = programaGateway.getPorUuid(uuidPrograma);
        if (programa == null)
            formateadorExcepciones.lanzarEntidadNoExiste(
                    String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, PROGRAMA_ACADEMICO, uuidPrograma));
        return programa;
    }

    private ProgramaAcademico bloquear(String uuidPrograma) {
        ProgramaAcademico programa = programaGateway.bloquear(uuidPrograma);
        if (programa == null)
            formateadorExcepciones.lanzarEntidadNoExiste(
                    String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, PROGRAMA_ACADEMICO, uuidPrograma));
        return programa;
    }

    private int ultimoConsecutivo(ProgramaAcademico programa) {
        Integer ultimo = programa.getUltimoConsecutivoAsignatura();
        return ultimo == null ? 0 : ultimo;
    }

    private Asignatura obtenerExistente(String uuidAsignatura) {
        Asignatura asignatura = gateway.getPorUuid(uuidAsignatura);
        if (asignatura == null)
            formateadorExcepciones.lanzarEntidadNoExiste(
                    String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, ASIGNATURA, uuidAsignatura));
        return asignatura;
    }

    private void validarPaginacion(int pagina, int tamanio) {
        if (pagina < 0 || tamanio < 1)
            formateadorExcepciones.lanzarMalFormato(MensajesError.PAGINACION_ERROR);
    }

    private void lanzarNombreExiste(String nombre, ProgramaAcademico programa, Asignatura existente) {
        formateadorExcepciones.lanzarEntidadExiste(String.format(
                MensajesError.ASIGNATURA_NOMBRE_EXISTE, nombre, programa.getNombre(), existente.getCodigoAsignatura()));
    }
}
