import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../enviroments/environment';
import { AnexoAcademicoDTOPeticion } from '../models/SolicitudAcademica/DTORequest/AnexoAcademicoDTOPeticion';
import { AnexoAcademicoDTORespuesta } from '../models/SolicitudAcademica/DTOResponse/AnexoAcademicoDTORespuesta';
import { HistorialSolicitudAcademicaDTORespuesta } from '../models/SolicitudAcademica/DTOResponse/HistorialSolicitudAcademicaDTORespuesta';
import { ResolucionAcademicaDTORespuesta } from '../models/SolicitudAcademica/DTOResponse/ResolucionAcademicaDTORespuesta';
import { SolicitudAcademicaDetalleDTORespuesta } from '../models/SolicitudAcademica/DTOResponse/SolicitudAcademicaDetalleDTORespuesta';
import { SolicitudAcademicaResumenDTORespuesta } from '../models/SolicitudAcademica/DTOResponse/SolicitudAcademicaResumenDTORespuesta';
import { parsearErrorBlob } from '../utils/descargas';

@Injectable({
  providedIn: 'root'
})
export class SolicitudAcademicaService {
  private url = `${environment.apiUrl}/solicitudes-academicas`;

  constructor(private http: HttpClient) {}

  getSolicitudesEstudiante(): Observable<SolicitudAcademicaResumenDTORespuesta[]> {
    return this.http.get<SolicitudAcademicaResumenDTORespuesta[]>(`${this.url}/estudiante`);
  }

  getSolicitudesFuncionario(): Observable<SolicitudAcademicaResumenDTORespuesta[]> {
    return this.http.get<SolicitudAcademicaResumenDTORespuesta[]>(`${this.url}/funcionario`);
  }

  getSolicitudesDecano(): Observable<SolicitudAcademicaResumenDTORespuesta[]> {
    return this.http.get<SolicitudAcademicaResumenDTORespuesta[]>(`${this.url}/decano`);
  }

  getSolicitud(uuidSolicitud: string): Observable<SolicitudAcademicaDetalleDTORespuesta> {
    return this.http.get<SolicitudAcademicaDetalleDTORespuesta>(`${this.url}/${uuidSolicitud}`);
  }

  getHistorial(uuidSolicitud: string): Observable<HistorialSolicitudAcademicaDTORespuesta[]> {
    return this.http.get<HistorialSolicitudAcademicaDTORespuesta[]>(`${this.url}/${uuidSolicitud}/historial`);
  }

  adjuntarAnexo(uuidSolicitud: string, peticion: AnexoAcademicoDTOPeticion): Observable<AnexoAcademicoDTORespuesta> {
    return this.http.post<AnexoAcademicoDTORespuesta>(`${this.url}/${uuidSolicitud}/anexos`, this.armarFormDataAnexo(peticion));
  }

  descargarAnexo(uuidSolicitud: string, uuidAnexo: string): Observable<Blob> {
    return this.http
      .get(`${this.url}/${uuidSolicitud}/anexos/${uuidAnexo}`, { responseType: 'blob' })
      .pipe(parsearErrorBlob());
  }

  adjuntarResolucion(uuidSolicitud: string, archivo: File): Observable<ResolucionAcademicaDTORespuesta> {
    const formData = new FormData();
    formData.append('archivo', archivo, archivo.name);
    return this.http.post<ResolucionAcademicaDTORespuesta>(`${this.url}/${uuidSolicitud}/resolucion`, formData);
  }

  descargarResolucion(uuidSolicitud: string): Observable<Blob> {
    return this.http
      .get(`${this.url}/${uuidSolicitud}/resolucion`, { responseType: 'blob' })
      .pipe(parsearErrorBlob());
  }

  private armarFormDataAnexo(peticion: AnexoAcademicoDTOPeticion): FormData {
    const formData = new FormData();
    formData.append('archivo', peticion.archivo, peticion.archivo.name);
    if (peticion.tipoAnexo && peticion.tipoAnexo.trim() !== '') {
      formData.append('tipoAnexo', peticion.tipoAnexo.trim());
    }
    return formData;
  }
}
