import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../enviroments/environment';
import { AprobacionDecanoDTOPeticion } from '../models/CancelacionMatricula/DTORequest/AprobacionDecanoDTOPeticion';
import { ObservacionDTOPeticion } from '../models/CancelacionMatricula/DTORequest/ObservacionDTOPeticion';
import { RadicacionCancelacionMatriculaDTOPeticion } from '../models/CancelacionMatricula/DTORequest/RadicacionCancelacionMatriculaDTOPeticion';
import { RemisionDecanoDTOPeticion } from '../models/CancelacionMatricula/DTORequest/RemisionDecanoDTOPeticion';
import { CancelacionMatriculaDetalleDTORespuesta } from '../models/CancelacionMatricula/DTOResponse/CancelacionMatriculaDetalleDTORespuesta';
import { FormularioCancelacionMatriculaDTORespuesta } from '../models/CancelacionMatricula/DTOResponse/FormularioCancelacionMatriculaDTORespuesta';
import { RadicacionCancelacionMatriculaDTORespuesta } from '../models/CancelacionMatricula/DTOResponse/RadicacionCancelacionMatriculaDTORespuesta';

@Injectable({
  providedIn: 'root'
})
export class CancelacionMatriculaService {
  private url = `${environment.apiUrl}/cancelaciones-matricula`;

  constructor(private http: HttpClient) {}

  getFormulario(): Observable<FormularioCancelacionMatriculaDTORespuesta> {
    return this.http.get<FormularioCancelacionMatriculaDTORespuesta>(`${this.url}/formulario`);
  }

  radicar(peticion: RadicacionCancelacionMatriculaDTOPeticion): Observable<RadicacionCancelacionMatriculaDTORespuesta> {
    return this.http.post<RadicacionCancelacionMatriculaDTORespuesta>(`${this.url}`, this.armarFormDataRadicacion(peticion));
  }

  getDetalle(uuidSolicitud: string): Observable<CancelacionMatriculaDetalleDTORespuesta> {
    return this.http.get<CancelacionMatriculaDetalleDTORespuesta>(`${this.url}/${uuidSolicitud}`);
  }

  rechazarPorFuncionario(
    uuidSolicitud: string,
    peticion: ObservacionDTOPeticion
  ): Observable<CancelacionMatriculaDetalleDTORespuesta> {
    return this.http.post<CancelacionMatriculaDetalleDTORespuesta>(`${this.url}/${uuidSolicitud}/funcionario/rechazar`, peticion);
  }

  remitirADecano(
    uuidSolicitud: string,
    peticion: RemisionDecanoDTOPeticion
  ): Observable<CancelacionMatriculaDetalleDTORespuesta> {
    return this.http.post<CancelacionMatriculaDetalleDTORespuesta>(`${this.url}/${uuidSolicitud}/funcionario/remitir`, peticion);
  }

  enviarRespuesta(uuidSolicitud: string): Observable<CancelacionMatriculaDetalleDTORespuesta> {
    return this.http.post<CancelacionMatriculaDetalleDTORespuesta>(`${this.url}/${uuidSolicitud}/funcionario/responder`, null);
  }

  aprobarPorDecano(
    uuidSolicitud: string,
    peticion: AprobacionDecanoDTOPeticion
  ): Observable<CancelacionMatriculaDetalleDTORespuesta> {
    return this.http.post<CancelacionMatriculaDetalleDTORespuesta>(`${this.url}/${uuidSolicitud}/decano/aprobar`, peticion);
  }

  rechazarPorDecano(
    uuidSolicitud: string,
    peticion: ObservacionDTOPeticion
  ): Observable<CancelacionMatriculaDetalleDTORespuesta> {
    return this.http.post<CancelacionMatriculaDetalleDTORespuesta>(`${this.url}/${uuidSolicitud}/decano/rechazar`, peticion);
  }

  private armarFormDataRadicacion(peticion: RadicacionCancelacionMatriculaDTOPeticion): FormData {
    const formData = new FormData();
    formData.append('motivo', peticion.motivo);
    for (const anexo of peticion.anexos) {
      formData.append(anexo.uuidTipoAnexoAcademico, anexo.archivo, anexo.archivo.name);
    }
    for (const soporte of peticion.soportes) {
      formData.append('soporte', soporte, soporte.name);
    }
    return formData;
  }
}
