import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../enviroments/environment';
import { TipoSolicitudDTORespuesta } from '../models/TipoSolicitud/DTOResponse/TipoSolicitudDTORespuesta';
import { TipoSolicitudDTOPeticion } from '../models/TipoSolicitud/DTORequest/TipoSolicitudDTOPeticion';
import { PaginacionRespuestaDTO } from '../models/PaginacionRespuestaDTO';

/**
 * Servicio encargado de la gestión de los tipos de solicitud.
 * Permite consultar, crear, actualizar y filtrar tipos de solicitudes
 * según distintos criterios como nombre, perfil o funcionario.
 *
 * @author Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Injectable({
  providedIn: 'root'
})
export class TipoSolicitudService {

  /**
   * URL base del servicio de tipos de solicitud.
   */
  private url = `${environment.apiUrl}/tipos/solicitudes`;

  constructor(private http: HttpClient) {}

  /**
   * Obtiene todos los tipos de solicitud.
   */
  getTiposSolicitud(): Observable<TipoSolicitudDTORespuesta[]> {
    return this.http.get<TipoSolicitudDTORespuesta[]>(`${this.url}`);
  }

  /**
   * Obtiene un tipo de solicitud por su identificador.
   */
  getTipoSolicitud(uuidTipoSolicitud: string): Observable<TipoSolicitudDTORespuesta> {
    return this.http.get<TipoSolicitudDTORespuesta>(`${this.url}/${uuidTipoSolicitud}`);
  }

  /**
   * Obtiene los tipos de solicitud de forma paginada.
   */
  getTiposSolicitudPaginado(
    pagina: number,
    tamanio: number
  ): Observable<PaginacionRespuestaDTO<TipoSolicitudDTORespuesta>> {
    return this.http.get<PaginacionRespuestaDTO<TipoSolicitudDTORespuesta>>(
      `${this.url}/paginado?pagina=${pagina}&tamanio=${tamanio}`
    );
  }

  /**
   * Obtiene los tipos de solicitud aplicando filtros por nombre y funcionario.
   */
  getTiposSolicitudFiltrado(
    nombreSolicitud: string,
    funcionario: string,
    pagina: number,
    tamanio: number
  ): Observable<PaginacionRespuestaDTO<TipoSolicitudDTORespuesta>> {
    const params = new URLSearchParams();

    if (nombreSolicitud && nombreSolicitud.trim() !== '') {
      params.append('nombreSolicitud', nombreSolicitud.trim());
    }

    if (funcionario && funcionario.trim() !== '') {
      params.append('funcionario', funcionario.trim());
    }

    params.append('pagina', pagina.toString());
    params.append('tamanio', tamanio.toString());

    return this.http.get<PaginacionRespuestaDTO<TipoSolicitudDTORespuesta>>(
      `${this.url}/filtro?${params.toString()}`
    );
  }

  /**
   * Crea un nuevo tipo de solicitud.
   */
  crearTipoSolicitud(
    peticion: TipoSolicitudDTOPeticion
  ): Observable<TipoSolicitudDTORespuesta> {
    return this.http.post<TipoSolicitudDTORespuesta>(`${this.url}`, peticion);
  }

  /**
   * Crea múltiples tipos de solicitud a partir de un archivo.
   */
  crearTiposSolicitudDesdeArchivo(
    file: File
  ): Observable<TipoSolicitudDTORespuesta[]> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<TipoSolicitudDTORespuesta[]>(
      `${this.url}/cargar/archivo`,
      formData
    );
  }

  /**
   * Actualiza un tipo de solicitud existente.
   */
  actualizarTipoSolicitud(
    uuidTipoSolicitud: string,
    peticion: TipoSolicitudDTOPeticion
  ): Observable<TipoSolicitudDTORespuesta> {
    return this.http.put<TipoSolicitudDTORespuesta>(
      `${this.url}/${uuidTipoSolicitud}`,
      peticion
    );
  }

  /**
   * Obtiene los tipos de solicitud asociados a un perfil de solicitante de forma paginada.
   */
  getTiposSolicitudPorPerfilSolicitante(
    perfil: string,
    pagina: number,
    tamanio: number
  ): Observable<PaginacionRespuestaDTO<TipoSolicitudDTORespuesta>> {
    return this.http.get<PaginacionRespuestaDTO<TipoSolicitudDTORespuesta>>(
      `${this.url}/perfil/paginado?perfil=${perfil}&pagina=${pagina}&tamanio=${tamanio}`
    );
  }

  /**
   * Obtiene los tipos de solicitud filtrados por nombre y perfil de solicitante.
   */
  getTiposSolicitudPorNombreYPerfilSolicitante(
    nombre: string,
    perfil: string,
    pagina: number,
    tamanio: number
  ): Observable<PaginacionRespuestaDTO<TipoSolicitudDTORespuesta>> {
    const params = new URLSearchParams();
    if (nombre && nombre.trim() !== '') params.append('nombre', nombre.trim());
    params.append('perfil', perfil);
    params.append('pagina', pagina.toString());
    params.append('tamanio', tamanio.toString());

    return this.http.get<PaginacionRespuestaDTO<TipoSolicitudDTORespuesta>>(
      `${this.url}/perfil/filtro?${params.toString()}`
    );
  }

  /**
   * Obtiene los tipos de solicitud asociados a un perfil específico.
   */
  getTiposSolicitudPorPerfil(
    perfil: string
  ): Observable<TipoSolicitudDTORespuesta[]> {
    return this.http.get<TipoSolicitudDTORespuesta[]>(
      `${this.url}/perfil?perfil=${perfil}`
    );
  }

}
