

===== ARCHIVO: backend/solicitudes/src/main/java/com/unicauca/cfiet/solicitudes/infraestructura/configuracion/seguridad/configuracion/ConfiguracionSeguridad.java =====

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
                        .requestMatchers(HttpMethod.GET, baseUrl + "asignaturas/programa/{uuidPrograma}").hasAnyAuthority(ApplicationConstantes.SECRETARIO_GENERAL, ApplicationConstantes.DECANO, ApplicationConstantes.FUNCIONARIO_ACADEMICO_ROL)
                        .requestMatchers(HttpMethod.POST, baseUrl + "asignaturas/cargar/archivo").hasAnyAuthority(ApplicationConstantes.SECRETARIO_GENERAL, ApplicationConstantes.DECANO)
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
                        .requestMatchers(HttpMethod.GET, baseUrl + "catalogos-academicos/programas").authenticated()
                        .requestMatchers(HttpMethod.GET, baseUrl + "catalogos-academicos/**").authenticated()
                        .requestMatchers(baseUrl + "catalogos-academicos/**").denyAll()
                        .requestMatchers(HttpMethod.GET, baseUrl + "solicitudes-academicas/estudiante").hasAnyAuthority(ApplicationConstantes.ESTUDIANTE_ROL)
                        .requestMatchers(HttpMethod.GET, baseUrl + "solicitudes-academicas/funcionario").hasAnyAuthority(ApplicationConstantes.FUNCIONARIO_ACADEMICO_ROL)
                        .requestMatchers(HttpMethod.GET, baseUrl + "solicitudes-academicas/decano").hasAnyAuthority(ApplicationConstantes.DECANO)
                        .requestMatchers(HttpMethod.GET, baseUrl + "solicitudes-academicas/{uuidSolicitud}").hasAnyAuthority(ApplicationConstantes.ESTUDIANTE_ROL, ApplicationConstantes.FUNCIONARIO_ACADEMICO_ROL, ApplicationConstantes.DECANO)
                        .requestMatchers(HttpMethod.GET, baseUrl + "solicitudes-academicas/{uuidSolicitud}/historial").hasAnyAuthority(ApplicationConstantes.ESTUDIANTE_ROL, ApplicationConstantes.FUNCIONARIO_ACADEMICO_ROL, ApplicationConstantes.DECANO)
                        .requestMatchers(HttpMethod.GET, baseUrl + "solicitudes-academicas/{uuidSolicitud}/anexos/{uuidAnexo}").hasAnyAuthority(ApplicationConstantes.ESTUDIANTE_ROL, ApplicationConstantes.FUNCIONARIO_ACADEMICO_ROL, ApplicationConstantes.DECANO)
                        .requestMatchers(HttpMethod.GET, baseUrl + "solicitudes-academicas/{uuidSolicitud}/resolucion").hasAnyAuthority(ApplicationConstantes.ESTUDIANTE_ROL, ApplicationConstantes.FUNCIONARIO_ACADEMICO_ROL, ApplicationConstantes.DECANO)
                        .requestMatchers(HttpMethod.POST, baseUrl + "solicitudes-academicas/{uuidSolicitud}/anexos").hasAnyAuthority(ApplicationConstantes.ESTUDIANTE_ROL, ApplicationConstantes.FUNCIONARIO_ACADEMICO_ROL)
                        .requestMatchers(HttpMethod.POST, baseUrl + "solicitudes-academicas/{uuidSolicitud}/resolucion").hasAnyAuthority(ApplicationConstantes.FUNCIONARIO_ACADEMICO_ROL)
                        .requestMatchers(baseUrl + "solicitudes-academicas/**").denyAll()
                        .requestMatchers(HttpMethod.GET, baseUrl + "cancelaciones-matricula/formulario").hasAnyAuthority(ApplicationConstantes.ESTUDIANTE_ROL)
                        .requestMatchers(HttpMethod.POST, baseUrl + "cancelaciones-matricula").hasAnyAuthority(ApplicationConstantes.ESTUDIANTE_ROL)
                        .requestMatchers(HttpMethod.POST, baseUrl + "cancelaciones-matricula/{uuidSolicitud}/funcionario/**").hasAnyAuthority(ApplicationConstantes.FUNCIONARIO_ACADEMICO_ROL)
                        .requestMatchers(HttpMethod.POST, baseUrl + "cancelaciones-matricula/{uuidSolicitud}/decano/**").hasAnyAuthority(ApplicationConstantes.DECANO)
                        .requestMatchers(HttpMethod.GET, baseUrl + "cancelaciones-matricula/{uuidSolicitud}").hasAnyAuthority(ApplicationConstantes.ESTUDIANTE_ROL, ApplicationConstantes.FUNCIONARIO_ACADEMICO_ROL, ApplicationConstantes.DECANO)
                        .requestMatchers(baseUrl + "cancelaciones-matricula/**").denyAll()
                        .requestMatchers(HttpMethod.GET, baseUrl + "cancelaciones-asignatura/formulario").hasAnyAuthority(ApplicationConstantes.ESTUDIANTE_ROL)
                        .requestMatchers(HttpMethod.POST, baseUrl + "cancelaciones-asignatura").hasAnyAuthority(ApplicationConstantes.ESTUDIANTE_ROL)
                        .requestMatchers(HttpMethod.POST, baseUrl + "cancelaciones-asignatura/{uuidSolicitud}/funcionario/**").hasAnyAuthority(ApplicationConstantes.FUNCIONARIO_ACADEMICO_ROL)
                        .requestMatchers(HttpMethod.POST, baseUrl + "cancelaciones-asignatura/{uuidSolicitud}/decano/**").hasAnyAuthority(ApplicationConstantes.DECANO)
                        .requestMatchers(HttpMethod.GET, baseUrl + "cancelaciones-asignatura/{uuidSolicitud}").hasAnyAuthority(ApplicationConstantes.ESTUDIANTE_ROL, ApplicationConstantes.FUNCIONARIO_ACADEMICO_ROL, ApplicationConstantes.DECANO)
                        .requestMatchers(baseUrl + "cancelaciones-asignatura/**").denyAll()
                        .requestMatchers(HttpMethod.GET, baseUrl + "examenes-supletorios/formulario").hasAnyAuthority(ApplicationConstantes.ESTUDIANTE_ROL)
                        .requestMatchers(HttpMethod.POST, baseUrl + "examenes-supletorios").hasAnyAuthority(ApplicationConstantes.ESTUDIANTE_ROL)
                        .requestMatchers(HttpMethod.POST, baseUrl + "examenes-supletorios/{uuidSolicitud}/funcionario/**").hasAnyAuthority(ApplicationConstantes.FUNCIONARIO_ACADEMICO_ROL)
                        .requestMatchers(HttpMethod.POST, baseUrl + "examenes-supletorios/{uuidSolicitud}/decano/**").hasAnyAuthority(ApplicationConstantes.DECANO)
                        .requestMatchers(HttpMethod.POST, baseUrl + "examenes-supletorios/{uuidSolicitud}/estudiante/**").hasAnyAuthority(ApplicationConstantes.ESTUDIANTE_ROL)
                        .requestMatchers(HttpMethod.GET, baseUrl + "examenes-supletorios/{uuidSolicitud}").hasAnyAuthority(ApplicationConstantes.ESTUDIANTE_ROL, ApplicationConstantes.FUNCIONARIO_ACADEMICO_ROL, ApplicationConstantes.DECANO)
                        .requestMatchers(baseUrl + "examenes-supletorios/**").denyAll()
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



===== ARCHIVO: backend/solicitudes/src/main/java/com/unicauca/cfiet/solicitudes/infraestructura/configuracion/seguridad/jwt/JwtServicio.java =====

package com.unicauca.cfiet.solicitudes.infraestructura.configuracion.seguridad.jwt;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * Servicio para la generación y validación de tokens JWT.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Service
public class JwtServicio {
    private static final String SECRET_KEY="<REDACTADO>";

    private Key getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private String getToken(Map<String,Object> extraClaim, UserDetails user) {
        return Jwts
                .builder()
                .setClaims(extraClaim)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                .signWith(getKey(),SignatureAlgorithm.HS256)
                .compact();
    }

    public String getToken(UserDetails user){
        return getToken(new HashMap<>(),user);
    }

    private Claims getAllClaims(String token){
        return Jwts
                .parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public <T> T getClaim(String token, Function <Claims,T> claimResolver){
        final Claims claims = getAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Date getExpiration(String token){
        return getClaim(token, Claims::getExpiration);
    }

    public  boolean isTokenExpired(String token){
        return getExpiration(token).before(new Date());
    }

    public String getUsernameFromToken(String token) {
        return getClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}



===== ARCHIVO: backend/solicitudes/src/main/java/com/unicauca/cfiet/solicitudes/infraestructura/configuracion/seguridad/jwt/JwtFiltroAutenticacion.java =====

package com.unicauca.cfiet.solicitudes.infraestructura.configuracion.seguridad.jwt;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.estructura.Error;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.estructura.CodigoError;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 *  Filtro de autenticación
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component
@RequiredArgsConstructor
public class JwtFiltroAutenticacion extends OncePerRequestFilter {
    private final JwtServicio jwtServicio;
    private final UserDetailsService userDetailsService;
    private final String AUTHENTICATION_SCHEME = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        final String token = getTokenFromRequest(request);
        final String username;

        if(token == null){
            filterChain.doFilter(request, response);
            return;
        }

        try {
            username = jwtServicio.getUsernameFromToken(token);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                if (jwtServicio.isTokenValid(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (ExpiredJwtException e) {
            sendErrorResponse(response, request,
                    CodigoError.ERROR_TOKEN_EXPIRADO.getMensaje(),
                    HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response,
                                   HttpServletRequest request,
                                   String mensaje,
                                   int status) throws IOException {
        response.setContentType("application/json");
        response.setStatus(status);

        Error error = Error.builder()
                .codigoError(String.valueOf(status))
                .mensaje(mensaje)
                .httpCodigo(status)
                .url(request.getRequestURL().toString())
                .metodo(request.getMethod())
                .build();

        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(error));
    }


    private String getTokenFromRequest(HttpServletRequest request) {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if(StringUtils.hasText(authHeader) && authHeader.startsWith(AUTHENTICATION_SCHEME))
            return authHeader.substring(AUTHENTICATION_SCHEME.length());
        return null;
    }
}



===== ARCHIVO: backend/solicitudes/src/main/java/com/unicauca/cfiet/solicitudes/dominio/servicios/MaquinaEtapas.java =====

package com.unicauca.cfiet.solicitudes.dominio.servicios;

import com.unicauca.cfiet.solicitudes.aplicacion.output.ExcepcionesFormateadorIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.AccionEtapa;
import com.unicauca.cfiet.solicitudes.dominio.modelos.EntregaTransicion;
import com.unicauca.cfiet.solicitudes.dominio.modelos.ResponsableEtapa;
import com.unicauca.cfiet.solicitudes.dominio.modelos.ResultadoTransicion;
import com.unicauca.cfiet.solicitudes.dominio.modelos.RolEtiquetaEtapa;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoProcesoAcademico;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EtapaSolicitudAcademicaConstantes.*;

public class MaquinaEtapas {
    private static final String ETAPA_NUEVA = "(nueva)";
    private static final String OBSERVACION = "una observación";
    private static final String RESOLUCION = "el escaneo de la Resolución";
    private static final String RECIBO = "el recibo de pago";
    private static final String COMPROBANTE = "el comprobante de pago";

    private static final Set<TipoProcesoAcademico> CANCELACIONES =
            EnumSet.of(TipoProcesoAcademico.CANCELACION_MATRICULA, TipoProcesoAcademico.CANCELACION_ASIGNATURA);
    private static final Set<TipoProcesoAcademico> SUPLETORIO = EnumSet.of(TipoProcesoAcademico.EXAMEN_SUPLETORIO);
    private static final Set<TipoProcesoAcademico> TODOS = EnumSet.allOf(TipoProcesoAcademico.class);

    private static final Map<String, ResponsableEtapa> RESPONSABLES = Map.of(
            RADICADA, ResponsableEtapa.FUNCIONARIO,
            EN_REVISION_DECANO, ResponsableEtapa.DECANO,
            APROBADA_POR_DECANO, ResponsableEtapa.FUNCIONARIO,
            RECHAZADA_POR_DECANO, ResponsableEtapa.FUNCIONARIO,
            PENDIENTE_PAGO, ResponsableEtapa.ESTUDIANTE,
            EN_VERIFICACION_PAGO, ResponsableEtapa.FUNCIONARIO,
            APROBADA, ResponsableEtapa.NINGUNO,
            RECHAZADA, ResponsableEtapa.NINGUNO);

    private static final List<Transicion> TRANSICIONES = List.of(
            new Transicion(TODOS, null, AccionEtapa.RADICAR, RADICADA, RolEtiquetaEtapa.ESTUDIANTE, false, false, false, false),
            new Transicion(CANCELACIONES, RADICADA, AccionEtapa.RECHAZAR_FUNCIONARIO, RECHAZADA, RolEtiquetaEtapa.FUNCIONARIO, true, true, false, false),
            new Transicion(SUPLETORIO, RADICADA, AccionEtapa.RECHAZAR_FUNCIONARIO, RECHAZADA, RolEtiquetaEtapa.FUNCIONARIO, true, false, false, false),
            new Transicion(TODOS, RADICADA, AccionEtapa.REMITIR_DECANO, EN_REVISION_DECANO, RolEtiquetaEtapa.FUNCIONARIO, false, false, false, false),
            new Transicion(TODOS, EN_REVISION_DECANO, AccionEtapa.APROBAR_DECANO, APROBADA_POR_DECANO, RolEtiquetaEtapa.DECANO, false, false, false, false),
            new Transicion(TODOS, EN_REVISION_DECANO, AccionEtapa.RECHAZAR_DECANO, RECHAZADA_POR_DECANO, RolEtiquetaEtapa.DECANO, false, false, false, false),
            new Transicion(CANCELACIONES, APROBADA_POR_DECANO, AccionEtapa.ENVIAR_RESPUESTA, APROBADA, RolEtiquetaEtapa.FUNCIONARIO, false, true, false, false),
            new Transicion(CANCELACIONES, RECHAZADA_POR_DECANO, AccionEtapa.ENVIAR_RESPUESTA, RECHAZADA, RolEtiquetaEtapa.FUNCIONARIO, false, true, false, false),
            new Transicion(SUPLETORIO, RECHAZADA_POR_DECANO, AccionEtapa.ENVIAR_RESPUESTA, RECHAZADA, RolEtiquetaEtapa.FUNCIONARIO, false, false, false, false),
            new Transicion(SUPLETORIO, APROBADA_POR_DECANO, AccionEtapa.ENVIAR_RECIBO, PENDIENTE_PAGO, RolEtiquetaEtapa.FUNCIONARIO, false, false, true, false),
            new Transicion(SUPLETORIO, PENDIENTE_PAGO, AccionEtapa.SUBIR_COMPROBANTE, EN_VERIFICACION_PAGO, RolEtiquetaEtapa.ESTUDIANTE, false, false, false, true),
            new Transicion(SUPLETORIO, EN_VERIFICACION_PAGO, AccionEtapa.APROBAR_COMPROBANTE, APROBADA, RolEtiquetaEtapa.FUNCIONARIO, false, false, false, false),
            new Transicion(SUPLETORIO, EN_VERIFICACION_PAGO, AccionEtapa.RECHAZAR_COMPROBANTE, RECHAZADA, RolEtiquetaEtapa.FUNCIONARIO, true, false, false, false));

    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;

    public MaquinaEtapas(ExcepcionesFormateadorIntPuerto formateadorExcepciones) {
        this.formateadorExcepciones = formateadorExcepciones;
    }

    public ResultadoTransicion siguienteEtapa(TipoProcesoAcademico tipo, String etapaActual, AccionEtapa accion, RolEtiquetaEtapa rol) {
        if (tipo == null || accion == null || rol == null)
            formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.DATOS_TRANSICION_INCOMPLETOS);

        String origen = etapaActual == null || etapaActual.isBlank() ? null : etapaActual.trim();
        if (origen != null && esFinal(origen))
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.ETAPA_FINAL_SIN_ACCIONES, origen));

        Transicion transicion = TRANSICIONES.stream()
                .filter(t -> t.procesos().contains(tipo) && Objects.equals(t.origen(), origen) && t.accion() == accion)
                .findFirst()
                .orElse(null);
        if (transicion == null)
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(
                    MensajesError.TRANSICION_NO_PERMITIDA, accion, nombreEtapa(origen), tipo.getNombre()));
        if (transicion.rol() != rol)
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(
                    MensajesError.ROL_TRANSICION_NO_PERMITIDO, rol, accion, nombreEtapa(origen), transicion.rol()));

        return ResultadoTransicion.builder()
                .etapaSiguiente(transicion.destino())
                .observacionObligatoria(transicion.observacion())
                .resolucionObligatoria(transicion.resolucion())
                .reciboObligatorio(transicion.recibo())
                .comprobanteObligatorio(transicion.comprobante())
                .build();
    }

    public ResultadoTransicion transicionar(TipoProcesoAcademico tipo, String etapaActual, AccionEtapa accion,
                                           RolEtiquetaEtapa rol, EntregaTransicion entrega) {
        ResultadoTransicion resultado = siguienteEtapa(tipo, etapaActual, accion, rol);
        EntregaTransicion recibida = entrega == null ? new EntregaTransicion() : entrega;
        List<String> faltantes = new ArrayList<>();
        if (resultado.isObservacionObligatoria() && (recibida.getObservacion() == null || recibida.getObservacion().isBlank()))
            faltantes.add(OBSERVACION);
        if (resultado.isResolucionObligatoria() && !recibida.isResolucion())
            faltantes.add(RESOLUCION);
        if (resultado.isReciboObligatorio() && !recibida.isRecibo())
            faltantes.add(RECIBO);
        if (resultado.isComprobanteObligatorio() && !recibida.isComprobante())
            faltantes.add(COMPROBANTE);
        if (!faltantes.isEmpty())
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(
                    MensajesError.REQUISITO_TRANSICION_FALTANTE, accion, String.join(" y ", faltantes)));
        return resultado;
    }

