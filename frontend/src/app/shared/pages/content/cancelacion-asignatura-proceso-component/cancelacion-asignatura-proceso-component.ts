import { Component, Input, OnChanges, SimpleChanges, inject } from '@angular/core';
import { ResolucionesAsignaturaComponent } from '../../../others/resoluciones-asignatura-component/resoluciones-asignatura-component';
import { NgTemplateOutlet } from '@angular/common';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { EMPTY, Observable, finalize, map, of, switchMap, tap, throwError } from 'rxjs';
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
  ETAPAS_FINALES,
  PARAMETRO_RESULTADO,
  RESULTADOS_FILA,
  ROLES_ETIQUETA,
  ResultadoFila,
  resultadoFilaValido,
  RolEtiqueta,
  TAMANIO_MAXIMO_PAZ_Y_SALVOS_BYTES
} from '../../../../core/constantes/procesos-academicos';
import {
  CampoObservacion,
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
import { TarjetaPlegableDirective } from '../../../others/tarjeta-plegable-directive/tarjeta-plegable-directive';
import {
  ComunicacionEnEdicion,
  MAXIMO_MENSAJE_INICIAL,
  MAXIMO_TEXTO_COMUNICACION,
  errorDeMensajeInicial,
  erroresDeProgramacion,
  erroresDeTextos,
  programacionAPeticion,
  programacionVacia
} from '../../../../core/utils/notificacion-resolucion';
import {
  comoTipoAnexo,
  errorDeArchivo,
  errorDeFaltas,
  errorDeNotaAsignatura,
  errorDeTexto,
  erroresDeCampos
} from '../../../../core/utils/validaciones-academicas';
import { AMBITOS_SITUACION, OpcionSituacion, opcionesDeSituacion, textoSituacion } from '../../../../core/utils/situaciones-academicas';
import { ETIQUETA_ENVIAR_DECISION, accionDeDecisiones, decisionCompleta, esAccionDecisionDecano } from '../../../../core/utils/decision-decano';

interface EvaluacionEnEdicion {
  faltas: string;
  nota: string;
  situacion: string | null;
  cancelar: string | null;
}

interface AjusteEnEdicion {
  matricula: string | null;
  situacion: string | null;
  nuevaMatricula: string | null;
}

const CLAVES_AJUSTE: Record<keyof AjusteEnEdicion, string> = { matricula: 'matricula', situacion: 'cancelar', nuevaMatricula: 'nueva' };

type CampoEvaluacion = 'faltas' | 'nota' | 'situacion' | 'cancelar' | 'decision';

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
  [ACCIONES_ACADEMICAS.RECHAZAR_FUNCIONARIO]: 'No aprobar solicitud',
  [ACCIONES_ACADEMICAS.REMITIR_DECANO]: 'Remitir al Decano',
  [ACCIONES_ACADEMICAS.ENVIAR_RESPUESTA]: 'Enviar respuesta'
};

const TITULOS_DIALOGO: Partial<Record<AccionAcademica, string>> = {
  [ACCIONES_ACADEMICAS.RECHAZAR_FUNCIONARIO]: 'No aprobar la solicitud',
  [ACCIONES_ACADEMICAS.REMITIR_DECANO]: 'Remitir la solicitud al Decano',
  [ACCIONES_ACADEMICAS.APROBAR_DECANO]: 'Enviar la decisión del Decano',
  [ACCIONES_ACADEMICAS.RECHAZAR_DECANO]: 'No aprobar la solicitud completa',
  [ACCIONES_ACADEMICAS.ENVIAR_RESPUESTA]: 'Enviar la respuesta al estudiante'
};

const TEXTOS_BOTON_DIALOGO: Partial<Record<AccionAcademica, string>> = {
  [ACCIONES_ACADEMICAS.RECHAZAR_FUNCIONARIO]: 'No aprobar solicitud',
  [ACCIONES_ACADEMICAS.REMITIR_DECANO]: 'Remitir al Decano',
  [ACCIONES_ACADEMICAS.APROBAR_DECANO]: ETIQUETA_ENVIAR_DECISION,
  [ACCIONES_ACADEMICAS.RECHAZAR_DECANO]: ETIQUETA_ENVIAR_DECISION,
  [ACCIONES_ACADEMICAS.ENVIAR_RESPUESTA]: 'Enviar respuesta'
};

