import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { Observable, finalize, map, of, switchMap, tap } from 'rxjs';
import { GenericDialogFormComponent } from '../../../generic-dialog-form-component/generic-dialog-form-component';
import { SimpleButtonComponent } from '../../../buttons/simple-button-component/simple-button-component';
import { InputTextTareaComponent } from '../../../inputs/input-text-tarea-component/input-text-tarea-component';
import { InputDateComponent } from '../../../inputs/input-date-component/input-date-component';
import { InputAnexoUploadComponent } from '../../../inputs/input-anexo-upload-component/input-anexo-upload-component';
import { ExamenSupletorioService } from '../../../../core/services/examen-supletorio-service';
import { SolicitudAcademicaService } from '../../../../core/services/solicitud-academica-service';
import { CatalogoAcademicoService } from '../../../../core/services/catalogo-academico-service';
import { ErrorHandlerService } from '../../../../core/services/error-handler-service';
import { ToastService } from '../../../../core/services/toast-service';
import { SolicitudAcademicaDetalleDTORespuesta } from '../../../../core/models/SolicitudAcademica/DTOResponse/SolicitudAcademicaDetalleDTORespuesta';
import { ExamenSupletorioDetalleDTORespuesta } from '../../../../core/models/ExamenSupletorio/DTOResponse/ExamenSupletorioDetalleDTORespuesta';
import { TipoAnexoAcademicoDTORespuesta } from '../../../../core/models/CatalogoAcademico/DTOResponse/TipoAnexoAcademicoDTORespuesta';
import { TipoAnexoDTORespuesta } from '../../../../core/models/TipoSolicitud/DTOResponse/TipoAnexoDTORespuesta';
import {
  ACCIONES_ACADEMICAS,
  ANEXOS_PAGO_SUPLETORIO,
  AccionAcademica,
  CAUSAS_SUPLETORIO,
  ETAPAS_ACADEMICAS,
  MAXIMO_CARACTERES_OBSERVACION,
  ROLES_ETIQUETA,
  RolEtiqueta
} from '../../../../core/constantes/procesos-academicos';
import { comoTipoAnexo, errorDeArchivo, errorDeTexto, erroresDeCampos, esFechaValida } from '../../../../core/utils/validaciones-academicas';
import { formatearFecha } from '../../../../core/utils/formato';

const ACCIONES_DEL_PROCESO: AccionAcademica[] = [
  ACCIONES_ACADEMICAS.RECHAZAR_FUNCIONARIO,
  ACCIONES_ACADEMICAS.REMITIR_DECANO,
  ACCIONES_ACADEMICAS.APROBAR_DECANO,
  ACCIONES_ACADEMICAS.RECHAZAR_DECANO,
  ACCIONES_ACADEMICAS.ENVIAR_RESPUESTA,
  ACCIONES_ACADEMICAS.ENVIAR_RECIBO,
  ACCIONES_ACADEMICAS.SUBIR_COMPROBANTE,
  ACCIONES_ACADEMICAS.APROBAR_COMPROBANTE,
  ACCIONES_ACADEMICAS.RECHAZAR_COMPROBANTE
];

const ACCIONES_DE_RECHAZO: AccionAcademica[] = [
  ACCIONES_ACADEMICAS.RECHAZAR_FUNCIONARIO,
  ACCIONES_ACADEMICAS.RECHAZAR_DECANO,
  ACCIONES_ACADEMICAS.RECHAZAR_COMPROBANTE
];

const ACCIONES_CON_OBSERVACION: AccionAcademica[] = [
  ...ACCIONES_DE_RECHAZO,
  ACCIONES_ACADEMICAS.REMITIR_DECANO,
  ACCIONES_ACADEMICAS.APROBAR_DECANO
];

const ANEXO_DE_ACCION: Partial<Record<AccionAcademica, string>> = {
  [ACCIONES_ACADEMICAS.ENVIAR_RECIBO]: ANEXOS_PAGO_SUPLETORIO.RECIBO,
  [ACCIONES_ACADEMICAS.SUBIR_COMPROBANTE]: ANEXOS_PAGO_SUPLETORIO.COMPROBANTE
};