    public List<AccionEtapa> accionesDisponibles(TipoProcesoAcademico tipo, String etapaActual, RolEtiquetaEtapa rol) {
        if (tipo == null || rol == null)
            formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.DATOS_TRANSICION_INCOMPLETOS);
        String origen = etapaActual == null || etapaActual.isBlank() ? null : etapaActual.trim();
        if (origen != null && esFinal(origen))
            return List.of();
        return TRANSICIONES.stream()
                .filter(t -> t.procesos().contains(tipo) && Objects.equals(t.origen(), origen) && t.rol() == rol)
                .map(Transicion::accion)
                .distinct()
                .toList();
    }

    public String etapaDestino(TipoProcesoAcademico tipo, String etapaActual, AccionEtapa accion) {
        String origen = etapaActual == null || etapaActual.isBlank() ? null : etapaActual.trim();
        return TRANSICIONES.stream()
                .filter(t -> t.procesos().contains(tipo) && Objects.equals(t.origen(), origen) && t.accion() == accion)
                .map(Transicion::destino)
                .findFirst()
                .orElse(null);
    }

    public ResponsableEtapa responsableActual(String codigoEtapa) {
        ResponsableEtapa responsable = codigoEtapa == null ? null : RESPONSABLES.get(codigoEtapa.trim());
        if (responsable == null)
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.ETAPA_NO_VALIDA, codigoEtapa));
        return responsable;
    }

    public boolean esFinal(String codigoEtapa) {
        return responsableActual(codigoEtapa) == ResponsableEtapa.NINGUNO;
    }

    private String nombreEtapa(String etapa) {
        return etapa == null ? ETAPA_NUEVA : etapa;
    }

    private record Transicion(Set<TipoProcesoAcademico> procesos, String origen, AccionEtapa accion, String destino,
                              RolEtiquetaEtapa rol, boolean observacion, boolean resolucion, boolean recibo, boolean comprobante) {
    }
}



===== ARCHIVO: backend/solicitudes/src/main/java/com/unicauca/cfiet/solicitudes/infraestructura/configuracion/BeanConfiguracion.java =====

package com.unicauca.cfiet.solicitudes.infraestructura.configuracion;

import com.unicauca.cfiet.solicitudes.aplicacion.input.AnexoAcademicoCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.ConsultaSolicitudAcademicaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.SolicitudAcademicaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.UsuarioCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.*;
import com.unicauca.cfiet.solicitudes.dominio.casosdeuso.*;
import com.unicauca.cfiet.solicitudes.dominio.servicios.MaquinaEtapas;
import com.unicauca.cfiet.solicitudes.infraestructura.configuracion.lectorArchivos.almacenador.AlmacenadorArchivos;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Gestiona las implementaciones de los casos de uso.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Configuration
public class BeanConfiguracion {

    @Bean
    public RolCUImplAdaptador createRolCU(RolGatewayIntPuerto gateway, ExcepcionesFormateadorIntPuerto formateadorExcepciones, LogCUIntPuerto logCU){
        return new RolCUImplAdaptador(gateway, formateadorExcepciones, logCU);
    }

    @Bean
    public UsuarioCUImplAdaptador createUsuarioCU(UsuarioGatewayIntPuerto gateway,
                                                  ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                  LogCUIntPuerto logCU,
                                                  PasswordEncoderGatewayIntPuerto encoder){
        return new UsuarioCUImplAdaptador(gateway, formateadorExcepciones, logCU, encoder);
    }

    @Bean
    public SesionCUImplAdaptador createSesionCU(SesionGatewayIntPuerto gateway,
                                                LogCUIntPuerto logCU,
                                                ExcepcionesFormateadorIntPuerto formateadorExcepciones){
        return new SesionCUImplAdaptador(gateway, logCU, formateadorExcepciones);
    }
    @Bean
    public LogCUImplAdaptador crearLogCU(LogGatewayIntPuerto gateway,ExcepcionesFormateadorIntPuerto formateadorExcepciones, IJwtServicio jwtServicio){
        return new LogCUImplAdaptador(gateway, formateadorExcepciones, jwtServicio);
    }

    @Bean
    public TipoSolicitudCUImplAdaptador crearTipoSolicitudCU(TipoSolicitudGatewayIntPuerto gateway, UsuarioGatewayIntPuerto gatewayUsuario, ExcepcionesFormateadorIntPuerto formateadorExcepciones, LogCUIntPuerto log, RolGatewayIntPuerto rolGateway){
        return new TipoSolicitudCUImplAdaptador(gateway, gatewayUsuario, formateadorExcepciones, log, rolGateway);
    }

    @Bean
    public OrdenDelDiaCUImplAdaptador crearOrdenDelDiaCU(OrdenDelDiaGatewayIntPuerto gateway,
                                                         SolicitudGatewayIntPuerto gatewaySolicitud,
                                                         ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                         LogCUIntPuerto log,
                                                         OrdenDelDiaExportador exportador,
                                                         RespuestaGatewayIntPuerto gatewayRespuesta){
        return new OrdenDelDiaCUImplAdaptador(gateway, gatewaySolicitud,formateadorExcepciones, log, exportador, gatewayRespuesta);
    }

    @Bean
    public SolicitudCUImplAdaptador crearSolicitudCU(SolicitudGatewayIntPuerto gateway,
                                                     ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                     TipoSolicitudGatewayIntPuerto gatewayTipoSolicitud,
                                                     OrdenDelDiaGatewayIntPuerto gatewayOrdenDelDia,
                                                     UsuarioGatewayIntPuerto gatewayUsuario,
                                                     LogCUIntPuerto log,
                                                     SesionGatewayIntPuerto gatewaySesion,
                                                     IJwtServicio jwtServicio,
                                                     AlmacenadorArchivos almacenadorArchivos){
        return new SolicitudCUImplAdaptador(gateway, formateadorExcepciones, gatewayTipoSolicitud, gatewayOrdenDelDia, gatewayUsuario, log, gatewaySesion, jwtServicio, almacenadorArchivos);
    }

    @Bean
    public RespuestaCUImplAdaptador crearRespuestaCU(RespuestaGatewayIntPuerto gateway,
                                                     SolicitudGatewayIntPuerto solicitudGateway,
                                                     ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                     LogCUIntPuerto log,
                                                     AlmacenadorArchivos almacenadorArchivos){
        return new RespuestaCUImplAdaptador(gateway, solicitudGateway, formateadorExcepciones, log, almacenadorArchivos);
    }

    @Bean
    public AsignaturaCUImplAdaptador crearAsignaturaCU(AsignaturaGatewayIntPuerto gateway,
                                                       ProgramaAcademicoGatewayIntPuerto programaGateway,
                                                       ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                       LogCUIntPuerto log){
        return new AsignaturaCUImplAdaptador(gateway, programaGateway, formateadorExcepciones, log);
    }

    @Bean
    public ProgramaAcademicoCUImplAdaptador crearProgramaAcademicoCU(ProgramaAcademicoGatewayIntPuerto gateway){
        return new ProgramaAcademicoCUImplAdaptador(gateway);
    }

    @Bean
    public EstudianteCUImplAdaptador crearEstudianteCU(UsuarioCUIntPuerto usuarioCU,
                                                       EstudianteGatewayIntPuerto gateway,
                                                       AsignaturaGatewayIntPuerto asignaturaGateway,
                                                       ProgramaAcademicoGatewayIntPuerto programaGateway,
                                                       RolGatewayIntPuerto rolGateway,
                                                       UsuarioGatewayIntPuerto usuarioGateway,
                                                       DatosPersonalesGatewayIntPuerto datosPersonalesGateway,
                                                       SesionGatewayIntPuerto sesionGateway,
                                                       IJwtServicio jwtServicio,
                                                       ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                       LogCUIntPuerto log){
        return new EstudianteCUImplAdaptador(usuarioCU, gateway, asignaturaGateway, programaGateway, rolGateway, usuarioGateway, datosPersonalesGateway, sesionGateway, jwtServicio, formateadorExcepciones, log);
    }

    @Bean
    public FuncionarioAcademicoCUImplAdaptador crearFuncionarioAcademicoCU(UsuarioCUIntPuerto usuarioCU,
                                                                         FuncionarioAcademicoGatewayIntPuerto gateway,
                                                                         RolGatewayIntPuerto rolGateway,
                                                                         UsuarioGatewayIntPuerto usuarioGateway,
                                                                         DatosPersonalesGatewayIntPuerto datosPersonalesGateway,
                                                                         ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                                         LogCUIntPuerto log){
        return new FuncionarioAcademicoCUImplAdaptador(usuarioCU, gateway, rolGateway, usuarioGateway, datosPersonalesGateway, formateadorExcepciones, log);
    }

    @Bean
    public TipoSolicitudAcademicaCUImplAdaptador crearTipoSolicitudAcademicaCU(TipoSolicitudAcademicaGatewayIntPuerto gateway){
        return new TipoSolicitudAcademicaCUImplAdaptador(gateway);
    }

    @Bean
    public EtapaSolicitudAcademicaCUImplAdaptador crearEtapaSolicitudAcademicaCU(EtapaSolicitudAcademicaGatewayIntPuerto gateway,
                                                                               EtapaEtiquetaRolGatewayIntPuerto etiquetaGateway,
                                                                               TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway,
                                                                               ExcepcionesFormateadorIntPuerto formateadorExcepciones){
        return new EtapaSolicitudAcademicaCUImplAdaptador(gateway, etiquetaGateway, tipoSolicitudGateway, formateadorExcepciones);
    }

    @Bean
    public TipoAnexoAcademicoCUImplAdaptador crearTipoAnexoAcademicoCU(TipoAnexoAcademicoGatewayIntPuerto gateway,
                                                                     TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway,
                                                                     ExcepcionesFormateadorIntPuerto formateadorExcepciones){
        return new TipoAnexoAcademicoCUImplAdaptador(gateway, tipoSolicitudGateway, formateadorExcepciones);
    }

    @Bean
    public SituacionAcademicaAsignaturaCUImplAdaptador crearSituacionAcademicaAsignaturaCU(SituacionAcademicaAsignaturaGatewayIntPuerto gateway){
        return new SituacionAcademicaAsignaturaCUImplAdaptador(gateway);
    }

    @Bean
    public AsignacionFuncionarioAcademicoCUImplAdaptador crearAsignacionFuncionarioAcademicoCU(TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway,
                                                                                             FuncionarioAcademicoGatewayIntPuerto funcionarioGateway,
                                                                                             ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                                                             LogCUIntPuerto log){
        return new AsignacionFuncionarioAcademicoCUImplAdaptador(tipoSolicitudGateway, funcionarioGateway, formateadorExcepciones, log);
    }

    @Bean
    public MaquinaEtapas crearMaquinaEtapas(ExcepcionesFormateadorIntPuerto formateadorExcepciones){
        return new MaquinaEtapas(formateadorExcepciones);
    }

