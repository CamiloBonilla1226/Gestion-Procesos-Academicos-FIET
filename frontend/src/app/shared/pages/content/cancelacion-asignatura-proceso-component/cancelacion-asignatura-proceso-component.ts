import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { Observable, finalize, map, of, switchMap, tap } from 'rxjs';
import { GenericDialogFormComponent } from '../../../generic-dialog-form-component/generic-dialog-form-component';
import { TableGenericComponent, TableHeader } from '../../../table-generic-component/table-generic-component';
import { SimpleButtonComponent } from '../../../buttons/simple-button-component/simple-button-component';
import { InputTextComponent } from '../../../inputs/input-text-component/input-text-component';
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
import {
  ACCIONES_ACADEMICAS,
  AccionAcademica,
  ETAPAS_FINALES,
  FORMATOS_RESOLUCION,
  MAXIMO_CARACTERES_OBSERVACION,
  MAXIMO_CARACTERES_OBSERVACION_ASIGNATURA,
  NOTA_MINIMA_PARA_CUMPLIR,
  OPCIONES_SI_NO,
  ROLES_ETIQUETA,
  RolEtiqueta
} from '../../../../core/constantes/procesos-academicos';
import {
  comoTipoAnexo,
  errorDeArchivo,
  errorDeTexto,
  erroresDeCampos,
  leerFaltas,
  leerNota
} from '../../../../core/utils/validaciones-academicas';

interface EvaluacionEnEdicion {
  faltas: string;
  nota: string;
  situacion: string | null;
  cumple: boolean | null;
  observacion: string;
}

interface DecisionEnEdicion {
  aprobada: boolean | null;
  situacion: string | null;
  observacion: string;
}

interface Columna {
  titulo: string;
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
  [ACCIONES_ACADEMICAS.APROBAR_DECANO]: 'Decidir por asignatura',
  [ACCIONES_ACADEMICAS.RECHAZAR_DECANO]: 'Rechazar solicitud completa',
  [ACCIONES_ACADEMICAS.ENVIAR_RESPUESTA]: 'Enviar respuesta'
};

const TITULOS_DIALOGO: Partial<Record<AccionAcademica, string>> = {
  [ACCIONES_ACADEMICAS.RECHAZAR_FUNCIONARIO]: 'Rechazar la solicitud',
  [ACCIONES_ACADEMICAS.REMITIR_DECANO]: 'Remitir la solicitud al Decano',
  [ACCIONES_ACADEMICAS.APROBAR_DECANO]: 'Decisión del Decano por asignatura',
  [ACCIONES_ACADEMICAS.RECHAZAR_DECANO]: 'Rechazar la solicitud completa',
  [ACCIONES_ACADEMICAS.ENVIAR_RESPUESTA]: 'Enviar la respuesta al estudiante'
};

const MENSAJES_EXITO: Partial<Record<AccionAcademica, string>> = {
  [ACCIONES_ACADEMICAS.RECHAZAR_FUNCIONARIO]: 'La solicitud quedó rechazada.',
  [ACCIONES_ACADEMICAS.REMITIR_DECANO]: 'La solicitud fue remitida al Decano.',
  [ACCIONES_ACADEMICAS.APROBAR_DECANO]: 'La decisión por asignatura quedó registrada.',
  [ACCIONES_ACADEMICAS.RECHAZAR_DECANO]: 'La solicitud quedó rechazada.',
  [ACCIONES_ACADEMICAS.ENVIAR_RESPUESTA]: 'La respuesta fue enviada al estudiante.'
};

const SIN_REGISTRAR = 'Sin registrar';

function textoSiNo(valor: boolean | null, si: string, no: string): string | null {
  if (valor === null || valor === undefined) return null;
  return valor ? si : no;
}

function textoSituacion(situacion: { codigo: string; nombre: string } | null): string | null {
  return situacion ? `${situacion.codigo} - ${situacion.nombre}` : null;
}

