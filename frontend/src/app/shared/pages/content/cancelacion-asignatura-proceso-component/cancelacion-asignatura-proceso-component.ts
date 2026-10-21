import { Component, Input, OnChanges, SimpleChanges, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { EMPTY, Observable, finalize, map, of, switchMap, tap } from 'rxjs';
import { GenericDialogFormComponent } from '../../../generic-dialog-form-component/generic-dialog-form-component';
import { SimpleButtonComponent } from '../../../buttons/simple-button-component/simple-button-component';
import { InputTextTareaComponent } from '../../../inputs/input-text-tarea-component/input-text-tarea-component';
import { InputSelectComponent } from '../../../inputs/input-select-component/input-select-component';
import { InputAnexoUploadComponent } from '../../../inputs/input-anexo-upload-component/input-anexo-upload-component';
import { CancelacionAsignaturaService } from '../../../../core/services/cancelacion-asignatura-service';
import { SolicitudAcademicaService } from '../../../../core/services/solicitud-academica-service';
import { CatalogoAcademicoService } from '../../../../core/services/catalogo-academico-service';
import { ErrorHandlerService } from '../../../../core/services/error-handler-service';
import { ToastService } from '../../../../core/services/toast-service';
import { SolicitudAcademicaDetalleDTORespuesta } from '../../../../core/models/SolicitudAcademica/DTOResponse/SolicitudAcademicaDetalleDTORespuesta';
import { CancelacionAsignaturaDetalleDTORespuesta } from '../../../../core/models/CancelacionAsignatura/DTOResponse/CancelacionAsignaturaDetalleDTORespuesta';
import { AsignaturaSolicitadaDTORespuesta } from '../../../../core/models/CancelacionAsignatura/DTOResponse/AsignaturaSolicitadaDTORespuesta';
import { EvaluacionCancelacionAsignaturaDTOPeticion } from '../../../../core/models/CancelacionAsignatura/DTORequest/EvaluacionCancelacionAsignaturaDTOPeticion';
import { DecisionAsignaturaDTOPeticion } from '../../../../core/models/CancelacionAsignatura/DTORequest/DecisionAsignaturaDTOPeticion';
import { AjusteAsignaturaDTOPeticion } from '../../../../core/models/CancelacionAsignatura/DTORequest/AjusteAsignaturaDTOPeticion';
import { AprobacionCancelacionAsignaturaDTOPeticion } from '../../../../core/models/CancelacionAsignatura/DTORequest/AprobacionCancelacionAsignaturaDTOPeticion';
import {
  ACCIONES_ACADEMICAS,
  AccionAcademica,
  ETAPAS_ACADEMICAS,
  FORMATOS_RESOLUCION,
  MAXIMO_CARACTERES_OBSERVACION,
  NOTA_MINIMA_PARA_CUMPLIR,
  ETAPAS_FINALES,
  PARAMETRO_RESULTADO,
  RESULTADOS_FILA,
  ROLES_ETIQUETA,
  ResultadoFila,
  resultadoFilaValido,
  RolEtiqueta
} from '../../../../core/constantes/procesos-academicos';
import {
  ETIQUETAS_FINALES_ESTUDIANTE,
  NOTA_APROBAR_DECANO,
  RUTAS_ENCABEZADO_ASIGNATURAS,
  campoObservacion,
  esEtiqueta,
  etiquetaCampoObservacion
} from '../../../../core/constantes/etiquetas-academicas';
import { NOTA_SIMCA } from '../../../../core/constantes/textos-cancelacion-matricula';
import { CeldaEncabezado, agruparEncabezados } from '../../../../core/utils/encabezado-agrupado';
import { ResumenSolicitudComponent } from '../../../others/resumen-solicitud-component/resumen-solicitud-component';
import { ComunicacionRespuestaComponent } from '../../../others/comunicacion-respuesta-component/comunicacion-respuesta-component';
import { NotificacionResolucionComponent } from '../../../others/notificacion-resolucion-component/notificacion-resolucion-component';
import {
  ComunicacionEnEdicion,
  MAXIMO_MENSAJE_INICIAL,
  errorDeMensajeInicial,
  erroresDeProgramacion,
  programacionAPeticion,
  programacionVacia
} from '../../../../core/utils/notificacion-resolucion';
import {
  comoTipoAnexo,
  errorDeArchivo,
  errorDeFaltas,
  errorDeNotaAsignatura,
  errorDeTexto,
  erroresDeCampos,
  numeroDeValor
} from '../../../../core/utils/validaciones-academicas';
import { AMBITOS_SITUACION, OpcionSituacion, opcionesDeSituacion, textoSituacion } from '../../../../core/utils/situaciones-academicas';

interface EvaluacionEnEdicion {
  faltas: string;
  nota: string;
  situacion: string | null;
  cancelar: string | null;
  cumple: boolean | null;
}

interface AjusteEnEdicion {
  situacion: string | null;
}

type ModoDecision = 'aceptar' | 'modificar';

type CampoEvaluacion = 'faltas' | 'nota' | 'situacion' | 'cancelar' | 'cumple' | 'decision';

interface Columna {
  titulo: string;
  campo?: CampoEvaluacion;
  valor: (a: AsignaturaSolicitadaDTORespuesta) => string | null;
}

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
  [ACCIONES_ACADEMICAS.RECHAZAR_DECANO]: 'Rechazar solicitud completa',
  [ACCIONES_ACADEMICAS.ENVIAR_RESPUESTA]: 'Enviar respuesta'
};

