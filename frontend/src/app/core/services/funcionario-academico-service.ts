import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../enviroments/environment';
import { PaginacionRespuestaDTO } from '../models/PaginacionRespuestaDTO';
import { FuncionarioAcademicoDTOPeticion } from '../models/FuncionarioAcademico/DTORequest/FuncionarioAcademicoDTOPeticion';
import { FuncionarioAcademicoActualizarDTOPeticion } from '../models/FuncionarioAcademico/DTORequest/FuncionarioAcademicoActualizarDTOPeticion';
import { FuncionarioAcademicoDTORespuesta } from '../models/FuncionarioAcademico/DTOResponse/FuncionarioAcademicoDTORespuesta';
import { armarParametros } from '../utils/parametros-http';

@Injectable({
  providedIn: 'root'
})
export class FuncionarioAcademicoService {
  private url = `${environment.apiUrl}/funcionarios-academicos`;

  constructor(private http: HttpClient) {}

  crearFuncionarioAcademico(peticion: FuncionarioAcademicoDTOPeticion): Observable<FuncionarioAcademicoDTORespuesta> {
    return this.http.post<FuncionarioAcademicoDTORespuesta>(`${this.url}`, peticion);
  }

  crearFuncionariosAcademicosDesdeArchivo(file: File): Observable<FuncionarioAcademicoDTORespuesta[]> {
    const formData = new FormData();
    formData.append('file', file, file.name);
    return this.http.post<FuncionarioAcademicoDTORespuesta[]>(`${this.url}/cargar/archivo`, formData);
  }

  getFuncionariosAcademicosPaginado(
    pagina: number,
    tamanio: number
  ): Observable<PaginacionRespuestaDTO<FuncionarioAcademicoDTORespuesta>> {
    return this.http.get<PaginacionRespuestaDTO<FuncionarioAcademicoDTORespuesta>>(`${this.url}/paginado`, {
      params: armarParametros({ pagina, tamanio })
    });
  }

  getFuncionariosAcademicosFiltrados(
    filtro: { nombre?: string | null; apellido?: string | null; dependencia?: string | null },
    pagina: number,
    tamanio: number
  ): Observable<PaginacionRespuestaDTO<FuncionarioAcademicoDTORespuesta>> {
    return this.http.get<PaginacionRespuestaDTO<FuncionarioAcademicoDTORespuesta>>(`${this.url}/filtro`, {
      params: armarParametros({
        nombre: filtro.nombre,
        apellido: filtro.apellido,
        dependencia: filtro.dependencia,
        pagina,
        tamanio
      })
    });
  }

  getFuncionarioAcademico(uuidFuncionario: string): Observable<FuncionarioAcademicoDTORespuesta> {
    return this.http.get<FuncionarioAcademicoDTORespuesta>(`${this.url}/${uuidFuncionario}`);
  }

  actualizarFuncionarioAcademico(
    uuidFuncionario: string,
    peticion: FuncionarioAcademicoActualizarDTOPeticion
  ): Observable<FuncionarioAcademicoDTORespuesta> {
    return this.http.put<FuncionarioAcademicoDTORespuesta>(`${this.url}/${uuidFuncionario}`, peticion);
  }
}