    @Bean
    public SolicitudAcademicaCUImplAdaptador crearSolicitudAcademicaCU(SolicitudAcademicaGatewayIntPuerto gateway,
                                                                     EstudianteGatewayIntPuerto estudianteGateway,
                                                                     TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway,
                                                                     EtapaSolicitudAcademicaGatewayIntPuerto etapaGateway,
                                                                     ResolucionAcademicaGatewayIntPuerto resolucionGateway,
                                                                     AnexoAcademicoGatewayIntPuerto anexoGateway,
                                                                     UsuarioGatewayIntPuerto usuarioGateway,
                                                                     AlmacenamientoAnexosIntPuerto almacenamiento,
                                                                     MaquinaEtapas maquinaEtapas,
                                                                     ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                                     LogCUIntPuerto log){
        return new SolicitudAcademicaCUImplAdaptador(gateway, estudianteGateway, tipoSolicitudGateway, etapaGateway,
                resolucionGateway, anexoGateway, usuarioGateway, almacenamiento, maquinaEtapas, formateadorExcepciones, log,
                Clock.system(ZoneId.of("America/Bogota")));
    }

    @Bean
    public AnexoAcademicoCUImplAdaptador crearAnexoAcademicoCU(AnexoAcademicoGatewayIntPuerto anexoGateway,
                                                             SolicitudAcademicaGatewayIntPuerto solicitudGateway,
                                                             TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway,
                                                             TipoAnexoAcademicoGatewayIntPuerto tipoAnexoGateway,
                                                             UsuarioGatewayIntPuerto usuarioGateway,
                                                             AlmacenamientoAnexosIntPuerto almacenamiento,
                                                             ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                             LogCUIntPuerto log){
        return new AnexoAcademicoCUImplAdaptador(anexoGateway, solicitudGateway, tipoSolicitudGateway, tipoAnexoGateway,
                usuarioGateway, almacenamiento, formateadorExcepciones, log, Clock.system(ZoneId.of("America/Bogota")));
    }

    @Bean
    public ResolucionAcademicaCUImplAdaptador crearResolucionAcademicaCU(ResolucionAcademicaGatewayIntPuerto gateway,
                                                                       SolicitudAcademicaGatewayIntPuerto solicitudGateway,
                                                                       UsuarioGatewayIntPuerto usuarioGateway,
                                                                       AlmacenamientoAnexosIntPuerto almacenamiento,
                                                                       ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                                       LogCUIntPuerto log){
        return new ResolucionAcademicaCUImplAdaptador(gateway, solicitudGateway, usuarioGateway, almacenamiento,
                formateadorExcepciones, log, Clock.system(ZoneId.of("America/Bogota")));
    }

    @Bean
    public ConsultaSolicitudAcademicaCUImplAdaptador crearConsultaSolicitudAcademicaCU(SolicitudAcademicaGatewayIntPuerto gateway,
                                                                                     EtapaEtiquetaRolGatewayIntPuerto etiquetaGateway,
                                                                                     AnexoAcademicoGatewayIntPuerto anexoGateway,
                                                                                     HistorialSolicitudAcademicaGatewayIntPuerto historialGateway,
                                                                                     ResolucionAcademicaGatewayIntPuerto resolucionGateway,
                                                                                     SesionGatewayIntPuerto sesionGateway,
                                                                                     IJwtServicio jwtServicio,
                                                                                     AnexoAcademicoCUIntPuerto anexoCU,
                                                                                     MaquinaEtapas maquinaEtapas,
                                                                                     ExcepcionesFormateadorIntPuerto formateadorExcepciones){
        return new ConsultaSolicitudAcademicaCUImplAdaptador(gateway, etiquetaGateway, anexoGateway, historialGateway,
                resolucionGateway, sesionGateway, jwtServicio, anexoCU, maquinaEtapas, formateadorExcepciones);
    }

    @Bean
    public CancelacionMatriculaCUImplAdaptador crearCancelacionMatriculaCU(SolicitudCancelacionMatriculaGatewayIntPuerto gateway,
                                                                         EstudianteGatewayIntPuerto estudianteGateway,
                                                                         UsuarioGatewayIntPuerto usuarioGateway,
                                                                         TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway,
                                                                         TipoAnexoAcademicoGatewayIntPuerto tipoAnexoGateway,
                                                                         SolicitudAcademicaCUIntPuerto solicitudCU,
                                                                         AnexoAcademicoCUIntPuerto anexoCU,
                                                                         ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                                         LogCUIntPuerto log){
        return new CancelacionMatriculaCUImplAdaptador(gateway, estudianteGateway, usuarioGateway, tipoSolicitudGateway,
                tipoAnexoGateway, solicitudCU, anexoCU, formateadorExcepciones, log);
    }

    @Bean
    public TramiteCancelacionMatriculaCUImplAdaptador crearTramiteCancelacionMatriculaCU(SolicitudCancelacionMatriculaGatewayIntPuerto gateway,
                                                                                       SolicitudAcademicaGatewayIntPuerto solicitudGateway,
                                                                                       SituacionAcademicaAsignaturaGatewayIntPuerto situacionGateway,
                                                                                       UsuarioGatewayIntPuerto usuarioGateway,
                                                                                       SesionGatewayIntPuerto sesionGateway,
                                                                                       IJwtServicio jwtServicio,
                                                                                       SolicitudAcademicaCUIntPuerto solicitudCU,
                                                                                       MaquinaEtapas maquinaEtapas,
                                                                                       ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                                                       LogCUIntPuerto log){
        return new TramiteCancelacionMatriculaCUImplAdaptador(gateway, solicitudGateway, situacionGateway, usuarioGateway,
                sesionGateway, jwtServicio, solicitudCU, maquinaEtapas, formateadorExcepciones, log);
    }

    @Bean
    public ConsultaCancelacionMatriculaCUImplAdaptador crearConsultaCancelacionMatriculaCU(SolicitudCancelacionMatriculaGatewayIntPuerto gateway,
                                                                                         EstudianteGatewayIntPuerto estudianteGateway,
                                                                                         TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway,
                                                                                         TipoAnexoAcademicoGatewayIntPuerto tipoAnexoGateway,
                                                                                         SesionGatewayIntPuerto sesionGateway,
                                                                                         IJwtServicio jwtServicio,
                                                                                         ConsultaSolicitudAcademicaCUIntPuerto consultaCU,
                                                                                         MaquinaEtapas maquinaEtapas,
                                                                                         ExcepcionesFormateadorIntPuerto formateadorExcepciones){
        return new ConsultaCancelacionMatriculaCUImplAdaptador(gateway, estudianteGateway, tipoSolicitudGateway, tipoAnexoGateway,
                sesionGateway, jwtServicio, consultaCU, maquinaEtapas, formateadorExcepciones);
    }

    @Bean
    public CancelacionAsignaturaCUImplAdaptador crearCancelacionAsignaturaCU(SolicitudCancelacionAsignaturaGatewayIntPuerto gateway,
                                                                           SolicitudCancelacionMatriculaGatewayIntPuerto asignaturaSolicitudGateway,
                                                                           EstudianteGatewayIntPuerto estudianteGateway,
                                                                           UsuarioGatewayIntPuerto usuarioGateway,
                                                                           TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway,
                                                                           SolicitudAcademicaCUIntPuerto solicitudCU,
                                                                           AnexoAcademicoCUIntPuerto anexoCU,
                                                                           ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                                           LogCUIntPuerto log){
        return new CancelacionAsignaturaCUImplAdaptador(gateway, asignaturaSolicitudGateway, estudianteGateway, usuarioGateway,
                tipoSolicitudGateway, solicitudCU, anexoCU, formateadorExcepciones, log);
    }

    @Bean
    public TramiteCancelacionAsignaturaCUImplAdaptador crearTramiteCancelacionAsignaturaCU(SolicitudCancelacionAsignaturaGatewayIntPuerto gateway,
                                                                                         SolicitudCancelacionMatriculaGatewayIntPuerto asignaturaSolicitudGateway,
                                                                                         SolicitudAcademicaGatewayIntPuerto solicitudGateway,
                                                                                         SituacionAcademicaAsignaturaGatewayIntPuerto situacionGateway,
                                                                                         UsuarioGatewayIntPuerto usuarioGateway,
                                                                                         SesionGatewayIntPuerto sesionGateway,
                                                                                         IJwtServicio jwtServicio,
                                                                                         SolicitudAcademicaCUIntPuerto solicitudCU,
                                                                                         MaquinaEtapas maquinaEtapas,
                                                                                         ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                                                         LogCUIntPuerto log){
        return new TramiteCancelacionAsignaturaCUImplAdaptador(gateway, asignaturaSolicitudGateway, solicitudGateway, situacionGateway,
                usuarioGateway, sesionGateway, jwtServicio, solicitudCU, maquinaEtapas, formateadorExcepciones, log);
    }

    @Bean
    public ConsultaCancelacionAsignaturaCUImplAdaptador crearConsultaCancelacionAsignaturaCU(SolicitudCancelacionAsignaturaGatewayIntPuerto gateway,
                                                                                           EstudianteGatewayIntPuerto estudianteGateway,
                                                                                           SesionGatewayIntPuerto sesionGateway,
                                                                                           IJwtServicio jwtServicio,
                                                                                           ConsultaSolicitudAcademicaCUIntPuerto consultaCU,
                                                                                           MaquinaEtapas maquinaEtapas,
                                                                                           ExcepcionesFormateadorIntPuerto formateadorExcepciones){
        return new ConsultaCancelacionAsignaturaCUImplAdaptador(gateway, estudianteGateway, sesionGateway, jwtServicio,
                consultaCU, maquinaEtapas, formateadorExcepciones);
    }

    @Bean
    public ExamenSupletorioCUImplAdaptador crearExamenSupletorioCU(SolicitudExamenSupletorioGatewayIntPuerto gateway,
                                                                 EstudianteGatewayIntPuerto estudianteGateway,
                                                                 UsuarioGatewayIntPuerto usuarioGateway,
                                                                 TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway,
                                                                 TipoAnexoAcademicoGatewayIntPuerto tipoAnexoGateway,
                                                                 SolicitudAcademicaCUIntPuerto solicitudCU,
                                                                 AnexoAcademicoCUIntPuerto anexoCU,
                                                                 ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                                 LogCUIntPuerto log){
        return new ExamenSupletorioCUImplAdaptador(gateway, estudianteGateway, usuarioGateway, tipoSolicitudGateway, tipoAnexoGateway,
                solicitudCU, anexoCU, formateadorExcepciones, log, Clock.system(ZoneId.of("America/Bogota")));
    }

    @Bean
    public TramiteExamenSupletorioCUImplAdaptador crearTramiteExamenSupletorioCU(SolicitudExamenSupletorioGatewayIntPuerto gateway,
                                                                               SolicitudAcademicaGatewayIntPuerto solicitudGateway,
                                                                               UsuarioGatewayIntPuerto usuarioGateway,
                                                                               SesionGatewayIntPuerto sesionGateway,
                                                                               IJwtServicio jwtServicio,
                                                                               SolicitudAcademicaCUIntPuerto solicitudCU,
                                                                               MaquinaEtapas maquinaEtapas,
                                                                               ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                                               LogCUIntPuerto log){
        return new TramiteExamenSupletorioCUImplAdaptador(gateway, solicitudGateway, usuarioGateway, sesionGateway, jwtServicio,
                solicitudCU, maquinaEtapas, formateadorExcepciones, log);
    }

    @Bean
    public ConsultaExamenSupletorioCUImplAdaptador crearConsultaExamenSupletorioCU(SolicitudExamenSupletorioGatewayIntPuerto gateway,
                                                                                 EstudianteGatewayIntPuerto estudianteGateway,
                                                                                 TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway,
                                                                                 TipoAnexoAcademicoGatewayIntPuerto tipoAnexoGateway,
                                                                                 SesionGatewayIntPuerto sesionGateway,
                                                                                 IJwtServicio jwtServicio,
                                                                                 ConsultaSolicitudAcademicaCUIntPuerto consultaCU,
                                                                                 ExcepcionesFormateadorIntPuerto formateadorExcepciones){
        return new ConsultaExamenSupletorioCUImplAdaptador(gateway, estudianteGateway, tipoSolicitudGateway, tipoAnexoGateway,
                sesionGateway, jwtServicio, consultaCU, formateadorExcepciones);
    }
}



===== ARCHIVO: backend/solicitudes/src/main/java/com/unicauca/cfiet/solicitudes/infraestructura/output/persistencia/entidades/EstudianteEntidad.java =====

package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "ESTUDIANTE",
        uniqueConstraints = @UniqueConstraint(name = "uk_estudiante_codigo", columnNames = "codigoEstudiantil")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstudianteEntidad implements Persistable<String> {
    @Id
    @Column(name = "Usuario_uuid", length = 100)
    private String uuidUsuario;
    @MapsId
    @OneToOne(optional = false)
    @JoinColumn(
            name = "Usuario_uuid",
            columnDefinition = "varchar(100)",
            foreignKey = @ForeignKey(name = "fk_estudiante_usuario")
    )
    private UsuarioEntidad usuario;
    @Column(name = "codigoEstudiantil", length = 45, nullable = false)
    private String codigoEstudiantil;
    @ManyToOne(optional = false)
    @JoinColumn(
            name = "ProgramaAcademico_uuid",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_estudiante_programa")
    )
    private ProgramaAcademicoEntidad programaAcademico;
    @Column(name = "semestre", length = 10, nullable = false)
    private String semestre;
    @Column(name = "facultad", length = 100, nullable = false)
    private String facultad;
    @OneToMany(mappedBy = "estudiante", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Builder.Default
    private List<AsignaturaMatriculadaEntidad> asignaturasMatriculadas = new ArrayList<>();
    @Transient
    private boolean nuevo;

    @Override
    public String getId() {
        return uuidUsuario;
    }

    @Override
    public boolean isNew() {
        return nuevo;
    }
}



===== ARCHIVO: backend/solicitudes/src/main/java/com/unicauca/cfiet/solicitudes/infraestructura/output/persistencia/entidades/SolicitudEntidad.java =====

package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Entity
@Table(name = "solicitudes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudEntidad {
    @Id
    @Column(length = 150)
    private String uuidSolicitud;
    @Column(length = 1000)
    private String consecutivo;
    @Column(length = 1000)
    private String nombre;
    @Column(length = 1500)
    private String descripcion;
    @Column(nullable = false, length = 1000)
    private String estado;
    @ManyToOne
    @JoinColumn(name = "uuidTipoSolicitud", nullable = false)
    private TipoSolicitudEntidad objTipoSolicitud;
    @OneToMany(
            fetch = FetchType.LAZY,
            mappedBy = "objSolicitud",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<AnexoEntidad> anexos;
    @ManyToOne
    @JoinColumn(name = "uuidOrdenDelDia")
    private OrdenDelDiaEntidad objOrdenDelDia;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "uuidInformacionSolicitante", referencedColumnName = "uuidInformacionSolicitante")
    private InformacionSolicitanteEntidad informacionSolicitante;
    @ManyToOne
    @JoinColumn(name = "uuidUsuario")
    private FuncionarioEntidad objFuncionario;
    @OneToOne(mappedBy = "solicitud", fetch = FetchType.LAZY)
    private RespuestaEntidad respuesta;
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
    }
}