const MENSAJES_EXITO: Partial<Record<AccionAcademica, string>> = {
  [ACCIONES_ACADEMICAS.RECHAZAR_FUNCIONARIO]: 'La solicitud quedó no aprobada.',
  [ACCIONES_ACADEMICAS.REMITIR_DECANO]: 'La solicitud fue remitida al Decano.',
  [ACCIONES_ACADEMICAS.APROBAR_DECANO]: 'La decisión por asignatura quedó registrada.',
  [ACCIONES_ACADEMICAS.RECHAZAR_DECANO]: 'La solicitud quedó no aprobada.',
  [ACCIONES_ACADEMICAS.ENVIAR_RESPUESTA]: 'La respuesta quedó registrada y la comunicación de la Resolución quedó programada.'
};

const SIN_DATO = '—';

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
const COLUMNA_NUEVA_MATRICULA: Columna = { titulo: 'Al matricularla de nuevo', valor: a => textoSituacion(a.situacionNuevaMatricula) };
const COLUMNA_DECISION: Columna = { titulo: 'Decisión del Decano', valor: a => textoSiNo(a.aprobadaPorDecano, 'Aprobada', 'No aprobada') };
const COLUMNA_OBSERVACION_DECISION: Columna = { titulo: 'Observación de la decisión', valor: a => textoLibre(a.observacionDecision) };
const COLUMNA_DECIDIR: Columna = { titulo: 'Decisión del Decano', campo: 'decision', valor: () => null };
const COLUMNA_SITUACION_ESTUDIANTE: Columna = { titulo: 'Situación académica al cancelar', valor: a => textoSituacion(a.situacionCancelar) };
const COLUMNA_NUEVA_MATRICULA_ESTUDIANTE: Columna = {
  titulo: 'Estado académico de la asignatura cuando la matricule de nuevo',
  valor: a => textoSituacion(a.situacionNuevaMatricula)
};

const COLUMNAS_ESTUDIANTE_FINAL: Columna[] = [
  COLUMNA_CODIGO,
  COLUMNA_NOMBRE,
  COLUMNA_DECISION,
  COLUMNA_SITUACION_ESTUDIANTE,
  COLUMNA_NUEVA_MATRICULA_ESTUDIANTE
];
const COLUMNAS_ESTUDIANTE_EN_CURSO: Columna[] = [COLUMNA_CODIGO, COLUMNA_NOMBRE];
const COLUMNAS_COMPLETAS: Columna[] = [
  COLUMNA_CODIGO,
  COLUMNA_NOMBRE,
  { titulo: 'Faltas', campo: 'faltas', valor: a => textoLibre(a.numeroFaltas) },
  { titulo: 'Nota', campo: 'nota', valor: a => textoLibre(a.nota) },
  { titulo: 'Situación en la matrícula', campo: 'situacion', valor: a => textoSituacion(a.situacionMatricula) },
  COLUMNA_CANCELAR,
  COLUMNA_NUEVA_MATRICULA,
  COLUMNA_DECISION,
  COLUMNA_OBSERVACION_DECISION
];
const COLUMNAS_DECANO: Columna[] = COLUMNAS_COMPLETAS.filter(c => c !== COLUMNA_OBSERVACION_DECISION);
const COLUMNAS_EDICION: Columna[] = COLUMNAS_COMPLETAS.filter(c => c.campo || c === COLUMNA_CODIGO || c === COLUMNA_NOMBRE);
const COLUMNAS_FIJAS = new Set([COLUMNA_CODIGO.titulo, COLUMNA_NOMBRE.titulo]);
const COLUMNAS_FIJAS_DECANO = new Set([...COLUMNAS_FIJAS, ...COLUMNAS_COMPLETAS.filter(c => c.campo).map(c => c.titulo)]);
const COLUMNAS_FIJAS_EVALUACION = new Set([...COLUMNAS_FIJAS_DECANO, COLUMNA_NUEVA_MATRICULA.titulo]);

const CAMPO_DECISION_PARCIAL: CampoObservacion = {
  etiqueta: 'Comentario para el Funcionario Académico',
  obligatoria: true,
  aviso: 'Al menos una asignatura no fue aprobada; el Funcionario Académico recibirá este comentario.'
};

const CAMPO_RECHAZO_DECANO: CampoObservacion = {
  etiqueta: 'Comentario para el Funcionario Académico',
  obligatoria: true,
  aviso: 'El Funcionario Académico recibirá este comentario junto con la solicitud no aprobada.'
};

