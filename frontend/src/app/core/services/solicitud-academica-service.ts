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
import { EliminacionRespuestasDTORespuesta } from '../models/SolicitudAcademica/DTOResponse/EliminacionRespuestasDTORespuesta';
import { CorreosPendientesDTORespuesta } from '../models/SolicitudAcademica/DTOResponse/CorreosPendientesDTORespuesta';
import { ValorProvisionalFormatoDTOPeticion } from '../models/SolicitudAcademica/DTORequest/ValorProvisionalFormatoDTOPeticion';
import { parsearErrorBlob } from '../utils/descargas';
import { armarParametros } from '../utils/parametros-http';

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

  getCorreosPendientes(): Observable<CorreosPendientesDTORespuesta> {
    return this.http.get<CorreosPendientesDTORespuesta>(`${this.url}/funcionario/correos-pendientes`);
  }

  contarRespuestasEliminables(desde: string, hasta: string): Observable<EliminacionRespuestasDTORespuesta> {
    return this.http.get<EliminacionRespuestasDTORespuesta>(`${this.url}/funcionario/respuestas/conteo`, {
      params: armarParametros({ desde, hasta })
    });
  }

  eliminarRespuestas(desde: string, hasta: string): Observable<EliminacionRespuestasDTORespuesta> {
    return this.http.delete<EliminacionRespuestasDTORespuesta>(`${this.url}/funcionario/respuestas`, {
      params: armarParametros({ desde, hasta })
    });
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

  descargarFormato(uuidSolicitud: string): Observable<Blob> {
    return this.http
      .get(`${this.url}/${uuidSolicitud}/formato`, { responseType: 'blob' })
      .pipe(parsearErrorBlob());
  }

  radicarFormato(uuidSolicitud: string, archivo: File): Observable<SolicitudAcademicaDetalleDTORespuesta> {
    const formData = new FormData();
    formData.append('archivo', archivo, archivo.name);
    return this.http.post<SolicitudAcademicaDetalleDTORespuesta>(`${this.url}/${uuidSolicitud}/estudiante/radicar`, formData);
  }

  subirFormatoDecano(uuidSolicitud: string, archivo: File): Observable<SolicitudAcademicaDetalleDTORespuesta> {
    const formData = new FormData();
    formData.append('archivo', archivo, archivo.name);
    return this.http.post<SolicitudAcademicaDetalleDTORespuesta>(`${this.url}/${uuidSolicitud}/formato/decano`, formData);
  }

  descartarSolicitud(uuidSolicitud: string): Observable<void> {
    return this.http.delete<void>(`${this.url}/${uuidSolicitud}/estudiante`);
  }

  descargarFormatoCompuesto(uuidSolicitud: string, valoresProvisionales: ValorProvisionalFormatoDTOPeticion[] = []): Observable<Blob> {
    return this.http
      .post(`${this.url}/${uuidSolicitud}/formato/compuesto`, { valoresProvisionales }, { responseType: 'blob' })
      .pipe(parsearErrorBlob());
  }

  guardarAjusteFormato(uuidSolicitud: string, ajusteXmm: number, ajusteYmm: number): Observable<SolicitudAcademicaDetalleDTORespuesta> {
    return this.http.put<SolicitudAcademicaDetalleDTORespuesta>(`${this.url}/${uuidSolicitud}/formato/ajuste`, { ajusteXmm, ajusteYmm });
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
