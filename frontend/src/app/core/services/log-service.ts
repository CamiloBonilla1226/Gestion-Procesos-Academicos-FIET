import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { LogDTORespuesta } from '../models/Log/DTOResponse/LogDTORespuesta';
import { environment } from '../../../enviroments/environment';
import { PaginacionRespuestaDTO } from '../models/PaginacionRespuestaDTO';

/**
 * Servicio encargado de la gestión de logs del sistema.
 * Permite consultar registros de actividad de forma general,
 * paginada y con filtros.
 *
 * @author Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Injectable({
  providedIn: 'root'
})
export class LogService {

  /**
   * URL base del servicio de logs.
   */
  private url = `${environment.apiUrl}/logs`;

  constructor(private http: HttpClient) { }

  /**
   * Obtiene la lista completa de logs del sistema.
   *
   * @returns Observable con el listado de logs
   */
  getLogs(): Observable<LogDTORespuesta[]> {
    return this.http.get<LogDTORespuesta[]>(this.url);
  }

  /**
   * Obtiene los logs del sistema de forma paginada.
   *
   * @param pagina Número de la página a consultar
   * @param tamanio Cantidad de registros por página
   * @returns Observable con la respuesta paginada de logs
   */
  getLogsPaginado(
    pagina: number,
    tamanio: number
  ): Observable<PaginacionRespuestaDTO<LogDTORespuesta>> {
    return this.http.get<PaginacionRespuestaDTO<LogDTORespuesta>>(
      `${this.url}/paginado?pagina=${pagina}&tamanio=${tamanio}`
    );
  }

  /**
   * Obtiene los logs del sistema aplicando filtros y paginación.
   *
   * @param responsable Nombre del responsable del evento
   * @param fecha Fecha del log a filtrar
   * @param pagina Número de la página a consultar
   * @param tamanio Cantidad de registros por página
   * @returns Observable con la respuesta paginada de logs filtrados
   */
  getLogsFiltrados(
    responsable: string,
    fecha: string,
    pagina: number,
    tamanio: number
  ): Observable<PaginacionRespuestaDTO<LogDTORespuesta>> {
    const params = new URLSearchParams();

    if (responsable && responsable.trim() !== '') 
      params.append('responsable', responsable.trim());

    if (fecha && fecha.trim() !== '') 
      params.append('fecha', fecha.trim());

    params.append('pagina', pagina.toString());
    params.append('tamanio', tamanio.toString());

    return this.http.get<PaginacionRespuestaDTO<LogDTORespuesta>>(
      `${this.url}/filtro?${params.toString()}`
    );
  }
}