===== ARCHIVO: backend/solicitudes/src/main/java/com/unicauca/cfiet/solicitudes/infraestructura/output/persistencia/entidades/SolicitudAcademicaEntidad.java =====

package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "SOLICITUD_ACADEMICA",
        uniqueConstraints = @UniqueConstraint(name = "uk_solacad_radicado", columnNames = "radicado")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudAcademicaEntidad {
    @Id
    @Column(name = "uuidSolicitudAcademica", length = 100)
    private String uuidSolicitudAcademica;
    @Column(name = "radicado", length = 20, nullable = false)
    private String radicado;
    @ManyToOne(optional = false)
    @JoinColumn(
            name = "Estudiante_uuid",
            nullable = false,
            columnDefinition = "varchar(100)",
            foreignKey = @ForeignKey(name = "fk_solacad_estudiante")
    )
    private EstudianteEntidad estudiante;
    @ManyToOne(optional = false)
    @JoinColumn(
            name = "TipoSolicitudAcademica_uuid",
            nullable = false,
            columnDefinition = "varchar(100)",
            foreignKey = @ForeignKey(name = "fk_solacad_tiposolicitud")
    )
    private TipoSolicitudAcademicaEntidad tipoSolicitudAcademica;
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "fechaCreacion", nullable = false, columnDefinition = "datetime")
    private LocalDateTime fechaCreacion;
    @ManyToOne(optional = false)
    @JoinColumn(
            name = "Etapa_uuid",
            nullable = false,
            columnDefinition = "varchar(100)",
            foreignKey = @ForeignKey(name = "fk_solacad_etapa")
    )
    private EtapaSolicitudAcademicaEntidad etapa;
}



===== ARCHIVO: backend/solicitudes/src/main/java/com/unicauca/cfiet/solicitudes/infraestructura/input/controladorCancelacionesMatricula/controlador/CancelacionMatriculaRestController.java =====

package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesMatricula.controlador;

import com.unicauca.cfiet.solicitudes.aplicacion.input.CancelacionMatriculaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.ConsultaCancelacionMatriculaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.TramiteCancelacionMatriculaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.ApplicationConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.SolicitudCancelacionMatricula;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesMatricula.DTOPeticion.AprobacionDecanoDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesMatricula.DTOPeticion.ObservacionDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesMatricula.DTOPeticion.RemisionDecanoDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesMatricula.DTORespuesta.CancelacionMatriculaDetalleDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesMatricula.DTORespuesta.FormularioCancelacionMatriculaDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesMatricula.DTORespuesta.RadicacionCancelacionMatriculaDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesMatricula.mapeador.MapperCancelacionMatriculaInfraestructuraDominio;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@RestController
@RequestMapping("${url.application}cancelaciones-matricula")
@CrossOrigin(origins = "${url.frontend}")
@RequiredArgsConstructor
@Tag(name = "Cancelaciones de matrícula", description = "Radicación, detalle y trámite de las solicitudes de Cancelación de Matrícula.")
public class CancelacionMatriculaRestController {
    private static final String UUID_SOLICITUD = "/{uuidSolicitud:[0-9a-fA-F\\-]{36}}";

    private final CancelacionMatriculaCUIntPuerto radicacionCU;
    private final ConsultaCancelacionMatriculaCUIntPuerto consultaCU;
    private final TramiteCancelacionMatriculaCUIntPuerto tramiteCU;
    private final MapperCancelacionMatriculaInfraestructuraDominio mapper;

    @PreAuthorize(ApplicationConstantes.ESTUDIANTE_ACCESO)
    @Transactional(readOnly = true)
    @GetMapping("/formulario")
    public ResponseEntity<FormularioCancelacionMatriculaDTORespuesta> getFormulario(@RequestHeader("Authorization") String token) {
        return new ResponseEntity<>(mapper.mapearFormularioARespuesta(consultaCU.getFormulario(token.substring(7))), HttpStatus.OK);
    }

    @PreAuthorize(ApplicationConstantes.ESTUDIANTE_ACCESO)
    @Transactional
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> radicar(@RequestParam(value = "motivo", required = false) String motivo,
                                     MultipartHttpServletRequest peticion,
                                     @RequestHeader("Authorization") String token) {
        SolicitudCancelacionMatricula cancelacion;
        try {
            String uuidEstudiante = consultaCU.getEstudianteAutenticado(token.substring(7));
            cancelacion = radicacionCU.radicarCancelacionMatricula(uuidEstudiante, motivo,
                    mapper.mapearAnexos(peticion.getMultiFileMap()), token.substring(7));
        } catch (DataAccessException ex) {
            return errorBaseDeDatos(ex);
        }
        return new ResponseEntity<RadicacionCancelacionMatriculaDTORespuesta>(mapper.mapearRadicacionARespuesta(cancelacion), HttpStatus.OK);
    }

    @PreAuthorize(ApplicationConstantes.ESTUDIANTE_FUNCIONARIO_ACADEMICO_DECANO_ACCESO)
    @Transactional(readOnly = true)
    @GetMapping(UUID_SOLICITUD)
    public ResponseEntity<CancelacionMatriculaDetalleDTORespuesta> getDetalle(@PathVariable String uuidSolicitud,
                                                                             @RequestHeader("Authorization") String token) {
        return new ResponseEntity<>(mapper.mapearDetalleARespuesta(consultaCU.getDetalle(uuidSolicitud, token.substring(7))), HttpStatus.OK);
    }

    @PreAuthorize(ApplicationConstantes.FUNCIONARIO_ACADEMICO_ACCESO)
    @Transactional
    @PostMapping(UUID_SOLICITUD + "/funcionario/rechazar")
    public ResponseEntity<?> rechazarPorFuncionario(@PathVariable String uuidSolicitud,
                                                    @RequestBody ObservacionDTOPeticion peticion,
                                                    @RequestHeader("Authorization") String token) {
        return accion(uuidSolicitud, token, () -> tramiteCU.rechazarPorFuncionario(uuidSolicitud, peticion.getObservacion(), token.substring(7)));
    }

    @PreAuthorize(ApplicationConstantes.FUNCIONARIO_ACADEMICO_ACCESO)
    @Transactional
    @PostMapping(UUID_SOLICITUD + "/funcionario/remitir")
    public ResponseEntity<?> remitirADecano(@PathVariable String uuidSolicitud,
                                            @Valid @RequestBody RemisionDecanoDTOPeticion peticion,
                                            @RequestHeader("Authorization") String token) {
        return accion(uuidSolicitud, token, () -> tramiteCU.remitirADecano(uuidSolicitud,
                mapper.mapearEvaluaciones(peticion.getEvaluaciones()), peticion.getObservacion(), token.substring(7)));
    }

    @PreAuthorize(ApplicationConstantes.FUNCIONARIO_ACADEMICO_ACCESO)
    @Transactional
    @PostMapping(UUID_SOLICITUD + "/funcionario/responder")
    public ResponseEntity<?> enviarRespuesta(@PathVariable String uuidSolicitud,
                                             @RequestBody(required = false) ObservacionDTOPeticion peticion,
                                             @RequestHeader("Authorization") String token) {
        return accion(uuidSolicitud, token, () -> tramiteCU.enviarRespuesta(uuidSolicitud,
                peticion == null ? null : peticion.getObservacion(), token.substring(7)));
    }

    @PreAuthorize(ApplicationConstantes.DECANO_ACCESO)
    @Transactional
    @PostMapping(UUID_SOLICITUD + "/decano/aprobar")
    public ResponseEntity<?> aprobarPorDecano(@PathVariable String uuidSolicitud,
                                              @Valid @RequestBody AprobacionDecanoDTOPeticion peticion,
                                              @RequestHeader("Authorization") String token) {
        return accion(uuidSolicitud, token, () -> tramiteCU.aprobarPorDecano(uuidSolicitud,
                mapper.mapearSituaciones(peticion.getSituaciones()), peticion.getObservacion(), token.substring(7)));
    }

    @PreAuthorize(ApplicationConstantes.DECANO_ACCESO)
    @Transactional
    @PostMapping(UUID_SOLICITUD + "/decano/rechazar")
    public ResponseEntity<?> rechazarPorDecano(@PathVariable String uuidSolicitud,
                                               @RequestBody(required = false) ObservacionDTOPeticion peticion,
                                               @RequestHeader("Authorization") String token) {
        return accion(uuidSolicitud, token, () -> tramiteCU.rechazarPorDecano(uuidSolicitud,
                peticion == null ? null : peticion.getObservacion(), token.substring(7)));
    }

    private ResponseEntity<?> accion(String uuidSolicitud, String token, Supplier<SolicitudCancelacionMatricula> accion) {
        try {
            accion.get();
        } catch (DataAccessException ex) {
            return errorBaseDeDatos(ex);
        }
        return new ResponseEntity<CancelacionMatriculaDetalleDTORespuesta>(
                mapper.mapearDetalleARespuesta(consultaCU.getDetalle(uuidSolicitud, token.substring(7))), HttpStatus.OK);
    }

