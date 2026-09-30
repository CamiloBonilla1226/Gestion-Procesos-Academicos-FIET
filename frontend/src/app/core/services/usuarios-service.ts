import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { UsuarioDTORespuesta } from '../models/Usuario/DTOResponse/UsuarioDTORespuesta';
import { UsuarioLivianoDTORespuesta } from '../models/Usuario/DTOResponse/UsuarioLivianoDTORespuesta';
import { UsuarioDTOPeticion } from '../models/Usuario/DTORequest/UsuarioDTOPeticion';
import { UsuarioActualizarDTOPeticion } from '../models/Usuario/DTORequest/UsuarioActualizarDTOPeticion';
import { CambioContraseñaDTOPeticion } from '../models/Usuario/DTORequest/CambioContraseñaDTOPeticion';
import { TipoUsuarioDTORespuesta } from '../models/Usuario/DTOResponse/TipoUsuarioDTORespuesta';
import { environment } from '../../../enviroments/environment';
import { PaginacionRespuestaDTO } from '../models/PaginacionRespuestaDTO';

/**
 * Servicio encargado de la gestión de usuarios del sistema.
 * Permite consultar, crear, actualizar usuarios y administrar contraseñas.
 *
 * @author Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Injectable({
  providedIn: 'root',
})
export class UsuariosService {
  /**
   * URL base del recurso de usuarios en la API.
   */
  private url = `${environment.apiUrl}/usuarios`;

  constructor(private http: HttpClient) {}

  /**
   * Obtiene el número total de usuarios registrados en el sistema.
   */
  getTotalUsuarios(): Observable<number> {
    return this.http.get<number>(`${this.url}/total`);
  }

  /**
   * Obtiene los tipos de usuario disponibles.
   */
  getTiposUsuario(): Observable<TipoUsuarioDTORespuesta[]> {
    return this.http.get<TipoUsuarioDTORespuesta[]>(`${this.url}/tipos`);
  }

  /**
   * Obtiene la lista de usuarios de forma paginada.
   *
   * @param pagina Número de página
   * @param tamanio Cantidad de registros por página
   */
  getUsuariosPaginado(
    pagina: number,
    tamanio: number
  ): Observable<PaginacionRespuestaDTO<UsuarioLivianoDTORespuesta>> {
    return this.http.get<PaginacionRespuestaDTO<UsuarioLivianoDTORespuesta>>(
      `${this.url}/paginado?pagina=${pagina}&tamanio=${tamanio}`
    );
  }

  /**
   * Obtiene usuarios filtrados por nombre completo de forma paginada.
   *
   * @param nombreCompleto Nombre completo del usuario
   * @param pagina Número de página
   * @param tamanio Cantidad de registros por página
   */
  getUsuariosFiltrados(
    nombreCompleto: string,
    pagina: number,
    tamanio: number
  ): Observable<PaginacionRespuestaDTO<UsuarioLivianoDTORespuesta>> {
    const params = new URLSearchParams();

    if (nombreCompleto && nombreCompleto.trim() !== '') {
      params.append('nombreCompleto', nombreCompleto.trim());
    }

    params.append('pagina', pagina.toString());
    params.append('tamanio', tamanio.toString());

    return this.http.get<PaginacionRespuestaDTO<UsuarioLivianoDTORespuesta>>(
      `${this.url}/filtro?${params.toString()}`
    );
  }

  /**
   * Obtiene la lista completa de usuarios.
   */
  getUsuarios(): Observable<UsuarioLivianoDTORespuesta[]> {
    return this.http.get<UsuarioLivianoDTORespuesta[]>(`${this.url}`);
  }

  /**
   * Obtiene la lista de usuarios que tienen rol de funcionario.
   */
  getFuncionarios(): Observable<UsuarioLivianoDTORespuesta[]> {
    return this.http.get<UsuarioLivianoDTORespuesta[]>(`${this.url}/funcionarios`);
  }

  /**
   * Obtiene la información detallada de un usuario específico.
   *
   * @param uuidUsuario Identificador único del usuario
   */
  getUsuario(uuidUsuario: string): Observable<UsuarioDTORespuesta> {
    return this.http.get<UsuarioDTORespuesta>(`${this.url}/${uuidUsuario}`);
  }

  /**
   * Crea un nuevo usuario en el sistema.
   *
   * @param peticion Datos del usuario
   * @param tipoUsuario Tipo de usuario a crear
   */
  crearUsuario(peticion: UsuarioDTOPeticion, tipoUsuario: string): Observable<UsuarioDTORespuesta> {
    return this.http.post<UsuarioDTORespuesta>(`${this.url}?tipoUsuario=${tipoUsuario}`, peticion);
  }

  /**
   * Crea múltiples usuarios a partir de un archivo.
   *
   * @param file Archivo con la información de los usuarios
   */
  crearUsuariosDesdeArchivo(file: File): Observable<UsuarioDTORespuesta[]> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post<UsuarioDTORespuesta[]>(`${this.url}/cargar/archivo`, formData);
  }

  /**
   * Actualiza la información de un usuario existente.
   *
   * @param uuidUsuario Identificador único del usuario
   * @param peticion Datos actualizados del usuario
   */
  actualizarUsuario(
    uuidUsuario: string,
    peticion: UsuarioActualizarDTOPeticion
  ): Observable<UsuarioDTORespuesta> {
    return this.http.put<UsuarioDTORespuesta>(`${this.url}/${uuidUsuario}`, peticion);
  }

  /**
   * Cambia la contraseña de un usuario.
   *
   * @param uuidUsuario Identificador único del usuario
   * @param peticion Datos para el cambio de contraseña
   */
  cambiarContraseña(uuidUsuario: string, peticion: CambioContraseñaDTOPeticion): Observable<void> {
    return this.http.patch<void>(`${this.url}/${uuidUsuario}`, peticion);
  }
}
