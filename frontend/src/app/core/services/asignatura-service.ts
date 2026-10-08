import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../enviroments/environment';
import { PaginacionRespuestaDTO } from '../models/PaginacionRespuestaDTO';
import { AsignaturaDTOPeticion } from '../models/Asignatura/DTORequest/AsignaturaDTOPeticion';
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

  getAsignaturasPaginado(pagina: number, tamanio: number): Observable<PaginacionRespuestaDTO<AsignaturaDTORespuesta>> {
    return this.http.get<PaginacionRespuestaDTO<AsignaturaDTORespuesta>>(`${this.url}/paginado`, {
      params: armarParametros({ pagina, tamanio })
    });
  }

  getAsignaturasFiltradas(
    texto: string | null,
    pagina: number,
    tamanio: number
  ): Observable<PaginacionRespuestaDTO<AsignaturaDTORespuesta>> {
    return this.http.get<PaginacionRespuestaDTO<AsignaturaDTORespuesta>>(`${this.url}/filtro`, {
      params: armarParametros({ texto, pagina, tamanio })
    });
  }

  getAsignatura(uuidAsignatura: string): Observable<AsignaturaDTORespuesta> {
    return this.http.get<AsignaturaDTORespuesta>(`${this.url}/${uuidAsignatura}`);
  }

  actualizarAsignatura(uuidAsignatura: string, peticion: AsignaturaDTOPeticion): Observable<AsignaturaDTORespuesta> {
    return this.http.put<AsignaturaDTORespuesta>(`${this.url}/${uuidAsignatura}`, peticion);
  }
}
