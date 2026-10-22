import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { Observable, catchError, finalize, map, of, switchMap, tap, throwError } from 'rxjs';
import { GenericDialogFormComponent } from '../../../generic-dialog-form-component/generic-dialog-form-component';
import { SimpleButtonComponent } from '../../../buttons/simple-button-component/simple-button-component';
import { InputTextTareaComponent } from '../../../inputs/input-text-tarea-component/input-text-tarea-component';
import { InputAnexoUploadComponent } from '../../../inputs/input-anexo-upload-component/input-anexo-upload-component';
import { CancelacionMatriculaService } from '../../../../core/services/cancelacion-matricula-service';
import { SolicitudAcademicaService } from '../../../../core/services/solicitud-academica-service';
import { CatalogoAcademicoService } from '../../../../core/services/catalogo-academico-service';
import { ErrorHandlerService } from '../../../../core/services/error-handler-service';
import { ToastService } from '../../../../core/services/toast-service';
import { SolicitudAcademicaDetalleDTORespuesta } from '../../../../core/models/SolicitudAcademica/DTOResponse/SolicitudAcademicaDetalleDTORespuesta';
import { CancelacionMatriculaDetalleDTORespuesta } from '../../../../core/models/CancelacionMatricula/DTOResponse/CancelacionMatriculaDetalleDTORespuesta';
import { AsignaturaCancelacionDTORespuesta } from '../../../../core/models/CancelacionMatricula/DTOResponse/AsignaturaCancelacionDTORespuesta';
import { EvaluacionAsignaturaDTOPeticion } from '../../../../core/models/CancelacionMatricula/DTORequest/EvaluacionAsignaturaDTOPeticion';
import {
  ACCIONES_ACADEMICAS,
  AccionAcademica,
  FORMATOS_RESOLUCION,
  MAXIMO_CARACTERES_OBSERVACION,
  ROLES_ETIQUETA,
  RolEtiqueta
} from '../../../../core/constantes/procesos-academicos';
import {
  NOTA_APROBAR_DECANO,
  RUTAS_ENCABEZADO_ASIGNATURAS,
  campoObservacion,
  etiquetaCampoObservacion
} from '../../../../core/constantes/etiquetas-academicas';
import { NOTA_SIMCA } from '../../../../core/constantes/textos-cancelacion-matricula';
import { CeldaEncabezado, agruparEncabezados } from '../../../../core/utils/encabezado-agrupado';
import { ResumenSolicitudComponent } from '../../../others/resumen-solicitud-component/resumen-solicitud-component';
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
import {
  comoTipoAnexo,
  errorDeArchivo,
  errorDeFaltas,
  errorDeNotaAsignatura,
  errorDeObservacion,
  erroresDeCampos
} from '../../../../core/utils/validaciones-academicas';
import { AMBITOS_SITUACION, OpcionSituacion, opcionesDeSituacion, textoSituacion } from '../../../../core/utils/situaciones-academicas';

interface EvaluacionEnEdicion {
  faltas: string;
  nota: string;
  situacion: string | null;
  cancelar: string | null;
}

type CampoEvaluacion = 'faltas' | 'nota' | 'situacion' | 'cancelar';

interface ColumnaAsignatura {
  titulo: string;
  campo: CampoEvaluacion | null;
  valor: (a: AsignaturaCancelacionDTORespuesta) => string | null;
}

function textoLibre(valor: string | null | undefined): string | null {
  return valor !== null && valor !== undefined && String(valor).trim() !== '' ? String(valor) : null;
}

const COLUMNAS: ColumnaAsignatura[] = [
  { titulo: 'Código', campo: null, valor: a => a.codigoAsignatura },
  { titulo: 'Asignatura', campo: null, valor: a => a.nombreAsignatura },
  { titulo: 'Faltas', campo: 'faltas', valor: a => textoLibre(a.numeroFaltas) },
  { titulo: 'Nota', campo: 'nota', valor: a => textoLibre(a.nota) },
  { titulo: 'Situación en la matrícula', campo: 'situacion', valor: a => textoSituacion(a.situacionMatricula) },
  { titulo: 'Situación al cancelar', campo: 'cancelar', valor: a => textoSituacion(a.situacionCancelar) }
];

