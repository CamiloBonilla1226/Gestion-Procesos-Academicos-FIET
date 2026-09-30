import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { RolDTORespuesta } from '../models/Rol/DTOResponse/RolDTORespuesta';
import { RolDTOPeticion } from '../models/Rol/DTORequest/RolDTOPeticion';
import { environment } from '../../../enviroments/environment';

/**
 * Servicio encargado de la gestión de roles del sistema.
 * Permite consultar, obtener y actualizar la información
 * relacionada con los roles.
 *
 * @author Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Injectable({
  providedIn: 'root'
})
export class RolesService {

  /**
   * URL base del servicio de roles.
   */
  private url = `${environment.apiUrl}/roles`;

  constructor(private http: HttpClient) { }

  /**
   * Obtiene la lista completa de roles del sistema.
   *
   * @returns Observable con el listado de roles
   */
  getRoles(): Observable<RolDTORespuesta[]> {
    return this.http.get<RolDTORespuesta[]>(this.url);
  }

  /**
   * Obtiene la lista de roles de forma paginada.
   *
   * @param pagina Número de la página a consultar
   * @param tamanio Cantidad de registros por página
   * @returns Observable con el listado paginado de roles
   */
  getRolesPaginados(
    pagina: number,
    tamanio: number
  ): Observable<RolDTORespuesta[]> {
    return this.http.get<RolDTORespuesta[]>(
      `${this.url}/paginado?pagina=${pagina}&tamanio=${tamanio}`
    );
  }

  /**
   * Obtiene la información de un rol específico.
   *
   * @param uuidRol Identificador único del rol
   * @returns Observable con la información del rol
   */
  getRol(uuidRol: string): Observable<RolDTORespuesta> {
    return this.http.get<RolDTORespuesta>(`${this.url}/${uuidRol}`);
  }

  /**
   * Actualiza la información de un rol existente.
   *
   * @param uuidRol Identificador único del rol
   * @param rol Datos del rol a actualizar
   * @returns Observable con el rol actualizado
   */
  actualizarRol(
    uuidRol: string,
    rol: RolDTOPeticion
  ): Observable<RolDTORespuesta> {
    return this.http.put<RolDTORespuesta>(`${this.url}/${uuidRol}`, rol);
  }
}