const ETIQUETAS_BOTON: Partial<Record<AccionAcademica, string>> = {
  [ACCIONES_ACADEMICAS.RECHAZAR_FUNCIONARIO]: 'Rechazar solicitud',
  [ACCIONES_ACADEMICAS.REMITIR_DECANO]: 'Remitir al Decano',
  [ACCIONES_ACADEMICAS.APROBAR_DECANO]: 'Aprobar',
  [ACCIONES_ACADEMICAS.RECHAZAR_DECANO]: 'Rechazar',
  [ACCIONES_ACADEMICAS.ENVIAR_RESPUESTA]: 'Enviar respuesta',
  [ACCIONES_ACADEMICAS.ENVIAR_RECIBO]: 'Enviar recibo de pago',
  [ACCIONES_ACADEMICAS.SUBIR_COMPROBANTE]: 'Subir comprobante de pago',
  [ACCIONES_ACADEMICAS.APROBAR_COMPROBANTE]: 'Aprobar comprobante',
  [ACCIONES_ACADEMICAS.RECHAZAR_COMPROBANTE]: 'Rechazar comprobante'
};

const TITULOS_DIALOGO: Partial<Record<AccionAcademica, string>> = {
  [ACCIONES_ACADEMICAS.RECHAZAR_FUNCIONARIO]: 'Rechazar la solicitud',
  [ACCIONES_ACADEMICAS.REMITIR_DECANO]: 'Remitir la solicitud al Decano',
  [ACCIONES_ACADEMICAS.APROBAR_DECANO]: 'Aprobar el examen supletorio',
  [ACCIONES_ACADEMICAS.RECHAZAR_DECANO]: 'Rechazar el examen supletorio',
  [ACCIONES_ACADEMICAS.ENVIAR_RESPUESTA]: 'Enviar la respuesta al estudiante',
  [ACCIONES_ACADEMICAS.ENVIAR_RECIBO]: 'Enviar el recibo de pago',
  [ACCIONES_ACADEMICAS.SUBIR_COMPROBANTE]: 'Subir el comprobante de pago',
  [ACCIONES_ACADEMICAS.APROBAR_COMPROBANTE]: 'Aprobar el comprobante de pago',
  [ACCIONES_ACADEMICAS.RECHAZAR_COMPROBANTE]: 'Rechazar el comprobante de pago'
};

const MENSAJES_EXITO: Partial<Record<AccionAcademica, string>> = {
  [ACCIONES_ACADEMICAS.RECHAZAR_FUNCIONARIO]: 'La solicitud quedó rechazada.',
  [ACCIONES_ACADEMICAS.REMITIR_DECANO]: 'La solicitud fue remitida al Decano.',
  [ACCIONES_ACADEMICAS.APROBAR_DECANO]: 'El examen supletorio quedó aprobado.',
  [ACCIONES_ACADEMICAS.RECHAZAR_DECANO]: 'El examen supletorio quedó rechazado.',
  [ACCIONES_ACADEMICAS.ENVIAR_RESPUESTA]: 'La respuesta fue enviada al estudiante.',
  [ACCIONES_ACADEMICAS.ENVIAR_RECIBO]: 'El recibo de pago fue enviado al estudiante.',
  [ACCIONES_ACADEMICAS.SUBIR_COMPROBANTE]: 'El comprobante de pago fue enviado.',
  [ACCIONES_ACADEMICAS.APROBAR_COMPROBANTE]: 'El comprobante quedó aprobado.',
  [ACCIONES_ACADEMICAS.RECHAZAR_COMPROBANTE]: 'El comprobante quedó rechazado.'
};

const TEXTOS_CAUSA: Record<string, string> = {
  [CAUSAS_SUPLETORIO.CRUCE]: 'Cruce con el examen de otra asignatura',
  [CAUSAS_SUPLETORIO.OTRA]: 'Otra causa justificada'
};

function mismoNombre(a: string | null | undefined, b: string): boolean {
  return (a ?? '').trim().normalize('NFC').toLowerCase() === b.normalize('NFC').toLowerCase();
}

