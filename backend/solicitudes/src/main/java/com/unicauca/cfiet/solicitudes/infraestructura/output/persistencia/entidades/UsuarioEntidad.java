package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Entity
@Table(name = "usuarios")
@Getter
@Setter
@AllArgsConstructor
@SuperBuilder
public class UsuarioEntidad extends UsuarioLivianoEntidad implements UserDetails {
    @Column(nullable = false, length = 1000)
    private String tipoDocumento;
    @Column(nullable = false, unique = true)
    private String numeroDocumento;
    @Column(nullable = false, length = 1000)
    private String telefono;
    @Column(nullable = false, unique = true)
    private String correoElectronico;
    @Column(nullable = false, unique = true)
    private String username;
    @Column(nullable = false, length = 1000)
    private String password;
    @ManyToOne
    @JoinColumn(name = "uuidTipoUsuario", nullable = false)
    private TipoUsuarioEntidad objTipoUsuario;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name="Usuario_has_Roles",
            joinColumns = @JoinColumn(name = "uuidUsuario"),
            inverseJoinColumns = @JoinColumn(name = "uuidRol")
    )
    private List<RolEntidad> roles;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "objUsuarioLog")
    private List<LogEntidad> logs;

    public UsuarioEntidad(){
        super();
        this.roles = new ArrayList<>();
        this.logs = new ArrayList<>();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> autorizaciones = new ArrayList<>();
        for(RolEntidad rol : roles)
            autorizaciones.add(new SimpleGrantedAuthority(rol.getNombre()));
        return autorizaciones;
    }
}
