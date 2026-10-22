import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { Observable, catchError, finalize, map, of, switchMap, tap, throwError } from 'rxjs';
import { GenericDialogFormComponent } from '../../../generic-dialog-form-component/generic-dialog-form-component';
import { SimpleButtonComponent } from '../../../buttons/simple-button-component/simple-button-component';
import { InputTextTareaComponent } from '../../../inputs/input-text-tarea-component/input-text-tarea-component';
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
import { DocenteDTORespuesta } from '../../../../core/models/ExamenSupletorio/DTOResponse/DocenteDTORespuesta';
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
import {
  ETIQUETAS_ESTUDIANTE,
  NOTA_APROBAR_DECANO,
  campoObservacion,
  esEtiqueta,
  etiquetaCampoObservacion
} from '../../../../core/constantes/etiquetas-academicas';
import {
  comoTipoAnexo,
  errorDeArchivo,
  errorDeObservacion,
  erroresDeCampos
} from '../../../../core/utils/validaciones-academicas';
import { formatearFecha } from '../../../../core/utils/formato';
import { ResumenSolicitudComponent } from '../../../others/resumen-solicitud-component/resumen-solicitud-component';
import { SelectorDocenteComponent } from '../../../others/selector-docente-component/selector-docente-component';
import { DocumentoAdicionalComponent } from '../../../others/documento-adicional-component/documento-adicional-component';
import { DecisionDecanoComponent } from '../../../others/decision-decano-component/decision-decano-component';
import { DecisionDecano, ETIQUETA_ENVIAR_DECISION, accionDeDecision, esAccionDecisionDecano } from '../../../../core/utils/decision-decano';
import {
  TITULO_FALLA_DOCUMENTO_ADICIONAL,
  admiteDocumentoAdicional,
  mensajeDeFalla,
  subirDocumentoAdicional,
  tieneDocumentoAdicional
} from '../../../../core/utils/documento-adicional';

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

export type AvisoEtapaSupletorio = 'PENDIENTE_PAGO' | 'EN_VERIFICACION_PAGO' | 'COMPROBANTE_RECHAZADO';

const ANEXO_DE_ACCION: Partial<Record<AccionAcademica, string>> = {
  [ACCIONES_ACADEMICAS.ENVIAR_RECIBO]: ANEXOS_PAGO_SUPLETORIO.RECIBO,
  [ACCIONES_ACADEMICAS.SUBIR_COMPROBANTE]: ANEXOS_PAGO_SUPLETORIO.COMPROBANTE
};

const ETIQUETAS_BOTON: Partial<Record<AccionAcademica, string>> = {
  [ACCIONES_ACADEMICAS.RECHAZAR_FUNCIONARIO]: 'Rechazar solicitud',
  [ACCIONES_ACADEMICAS.REMITIR_DECANO]: 'Remitir al Decano',
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

const TEXTOS_BOTON_DIALOGO: Partial<Record<AccionAcademica, string>> = {
  [ACCIONES_ACADEMICAS.RECHAZAR_FUNCIONARIO]: 'Rechazar solicitud',
  [ACCIONES_ACADEMICAS.REMITIR_DECANO]: 'Remitir al Decano',
  [ACCIONES_ACADEMICAS.RECHAZAR_DECANO]: ETIQUETA_ENVIAR_DECISION,
  [ACCIONES_ACADEMICAS.ENVIAR_RESPUESTA]: 'Enviar respuesta',
  [ACCIONES_ACADEMICAS.APROBAR_DECANO]: ETIQUETA_ENVIAR_DECISION,
  [ACCIONES_ACADEMICAS.ENVIAR_RECIBO]: 'Enviar recibo',
  [ACCIONES_ACADEMICAS.SUBIR_COMPROBANTE]: 'Subir comprobante',
  [ACCIONES_ACADEMICAS.APROBAR_COMPROBANTE]: 'Aprobar comprobante',
  [ACCIONES_ACADEMICAS.RECHAZAR_COMPROBANTE]: 'Rechazar comprobante'
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
  imports: [
    GenericDialogFormComponent,
    SimpleButtonComponent,
    InputTextTareaComponent,
    InputAnexoUploadComponent,
    ResumenSolicitudComponent,
    SelectorDocenteComponent,
    DocumentoAdicionalComponent,
    DecisionDecanoComponent
  ],
  templateUrl: './examen-supletorio-proceso-component.html',
  styleUrl: '../estilos-academicos.css'
})
export class ExamenSupletorioProcesoComponent implements OnChanges {
  @Input({ required: true }) uuidSolicitud!: string;
  @Input({ required: true }) solicitud!: SolicitudAcademicaDetalleDTORespuesta;
  @Input({ required: true }) rol!: RolEtiqueta;
  @Input({ required: true }) recargar!: () => void;