    private ResponseEntity<Map<String, Object>> errorBaseDeDatos(DataAccessException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Error insertando en la base de datos....");
        response.put("error", ex.getMessage() + " " + ex.getMostSpecificCause().getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}



===== ARCHIVO: backend/solicitudes/src/main/java/com/unicauca/cfiet/solicitudes/dominio/casosdeuso/TramiteCancelacionMatriculaCUImplAdaptador.java =====

package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.SolicitudAcademicaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.TramiteCancelacionMatriculaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.*;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EstadoAsignaturaMatriculadaConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.dominio.servicios.MaquinaEtapas;
import com.unicauca.cfiet.solicitudes.dominio.servicios.TramiteCancelacion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;

import java.util.List;
import java.util.Map;

import static com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EtapaSolicitudAcademicaConstantes.APROBADA;

public class TramiteCancelacionMatriculaCUImplAdaptador implements TramiteCancelacionMatriculaCUIntPuerto {
    private static final String CANCELACION_MATRICULA = "Solicitud de cancelación de matrícula";
    private static final String SITUACION_MATRICULA = "la situación en la matrícula";
    private static final String SITUACION_CANCELAR = "la situación al cancelar";

    private final SolicitudCancelacionMatriculaGatewayIntPuerto gateway;
    private final SolicitudAcademicaCUIntPuerto solicitudCU;
    private final TramiteCancelacion tramite;
    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;
    private final LogCUIntPuerto log;

    public TramiteCancelacionMatriculaCUImplAdaptador(SolicitudCancelacionMatriculaGatewayIntPuerto gateway,
                                                      SolicitudAcademicaGatewayIntPuerto solicitudGateway,
                                                      SituacionAcademicaAsignaturaGatewayIntPuerto situacionGateway,
                                                      UsuarioGatewayIntPuerto usuarioGateway,
                                                      SesionGatewayIntPuerto sesionGateway,
                                                      IJwtServicio jwtServicio,
                                                      SolicitudAcademicaCUIntPuerto solicitudCU,
                                                      MaquinaEtapas maquinaEtapas,
                                                      ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                      LogCUIntPuerto log) {
        this.gateway = gateway;
        this.solicitudCU = solicitudCU;
        this.tramite = new TramiteCancelacion(gateway, solicitudGateway, situacionGateway, usuarioGateway, sesionGateway, jwtServicio,
                maquinaEtapas, formateadorExcepciones);
        this.formateadorExcepciones = formateadorExcepciones;
        this.log = log;
    }

    @Override
    public SolicitudCancelacionMatricula rechazarPorFuncionario(String uuidSolicitudAcademica, String observacion, String token) {
        ActorSolicitud actor = tramite.actorDe(token, RolEtiquetaEtapa.FUNCIONARIO);
        SolicitudCancelacionMatricula cancelacion = cancelacionDe(uuidSolicitudAcademica);
        SolicitudAcademica actualizada = solicitudCU.cambiarEtapa(uuidSolicitudAcademica, AccionEtapa.RECHAZAR_FUNCIONARIO, actor, observacion, token);
        return conSolicitud(cancelacion, actualizada);
    }

    @Override
    public SolicitudCancelacionMatricula remitirADecano(String uuidSolicitudAcademica, List<EvaluacionAsignatura> evaluaciones,
                                                        String observacion, String token) {
        ActorSolicitud actor = tramite.actorDe(token, RolEtiquetaEtapa.FUNCIONARIO);
        SolicitudCancelacionMatricula cancelacion = cancelacionDe(uuidSolicitudAcademica);
        SolicitudAcademica solicitud = tramite.solicitudValidada(TipoProcesoAcademico.CANCELACION_MATRICULA, uuidSolicitudAcademica,
                AccionEtapa.REMITIR_DECANO, actor);

        Map<String, SituacionAcademicaAsignatura> catalogo = tramite.catalogoSituaciones();
        Map<String, EvaluacionAsignatura> porAsignatura = tramite.cobertura(cancelacion.getAsignaturas(), solicitud.getRadicado(), evaluaciones,
                EvaluacionAsignatura::getUuidAsignaturaSolicitud);
        for (AsignaturaSolicitudAcademica asignatura : cancelacion.getAsignaturas()) {
            EvaluacionAsignatura evaluacion = porAsignatura.get(asignatura.getUuidAsignaturaSolicitud());
            String nombre = tramite.nombreDe(asignatura);
            asignatura.setNumeroFaltas(tramite.faltasDe(evaluacion.getNumeroFaltas(), nombre));
            asignatura.setNota(tramite.notaDe(evaluacion.getNota(), nombre));
            asignatura.setSituacionMatricula(tramite.situacionDe(catalogo, evaluacion.getUuidSituacionMatricula(), SITUACION_MATRICULA, nombre));
            asignatura.setSituacionCancelar(tramite.tieneTexto(evaluacion.getUuidSituacionCancelar())
                    ? tramite.situacionDe(catalogo, evaluacion.getUuidSituacionCancelar(), SITUACION_CANCELAR, nombre) : null);
        }

        SolicitudAcademica actualizada = solicitudCU.cambiarEtapa(uuidSolicitudAcademica, AccionEtapa.REMITIR_DECANO, actor, observacion, token);
        cancelacion.setAsignaturas(tramite.guardarAsignaturas(cancelacion.getAsignaturas()));
        log.crearLog("Evaluar asignaturas de cancelación de matrícula",
                String.format("Solicitud académica %s, acción %s: %d asignaturas evaluadas", actualizada.getRadicado(),
                        AccionEtapa.REMITIR_DECANO, cancelacion.getAsignaturas().size()),
                token);
        return conSolicitud(cancelacion, actualizada);
    }

    @Override
    public SolicitudCancelacionMatricula aprobarPorDecano(String uuidSolicitudAcademica, List<SituacionCancelarAsignatura> situacionesAlCancelar,
                                                          String observacion, String token) {
        ActorSolicitud actor = tramite.actorDe(token, RolEtiquetaEtapa.DECANO);
        SolicitudCancelacionMatricula cancelacion = cancelacionDe(uuidSolicitudAcademica);
        SolicitudAcademica solicitud = tramite.solicitudValidada(TipoProcesoAcademico.CANCELACION_MATRICULA, uuidSolicitudAcademica,
                AccionEtapa.APROBAR_DECANO, actor);

        Map<String, SituacionAcademicaAsignatura> catalogo = tramite.catalogoSituaciones();
        Map<String, SituacionCancelarAsignatura> porAsignatura = tramite.cobertura(cancelacion.getAsignaturas(), solicitud.getRadicado(),
                situacionesAlCancelar, SituacionCancelarAsignatura::getUuidAsignaturaSolicitud);
        for (AsignaturaSolicitudAcademica asignatura : cancelacion.getAsignaturas()) {
            SituacionCancelarAsignatura situacion = porAsignatura.get(asignatura.getUuidAsignaturaSolicitud());
            asignatura.setSituacionCancelar(tramite.situacionDe(catalogo, situacion.getUuidSituacionCancelar(), SITUACION_CANCELAR,
                    tramite.nombreDe(asignatura)));
        }

        SolicitudAcademica actualizada = solicitudCU.cambiarEtapa(uuidSolicitudAcademica, AccionEtapa.APROBAR_DECANO, actor, observacion, token);
        cancelacion.setAsignaturas(tramite.guardarAsignaturas(cancelacion.getAsignaturas()));
        log.crearLog("Registrar situación al cancelar",
                String.format("Solicitud académica %s, acción %s: situación al cancelar de %d asignaturas", actualizada.getRadicado(),
                        AccionEtapa.APROBAR_DECANO, cancelacion.getAsignaturas().size()),
                token);
        return conSolicitud(cancelacion, actualizada);
    }

    @Override
    public SolicitudCancelacionMatricula rechazarPorDecano(String uuidSolicitudAcademica, String observacion, String token) {
        ActorSolicitud actor = tramite.actorDe(token, RolEtiquetaEtapa.DECANO);
        SolicitudCancelacionMatricula cancelacion = cancelacionDe(uuidSolicitudAcademica);
        SolicitudAcademica actualizada = solicitudCU.cambiarEtapa(uuidSolicitudAcademica, AccionEtapa.RECHAZAR_DECANO, actor, observacion, token);
        return conSolicitud(cancelacion, actualizada);
    }

    @Override
    public SolicitudCancelacionMatricula enviarRespuesta(String uuidSolicitudAcademica, String observacion, String token) {
        ActorSolicitud actor = tramite.actorDe(token, RolEtiquetaEtapa.FUNCIONARIO);
        SolicitudCancelacionMatricula cancelacion = cancelacionDe(uuidSolicitudAcademica);
        SolicitudAcademica actualizada = solicitudCU.cambiarEtapa(uuidSolicitudAcademica, AccionEtapa.ENVIAR_RESPUESTA, actor, observacion, token);
        if (APROBADA.equals(actualizada.getEtapa().getCodigo())) {
            int canceladas = tramite.cancelarActivas(cancelacion.getAsignaturas());
            log.crearLog("Cancelar asignaturas matriculadas",
                    String.format("Solicitud académica %s aprobada: %d asignaturas matriculadas pasan a %s", actualizada.getRadicado(),
                            canceladas, EstadoAsignaturaMatriculadaConstantes.CANCELADA),
                    token);
        }
        return conSolicitud(cancelacion, actualizada);
    }

    private SolicitudCancelacionMatricula cancelacionDe(String uuidSolicitudAcademica) {
        SolicitudCancelacionMatricula cancelacion = tramite.tieneTexto(uuidSolicitudAcademica) ? gateway.getPorSolicitud(uuidSolicitudAcademica) : null;
        if (cancelacion == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, CANCELACION_MATRICULA, uuidSolicitudAcademica));
        return cancelacion;
    }

    private SolicitudCancelacionMatricula conSolicitud(SolicitudCancelacionMatricula cancelacion, SolicitudAcademica solicitud) {
        cancelacion.setSolicitudAcademica(solicitud);
        return cancelacion;
    }
}



===== ARCHIVO: backend/solicitudes/src/main/java/com/unicauca/cfiet/solicitudes/dominio/casosdeuso/CancelacionMatriculaCUImplAdaptador.java =====

package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.AnexoAcademicoCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.CancelacionMatriculaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.SolicitudAcademicaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.*;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.ApplicationConstantes;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EstadoAsignaturaMatriculadaConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.dominio.servicios.ValidadorArchivoAdjunto;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;

import java.util.*;

import static com.unicauca.cfiet.solicitudes.dominio.helper.constantes.AnexoAcademicoConstantes.FORMATOS_SOPORTE_LIBRE;

public class CancelacionMatriculaCUImplAdaptador implements CancelacionMatriculaCUIntPuerto {
    private static final String USUARIO = "Usuario";
    private static final String TIPO_SOLICITUD_ACADEMICA = "Tipo de solicitud académica";
    private static final String TIPO_ANEXO_ACADEMICO = "Tipo de anexo académico";
    private static final String NOMBRE = "nombre";
    private static final String SOPORTE_LIBRE = "Soporte libre";
    private static final int MAXIMO_MOTIVO = 255;

    private final SolicitudCancelacionMatriculaGatewayIntPuerto gateway;
    private final EstudianteGatewayIntPuerto estudianteGateway;
    private final UsuarioGatewayIntPuerto usuarioGateway;
    private final TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway;
    private final TipoAnexoAcademicoGatewayIntPuerto tipoAnexoGateway;
    private final SolicitudAcademicaCUIntPuerto solicitudCU;
    private final AnexoAcademicoCUIntPuerto anexoCU;
    private final ValidadorArchivoAdjunto validadorArchivo;
    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;
    private final LogCUIntPuerto log;

    public CancelacionMatriculaCUImplAdaptador(SolicitudCancelacionMatriculaGatewayIntPuerto gateway,
                                               EstudianteGatewayIntPuerto estudianteGateway,
                                               UsuarioGatewayIntPuerto usuarioGateway,
                                               TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway,
                                               TipoAnexoAcademicoGatewayIntPuerto tipoAnexoGateway,
                                               SolicitudAcademicaCUIntPuerto solicitudCU,
                                               AnexoAcademicoCUIntPuerto anexoCU,
                                               ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                               LogCUIntPuerto log) {
        this.gateway = gateway;
        this.estudianteGateway = estudianteGateway;
        this.usuarioGateway = usuarioGateway;
        this.tipoSolicitudGateway = tipoSolicitudGateway;
        this.tipoAnexoGateway = tipoAnexoGateway;
        this.solicitudCU = solicitudCU;
        this.anexoCU = anexoCU;
        this.validadorArchivo = new ValidadorArchivoAdjunto(formateadorExcepciones);
        this.formateadorExcepciones = formateadorExcepciones;
        this.log = log;
    }

    @Override
    public SolicitudCancelacionMatricula radicarCancelacionMatricula(String uuidEstudiante, String motivoCancelacion,
                                                                     List<AnexoRadicacion> anexos, String token) {
        validarEstudiante(uuidEstudiante);
        List<AsignaturaMatriculada> activas = asignaturasActivas(uuidEstudiante);
        String motivo = motivoDe(motivoCancelacion);
        TipoSolicitudAcademica tipo = tipoCancelacionMatricula();
        List<AnexoRadicacion> entregados = anexos == null ? List.of() : anexos;
        validarAnexos(entregados, tipo);

        SolicitudAcademica solicitud = solicitudCU.crearSolicitud(uuidEstudiante, tipo.getUuidTipoSolicitudAcademica(), token);
        SolicitudCancelacionMatricula guardada = gateway.guardar(SolicitudCancelacionMatricula.builder()
                .solicitudAcademica(solicitud)
                .motivoCancelacion(motivo)
                .asignaturas(activas.stream()
                        .map(asignatura -> AsignaturaSolicitudAcademica.builder()
                                .uuidAsignaturaSolicitud(UUID.randomUUID().toString())
                                .asignaturaMatriculada(asignatura)
                                .build())
                        .toList())
                .build());

        ActorSolicitud actor = ActorSolicitud.builder().uuidUsuario(uuidEstudiante).rol(RolEtiquetaEtapa.ESTUDIANTE).build();
        for (AnexoRadicacion anexo : entregados)
            anexoCU.adjuntarAnexo(solicitud.getUuidSolicitudAcademica(), anexo.getUuidTipoAnexoAcademico(), anexo.getArchivo(), actor, token);

        log.crearLog("Radicar cancelación de matrícula",
                String.format("Solicitud académica %s, acción RADICAR: cancelación de matrícula con %d asignaturas activas y %d anexos",
                        solicitud.getRadicado(), activas.size(), entregados.size()),
                token);
        guardada.setSolicitudAcademica(solicitud);
        return guardada;
    }

    private void validarEstudiante(String uuidEstudiante) {
        Usuario usuario = tieneTexto(uuidEstudiante) ? usuarioGateway.getUsuario(uuidEstudiante) : null;
        if (usuario == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, USUARIO, uuidEstudiante));
        boolean esEstudiante = usuario.getRoles() != null && usuario.getRoles().stream()
                .anyMatch(rol -> ApplicationConstantes.ESTUDIANTE_ROL.equals(rol.getNombre()));
        if (!esEstudiante || estudianteGateway.getPorUuid(uuidEstudiante) == null)
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.ACTOR_SIN_ROL, uuidEstudiante, ApplicationConstantes.ESTUDIANTE_ROL));
    }

    private List<AsignaturaMatriculada> asignaturasActivas(String uuidEstudiante) {
        List<AsignaturaMatriculada> activas = estudianteGateway.getAsignaturasMatriculadas(uuidEstudiante).stream()
                .filter(asignatura -> EstadoAsignaturaMatriculadaConstantes.ACTIVA.equals(asignatura.getEstado()))
                .toList();
        if (activas.isEmpty())
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.SIN_ASIGNATURAS_ACTIVAS, uuidEstudiante));
        return activas;
    }

    private String motivoDe(String motivoCancelacion) {
        if (!tieneTexto(motivoCancelacion))
            formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.MOTIVO_CANCELACION_REQUERIDO);
        String motivo = motivoCancelacion.trim();
        if (motivo.length() > MAXIMO_MOTIVO)
            formateadorExcepciones.lanzarMalFormato(String.format(MensajesError.MOTIVO_CANCELACION_MUY_LARGO, MAXIMO_MOTIVO));
        return motivo;
    }

    private TipoSolicitudAcademica tipoCancelacionMatricula() {
        TipoSolicitudAcademica tipo = tipoSolicitudGateway.getTodos().stream()
                .filter(t -> TipoProcesoAcademico.porNombre(t.getNombre()) == TipoProcesoAcademico.CANCELACION_MATRICULA)
                .findFirst()
                .orElse(null);
        if (tipo == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA_FILTRO,
                    TIPO_SOLICITUD_ACADEMICA, NOMBRE, TipoProcesoAcademico.CANCELACION_MATRICULA.getNombre()));
        return tipo;
    }

    private void validarAnexos(List<AnexoRadicacion> entregados, TipoSolicitudAcademica tipo) {
        Map<String, TipoAnexoAcademico> catalogo = new LinkedHashMap<>();
        for (TipoAnexoAcademico tipoAnexo : tipoAnexoGateway.getPorTipo(tipo.getUuidTipoSolicitudAcademica()))
            catalogo.put(tipoAnexo.getUuidTipoAnexoAcademico(), tipoAnexo);

        Set<String> presentes = new HashSet<>();
        for (AnexoRadicacion anexo : entregados) {
            String uuidTipo = anexo == null ? null : anexo.getUuidTipoAnexoAcademico();
            if (tieneTexto(uuidTipo) && !catalogo.containsKey(uuidTipo))
                anexoDeOtroTipo(uuidTipo, tipo);
            if (tieneTexto(uuidTipo))
                presentes.add(uuidTipo);
        }
        List<String> faltantes = catalogo.values().stream()
                .filter(tipoAnexo -> Boolean.TRUE.equals(tipoAnexo.getObligatorio()))
                .filter(tipoAnexo -> !presentes.contains(tipoAnexo.getUuidTipoAnexoAcademico()))
                .map(TipoAnexoAcademico::getNombre)
                .toList();
        if (!faltantes.isEmpty())
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.ANEXOS_OBLIGATORIOS_FALTANTES, String.join("; ", faltantes)));

        for (AnexoRadicacion anexo : entregados) {
            TipoAnexoAcademico tipoAnexo = anexo == null ? null : catalogo.get(anexo.getUuidTipoAnexoAcademico());
            validadorArchivo.validar(anexo == null ? null : anexo.getArchivo(),
                    tipoAnexo == null ? SOPORTE_LIBRE : tipoAnexo.getNombre(),
                    tipoAnexo == null ? FORMATOS_SOPORTE_LIBRE : tipoAnexo.getFormatosPermitidos());
        }
    }

    private void anexoDeOtroTipo(String uuidTipoAnexo, TipoSolicitudAcademica tipo) {
        TipoAnexoAcademico otro = tipoAnexoGateway.getPorUuid(uuidTipoAnexo);
        if (otro == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, TIPO_ANEXO_ACADEMICO, uuidTipoAnexo));
        formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.ANEXO_DE_OTRO_TIPO, otro.getNombre(), tipo.getNombre()));
    }

    private boolean tieneTexto(String valor) {
        return valor != null && !valor.isBlank();
    }
}



