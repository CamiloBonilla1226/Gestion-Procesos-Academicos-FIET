import { Component, EventEmitter, Output } from '@angular/core';
import { finalize } from 'rxjs';
import { GenericDialogFormComponent } from '../../generic-dialog-form-component/generic-dialog-form-component';
import { SolicitudAcademicaService } from '../../../core/services/solicitud-academica-service';
import { ErrorHandlerService } from '../../../core/services/error-handler-service';
import { ToastService } from '../../../core/services/toast-service';
import { ahoraEnColombia } from '../../../core/utils/notificacion-resolucion';

const FORMATO_FECHA = /^(\d{4})-(\d{2})-(\d{2})$/;

export function textoFechaCorta(valor: string): string {
  const coincidencia = FORMATO_FECHA.exec(valor ?? '');
  if (!coincidencia) return valor ?? '';
  const [, anio, mes, dia] = coincidencia.map(Number);
  return new Intl.DateTimeFormat('es-CO', { day: 'numeric', month: 'long', year: 'numeric' }).format(new Date(anio, mes - 1, dia));
}

export function erroresDeRango(desde: string, hasta: string, hoy: string): Record<string, string> {
  const errores: Record<string, string> = {};
  if (!FORMATO_FECHA.test(desde)) errores['desde'] = 'Se debe seleccionar la fecha inicial.';
  else if (desde > hoy) errores['desde'] = 'La fecha inicial no puede ser posterior a hoy.';
  if (!FORMATO_FECHA.test(hasta)) errores['hasta'] = 'Se debe seleccionar la fecha final.';
  else if (hasta > hoy) errores['hasta'] = 'La fecha final no puede ser posterior a hoy.';
  if (!errores['desde'] && !errores['hasta'] && desde > hasta) errores['hasta'] = 'La fecha final debe ser igual o posterior a la inicial.';
  return errores;
}

@Component({
  selector: 'app-eliminacion-respuestas-component',
  imports: [GenericDialogFormComponent],
  templateUrl: './eliminacion-respuestas-component.html',
  styleUrl: '../../pages/content/estilos-academicos.css'
})
export class EliminacionRespuestasComponent {
  @Output() eliminadas = new EventEmitter<number>();

  readonly hoy = ahoraEnColombia().slice(0, 10);

  desde = '';
  hasta = '';
  errores: Record<string, string> = {};
  contando = false;
  enviando = false;
  dialogoVisible = false;
  cantidad = 0;
  rangoConfirmado: { desde: string; hasta: string } | null = null;

  constructor(
    private solicitudAcademicaService: SolicitudAcademicaService,
    private errorHandlerService: ErrorHandlerService,
    private toastService: ToastService
  ) {}

  get mensajeConfirmacion(): string {
    if (!this.rangoConfirmado) return '';
    const sujeto = this.cantidad === 1 ? 'Se eliminará 1 respuesta' : `Se eliminarán ${this.cantidad} respuestas`;
    return `${sujeto} entre ${textoFechaCorta(this.rangoConfirmado.desde)} y ${textoFechaCorta(this.rangoConfirmado.hasta)}. `
      + 'Esta acción no se puede deshacer. ¿Seguro?';
  }

  valorDe(evento: Event): string {
    return (evento.target as HTMLInputElement).value;
  }

  revisar(): void {
    if (this.contando || this.enviando) return;
    this.errores = erroresDeRango(this.desde, this.hasta, this.hoy);
    if (Object.keys(this.errores).length > 0) return;
    const rango = { desde: this.desde, hasta: this.hasta };
    this.contando = true;
    this.solicitudAcademicaService
      .contarRespuestasEliminables(rango.desde, rango.hasta)
      .pipe(finalize(() => (this.contando = false)))
      .subscribe({
        next: conteo => {
          if (conteo.cantidad === 0) {
            this.toastService.showInfo('Sin respuestas',
              `No hay respuestas cerradas para eliminar entre ${textoFechaCorta(rango.desde)} y ${textoFechaCorta(rango.hasta)}.`);
            return;
          }
          this.cantidad = conteo.cantidad;
          this.rangoConfirmado = rango;
          this.dialogoVisible = true;
        },
        error: err => this.errorHandlerService.handleError(err, 'Error', 'No se pudieron contar las respuestas')
      });
  }

  cerrar(visible: boolean): void {
    if (visible || this.enviando) return;
    this.dialogoVisible = false;
    this.rangoConfirmado = null;
  }

  confirmar(): void {
    if (this.enviando || !this.rangoConfirmado) return;
    const rango = this.rangoConfirmado;
    this.enviando = true;
    this.solicitudAcademicaService
      .eliminarRespuestas(rango.desde, rango.hasta)
      .pipe(finalize(() => (this.enviando = false)))
      .subscribe({
        next: resultado => {
          this.dialogoVisible = false;
          this.rangoConfirmado = null;
          const texto = resultado.cantidad === 1 ? 'Se eliminó 1 respuesta.' : `Se eliminaron ${resultado.cantidad} respuestas.`;
          this.toastService.showSuccess('Operación realizada', texto);
          this.eliminadas.emit(resultado.cantidad);
        },
        error: err => this.errorHandlerService.handleError(err, 'Error', 'No se pudieron eliminar las respuestas')
      });
  }
}