function textoNota(nota: number | null): string | null {
  return nota === null || nota === undefined ? null : nota.toFixed(1).replace('.', ',');
}

function textoLibre(valor: string | null): string | null {
  return valor && valor.trim() !== '' ? valor : null;
}

const COLUMNA_CODIGO: Columna = { titulo: 'Código', valor: a => a.codigoAsignatura };
const COLUMNA_NOMBRE: Columna = { titulo: 'Asignatura', valor: a => a.nombreAsignatura };
const COLUMNA_DECISION: Columna = { titulo: 'Decisión del Decano', valor: a => textoSiNo(a.aprobadaPorDecano, 'Aprobada', 'No aprobada') };
const COLUMNA_OBSERVACION_DECISION: Columna = { titulo: 'Observación de la decisión', valor: a => textoLibre(a.observacionDecision) };

const COLUMNAS_ESTUDIANTE_FINAL: Columna[] = [COLUMNA_CODIGO, COLUMNA_NOMBRE, COLUMNA_DECISION, COLUMNA_OBSERVACION_DECISION];
const COLUMNAS_ESTUDIANTE_EN_CURSO: Columna[] = [COLUMNA_CODIGO, COLUMNA_NOMBRE];
const COLUMNAS_COMPLETAS: Columna[] = [
  COLUMNA_CODIGO,
  COLUMNA_NOMBRE,
  { titulo: 'Faltas', valor: a => (a.numeroFaltas === null || a.numeroFaltas === undefined ? null : String(a.numeroFaltas)) },
  { titulo: 'Nota', valor: a => textoNota(a.nota) },
  { titulo: 'Situación en la matrícula', valor: a => textoSituacion(a.situacionMatricula) },
  { titulo: 'Cumple condiciones', valor: a => textoSiNo(a.cumpleCondiciones, 'Sí', 'No') },
  { titulo: 'Observación de la evaluación', valor: a => textoLibre(a.observacionEvaluacion) },
  COLUMNA_DECISION,
  COLUMNA_OBSERVACION_DECISION,
  { titulo: 'Situación al cancelar', valor: a => textoSituacion(a.situacionCancelar) }
];
const COLUMNAS_FIJAS = new Set([COLUMNA_CODIGO.titulo, COLUMNA_NOMBRE.titulo]);

@Component({
  selector: 'app-cancelacion-asignatura-proceso-component',
  imports: [
    GenericDialogFormComponent,
    TableGenericComponent,
    SimpleButtonComponent,
    InputTextComponent,
    InputTextTareaComponent,
    InputSelectComponent,
    InputAnexoUploadComponent
  ],
  templateUrl: './cancelacion-asignatura-proceso-component.html'
})
export class CancelacionAsignaturaProcesoComponent implements OnChanges {
  @Input({ required: true }) uuidSolicitud!: string;
  @Input({ required: true }) solicitud!: SolicitudAcademicaDetalleDTORespuesta;
  @Input({ required: true }) rol!: RolEtiqueta;
  @Input({ required: true }) recargar!: () => void;

  readonly acciones = ACCIONES_ACADEMICAS;
  readonly opcionesSiNo = OPCIONES_SI_NO;
  readonly maximoObservacion = MAXIMO_CARACTERES_OBSERVACION;
  readonly maximoObservacionAsignatura = MAXIMO_CARACTERES_OBSERVACION_ASIGNATURA;
  readonly tipoResolucion = comoTipoAnexo('resolucion', 'Escaneo de la Resolución firmada', FORMATOS_RESOLUCION, true);
  readonly textoNota = textoNota;

  detalle: CancelacionAsignaturaDetalleDTORespuesta | null = null;
  encabezados: TableHeader[] = [];
  filasAsignaturas: Record<string, string>[] = [];
  cargando = false;
  errorCarga = false;

  opcionesSituacion: { label: string; value: string }[] = [];
  cargandoSituaciones = false;

