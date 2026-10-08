import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../enviroments/environment';
import { CAUSAS_SUPLETORIO } from '../constantes/procesos-academicos';
import { ObservacionDTOPeticion } from '../models/CancelacionMatricula/DTORequest/ObservacionDTOPeticion';
import { AprobacionComprobanteDTOPeticion } from '../models/ExamenSupletorio/DTORequest/AprobacionComprobanteDTOPeticion';
import { RadicacionExamenSupletorioDTOPeticion } from '../models/ExamenSupletorio/DTORequest/RadicacionExamenSupletorioDTOPeticion';
import { RemisionSupletorioDTOPeticion } from '../models/ExamenSupletorio/DTORequest/RemisionSupletorioDTOPeticion';
import { ExamenSupletorioDetalleDTORespuesta } from '../models/ExamenSupletorio/DTOResponse/ExamenSupletorioDetalleDTORespuesta';
import { FormularioExamenSupletorioDTORespuesta } from '../models/ExamenSupletorio/DTOResponse/FormularioExamenSupletorioDTORespuesta';
import { RadicacionExamenSupletorioDTORespuesta } from '../models/ExamenSupletorio/DTOResponse/RadicacionExamenSupletorioDTORespuesta';

@Injectable({
  providedIn: 'root'
})
export class ExamenSupletorioService {
  private url = `${environment.apiUrl}/examenes-supletorios`;

  constructor(private http: HttpClient) {}

  getFormulario(): Observable<FormularioExamenSupletorioDTORespuesta> {
    return this.http.get<FormularioExamenSupletorioDTORespuesta>(`${this.url}/formulario`);
  }

  radicar(peticion: RadicacionExamenSupletorioDTOPeticion): Observable<RadicacionExamenSupletorioDTORespuesta> {
    return this.http.post<RadicacionExamenSupletorioDTORespuesta>(`${this.url}`, this.armarFormDataRadicacion(peticion));
  }

  getDetalle(uuidSolicitud: string): Observable<ExamenSupletorioDetalleDTORespuesta> {
    return this.http.get<ExamenSupletorioDetalleDTORespuesta>(`${this.url}/${uuidSolicitud}`);
  }

  rechazarPorFuncionario(uuidSolicitud: string, peticion: ObservacionDTOPeticion): Observable<ExamenSupletorioDetalleDTORespuesta> {
    return this.http.post<ExamenSupletorioDetalleDTORespuesta>(`${this.url}/${uuidSolicitud}/funcionario/rechazar`, peticion);
  }

  remitirADecano(uuidSolicitud: string, peticion: RemisionSupletorioDTOPeticion): Observable<ExamenSupletorioDetalleDTORespuesta> {
    return this.http.post<ExamenSupletorioDetalleDTORespuesta>(`${this.url}/${uuidSolicitud}/funcionario/remitir`, peticion);
  }

  enviarRespuesta(uuidSolicitud: string): Observable<ExamenSupletorioDetalleDTORespuesta> {
    return this.http.post<ExamenSupletorioDetalleDTORespuesta>(`${this.url}/${uuidSolicitud}/funcionario/responder`, null);
  }

  enviarRecibo(uuidSolicitud: string): Observable<ExamenSupletorioDetalleDTORespuesta> {
    return this.http.post<ExamenSupletorioDetalleDTORespuesta>(`${this.url}/${uuidSolicitud}/funcionario/recibo`, null);
  }

  aprobarComprobante(
    uuidSolicitud: string,
    peticion?: AprobacionComprobanteDTOPeticion
  ): Observable<ExamenSupletorioDetalleDTORespuesta> {
    return this.http.post<ExamenSupletorioDetalleDTORespuesta>(
      `${this.url}/${uuidSolicitud}/funcionario/comprobante/aprobar`,
      peticion ?? null
    );
  }

  rechazarComprobante(uuidSolicitud: string, peticion: ObservacionDTOPeticion): Observable<ExamenSupletorioDetalleDTORespuesta> {
    return this.http.post<ExamenSupletorioDetalleDTORespuesta>(
      `${this.url}/${uuidSolicitud}/funcionario/comprobante/rechazar`,
      peticion
    );
  }

  aprobarPorDecano(uuidSolicitud: string, peticion?: ObservacionDTOPeticion): Observable<ExamenSupletorioDetalleDTORespuesta> {
    return this.http.post<ExamenSupletorioDetalleDTORespuesta>(`${this.url}/${uuidSolicitud}/decano/aprobar`, peticion ?? null);
  }

  rechazarPorDecano(uuidSolicitud: string, peticion: ObservacionDTOPeticion): Observable<ExamenSupletorioDetalleDTORespuesta> {
    return this.http.post<ExamenSupletorioDetalleDTORespuesta>(`${this.url}/${uuidSolicitud}/decano/rechazar`, peticion);
  }

  subirComprobante(uuidSolicitud: string): Observable<ExamenSupletorioDetalleDTORespuesta> {
    return this.http.post<ExamenSupletorioDetalleDTORespuesta>(`${this.url}/${uuidSolicitud}/estudiante/comprobante`, null);
  }

  private armarFormDataRadicacion(peticion: RadicacionExamenSupletorioDTOPeticion): FormData {
    const formData = new FormData();
    formData.append('asignaturaMatriculada', peticion.asignaturaMatriculada);
    formData.append('fechaExamenNoPresentado', peticion.fechaExamenNoPresentado);
    formData.append('tipoCausa', peticion.tipoCausa);
    if (peticion.tipoCausa.trim().toLowerCase() === CAUSAS_SUPLETORIO.CRUCE) {
      this.agregarSiTieneValor(formData, 'asignaturaCruzada', peticion.asignaturaCruzada);
      this.agregarSiTieneValor(formData, 'fechaExamenCruzada', peticion.fechaExamenCruzada);
      this.agregarSiTieneValor(formData, 'horaExamenCruzada', peticion.horaExamenCruzada);
    }
    for (const anexo of peticion.anexos) {
      formData.append(anexo.uuidTipoAnexoAcademico, anexo.archivo, anexo.archivo.name);
    }
    return formData;
  }

  private agregarSiTieneValor(formData: FormData, nombre: string, valor: string | null | undefined): void {
    if (valor && valor.trim() !== '') {
      formData.append(nombre, valor.trim());
    }
  }
}
