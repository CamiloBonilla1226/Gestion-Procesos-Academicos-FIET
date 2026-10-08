import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../enviroments/environment';
import { ObservacionDTOPeticion } from '../models/CancelacionMatricula/DTORequest/ObservacionDTOPeticion';
import { AprobacionCancelacionAsignaturaDTOPeticion } from '../models/CancelacionAsignatura/DTORequest/AprobacionCancelacionAsignaturaDTOPeticion';
import { RadicacionCancelacionAsignaturaDTOPeticion } from '../models/CancelacionAsignatura/DTORequest/RadicacionCancelacionAsignaturaDTOPeticion';
import { RemisionCancelacionAsignaturaDTOPeticion } from '../models/CancelacionAsignatura/DTORequest/RemisionCancelacionAsignaturaDTOPeticion';
import { CancelacionAsignaturaDetalleDTORespuesta } from '../models/CancelacionAsignatura/DTOResponse/CancelacionAsignaturaDetalleDTORespuesta';
import { FormularioCancelacionAsignaturaDTORespuesta } from '../models/CancelacionAsignatura/DTOResponse/FormularioCancelacionAsignaturaDTORespuesta';
import { RadicacionCancelacionAsignaturaDTORespuesta } from '../models/CancelacionAsignatura/DTOResponse/RadicacionCancelacionAsignaturaDTORespuesta';

@Injectable({
  providedIn: 'root'
})
export class CancelacionAsignaturaService {
  private url = `${environment.apiUrl}/cancelaciones-asignatura`;

  constructor(private http: HttpClient) {}

  getFormulario(): Observable<FormularioCancelacionAsignaturaDTORespuesta> {
    return this.http.get<FormularioCancelacionAsignaturaDTORespuesta>(`${this.url}/formulario`);
  }

  radicar(peticion: RadicacionCancelacionAsignaturaDTOPeticion): Observable<RadicacionCancelacionAsignaturaDTORespuesta> {
    return this.http.post<RadicacionCancelacionAsignaturaDTORespuesta>(`${this.url}`, this.armarFormDataRadicacion(peticion));
  }

  getDetalle(uuidSolicitud: string): Observable<CancelacionAsignaturaDetalleDTORespuesta> {
    return this.http.get<CancelacionAsignaturaDetalleDTORespuesta>(`${this.url}/${uuidSolicitud}`);
  }

  rechazarPorFuncionario(
    uuidSolicitud: string,
    peticion: ObservacionDTOPeticion
  ): Observable<CancelacionAsignaturaDetalleDTORespuesta> {
    return this.http.post<CancelacionAsignaturaDetalleDTORespuesta>(`${this.url}/${uuidSolicitud}/funcionario/rechazar`, peticion);
  }

  remitirADecano(
    uuidSolicitud: string,
    peticion: RemisionCancelacionAsignaturaDTOPeticion
  ): Observable<CancelacionAsignaturaDetalleDTORespuesta> {
    return this.http.post<CancelacionAsignaturaDetalleDTORespuesta>(`${this.url}/${uuidSolicitud}/funcionario/remitir`, peticion);
  }

  enviarRespuesta(uuidSolicitud: string): Observable<CancelacionAsignaturaDetalleDTORespuesta> {
    return this.http.post<CancelacionAsignaturaDetalleDTORespuesta>(`${this.url}/${uuidSolicitud}/funcionario/responder`, null);
  }

  aprobarPorDecano(
    uuidSolicitud: string,
    peticion: AprobacionCancelacionAsignaturaDTOPeticion
  ): Observable<CancelacionAsignaturaDetalleDTORespuesta> {
    return this.http.post<CancelacionAsignaturaDetalleDTORespuesta>(`${this.url}/${uuidSolicitud}/decano/aprobar`, peticion);
  }

  rechazarPorDecano(
    uuidSolicitud: string,
    peticion: ObservacionDTOPeticion
  ): Observable<CancelacionAsignaturaDetalleDTORespuesta> {
    return this.http.post<CancelacionAsignaturaDetalleDTORespuesta>(`${this.url}/${uuidSolicitud}/decano/rechazar`, peticion);
  }

  private armarFormDataRadicacion(peticion: RadicacionCancelacionAsignaturaDTOPeticion): FormData {
    const formData = new FormData();
    formData.append('motivo', peticion.motivo);
    for (const uuidAsignaturaMatriculada of peticion.asignaturas) {
      formData.append('asignaturas', uuidAsignaturaMatriculada);
    }
    for (const soporte of peticion.soportes) {
      formData.append('soporte', soporte, soporte.name);
    }
    return formData;
  }
}
