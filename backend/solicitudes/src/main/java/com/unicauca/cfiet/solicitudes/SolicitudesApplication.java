package com.unicauca.cfiet.solicitudes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del backend de "Gestión de Procesos Académicos FIET".
 *
 * Este módulo es la base heredada del prototipo "Gestión de Solicitudes
 * Consejo de Facultad" desarrollado por Julián David Camacho Erazo
 * (https://github.com/jdacamacho/back-fiet-sc). Sobre esta base se
 * extenderá, como trabajo de grado, la semi-automatización de tres
 * procesos académicos de la Decanatura FIET (Universidad del Cauca):
 * Cancelación de Matrícula, Cancelación de Asignatura y Expedición de
 * Exámenes Supletorios. Ver /docs en la raíz del repositorio para el
 * modelo de datos, el diccionario de datos y las reglas de negocio
 * documentadas para esta extensión.
 */
@SpringBootApplication
public class SolicitudesApplication {

	public static void main(String[] args) {
		SpringApplication.run(SolicitudesApplication.class, args);
	}

}
