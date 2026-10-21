import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../enviroments/environment';
import { ObservacionDTOPeticion } from '../models/CancelacionMatricula/DTORequest/ObservacionDTOPeticion';
import { AprobacionCancelacionAsignaturaDTOPeticion } from '../models/CancelacionAsignatura/DTORequest/AprobacionCancelacionAsignaturaDTOPeticion';
import { RadicacionCancelacionAsignaturaDTOPeticion } from '../models/CancelacionAsignatura/DTORequest/RadicacionCancelacionAsignaturaDTOPeticion';
import { RemisionCancelacionAsignaturaDTOPeticion } from '../models/CancelacionAsignatura/DTORequest/RemisionCancelacionAsignaturaDTOPeticion';
import { RespuestaCancelacionAsignaturaDTOPeticion } from '../models/CancelacionAsignatura/DTORequest/RespuestaCancelacionAsignaturaDTOPeticion';
import { ProgramacionResolucionDTOPeticion } from '../models/CancelacionAsignatura/DTORequest/ProgramacionResolucionDTOPeticion';
import { NotificacionResolucionDTORespuesta } from '../models/CancelacionAsignatura/DTOResponse/NotificacionResolucionDTORespuesta';
import { PlantillaNotificacionResolucionDTORespuesta } from '../models/CancelacionAsignatura/DTOResponse/PlantillaNotificacionResolucionDTORespuesta';
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

  enviarRespuesta(
    uuidSolicitud: string,
    peticion?: RespuestaCancelacionAsignaturaDTOPeticion
  ): Observable<CancelacionAsignaturaDetalleDTORespuesta> {
    return this.http.post<CancelacionAsignaturaDetalleDTORespuesta>(`${this.url}/${uuidSolicitud}/funcionario/responder`, peticion ?? null);
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

  getPlantillaNotificacion(uuidSolicitud: string): Observable<PlantillaNotificacionResolucionDTORespuesta> {
    return this.http.get<PlantillaNotificacionResolucionDTORespuesta>(`${this.url}/${uuidSolicitud}/funcionario/notificacion/plantilla`);
  }

  getNotificacion(uuidSolicitud: string): Observable<NotificacionResolucionDTORespuesta | null> {
    return this.http.get<NotificacionResolucionDTORespuesta | null>(`${this.url}/${uuidSolicitud}/funcionario/notificacion`);
  }

  reprogramarNotificacion(
    uuidSolicitud: string,
    peticion: ProgramacionResolucionDTOPeticion
  ): Observable<NotificacionResolucionDTORespuesta> {
    return this.http.put<NotificacionResolucionDTORespuesta>(`${this.url}/${uuidSolicitud}/funcionario/notificacion`, peticion);
  }

  registrarFirma(uuidSolicitud: string, archivo: File): Observable<NotificacionResolucionDTORespuesta> {
    const formData = new FormData();
    formData.append('archivo', archivo, archivo.name);
    return this.http.post<NotificacionResolucionDTORespuesta>(`${this.url}/${uuidSolicitud}/funcionario/notificacion/firma`, formData);
  }

  reintentarNotificacion(uuidSolicitud: string): Observable<NotificacionResolucionDTORespuesta> {
    return this.http.post<NotificacionResolucionDTORespuesta>(`${this.url}/${uuidSolicitud}/funcionario/notificacion/reintentar`, null);
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
