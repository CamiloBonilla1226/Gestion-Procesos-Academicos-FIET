import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../enviroments/environment';
import { NotificacionDocenteResumenDTORespuesta } from '../models/NotificacionDocente/DTOResponse/NotificacionDocenteResumenDTORespuesta';
import { NotificacionDocenteDetalleDTORespuesta } from '../models/NotificacionDocente/DTOResponse/NotificacionDocenteDetalleDTORespuesta';
import { parsearErrorBlob } from '../utils/descargas';

@Injectable({
  providedIn: 'root'
})
export class NotificacionDocenteService {
  private url = `${environment.apiUrl}/notificaciones-docente`;

  constructor(private http: HttpClient) {}

  getMisNotificaciones(): Observable<NotificacionDocenteResumenDTORespuesta[]> {
    return this.http.get<NotificacionDocenteResumenDTORespuesta[]>(this.url);
  }

  getNotificacion(uuidNotificacion: string): Observable<NotificacionDocenteDetalleDTORespuesta> {
    return this.http.get<NotificacionDocenteDetalleDTORespuesta>(`${this.url}/${uuidNotificacion}`);
  }

  marcarLeida(uuidNotificacion: string): Observable<NotificacionDocenteDetalleDTORespuesta> {
    return this.http.patch<NotificacionDocenteDetalleDTORespuesta>(`${this.url}/${uuidNotificacion}/leida`, null);
  }

  descargarAnexo(uuidNotificacion: string, uuidAnexo: string): Observable<Blob> {
    return this.http
      .get(`${this.url}/${uuidNotificacion}/anexos/${uuidAnexo}`, { responseType: 'blob' })
      .pipe(parsearErrorBlob());
  }
}
