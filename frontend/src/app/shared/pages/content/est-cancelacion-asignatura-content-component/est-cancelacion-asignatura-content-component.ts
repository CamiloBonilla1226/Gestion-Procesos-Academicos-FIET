import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { InputTextTareaComponent } from '../../../inputs/input-text-tarea-component/input-text-tarea-component';
import { InputAnexoUploadComponent } from '../../../inputs/input-anexo-upload-component/input-anexo-upload-component';
import { SimpleButtonComponent } from '../../../buttons/simple-button-component/simple-button-component';
import { CancelacionAsignaturaService } from '../../../../core/services/cancelacion-asignatura-service';
import { ErrorHandlerService } from '../../../../core/services/error-handler-service';
import { ToastService } from '../../../../core/services/toast-service';
import { RadicacionCancelacionAsignaturaDTORespuesta } from '../../../../core/models/CancelacionAsignatura/DTOResponse/RadicacionCancelacionAsignaturaDTORespuesta';
import { FormularioCancelacionAsignaturaDTORespuesta } from '../../../../core/models/CancelacionAsignatura/DTOResponse/FormularioCancelacionAsignaturaDTORespuesta';
import {
  FORMATOS_SOPORTE_LIBRE,
  MAXIMO_CARACTERES_MOTIVO,
  RUTAS_SOLICITUDES_POR_ROL,
  ROLES_ETIQUETA,
  TAMANIO_MAXIMO_ARCHIVO_BYTES,
  TAMANIO_MAXIMO_PETICION_BYTES
} from '../../../../core/constantes/procesos-academicos';
import { comoTipoAnexo, errorDeArchivo, errorDeTexto, erroresDeCampos } from '../../../../core/utils/validaciones-academicas';
import { formatearTamanio } from '../../../../core/utils/formato';

@Component({
  selector: 'app-est-cancelacion-asignatura-content-component',
  imports: [InputTextTareaComponent, InputAnexoUploadComponent, SimpleButtonComponent],
  templateUrl: './est-cancelacion-asignatura-content-component.html'
})
export class EstCancelacionAsignaturaContentComponent implements OnInit {
  readonly maximoMotivo = MAXIMO_CARACTERES_MOTIVO;
  readonly formatearTamanio = formatearTamanio;

  formulario: FormularioCancelacionAsignaturaDTORespuesta | null = null;
  cargando = false;
  errorCarga = false;

  motivo = '';
  seleccionadas = new Set<string>();
  soportes: File[] = [];
  formatosSoporte = FORMATOS_SOPORTE_LIBRE;
  maximoSoporteBytes = TAMANIO_MAXIMO_ARCHIVO_BYTES;
  tipoSoporte = comoTipoAnexo('soporte', 'Soporte del motivo (opcional)', FORMATOS_SOPORTE_LIBRE, false);

  errores: Record<string, string> = {};
  erroresServidor: Record<string, string> = {};
  intentoEnvio = false;
  enviando = false;
  radicacion: RadicacionCancelacionAsignaturaDTORespuesta | null = null;

  constructor(
    private cancelacionAsignaturaService: CancelacionAsignaturaService,
    private errorHandlerService: ErrorHandlerService,
    private toastService: ToastService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.cargarFormulario();
  }

  get puedeRadicar(): boolean {
    return !!this.formulario && this.formulario.asignaturas.length > 0;
  }

  get erroresServidorGenerales(): string[] {
    const conocidos = new Set(['motivo', 'asignaturas', 'soporte']);
    return Object.entries(this.erroresServidor)
      .filter(([campo]) => !conocidos.has(campo))
      .map(([campo, mensaje]) => `${campo}: ${mensaje}`);
  }

