import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { InputSelectComponent } from '../../../inputs/input-select-component/input-select-component';
import { InputDateComponent } from '../../../inputs/input-date-component/input-date-component';
import { InputTextComponent } from '../../../inputs/input-text-component/input-text-component';
import { InputAnexoUploadComponent } from '../../../inputs/input-anexo-upload-component/input-anexo-upload-component';
import { SimpleButtonComponent } from '../../../buttons/simple-button-component/simple-button-component';
import { ExamenSupletorioService } from '../../../../core/services/examen-supletorio-service';
import { ErrorHandlerService } from '../../../../core/services/error-handler-service';
import { ToastService } from '../../../../core/services/toast-service';
import { FormularioExamenSupletorioDTORespuesta } from '../../../../core/models/ExamenSupletorio/DTOResponse/FormularioExamenSupletorioDTORespuesta';
import { AnexoSupletorioDTORespuesta } from '../../../../core/models/ExamenSupletorio/DTOResponse/AnexoSupletorioDTORespuesta';
import { RadicacionExamenSupletorioDTORespuesta } from '../../../../core/models/ExamenSupletorio/DTOResponse/RadicacionExamenSupletorioDTORespuesta';
import { TipoAnexoDTORespuesta } from '../../../../core/models/TipoSolicitud/DTOResponse/TipoAnexoDTORespuesta';
import {
  CAUSAS_SUPLETORIO,
  PLAZO_SUPLETORIO_DIAS_HABILES,
  RUTAS_SOLICITUDES_POR_ROL,
  ROLES_ETIQUETA,
  TAMANIO_MAXIMO_PETICION_BYTES
} from '../../../../core/constantes/procesos-academicos';
import {
  comoTipoAnexo,
  errorDeArchivo,
  erroresDeCampos,
  esFechaValida,
  esHoraValida,
  fechaDeHoy
} from '../../../../core/utils/validaciones-academicas';

interface CampoAnexo {
  anexo: AnexoSupletorioDTORespuesta;
  tipo: TipoAnexoDTORespuesta;
}

const TEXTOS_CAUSA: Record<string, string> = {
  [CAUSAS_SUPLETORIO.CRUCE]: 'Cruce con el examen de otra asignatura',
  [CAUSAS_SUPLETORIO.OTRA]: 'Otra causa justificada'
};

@Component({
  selector: 'app-est-examen-supletorio-content-component',
  imports: [InputSelectComponent, InputDateComponent, InputTextComponent, InputAnexoUploadComponent, SimpleButtonComponent],
  templateUrl: './est-examen-supletorio-content-component.html'
})
export class EstExamenSupletorioContentComponent implements OnInit {
  readonly causas = CAUSAS_SUPLETORIO;

  formulario: FormularioExamenSupletorioDTORespuesta | null = null;
  campos: CampoAnexo[] = [];
  cargando = false;
  errorCarga = false;

  asignatura: string | null = null;
  fechaExamen = '';
  causa: string | null = null;
  asignaturaCruzada: string | null = null;
  fechaCruce = '';
  horaCruce = '';
  archivos: Record<string, File | null> = {};

  errores: Record<string, string> = {};
  erroresServidor: string[] = [];
  intentoEnvio = false;
  enviando = false;
  radicacion: RadicacionExamenSupletorioDTORespuesta | null = null;

