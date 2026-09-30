package com.unicauca.cfiet.solicitudes.infraestructura.output.encoder;

import com.unicauca.cfiet.solicitudes.aplicacion.output.PasswordEncoderGatewayIntPuerto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Servicio para encriptar y validar contraseñas de usuarios.
 * Implementa la interfaz PasswordEncoderGatewayIntPuerto.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Service
@RequiredArgsConstructor
public class PasswordEnconderGatewayImplAdaptador implements PasswordEncoderGatewayIntPuerto {
    private final PasswordEncoder passwordEncoder;

    @Override
    public String encriptarContraseña(String contraseña) {
        return passwordEncoder.encode(contraseña);
    }

    @Override
    public boolean contraseñaCoincide(String contraseñaOriginal, String contraseña) {
        return passwordEncoder.matches(contraseña, contraseñaOriginal);
    }
}
