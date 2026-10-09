import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { Observable, finalize, map, of, switchMap, tap } from 'rxjs';
import { GenericDialogFormComponent } from '../../../generic-dialog-form-component/generic-dialog-form-component';
import { TableGenericComponent, TableHeader } from '../../../table-generic-component/table-generic-component';
import { SimpleButtonComponent } from '../../../buttons/simple-button-component/simple-button-component';
import { InputTextComponent } from '../../../inputs/input-text-component/input-text-component';
import { InputTextTareaComponent } from '../../../inputs/input-text-tarea-component/input-text-tarea-component';
import { InputSelectComponent } from '../../../inputs/input-select-component/input-select-component';
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
import { SituacionCancelarDTOPeticion } from '../../../../core/models/CancelacionMatricula/DTORequest/SituacionCancelarDTOPeticion';
import {
  ACCIONES_ACADEMICAS,
  AccionAcademica,
  FORMATOS_RESOLUCION,
  MAXIMO_CARACTERES_OBSERVACION,
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
  [ACCIONES_ACADEMICAS.APROBAR_DECANO]: 'Aprobar',
  [ACCIONES_ACADEMICAS.RECHAZAR_DECANO]: 'Rechazar',
  [ACCIONES_ACADEMICAS.ENVIAR_RESPUESTA]: 'Enviar respuesta'
};

const TITULOS_DIALOGO: Partial<Record<AccionAcademica, string>> = {
  [ACCIONES_ACADEMICAS.RECHAZAR_FUNCIONARIO]: 'Rechazar la solicitud',
  [ACCIONES_ACADEMICAS.REMITIR_DECANO]: 'Remitir la solicitud al Decano',
  [ACCIONES_ACADEMICAS.APROBAR_DECANO]: 'Aprobar la cancelación de matrícula',
  [ACCIONES_ACADEMICAS.RECHAZAR_DECANO]: 'Rechazar la cancelación de matrícula',
  [ACCIONES_ACADEMICAS.ENVIAR_RESPUESTA]: 'Enviar la respuesta al estudiante'
};

const MENSAJES_EXITO: Partial<Record<AccionAcademica, string>> = {
  [ACCIONES_ACADEMICAS.RECHAZAR_FUNCIONARIO]: 'La solicitud quedó rechazada.',
  [ACCIONES_ACADEMICAS.REMITIR_DECANO]: 'La solicitud fue remitida al Decano.',
  [ACCIONES_ACADEMICAS.APROBAR_DECANO]: 'La cancelación quedó aprobada.',
  [ACCIONES_ACADEMICAS.RECHAZAR_DECANO]: 'La cancelación quedó rechazada.',
  [ACCIONES_ACADEMICAS.ENVIAR_RESPUESTA]: 'La respuesta fue enviada al estudiante.'
};

const SIN_REGISTRAR = 'Sin registrar';
const TITULOS_COLUMNAS = ['Código', 'Asignatura', 'Faltas', 'Nota', 'Situación en la matrícula', 'Situación al cancelar'];
const COLUMNAS_FIJAS = ['Código', 'Asignatura'];

@Component({
  selector: 'app-cancelacion-matricula-proceso-component',
  imports: [
    GenericDialogFormComponent,
    TableGenericComponent,
    SimpleButtonComponent,
    InputTextComponent,
    InputTextTareaComponent,
    InputSelectComponent,
    InputAnexoUploadComponent
  ],
  templateUrl: './cancelacion-matricula-proceso-component.html'
})
export class CancelacionMatriculaProcesoComponent implements OnChanges {
  @Input({ required: true }) uuidSolicitud!: string;
  @Input({ required: true }) solicitud!: SolicitudAcademicaDetalleDTORespuesta;
  @Input({ required: true }) rol!: RolEtiqueta;
  @Input({ required: true }) recargar!: () => void;

  readonly acciones = ACCIONES_ACADEMICAS;
  readonly maximoObservacion = MAXIMO_CARACTERES_OBSERVACION;
  readonly tipoResolucion = comoTipoAnexo('resolucion', 'Escaneo de la Resolución firmada', FORMATOS_RESOLUCION, true);

  encabezados: TableHeader[] = TITULOS_COLUMNAS.map(title => ({ title }));