  accionActiva: AccionAcademica | null = null;
  dialogoVisible = false;
  enviando = false;
  observacion = '';
  archivoResolucion: File | null = null;
  resolucionSubida = false;
  evaluaciones: Record<string, EvaluacionEnEdicion> = {};
  decisiones: Record<string, DecisionEnEdicion> = {};
  errores: Record<string, string> = {};
  erroresServidor: string[] = [];

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

  get asignaturas(): AsignaturaSolicitadaDTORespuesta[] {
    return this.detalle?.asignaturas ?? [];
  }

  get tituloDialogo(): string {
    return this.accionActiva ? TITULOS_DIALOGO[this.accionActiva] ?? '' : '';
  }

  get hayResolucion(): boolean {
    return this.solicitud?.tieneResolucion || this.resolucionSubida;
  }

  get ningunaCumple(): boolean {
    const lista = Object.values(this.evaluaciones);
    return lista.length > 0 && lista.every(e => e.cumple === false);
  }

  get ningunaAprobada(): boolean {
    const lista = Object.values(this.decisiones);
    return lista.length > 0 && lista.every(d => d.aprobada === false);
  }

  etiquetaBoton(accion: AccionAcademica): string {
    return ETIQUETAS_BOTON[accion] ?? accion;
  }

  textoCumple(valor: boolean | null): string {
    return textoSiNo(valor, 'Sí', 'No') ?? SIN_REGISTRAR;
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
          this.encabezados = [];
          this.filasAsignaturas = [];
          this.errorCarga = true;
          this.errorHandlerService.handleError(err, 'Error', 'No se pudo cargar la cancelación de asignatura');
        }
      });
  }

  abrir(accion: AccionAcademica): void {
    if (!this.accionesVisibles.includes(accion) || this.cargandoSituaciones) return;
    this.accionActiva = accion;
    this.observacion = '';
    this.archivoResolucion = null;
    this.errores = {};
    this.erroresServidor = [];
    this.evaluaciones = {};
    this.decisiones = {};
    if (accion === ACCIONES_ACADEMICAS.REMITIR_DECANO) {
      this.evaluaciones = Object.fromEntries(this.asignaturas.map(a => [a.uuidAsignaturaSolicitud, this.evaluacionInicial(a)]));
      this.abrirConSituaciones();
      return;
    }
    if (accion === ACCIONES_ACADEMICAS.APROBAR_DECANO) {
      this.decisiones = Object.fromEntries(this.asignaturas.map(a => [a.uuidAsignaturaSolicitud, this.decisionInicial(a)]));
      this.abrirConSituaciones();
      return;
    }
    this.dialogoVisible = true;
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

  private armarTabla(detalle: CancelacionAsignaturaDetalleDTORespuesta): void {
    const columnas = this.columnasDelRol().filter(
      columna => COLUMNAS_FIJAS.has(columna.titulo) || detalle.asignaturas.some(a => columna.valor(a) !== null)
    );
    this.encabezados = columnas.map(columna => ({ title: columna.titulo }));
    this.filasAsignaturas = detalle.asignaturas.map(a =>
      Object.fromEntries(columnas.map(columna => [columna.titulo, columna.valor(a) ?? SIN_REGISTRAR]))
    );
  }

  private columnasDelRol(): Columna[] {
    if (this.rol !== ROLES_ETIQUETA.ESTUDIANTE) return COLUMNAS_COMPLETAS;
    return ETAPAS_FINALES.includes(this.solicitud?.etapaCodigo ?? '') ? COLUMNAS_ESTUDIANTE_FINAL : COLUMNAS_ESTUDIANTE_EN_CURSO;
  }

  private evaluacionInicial(a: AsignaturaSolicitadaDTORespuesta): EvaluacionEnEdicion {
    return {
      faltas: a.numeroFaltas === null ? '' : String(a.numeroFaltas),
      nota: a.nota === null ? '' : a.nota.toFixed(1),
      situacion: a.situacionMatricula?.uuidSituacionAcademica ?? null,
      cumple: a.cumpleCondiciones,
      observacion: a.observacionEvaluacion ?? ''
    };
  }

  private decisionInicial(a: AsignaturaSolicitadaDTORespuesta): DecisionEnEdicion {
    const aprobada = a.aprobadaPorDecano ?? (a.cumpleCondiciones === true ? null : false);
    return {
      aprobada,
      situacion: a.situacionCancelar?.uuidSituacionAcademica ?? null,
      observacion: a.observacionDecision ?? ''
    };
  }

  private ejecutar(accion: AccionAcademica): Observable<unknown> {
    const uuid = this.uuidSolicitud;
    const observacion = this.observacion.trim();
    switch (accion) {
      case ACCIONES_ACADEMICAS.RECHAZAR_FUNCIONARIO:
        return this.subirResolucion().pipe(
          switchMap(() => this.cancelacionAsignaturaService.rechazarPorFuncionario(uuid, { observacion }))
        );
      case ACCIONES_ACADEMICAS.REMITIR_DECANO:
        return this.cancelacionAsignaturaService.remitirADecano(uuid, {
          observacion: observacion === '' ? null : observacion,
          evaluaciones: this.armarEvaluaciones()
        });
      case ACCIONES_ACADEMICAS.APROBAR_DECANO:
        return this.cancelacionAsignaturaService.aprobarPorDecano(uuid, { decisiones: this.armarDecisiones() });
      case ACCIONES_ACADEMICAS.RECHAZAR_DECANO:
        return this.cancelacionAsignaturaService.rechazarPorDecano(uuid, { observacion });
      default:
        return this.subirResolucion().pipe(switchMap(() => this.cancelacionAsignaturaService.enviarRespuesta(uuid)));
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
      const observacion = evaluacion.observacion.trim();
      return {
        asignaturaSolicitudUuid: a.uuidAsignaturaSolicitud,
        numeroFaltas: leerFaltas(evaluacion.faltas)!,
        nota: leerNota(evaluacion.nota)!,
        situacionMatriculaUuid: evaluacion.situacion!,
        cumpleCondiciones: evaluacion.cumple!,
        observacionEvaluacion: observacion === '' ? null : observacion
      };
    });
  }

  private armarDecisiones(): DecisionAsignaturaDTOPeticion[] {
    return this.asignaturas.map(a => {
      const decision = this.decisiones[a.uuidAsignaturaSolicitud];
      const observacion = decision.observacion.trim();
      return {
        asignaturaSolicitudUuid: a.uuidAsignaturaSolicitud,
        aprobada: decision.aprobada!,
        situacionCancelarUuid: decision.aprobada ? decision.situacion : null,
        observacionDecision: observacion === '' ? null : observacion
      };
    });
  }

  private validar(accion: AccionAcademica): Record<string, string> {
    const errores: Record<string, string> = {};
    const observacionObligatoria =
      accion === ACCIONES_ACADEMICAS.RECHAZAR_FUNCIONARIO || accion === ACCIONES_ACADEMICAS.RECHAZAR_DECANO;
    const errorObservacion = errorDeTexto(this.observacion, MAXIMO_CARACTERES_OBSERVACION, observacionObligatoria, 'La observación', true);
    if (errorObservacion) errores['observacion'] = errorObservacion;

    const exigeResolucion =
      accion === ACCIONES_ACADEMICAS.RECHAZAR_FUNCIONARIO || accion === ACCIONES_ACADEMICAS.ENVIAR_RESPUESTA;
    if (exigeResolucion && !this.archivoResolucion && !this.hayResolucion) {
      errores['resolucion'] = 'Debes subir el escaneo de la Resolución firmada (PDF).';
    }

    if (accion === ACCIONES_ACADEMICAS.REMITIR_DECANO) this.validarEvaluaciones(errores);
    if (accion === ACCIONES_ACADEMICAS.APROBAR_DECANO) this.validarDecisiones(errores);
    return errores;
  }

  private validarEvaluaciones(errores: Record<string, string>): void {
    for (const a of this.asignaturas) {
      const uuid = a.uuidAsignaturaSolicitud;
      const evaluacion = this.evaluaciones[uuid];
      const nota = leerNota(evaluacion?.nota);
      if (leerFaltas(evaluacion?.faltas) === null) errores[`faltas-${uuid}`] = 'Las faltas deben ser un entero mayor o igual a 0.';
      if (nota === null) errores[`nota-${uuid}`] = 'La nota debe estar entre 0.0 y 5.0 con un decimal.';
      if (!evaluacion?.situacion) errores[`situacion-${uuid}`] = 'Elige la situación en la matrícula.';
      if (evaluacion?.cumple === null || evaluacion?.cumple === undefined) {
        errores[`cumple-${uuid}`] = 'Indica si cumple las condiciones.';
      } else if (evaluacion.cumple && nota !== null && nota < NOTA_MINIMA_PARA_CUMPLIR) {
        errores[`cumple-${uuid}`] = 'Con nota menor a 3.0 no puede marcarse que cumple las condiciones.';
      }
      const errorObservacion = errorDeTexto(
        evaluacion?.observacion,
        MAXIMO_CARACTERES_OBSERVACION_ASIGNATURA,
        evaluacion?.cumple === false,
        'La observación de la evaluación',
        true
      );
      if (errorObservacion) errores[`observacion-${uuid}`] = errorObservacion;
    }
    if (!Object.values(this.evaluaciones).some(e => e.cumple === true)) {
      errores['ninguna'] = 'Ninguna asignatura cumple las condiciones: en lugar de remitir la solicitud al Decano debes rechazarla.';
    }
  }

  private validarDecisiones(errores: Record<string, string>): void {
    for (const a of this.asignaturas) {
      const uuid = a.uuidAsignaturaSolicitud;
      const decision = this.decisiones[uuid];
      if (decision?.aprobada === null || decision?.aprobada === undefined) {
        errores[`decision-${uuid}`] = 'Elige si se aprueba la cancelación de esta asignatura.';
        continue;
      }
      if (decision.aprobada && a.cumpleCondiciones !== true) {
        errores[`decision-${uuid}`] = 'La asignatura no cumple las condiciones y su cancelación no se puede aprobar.';
      }
      if (decision.aprobada && !decision.situacion) errores[`cancelar-${uuid}`] = 'Elige la situación al cancelar.';
      const errorObservacion = errorDeTexto(
        decision.observacion,
        MAXIMO_CARACTERES_OBSERVACION_ASIGNATURA,
        !decision.aprobada,
        'La observación de la decisión',
        true
      );
      if (errorObservacion) errores[`observacion-${uuid}`] = errorObservacion;
    }
    if (!Object.values(this.decisiones).some(d => d.aprobada === true)) {
      errores['ninguna'] = 'No se aprobó ninguna asignatura: para negar la solicitud completa usa Rechazar solicitud completa.';
    }
  }

  private abrirConSituaciones(): void {
    if (this.opcionesSituacion.length > 0) {
      this.dialogoVisible = true;
      return;
    }
    this.cargandoSituaciones = true;
    this.catalogoAcademicoService
      .getSituaciones()
      .pipe(finalize(() => (this.cargandoSituaciones = false)))
      .subscribe({
        next: situaciones => {
          this.opcionesSituacion = (situaciones ?? []).map(s => ({ label: `${s.codigo} - ${s.nombre}`, value: s.uuidSituacionAcademica }));
          if (this.opcionesSituacion.length === 0) {
            this.accionActiva = null;
            this.errorHandlerService.handleError(
              { message: 'El catálogo de situaciones académicas está vacío' },
              'Error',
              'No se pudieron cargar las situaciones académicas'
            );
            return;
          }
          this.dialogoVisible = true;
        },
        error: err => {
          this.accionActiva = null;
          this.errorHandlerService.handleError(err, 'Error', 'No se pudieron cargar las situaciones académicas');
        }
      });
  }
}
