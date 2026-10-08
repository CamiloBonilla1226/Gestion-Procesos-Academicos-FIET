import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../enviroments/environment';
import { PaginacionRespuestaDTO } from '../models/PaginacionRespuestaDTO';
import { EstudianteDTOPeticion } from '../models/Estudiante/DTORequest/EstudianteDTOPeticion';
import { EstudianteActualizarDTOPeticion } from '../models/Estudiante/DTORequest/EstudianteActualizarDTOPeticion';
import { AsignaturaMatriculadaDTOPeticion } from '../models/Estudiante/DTORequest/AsignaturaMatriculadaDTOPeticion';
import { CambioEstadoAsignaturaDTOPeticion } from '../models/Estudiante/DTORequest/CambioEstadoAsignaturaDTOPeticion';
import { EstudianteDTORespuesta } from '../models/Estudiante/DTOResponse/EstudianteDTORespuesta';
import { AsignaturaMatriculadaDTORespuesta } from '../models/Estudiante/DTOResponse/AsignaturaMatriculadaDTORespuesta';
import { armarParametros } from '../utils/parametros-http';

@Injectable({
  providedIn: 'root'
})
export class EstudianteService {
  private url = `${environment.apiUrl}/estudiantes`;

  constructor(private http: HttpClient) {}

  crearEstudiante(peticion: EstudianteDTOPeticion): Observable<EstudianteDTORespuesta> {
    return this.http.post<EstudianteDTORespuesta>(`${this.url}`, peticion);
  }

  crearEstudiantesDesdeArchivo(file: File): Observable<EstudianteDTORespuesta[]> {
    const formData = new FormData();
    formData.append('file', file, file.name);
    return this.http.post<EstudianteDTORespuesta[]>(`${this.url}/cargar/archivo`, formData);
  }

  getEstudiantesPaginado(pagina: number, tamanio: number): Observable<PaginacionRespuestaDTO<EstudianteDTORespuesta>> {
    return this.http.get<PaginacionRespuestaDTO<EstudianteDTORespuesta>>(`${this.url}/paginado`, {
      params: armarParametros({ pagina, tamanio })
    });
  }

  getEstudiantesFiltrados(
    filtro: { nombre?: string | null; apellido?: string | null; codigo?: string | null },
    pagina: number,
    tamanio: number
  ): Observable<PaginacionRespuestaDTO<EstudianteDTORespuesta>> {
    return this.http.get<PaginacionRespuestaDTO<EstudianteDTORespuesta>>(`${this.url}/filtro`, {
      params: armarParametros({ nombre: filtro.nombre, apellido: filtro.apellido, codigo: filtro.codigo, pagina, tamanio })
    });
  }

  getMisAsignaturas(): Observable<AsignaturaMatriculadaDTORespuesta[]> {
    return this.http.get<AsignaturaMatriculadaDTORespuesta[]>(`${this.url}/mis-asignaturas`);
  }

  getEstudiante(uuidEstudiante: string): Observable<EstudianteDTORespuesta> {
    return this.http.get<EstudianteDTORespuesta>(`${this.url}/${uuidEstudiante}`);
  }

  actualizarEstudiante(uuidEstudiante: string, peticion: EstudianteActualizarDTOPeticion): Observable<EstudianteDTORespuesta> {
    return this.http.put<EstudianteDTORespuesta>(`${this.url}/${uuidEstudiante}`, peticion);
  }

  agregarAsignaturaMatriculada(
    uuidEstudiante: string,
    peticion: AsignaturaMatriculadaDTOPeticion
  ): Observable<AsignaturaMatriculadaDTORespuesta> {
    return this.http.post<AsignaturaMatriculadaDTORespuesta>(`${this.url}/${uuidEstudiante}/asignaturas`, peticion);
  }

  cambiarEstadoAsignatura(
    uuidEstudiante: string,
    uuidMatricula: string,
    peticion: CambioEstadoAsignaturaDTOPeticion
  ): Observable<AsignaturaMatriculadaDTORespuesta> {
    return this.http.patch<AsignaturaMatriculadaDTORespuesta>(
      `${this.url}/${uuidEstudiante}/asignaturas/${uuidMatricula}/estado`,
      peticion
    );
  }
}