const ACCIONES_DEL_PROCESO: AccionAcademica[] = [
  ACCIONES_ACADEMICAS.RECHAZAR_FUNCIONARIO,
  ACCIONES_ACADEMICAS.REMITIR_DECANO,
  ACCIONES_ACADEMICAS.APROBAR_DECANO,
  ACCIONES_ACADEMICAS.RECHAZAR_DECANO,
  ACCIONES_ACADEMICAS.ENVIAR_RESPUESTA
];

const ETIQUETAS_BOTON: Partial<Record<AccionAcademica, string>> = {
  [ACCIONES_ACADEMICAS.RECHAZAR_FUNCIONARIO]: 'Rechazar solicitud',
  [ACCIONES_ACADEMICAS.REMITIR_DECANO]: 'Remitir al Decano',
  [ACCIONES_ACADEMICAS.ENVIAR_RESPUESTA]: 'Enviar respuesta'
};

const TITULOS_DIALOGO: Partial<Record<AccionAcademica, string>> = {
  [ACCIONES_ACADEMICAS.RECHAZAR_FUNCIONARIO]: 'Rechazar la solicitud',
  [ACCIONES_ACADEMICAS.REMITIR_DECANO]: 'Remitir la solicitud al Decano',
  [ACCIONES_ACADEMICAS.APROBAR_DECANO]: 'Aprobar la cancelación de matrícula',
  [ACCIONES_ACADEMICAS.RECHAZAR_DECANO]: 'Rechazar la cancelación de matrícula',
  [ACCIONES_ACADEMICAS.ENVIAR_RESPUESTA]: 'Enviar la respuesta al estudiante'
};

const TEXTOS_BOTON_DIALOGO: Partial<Record<AccionAcademica, string>> = {
  [ACCIONES_ACADEMICAS.RECHAZAR_FUNCIONARIO]: 'Rechazar solicitud',
  [ACCIONES_ACADEMICAS.REMITIR_DECANO]: 'Remitir al Decano',
  [ACCIONES_ACADEMICAS.RECHAZAR_DECANO]: ETIQUETA_ENVIAR_DECISION,
  [ACCIONES_ACADEMICAS.ENVIAR_RESPUESTA]: 'Enviar respuesta',
  [ACCIONES_ACADEMICAS.APROBAR_DECANO]: ETIQUETA_ENVIAR_DECISION
};

const MENSAJES_EXITO: Partial<Record<AccionAcademica, string>> = {
  [ACCIONES_ACADEMICAS.RECHAZAR_FUNCIONARIO]: 'La solicitud quedó rechazada.',
  [ACCIONES_ACADEMICAS.REMITIR_DECANO]: 'La solicitud fue remitida al Decano.',
  [ACCIONES_ACADEMICAS.APROBAR_DECANO]: 'La cancelación quedó aprobada.',
  [ACCIONES_ACADEMICAS.RECHAZAR_DECANO]: 'La cancelación quedó rechazada.',
  [ACCIONES_ACADEMICAS.ENVIAR_RESPUESTA]: 'La respuesta fue enviada al estudiante.'
};

const COLUMNAS_FIJAS = ['Código', 'Asignatura'];

@Component({
  selector: 'app-cancelacion-matricula-proceso-component',
  imports: [
    GenericDialogFormComponent,
    SimpleButtonComponent,
    InputTextTareaComponent,
    InputAnexoUploadComponent,
    ResumenSolicitudComponent,
    DocumentoAdicionalComponent,
    DecisionDecanoComponent
  ],
  templateUrl: './cancelacion-matricula-proceso-component.html',
  styleUrls: ['../estilos-academicos.css', '../tabla-asignaturas.css']
})
export class CancelacionMatriculaProcesoComponent implements OnChanges {
  @Input({ required: true }) uuidSolicitud!: string;
  @Input({ required: true }) solicitud!: SolicitudAcademicaDetalleDTORespuesta;
  @Input({ required: true }) rol!: RolEtiqueta;
  @Input({ required: true }) recargar!: () => void;