@Component({
  selector: 'app-examen-supletorio-proceso-component',
  imports: [GenericDialogFormComponent, SimpleButtonComponent, InputTextTareaComponent, InputDateComponent, InputAnexoUploadComponent],
  templateUrl: './examen-supletorio-proceso-component.html'
})
export class ExamenSupletorioProcesoComponent implements OnChanges {
  @Input({ required: true }) uuidSolicitud!: string;
  @Input({ required: true }) solicitud!: SolicitudAcademicaDetalleDTORespuesta;
  @Input({ required: true }) rol!: RolEtiqueta;
  @Input({ required: true }) recargar!: () => void;

  readonly acciones = ACCIONES_ACADEMICAS;
  readonly maximoObservacion = MAXIMO_CARACTERES_OBSERVACION;
  readonly formatearFecha = formatearFecha;

  detalle: ExamenSupletorioDetalleDTORespuesta | null = null;
  cargando = false;
  errorCarga = false;

  tiposAnexo: TipoAnexoAcademicoDTORespuesta[] = [];
  cargandoTipos = false;

  accionActiva: AccionAcademica | null = null;
  dialogoVisible = false;
  enviando = false;
  observacion = '';
  requisitosVerificados = false;
  fechaAcordada = '';
  tipoAnexoPago: TipoAnexoAcademicoDTORespuesta | null = null;
  campoAnexoPago: TipoAnexoDTORespuesta | null = null;
  archivoPago: File | null = null;
  anexoPagoSubido = false;
  errores: Record<string, string> = {};
  erroresServidor: string[] = [];

  constructor(
    private examenSupletorioService: ExamenSupletorioService,
    private solicitudAcademicaService: SolicitudAcademicaService,
    private catalogoAcademicoService: CatalogoAcademicoService,
    private errorHandlerService: ErrorHandlerService,
    private toastService: ToastService
  ) {}

  ngOnChanges(cambios: SimpleChanges): void {
    if (cambios['uuidSolicitud'] || cambios['solicitud']) {
      this.anexoPagoSubido = false;
      this.cargarDetalle();
    }
  }

  get accionesVisibles(): AccionAcademica[] {
    const disponibles = this.solicitud?.accionesDisponibles ?? [];
    return ACCIONES_DEL_PROCESO.filter(accion => disponibles.includes(accion));
  }

  get tituloDialogo(): string {
    return this.accionActiva ? TITULOS_DIALOGO[this.accionActiva] ?? '' : '';
  }

  get esEstudiante(): boolean {
    return this.rol === ROLES_ETIQUETA.ESTUDIANTE;
  }

  get etapa(): string {
    return this.solicitud?.etapaCodigo ?? '';
  }

  get comprobanteRechazado(): boolean {
    return (
      this.etapa === ETAPAS_ACADEMICAS.RECHAZADA &&
      (this.solicitud?.anexos ?? []).some(anexo => mismoNombre(anexo.tipoAnexo, ANEXOS_PAGO_SUPLETORIO.COMPROBANTE))
    );
  }

  get pideObservacion(): boolean {
    return !!this.accionActiva && ACCIONES_CON_OBSERVACION.includes(this.accionActiva);
  }

  get observacionObligatoria(): boolean {
    return !!this.accionActiva && ACCIONES_DE_RECHAZO.includes(this.accionActiva);
  }

  get pideAnexoPago(): boolean {
    return !!this.accionActiva && !!ANEXO_DE_ACCION[this.accionActiva];
  }

  get anexoPagoYaCargado(): boolean {
    if (this.anexoPagoSubido) return true;
    const uuid = this.tipoAnexoPago?.uuidTipoAnexoAcademico;
    return !!uuid && (this.solicitud?.anexos ?? []).some(anexo => anexo.uuidTipoAnexoAcademico === uuid);
  }

  etiquetaBoton(accion: AccionAcademica): string {
    return ETIQUETAS_BOTON[accion] ?? accion;
  }

  esRechazo(accion: AccionAcademica): boolean {
    return ACCIONES_DE_RECHAZO.includes(accion);
  }

  textoCausa(causa: string | null | undefined): string {
    return TEXTOS_CAUSA[(causa ?? '').toLowerCase()] ?? causa ?? '';
  }

