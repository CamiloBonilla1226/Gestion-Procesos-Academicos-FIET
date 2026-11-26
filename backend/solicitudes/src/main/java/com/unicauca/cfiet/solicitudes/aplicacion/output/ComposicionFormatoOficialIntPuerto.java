package com.unicauca.cfiet.solicitudes.aplicacion.output;

import com.unicauca.cfiet.solicitudes.dominio.modelos.DatosFormatoOficial;

public interface ComposicionFormatoOficialIntPuerto {

    byte[] generar(DatosFormatoOficial datos);

    byte[] componer(byte[] escaneo, DatosFormatoOficial datos, double ajusteXmm, double ajusteYmm);

    int paginasMinimas(DatosFormatoOficial datos);

    int contarPaginas(byte[] pdf);
}
