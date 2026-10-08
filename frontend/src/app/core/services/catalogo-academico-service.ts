import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../enviroments/environment';
import { RolEtiqueta } from '../constantes/procesos-academicos';
import { AsignacionFuncionarioAcademicoDTOPeticion } from '../models/CatalogoAcademico/DTORequest/AsignacionFuncionarioAcademicoDTOPeticion';
import { EtapaEtiquetaRolDTORespuesta } from '../models/CatalogoAcademico/DTOResponse/EtapaEtiquetaRolDTORespuesta';
import { EtapaSolicitudAcademicaDTORespuesta } from '../models/CatalogoAcademico/DTOResponse/EtapaSolicitudAcademicaDTORespuesta';
import { SituacionAcademicaAsignaturaDTORespuesta } from '../models/CatalogoAcademico/DTOResponse/SituacionAcademicaAsignaturaDTORespuesta';
import { TipoAnexoAcademicoDTORespuesta } from '../models/CatalogoAcademico/DTOResponse/TipoAnexoAcademicoDTORespuesta';
import { TipoSolicitudAcademicaDTORespuesta } from '../models/CatalogoAcademico/DTOResponse/TipoSolicitudAcademicaDTORespuesta';
import { armarParametros } from '../utils/parametros-http';

@Injectable({
  providedIn: 'root'
})
export class CatalogoAcademicoService {
  private url = `${environment.apiUrl}/catalogos-academicos`;

  constructor(private http: HttpClient) {}

  getTiposSolicitud(): Observable<TipoSolicitudAcademicaDTORespuesta[]> {
    return this.http.get<TipoSolicitudAcademicaDTORespuesta[]>(`${this.url}/tipos-solicitud`);
  }

  getEtapasPorTipo(uuidTipo: string): Observable<EtapaSolicitudAcademicaDTORespuesta[]> {
    return this.http.get<EtapaSolicitudAcademicaDTORespuesta[]>(`${this.url}/tipos-solicitud/${uuidTipo}/etapas`);
  }

  getTiposAnexoPorTipo(uuidTipo: string): Observable<TipoAnexoAcademicoDTORespuesta[]> {
    return this.http.get<TipoAnexoAcademicoDTORespuesta[]>(`${this.url}/tipos-solicitud/${uuidTipo}/tipos-anexo`);
  }

  getEtiquetasPorRol(rol: RolEtiqueta): Observable<EtapaEtiquetaRolDTORespuesta[]> {
    return this.http.get<EtapaEtiquetaRolDTORespuesta[]>(`${this.url}/etiquetas`, {
      params: armarParametros({ rol })
    });
  }

  getSituaciones(): Observable<SituacionAcademicaAsignaturaDTORespuesta[]> {
    return this.http.get<SituacionAcademicaAsignaturaDTORespuesta[]>(`${this.url}/situaciones`);
  }

  asignarFuncionarioAcademico(
    uuidTipo: string,
    peticion: AsignacionFuncionarioAcademicoDTOPeticion
  ): Observable<TipoSolicitudAcademicaDTORespuesta> {
    return this.http.put<TipoSolicitudAcademicaDTORespuesta>(`${this.url}/tipos-solicitud/${uuidTipo}/funcionario`, peticion);
  }
}