const TITULOS_DIALOGO: Partial<Record<AccionAcademica, string>> = {
  [ACCIONES_ACADEMICAS.RECHAZAR_FUNCIONARIO]: 'Rechazar la solicitud',
  [ACCIONES_ACADEMICAS.REMITIR_DECANO]: 'Remitir la solicitud al Decano',
  [ACCIONES_ACADEMICAS.RECHAZAR_DECANO]: 'Rechazar la solicitud completa',
  [ACCIONES_ACADEMICAS.ENVIAR_RESPUESTA]: 'Enviar la respuesta al estudiante'
};

const TEXTOS_BOTON_DIALOGO: Partial<Record<AccionAcademica, string>> = {
  [ACCIONES_ACADEMICAS.RECHAZAR_FUNCIONARIO]: 'Rechazar solicitud',
  [ACCIONES_ACADEMICAS.REMITIR_DECANO]: 'Remitir al Decano',
  [ACCIONES_ACADEMICAS.RECHAZAR_DECANO]: 'Rechazar solicitud',
  [ACCIONES_ACADEMICAS.ENVIAR_RESPUESTA]: 'Enviar respuesta'
};

const TITULOS_DECISION: Record<ModoDecision, string> = {
  aceptar: 'Aceptar la evaluación del Funcionario',
  modificar: 'Enviar la decisión modificada'
};

const TEXTOS_BOTON_DECISION: Record<ModoDecision, string> = {
  aceptar: 'Aceptar evaluación',
  modificar: 'Enviar decisión'
};

const MENSAJES_EXITO: Partial<Record<AccionAcademica, string>> = {
  [ACCIONES_ACADEMICAS.RECHAZAR_FUNCIONARIO]: 'La solicitud quedó rechazada.',
  [ACCIONES_ACADEMICAS.REMITIR_DECANO]: 'La solicitud fue remitida al Decano.',
  [ACCIONES_ACADEMICAS.APROBAR_DECANO]: 'La decisión por asignatura quedó registrada.',
  [ACCIONES_ACADEMICAS.RECHAZAR_DECANO]: 'La solicitud quedó rechazada.',
  [ACCIONES_ACADEMICAS.ENVIAR_RESPUESTA]: 'La respuesta quedó registrada y la comunicación de la Resolución quedó programada.'
};

const SIN_DATO = '—';

const ETAPAS_DECISION_DECANO: (string | undefined)[] = [ETAPAS_ACADEMICAS.APROBADA_POR_DECANO, ETAPAS_ACADEMICAS.RECHAZADA_POR_DECANO];

function textoSiNo(valor: boolean | null, si: string, no: string): string | null {
  if (valor === null || valor === undefined) return null;
  return valor ? si : no;
}

function textoLibre(valor: string | null): string | null {
  return valor && valor.trim() !== '' ? valor : null;
}

