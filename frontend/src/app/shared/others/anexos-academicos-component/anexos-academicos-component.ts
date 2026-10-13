import { Component, Input } from '@angular/core';
import { finalize } from 'rxjs';
import { SolicitudAcademicaService } from '../../../core/services/solicitud-academica-service';
import { ErrorHandlerService } from '../../../core/services/error-handler-service';
import { AnexoAcademicoDTORespuesta } from '../../../core/models/SolicitudAcademica/DTOResponse/AnexoAcademicoDTORespuesta';
import { guardarArchivo } from '../../../core/utils/descargas';
import { formatearFechaHora, formatearTamanio } from '../../../core/utils/formato';

@Component({
  selector: 'app-anexos-academicos-component',
  templateUrl: './anexos-academicos-component.html',
  styleUrl: './anexos-academicos-component.css'
})
export class AnexosAcademicosComponent {
  @Input({ required: true }) uuidSolicitud!: string;

  @Input() anexos: AnexoAcademicoDTORespuesta[] = [];

  descargando = new Set<string>();
  abriendo = new Set<string>();

  readonly formatearFechaHora = formatearFechaHora;
  readonly formatearTamanio = formatearTamanio;

  constructor(
    private solicitudAcademicaService: SolicitudAcademicaService,
    private errorHandlerService: ErrorHandlerService
  ) {}

  tipoDe(anexo: AnexoAcademicoDTORespuesta): string {
    return anexo.tipoAnexo ?? 'Soporte';
  }

  ocupado(anexo: AnexoAcademicoDTORespuesta): boolean {
    return this.descargando.has(anexo.uuidAnexoAcademico) || this.abriendo.has(anexo.uuidAnexoAcademico);
  }

  descargar(anexo: AnexoAcademicoDTORespuesta): void {
    if (this.ocupado(anexo)) return;
    this.descargando.add(anexo.uuidAnexoAcademico);
    this.solicitudAcademicaService
      .descargarAnexo(this.uuidSolicitud, anexo.uuidAnexoAcademico)
      .pipe(finalize(() => this.descargando.delete(anexo.uuidAnexoAcademico)))
      .subscribe({
        next: blob => guardarArchivo(blob, anexo.nombreArchivo),
        error: err => this.errorHandlerService.handleError(err, 'Error', 'No se pudo descargar el anexo')
      });
  }

  visualizar(anexo: AnexoAcademicoDTORespuesta): void {
    if (this.ocupado(anexo) || typeof window === 'undefined') return;
    const ventana = window.open('', '_blank');
    this.abriendo.add(anexo.uuidAnexoAcademico);
    this.solicitudAcademicaService
      .descargarAnexo(this.uuidSolicitud, anexo.uuidAnexoAcademico)
      .pipe(finalize(() => this.abriendo.delete(anexo.uuidAnexoAcademico)))
      .subscribe({
        next: blob => this.mostrar(blob, anexo, ventana),
        error: err => {
          ventana?.close();
          this.errorHandlerService.handleError(err, 'Error', 'No se pudo abrir el anexo');
        }
      });
  }

  private mostrar(blob: Blob, anexo: AnexoAcademicoDTORespuesta, ventana: Window | null): void {
    if (!ventana) {
      guardarArchivo(blob, anexo.nombreArchivo);
      return;
    }
    const tipo = anexo.tipoArchivo || blob.type;
    const url = URL.createObjectURL(tipo && tipo !== blob.type ? new Blob([blob], { type: tipo }) : blob);
    ventana.location.href = url;
    setTimeout(() => URL.revokeObjectURL(url), 60000);
  }
}