@Component({
  selector: 'app-cancelacion-asignatura-proceso-component',
  imports: [
    ResolucionesAsignaturaComponent,
    NgTemplateOutlet,
    GenericDialogFormComponent,
    SimpleButtonComponent,
    InputTextTareaComponent,
    InputSelectComponent,
    InputAnexoUploadComponent,
    ResumenSolicitudComponent,
    ComunicacionRespuestaComponent,
    NotificacionResolucionComponent,
    TarjetaPlegableDirective
  ],
  templateUrl: './cancelacion-asignatura-proceso-component.html',
  styleUrls: ['../estilos-academicos.css', '../tabla-asignaturas.css']
})
export class CancelacionAsignaturaProcesoComponent implements OnChanges {
  @Input({ required: true }) uuidSolicitud!: string;
  @Input({ required: true }) solicitud!: SolicitudAcademicaDetalleDTORespuesta;
  @Input({ required: true }) rol!: RolEtiqueta;
  @Input({ required: true }) recargar!: () => void;

  faltaFormatoDecano(accion: AccionAcademica): boolean {
    return (accion === ACCIONES_ACADEMICAS.ENVIAR_RESPUESTA || accion === ACCIONES_ACADEMICAS.ENVIAR_RECIBO)
      && !this.hayFormatoDecano;
  }

  get hayFormatoDecano(): boolean {
    return !!this.solicitud?.formatoFirmadoDecanoUuid || this.formatoDecanoSubido || !!this.archivoFormatoDecano;
  }

  readonly acciones = ACCIONES_ACADEMICAS;
  readonly maximoObservacion = MAXIMO_CARACTERES_OBSERVACION;
  readonly tipoFormatoDecano = comoTipoAnexo('formato-decano', 'Formato firmado por el Decano (PDF)', FORMATOS_RESOLUCION, true);
  readonly tipoResolucion = comoTipoAnexo('resolucion', 'Resolución sin firmar por el estudiante', FORMATOS_RESOLUCION, true);
  readonly notaAprobarDecano = NOTA_APROBAR_DECANO;
  readonly notaSimca = NOTA_SIMCA;
  readonly sinDato = SIN_DATO;
  readonly columnaDecision = COLUMNA_DECISION;
  readonly etiquetaEnviarDecision = ETIQUETA_ENVIAR_DECISION;

  private readonly ruta = inject(ActivatedRoute, { optional: true });
  readonly columnaNombre = COLUMNA_NOMBRE;
  readonly columnaNuevaMatricula = COLUMNA_NUEVA_MATRICULA;
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
  dialogoVisible = false;
  enviando = false;
  observacion = '';
  archivoResolucion: File | null = null;
  resolucionSubida = false;
  archivoFormatoDecano: File | null = null;
  errorFormatoDecano = '';
  formatoDecanoSubido = false;
  evaluaciones: Record<string, EvaluacionEnEdicion> = {};
  decisiones: Record<string, boolean | null> = {};
  ajustes: Record<string, AjusteEnEdicion> = {};
  errores: Record<string, string> = {};
  erroresServidor: string[] = [];
  comunicacion: ComunicacionEnEdicion | null = null;
  prefijoResolucion = '';
  maximoMensaje = MAXIMO_MENSAJE_INICIAL;
  maximoTexto = MAXIMO_TEXTO_COMUNICACION;
  nombreEstudiante = '';
  sugeridoComunicacion = '';
  sugeridoDarca = '';
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
      this.formatoDecanoSubido = false;
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

  get accionesBotones(): AccionAcademica[] {
    return this.accionesVisibles.filter(accion => !esAccionDecisionDecano(accion));
  }

  get esFuncionario(): boolean {
    return this.rol === ROLES_ETIQUETA.FUNCIONARIO;
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
    const plural = this.resultadoResaltado === RESULTADOS_FILA.APROBADA ? 'aprobadas' : 'no aprobadas';
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
    return this.accionActiva ? TITULOS_DIALOGO[this.accionActiva] ?? '' : '';
  }

  get textoBotonDialogo(): string {
    return this.accionActiva ? TEXTOS_BOTON_DIALOGO[this.accionActiva] ?? 'Confirmar' : 'Confirmar';
  }

  get hayResolucion(): boolean {
    return this.solicitud?.tieneResolucion || this.resolucionSubida;
  }