===== ARCHIVO: backend/solicitudes/src/main/java/com/unicauca/cfiet/solicitudes/infraestructura/input/controladorExamenesSupletorios/controlador/ExamenSupletorioRestController.java =====

package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorExamenesSupletorios.controlador;

import com.unicauca.cfiet.solicitudes.aplicacion.input.ConsultaExamenSupletorioCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.ExamenSupletorioCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.TramiteExamenSupletorioCUIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.ApplicationConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.SolicitudExamenSupletorio;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesMatricula.DTOPeticion.ObservacionDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorExamenesSupletorios.DTOPeticion.AprobacionComprobanteDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorExamenesSupletorios.DTOPeticion.RemisionSupletorioDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorExamenesSupletorios.DTORespuesta.ExamenSupletorioDetalleDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorExamenesSupletorios.DTORespuesta.FormularioExamenSupletorioDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorExamenesSupletorios.DTORespuesta.RadicacionExamenSupletorioDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorExamenesSupletorios.mapeador.MapperExamenSupletorioInfraestructuraDominio;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@RestController
@RequestMapping("${url.application}examenes-supletorios")
@CrossOrigin(origins = "${url.frontend}")
@RequiredArgsConstructor
@Tag(name = "Exámenes supletorios", description = "Radicación, detalle y trámite de las solicitudes de Examen Supletorio.")
public class ExamenSupletorioRestController {
    private static final String UUID_SOLICITUD = "/{uuidSolicitud:[0-9a-fA-F\\-]{36}}";
    private static final String FECHA_EXAMEN = "la fecha del examen no presentado";
    private static final String FECHA_CRUZADA = "la fecha del examen cruzado";
    private static final String FECHA_ACORDADA = "la fecha acordada del supletorio";

    private final ExamenSupletorioCUIntPuerto radicacionCU;
    private final ConsultaExamenSupletorioCUIntPuerto consultaCU;
    private final TramiteExamenSupletorioCUIntPuerto tramiteCU;
    private final MapperExamenSupletorioInfraestructuraDominio mapper;

    @PreAuthorize(ApplicationConstantes.ESTUDIANTE_ACCESO)
    @Transactional(readOnly = true)
    @GetMapping("/formulario")
    public ResponseEntity<FormularioExamenSupletorioDTORespuesta> getFormulario(@RequestHeader("Authorization") String token) {
        return new ResponseEntity<>(mapper.mapearFormularioARespuesta(consultaCU.getFormulario(token.substring(7))), HttpStatus.OK);
    }

    @PreAuthorize(ApplicationConstantes.ESTUDIANTE_ACCESO)
    @Transactional
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> radicar(@RequestParam(value = "asignaturaMatriculada", required = false) String asignaturaMatriculada,
                                     @RequestParam(value = "fechaExamenNoPresentado", required = false) String fechaExamenNoPresentado,
                                     @RequestParam(value = "tipoCausa", required = false) String tipoCausa,
                                     @RequestParam(value = "asignaturaCruzada", required = false) String asignaturaCruzada,
                                     @RequestParam(value = "fechaExamenCruzada", required = false) String fechaExamenCruzada,
                                     @RequestParam(value = "horaExamenCruzada", required = false) String horaExamenCruzada,
                                     MultipartHttpServletRequest peticion,
                                     @RequestHeader("Authorization") String token) {
        SolicitudExamenSupletorio supletorio;
        try {
            String uuidEstudiante = consultaCU.getEstudianteAutenticado(token.substring(7));
            supletorio = radicacionCU.radicarExamenSupletorio(uuidEstudiante, asignaturaMatriculada,
                    mapper.mapearFecha(fechaExamenNoPresentado, FECHA_EXAMEN), tipoCausa, asignaturaCruzada,
                    mapper.mapearFecha(fechaExamenCruzada, FECHA_CRUZADA), horaExamenCruzada,
                    mapper.mapearAnexos(peticion.getMultiFileMap()), token.substring(7));
        } catch (DataAccessException ex) {
            return errorBaseDeDatos(ex);
        }
        return new ResponseEntity<RadicacionExamenSupletorioDTORespuesta>(mapper.mapearRadicacionARespuesta(supletorio), HttpStatus.OK);
    }

    @PreAuthorize(ApplicationConstantes.ESTUDIANTE_FUNCIONARIO_ACADEMICO_DECANO_ACCESO)
    @Transactional(readOnly = true)
    @GetMapping(UUID_SOLICITUD)
    public ResponseEntity<ExamenSupletorioDetalleDTORespuesta> getDetalle(@PathVariable String uuidSolicitud,
                                                                         @RequestHeader("Authorization") String token) {
        return new ResponseEntity<>(mapper.mapearDetalleARespuesta(consultaCU.getDetalle(uuidSolicitud, token.substring(7))), HttpStatus.OK);
    }

    @PreAuthorize(ApplicationConstantes.FUNCIONARIO_ACADEMICO_ACCESO)
    @Transactional
    @PostMapping(UUID_SOLICITUD + "/funcionario/rechazar")
    public ResponseEntity<?> rechazarPorFuncionario(@PathVariable String uuidSolicitud,
                                                    @RequestBody ObservacionDTOPeticion peticion,
                                                    @RequestHeader("Authorization") String token) {
        return accion(uuidSolicitud, token, () -> tramiteCU.rechazarPorFuncionario(uuidSolicitud, peticion.getObservacion(), token.substring(7)));
    }

    @PreAuthorize(ApplicationConstantes.FUNCIONARIO_ACADEMICO_ACCESO)
    @Transactional
    @PostMapping(UUID_SOLICITUD + "/funcionario/remitir")
    public ResponseEntity<?> remitirADecano(@PathVariable String uuidSolicitud,
                                            @RequestBody RemisionSupletorioDTOPeticion peticion,
                                            @RequestHeader("Authorization") String token) {
        return accion(uuidSolicitud, token, () -> tramiteCU.remitirADecano(uuidSolicitud, peticion.getObservacion(), token.substring(7)));
    }

    @PreAuthorize(ApplicationConstantes.FUNCIONARIO_ACADEMICO_ACCESO)
    @Transactional
    @PostMapping(UUID_SOLICITUD + "/funcionario/responder")
    public ResponseEntity<?> enviarRespuesta(@PathVariable String uuidSolicitud,
                                             @RequestBody(required = false) ObservacionDTOPeticion peticion,
                                             @RequestHeader("Authorization") String token) {
        return accion(uuidSolicitud, token, () -> tramiteCU.enviarRespuesta(uuidSolicitud,
                peticion == null ? null : peticion.getObservacion(), token.substring(7)));
    }

    @PreAuthorize(ApplicationConstantes.FUNCIONARIO_ACADEMICO_ACCESO)
    @Transactional
    @PostMapping(UUID_SOLICITUD + "/funcionario/recibo")
    public ResponseEntity<?> enviarRecibo(@PathVariable String uuidSolicitud,
                                          @RequestHeader("Authorization") String token) {
        return accion(uuidSolicitud, token, () -> tramiteCU.enviarRecibo(uuidSolicitud, token.substring(7)));
    }

    @PreAuthorize(ApplicationConstantes.FUNCIONARIO_ACADEMICO_ACCESO)
    @Transactional
    @PostMapping(UUID_SOLICITUD + "/funcionario/comprobante/aprobar")
    public ResponseEntity<?> aprobarComprobante(@PathVariable String uuidSolicitud,
                                                @RequestBody(required = false) AprobacionComprobanteDTOPeticion peticion,
                                                @RequestHeader("Authorization") String token) {
        return accion(uuidSolicitud, token, () -> tramiteCU.aprobarComprobante(uuidSolicitud,
                mapper.mapearFecha(peticion == null ? null : peticion.getFechaAcordadaExamen(), FECHA_ACORDADA), token.substring(7)));
    }

    @PreAuthorize(ApplicationConstantes.FUNCIONARIO_ACADEMICO_ACCESO)
    @Transactional
    @PostMapping(UUID_SOLICITUD + "/funcionario/comprobante/rechazar")
    public ResponseEntity<?> rechazarComprobante(@PathVariable String uuidSolicitud,
                                                 @RequestBody ObservacionDTOPeticion peticion,
                                                 @RequestHeader("Authorization") String token) {
        return accion(uuidSolicitud, token, () -> tramiteCU.rechazarComprobante(uuidSolicitud, peticion.getObservacion(), token.substring(7)));
    }

    @PreAuthorize(ApplicationConstantes.DECANO_ACCESO)
    @Transactional
    @PostMapping(UUID_SOLICITUD + "/decano/aprobar")
    public ResponseEntity<?> aprobarPorDecano(@PathVariable String uuidSolicitud,
                                              @RequestBody(required = false) ObservacionDTOPeticion peticion,
                                              @RequestHeader("Authorization") String token) {
        return accion(uuidSolicitud, token, () -> tramiteCU.aprobarPorDecano(uuidSolicitud,
                peticion == null ? null : peticion.getObservacion(), token.substring(7)));
    }

    @PreAuthorize(ApplicationConstantes.DECANO_ACCESO)
    @Transactional
    @PostMapping(UUID_SOLICITUD + "/decano/rechazar")
    public ResponseEntity<?> rechazarPorDecano(@PathVariable String uuidSolicitud,
                                               @RequestBody(required = false) ObservacionDTOPeticion peticion,
                                               @RequestHeader("Authorization") String token) {
        return accion(uuidSolicitud, token, () -> tramiteCU.rechazarPorDecano(uuidSolicitud,
                peticion == null ? null : peticion.getObservacion(), token.substring(7)));
    }

    @PreAuthorize(ApplicationConstantes.ESTUDIANTE_ACCESO)
    @Transactional
    @PostMapping(UUID_SOLICITUD + "/estudiante/comprobante")
    public ResponseEntity<?> subirComprobante(@PathVariable String uuidSolicitud,
                                              @RequestHeader("Authorization") String token) {
        return accion(uuidSolicitud, token, () -> tramiteCU.subirComprobante(uuidSolicitud, token.substring(7)));
    }

    private ResponseEntity<?> accion(String uuidSolicitud, String token, Supplier<SolicitudExamenSupletorio> accion) {
        try {
            accion.get();
        } catch (DataAccessException ex) {
            return errorBaseDeDatos(ex);
        }
        return new ResponseEntity<ExamenSupletorioDetalleDTORespuesta>(
                mapper.mapearDetalleARespuesta(consultaCU.getDetalle(uuidSolicitud, token.substring(7))), HttpStatus.OK);
    }

    private ResponseEntity<Map<String, Object>> errorBaseDeDatos(DataAccessException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Error insertando en la base de datos....");
        response.put("error", ex.getMessage() + " " + ex.getMostSpecificCause().getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}



===== ARCHIVO: backend/solicitudes/src/main/java/com/unicauca/cfiet/solicitudes/dominio/casosdeuso/TramiteExamenSupletorioCUImplAdaptador.java =====

package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.SolicitudAcademicaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.TramiteExamenSupletorioCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.*;
import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.dominio.servicios.MaquinaEtapas;
import com.unicauca.cfiet.solicitudes.dominio.servicios.TramiteSolicitud;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class TramiteExamenSupletorioCUImplAdaptador implements TramiteExamenSupletorioCUIntPuerto {
    private static final String EXAMEN_SUPLETORIO = "Solicitud de examen supletorio";
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final SolicitudExamenSupletorioGatewayIntPuerto gateway;
    private final SolicitudAcademicaCUIntPuerto solicitudCU;
    private final TramiteSolicitud tramite;
    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;
    private final LogCUIntPuerto log;

    public TramiteExamenSupletorioCUImplAdaptador(SolicitudExamenSupletorioGatewayIntPuerto gateway,
                                                  SolicitudAcademicaGatewayIntPuerto solicitudGateway,
                                                  UsuarioGatewayIntPuerto usuarioGateway,
                                                  SesionGatewayIntPuerto sesionGateway,
                                                  IJwtServicio jwtServicio,
                                                  SolicitudAcademicaCUIntPuerto solicitudCU,
                                                  MaquinaEtapas maquinaEtapas,
                                                  ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                  LogCUIntPuerto log) {
        this.gateway = gateway;
        this.solicitudCU = solicitudCU;
        this.tramite = new TramiteSolicitud(solicitudGateway, usuarioGateway, sesionGateway, jwtServicio, maquinaEtapas, formateadorExcepciones);
        this.formateadorExcepciones = formateadorExcepciones;
        this.log = log;
    }

    @Override
    public SolicitudExamenSupletorio rechazarPorFuncionario(String uuidSolicitudAcademica, String observacion, String token) {
        return accion(uuidSolicitudAcademica, AccionEtapa.RECHAZAR_FUNCIONARIO, RolEtiquetaEtapa.FUNCIONARIO, observacion, token);
    }

    @Override
    public SolicitudExamenSupletorio remitirADecano(String uuidSolicitudAcademica, String observacion, String token) {
        return accion(uuidSolicitudAcademica, AccionEtapa.REMITIR_DECANO, RolEtiquetaEtapa.FUNCIONARIO, observacion, token);
    }

    @Override
    public SolicitudExamenSupletorio aprobarPorDecano(String uuidSolicitudAcademica, String observacion, String token) {
        return accion(uuidSolicitudAcademica, AccionEtapa.APROBAR_DECANO, RolEtiquetaEtapa.DECANO, observacion, token);
    }

    @Override
    public SolicitudExamenSupletorio rechazarPorDecano(String uuidSolicitudAcademica, String observacion, String token) {
        return accion(uuidSolicitudAcademica, AccionEtapa.RECHAZAR_DECANO, RolEtiquetaEtapa.DECANO, observacion, token);
    }

    @Override
    public SolicitudExamenSupletorio enviarRespuesta(String uuidSolicitudAcademica, String observacion, String token) {
        return accion(uuidSolicitudAcademica, AccionEtapa.ENVIAR_RESPUESTA, RolEtiquetaEtapa.FUNCIONARIO, observacion, token);
    }

    @Override
    public SolicitudExamenSupletorio enviarRecibo(String uuidSolicitudAcademica, String token) {
        return accion(uuidSolicitudAcademica, AccionEtapa.ENVIAR_RECIBO, RolEtiquetaEtapa.FUNCIONARIO, null, token);
    }

    @Override
    public SolicitudExamenSupletorio subirComprobante(String uuidSolicitudAcademica, String token) {
        return accion(uuidSolicitudAcademica, AccionEtapa.SUBIR_COMPROBANTE, RolEtiquetaEtapa.ESTUDIANTE, null, token);
    }

    @Override
    public SolicitudExamenSupletorio aprobarComprobante(String uuidSolicitudAcademica, LocalDate fechaAcordadaExamen, String token) {
        ActorSolicitud actor = tramite.actorDe(token, RolEtiquetaEtapa.FUNCIONARIO);
        SolicitudExamenSupletorio supletorio = supletorioDe(uuidSolicitudAcademica);
        tramite.solicitudValidada(TipoProcesoAcademico.EXAMEN_SUPLETORIO, uuidSolicitudAcademica, AccionEtapa.APROBAR_COMPROBANTE, actor);
        LocalDate fechaExamen = supletorio.getFechaExamenNoPresentado();
        if (fechaAcordadaExamen != null && fechaExamen != null && fechaAcordadaExamen.isBefore(fechaExamen))
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.FECHA_ACORDADA_ANTERIOR,
                    fechaAcordadaExamen.format(FORMATO_FECHA), fechaExamen.format(FORMATO_FECHA)));

        SolicitudAcademica actualizada = solicitudCU.cambiarEtapa(uuidSolicitudAcademica, AccionEtapa.APROBAR_COMPROBANTE, actor, null, token);
        if (fechaAcordadaExamen != null) {
            gateway.actualizarFechaAcordada(uuidSolicitudAcademica, fechaAcordadaExamen.atStartOfDay());
            supletorio.setFechaAcordadaExamen(fechaAcordadaExamen.atStartOfDay());
            log.crearLog("Acordar fecha de examen supletorio",
                    String.format("Solicitud académica %s, acción %s: fecha acordada %s", actualizada.getRadicado(),
                            AccionEtapa.APROBAR_COMPROBANTE, fechaAcordadaExamen.format(FORMATO_FECHA)),
                    token);
        }
        return conSolicitud(supletorio, actualizada);
    }