  readonly acciones = ACCIONES_ACADEMICAS;
  readonly maximoObservacion = MAXIMO_CARACTERES_OBSERVACION;
  readonly tipoResolucion = comoTipoAnexo('resolucion', 'Escaneo de la Resolución firmada', FORMATOS_RESOLUCION, true);

  readonly notaAprobarDecano = NOTA_APROBAR_DECANO;
  readonly notaSimca = NOTA_SIMCA;
  readonly etiquetaEnviarDecision = ETIQUETA_ENVIAR_DECISION;

  columnas: ColumnaAsignatura[] = COLUMNAS;
  encabezadoAgrupado: CeldaEncabezado[][] = agruparEncabezados(COLUMNAS.map(c => c.titulo), RUTAS_ENCABEZADO_ASIGNATURAS);

  detalle: CancelacionMatriculaDetalleDTORespuesta | null = null;
  faltanDatosTabla = false;
  cargando = false;
  errorCarga = false;

  opcionesSituacion: OpcionSituacion[] = [];
  opcionesSituacionCancelar: OpcionSituacion[] = [];
  cargandoSituaciones = false;

  accionActiva: AccionAcademica | null = null;
  dialogoVisible = false;
  enviando = false;
  observacion = '';
  archivoResolucion: File | null = null;
  resolucionSubida = false;
  archivoAdicional: File | null = null;
  adicionalSubido = false;
  errorAdicional = '';
  evaluaciones: Record<string, EvaluacionEnEdicion> = {};
  decisionDecano: DecisionDecano | null = null;
  errores: Record<string, string> = {};
  erroresServidor: string[] = [];

  constructor(
    private cancelacionMatriculaService: CancelacionMatriculaService,
    private solicitudAcademicaService: SolicitudAcademicaService,
    private catalogoAcademicoService: CatalogoAcademicoService,
    private errorHandlerService: ErrorHandlerService,
    private toastService: ToastService
  ) {}