  get campoObservacion(): CampoObservacion | null {
    if (this.accionActiva === ACCIONES_ACADEMICAS.RECHAZAR_DECANO) return CAMPO_RECHAZO_DECANO;
    if (this.accionActiva === ACCIONES_ACADEMICAS.APROBAR_DECANO && this.listaDecisiones().includes(false)) return CAMPO_DECISION_PARCIAL;
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

  get editable(): boolean {
    return this.accionesVisibles.includes(ACCIONES_ACADEMICAS.REMITIR_DECANO);
  }

  get decide(): boolean {
    return this.accionesVisibles.some(accion => esAccionDecisionDecano(accion));
  }

  get decisionLista(): boolean {
    return decisionCompleta(this.listaDecisiones());
  }

  get todasRechazadas(): boolean {
    return this.decisionLista && this.asignaturas.every(a => this.decisiones[a.uuidAsignaturaSolicitud] === false);
  }

  get asignaturasConAjuste(): AsignaturaSolicitadaDTORespuesta[] {
    if (this.solicitud?.etapaCodigo !== ETAPAS_ACADEMICAS.APROBADA_POR_DECANO) return [];
    return this.asignaturas.filter(a => a.aprobadaPorDecano === true);
  }

  get ajustable(): boolean {
    return this.accionesVisibles.includes(ACCIONES_ACADEMICAS.ENVIAR_RESPUESTA) && this.asignaturasConAjuste.length > 0;
  }

  get asignaturasSinSituaciones(): number {
    return this.asignaturasConAjuste.filter(a => !this.ajusteCompleto(this.ajustes[a.uuidAsignaturaSolicitud])).length;
  }

  botonDeshabilitado(accion: AccionAcademica): boolean {
    if (!this.detalle || this.cargandoSituaciones || this.cargandoPlantilla) return true;
    if (this.faltaFormatoDecano(accion)) return true;
    return accion === ACCIONES_ACADEMICAS.ENVIAR_RESPUESTA && this.ajustable && this.asignaturasSinSituaciones > 0;
  }

  readonly ajustesResolucion = (): AjusteAsignaturaDTOPeticion[] => (this.ajustable ? this.armarAjustes() : []);

  get pideFormatoDecano(): boolean {
    const etapa = this.solicitud?.etapaCodigo;
    return this.esFuncionario && (etapa === ETAPAS_ACADEMICAS.APROBADA_POR_DECANO || etapa === ETAPAS_ACADEMICAS.RECHAZADA_POR_DECANO);
  }

  alSeleccionarFormatoDecano(archivo: File, entrada: InputAnexoUploadComponent): void {
    const error = errorDeArchivo(archivo, FORMATOS_RESOLUCION, TAMANIO_MAXIMO_PAZ_Y_SALVOS_BYTES);
    if (error) {
      this.archivoFormatoDecano = null;
      this.errorFormatoDecano = error;
      entrada.reset();
      return;
    }
    this.archivoFormatoDecano = archivo;
    this.errorFormatoDecano = '';
  }

  alQuitarFormatoDecano(): void {
    this.archivoFormatoDecano = null;
    this.errorFormatoDecano = '';
  }

  private subirFormatoDecano(): Observable<void> {
    const archivo = this.archivoFormatoDecano;
    if (!archivo) return of(undefined);
    return this.catalogoAcademicoService.getTiposAnexoPorTipo(this.solicitud.uuidTipoSolicitudAcademica).pipe(
      switchMap(tipos => {
        const tipo = (tipos ?? []).find(t => (t.nombre ?? '').trim().toLowerCase() === 'formato firmado por el decano');
        if (!tipo) return throwError(() => ({ message: 'El catálogo no tiene configurado el tipo de anexo "Formato firmado por el Decano".' }));
        return this.solicitudAcademicaService.adjuntarAnexo(this.uuidSolicitud, { archivo, tipoAnexo: tipo.uuidTipoAnexoAcademico });
      }),
      tap(() => {
        this.formatoDecanoSubido = true;
        this.archivoFormatoDecano = null;
      }),
      map(() => undefined)
    );
  }

  get muestraResoluciones(): boolean {
    const etapa = this.solicitud?.etapaCodigo;
    return this.esFuncionario && (etapa === ETAPAS_ACADEMICAS.APROBADA_POR_DECANO || etapa === ETAPAS_ACADEMICAS.RECHAZADA_POR_DECANO);
  }

  cambiarAjuste(uuid: string, campo: keyof AjusteEnEdicion, valor: string): void {
    const ajuste = this.ajustes[uuid];
    if (!ajuste) return;
    ajuste[campo] = valor || null;
    delete this.errores[`ajuste-${CLAVES_AJUSTE[campo]}-${uuid}`];
  }

  private ajusteCompleto(ajuste: AjusteEnEdicion | undefined): boolean {
    return !!ajuste?.matricula && !!ajuste.situacion && !!ajuste.nuevaMatricula;
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

  cambiarDecision(uuid: string, valor: string): void {
    this.decisiones[uuid] = valor === 'aprobar' ? true : valor === 'rechazar' ? false : null;
  }

  errorCelda(columna: Columna, a: AsignaturaSolicitadaDTORespuesta): string {
    const uuid = a.uuidAsignaturaSolicitud;
    if (this.ajustable && this.ajustes[uuid]) {
      const clave = columna === COLUMNA_NUEVA_MATRICULA ? 'nueva' : columna.campo === 'situacion' ? 'matricula'
        : columna.campo === 'cancelar' ? 'cancelar' : null;
      return clave ? this.errores[`ajuste-${clave}-${uuid}`] ?? '' : '';
    }
    return this.editable && columna.campo ? this.errores[`${columna.campo}-${uuid}`] ?? '' : '';
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

  decidirTodas(aprobada: boolean): void {
    if (!this.decide || !this.detalle) return;
    this.asignaturas.forEach(a => (this.decisiones[a.uuidAsignaturaSolicitud] = aprobada));
    this.abrirDecision();
  }

  abrirDecision(): void {
    if (!this.decide) return;
    const accion = accionDeDecisiones(this.listaDecisiones());
    if (accion) this.abrir(accion);
  }

  abrir(accion: AccionAcademica): void {
    if (!this.accionesVisibles.includes(accion) || this.cargandoSituaciones || this.cargandoPlantilla) return;
    this.accionActiva = accion;
    this.observacion = '';
    this.archivoResolucion = null;
    this.errores = {};
    this.erroresServidor = [];
    this.faltanDatosTabla = false;
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
          this.prefijoResolucion = plantilla.prefijoResolucion ?? '';
          this.maximoMensaje = plantilla.maximoMensaje || MAXIMO_MENSAJE_INICIAL;
          this.maximoTexto = plantilla.maximoTexto || MAXIMO_TEXTO_COMUNICACION;
          this.nombreEstudiante = plantilla.nombreEstudiante ?? '';
          this.sugeridoComunicacion = plantilla.textoComunicacion ?? '';
          this.sugeridoDarca = plantilla.textoDarca ?? '';
          this.comunicacion = {
            mensaje: plantilla.mensajeInicial ?? '',
            textoComunicacion: this.sugeridoComunicacion,
            textoDarca: this.sugeridoDarca,
            programacion: programacionVacia(plantilla.anioResolucion, plantilla.decisionPropuesta ?? '')
          };
          this.dialogoVisible = true;
        },
        error: err => {
          this.accionActiva = null;
          this.errorHandlerService.handleError(err, 'Error', 'No se pudo cargar la plantilla de la comunicación');
        }
      });
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
    this.ejecutar(accion)
      .pipe(finalize(() => (this.enviando = false)))
      .subscribe({
        next: () => {
          this.toastService.showSuccess('Operación realizada', MENSAJES_EXITO[accion] ?? 'Acción realizada.');
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

  private armarTabla(detalle: CancelacionAsignaturaDetalleDTORespuesta): void {
    this.faltanDatosTabla = false;
    this.columnas = this.editable ? COLUMNAS_EDICION : this.columnasConDatos(detalle);
    if (this.decide) this.columnas = [...this.columnas.filter(c => c !== COLUMNA_DECISION), COLUMNA_DECIDIR];
    this.encabezadoAgrupado = agruparEncabezados(this.columnas.map(columna => columna.titulo), RUTAS_ENCABEZADO_ASIGNATURAS);
    this.evaluaciones = this.editable
      ? Object.fromEntries(detalle.asignaturas.map(a => [a.uuidAsignaturaSolicitud, this.evaluacionInicial(a)]))
      : {};
    this.decisiones = this.decide
      ? Object.fromEntries(detalle.asignaturas.map(a => [a.uuidAsignaturaSolicitud, null]))
      : {};
    this.ajustes = this.ajustable ? Object.fromEntries(this.asignaturasConAjuste.map(a => [a.uuidAsignaturaSolicitud, this.ajusteInicial(a)])) : {};
    if (this.editable || this.ajustable) this.asegurarSituaciones(() => undefined);
  }

  private ajusteInicial(a: AsignaturaSolicitadaDTORespuesta): AjusteEnEdicion {
    return {
      matricula: a.situacionMatricula?.uuidSituacionAcademica ?? null,
      situacion: a.situacionCancelar?.uuidSituacionAcademica ?? null,
      nuevaMatricula: a.situacionNuevaMatricula?.uuidSituacionAcademica ?? null
    };
  }

  private columnasConDatos(detalle: CancelacionAsignaturaDetalleDTORespuesta): Columna[] {
    const fijas = this.rol === ROLES_ETIQUETA.ESTUDIANTE ? COLUMNAS_FIJAS
      : this.rol === ROLES_ETIQUETA.DECANO ? COLUMNAS_FIJAS_DECANO : COLUMNAS_FIJAS_EVALUACION;
    const hayAprobadas = detalle.asignaturas.some(a => a.aprobadaPorDecano === true);
    return this.columnasDelRol().filter(
      columna =>
        fijas.has(columna.titulo) ||
        ((columna === COLUMNA_SITUACION_ESTUDIANTE || columna === COLUMNA_NUEVA_MATRICULA_ESTUDIANTE) && hayAprobadas) ||
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
      cancelar: a.situacionCancelar?.uuidSituacionAcademica ?? null
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
          enviarMensaje: true,
          mensaje: comunicacion.mensaje.trim(),
          textoComunicacion: comunicacion.textoComunicacion.trim(),
          textoDarca: comunicacion.textoDarca.trim(),
          programacion: programacionAPeticion(comunicacion.programacion)
        };
        return this.subirFormatoDecano().pipe(
          switchMap(() => this.subirResolucion()),
          switchMap(() => this.cancelacionAsignaturaService.enviarRespuesta(uuid, cuerpo))
        );
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
        observacionEvaluacion: null
      };
    });
  }

  private armarAprobacion(observacion: string | null): AprobacionCancelacionAsignaturaDTOPeticion {
    return { decisiones: this.armarDecisiones(), observacion };
  }

  private listaDecisiones(): (boolean | null)[] {
    return this.asignaturas.map(a => this.decisiones[a.uuidAsignaturaSolicitud] ?? null);
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
      situacionMatriculaUuid: this.ajustes[a.uuidAsignaturaSolicitud].matricula,
      situacionCancelarUuid: this.ajustes[a.uuidAsignaturaSolicitud].situacion,
      situacionNuevaMatriculaUuid: this.ajustes[a.uuidAsignaturaSolicitud].nuevaMatricula,
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
      errores['resolucion'] = 'Se debe adjuntar la Resolución sin firmar por el estudiante (PDF).';
    }

    if (accion === ACCIONES_ACADEMICAS.ENVIAR_RESPUESTA) {
      if (!this.comunicacion) {
        errores['programacion'] = 'Falta programar la comunicación de la Resolución.';
      } else {
        Object.assign(errores, erroresDeProgramacion(this.comunicacion.programacion));
        const errorMensaje = errorDeMensajeInicial(this.comunicacion, this.maximoMensaje);
        if (errorMensaje) errores['mensaje'] = errorMensaje;
        Object.assign(errores, erroresDeTextos(this.comunicacion, this.maximoTexto));
      }
    }

    if (accion === ACCIONES_ACADEMICAS.ENVIAR_RESPUESTA) {
      for (const a of this.asignaturasConAjuste) {
        const uuid = a.uuidAsignaturaSolicitud;
        const ajuste = this.ajustes[uuid];
        if (!ajuste?.matricula) errores[`ajuste-matricula-${uuid}`] = 'Se debe seleccionar la situación en la matrícula.';
        if (!ajuste?.situacion) errores[`ajuste-cancelar-${uuid}`] = 'Se debe seleccionar la situación al cancelar.';
        if (!ajuste?.nuevaMatricula) errores[`ajuste-nueva-${uuid}`] = 'Se debe seleccionar la situación al matricularla de nuevo.';
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
      if (errorFaltas) errores[`faltas-${uuid}`] = errorFaltas;
      if (errorNota) errores[`nota-${uuid}`] = errorNota;
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