    @Override
    public SolicitudExamenSupletorio rechazarComprobante(String uuidSolicitudAcademica, String observacion, String token) {
        return accion(uuidSolicitudAcademica, AccionEtapa.RECHAZAR_COMPROBANTE, RolEtiquetaEtapa.FUNCIONARIO, observacion, token);
    }

    private SolicitudExamenSupletorio accion(String uuidSolicitudAcademica, AccionEtapa accion, RolEtiquetaEtapa rol, String observacion,
                                             String token) {
        ActorSolicitud actor = tramite.actorDe(token, rol);
        SolicitudExamenSupletorio supletorio = supletorioDe(uuidSolicitudAcademica);
        return conSolicitud(supletorio, solicitudCU.cambiarEtapa(uuidSolicitudAcademica, accion, actor, observacion, token));
    }

    private SolicitudExamenSupletorio supletorioDe(String uuidSolicitudAcademica) {
        SolicitudExamenSupletorio supletorio = tramite.tieneTexto(uuidSolicitudAcademica) ? gateway.getPorSolicitud(uuidSolicitudAcademica) : null;
        if (supletorio == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, EXAMEN_SUPLETORIO, uuidSolicitudAcademica));
        return supletorio;
    }

    private SolicitudExamenSupletorio conSolicitud(SolicitudExamenSupletorio supletorio, SolicitudAcademica solicitud) {
        supletorio.setSolicitudAcademica(solicitud);
        return supletorio;
    }
}



===== ARCHIVO: backend/solicitudes/src/main/java/com/unicauca/cfiet/solicitudes/infraestructura/input/controladorUsuarios/controlador/UsuarioRestController.java =====

package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorUsuarios.controlador;

import com.unicauca.cfiet.solicitudes.aplicacion.input.UsuarioCUIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.ApplicationConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Funcionario;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoUsuario;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Usuario;
import com.unicauca.cfiet.solicitudes.dominio.modelos.UsuarioLiviano;
import com.unicauca.cfiet.solicitudes.infraestructura.configuracion.lectorArchivos.ProcesadorArchivos;
import com.unicauca.cfiet.solicitudes.infraestructura.configuracion.lectorArchivos.validadoresArchivos.ValidadorPeticionesExcel;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorUsuarios.DTOPeticion.CambioContraseñaDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorUsuarios.DTOPeticion.UsuarioActualizarDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorUsuarios.DTOPeticion.UsuarioDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorUsuarios.DTORespuesta.TipoUsuarioDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorUsuarios.DTORespuesta.UsuarioDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorUsuarios.DTORespuesta.UsuarioLivianoDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorUsuarios.mapeador.MapperUsuarioInfraestructuraDominio;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@RestController
@RequestMapping("${url.application}usuarios")
@CrossOrigin(origins = "${url.frontend}")
@Validated
@Tag(name = "Usuarios", description = "Operaciones relacionadas con la gestión de Usuarios.")
public class UsuarioRestController{
    private final UsuarioCUIntPuerto casoDeUso;
    private final MapperUsuarioInfraestructuraDominio mapper;
    private final ProcesadorArchivos<UsuarioDTOPeticion> procesadorArchivos;
    private final ValidadorPeticionesExcel<UsuarioDTOPeticion> validadorPeticion;

    public UsuarioRestController(UsuarioCUIntPuerto casoDeUso,
                                 MapperUsuarioInfraestructuraDominio mapper,
                                 @Qualifier("archivos-usuarios") ProcesadorArchivos<UsuarioDTOPeticion> procesadorArchivos,
                                 @Qualifier("validador-usuarios") ValidadorPeticionesExcel<UsuarioDTOPeticion> validadorPeticion){
        this.casoDeUso = casoDeUso;
        this.mapper = mapper;
        this.procesadorArchivos = procesadorArchivos;
        this.validadorPeticion = validadorPeticion;
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_ACCESO)
    @GetMapping("/tipos")
    public ResponseEntity<?> getTiposUsuarios(){
        List<TipoUsuario> tiposUsuario = casoDeUso.getTiposUsuario();
        return new ResponseEntity<List<TipoUsuarioDTORespuesta>>(
                mapper.mapearTipoUsuarioARespuesta(tiposUsuario), HttpStatus.OK
        );
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_ACCESO)
    @GetMapping("/paginado")
    public ResponseEntity<?> indexPaginado(@RequestParam("pagina") int pagina,
                                           @RequestParam("tamanio") int tamanio) {
        var respuesta = casoDeUso.getUsuarios(pagina, tamanio);
        return new ResponseEntity<>(
                new PaginacionRespuestaDTO<>(
                        mapper.mapearModelosARespuestaLiviano(respuesta.getContent()),
                        respuesta.getTotalElements()
                ),
                HttpStatus.OK
        );
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_ACCESO)
    @GetMapping("/filtro")
    public ResponseEntity<?> getUsuariosByNombresApellidos(
            @RequestParam(value = "nombreCompleto", required = false) String nombreCompleto,
            @RequestParam("pagina") int pagina,
            @RequestParam("tamanio") int tamanio) {
        var respuesta = casoDeUso.getUsuariosByNombreCompleto(nombreCompleto, pagina, tamanio);
        return new ResponseEntity<>(
                new PaginacionRespuestaDTO<>(
                        mapper.mapearModelosARespuestaLiviano(respuesta.getContent()),
                        respuesta.getTotalElements()
                ),
                HttpStatus.OK
        );
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_ACCESO)
    @GetMapping
    public ResponseEntity<?> index(){
        List<UsuarioLiviano> usuarios = casoDeUso.getUsuarios();
        return new ResponseEntity<List<UsuarioLivianoDTORespuesta>>(
                mapper.mapearModelosARespuestaLiviano(usuarios), HttpStatus.OK
        );
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_ACCESO)
    @GetMapping("/funcionarios")
    public ResponseEntity<?> getFuncionarios(){
        List<Funcionario> funcionarios = casoDeUso.getFuncionarios();
        return new ResponseEntity<List<UsuarioLivianoDTORespuesta>>(
                mapper.mapearModelosARespuestaFuncionario(funcionarios), HttpStatus.OK
        );
    }