  cargarFormulario(): void {
    this.cargando = true;
    this.errorCarga = false;
    this.cancelacionAsignaturaService
      .getFormulario()
      .pipe(finalize(() => (this.cargando = false)))
      .subscribe({
        next: formulario => {
          this.formulario = formulario;
          const soporte = formulario.soportes?.[0];
          if (soporte) {
            this.formatosSoporte = soporte.formatosPermitidos || FORMATOS_SOPORTE_LIBRE;
            this.maximoSoporteBytes = soporte.tamanioMaximoBytes || TAMANIO_MAXIMO_ARCHIVO_BYTES;
            this.tipoSoporte = comoTipoAnexo('soporte', `${soporte.nombre} (opcional)`, this.formatosSoporte, false);
          }
        },
        error: err => {
          this.errorCarga = true;
          this.errorHandlerService.handleError(err, 'Error', 'No se pudo cargar el formulario');
        }
      });
  }

  estaSeleccionada(uuid: string): boolean {
    return this.seleccionadas.has(uuid);
  }

  alCambiarSeleccion(uuid: string, evento: Event): void {
    const marcada = (evento.target as HTMLInputElement).checked;
    const nuevas = new Set(this.seleccionadas);
    if (marcada) nuevas.add(uuid);
    else nuevas.delete(uuid);
    this.seleccionadas = nuevas;
    this.revalidar();
  }

  alCambiarMotivo(valor: string): void {
    this.motivo = valor ?? '';
    this.revalidar();
  }

  alAgregarSoporte(archivo: File, entrada: InputAnexoUploadComponent): void {
    entrada.reset();
    const error = errorDeArchivo(archivo, this.formatosSoporte, this.maximoSoporteBytes);
    if (error) {
      this.errores['soporte'] = error;
      return;
    }
    this.soportes = [...this.soportes, archivo];
    delete this.errores['soporte'];
    this.revalidar();
  }

  quitarSoporte(indice: number): void {
    this.soportes = this.soportes.filter((_, i) => i !== indice);
    this.revalidar();
  }

  radicar(): void {
    if (this.enviando || !this.puedeRadicar) return;
    this.intentoEnvio = true;
    this.errores = this.validar();
    if (Object.keys(this.errores).length > 0) {
      this.toastService.showError('Revisa el formulario', 'Hay campos pendientes o con errores.');
      return;
    }
    this.enviando = true;
    this.erroresServidor = {};
    const asignaturas = this.formulario!.asignaturas
      .map(a => a.uuidAsignaturaMatriculada)
      .filter(uuid => this.seleccionadas.has(uuid));
    this.cancelacionAsignaturaService
      .radicar({ motivo: this.motivo.trim(), asignaturas, soportes: this.soportes })
      .pipe(finalize(() => (this.enviando = false)))
      .subscribe({
        next: respuesta => {
          this.radicacion = respuesta;
          this.toastService.showSuccess('Solicitud radicada', `Radicado ${respuesta.radicado}`);
          this.irAlDetalle();
        },
        error: err => {
          this.erroresServidor = erroresDeCampos(err);
          this.errorHandlerService.handleError(err, 'Error', 'No se pudo radicar la solicitud');
        }
      });
  }

  irAlDetalle(): void {
    if (!this.radicacion) return;
    this.router.navigate([RUTAS_SOLICITUDES_POR_ROL[ROLES_ETIQUETA.ESTUDIANTE], this.radicacion.uuidSolicitudAcademica]);
  }

  private revalidar(): void {
    if (this.intentoEnvio) this.errores = this.validar();
  }

  private validar(): Record<string, string> {
    const errores: Record<string, string> = {};
    const errorMotivo = errorDeTexto(this.motivo, MAXIMO_CARACTERES_MOTIVO, true, 'El motivo de la cancelación');
    if (errorMotivo) errores['motivo'] = errorMotivo;
    if (this.seleccionadas.size === 0) errores['asignaturas'] = 'Elige al menos una asignatura para cancelar.';
    let total = 0;
    for (const soporte of this.soportes) {
      total += soporte.size;
      const error = errorDeArchivo(soporte, this.formatosSoporte, this.maximoSoporteBytes);
      if (error) errores['soporte'] = error;
    }
    if (total > TAMANIO_MAXIMO_PETICION_BYTES) {
      errores['total'] = 'Entre todos los archivos se superan los 20 MB que admite el envío.';
    }
    return errores;
  }
}
