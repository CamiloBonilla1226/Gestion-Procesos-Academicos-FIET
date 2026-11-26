import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Observable, concatMap, finalize, from, last, map, of, switchMap, tap } from 'rxjs';
import { InputAnexoUploadComponent } from '../../inputs/input-anexo-upload-component/input-anexo-upload-component';
import { SimpleButtonComponent } from '../../buttons/simple-button-component/simple-button-component';
import { GenericDialogFormComponent } from '../../generic-dialog-form-component/generic-dialog-form-component';
import { SolicitudAcademicaService } from '../../../core/services/solicitud-academica-service';
import { ErrorHandlerService } from '../../../core/services/error-handler-service';
import { ToastService } from '../../../core/services/toast-service';
import {
  FORMATOS_RESOLUCION,
  TAMANIO_MAXIMO_ARCHIVO_BYTES,
  TAMANIO_MAXIMO_PAZ_Y_SALVOS_BYTES
} from '../../../core/constantes/procesos-academicos';
import { comoTipoAnexo, errorDeArchivo } from '../../../core/utils/validaciones-academicas';
import { guardarArchivo } from '../../../core/utils/descargas';
import { formatearTamanio } from '../../../core/utils/formato';

@Component({
  selector: 'app-formato-estudiante-component',
  imports: [InputAnexoUploadComponent, SimpleButtonComponent, GenericDialogFormComponent],
  templateUrl: './formato-estudiante-component.html',
  styleUrl: '../../pages/content/estilos-academicos.css'
})
export class FormatoEstudianteComponent implements OnInit {
  @Input() uuidSolicitud = '';
  @Input() radicado = '';
  @Input() supletorio = false;
  @Input() descargarAlIniciar = false;
  @Input() soportes = false;
  @Input() formatosSoporte = 'pdf';
  @Input() maximoSoporteBytes = TAMANIO_MAXIMO_ARCHIVO_BYTES;
  @Input() nombreSoporte = 'Soporte del motivo de la cancelación';
  @Output() radicada = new EventEmitter<void>();
  @Output() descartada = new EventEmitter<void>();

  readonly formatearTamanio = formatearTamanio;
  readonly tipo = comoTipoAnexo('formato-firmado', 'Formato firmado y escaneado (PDF)', FORMATOS_RESOLUCION, true);
  archivo: File | null = null;
  soportesElegidos: File[] = [];
  error = '';
  errorSoporte = '';
  descargando = false;
  subiendo = false;
  descartando = false;
  confirmarDescarte = false;

  constructor(
    private solicitudAcademicaService: SolicitudAcademicaService,
    private errorHandlerService: ErrorHandlerService,
    private toastService: ToastService
  ) {}

  get tipoSoporte() {
    return comoTipoAnexo('soporte-motivo', `${this.nombreSoporte} (opcional, ${this.formatosSoporte.toUpperCase()})`, this.formatosSoporte, false);
  }

  ngOnInit(): void {
    if (this.descargarAlIniciar && this.uuidSolicitud) this.descargar();
  }

  descargar(): void {
    if (this.descargando) return;
    this.descargando = true;
    this.solicitudAcademicaService
      .descargarFormato(this.uuidSolicitud)
      .pipe(finalize(() => (this.descargando = false)))
      .subscribe({
        next: blob => guardarArchivo(blob, `Formato-${this.radicado}-sin-firmar.pdf`),
        error: err => this.errorHandlerService.handleError(err, 'Error', 'No se pudo descargar el formato')
      });
  }

  alSeleccionar(archivo: File, entrada: InputAnexoUploadComponent): void {
    const error = errorDeArchivo(archivo, FORMATOS_RESOLUCION, TAMANIO_MAXIMO_PAZ_Y_SALVOS_BYTES);
    if (error) {
      this.error = error;
      this.archivo = null;
      entrada.reset();
      return;
    }
    this.error = '';
    this.archivo = archivo;
  }

  alQuitar(): void {
    this.archivo = null;
    this.error = '';
  }

  alAgregarSoporte(archivo: File, entrada: InputAnexoUploadComponent): void {
    entrada.reset();
    const error = errorDeArchivo(archivo, this.formatosSoporte, this.maximoSoporteBytes);
    if (error) {
      this.errorSoporte = error;
      return;
    }
    this.errorSoporte = '';
    this.soportesElegidos = [...this.soportesElegidos, archivo];
  }

  quitarSoporte(indice: number): void {
    this.soportesElegidos = this.soportesElegidos.filter((_, i) => i !== indice);
  }

  subir(): void {
    if (this.subiendo) return;
    const archivo = this.archivo;
    if (!archivo) {
      this.error = 'Se debe seleccionar el formato firmado y escaneado en PDF.';
      return;
    }
    this.subiendo = true;
    this.subirSoportes()
      .pipe(
        switchMap(() => this.solicitudAcademicaService.radicarFormato(this.uuidSolicitud, archivo)),
        finalize(() => (this.subiendo = false))
      )
      .subscribe({
        next: () => {
          this.toastService.showSuccess('Solicitud radicada', `Radicado ${this.radicado}`);
          this.radicada.emit();
        },
        error: err => this.errorHandlerService.handleError(err, 'Error', 'No se pudo radicar la solicitud')
      });
  }

  descartar(): void {
    if (this.descartando) return;
    this.descartando = true;
    this.solicitudAcademicaService
      .descartarSolicitud(this.uuidSolicitud)
      .pipe(finalize(() => (this.descartando = false)))
      .subscribe({
        next: () => {
          this.confirmarDescarte = false;
          this.toastService.showSuccess('Solicitud descartada', `Se descartó la solicitud ${this.radicado}`);
          this.descartada.emit();
        },
        error: err => this.errorHandlerService.handleError(err, 'Error', 'No se pudo descartar la solicitud')
      });
  }

  private subirSoportes(): Observable<void> {
    if (!this.soportes || this.soportesElegidos.length === 0) return of(undefined);
    return from([...this.soportesElegidos]).pipe(
      concatMap(soporte =>
        this.solicitudAcademicaService
          .adjuntarAnexo(this.uuidSolicitud, { archivo: soporte, tipoAnexo: null })
          .pipe(tap(() => (this.soportesElegidos = this.soportesElegidos.filter(elegido => elegido !== soporte))))
      ),
      last(),
      map(() => undefined)
    );
  }
}