    @GetMapping("/{uuidUsuario}")
    public ResponseEntity<?> getUsuario(@PathVariable String uuidUsuario){
        Usuario usuario = casoDeUso.getUsuario(uuidUsuario);
        return new ResponseEntity<UsuarioDTORespuesta>(
                mapper.mapearModeloARespuesta(usuario), HttpStatus.OK
        );
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_ACCESO)
    @Transactional
    @PostMapping
    public ResponseEntity<?> crearUsuario(@Valid @RequestBody UsuarioDTOPeticion peticion, @RequestParam String tipoUsuario,
                                          @RequestHeader("Authorization") String token){
        Usuario usuario;
        try{
            usuario = casoDeUso.crearUsuario(mapper.mapearPeticionAModelo(peticion), tipoUsuario, token.substring(7));
        } catch (DataAccessException ex){
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Error insertando en la base de datos....");
            response.put("error", ex.getMessage() + " " + ex.getMostSpecificCause().getMessage());
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<UsuarioDTORespuesta>(
                mapper.mapearModeloARespuesta(usuario), HttpStatus.OK
        );
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_ACCESO)
    @Transactional
    @PostMapping("/cargar/archivo")
    public ResponseEntity<?> crearUsuarios(@RequestParam("file") MultipartFile file, @RequestHeader("Authorization") String token){
        List<UsuarioDTOPeticion> peticiones = procesadorArchivos.procesarArchivo(file);
        Map<String, String> erroresPeticiones;
        for(UsuarioDTOPeticion peticion : peticiones) {
            erroresPeticiones = validadorPeticion.validar(peticion);
            if(erroresPeticiones != null)
                return new ResponseEntity<Map<String, String>>(erroresPeticiones, HttpStatus.BAD_REQUEST);
        }

        List<Usuario> usuarios = mapper.mapearPeticionesAModelo(peticiones);
        List<Usuario> respuesta;
        try {
            respuesta = casoDeUso.crearUsuarios(usuarios, token.substring(7));
        } catch (DataAccessException ex){
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Error insertando en la base de datos....");
            response.put("error", ex.getMessage() + " " + ex.getMostSpecificCause().getMessage());
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<List<UsuarioDTORespuesta>>(
                mapper.mapearModelosARespuesta(respuesta), HttpStatus.OK
        );
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_ACCESO)
    @Transactional
    @PutMapping("/{uuidUsuario}")
    public ResponseEntity<?> actualizarUsuario(@PathVariable String uuidUsuario, @Valid @RequestBody UsuarioActualizarDTOPeticion peticion,
                                               @RequestHeader("Authorization") String token){
        Usuario usuario;
        try{
            usuario = casoDeUso.actualizarUsuario(uuidUsuario, mapper.mapearPeticionActualizarAModelo(peticion), token.substring(7));
        } catch (DataAccessException ex){
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Error insertando en la base de datos....");
            response.put("error", ex.getMessage() + " " + ex.getMostSpecificCause().getMessage());
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<UsuarioDTORespuesta>(
                mapper.mapearModeloARespuesta(usuario), HttpStatus.OK
        );
    }

    @Transactional
    @PatchMapping("/{uuidUsuario}")
    public ResponseEntity<?> actualizarContraseña(@PathVariable String uuidUsuario, @Valid @RequestBody CambioContraseñaDTOPeticion peticion,
                                                  @RequestHeader("Authorization") String token){
        try{
            casoDeUso.cambiarContraseña(uuidUsuario, peticion.getContraseña(), peticion.getNuevaContraseña(), token.substring(7));
        } catch (DataAccessException ex){
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Error insertando en la base de datos....");
            response.put("error", ex.getMessage() + " " + ex.getMostSpecificCause().getMessage());
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return ResponseEntity.ok().build();
    }
}



===== ARCHIVO: backend/solicitudes/src/main/java/com/unicauca/cfiet/solicitudes/dominio/casosdeuso/UsuarioCUImplAdaptador.java =====

package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.UsuarioCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.ExcepcionesFormateadorIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.PasswordEncoderGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.UsuarioGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Funcionario;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoUsuario;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Usuario;
import com.unicauca.cfiet.solicitudes.dominio.modelos.UsuarioLiviano;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implementación de la interfaz de los casos de uso para la gestión de usuarios.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
public class UsuarioCUImplAdaptador implements UsuarioCUIntPuerto {
    private final UsuarioGatewayIntPuerto gateway;
    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;
    private final LogCUIntPuerto log;
    private final PasswordEncoderGatewayIntPuerto encoder;
    /* Constantes */
    private static final String USUARIOS = "usuarios";
    private static final String USUARIO = "Usuario";
    private static final String NUMERO_DOCUMENTO = "Numero de documento";
    private static final String CORREO_ELECTRONICO = "Correo Electrónico";
    private  static final String USERNAME = "username";
    private static final String TIPO_USUARIO = "Tipo de usuario";
    private static final String TIPOS_USUARIO = "Tipos de usuario";
    private static final String NOMBRE = "nombre";

    public UsuarioCUImplAdaptador(UsuarioGatewayIntPuerto gateway,
                                  ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                  LogCUIntPuerto log,
                                  PasswordEncoderGatewayIntPuerto encoder){
        this.gateway = gateway;
        this.formateadorExcepciones = formateadorExcepciones;
        this.log = log;
        this.encoder = encoder;
    }

    @Override
    public List<UsuarioLiviano> getUsuarios() {
        List<UsuarioLiviano> usuarios = gateway.getUsuarios();
        if(usuarios.isEmpty())
            formateadorExcepciones.lanzarSinInformacion(String.format(MensajesError.SIN_REGISTROS, USUARIOS));
        return  usuarios;
    }

    @Override
    public List<Funcionario> getFuncionarios(){
        return gateway.getFuncionarios();
    }

    @Override
    public PaginacionRespuestaDTO<UsuarioLiviano> getUsuarios(int pagina, int tamanio) {
        if (pagina < 0 || tamanio < 0)
            formateadorExcepciones.lanzarMalFormato(MensajesError.PAGINACION_ERROR);
        PaginacionRespuestaDTO<UsuarioLiviano> respuesta = gateway.getUsuarios(pagina, tamanio);
        if (respuesta.getContent().isEmpty())
            formateadorExcepciones.lanzarSinInformacion(String.format(MensajesError.SIN_REGISTROS, USUARIOS));
        return respuesta;
    }

    @Override
    public PaginacionRespuestaDTO<UsuarioLiviano> getUsuariosByNombreCompleto(String nombreCompleto, int pagina, int tamanio) {
        if (pagina < 0 || tamanio < 0)
            formateadorExcepciones.lanzarMalFormato(MensajesError.PAGINACION_ERROR);
        PaginacionRespuestaDTO<UsuarioLiviano> respuesta = gateway.getUsuariosByNombreCompleto(nombreCompleto, pagina, tamanio);
        if (respuesta.getContent().isEmpty())
            formateadorExcepciones.lanzarSinInformacion(String.format(MensajesError.SIN_REGISTROS, USUARIOS));
        return respuesta;
    }

    @Override
    public Usuario getUsuario(String uuidUsuario) {
        Usuario usuario = gateway.getUsuario(uuidUsuario);
        if(usuario == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, USUARIO, uuidUsuario));
        return usuario;
    }

    @Override
    public Usuario crearUsuario(Usuario usuario, String tipoUsuario, String token) {
        if(!usuario.revisarTipoDocumento())
            formateadorExcepciones.lanzarMalFormato(MensajesError.TIPO_DOCUMENTO_ERRONEO);
        if(gateway.existeUsuarioNumeroDocumento(usuario.getNumeroDocumento()))
            formateadorExcepciones.lanzarEntidadExiste(String.format(MensajesError.ATRIBUTO_UNICO_YA_EXISTE, USUARIO, NUMERO_DOCUMENTO, usuario.getNumeroDocumento()));
        if(gateway.existeUsuarioCorreo(usuario.getCorreoElectronico()))
            formateadorExcepciones.lanzarEntidadExiste(String.format(MensajesError.ATRIBUTO_UNICO_YA_EXISTE, USUARIO, CORREO_ELECTRONICO, usuario.getCorreoElectronico()));
        if(gateway.existeUsuarioUsername(usuario.getUsername()))
            formateadorExcepciones.lanzarEntidadExiste(String.format(MensajesError.ATRIBUTO_UNICO_YA_EXISTE, USUARIO, USERNAME, usuario.getUsername()));

        TipoUsuario objTipoUsuario = gateway.getTipoUsuarioPorNombre(usuario.getObjTipoUsuario().getNombre());
        if(objTipoUsuario == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA_FILTRO, TIPO_USUARIO,  NOMBRE, usuario.getObjTipoUsuario().getNombre()));

        if(usuario.tieneRolesDuplicados())
            formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.ROLES_DUPLICADOS_USUARIO);
        if(!usuario.rolesSonValidos(gateway.getRoles()))
            formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.ROLES_NO_VALIDOS);

        usuario.setUuidUsuario(UUID.randomUUID().toString());
        usuario.setPassword(encoder.encriptarContraseña(usuario.getPassword()));
        usuario.setObjTipoUsuario(objTipoUsuario);
        Usuario instancia = usuario.crearInstancia(tipoUsuario);
        if(instancia == null)
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.INSTANCIA_NO_VALIDA, USUARIO));

        log.crearLog("Crear Usuario", String.format("Usuario %s %s creado con uuid %s",usuario.getNombres(), usuario.getApellidos(), usuario.getUuidUsuario()), token);
        return gateway.guardarUsuario(instancia);
    }

    @Override
    public List<Usuario> crearUsuarios(List<Usuario> usuarios, String token) {
        if (usuarios.isEmpty())
            formateadorExcepciones.lanzarSinInformacion(String.format(MensajesError.ARCHIVO_EXCEL_VACIO, USUARIOS));

        for (Usuario usuario : usuarios) {
            if(!usuario.revisarTipoDocumento())
                formateadorExcepciones.lanzarMalFormato(MensajesError.TIPO_DOCUMENTO_ERRONEO);
            if (gateway.existeUsuarioNumeroDocumento(usuario.getNumeroDocumento()))
                formateadorExcepciones.lanzarEntidadExiste(String.format(
                        MensajesError.ATRIBUTO_UNICO_YA_EXISTE, USUARIO, NUMERO_DOCUMENTO, usuario.getNumeroDocumento()));

            if (gateway.existeUsuarioCorreo(usuario.getCorreoElectronico()))
                formateadorExcepciones.lanzarEntidadExiste(String.format(
                        MensajesError.ATRIBUTO_UNICO_YA_EXISTE, USUARIO, CORREO_ELECTRONICO, usuario.getCorreoElectronico()));

            if (gateway.existeUsuarioUsername(usuario.getUsername()))
                formateadorExcepciones.lanzarEntidadExiste(String.format(
                        MensajesError.ATRIBUTO_UNICO_YA_EXISTE, USUARIO, USERNAME, usuario.getUsername()));

            TipoUsuario objTipoUsuario = gateway.getTipoUsuarioPorNombre(usuario.getObjTipoUsuario().getNombre());
            if (objTipoUsuario == null)
                formateadorExcepciones.lanzarEntidadNoExiste(String.format(
                        MensajesError.ENTIDAD_NO_ENCONTRADA_FILTRO, TIPO_USUARIO, NOMBRE, usuario.getObjTipoUsuario().getNombre()));

            if (usuario.tieneRolesDuplicados())
                formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.ROLES_DUPLICADOS_USUARIO);

            if (!usuario.rolesSonValidos(gateway.getRoles()))
                formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.ROLES_NO_VALIDOS);

            usuario.setUuidUsuario(UUID.randomUUID().toString());
            usuario.setPassword(encoder.encriptarContraseña(usuario.getPassword()));
            usuario.setObjTipoUsuario(objTipoUsuario);
        }

        List<Usuario> instancias = new ArrayList<>();
        for (Usuario usuario : usuarios) {
            String tipoUsuario = usuario.getRoles().get(0).getNombre().replace(" ", "");
            Usuario instancia = usuario.crearInstancia(tipoUsuario);

            if (instancia == null)
                formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.INSTANCIA_NO_VALIDA, USUARIO));

            instancias.add(instancia);
        }

        List<Usuario> guardados = gateway.guardarUsuarios(instancias);

        for (Usuario usuario : guardados) {
            log.crearLog("Crear Usuario", String.format(
                    "Usuario %s %s creado con uuid %s",
                    usuario.getNombres(), usuario.getApellidos(), usuario.getUuidUsuario()), token);
        }

        return guardados;
    }

    @Override
    public Usuario actualizarUsuario(String uuidUsuario, Usuario usuario, String token) {
        Usuario usuarioObtenido = gateway.getUsuario(uuidUsuario);
        if(usuarioObtenido == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, USUARIO, uuidUsuario));

        if(!usuario.revisarTipoDocumento())
            formateadorExcepciones.lanzarMalFormato(MensajesError.TIPO_DOCUMENTO_ERRONEO);

        if(usuario.tieneRolesDuplicados())
            formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.ROLES_DUPLICADOS_USUARIO);
        if(!usuario.rolesSonValidos(gateway.getRoles()))
            formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.ROLES_NO_VALIDOS);

        if(!usuarioObtenido.getNumeroDocumento().equals(usuario.getNumeroDocumento())){
            if(gateway.existeUsuarioNumeroDocumento(usuario.getNumeroDocumento()))
                formateadorExcepciones.lanzarEntidadExiste(String.format(MensajesError.ATRIBUTO_UNICO_YA_EXISTE, USUARIO, NUMERO_DOCUMENTO, usuario.getNumeroDocumento()));
        }

        if(!usuarioObtenido.getCorreoElectronico().equals(usuario.getCorreoElectronico())){
            if(gateway.existeUsuarioCorreo(usuario.getCorreoElectronico()))
                formateadorExcepciones.lanzarEntidadExiste(String.format(MensajesError.ATRIBUTO_UNICO_YA_EXISTE, USUARIO, CORREO_ELECTRONICO, usuario.getCorreoElectronico()));
        }

        if(!usuarioObtenido.getUsername().equals(usuario.getUsername())) {
            if (gateway.existeUsuarioUsername(usuario.getUsername()))
                formateadorExcepciones.lanzarEntidadExiste(String.format(MensajesError.ATRIBUTO_UNICO_YA_EXISTE, USUARIO, USERNAME, usuario.getUsername()));
        }

        usuarioObtenido.actualizarUsuario(usuario);
        log.crearLog("Actualizar Usuario", "Usuario actualizado con éxito", token);
        return gateway.guardarUsuario(usuarioObtenido);
    }

    @Override
    public Usuario cambiarContraseña(String uuidUsuario, String contraseña, String nuevaContraseña, String token) {
        Usuario usuario = getUsuario(uuidUsuario);
        if(!encoder.contraseñaCoincide(usuario.getPassword(), contraseña))
            formateadorExcepciones.lanzarCredencialesErroneas(MensajesError.CONTRASEÑA_INCORRECTA);
        usuario.setPassword(encoder.encriptarContraseña(nuevaContraseña));
        log.crearLog("Actualizar Usuario", "Usuario actualizo contraseña con éxito", token);
        return gateway.guardarUsuario(usuario);
    }

    @Override
    public List<TipoUsuario> getTiposUsuario() {
        List<TipoUsuario> tiposUsuario = gateway.getTiposUsuario();
        if(tiposUsuario.isEmpty())
            formateadorExcepciones.lanzarSinInformacion(String.format(MensajesError.SIN_REGISTROS, TIPOS_USUARIO));
        return  tiposUsuario;
    }
}



===== ARCHIVO: backend/solicitudes/src/main/java/com/unicauca/cfiet/solicitudes/infraestructura/output/persistencia/gateway/UsuarioGatewayImplAdapter.java =====

package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.gateway;

import com.unicauca.cfiet.solicitudes.aplicacion.output.UsuarioGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.*;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper.*;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.FuncionarioRepositorio;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.UsuarioLivianoRepositorio;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.UsuarioRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;

/**
 * Implementación de la fachada con el servicio de persistencia para la gestión de usuarios.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Service
@RequiredArgsConstructor
public class UsuarioGatewayImplAdapter implements UsuarioGatewayIntPuerto {
    private final UsuarioLivianoRepositorio repositorioBasico;
    private final UsuarioRepositorio repositorio;
    private final FuncionarioRepositorio repositorioFuncionarios;
    private final UsuarioOwnMapper usuarioMapper;
    private final UsuarioLivianoOwnMapper usuarioLivianoOwnMapper;
    private final FuncionarioOwnMapper funcionarioMapper;
    private final TipoUsuarioOwnMapper tipoUsuarioMapper;
    private final RolOwnMapperImpl rolMapper;

    @Override
    public List<UsuarioLiviano> getUsuarios() {
        List<UsuarioLivianoEntidad> entidades = repositorioBasico.findAll(Sort.by("fechaCreacion").descending());

        return entidades.stream()
                .map(usuarioLivianoOwnMapper::toDominio)
                .toList();
    }

    @Override
    public PaginacionRespuestaDTO<UsuarioLiviano> getUsuarios(int pagina, int tamanio) {
        Pageable paginado = PageRequest.of(
                pagina,
                tamanio,
                Sort.by("fechaCreacion").descending()
                        .and(Sort.by("uuidUsuario").ascending())
        );

        Page<UsuarioLivianoEntidad> page = repositorioBasico.findAll(paginado);

        List<UsuarioLiviano> usuarios = page.getContent().stream()
                .map(usuarioLivianoOwnMapper::toDominio)
                .toList();

        return new PaginacionRespuestaDTO<>(usuarios, page.getTotalElements());
    }

    @Override
    public PaginacionRespuestaDTO<UsuarioLiviano> getUsuariosByNombreCompleto(
            String nombreCompleto, int pagina, int tamanio) {

        Pageable paginado = PageRequest.of(
                pagina,
                tamanio,
                Sort.by("fechaCreacion").descending()
                        .and(Sort.by("uuidUsuario").ascending())
        );

        Page<UsuarioLivianoEntidad> page =
                repositorioBasico.findByNombreCompleto(nombreCompleto, paginado);

        List<UsuarioLiviano> usuarios = page.getContent().stream()
                .map(usuarioLivianoOwnMapper::toDominio)
                .toList();

        return new PaginacionRespuestaDTO<>(usuarios, page.getTotalElements());
    }

    @Override
    public List<Funcionario> getFuncionarios() {
        List<FuncionarioEntidad> entidades = repositorioFuncionarios.findAll();
        return entidades.stream()
                .map(funcionarioMapper::toDominio)
                .toList();
    }

    @Override
    public Usuario getUsuario(String uuid) {
        return repositorio.findById(uuid)
                .map(e -> {
                    if (e instanceof FuncionarioEntidad fe)
                        return funcionarioMapper.toDominio(fe);
                    return usuarioMapper.toDominio(e);
                })
                .orElse(null);
    }

    @Override
    public Usuario guardarUsuario(Usuario usuario) {
        UsuarioEntidad entidad;

        if (usuario instanceof Funcionario f)
            entidad = funcionarioMapper.toEntidad(f);
        else
            entidad = usuarioMapper.toEntidad(usuario);

        UsuarioEntidad guardado = repositorio.save(entidad);

        if (guardado instanceof FuncionarioEntidad fe)
            return funcionarioMapper.toDominio(fe);

        return usuarioMapper.toDominio(guardado);
    }

    @Override
    public List<Usuario> guardarUsuarios(List<Usuario> usuarios) {
        List<UsuarioEntidad> entidades = usuarios.stream()
                .map(u -> (u instanceof Funcionario f)
                        ? funcionarioMapper.toEntidad(f)
                        : usuarioMapper.toEntidad(u))
                .toList();

        List<UsuarioEntidad> guardados = repositorio.saveAll(entidades);

        return guardados.stream()
                .map(e -> {
                    if (e instanceof FuncionarioEntidad fe)
                        return funcionarioMapper.toDominio(fe);
                    return usuarioMapper.toDominio(e);
                })
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public boolean existeUsuarioNumeroDocumento(String numeroDocumento) {
        return repositorio.existsByNumeroDocumento(numeroDocumento);
    }

    @Override
    public boolean existeUsuarioCorreo(String correo) {
        return repositorio.existsByCorreoElectronico(correo);
    }

    @Override
    public boolean existeUsuarioUsername(String username) {
        return repositorio.existsByUsername(username);
    }

    @Override
    public List<TipoUsuario> getTiposUsuario() {
        return repositorio.findAllTipoUsuario().stream()
                .map(tipoUsuarioMapper::toDominio)
                .toList();
    }

    @Override
    public TipoUsuario getTipoUsuarioPorNombre(String nombre) {
        return repositorio.findTipoUsuarioByNombre(nombre)
                .map(tipoUsuarioMapper::toDominio)
                .orElse(null);
    }

    @Override
    public List<Rol> getRoles() {
        return repositorio.findAllRoles().stream()
                .map(rolMapper::toDominio)
                .toList();
    }
}

