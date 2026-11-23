package com.unicauca.cfiet.solicitudes.infraestructura.configuracion;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

public class VariablesEntornoObligatorias implements EnvironmentPostProcessor {

    private static final Map<String, String> VARIABLES = new LinkedHashMap<>();

    static {
        VARIABLES.put("jwt.secreto", "JWT_SECRET");
        VARIABLES.put("correo.darca.destinatario", "CORREO_DARCA");
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment entorno, SpringApplication aplicacion) {
        List<String> faltantes = VARIABLES.entrySet().stream()
                .filter(variable -> !definida(entorno, variable.getKey()))
                .map(Map.Entry::getValue)
                .toList();
        if (!faltantes.isEmpty()) {
            throw new IllegalStateException("Faltan variables de entorno obligatorias: " + String.join(", ", faltantes)
                    + ". Definelas en backend/.env (ver backend/.env.example) o en la consola antes de arrancar el backend.");
        }
    }

    private boolean definida(ConfigurableEnvironment entorno, String propiedad) {
        try {
            String valor = entorno.getProperty(propiedad);
            return valor != null && !valor.isBlank();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
