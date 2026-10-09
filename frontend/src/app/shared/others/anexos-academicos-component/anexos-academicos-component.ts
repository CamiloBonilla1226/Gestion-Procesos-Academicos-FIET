import { Component, Input } from '@angular/core';
import { finalize } from 'rxjs';
import { SimpleButtonComponent } from '../../buttons/simple-button-component/simple-button-component';
import { SolicitudAcademicaService } from '../../../core/services/solicitud-academica-service';
import { ErrorHandlerService } from '../../../core/services/error-handler-service';
import { AnexoAcademicoDTORespuesta } from '../../../core/models/SolicitudAcademica/DTOResponse/AnexoAcademicoDTORespuesta';
import { guardarArchivo } from '../../../core/utils/descargas';
import { formatearFechaHora, formatearTamanio } from '../../../core/utils/formato';

@Component({
  selector: 'app-anexos-academicos-component',
  imports: [SimpleButtonComponent],
  templateUrl: './anexos-academicos-component.html'
})
export class AnexosAcademicosComponent {
  @Input({ required: true }) uuidSolicitud!: string;

  @Input() anexos: AnexoAcademicoDTORespuesta[] = [];

  descargando = new Set<string>();

  readonly formatearFechaHora = formatearFechaHora;
  readonly formatearTamanio = formatearTamanio;

  constructor(
    private solicitudAcademicaService: SolicitudAcademicaService,
    private errorHandlerService: ErrorHandlerService
  ) {}

  tipoDe(anexo: AnexoAcademicoDTORespuesta): string {
    return anexo.tipoAnexo ?? 'Soporte';
  }

  descargar(anexo: AnexoAcademicoDTORespuesta): void {
    if (this.descargando.has(anexo.uuidAnexoAcademico)) return;
    this.descargando.add(anexo.uuidAnexoAcademico);
    this.solicitudAcademicaService
      .descargarAnexo(this.uuidSolicitud, anexo.uuidAnexoAcademico)
      .pipe(finalize(() => this.descargando.delete(anexo.uuidAnexoAcademico)))
      .subscribe({
        next: blob => guardarArchivo(blob, anexo.nombreArchivo),
        error: err => this.errorHandlerService.handleError(err, 'Error', 'No se pudo descargar el anexo')
      });
  }
}