const COLUMNA_CODIGO: Columna = { titulo: 'Código', valor: a => a.codigoAsignatura };
const COLUMNA_NOMBRE: Columna = { titulo: 'Asignatura', valor: a => a.nombreAsignatura };
const COLUMNA_CANCELAR: Columna = { titulo: 'Situación al cancelar', campo: 'cancelar', valor: a => textoSituacion(a.situacionCancelar) };
const COLUMNA_DECISION: Columna = { titulo: 'Decisión del Decano', valor: a => textoSiNo(a.aprobadaPorDecano, 'Aprobada', 'No aprobada') };
const COLUMNA_OBSERVACION_DECISION: Columna = { titulo: 'Observación de la decisión', valor: a => textoLibre(a.observacionDecision) };
const COLUMNA_DECIDIR: Columna = { titulo: 'Decisión del Decano', campo: 'decision', valor: () => null };
const COLUMNA_SITUACION_ESTUDIANTE: Columna = { titulo: 'Situación académica al cancelar', valor: a => textoSituacion(a.situacionCancelar) };

const COLUMNAS_ESTUDIANTE_FINAL: Columna[] = [COLUMNA_CODIGO, COLUMNA_NOMBRE, COLUMNA_DECISION, COLUMNA_SITUACION_ESTUDIANTE];
const COLUMNAS_ESTUDIANTE_EN_CURSO: Columna[] = [COLUMNA_CODIGO, COLUMNA_NOMBRE];
const COLUMNAS_COMPLETAS: Columna[] = [
  COLUMNA_CODIGO,
  COLUMNA_NOMBRE,
  { titulo: 'Faltas', campo: 'faltas', valor: a => textoLibre(a.numeroFaltas) },
  { titulo: 'Nota', campo: 'nota', valor: a => textoLibre(a.nota) },
  { titulo: 'Situación en la matrícula', campo: 'situacion', valor: a => textoSituacion(a.situacionMatricula) },
  COLUMNA_CANCELAR,
  { titulo: 'Cumple condiciones', campo: 'cumple', valor: a => textoSiNo(a.cumpleCondiciones, 'Sí', 'No') },
  COLUMNA_DECISION,
  COLUMNA_OBSERVACION_DECISION
];
const COLUMNAS_DECANO: Columna[] = COLUMNAS_COMPLETAS.filter(c => c !== COLUMNA_CANCELAR && c !== COLUMNA_OBSERVACION_DECISION);
const COLUMNAS_EDICION: Columna[] = COLUMNAS_COMPLETAS.filter(c => c.campo || c === COLUMNA_CODIGO || c === COLUMNA_NOMBRE);
const COLUMNAS_FIJAS = new Set([COLUMNA_CODIGO.titulo, COLUMNA_NOMBRE.titulo]);
const COLUMNAS_FIJAS_EVALUACION = new Set([
  ...COLUMNAS_FIJAS,
  ...COLUMNAS_COMPLETAS.filter(c => c.campo && c.campo !== 'cumple').map(c => c.titulo)
]);

@Component({
  selector: 'app-cancelacion-asignatura-proceso-component',
  imports: [
    GenericDialogFormComponent,
    SimpleButtonComponent,
    InputTextTareaComponent,
    InputSelectComponent,
    InputAnexoUploadComponent,
    ResumenSolicitudComponent,
    ComunicacionRespuestaComponent,
    NotificacionResolucionComponent
  ],
  templateUrl: './cancelacion-asignatura-proceso-component.html',
  styleUrls: ['../estilos-academicos.css', '../tabla-asignaturas.css']
})
export class CancelacionAsignaturaProcesoComponent implements OnChanges {
  @Input({ required: true }) uuidSolicitud!: string;
  @Input({ required: true }) solicitud!: SolicitudAcademicaDetalleDTORespuesta;
  @Input({ required: true }) rol!: RolEtiqueta;
  @Input({ required: true }) recargar!: () => void;

  readonly acciones = ACCIONES_ACADEMICAS;
  readonly maximoObservacion = MAXIMO_CARACTERES_OBSERVACION;
  readonly tipoResolucion = comoTipoAnexo('resolucion', 'Resolución sin firmar por el estudiante', FORMATOS_RESOLUCION, true);
  readonly notaAprobarDecano = NOTA_APROBAR_DECANO;
  readonly notaSimca = NOTA_SIMCA;
  readonly sinDato = SIN_DATO;
  readonly columnaDecision = COLUMNA_DECISION;

  private readonly ruta = inject(ActivatedRoute, { optional: true });
  readonly columnaNombre = COLUMNA_NOMBRE;
  readonly resultadoPedido = toSignal(
    (this.ruta?.queryParamMap ?? EMPTY).pipe(map(parametros => resultadoFilaValido(parametros.get(PARAMETRO_RESULTADO)))),
    { initialValue: resultadoFilaValido(this.ruta?.snapshot?.queryParamMap?.get(PARAMETRO_RESULTADO)) }
  );