  cargarDetalle(): void {
    if (!this.uuidSolicitud) return;
    this.cargando = true;
    this.errorCarga = false;
    this.examenSupletorioService
      .getDetalle(this.uuidSolicitud)
      .pipe(finalize(() => (this.cargando = false)))
      .subscribe({
        next: detalle => (this.detalle = detalle),
        error: err => {
          this.detalle = null;
          this.errorCarga = true;
          this.errorHandlerService.handleError(err, 'Error', 'No se pudo cargar el examen supletorio');
        }
      });
  }

  abrir(accion: AccionAcademica): void {
    if (!this.accionesVisibles.includes(accion) || this.cargandoTipos) return;
    this.accionActiva = accion;
    this.observacion = '';
    this.requisitosVerificados = false;
    this.fechaAcordada = '';
    this.archivoPago = null;
    this.tipoAnexoPago = null;
    this.campoAnexoPago = null;
    this.errores = {};
    this.erroresServidor = [];
    const nombreAnexo = ANEXO_DE_ACCION[accion];
    if (nombreAnexo) {
      this.abrirConTipoAnexo(nombreAnexo);
      return;
    }
    this.dialogoVisible = true;
  }

  cerrar(visible: boolean): void {
    if (visible || this.enviando) return;
    this.dialogoVisible = false;
    this.accionActiva = null;
  }

  alCambiarVerificacion(evento: Event): void {
    this.requisitosVerificados = (evento.target as HTMLInputElement).checked;
  }

  alSeleccionarAnexoPago(archivo: File, entrada: InputAnexoUploadComponent): void {
    const error = errorDeArchivo(archivo, this.tipoAnexoPago?.formatosPermitidos);
    if (error) {
      this.archivoPago = null;
      this.errores['anexoPago'] = error;
      entrada.reset();
      return;
    }
    this.archivoPago = archivo;
    delete this.errores['anexoPago'];
  }

  alQuitarAnexoPago(): void {
    this.archivoPago = null;
  }

  confirmar(): void {
    if (this.enviando || !this.accionActiva) return;
    const accion = this.accionActiva;
    this.errores = this.validar(accion);
    if (Object.keys(this.errores).length > 0) return;
    this.enviando = true;
    this.erroresServidor = [];
    this.ejecutar(accion)
      .pipe(finalize(() => (this.enviando = false)))
      .subscribe({
        next: () => {
          this.toastService.showSuccess('Listo', MENSAJES_EXITO[accion] ?? 'Acción realizada.');
          this.dialogoVisible = false;
          this.accionActiva = null;
          this.recargar();
        },
        error: err => {
          this.erroresServidor = Object.entries(erroresDeCampos(err)).map(([campo, mensaje]) => `${campo}: ${mensaje}`);
          this.errorHandlerService.handleError(err, 'Error', 'No se pudo completar la acción');
        }
      });
  }

  private abrirConTipoAnexo(nombre: string): void {
    const encontrado = this.tiposAnexo.find(tipo => mismoNombre(tipo.nombre, nombre));
    if (encontrado) {
      this.prepararAnexoPago(encontrado);
      return;
    }
    this.cargandoTipos = true;
    this.catalogoAcademicoService
      .getTiposAnexoPorTipo(this.solicitud.uuidTipoSolicitudAcademica)
      .pipe(finalize(() => (this.cargandoTipos = false)))
      .subscribe({
        next: tipos => {
          this.tiposAnexo = tipos ?? [];
          const tipo = this.tiposAnexo.find(t => mismoNombre(t.nombre, nombre));
          if (!tipo) {
            this.accionActiva = null;
            this.errorHandlerService.handleError(
              { message: `El catálogo no tiene el tipo de anexo ${nombre}` },
              'Error',
              'No se pudo preparar la carga del anexo'
            );
            return;
          }
          this.prepararAnexoPago(tipo);
        },
        error: err => {
          this.accionActiva = null;
          this.errorHandlerService.handleError(err, 'Error', 'No se pudieron cargar los tipos de anexo');
        }
      });
  }

  private prepararAnexoPago(tipo: TipoAnexoAcademicoDTORespuesta): void {
    this.tipoAnexoPago = tipo;
    this.campoAnexoPago = comoTipoAnexo(tipo.uuidTipoAnexoAcademico, tipo.nombre, tipo.formatosPermitidos, true);
    this.dialogoVisible = true;
  }

