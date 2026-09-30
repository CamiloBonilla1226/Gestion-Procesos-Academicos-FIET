import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../enviroments/environment';
import { PaginacionRespuestaDTO } from '../models/PaginacionRespuestaDTO';
import { RespuestaDTORespuesta } from '../models/Respuesta/DTOResponse/RespuestaDTORespuesta';
import { RespuestaDTOPeticion } from '../models/Respuesta/DTORequest/RespuestaDTOPeticion';

@Injectable({
  providedIn: 'root',
})
export class RespuestasService {
  private url = `${environment.apiUrl}/respuestas`;

  constructor(private http: HttpClient) {}

  getRespuestas(
    pagina: number,
    tamanio: number
  ): Observable<PaginacionRespuestaDTO<RespuestaDTORespuesta>> {
    return this.http.get<PaginacionRespuestaDTO<RespuestaDTORespuesta>>(
      `${this.url}/paginado?pagina=${pagina}&tamanio=${tamanio}`
    );
  }

  getRespuestasPorNombreSolicitud(
    nombreSolicitud: string,
    pagina: number,
    tamanio: number
  ): Observable<PaginacionRespuestaDTO<RespuestaDTORespuesta>> {
    return this.http.get<PaginacionRespuestaDTO<RespuestaDTORespuesta>>(
      `${this.url}/paginado/nombre-solicitud?nombreSolicitud=${nombreSolicitud}&pagina=${pagina}&tamanio=${tamanio}`
    );
  }

  getRespuestasPorFuncionario(
    uuidFuncionario: string,
    pagina: number,
    tamanio: number
  ): Observable<PaginacionRespuestaDTO<RespuestaDTORespuesta>> {
    return this.http.get<PaginacionRespuestaDTO<RespuestaDTORespuesta>>(
      `${this.url}/paginado/funcionario?uuidFuncionario=${uuidFuncionario}&pagina=${pagina}&tamanio=${tamanio}`
    );
  }

  getRespuestasPorFuncionarioYNombreSolicitud(
    uuidFuncionario: string,
    nombreSolicitud: string,
    pagina: number,
    tamanio: number
  ): Observable<PaginacionRespuestaDTO<RespuestaDTORespuesta>> {
    return this.http.get<PaginacionRespuestaDTO<RespuestaDTORespuesta>>(
      `${this.url}/paginado/funcionario-nombre-solicitud?uuidFuncionario=${uuidFuncionario}&nombreSolicitud=${nombreSolicitud}&pagina=${pagina}&tamanio=${tamanio}`
    );
  }

  getRespuesta(uuidRespuesta: string): Observable<RespuestaDTORespuesta> {
    return this.http.get<RespuestaDTORespuesta>(`${this.url}/${uuidRespuesta}`);
  }

  getRespuestaPorSolicitud(uuidSolicitud: string): Observable<RespuestaDTORespuesta> {
    return this.http.get<RespuestaDTORespuesta>(`${this.url}/solicitud/${uuidSolicitud}`);
  }

  registrarRespuesta(
    uuidSolicitud: string,
    peticion: RespuestaDTOPeticion
  ): Observable<RespuestaDTORespuesta> {
    return this.http.post<RespuestaDTORespuesta>(`${this.url}/${uuidSolicitud}`, peticion);
  }

  responderSolicitud(uuidRespuesta: string, archivo: File): Observable<RespuestaDTORespuesta> {
    const formData = new FormData();
    formData.append('archivo', archivo);
    return this.http.post<RespuestaDTORespuesta>(`${this.url}/${uuidRespuesta}/archivo`, formData);
  }

  descargarRespuesta(uuidRespuesta: string, fileName: string): Observable<Blob> {
    const url = `${environment.domain}${this.url}/${uuidRespuesta}/${fileName}`.replace(/^\/+/, '');
    return this.http.get(url, { responseType: 'blob' });
  }

  eliminarArchivoRespuesta(uuidRespuesta: string): Observable<RespuestaDTORespuesta> {
    return this.http.patch<RespuestaDTORespuesta>(`${this.url}/${uuidRespuesta}/archivo`, null);
  }
}