  detalle: CancelacionAsignaturaDetalleDTORespuesta | null = null;
  columnas: Columna[] = [];
  encabezadoAgrupado: CeldaEncabezado[][] = [];
  faltanDatosTabla = false;
  cargando = false;
  errorCarga = false;

  opcionesSituacion: OpcionSituacion[] = [];
  opcionesSituacionCancelar: OpcionSituacion[] = [];
  cargandoSituaciones = false;

  accionActiva: AccionAcademica | null = null;
  modoDecision: ModoDecision | null = null;
  dialogoVisible = false;
  enviando = false;
  observacion = '';
  archivoResolucion: File | null = null;
  resolucionSubida = false;
  evaluaciones: Record<string, EvaluacionEnEdicion> = {};
  decisiones: Record<string, boolean> = {};
  ajustes: Record<string, AjusteEnEdicion> = {};
  errores: Record<string, string> = {};
  erroresServidor: string[] = [];
  comunicacion: ComunicacionEnEdicion | null = null;
  sufijoResolucion = '';
  maximoMensaje = MAXIMO_MENSAJE_INICIAL;
  cargandoPlantilla = false;

  constructor(
    private cancelacionAsignaturaService: CancelacionAsignaturaService,
    private solicitudAcademicaService: SolicitudAcademicaService,
    private catalogoAcademicoService: CatalogoAcademicoService,
    private errorHandlerService: ErrorHandlerService,
    private toastService: ToastService
  ) {}

  ngOnChanges(cambios: SimpleChanges): void {
    if (cambios['uuidSolicitud'] || cambios['solicitud']) {
      this.resolucionSubida = false;
      this.cargarDetalle();
    } else if (cambios['rol'] && this.detalle) {
      this.armarTabla(this.detalle);
    }
  }

  get accionesVisibles(): AccionAcademica[] {
    if (this.rol === ROLES_ETIQUETA.ESTUDIANTE) return [];
    const disponibles = this.solicitud?.accionesDisponibles ?? [];
    return ACCIONES_DEL_PROCESO.filter(accion => disponibles.includes(accion));
  }

  get muestraPanelNotificacion(): boolean {
    return this.rol === ROLES_ETIQUETA.FUNCIONARIO && this.esFinal;
  }

  get muestraNotaSimca(): boolean {
    return this.rol !== ROLES_ETIQUETA.ESTUDIANTE;
  }

  get asignaturas(): AsignaturaSolicitadaDTORespuesta[] {
    return this.detalle?.asignaturas ?? [];
  }

  get esFinal(): boolean {
    return ETAPAS_FINALES.includes(this.solicitud?.etapaCodigo ?? '');
  }

  get resultadoResaltado(): ResultadoFila | null {
    const pedido = this.resultadoPedido();
    if (!pedido || !this.esFinal || this.rol !== ROLES_ETIQUETA.FUNCIONARIO) return null;
    return this.asignaturas.some(a => this.resultadoDe(a) === pedido) ? pedido : null;
  }

  get textoResaltado(): string {
    const plural = this.resultadoResaltado === RESULTADOS_FILA.APROBADA ? 'aprobadas' : 'rechazadas';
    return `Se resaltan las asignaturas ${plural} de la solicitud`;
  }

  get asignaturasTabla(): AsignaturaSolicitadaDTORespuesta[] {
    const resaltado = this.resultadoResaltado;
    if (!resaltado) return this.asignaturas;
    return [
      ...this.asignaturas.filter(a => this.resultadoDe(a) === resaltado),
      ...this.asignaturas.filter(a => this.resultadoDe(a) !== resaltado)
    ];
  }

  resultadoDe(a: AsignaturaSolicitadaDTORespuesta): ResultadoFila {
    return a.aprobadaPorDecano === true ? RESULTADOS_FILA.APROBADA : RESULTADOS_FILA.RECHAZADA;
  }

  esResaltada(a: AsignaturaSolicitadaDTORespuesta): boolean {
    return this.resultadoResaltado !== null && this.resultadoDe(a) === this.resultadoResaltado;
  }

  esAtenuada(a: AsignaturaSolicitadaDTORespuesta): boolean {
    return this.resultadoResaltado !== null && this.resultadoDe(a) !== this.resultadoResaltado;
  }

