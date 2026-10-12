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
  @Input() reglas: string[] = [];
  @Input() tituloValores = '';
  @Input() valores: string[] = [];
  @Input() describir: ((registro: any) => string) | null = null;
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
  creados: string[] = [];
  private cantidadCreada: number | null = null;

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
    const cantidad = this.cantidadCreada;
    this.archivo = null;
    this.error = '';
    this.erroresCarga = [];
    this.resultado = '';
    this.creados = [];
    this.cantidadCreada = null;
    this.visibleChange.emit(false);
    if (cantidad !== null) this.cargado.emit(cantidad);
  }

  confirmar(): void {
    if (this.enviando) return;
    if (this.resultado) {
      this.cerrar(false);
      return;
    }
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
          const describir = this.describir;
          this.resultado = `Se crearon ${cantidad} ${this.nombrePlural}.`;
          this.creados = describir ? (creados ?? []).map(registro => describir(registro)) : [];
          this.cantidadCreada = cantidad;
          this.archivo = null;
          this.error = '';
          this.toastService.showSuccess('Carga terminada', this.resultado);
        },
        error: err => {
          this.erroresCarga = mensajesDeError(err);
          this.errorHandlerService.handleError(err, 'Error', 'No se pudo cargar el archivo');
        }
      });
  }
}