  private ejecutar(accion: AccionAcademica): Observable<unknown> {
    const uuid = this.uuidSolicitud;
    const observacion = this.observacion.trim();
    switch (accion) {
      case ACCIONES_ACADEMICAS.RECHAZAR_FUNCIONARIO:
        return this.examenSupletorioService.rechazarPorFuncionario(uuid, { observacion });
      case ACCIONES_ACADEMICAS.REMITIR_DECANO:
        return this.examenSupletorioService.remitirADecano(uuid, {
          requisitosVerificados: true,
          observacion: observacion === '' ? null : observacion
        });
      case ACCIONES_ACADEMICAS.APROBAR_DECANO:
        return this.examenSupletorioService.aprobarPorDecano(uuid, observacion === '' ? undefined : { observacion });
      case ACCIONES_ACADEMICAS.RECHAZAR_DECANO:
        return this.examenSupletorioService.rechazarPorDecano(uuid, { observacion });
      case ACCIONES_ACADEMICAS.ENVIAR_RECIBO:
        return this.subirAnexoPago().pipe(switchMap(() => this.examenSupletorioService.enviarRecibo(uuid)));
      case ACCIONES_ACADEMICAS.SUBIR_COMPROBANTE:
        return this.subirAnexoPago().pipe(switchMap(() => this.examenSupletorioService.subirComprobante(uuid)));
      case ACCIONES_ACADEMICAS.APROBAR_COMPROBANTE: {
        const fecha = this.fechaAcordada.trim();
        return this.examenSupletorioService.aprobarComprobante(uuid, fecha === '' ? undefined : { fechaAcordadaExamen: fecha });
      }
      case ACCIONES_ACADEMICAS.RECHAZAR_COMPROBANTE:
        return this.examenSupletorioService.rechazarComprobante(uuid, { observacion });
      default:
        return this.examenSupletorioService.enviarRespuesta(uuid);
    }
  }

  private subirAnexoPago(): Observable<void> {
    if (!this.archivoPago || !this.tipoAnexoPago) return of(undefined);
    return this.solicitudAcademicaService
      .adjuntarAnexo(this.uuidSolicitud, { archivo: this.archivoPago, tipoAnexo: this.tipoAnexoPago.uuidTipoAnexoAcademico })
      .pipe(
        tap(() => {
          this.anexoPagoSubido = true;
          this.archivoPago = null;
        }),
        map(() => undefined)
      );
  }

  private validar(accion: AccionAcademica): Record<string, string> {
    const errores: Record<string, string> = {};
    if (ACCIONES_CON_OBSERVACION.includes(accion)) {
      const error = errorDeTexto(this.observacion, MAXIMO_CARACTERES_OBSERVACION, ACCIONES_DE_RECHAZO.includes(accion), 'La observación', true);
      if (error) errores['observacion'] = error;
    }
    if (accion === ACCIONES_ACADEMICAS.REMITIR_DECANO && !this.requisitosVerificados) {
      errores['requisitos'] = 'Confirma que verificaste los requisitos; si no se cumplen, rechaza la solicitud.';
    }
    if (ANEXO_DE_ACCION[accion]) {
      if (this.archivoPago) {
        const error = errorDeArchivo(this.archivoPago, this.tipoAnexoPago?.formatosPermitidos);
        if (error) errores['anexoPago'] = error;
      } else if (!this.anexoPagoYaCargado) {
        errores['anexoPago'] = `Debes subir el ${ANEXO_DE_ACCION[accion]!.toLowerCase()}.`;
      }
    }
    if (accion === ACCIONES_ACADEMICAS.APROBAR_COMPROBANTE) {
      const fecha = this.fechaAcordada.trim();
      if (fecha !== '' && !esFechaValida(fecha)) {
        errores['fechaAcordada'] = 'La fecha acordada no es una fecha válida.';
      } else if (fecha !== '' && this.detalle && fecha < this.detalle.fechaExamenNoPresentado) {
        errores['fechaAcordada'] = `La fecha acordada no puede ser anterior a la fecha del examen no presentado (${formatearFecha(this.detalle.fechaExamenNoPresentado)}).`;
      }
    }
    return errores;
  }
}