  get tituloDialogo(): string {
    if (this.accionActiva === ACCIONES_ACADEMICAS.APROBAR_DECANO) return TITULOS_DECISION[this.modoDecision ?? 'aceptar'];
    return this.accionActiva ? TITULOS_DIALOGO[this.accionActiva] ?? '' : '';
  }

  get textoBotonDialogo(): string {
    if (this.accionActiva === ACCIONES_ACADEMICAS.APROBAR_DECANO) return TEXTOS_BOTON_DECISION[this.modoDecision ?? 'aceptar'];
    return this.accionActiva ? TEXTOS_BOTON_DIALOGO[this.accionActiva] ?? 'Confirmar' : 'Confirmar';
  }

  get hayResolucion(): boolean {
    return this.solicitud?.tieneResolucion || this.resolucionSubida;
  }

  get campoObservacion() {
    return campoObservacion(this.accionActiva);
  }

  get etiquetaObservacion(): string {
    const campo = this.campoObservacion;
    return campo ? etiquetaCampoObservacion(campo) : '';
  }

  get observacionObligatoria(): boolean {
    if (this.accionActiva === ACCIONES_ACADEMICAS.ENVIAR_RESPUESTA) return this.respuestaExigeObservacion;
    return this.campoObservacion?.obligatoria ?? false;
  }

  get cambiadasPorDecano(): AsignaturaSolicitadaDTORespuesta[] {
    if (this.rol !== ROLES_ETIQUETA.FUNCIONARIO) return [];
    if (!ETAPAS_DECISION_DECANO.includes(this.solicitud?.etapaCodigo)) return [];
    return this.asignaturas.filter(a => a.decisionCambiadaPorDecano === true);
  }

  get ningunaCumple(): boolean {
    const lista = Object.values(this.evaluaciones);
    return lista.length > 0 && lista.every(e => e.cumple === false);
  }

  get editable(): boolean {
    return this.accionesVisibles.includes(ACCIONES_ACADEMICAS.REMITIR_DECANO);
  }

  get decide(): boolean {
    return this.accionesVisibles.includes(ACCIONES_ACADEMICAS.APROBAR_DECANO);
  }

  get hayCambiosDecano(): boolean {
    return this.asignaturas.some(a => this.decisiones[a.uuidAsignaturaSolicitud] !== (a.cumpleCondiciones === true));
  }

  get todasRechazadas(): boolean {
    return this.asignaturas.length > 0 && this.asignaturas.every(a => !this.decisiones[a.uuidAsignaturaSolicitud]);
  }

  get asignaturasConAjuste(): AsignaturaSolicitadaDTORespuesta[] {
    if (this.solicitud?.etapaCodigo !== ETAPAS_ACADEMICAS.APROBADA_POR_DECANO) return [];
    return this.asignaturas.filter(
      a => a.aprobadaPorDecano === true && (a.decisionCambiadaPorDecano === true || !a.situacionCancelar)
    );
  }

  get respuestaExigeObservacion(): boolean {
    return (
      this.solicitud?.etapaCodigo === ETAPAS_ACADEMICAS.RECHAZADA_POR_DECANO ||
      this.asignaturas.some(a => a.aprobadaPorDecano === false)
    );
  }

  valorDe(evento: Event): string {
    return (evento.target as HTMLInputElement | HTMLSelectElement).value;
  }

  valorCumple(valor: string): boolean | null {
    return valor === 'si' ? true : valor === 'no' ? false : null;
  }

  cambiarDecision(uuid: string, evento: Event): void {
    this.decisiones[uuid] = (evento.target as HTMLInputElement).checked;
  }

  decisionDistinta(a: AsignaturaSolicitadaDTORespuesta): boolean {
    return this.decisiones[a.uuidAsignaturaSolicitud] !== (a.cumpleCondiciones === true);
  }

  marcaCambioDecano(columna: Columna, a: AsignaturaSolicitadaDTORespuesta): boolean {
    return columna === COLUMNA_DECISION && this.rol === ROLES_ETIQUETA.FUNCIONARIO && a.decisionCambiadaPorDecano === true;
  }

  errorCelda(columna: Columna, a: AsignaturaSolicitadaDTORespuesta): string {
    return this.editable && columna.campo ? this.errores[`${columna.campo}-${a.uuidAsignaturaSolicitud}`] ?? '' : '';
  }

  esRechazo(accion: AccionAcademica): boolean {
    return accion === ACCIONES_ACADEMICAS.RECHAZAR_FUNCIONARIO || accion === ACCIONES_ACADEMICAS.RECHAZAR_DECANO;
  }