  constructor(
    private examenSupletorioService: ExamenSupletorioService,
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

  get plazoDiasHabiles(): number {
    return this.formulario?.plazoDiasHabiles ?? PLAZO_SUPLETORIO_DIAS_HABILES;
  }

  get opcionesAsignatura(): { label: string; value: string }[] {
    return (this.formulario?.asignaturas ?? []).map(a => ({
      label: `${a.codigoAsignatura} - ${a.nombreAsignatura} (grupo ${a.grupo})`,
      value: a.uuidAsignaturaMatriculada
    }));
  }

  get opcionesCruce(): { label: string; value: string }[] {
    return this.opcionesAsignatura.filter(opcion => opcion.value !== this.asignatura);
  }

  get opcionesCausa(): { label: string; value: string }[] {
    return (this.formulario?.causas ?? []).map(causa => ({ label: TEXTOS_CAUSA[causa] ?? causa, value: causa }));
  }

  get camposVisibles(): CampoAnexo[] {
    return this.campos.filter(campo => this.seExige(campo.anexo));
  }

  get esCruce(): boolean {
    return this.causa === CAUSAS_SUPLETORIO.CRUCE;
  }

  cargarFormulario(): void {
    this.cargando = true;
    this.errorCarga = false;
    this.examenSupletorioService
      .getFormulario()
      .pipe(finalize(() => (this.cargando = false)))
      .subscribe({
        next: formulario => {
          this.formulario = formulario;
          this.campos = (formulario.anexos ?? []).map(anexo => ({
            anexo,
            tipo: comoTipoAnexo(anexo.uuidTipoAnexoAcademico, anexo.nombre, anexo.formatosPermitidos, true)
          }));
          this.archivos = {};
        },
        error: err => {
          this.errorCarga = true;
          this.errorHandlerService.handleError(err, 'Error', 'No se pudo cargar el formulario');
        }
      });
  }

  alCambiarAsignatura(valor: string | null): void {
    this.asignatura = valor;
    if (this.asignaturaCruzada === valor) this.asignaturaCruzada = null;
    this.revalidar();
  }

  alCambiarFechaExamen(valor: string): void {
    this.fechaExamen = valor ?? '';
    this.revalidar();
  }

  alCambiarCausa(valor: string | null): void {
    this.causa = valor;
    for (const campo of this.campos) {
      if (!this.seExige(campo.anexo)) delete this.archivos[campo.anexo.uuidTipoAnexoAcademico];
    }
    if (!this.esCruce) {
      this.asignaturaCruzada = null;
      this.fechaCruce = '';
      this.horaCruce = '';
    }
    this.revalidar();
  }

  alCambiarAsignaturaCruzada(valor: string | null): void {
    this.asignaturaCruzada = valor;
    this.revalidar();
  }

  alCambiarFechaCruce(valor: string): void {
    this.fechaCruce = valor ?? '';
    this.revalidar();
  }

  alCambiarHoraCruce(valor: string): void {
    this.horaCruce = valor ?? '';
    this.revalidar();
  }

  alSeleccionarAnexo(campo: CampoAnexo, archivo: File, entrada: InputAnexoUploadComponent): void {
    const uuid = campo.anexo.uuidTipoAnexoAcademico;
    const error = errorDeArchivo(archivo, campo.anexo.formatosPermitidos, campo.anexo.tamanioMaximoBytes);
    if (error) {
      this.archivos[uuid] = null;
      this.errores[uuid] = error;
      entrada.reset();
      return;
    }
    this.archivos[uuid] = archivo;
    delete this.errores[uuid];
    this.revalidar();
  }

  alQuitarAnexo(campo: CampoAnexo): void {
    this.archivos[campo.anexo.uuidTipoAnexoAcademico] = null;
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
    this.erroresServidor = [];
    this.examenSupletorioService
      .radicar({
        asignaturaMatriculada: this.asignatura!,
        fechaExamenNoPresentado: this.fechaExamen.trim(),
        tipoCausa: this.causa!,
        asignaturaCruzada: this.esCruce ? this.asignaturaCruzada : null,
        fechaExamenCruzada: this.esCruce ? this.fechaCruce.trim() : null,
        horaExamenCruzada: this.esCruce ? this.horaCruce.trim() : null,
        anexos: this.camposVisibles.map(campo => ({
          uuidTipoAnexoAcademico: campo.anexo.uuidTipoAnexoAcademico,
          archivo: this.archivos[campo.anexo.uuidTipoAnexoAcademico]!
        }))
      })
      .pipe(finalize(() => (this.enviando = false)))
      .subscribe({
        next: respuesta => {
          this.radicacion = respuesta;
          this.toastService.showSuccess('Solicitud radicada', `Radicado ${respuesta.radicado}`);
          this.irAlDetalle();
        },
        error: err => {
          this.erroresServidor = Object.entries(erroresDeCampos(err)).map(([campo, mensaje]) => `${campo}: ${mensaje}`);
          this.errorHandlerService.handleError(err, 'Error', 'No se pudo radicar la solicitud');
        }
      });
  }

  irAlDetalle(): void {
    if (!this.radicacion) return;
    this.router.navigate([RUTAS_SOLICITUDES_POR_ROL[ROLES_ETIQUETA.ESTUDIANTE], this.radicacion.uuidSolicitudAcademica]);
  }

  private seExige(anexo: AnexoSupletorioDTORespuesta): boolean {
    const causas = anexo.causas ?? [];
    if (this.causa) return causas.includes(this.causa);
    const todas = this.formulario?.causas ?? [];
    return todas.length > 0 && todas.every(causa => causas.includes(causa));
  }

  private revalidar(): void {
    if (this.intentoEnvio) this.errores = this.validar();
  }

  private validar(): Record<string, string> {
    const errores: Record<string, string> = {};
    if (!this.asignatura) errores['asignatura'] = 'Elige la asignatura del examen no presentado.';
    if (!esFechaValida(this.fechaExamen)) {
      errores['fechaExamen'] = 'Indica la fecha del examen no presentado.';
    } else if (this.fechaExamen.trim() > fechaDeHoy()) {
      errores['fechaExamen'] = 'La fecha del examen no presentado no puede ser posterior a hoy.';
    }
    if (!this.causa) errores['causa'] = 'Elige la causa de la no presentación.';
    if (this.esCruce) {
      if (!this.asignaturaCruzada) {
        errores['asignaturaCruzada'] = 'Elige la asignatura con la que se cruzó el examen.';
      } else if (this.asignaturaCruzada === this.asignatura) {
        errores['asignaturaCruzada'] = 'La asignatura cruzada debe ser distinta de la del examen no presentado.';
      }
      if (!esFechaValida(this.fechaCruce)) errores['fechaCruce'] = 'Indica la fecha del examen cruzado.';
      if (!esHoraValida(this.horaCruce)) errores['horaCruce'] = 'La hora del examen cruzado debe tener el formato HH:mm, por ejemplo 14:00.';
    }
    let total = 0;
    for (const campo of this.camposVisibles) {
      const uuid = campo.anexo.uuidTipoAnexoAcademico;
      const archivo = this.archivos[uuid];
      if (!archivo) {
        errores[uuid] = 'Este anexo es obligatorio.';
        continue;
      }
      total += archivo.size;
      const error = errorDeArchivo(archivo, campo.anexo.formatosPermitidos, campo.anexo.tamanioMaximoBytes);
      if (error) errores[uuid] = error;
    }
    if (total > TAMANIO_MAXIMO_PETICION_BYTES) {
      errores['total'] = 'Entre todos los archivos se superan los 20 MB que admite el envío.';
    }
    return errores;
  }
}