  ngOnChanges(cambios: SimpleChanges): void {
    if (cambios['uuidSolicitud'] || cambios['solicitud']) {
      this.resolucionSubida = false;
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

  get muestraNotaSimca(): boolean {
    return this.rol !== ROLES_ETIQUETA.ESTUDIANTE;
  }

  get asignaturas(): AsignaturaCancelacionDTORespuesta[] {
    return this.detalle?.asignaturas ?? [];
  }

  get tituloDialogo(): string {
    return this.accionActiva ? TITULOS_DIALOGO[this.accionActiva] ?? '' : '';
  }

  get textoBotonDialogo(): string {
    return this.accionActiva ? TEXTOS_BOTON_DIALOGO[this.accionActiva] ?? 'Confirmar' : 'Confirmar';
  }

  get hayResolucion(): boolean {
    return this.solicitud?.tieneResolucion || this.resolucionSubida;
  }

  get pideDocumentoAdicional(): boolean {
    return admiteDocumentoAdicional(this.accionActiva);
  }

  get hayDocumentoAdicional(): boolean {
    return this.adicionalSubido || tieneDocumentoAdicional(this.solicitud?.anexos);
  }

  get campoObservacion() {
    return campoObservacion(this.accionActiva);
  }

  get etiquetaObservacion(): string {
    const campo = this.campoObservacion;
    return campo ? etiquetaCampoObservacion(campo) : '';
  }

  esRechazo(accion: AccionAcademica): boolean {
    return accion === ACCIONES_ACADEMICAS.RECHAZAR_FUNCIONARIO || accion === ACCIONES_ACADEMICAS.RECHAZAR_DECANO;
  }

  etiquetaBoton(accion: AccionAcademica): string {
    return ETIQUETAS_BOTON[accion] ?? accion;
  }

  get editable(): boolean {
    return this.accionesVisibles.includes(ACCIONES_ACADEMICAS.REMITIR_DECANO);
  }

  valorDe(evento: Event): string {
    return (evento.target as HTMLInputElement | HTMLSelectElement).value;
  }

  errorCelda(columna: ColumnaAsignatura, a: AsignaturaCancelacionDTORespuesta): string {
    return this.editable && columna.campo ? this.errores[`${columna.campo}-${a.uuidAsignaturaSolicitud}`] ?? '' : '';
  }

  cargarDetalle(): void {
    if (!this.uuidSolicitud) return;
    this.cargando = true;
    this.errorCarga = false;
    this.cancelacionMatriculaService
      .getDetalle(this.uuidSolicitud)
      .pipe(finalize(() => (this.cargando = false)))
      .subscribe({
        next: detalle => {
          this.detalle = detalle;
          this.faltanDatosTabla = false;
          this.columnas = this.columnasVisibles(detalle.asignaturas);
          this.encabezadoAgrupado = agruparEncabezados(this.columnas.map(c => c.titulo), RUTAS_ENCABEZADO_ASIGNATURAS);
          this.evaluaciones = this.editable ? this.evaluacionesIniciales(detalle.asignaturas) : {};
          if (this.editable) this.asegurarSituaciones(() => undefined);
        },
        error: err => {
          this.detalle = null;
          this.evaluaciones = {};
          this.errorCarga = true;
          this.errorHandlerService.handleError(err, 'Error', 'No se pudo cargar la cancelación de matrícula');
        }
      });
  }

  abrir(accion: AccionAcademica): void {
    if (!this.accionesVisibles.includes(accion) || this.cargandoSituaciones) return;
    this.accionActiva = accion;
    this.observacion = '';
    this.archivoResolucion = null;
    this.archivoAdicional = null;
    this.errorAdicional = '';
    this.errores = {};
    this.erroresServidor = [];
    this.faltanDatosTabla = false;
    if (accion === ACCIONES_ACADEMICAS.REMITIR_DECANO) {
      this.asegurarSituaciones(() => this.abrirRemision());
      return;
    }
    this.dialogoVisible = true;
  }

  abrirDecision(): void {
    const accion = accionDeDecision(this.decisionDecano);
    if (accion) this.abrir(accion);
  }

  private abrirRemision(): void {
    this.errores = this.erroresDeEvaluacion();
    if (Object.keys(this.errores).length > 0) {
      this.faltanDatosTabla = true;
      this.accionActiva = null;
      return;
    }
    this.dialogoVisible = true;
  }

  private evaluacionesIniciales(asignaturas: AsignaturaCancelacionDTORespuesta[]): Record<string, EvaluacionEnEdicion> {
    return Object.fromEntries(
      asignaturas.map(a => [
        a.uuidAsignaturaSolicitud,
        {
          faltas: a.numeroFaltas ?? '',
          nota: a.nota ?? '',
          situacion: a.situacionMatricula?.uuidSituacionAcademica ?? null,
          cancelar: a.situacionCancelar?.uuidSituacionAcademica ?? null
        }
      ])
    );
  }

  cerrar(visible: boolean): void {
    if (visible || this.enviando) return;
    this.dialogoVisible = false;
    this.accionActiva = null;
  }

  alSeleccionarResolucion(archivo: File, entrada: InputAnexoUploadComponent): void {
    const error = errorDeArchivo(archivo, FORMATOS_RESOLUCION);
    if (error) {
      this.archivoResolucion = null;
      this.errores['resolucion'] = error;
      entrada.reset();
      return;
    }
    this.archivoResolucion = archivo;
    delete this.errores['resolucion'];
  }

  alQuitarResolucion(): void {
    this.archivoResolucion = null;
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

  private columnasVisibles(asignaturas: AsignaturaCancelacionDTORespuesta[]): ColumnaAsignatura[] {
    if (this.rol !== ROLES_ETIQUETA.ESTUDIANTE) return COLUMNAS;
    return COLUMNAS.filter(
      columna => COLUMNAS_FIJAS.includes(columna.titulo) || asignaturas.some(a => columna.valor(a) !== null)
    );
  }

  private ejecutar(accion: AccionAcademica): Observable<unknown> {
    const uuid = this.uuidSolicitud;
    const observacion = this.observacion.trim();
    const opcional = observacion === '' ? null : observacion;
    switch (accion) {
      case ACCIONES_ACADEMICAS.RECHAZAR_FUNCIONARIO:
        return this.subirAdicional().pipe(
          switchMap(() => this.cancelacionMatriculaService.rechazarPorFuncionario(uuid, { observacion }))
        );
      case ACCIONES_ACADEMICAS.REMITIR_DECANO:
        return this.cancelacionMatriculaService.remitirADecano(uuid, {
          observacion: opcional,
          evaluaciones: this.armarEvaluaciones()
        });
      case ACCIONES_ACADEMICAS.APROBAR_DECANO:
        return this.cancelacionMatriculaService.aprobarPorDecano(uuid, { observacion: opcional });
      case ACCIONES_ACADEMICAS.RECHAZAR_DECANO:
        return this.cancelacionMatriculaService.rechazarPorDecano(uuid, { observacion: opcional });
      default:
        return this.subirResolucion().pipe(
          switchMap(() => this.subirAdicional()),
          switchMap(() => this.cancelacionMatriculaService.enviarRespuesta(uuid, { observacion: opcional }))
        );
    }
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

  private subirResolucion(): Observable<void> {
    if (!this.archivoResolucion) return of(undefined);
    return this.solicitudAcademicaService.adjuntarResolucion(this.uuidSolicitud, this.archivoResolucion).pipe(
      tap(() => {
        this.resolucionSubida = true;
        this.archivoResolucion = null;
      }),
      map(() => undefined)
    );
  }

  private armarEvaluaciones(): EvaluacionAsignaturaDTOPeticion[] {
    return this.asignaturas.map(a => {
      const evaluacion = this.evaluaciones[a.uuidAsignaturaSolicitud];
      return {
        asignaturaSolicitudUuid: a.uuidAsignaturaSolicitud,
        numeroFaltas: evaluacion.faltas.trim() || null,
        nota: evaluacion.nota.trim() || null,
        situacionMatriculaUuid: evaluacion.situacion,
        situacionCancelarUuid: evaluacion.cancelar
      };
    });
  }

  private validar(accion: AccionAcademica): Record<string, string> {
    const errores: Record<string, string> = {};
    const errorObservacion = errorDeObservacion(accion, this.observacion);
    if (errorObservacion) errores['observacion'] = errorObservacion;

    if (accion === ACCIONES_ACADEMICAS.ENVIAR_RESPUESTA && !this.archivoResolucion && !this.hayResolucion) {
      errores['resolucion'] = 'Debes subir el escaneo de la Resolución firmada (PDF).';
    }

    if (accion === ACCIONES_ACADEMICAS.REMITIR_DECANO) Object.assign(errores, this.erroresDeEvaluacion());
    return errores;
  }

  private erroresDeEvaluacion(): Record<string, string> {
    const errores: Record<string, string> = {};
    for (const a of this.asignaturas) {
      const evaluacion = this.evaluaciones[a.uuidAsignaturaSolicitud];
      const errorFaltas = errorDeFaltas(evaluacion?.faltas);
      const errorNota = errorDeNotaAsignatura(evaluacion?.nota);
      if (errorFaltas) errores[`faltas-${a.uuidAsignaturaSolicitud}`] = errorFaltas;
      if (errorNota) errores[`nota-${a.uuidAsignaturaSolicitud}`] = errorNota;
    }
    return errores;
  }

  private asegurarSituaciones(alListo: () => void): void {
    if (this.opcionesSituacion.length > 0 && this.opcionesSituacionCancelar.length > 0) {
      alListo();
      return;
    }
    this.cargandoSituaciones = true;
    this.catalogoAcademicoService
      .getSituaciones()
      .pipe(finalize(() => (this.cargandoSituaciones = false)))
      .subscribe({
        next: situaciones => {
          this.opcionesSituacion = opcionesDeSituacion(situaciones, AMBITOS_SITUACION.MATRICULA);
          this.opcionesSituacionCancelar = opcionesDeSituacion(situaciones, AMBITOS_SITUACION.CANCELACION);
          if (this.opcionesSituacion.length === 0 || this.opcionesSituacionCancelar.length === 0) {
            this.accionActiva = null;
            this.errorHandlerService.handleError(
              { message: 'El catálogo de situaciones académicas está vacío' },
              'Error',
              'No se pudieron cargar las situaciones académicas'
            );
            return;
          }
          alListo();
        },
        error: err => {
          this.accionActiva = null;
          this.errorHandlerService.handleError(err, 'Error', 'No se pudieron cargar las situaciones académicas');
        }
      });
  }
}
