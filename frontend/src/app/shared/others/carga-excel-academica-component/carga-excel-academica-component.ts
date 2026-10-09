import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Observable, finalize } from 'rxjs';
import { GenericDialogFormComponent } from '../../generic-dialog-form-component/generic-dialog-form-component';
import { InputAnexoUploadComponent } from '../../inputs/input-anexo-upload-component/input-anexo-upload-component';
import { ErrorHandlerService } from '../../../core/services/error-handler-service';
import { ToastService } from '../../../core/services/toast-service';
import { TAMANIO_MAXIMO_PETICION_BYTES } from '../../../core/constantes/procesos-academicos';
import { comoTipoAnexo, extensionDe } from '../../../core/utils/validaciones-academicas';
import { mensajesDeError } from '../../../core/utils/usuarios-academicos';

@Component({
  selector: 'app-carga-excel-academica-component',
  imports: [GenericDialogFormComponent, InputAnexoUploadComponent],
  templateUrl: './carga-excel-academica-component.html'
})
export class CargaExcelAcademicaComponent {
  @Input() visible = false;
  @Input({ required: true }) titulo!: string;
  @Input({ required: true }) encabezados!: string[];
  @Input() regla = '';
  @Input() nombrePlural = 'registros';
  @Input({ required: true }) cargar!: (archivo: File) => Observable<unknown[]>;
  @Output() visibleChange = new EventEmitter<boolean>();
  @Output() cargado = new EventEmitter<number>();

  readonly tipoArchivo = comoTipoAnexo('excel', 'Archivo Excel (.xlsx)', 'xlsx', true);

  archivo: File | null = null;
  enviando = false;
  error = '';
  erroresCarga: string[] = [];
  resultado = '';

  constructor(
    private errorHandlerService: ErrorHandlerService,
    private toastService: ToastService
  ) {}

  alSeleccionar(archivo: File, entrada: InputAnexoUploadComponent): void {
    this.resultado = '';
    this.erroresCarga = [];
    if (extensionDe(archivo.name) !== 'xlsx') {
      this.archivo = null;
      this.error = 'El archivo debe ser un Excel .xlsx.';
      entrada.reset();
      return;
    }
    if (archivo.size === 0 || archivo.size > TAMANIO_MAXIMO_PETICION_BYTES) {
      this.archivo = null;
      this.error = 'El archivo está vacío o supera los 20 MB.';
      entrada.reset();
      return;
    }
    this.error = '';
    this.archivo = archivo;
  }

  alQuitar(): void {
    this.archivo = null;
  }

  cerrar(visible: boolean): void {
    if (visible || this.enviando) return;
    this.archivo = null;
    this.error = '';
    this.erroresCarga = [];
    this.resultado = '';
    this.visibleChange.emit(false);
  }

  confirmar(): void {
    if (this.enviando) return;
    if (!this.archivo) {
      this.error = 'Elige el archivo Excel que quieres cargar.';
      return;
    }
    this.enviando = true;
    this.error = '';
    this.erroresCarga = [];
    this.resultado = '';
    this.cargar(this.archivo)
      .pipe(finalize(() => (this.enviando = false)))
      .subscribe({
        next: creados => {
          const cantidad = creados?.length ?? 0;
          this.resultado = `Se crearon ${cantidad} ${this.nombrePlural}.`;
          this.archivo = null;
          this.toastService.showSuccess('Carga terminada', this.resultado);
          this.cargado.emit(cantidad);
        },
        error: err => {
          this.erroresCarga = mensajesDeError(err);
          this.errorHandlerService.handleError(err, 'Error', 'No se pudo cargar el archivo');
        }
      });
  }
}