  etiquetaBoton(accion: AccionAcademica): string {
    return ETIQUETAS_BOTON[accion] ?? accion;
  }

  cargarDetalle(): void {
    if (!this.uuidSolicitud) return;
    this.cargando = true;
    this.errorCarga = false;
    this.cancelacionAsignaturaService
      .getDetalle(this.uuidSolicitud)
      .pipe(finalize(() => (this.cargando = false)))
      .subscribe({
        next: detalle => {
          this.detalle = detalle;
          this.armarTabla(detalle);
        },
        error: err => {
          this.detalle = null;
          this.columnas = [];
          this.encabezadoAgrupado = [];
          this.evaluaciones = {};
          this.decisiones = {};
          this.errorCarga = true;
          this.errorHandlerService.handleError(err, 'Error', 'No se pudo cargar la cancelación de asignatura');
        }
      });
  }

  abrirDecision(modo: ModoDecision): void {
    if (!this.decide) return;
    if (modo === 'modificar' && this.todasRechazadas) {
      this.abrir(ACCIONES_ACADEMICAS.RECHAZAR_DECANO);
      return;
    }
    this.abrir(ACCIONES_ACADEMICAS.APROBAR_DECANO, modo);
  }

  abrir(accion: AccionAcademica, modo: ModoDecision | null = null): void {
    if (!this.accionesVisibles.includes(accion) || this.cargandoSituaciones || this.cargandoPlantilla) return;
    this.accionActiva = accion;
    this.modoDecision = modo;
    this.observacion = '';
    this.archivoResolucion = null;
    this.errores = {};
    this.erroresServidor = [];
    this.faltanDatosTabla = false;
    this.ajustes = {};
    this.comunicacion = null;
    if (accion === ACCIONES_ACADEMICAS.REMITIR_DECANO) {
      this.asegurarSituaciones(() => this.abrirRemision());
      return;
    }
    if (accion === ACCIONES_ACADEMICAS.ENVIAR_RESPUESTA) {
      this.abrirRespuesta();
      return;
    }
    this.dialogoVisible = true;
  }

  private abrirRemision(): void {
    const errores: Record<string, string> = {};
    this.validarEvaluaciones(errores);
    this.errores = errores;
    if (Object.keys(errores).length > 0) {
      this.faltanDatosTabla = true;
      this.accionActiva = null;
      return;
    }
    this.dialogoVisible = true;
  }

  private abrirRespuesta(): void {
    this.cargandoPlantilla = true;
    this.cancelacionAsignaturaService
      .getPlantillaNotificacion(this.uuidSolicitud)
      .pipe(finalize(() => (this.cargandoPlantilla = false)))
      .subscribe({
        next: plantilla => {
          this.sufijoResolucion = plantilla.sufijoResolucion ?? '';
          this.maximoMensaje = plantilla.maximoMensaje || MAXIMO_MENSAJE_INICIAL;
          this.comunicacion = {
            enviarMensaje: true,
            mensaje: plantilla.mensajeInicial ?? '',
            programacion: programacionVacia(plantilla.anioResolucion, plantilla.decisionPropuesta ?? '')
          };
          this.abrirRespuestaConPlantilla();
        },
        error: err => {
          this.accionActiva = null;
          this.errorHandlerService.handleError(err, 'Error', 'No se pudo cargar la plantilla de la comunicación');
        }
      });
  }

  private abrirRespuestaConPlantilla(): void {
    const conAjuste = this.asignaturasConAjuste;
    this.ajustes = Object.fromEntries(
      conAjuste.map(a => [a.uuidAsignaturaSolicitud, { situacion: a.situacionCancelar?.uuidSituacionAcademica ?? null }])
    );
    if (conAjuste.length > 0) {
      this.asegurarSituaciones(() => (this.dialogoVisible = true));
      return;
    }
    this.dialogoVisible = true;
  }

  cerrar(visible: boolean): void {
    if (visible || this.enviando) return;
    this.dialogoVisible = false;
    this.accionActiva = null;
    this.modoDecision = null;
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
    this.ejecutar(accion)
      .pipe(finalize(() => (this.enviando = false)))
      .subscribe({
        next: () => {
          this.toastService.showSuccess('Listo', MENSAJES_EXITO[accion] ?? 'Acción realizada.');
          this.dialogoVisible = false;
          this.accionActiva = null;
          this.modoDecision = null;
          this.recargar();
        },
        error: err => {
          this.erroresServidor = Object.entries(erroresDeCampos(err)).map(([campo, mensaje]) => `${campo}: ${mensaje}`);
          this.errorHandlerService.handleError(err, 'Error', 'No se pudo completar la acción');
        }
      });
  }

