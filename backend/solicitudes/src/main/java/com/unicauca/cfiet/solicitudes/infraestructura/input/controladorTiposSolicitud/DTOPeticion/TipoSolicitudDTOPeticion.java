package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorTiposSolicitud.DTOPeticion;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Getter
@Setter
@Builder
public class TipoSolicitudDTOPeticion {
    @NotNull(message = "El nombre no puede ser nulo")
    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(min = 5, max = 1000, message = "El nombre debe tener entre 5 y 1000 caracteres")
    private String nombre;

    @Size(max = 1500, message = "La descripción no puede exceder los 1500 caracteres")
    private String descripcion;

    @NotNull(message = "La sección no puede ser nula")
    @NotBlank(message = "La sección no puede estar vacía")
    @Size(min = 5, max = 1000, message = "La sección debe tener entre 5 y 1000 caracteres")
    private String seccion;

    @NotNull(message = "El perfil de solicitante no puede ser nulo")
    @NotBlank(message = "El perfil de solicitante no puede estar vacio")
    @Size(min = 5, max = 1000, message = "El perfil de solicitante debe tener entre 5 y 1000 caracteres")
    private String perfilSolicitante;

    @Valid
    @NotNull(message = "La lista de anexos no puede ser nula")
    @Size(min = 1, message = "Debe asignar al menos un anexo")
    private List<TipoAnexoDTOPeticion> anexos;

    @NotNull(message = "La uuid del funcionario no puede ser nula")
    @NotBlank(message = "La uuid del funcionario no puede estar vacia")
    private String uuidFuncionario;
}