  readonly acciones = ACCIONES_ACADEMICAS;
  readonly notaAprobarDecano = NOTA_APROBAR_DECANO;
  readonly etiquetaEnviarDecision = ETIQUETA_ENVIAR_DECISION;
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
  docenteElegido: DocenteDTORespuesta | null = null;
  tipoAnexoPago: TipoAnexoAcademicoDTORespuesta | null = null;
  campoAnexoPago: TipoAnexoDTORespuesta | null = null;
  archivoPago: File | null = null;
  anexoPagoSubido = false;
  archivoAdicional: File | null = null;
  adicionalSubido = false;
  errorAdicional = '';
  decisionDecano: DecisionDecano | null = null;
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
      this.adicionalSubido = false;
      this.decisionDecano = null;
      this.cargarDetalle();
    }
  }

  get accionesVisibles(): AccionAcademica[] {
    const disponibles = this.solicitud?.accionesDisponibles ?? [];
    return ACCIONES_DEL_PROCESO.filter(accion => disponibles.includes(accion));
  }

  get accionesBotones(): AccionAcademica[] {
    return this.accionesVisibles.filter(accion => !esAccionDecisionDecano(accion));
  }

  get decide(): boolean {
    return this.accionesVisibles.some(accion => esAccionDecisionDecano(accion));
  }

  get tituloDialogo(): string {
    return this.accionActiva ? TITULOS_DIALOGO[this.accionActiva] ?? '' : '';
  }

  get textoBotonDialogo(): string {
    return this.accionActiva ? TEXTOS_BOTON_DIALOGO[this.accionActiva] ?? 'Confirmar' : 'Confirmar';
  }

  get esEstudiante(): boolean {
    return this.rol === ROLES_ETIQUETA.ESTUDIANTE;
  }

  get nombreDocente(): string | null {
    const docente = this.detalle?.docente;
    return docente ? `${docente.nombres} ${docente.apellidos}`.trim() : null;
  }

  alElegirDocente(docente: DocenteDTORespuesta | null): void {
    this.docenteElegido = docente;
    if (docente) delete this.errores['docente'];
  }

  get avisoEtapa(): AvisoEtapaSupletorio | null {
    if (this.enEtapa(ETAPAS_ACADEMICAS.PENDIENTE_PAGO, ETIQUETAS_ESTUDIANTE.PENDIENTE_PAGO)) return 'PENDIENTE_PAGO';
    if (this.enEtapa(ETAPAS_ACADEMICAS.EN_VERIFICACION_PAGO, ETIQUETAS_ESTUDIANTE.EN_VERIFICACION_PAGO)) return 'EN_VERIFICACION_PAGO';
    const conComprobante = (this.solicitud?.anexos ?? []).some(anexo => mismoNombre(anexo.tipoAnexo, ANEXOS_PAGO_SUPLETORIO.COMPROBANTE));
    return conComprobante && this.enEtapa(ETAPAS_ACADEMICAS.RECHAZADA, ETIQUETAS_ESTUDIANTE.RECHAZADA) ? 'COMPROBANTE_RECHAZADO' : null;
  }

  private enEtapa(codigo: string, etiquetaEstudiante: string): boolean {
    if (this.esEstudiante) return esEtiqueta(this.solicitud?.etiqueta, etiquetaEstudiante);
    return this.solicitud?.etapaCodigo === codigo;
  }

  get campoObservacion() {
    return campoObservacion(this.accionActiva);
  }

  get etiquetaObservacion(): string {
    const campo = this.campoObservacion;
    return campo ? etiquetaCampoObservacion(campo) : '';
  }

  get pideDocumentoAdicional(): boolean {
    return admiteDocumentoAdicional(this.accionActiva);
  }

  get hayDocumentoAdicional(): boolean {
    return this.adicionalSubido || tieneDocumentoAdicional(this.solicitud?.anexos);
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
    this.docenteElegido = null;
    this.archivoPago = null;
    this.archivoAdicional = null;
    this.errorAdicional = '';
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

  abrirDecision(): void {
    const accion = accionDeDecision(this.decisionDecano);
    if (accion) this.abrir(accion);
  }

  cerrar(visible: boolean): void {
    if (visible || this.enviando) return;
    this.dialogoVisible = false;
    this.accionActiva = null;
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
    this.errorAdicional = '';
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
          this.errorHandlerService.handleError(
            err,
            'Error',
            this.errorAdicional ? TITULO_FALLA_DOCUMENTO_ADICIONAL : 'No se pudo completar la acción'
          );
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
    if (!admiteDocumentoAdicional(accion)) return this.ejecutarAccion(accion);
    return this.subirAdicional().pipe(switchMap(() => this.ejecutarAccion(accion)));
  }

  private subirAdicional(): Observable<void> {
    return subirDocumentoAdicional(
      this.catalogoAcademicoService,
      this.solicitudAcademicaService,
      this.solicitud.uuidTipoSolicitudAcademica,
      this.uuidSolicitud,
      this.archivoAdicional
    ).pipe(
      tap(() => {
        if (this.archivoAdicional) this.adicionalSubido = true;
        this.archivoAdicional = null;
      }),
      catchError(err => {
        this.errorAdicional = mensajeDeFalla(err);
        return throwError(() => err);
      })
    );
  }

  private ejecutarAccion(accion: AccionAcademica): Observable<unknown> {
    const uuid = this.uuidSolicitud;
    const observacion = this.observacion.trim();
    const opcional = observacion === '' ? null : observacion;
    switch (accion) {
      case ACCIONES_ACADEMICAS.RECHAZAR_FUNCIONARIO:
        return this.examenSupletorioService.rechazarPorFuncionario(uuid, { observacion });
      case ACCIONES_ACADEMICAS.REMITIR_DECANO:
        return this.examenSupletorioService.remitirADecano(uuid, { observacion: opcional });
      case ACCIONES_ACADEMICAS.APROBAR_DECANO:
        return this.examenSupletorioService.aprobarPorDecano(uuid, opcional === null ? undefined : { observacion: opcional });
      case ACCIONES_ACADEMICAS.RECHAZAR_DECANO:
        return this.examenSupletorioService.rechazarPorDecano(uuid, { observacion: opcional });
      case ACCIONES_ACADEMICAS.ENVIAR_RECIBO:
        return this.subirAnexoPago().pipe(switchMap(() => this.examenSupletorioService.enviarRecibo(uuid)));
      case ACCIONES_ACADEMICAS.SUBIR_COMPROBANTE:
        return this.subirAnexoPago().pipe(switchMap(() => this.examenSupletorioService.subirComprobante(uuid)));
      case ACCIONES_ACADEMICAS.APROBAR_COMPROBANTE:
        return this.examenSupletorioService.aprobarComprobante(uuid, {
          observacion: opcional,
          docenteUuid: this.docenteElegido!.uuidUsuario
        });
      case ACCIONES_ACADEMICAS.RECHAZAR_COMPROBANTE:
        return this.examenSupletorioService.rechazarComprobante(uuid, { observacion });
      default:
        return this.examenSupletorioService.enviarRespuesta(uuid, { observacion: opcional });
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
    const errorObservacion = errorDeObservacion(accion, this.observacion);
    if (errorObservacion) errores['observacion'] = errorObservacion;
    if (ANEXO_DE_ACCION[accion]) {
      if (this.archivoPago) {
        const error = errorDeArchivo(this.archivoPago, this.tipoAnexoPago?.formatosPermitidos);
        if (error) errores['anexoPago'] = error;
      } else if (!this.anexoPagoYaCargado) {
        errores['anexoPago'] = `Debes subir el ${ANEXO_DE_ACCION[accion]!.toLowerCase()}.`;
      }
    }
    if (accion === ACCIONES_ACADEMICAS.APROBAR_COMPROBANTE && !this.docenteElegido) {
      errores['docente'] = 'Selecciona el profesor que atenderá el examen supletorio.';
    }
    return errores;
  }
}