  private armarTabla(detalle: CancelacionAsignaturaDetalleDTORespuesta): void {
    this.faltanDatosTabla = false;
    this.columnas = this.editable ? COLUMNAS_EDICION : this.columnasConDatos(detalle);
    if (this.decide) this.columnas = [...this.columnas.filter(c => c !== COLUMNA_DECISION), COLUMNA_DECIDIR];
    this.encabezadoAgrupado = agruparEncabezados(this.columnas.map(columna => columna.titulo), RUTAS_ENCABEZADO_ASIGNATURAS);
    this.evaluaciones = this.editable
      ? Object.fromEntries(detalle.asignaturas.map(a => [a.uuidAsignaturaSolicitud, this.evaluacionInicial(a)]))
      : {};
    this.decisiones = this.decide
      ? Object.fromEntries(detalle.asignaturas.map(a => [a.uuidAsignaturaSolicitud, a.cumpleCondiciones === true]))
      : {};
    if (this.editable) this.asegurarSituaciones(() => undefined);
  }

  private columnasConDatos(detalle: CancelacionAsignaturaDetalleDTORespuesta): Columna[] {
    const fijas = this.rol === ROLES_ETIQUETA.ESTUDIANTE ? COLUMNAS_FIJAS : COLUMNAS_FIJAS_EVALUACION;
    const hayAprobadas = detalle.asignaturas.some(a => a.aprobadaPorDecano === true);
    return this.columnasDelRol().filter(
      columna =>
        fijas.has(columna.titulo) ||
        (columna === COLUMNA_SITUACION_ESTUDIANTE && hayAprobadas) ||
        detalle.asignaturas.some(a => columna.valor(a) !== null)
    );
  }

  private columnasDelRol(): Columna[] {
    if (this.rol === ROLES_ETIQUETA.DECANO) return COLUMNAS_DECANO;
    if (this.rol !== ROLES_ETIQUETA.ESTUDIANTE) return COLUMNAS_COMPLETAS;
    return esEtiqueta(this.solicitud?.etiqueta, ...ETIQUETAS_FINALES_ESTUDIANTE) ? COLUMNAS_ESTUDIANTE_FINAL : COLUMNAS_ESTUDIANTE_EN_CURSO;
  }

  private evaluacionInicial(a: AsignaturaSolicitadaDTORespuesta): EvaluacionEnEdicion {
    return {
      faltas: a.numeroFaltas ?? '',
      nota: a.nota ?? '',
      situacion: a.situacionMatricula?.uuidSituacionAcademica ?? null,
      cancelar: a.situacionCancelar?.uuidSituacionAcademica ?? null,
      cumple: a.cumpleCondiciones
    };
  }