  detalle: CancelacionMatriculaDetalleDTORespuesta | null = null;
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
  situacionesCancelar: Record<string, string | null> = {};
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
      this.cargarDetalle();
    }
  }

  get accionesVisibles(): AccionAcademica[] {
    const disponibles = this.solicitud?.accionesDisponibles ?? [];
    return ACCIONES_DEL_PROCESO.filter(accion => disponibles.includes(accion));
  }

  get asignaturas(): AsignaturaCancelacionDTORespuesta[] {
    return this.detalle?.asignaturas ?? [];
  }

  get tituloDialogo(): string {
    return this.accionActiva ? TITULOS_DIALOGO[this.accionActiva] ?? '' : '';
  }

  get hayResolucion(): boolean {
    return this.solicitud?.tieneResolucion || this.resolucionSubida;
  }

  etiquetaBoton(accion: AccionAcademica): string {
    return ETIQUETAS_BOTON[accion] ?? accion;
  }

  textoNota(nota: number | null): string {
    return nota === null || nota === undefined ? 'Sin registrar' : nota.toFixed(1).replace('.', ',');
  }

  textoValor(valor: number | string | null | undefined): string {
    return valor === null || valor === undefined || valor === '' ? 'Sin registrar' : String(valor);
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
          this.filasAsignaturas = detalle.asignaturas.map(a => ({
            'Código': a.codigoAsignatura,
            'Asignatura': a.nombreAsignatura,
            'Faltas': this.textoValor(a.numeroFaltas),
            'Nota': this.textoNota(a.nota),
            'Situación en la matrícula': a.situacionMatricula ? `${a.situacionMatricula.codigo} - ${a.situacionMatricula.nombre}` : SIN_REGISTRAR,
            'Situación al cancelar': a.situacionCancelar ? `${a.situacionCancelar.codigo} - ${a.situacionCancelar.nombre}` : SIN_REGISTRAR
          }));
          this.encabezados = this.titulosVisibles().map(title => ({ title }));
        },
        error: err => {
          this.detalle = null;
          this.filasAsignaturas = [];
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
    this.errores = {};
    this.erroresServidor = [];
    this.evaluaciones = Object.fromEntries(
      this.asignaturas.map(a => [
        a.uuidAsignaturaSolicitud,
        {
          faltas: a.numeroFaltas === null ? '' : String(a.numeroFaltas),
          nota: a.nota === null ? '' : a.nota.toFixed(1),
          situacion: a.situacionMatricula?.uuidSituacionAcademica ?? null
        }
      ])
    );
    this.situacionesCancelar = Object.fromEntries(
      this.asignaturas.map(a => [a.uuidAsignaturaSolicitud, a.situacionCancelar?.uuidSituacionAcademica ?? null])
    );
    if (accion === ACCIONES_ACADEMICAS.REMITIR_DECANO || accion === ACCIONES_ACADEMICAS.APROBAR_DECANO) {
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

  private titulosVisibles(): string[] {
    if (this.rol !== ROLES_ETIQUETA.ESTUDIANTE) return TITULOS_COLUMNAS;
    return TITULOS_COLUMNAS.filter(
      titulo => COLUMNAS_FIJAS.includes(titulo) || this.filasAsignaturas.some(fila => fila[titulo] !== SIN_REGISTRAR)
    );
  }

  private ejecutar(accion: AccionAcademica): Observable<unknown> {
    const uuid = this.uuidSolicitud;
    const observacion = this.observacion.trim();
    switch (accion) {
      case ACCIONES_ACADEMICAS.RECHAZAR_FUNCIONARIO:
        return this.subirResolucion().pipe(
          switchMap(() => this.cancelacionMatriculaService.rechazarPorFuncionario(uuid, { observacion }))
        );
      case ACCIONES_ACADEMICAS.REMITIR_DECANO:
        return this.cancelacionMatriculaService.remitirADecano(uuid, {
          observacion: observacion === '' ? null : observacion,
          evaluaciones: this.armarEvaluaciones()
        });
      case ACCIONES_ACADEMICAS.APROBAR_DECANO:
        return this.cancelacionMatriculaService.aprobarPorDecano(uuid, { situaciones: this.armarSituaciones() });
      case ACCIONES_ACADEMICAS.RECHAZAR_DECANO:
        return this.cancelacionMatriculaService.rechazarPorDecano(uuid, { observacion });
      default:
        return this.subirResolucion().pipe(switchMap(() => this.cancelacionMatriculaService.enviarRespuesta(uuid)));
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

  private armarEvaluaciones(): EvaluacionAsignaturaDTOPeticion[] {
    return this.asignaturas.map(a => {
      const evaluacion = this.evaluaciones[a.uuidAsignaturaSolicitud];
      return {
        asignaturaSolicitudUuid: a.uuidAsignaturaSolicitud,
        numeroFaltas: leerFaltas(evaluacion.faltas)!,
        nota: leerNota(evaluacion.nota)!,
        situacionMatriculaUuid: evaluacion.situacion!
      };
    });
  }

  private armarSituaciones(): SituacionCancelarDTOPeticion[] {
    return this.asignaturas.map(a => ({
      asignaturaSolicitudUuid: a.uuidAsignaturaSolicitud,
      situacionCancelarUuid: this.situacionesCancelar[a.uuidAsignaturaSolicitud]!
    }));
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

    if (accion === ACCIONES_ACADEMICAS.REMITIR_DECANO) {
      for (const a of this.asignaturas) {
        const evaluacion = this.evaluaciones[a.uuidAsignaturaSolicitud];
        if (leerFaltas(evaluacion?.faltas) === null) {
          errores[`faltas-${a.uuidAsignaturaSolicitud}`] = 'Las faltas deben ser un entero mayor o igual a 0.';
        }
        if (leerNota(evaluacion?.nota) === null) {
          errores[`nota-${a.uuidAsignaturaSolicitud}`] = 'La nota debe estar entre 0.0 y 5.0 con un decimal.';
        }
        if (!evaluacion?.situacion) {
          errores[`situacion-${a.uuidAsignaturaSolicitud}`] = 'Elige la situación en la matrícula.';
        }
      }
    }

    if (accion === ACCIONES_ACADEMICAS.APROBAR_DECANO) {
      for (const a of this.asignaturas) {
        if (!this.situacionesCancelar[a.uuidAsignaturaSolicitud]) {
          errores[`cancelar-${a.uuidAsignaturaSolicitud}`] = 'Elige la situación al cancelar.';
        }
      }
    }
    return errores;
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
