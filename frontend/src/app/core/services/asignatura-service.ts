import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../enviroments/environment';
import { PaginacionRespuestaDTO } from '../models/PaginacionRespuestaDTO';
import { AsignaturaDTOPeticion } from '../models/Asignatura/DTORequest/AsignaturaDTOPeticion';
import { AsignaturaActualizarDTOPeticion } from '../models/Asignatura/DTORequest/AsignaturaActualizarDTOPeticion';
import { AsignaturaDTORespuesta } from '../models/Asignatura/DTOResponse/AsignaturaDTORespuesta';
import { armarParametros } from '../utils/parametros-http';

@Injectable({
  providedIn: 'root'
})
export class AsignaturaService {
  private url = `${environment.apiUrl}/asignaturas`;

  constructor(private http: HttpClient) {}

  crearAsignatura(peticion: AsignaturaDTOPeticion): Observable<AsignaturaDTORespuesta> {
    return this.http.post<AsignaturaDTORespuesta>(`${this.url}`, peticion);
  }

  crearAsignaturasDesdeArchivo(file: File): Observable<AsignaturaDTORespuesta[]> {
    const formData = new FormData();
    formData.append('file', file, file.name);
    return this.http.post<AsignaturaDTORespuesta[]>(`${this.url}/cargar/archivo`, formData);
  }

  getAsignaturasPaginado(pagina: number, tamanio: number): Observable<PaginacionRespuestaDTO<AsignaturaDTORespuesta>> {
    return this.http.get<PaginacionRespuestaDTO<AsignaturaDTORespuesta>>(`${this.url}/paginado`, {
      params: armarParametros({ pagina, tamanio })
    });
  }

  getAsignaturasFiltradas(
    filtro: { texto?: string | null; uuidProgramaAcademico?: string | null },
    pagina: number,
    tamanio: number
  ): Observable<PaginacionRespuestaDTO<AsignaturaDTORespuesta>> {
    return this.http.get<PaginacionRespuestaDTO<AsignaturaDTORespuesta>>(`${this.url}/filtro`, {
      params: armarParametros({ texto: filtro.texto, uuidProgramaAcademico: filtro.uuidProgramaAcademico, pagina, tamanio })
    });
  }

  getAsignaturasPorPrograma(uuidPrograma: string): Observable<AsignaturaDTORespuesta[]> {
    return this.http.get<AsignaturaDTORespuesta[]>(`${this.url}/programa/${uuidPrograma}`);
  }

  getAsignatura(uuidAsignatura: string): Observable<AsignaturaDTORespuesta> {
    return this.http.get<AsignaturaDTORespuesta>(`${this.url}/${uuidAsignatura}`);
  }

  actualizarAsignatura(uuidAsignatura: string, peticion: AsignaturaActualizarDTOPeticion): Observable<AsignaturaDTORespuesta> {
    return this.http.put<AsignaturaDTORespuesta>(`${this.url}/${uuidAsignatura}`, peticion);
  }
}
