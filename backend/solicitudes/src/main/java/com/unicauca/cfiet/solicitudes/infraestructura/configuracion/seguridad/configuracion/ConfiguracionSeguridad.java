package com.unicauca.cfiet.solicitudes.infraestructura.configuracion.seguridad.configuracion;

import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.ApplicationConstantes;
import com.unicauca.cfiet.solicitudes.infraestructura.configuracion.seguridad.jwt.JwtFiltroAutenticacion;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 *  Configuración de Acceso en endpoints.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class ConfiguracionSeguridad {
    @Value("${url.application}")
    private String baseUrl;

    @Value("${url.frontend}")
    private String frontendUrl;

    private final JwtFiltroAutenticacion jwtFiltro;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   CustomAuthenticationEntryPoint authEntryPoint,
                                                   CustomAccessDeniedHandler accessDeniedHandler) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(authRequest -> authRequest
                        .requestMatchers(baseUrl + "sesiones").permitAll()
                        .requestMatchers(HttpMethod.POST, baseUrl + "solicitudes/public").permitAll()
                        .requestMatchers(HttpMethod.GET, baseUrl + "tipos/solicitudes/perfil").permitAll()
                        .requestMatchers(HttpMethod.GET, baseUrl + "tipos/solicitudes/perfil/paginado").permitAll()
                        .requestMatchers(HttpMethod.GET, baseUrl + "tipos/solicitudes/perfil/filtro").permitAll()
                        .requestMatchers(HttpMethod.GET, baseUrl + "tipos/solicitudes/{uuidTipoSolicitud}").permitAll()
                        .requestMatchers(baseUrl + "tipos/solicitudes/**").hasAnyAuthority(ApplicationConstantes.SECRETARIO_GENERAL, ApplicationConstantes.DECANO)
                        .requestMatchers(HttpMethod.GET, baseUrl + "solicitudes/orden-del-dia/estado").authenticated()
                        .requestMatchers(baseUrl + "solicitudes/orden-del-dia/**").hasAnyAuthority(ApplicationConstantes.SECRETARIO_GENERAL, ApplicationConstantes.DECANO)
                        .requestMatchers(baseUrl + "logs/**").hasAnyAuthority(ApplicationConstantes.SECRETARIO_GENERAL, ApplicationConstantes.DECANO)
                        .requestMatchers(baseUrl + "roles/**").hasAnyAuthority(ApplicationConstantes.SECRETARIO_GENERAL, ApplicationConstantes.DECANO)
                        .requestMatchers(HttpMethod.GET, baseUrl + "usuarios").hasAnyAuthority(ApplicationConstantes.SECRETARIO_GENERAL, ApplicationConstantes.DECANO)
                        .requestMatchers(HttpMethod.GET, baseUrl + "usuarios/paginado").hasAnyAuthority(ApplicationConstantes.SECRETARIO_GENERAL, ApplicationConstantes.DECANO)
                        .requestMatchers(HttpMethod.GET, baseUrl + "usuarios/filtro").hasAnyAuthority(ApplicationConstantes.SECRETARIO_GENERAL, ApplicationConstantes.DECANO)
                        .requestMatchers(HttpMethod.GET, baseUrl + "usuarios/funcionarios").hasAnyAuthority(ApplicationConstantes.SECRETARIO_GENERAL, ApplicationConstantes.DECANO)
                        .requestMatchers(HttpMethod.GET, baseUrl + "usuarios/tipos").hasAnyAuthority(ApplicationConstantes.SECRETARIO_GENERAL, ApplicationConstantes.DECANO)
                        .requestMatchers(HttpMethod.GET, baseUrl + "usuarios/**").authenticated()
                        .requestMatchers(HttpMethod.PATCH, baseUrl + "usuarios/**").authenticated()
                        .requestMatchers(baseUrl + "usuarios/**").hasAnyAuthority(ApplicationConstantes.SECRETARIO_GENERAL, ApplicationConstantes.DECANO)
                        .requestMatchers(baseUrl + "solicitudes/**").authenticated()
                        .requestMatchers(HttpMethod.GET, baseUrl + "respuestas/**").authenticated()
                        .requestMatchers(HttpMethod.POST, baseUrl + "respuestas/**")
                        .hasAnyAuthority(
                                ApplicationConstantes.SECRETARIO_GENERAL,
                                ApplicationConstantes.DECANO,
                                ApplicationConstantes.FUNCIONARIO_ROL
                        )
                        .requestMatchers(HttpMethod.GET, baseUrl + "asignaturas/**").hasAnyAuthority(ApplicationConstantes.SECRETARIO_GENERAL, ApplicationConstantes.DECANO, ApplicationConstantes.FUNCIONARIO_ACADEMICO_ROL)
                        .requestMatchers(HttpMethod.POST, baseUrl + "asignaturas").hasAnyAuthority(ApplicationConstantes.SECRETARIO_GENERAL, ApplicationConstantes.DECANO)
                        .requestMatchers(HttpMethod.PUT, baseUrl + "asignaturas/**").hasAnyAuthority(ApplicationConstantes.SECRETARIO_GENERAL, ApplicationConstantes.DECANO)
                        .requestMatchers(baseUrl + "asignaturas/**").denyAll()
                        .requestMatchers(HttpMethod.POST, baseUrl + "estudiantes/cargar/archivo").hasAnyAuthority(ApplicationConstantes.SECRETARIO_GENERAL, ApplicationConstantes.DECANO)
                        .requestMatchers(HttpMethod.GET, baseUrl + "estudiantes/mis-asignaturas").hasAnyAuthority(ApplicationConstantes.ESTUDIANTE_ROL)
                        .requestMatchers(HttpMethod.GET, baseUrl + "estudiantes/paginado").hasAnyAuthority(ApplicationConstantes.SECRETARIO_GENERAL, ApplicationConstantes.DECANO, ApplicationConstantes.FUNCIONARIO_ACADEMICO_ROL)
                        .requestMatchers(HttpMethod.GET, baseUrl + "estudiantes/filtro").hasAnyAuthority(ApplicationConstantes.SECRETARIO_GENERAL, ApplicationConstantes.DECANO, ApplicationConstantes.FUNCIONARIO_ACADEMICO_ROL)
                        .requestMatchers(HttpMethod.GET, baseUrl + "estudiantes/{uuidEstudiante}").hasAnyAuthority(ApplicationConstantes.SECRETARIO_GENERAL, ApplicationConstantes.DECANO, ApplicationConstantes.FUNCIONARIO_ACADEMICO_ROL)
                        .requestMatchers(HttpMethod.POST, baseUrl + "estudiantes").hasAnyAuthority(ApplicationConstantes.SECRETARIO_GENERAL, ApplicationConstantes.DECANO)
                        .requestMatchers(HttpMethod.PUT, baseUrl + "estudiantes/{uuidEstudiante}").hasAnyAuthority(ApplicationConstantes.SECRETARIO_GENERAL, ApplicationConstantes.DECANO)
                        .requestMatchers(HttpMethod.POST, baseUrl + "estudiantes/{uuidEstudiante}/asignaturas").hasAnyAuthority(ApplicationConstantes.SECRETARIO_GENERAL, ApplicationConstantes.DECANO)
                        .requestMatchers(HttpMethod.PATCH, baseUrl + "estudiantes/{uuidEstudiante}/asignaturas/{uuidMatricula}/estado").hasAnyAuthority(ApplicationConstantes.SECRETARIO_GENERAL, ApplicationConstantes.DECANO)
                        .requestMatchers(baseUrl + "estudiantes/**").denyAll()
                        .requestMatchers(HttpMethod.POST, baseUrl + "funcionarios-academicos/cargar/archivo").hasAnyAuthority(ApplicationConstantes.SECRETARIO_GENERAL, ApplicationConstantes.DECANO)
                        .requestMatchers(HttpMethod.GET, baseUrl + "funcionarios-academicos/paginado").hasAnyAuthority(ApplicationConstantes.SECRETARIO_GENERAL, ApplicationConstantes.DECANO, ApplicationConstantes.FUNCIONARIO_ACADEMICO_ROL)
                        .requestMatchers(HttpMethod.GET, baseUrl + "funcionarios-academicos/filtro").hasAnyAuthority(ApplicationConstantes.SECRETARIO_GENERAL, ApplicationConstantes.DECANO, ApplicationConstantes.FUNCIONARIO_ACADEMICO_ROL)
                        .requestMatchers(HttpMethod.GET, baseUrl + "funcionarios-academicos/{uuidFuncionario}").hasAnyAuthority(ApplicationConstantes.SECRETARIO_GENERAL, ApplicationConstantes.DECANO, ApplicationConstantes.FUNCIONARIO_ACADEMICO_ROL)
                        .requestMatchers(HttpMethod.POST, baseUrl + "funcionarios-academicos").hasAnyAuthority(ApplicationConstantes.SECRETARIO_GENERAL, ApplicationConstantes.DECANO)
                        .requestMatchers(HttpMethod.PUT, baseUrl + "funcionarios-academicos/{uuidFuncionario}").hasAnyAuthority(ApplicationConstantes.SECRETARIO_GENERAL, ApplicationConstantes.DECANO)
                        .requestMatchers(baseUrl + "funcionarios-academicos/**").denyAll()
                        .requestMatchers(HttpMethod.PUT, baseUrl + "catalogos-academicos/tipos-solicitud/{uuidTipo}/funcionario").hasAnyAuthority(ApplicationConstantes.SECRETARIO_GENERAL, ApplicationConstantes.DECANO)
                        .requestMatchers(HttpMethod.GET, baseUrl + "catalogos-academicos/**").authenticated()
                        .requestMatchers(baseUrl + "catalogos-academicos/**").denyAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(authEntryPoint)
                    .accessDeniedHandler(accessDeniedHandler)
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtFiltro, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(List.of(frontendUrl));
        config.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}
