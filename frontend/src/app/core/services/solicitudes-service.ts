import { Injectable } from '@angular/core';
import { environment } from '../../../enviroments/environment';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SolicitudDTORespuesta } from '../models/Solicitudes/DTOResponse/SolicitudDTORespuesta';
import { PaginacionRespuestaDTO } from '../models/PaginacionRespuestaDTO';
import { OrdenDelDiaDTORespuesta } from '../models/Solicitudes/DTOResponse/OrdenDelDiaDTORespuesta';
import { OrdenDelDiaDTOPeticion } from '../models/Solicitudes/DTORequest/OrdenDelDiaDTOPeticion';
import { SolicitudDTOPeticion } from '../models/Solicitudes/DTORequest/SolicitudDTOPeticion';
import { SolicitudActualizarDTOPeticion } from '../models/Solicitudes/DTORequest/SolicitudActualizarDTOPeticion';
import { SolicitudPublicaDTOPeticion } from '../models/Solicitudes/DTORequest/SolicitudPublicaDTOPeticion';

/**
 * Servicio encargado de la gestión de solicitudes y órdenes del día.
 * Proporciona métodos para crear, consultar, actualizar, buscar,
 * exportar y descargar información relacionada.
 *
 * @author Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Injectable({
  providedIn: 'root',
})
export class SolicitudesService {
  /**
   * URL base del servicio de solicitudes.
   */
  private url = `${environment.apiUrl}/solicitudes`;

  constructor(private http: HttpClient) {}

  /**
   * Obtiene todas las órdenes del día.
   */
  getOrdenesDelDia(): Observable<OrdenDelDiaDTORespuesta[]> {
    return this.http.get<OrdenDelDiaDTORespuesta[]>(`${this.url}/orden-del-dia`);
  }

  /**
   * Obtiene las órdenes del día de forma paginada.
   */
  getOrdenesDelDiaPaginado(
    pagina: number,
    tamanio: number
  ): Observable<PaginacionRespuestaDTO<OrdenDelDiaDTORespuesta>> {
    return this.http.get<PaginacionRespuestaDTO<OrdenDelDiaDTORespuesta>>(
      `${this.url}/orden-del-dia/paginado?pagina=${pagina}&tamanio=${tamanio}`
    );
  }

  /**
   * Obtiene la información de una orden del día específica.
   */
  getOrdenDelDia(uuidOrdenDelDia: string): Observable<OrdenDelDiaDTORespuesta> {
    return this.http.get<OrdenDelDiaDTORespuesta>(`${this.url}/orden-del-dia/${uuidOrdenDelDia}`);
  }

  /**
   * Crea una nueva orden del día.
   */
  crearOrdenDelDia(peticion: OrdenDelDiaDTOPeticion): Observable<OrdenDelDiaDTORespuesta> {
    return this.http.post<OrdenDelDiaDTORespuesta>(`${this.url}/orden-del-dia`, peticion);
  }

  /**
   * Actualiza una orden del día existente.
   */
  actualizarOrdenDelDia(
    uuidOrdenDelDia: string,
    peticion: OrdenDelDiaDTOPeticion
  ): Observable<OrdenDelDiaDTORespuesta> {
    return this.http.put<OrdenDelDiaDTORespuesta>(
      `${this.url}/orden-del-dia/${uuidOrdenDelDia}`,
      peticion
    );
  }

  /**
   * Busca órdenes del día por un filtro de texto.
   */
  buscarOrdenesDelDia(
    filtro: string,
    pagina: number,
    tamanio: number
  ): Observable<PaginacionRespuestaDTO<OrdenDelDiaDTORespuesta>> {
    return this.http.get<PaginacionRespuestaDTO<OrdenDelDiaDTORespuesta>>(
      `${this.url}/orden-del-dia/buscar?filtro=${filtro}&pagina=${pagina}&tamanio=${tamanio}`
    );
  }

  /**
   * Obtiene las solicitudes asociadas a una orden del día.
   */
  getSolicitudesPorOrdenDelDia(uuidOrdenDelDia: string): Observable<SolicitudDTORespuesta[]> {
    return this.http.get<SolicitudDTORespuesta[]>(
      `${this.url}/orden-del-dia/${uuidOrdenDelDia}/solicitudes`
    );
  }

  /**
   * Obtiene todas las solicitudes.
   */
  getSolicitudes(): Observable<SolicitudDTORespuesta[]> {
    return this.http.get<SolicitudDTORespuesta[]>(`${this.url}`);
  }

  /**
   * Obtiene las solicitudes de forma paginada.
   */
  getSolicitudesPaginado(
    pagina: number,
    tamanio: number
  ): Observable<PaginacionRespuestaDTO<SolicitudDTORespuesta>> {
    return this.http.get<PaginacionRespuestaDTO<SolicitudDTORespuesta>>(
      `${this.url}/paginado?pagina=${pagina}&tamanio=${tamanio}`
    );
  }

  /**
   * Obtiene la información de una solicitud específica.
   */
  getSolicitud(uuidSolicitud: string): Observable<SolicitudDTORespuesta> {
    return this.http.get<SolicitudDTORespuesta>(`${this.url}/${uuidSolicitud}`);
  }

  /**
   * Envía una nueva solicitud con archivos adjuntos.
   */
  enviarSolicitud(
    peticion: SolicitudDTOPeticion,
    archivos: File[]
  ): Observable<SolicitudDTORespuesta> {
    const formData = new FormData();
    formData.append(
      'solicitud',
      new Blob([JSON.stringify(peticion)], { type: 'application/json' })
    );

    archivos.forEach((file) => formData.append('archivos', file));

    return this.http.post<SolicitudDTORespuesta>(`${this.url}`, formData);
  }

  /**
   * Envía una solicitud pública con archivos adjuntos.
   */
  enviarSolicitudPublica(
    peticion: SolicitudPublicaDTOPeticion,
    archivos: File[]
  ): Observable<SolicitudDTORespuesta> {
    const formData = new FormData();
    formData.append(
      'solicitud',
      new Blob([JSON.stringify(peticion)], { type: 'application/json' })
    );

    archivos.forEach((file) => formData.append('archivos', file));

    return this.http.post<SolicitudDTORespuesta>(`${this.url}/public`, formData);
  }

  /**
   * Actualiza una solicitud existente.
   */
  actualizarSolicitud(
    uuidSolicitud: string,
    peticion: SolicitudActualizarDTOPeticion
  ): Observable<SolicitudDTORespuesta> {
    return this.http.put<SolicitudDTORespuesta>(`${this.url}/${uuidSolicitud}`, peticion);
  }

  /**
   * Obtiene las solicitudes asociadas a un funcionario.
   */
  getSolicitudesPorFuncionario(
    uuidFuncionario: string,
    pagina: number,
    tamanio: number
  ): Observable<PaginacionRespuestaDTO<SolicitudDTORespuesta>> {
    return this.http.get<PaginacionRespuestaDTO<SolicitudDTORespuesta>>(
      `${this.url}/funcionario/${uuidFuncionario}?pagina=${pagina}&tamanio=${tamanio}`
    );
  }

  /**
   * Obtiene las solicitudes por estado.
   */
  getSolicitudesPorEstado(estado: string): Observable<SolicitudDTORespuesta[]> {
    return this.http.get<SolicitudDTORespuesta[]>(`${this.url}/estado?estado=${estado}`);
  }

  /**
   * Busca solicitudes por nombre.
   */
  buscarSolicitudesPorNombre(
    filtro: string,
    pagina: number,
    tamanio: number
  ): Observable<PaginacionRespuestaDTO<SolicitudDTORespuesta>> {
    return this.http.get<PaginacionRespuestaDTO<SolicitudDTORespuesta>>(
      `${this.url}/buscar?filtro=${filtro}&pagina=${pagina}&tamanio=${tamanio}`
    );
  }

  /**
   * Busca solicitudes por nombre y funcionario.
   */
  buscarSolicitudesPorNombreYFuncionario(
    uuidFuncionario: string,
    filtro: string,
    pagina: number,
    tamanio: number
  ): Observable<PaginacionRespuestaDTO<SolicitudDTORespuesta>> {
    return this.http.get<PaginacionRespuestaDTO<SolicitudDTORespuesta>>(
      `${this.url}/fun/buscar/?uuidFuncionario=${uuidFuncionario}&filtro=${filtro}&pagina=${pagina}&tamanio=${tamanio}`
    );
  }

  /**
   * Obtiene órdenes del día según su estado.
   */
  getOrdenesDelDiaPorEstado(estado: boolean): Observable<OrdenDelDiaDTORespuesta[]> {
    return this.http.get<OrdenDelDiaDTORespuesta[]>(
      `${this.url}/orden-del-dia/estado?estado=${estado}`
    );
  }

  /**
   * Descarga los anexos asociados a una orden del día.
   */
  descargarAnexosOrdenDelDia(uuidOrden: string, nombreOrden: string): Observable<Blob> {
    const params = { uuidOrden, nombreOrden };
    return this.http.get(`${this.url}/anexos/download`, {
      params,
      responseType: 'blob',
    });
  }

  /**
   * Exporta una orden del día en formato descargable.
   */
  exportarOrdenDelDia(uuidOrden: string): Observable<Blob> {
    return this.http.get(`${this.url}/exportar`, {
      params: { uuidOrden },
      responseType: 'blob',
    });
  }

  /**
   * Exporta una orden del día con las respuestas del consejo.
   */
  exportarOrdenDelDiaConRespuestas(uuidOrden: string): Observable<Blob> {
    return this.http.get(`${this.url}/exportar/respuestas`, {
      params: { uuidOrden },
      responseType: 'blob',
    });
  }

  /**
   * Exporta la orden del día en formato merge/reunión
   * (nombre + descripción + respuestas en secciones).
   */
  exportarOrdenDelDiaMerge(uuidOrden: string): Observable<Blob> {
    return this.http.get(`${this.url}/exportar/reunion`, {
      params: { uuidOrden },
      responseType: 'blob',
    });
  }
}