  private ejecutar(accion: AccionAcademica): Observable<unknown> {
    const uuid = this.uuidSolicitud;
    const observacion = this.observacion.trim();
    const opcional = observacion === '' ? null : observacion;
    switch (accion) {
      case ACCIONES_ACADEMICAS.RECHAZAR_FUNCIONARIO:
        return this.cancelacionAsignaturaService.rechazarPorFuncionario(uuid, { observacion });
      case ACCIONES_ACADEMICAS.REMITIR_DECANO:
        return this.cancelacionAsignaturaService.remitirADecano(uuid, {
          observacion: opcional,
          evaluaciones: this.armarEvaluaciones()
        });
      case ACCIONES_ACADEMICAS.APROBAR_DECANO:
        return this.cancelacionAsignaturaService.aprobarPorDecano(uuid, this.armarAprobacion(opcional));
      case ACCIONES_ACADEMICAS.RECHAZAR_DECANO:
        return this.cancelacionAsignaturaService.rechazarPorDecano(uuid, { observacion: opcional });
      default: {
        const ajustes = this.armarAjustes();
        const comunicacion = this.comunicacion!;
        const cuerpo = {
          observacion: opcional,
          ...(ajustes.length > 0 ? { ajustes } : {}),
          enviarMensaje: comunicacion.enviarMensaje,
          mensaje: comunicacion.enviarMensaje ? comunicacion.mensaje.trim() : null,
          programacion: programacionAPeticion(comunicacion.programacion)
        };
        return this.subirResolucion().pipe(switchMap(() => this.cancelacionAsignaturaService.enviarRespuesta(uuid, cuerpo)));
      }
    }
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

  private armarEvaluaciones(): EvaluacionCancelacionAsignaturaDTOPeticion[] {
    return this.asignaturas.map(a => {
      const evaluacion = this.evaluaciones[a.uuidAsignaturaSolicitud];
      return {
        asignaturaSolicitudUuid: a.uuidAsignaturaSolicitud,
        numeroFaltas: evaluacion.faltas.trim() || null,
        nota: evaluacion.nota.trim() || null,
        situacionMatriculaUuid: evaluacion.situacion,
        situacionCancelarUuid: evaluacion.cancelar,
        cumpleCondiciones: evaluacion.cumple!,
        observacionEvaluacion: null
      };
    });
  }

  private armarAprobacion(observacion: string | null): AprobacionCancelacionAsignaturaDTOPeticion {
    if (this.modoDecision === 'modificar') return { decisiones: this.armarDecisiones(), observacion };
    return { aceptarEvaluacion: true, observacion };
  }

  private armarDecisiones(): DecisionAsignaturaDTOPeticion[] {
    return this.asignaturas.map(a => ({
      asignaturaSolicitudUuid: a.uuidAsignaturaSolicitud,
      aprobada: this.decisiones[a.uuidAsignaturaSolicitud] === true
    }));
  }

  private armarAjustes(): AjusteAsignaturaDTOPeticion[] {
    return this.asignaturasConAjuste.map(a => ({
      asignaturaSolicitudUuid: a.uuidAsignaturaSolicitud,
      situacionCancelarUuid: this.ajustes[a.uuidAsignaturaSolicitud].situacion,
      observacionDecision: null
    }));
  }

  private validar(accion: AccionAcademica): Record<string, string> {
    const errores: Record<string, string> = {};
    if (this.campoObservacion) {
      const errorObservacion = errorDeTexto(this.observacion, this.maximoObservacion, this.observacionObligatoria, 'La observación', true);
      if (errorObservacion) errores['observacion'] = errorObservacion;
    }

    if (accion === ACCIONES_ACADEMICAS.ENVIAR_RESPUESTA && !this.archivoResolucion && !this.hayResolucion) {
      errores['resolucion'] = 'Debes subir la Resolución sin firmar por el estudiante (PDF).';
    }

    if (accion === ACCIONES_ACADEMICAS.ENVIAR_RESPUESTA) {
      if (!this.comunicacion) {
        errores['programacion'] = 'Falta programar la comunicación de la Resolución.';
      } else {
        Object.assign(errores, erroresDeProgramacion(this.comunicacion.programacion));
        const errorMensaje = errorDeMensajeInicial(this.comunicacion, this.maximoMensaje);
        if (errorMensaje) errores['mensaje'] = errorMensaje;
      }
    }

    if (accion === ACCIONES_ACADEMICAS.REMITIR_DECANO) this.validarEvaluaciones(errores);
    return errores;
  }

  private validarEvaluaciones(errores: Record<string, string>): void {
    for (const a of this.asignaturas) {
      const uuid = a.uuidAsignaturaSolicitud;
      const evaluacion = this.evaluaciones[uuid];
      const errorFaltas = errorDeFaltas(evaluacion?.faltas);
      const errorNota = errorDeNotaAsignatura(evaluacion?.nota);
      const nota = errorNota ? null : numeroDeValor(evaluacion?.nota);
      if (errorFaltas) errores[`faltas-${uuid}`] = errorFaltas;
      if (errorNota) errores[`nota-${uuid}`] = errorNota;
      if (evaluacion?.cumple === null || evaluacion?.cumple === undefined) {
        errores[`cumple-${uuid}`] = 'Indica si cumple las condiciones.';
      } else if (evaluacion.cumple && nota !== null && nota < NOTA_MINIMA_PARA_CUMPLIR) {
        errores[`cumple-${uuid}`] = 'Con una nota numérica menor a 3.0 no puede marcarse que cumple las condiciones.';
      }
    }
    if (!Object.values(this.evaluaciones).some(e => e.cumple === true)) {
      errores['ninguna'] = 'Ninguna asignatura cumple las condiciones: en lugar de remitir la solicitud al Decano debes rechazarla.';
    }
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
