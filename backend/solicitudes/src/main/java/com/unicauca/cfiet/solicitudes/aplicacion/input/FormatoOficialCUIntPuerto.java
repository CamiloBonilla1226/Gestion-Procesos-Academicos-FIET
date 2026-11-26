package com.unicauca.cfiet.solicitudes.aplicacion.input;

import com.unicauca.cfiet.solicitudes.dominio.modelos.ArchivoAdjunto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.SolicitudAcademica;
import com.unicauca.cfiet.solicitudes.dominio.modelos.ValorProvisionalFormato;

import java.math.BigDecimal;
import java.util.List;

public interface FormatoOficialCUIntPuerto {

    ArchivoAdjunto descargarFormato(String uuidSolicitudAcademica, String token);

    SolicitudAcademica radicar(String uuidSolicitudAcademica, ArchivoAdjunto archivo, String token);

    void descartar(String uuidSolicitudAcademica, String token);

    ArchivoAdjunto componer(String uuidSolicitudAcademica, List<ValorProvisionalFormato> valoresProvisionales, String token);

    void guardarAjuste(String uuidSolicitudAcademica, BigDecimal ajusteXmm, BigDecimal ajusteYmm, String token);

    void subirFormatoDecano(String uuidSolicitudAcademica, ArchivoAdjunto archivo, String token);
}
