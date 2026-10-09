import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { InputTextTareaComponent } from '../../../inputs/input-text-tarea-component/input-text-tarea-component';
import { InputAnexoUploadComponent } from '../../../inputs/input-anexo-upload-component/input-anexo-upload-component';
import { SimpleButtonComponent } from '../../../buttons/simple-button-component/simple-button-component';
import { CancelacionMatriculaService } from '../../../../core/services/cancelacion-matricula-service';
import { ErrorHandlerService } from '../../../../core/services/error-handler-service';
import { ToastService } from '../../../../core/services/toast-service';
import { FormularioCancelacionMatriculaDTORespuesta } from '../../../../core/models/CancelacionMatricula/DTOResponse/FormularioCancelacionMatriculaDTORespuesta';
import { RadicacionCancelacionMatriculaDTORespuesta } from '../../../../core/models/CancelacionMatricula/DTOResponse/RadicacionCancelacionMatriculaDTORespuesta';
import { AnexoRequeridoDTORespuesta } from '../../../../core/models/CancelacionMatricula/DTOResponse/AnexoRequeridoDTORespuesta';
import { TipoAnexoDTORespuesta } from '../../../../core/models/TipoSolicitud/DTOResponse/TipoAnexoDTORespuesta';
import {
  FORMATOS_SOPORTE_LIBRE,
  MAXIMO_CARACTERES_MOTIVO,
  RUTAS_SOLICITUDES_POR_ROL,
  ROLES_ETIQUETA,
  TAMANIO_MAXIMO_PETICION_BYTES
} from '../../../../core/constantes/procesos-academicos';
import { comoTipoAnexo, errorDeArchivo, errorDeTexto, erroresDeCampos } from '../../../../core/utils/validaciones-academicas';
import { formatearTamanio } from '../../../../core/utils/formato';

interface CampoAnexo {
  requerido: AnexoRequeridoDTORespuesta;
  tipo: TipoAnexoDTORespuesta;
}

@Component({
  selector: 'app-est-cancelacion-matricula-content-component',
  imports: [InputTextTareaComponent, InputAnexoUploadComponent, SimpleButtonComponent],
  templateUrl: './est-cancelacion-matricula-content-component.html'
})
export class EstCancelacionMatriculaContentComponent implements OnInit {
  readonly maximoMotivo = MAXIMO_CARACTERES_MOTIVO;
  readonly tipoSoporte = comoTipoAnexo('soporte', 'Soporte del motivo (opcional)', FORMATOS_SOPORTE_LIBRE, false);
  readonly formatearTamanio = formatearTamanio;

  formulario: FormularioCancelacionMatriculaDTORespuesta | null = null;
  campos: CampoAnexo[] = [];
  cargando = false;
  errorCarga = false;

  motivo = '';
  archivos: Record<string, File | null> = {};
  soportes: File[] = [];
  aceptaAdvertencia = false;

  errores: Record<string, string> = {};
  erroresServidor: Record<string, string> = {};
  intentoEnvio = false;
  enviando = false;
  radicacion: RadicacionCancelacionMatriculaDTORespuesta | null = null;

  constructor(
    private cancelacionMatriculaService: CancelacionMatriculaService,
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
    const conocidos = new Set(['motivo', 'soporte', ...this.campos.map(c => c.requerido.uuidTipoAnexoAcademico)]);
    return Object.entries(this.erroresServidor)
      .filter(([campo]) => !conocidos.has(campo))
      .map(([campo, mensaje]) => `${campo}: ${mensaje}`);
  }

  cargarFormulario(): void {
    this.cargando = true;
    this.errorCarga = false;
    this.cancelacionMatriculaService
      .getFormulario()
      .pipe(finalize(() => (this.cargando = false)))
      .subscribe({
        next: formulario => {
          this.formulario = formulario;
          this.campos = formulario.anexosRequeridos.map(requerido => ({
            requerido,
            tipo: comoTipoAnexo(requerido.uuidTipoAnexoAcademico, requerido.nombre, requerido.formatosPermitidos, requerido.obligatorio)
          }));
          this.archivos = Object.fromEntries(this.campos.map(c => [c.requerido.uuidTipoAnexoAcademico, null]));
        },
        error: err => {
          this.errorCarga = true;
          this.errorHandlerService.handleError(err, 'Error', 'No se pudo cargar el formulario');
        }
      });
  }

  alCambiarMotivo(valor: string): void {
    this.motivo = valor ?? '';
    this.revalidar();
  }

  alSeleccionarAnexo(campo: CampoAnexo, archivo: File, entrada: InputAnexoUploadComponent): void {
    const uuid = campo.requerido.uuidTipoAnexoAcademico;
    const error = errorDeArchivo(archivo, campo.requerido.formatosPermitidos);
    if (error) {
      this.archivos[uuid] = null;
      this.errores[uuid] = error;
      entrada.reset();
      return;
    }
    this.archivos[uuid] = archivo;
    this.revalidar();
  }

  alQuitarAnexo(campo: CampoAnexo): void {
    this.archivos[campo.requerido.uuidTipoAnexoAcademico] = null;
    this.revalidar();
  }

  alAgregarSoporte(archivo: File, entrada: InputAnexoUploadComponent): void {
    entrada.reset();
    const error = errorDeArchivo(archivo, FORMATOS_SOPORTE_LIBRE);
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

  alCambiarAdvertencia(evento: Event): void {
    this.aceptaAdvertencia = (evento.target as HTMLInputElement).checked;
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
    this.cancelacionMatriculaService
      .radicar({
        motivo: this.motivo.trim(),
        anexos: this.campos
          .filter(c => !!this.archivos[c.requerido.uuidTipoAnexoAcademico])
          .map(c => ({ uuidTipoAnexoAcademico: c.requerido.uuidTipoAnexoAcademico, archivo: this.archivos[c.requerido.uuidTipoAnexoAcademico]! })),
        soportes: this.soportes
      })
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
    let total = 0;
    for (const campo of this.campos) {
      const uuid = campo.requerido.uuidTipoAnexoAcademico;
      const archivo = this.archivos[uuid];
      if (!archivo) {
        if (campo.requerido.obligatorio) errores[uuid] = 'Este anexo es obligatorio.';
        continue;
      }
      total += archivo.size;
      const error = errorDeArchivo(archivo, campo.requerido.formatosPermitidos);
      if (error) errores[uuid] = error;
    }
    for (const soporte of this.soportes) {
      total += soporte.size;
      const error = errorDeArchivo(soporte, FORMATOS_SOPORTE_LIBRE);
      if (error) errores['soporte'] = error;
    }
    if (total > TAMANIO_MAXIMO_PETICION_BYTES) {
      errores['total'] = 'Entre todos los archivos se superan los 20 MB que admite el envío.';
    }
    if (!this.aceptaAdvertencia) errores['advertencia'] = 'Debes confirmar que leíste la advertencia.';
    return errores;
  }
}
