import { Component, OnDestroy, OnInit } from '@angular/core';
import { Observable, Subscription, finalize } from 'rxjs';
import { TableGenericComponent, TableHeader } from '../../../table-generic-component/table-generic-component';
import { Paginator } from '../../../paginator/paginator';
import { SimpleButtonComponent } from '../../../buttons/simple-button-component/simple-button-component';
import { GenericDialogFormComponent } from '../../../generic-dialog-form-component/generic-dialog-form-component';
import { GenericDialogInfoComponent } from '../../../generic-dialog-info-component/generic-dialog-info-component';
import { InputTextComponent } from '../../../inputs/input-text-component/input-text-component';
import { InputSelectComponent } from '../../../inputs/input-select-component/input-select-component';
import { DatosUsuarioAcademicoComponent } from '../../../others/datos-usuario-academico-component/datos-usuario-academico-component';
import { CargaExcelAcademicaComponent } from '../../../others/carga-excel-academica-component/carga-excel-academica-component';
import { EstudianteService } from '../../../../core/services/estudiante-service';
import { AsignaturaService } from '../../../../core/services/asignatura-service';
import { CatalogoAcademicoService } from '../../../../core/services/catalogo-academico-service';
import { ErrorHandlerService } from '../../../../core/services/error-handler-service';
import { ToastService } from '../../../../core/services/toast-service';
import { EstudianteDTORespuesta } from '../../../../core/models/Estudiante/DTOResponse/EstudianteDTORespuesta';
import { AsignaturaMatriculadaDTOPeticion } from '../../../../core/models/Estudiante/DTORequest/AsignaturaMatriculadaDTOPeticion';
import { AsignaturaMatriculadaDTORespuesta } from '../../../../core/models/Estudiante/DTOResponse/AsignaturaMatriculadaDTORespuesta';
import { EstudianteActualizarDTOPeticion } from '../../../../core/models/Estudiante/DTORequest/EstudianteActualizarDTOPeticion';
import { AsignaturaDTORespuesta } from '../../../../core/models/Asignatura/DTOResponse/AsignaturaDTORespuesta';
import { ProgramaAcademicoDTORespuesta } from '../../../../core/models/CatalogoAcademico/DTOResponse/ProgramaAcademicoDTORespuesta';
import {
  ENCABEZADOS_EXCEL_ESTUDIANTES,
  ESTADOS_ASIGNATURA_MATRICULADA,
  TAMANIO_PAGINA_ADMINISTRACION
} from '../../../../core/constantes/procesos-academicos';
import { errorDeTexto } from '../../../../core/utils/validaciones-academicas';
import {
  DatosUsuarioAcademico,
  datosUsuarioRecortados,
  datosUsuarioVacios,
  mensajesDeError,
  totalDePaginas,
  validarDatosUsuario
} from '../../../../core/utils/usuarios-academicos';
import {
  OpcionSelect,
  nombresDePrograma,
  opcionesDeAsignatura,
  opcionesDePrograma
} from '../../../../core/utils/programas-academicos';

interface DatosAcademicos {
  codigoEstudiantil: string;
  semestre: string;
  facultad: string;
}

interface FilaMatricula {
  codigoAsignatura: string | null;
  grupo: string;
}

const LIMITES_ACADEMICOS: { campo: keyof DatosAcademicos; maximo: number; nombre: string; femenino: boolean }[] = [
  { campo: 'codigoEstudiantil', maximo: 45, nombre: 'El código estudiantil', femenino: false },
  { campo: 'semestre', maximo: 10, nombre: 'El semestre', femenino: false },
  { campo: 'facultad', maximo: 100, nombre: 'La facultad', femenino: true }
];

function datosAcademicosVacios(): DatosAcademicos {
  return { codigoEstudiantil: '', semestre: '', facultad: '' };
}

function filaVacia(): FilaMatricula {
  return { codigoAsignatura: null, grupo: '' };
}

function erroresDeFila(fila: FilaMatricula, prefijo: string): Record<string, string> {
  const errores: Record<string, string> = {};
  const grupo = errorDeTexto(fila.grupo, 20, true, 'El grupo');
  if (!fila.codigoAsignatura) errores[`${prefijo}codigo`] = 'Elige la asignatura.';
  if (grupo) errores[`${prefijo}grupo`] = grupo;
  return errores;
}

function comoPeticion(fila: FilaMatricula): AsignaturaMatriculadaDTOPeticion {
  return { codigoAsignatura: fila.codigoAsignatura ?? '', grupo: fila.grupo.trim() };
}

@Component({
  selector: 'app-admin-estudiantes-content-component',
  imports: [
    TableGenericComponent,
    Paginator,
    SimpleButtonComponent,
    GenericDialogFormComponent,
    GenericDialogInfoComponent,
    InputTextComponent,
    InputSelectComponent,
    DatosUsuarioAcademicoComponent,
    CargaExcelAcademicaComponent
  ],
  templateUrl: './admin-estudiantes-content-component.html'
})
export class AdminEstudiantesContentComponent implements OnInit, OnDestroy {
  readonly encabezados: TableHeader[] = ['Código estudiantil', 'Nombres', 'Apellidos', 'Programa', 'Semestre'].map(title => ({ title }));
  readonly encabezadosExcel = ENCABEZADOS_EXCEL_ESTUDIANTES;
  readonly reglasExcel = [
    'Una fila por asignatura matriculada: las filas con el mismo número de documento se juntan en un estudiante y deben repetir los mismos datos personales y académicos, programa incluido.',
    'programaAcademico debe ser el nombre exacto de uno de los programas de la lista, sin importar mayúsculas.',
    'Cada codigoAsignatura debe existir en el catálogo y ser del programa del estudiante. La carga no crea asignaturas.',
    'La lectura termina en la primera fila vacía.'
  ];
  readonly opcionesEstado = Object.values(ESTADOS_ASIGNATURA_MATRICULADA).map(estado => ({ label: estado, value: estado }));
  readonly estadoActiva = ESTADOS_ASIGNATURA_MATRICULADA.ACTIVA;
  readonly cargarExcel = (archivo: File) => this.estudianteService.crearEstudiantesDesdeArchivo(archivo);

  filtro = { nombre: '', apellido: '', codigo: '' };
  estudiantes: EstudianteDTORespuesta[] = [];
  filas: Record<string, string>[] = [];
  paginaActual = 1;
  totalPaginas = 1;
  totalElementos = 0;
  cargando = false;
  errorCarga = false;

  programas: ProgramaAcademicoDTORespuesta[] = [];
  opcionesPrograma: OpcionSelect[] = [];
  nombresPrograma: string[] = [];
  cargandoProgramas = false;

  dialogoCrear = false;
  datosUsuario: DatosUsuarioAcademico = datosUsuarioVacios();
  datosAcademicos: DatosAcademicos = datosAcademicosVacios();
  uuidPrograma: string | null = null;
  programaAnterior: string | null = null;
  confirmandoCambioPrograma = false;
  asignaturasPrograma: AsignaturaDTORespuesta[] = [];
  cargandoAsignaturas = false;
  errorAsignaturas = false;
  filasMatricula: FilaMatricula[] = [filaVacia()];
  opcionesPorFila: OpcionSelect[][] = [[]];

  dialogoEditar = false;
  edicion: DatosAcademicos = datosAcademicosVacios();

  dialogoExcel = false;

  dialogoDetalle = false;
  seleccionado: EstudianteDTORespuesta | null = null;
  asignaturasDetalle: AsignaturaDTORespuesta[] = [];
  opcionesDetalle: OpcionSelect[] = [];
  cargandoDetalle = false;
  errorDetalle = false;
  nuevaMatricula: FilaMatricula = filaVacia();
  estadosNuevos: Record<string, string | null> = {};
  cambiando = new Set<string>();

  errores: Record<string, string> = {};
  erroresServidor: string[] = [];
  enviando = false;

  private consultaAsignaturas?: Subscription;
  private consultaDetalle?: Subscription;

  constructor(
    private estudianteService: EstudianteService,
    private asignaturaService: AsignaturaService,
    private catalogoAcademicoService: CatalogoAcademicoService,
    private errorHandlerService: ErrorHandlerService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.cargar();
  }

  ngOnDestroy(): void {
    this.consultaAsignaturas?.unsubscribe();
    this.consultaDetalle?.unsubscribe();
  }

  get nombreSeleccionado(): string {
    return this.seleccionado ? `${this.seleccionado.nombres} ${this.seleccionado.apellidos}` : '';
  }

  get nombreProgramaElegido(): string {
    return this.programas.find(p => p.uuidProgramaAcademico === this.uuidPrograma)?.nombre ?? '';
  }

  get seccionAsignaturasHabilitada(): boolean {
    return !!this.uuidPrograma && !this.confirmandoCambioPrograma && !this.cargandoAsignaturas && this.asignaturasPrograma.length > 0;
  }

  get puedeAgregarFila(): boolean {
    return this.seccionAsignaturasHabilitada && this.filasMatricula.length < this.asignaturasPrograma.length;
  }

  buscar(): void {
    this.paginaActual = 1;
    this.cargar();
  }

  cambiarPagina(pagina: number): void {
    this.paginaActual = pagina;
    this.cargar();
  }

  cargar(): void {
    this.cargando = true;
    this.errorCarga = false;
    this.estudianteService
      .getEstudiantesFiltrados(this.filtro, this.paginaActual - 1, TAMANIO_PAGINA_ADMINISTRACION)
      .pipe(finalize(() => (this.cargando = false)))
      .subscribe({
        next: pagina => {
          this.estudiantes = pagina.content ?? [];
          this.totalElementos = pagina.totalElements ?? 0;
          this.totalPaginas = totalDePaginas(this.totalElementos, TAMANIO_PAGINA_ADMINISTRACION);
          this.filas = this.estudiantes.map(e => ({
            uuid: e.uuidUsuario,
            'Código estudiantil': e.codigoEstudiantil,
            'Nombres': e.nombres,
            'Apellidos': e.apellidos,
            'Programa': e.programaAcademico,
            'Semestre': e.semestre
          }));
        },
        error: err => {
          this.estudiantes = [];
          this.filas = [];
          this.errorCarga = true;
          this.errorHandlerService.handleError(err, 'Error', 'No se pudieron cargar los estudiantes');
        }
      });
  }

  abrirCrear(): void {
    this.conProgramas(() => {
      this.datosUsuario = datosUsuarioVacios();
      this.datosAcademicos = datosAcademicosVacios();
      this.confirmandoCambioPrograma = false;
      this.programaAnterior = null;
      this.aplicarPrograma(null);
      this.limpiarErrores();
      this.dialogoCrear = true;
    });
  }

  abrirExcel(): void {
    this.conProgramas(() => (this.dialogoExcel = true));
  }

  elegirPrograma(uuid: string | null): void {
    if (this.confirmandoCambioPrograma) {
      this.uuidPrograma = uuid;
      if (uuid === this.programaAnterior) this.confirmandoCambioPrograma = false;
      return;
    }
    if (uuid === this.uuidPrograma) return;
    if (this.hayFilasLlenas()) {
      this.programaAnterior = this.uuidPrograma;
      this.uuidPrograma = uuid;
      this.confirmandoCambioPrograma = true;
      return;
    }
    this.aplicarPrograma(uuid);
  }

  confirmarCambioPrograma(): void {
    this.confirmandoCambioPrograma = false;
    this.programaAnterior = null;
    this.aplicarPrograma(this.uuidPrograma);
  }

  cancelarCambioPrograma(): void {
    this.uuidPrograma = this.programaAnterior;
    this.programaAnterior = null;
    this.confirmandoCambioPrograma = false;
  }

  recargarAsignaturasPrograma(): void {
    this.aplicarPrograma(this.uuidPrograma);
  }

  agregarFilaAsignatura(): void {
    if (!this.puedeAgregarFila) return;
    this.filasMatricula = [...this.filasMatricula, filaVacia()];
    this.actualizarOpciones();
  }

  quitarFilaAsignatura(indice: number): void {
    this.filasMatricula = this.filasMatricula.filter((_, i) => i !== indice);
    this.actualizarOpciones();
  }

  elegirAsignatura(indice: number, codigo: string | null): void {
    this.filasMatricula[indice].codigoAsignatura = codigo;
    this.actualizarOpciones();
  }

  crear(): void {
    if (this.enviando) return;
    this.errores = this.validarCreacion();
    if (Object.keys(this.errores).length > 0) return;
    this.enviar(
      this.estudianteService.crearEstudiante({
        ...datosUsuarioRecortados(this.datosUsuario),
        codigoEstudiantil: this.datosAcademicos.codigoEstudiantil.trim(),
        uuidProgramaAcademico: this.uuidPrograma ?? '',
        semestre: this.datosAcademicos.semestre.trim(),
        facultad: this.datosAcademicos.facultad.trim(),
        asignaturas: this.filasMatricula.map(comoPeticion)
      }),
      'El estudiante quedó creado.',
      'No se pudo crear el estudiante',
      () => {
        this.dialogoCrear = false;
        this.cargar();
      }
    );
  }

  abrirEditar(fila: Record<string, string>): void {
    this.seleccionado = this.estudiantes.find(e => e.uuidUsuario === fila['uuid']) ?? null;
    if (!this.seleccionado) return;
    this.edicion = datosAcademicosVacios();
    this.limpiarErrores();
    this.dialogoEditar = true;
  }

  editar(): void {
    if (this.enviando || !this.seleccionado) return;
    const peticion = this.peticionEdicion();
    this.errores = this.validarEdicion();
    if (Object.keys(this.errores).length > 0) return;
    this.enviar(
      this.estudianteService.actualizarEstudiante(this.seleccionado.uuidUsuario, peticion),
      'El estudiante quedó actualizado.',
      'No se pudo actualizar el estudiante',
      () => {
        this.dialogoEditar = false;
        this.cargar();
      }
    );
  }

  abrirDetalle(fila: Record<string, string>): void {
    this.seleccionado = this.estudiantes.find(e => e.uuidUsuario === fila['uuid']) ?? null;
    if (!this.seleccionado) return;
    this.nuevaMatricula = filaVacia();
    this.estadosNuevos = {};
    this.limpiarErrores();
    this.dialogoDetalle = true;
    this.cargarAsignaturasDetalle();
  }

  cargarAsignaturasDetalle(): void {
    this.consultaDetalle?.unsubscribe();
    this.asignaturasDetalle = [];
    this.opcionesDetalle = [];
    this.errorDetalle = false;
    const uuidPrograma = this.seleccionado?.uuidProgramaAcademico;
    if (!uuidPrograma) return;
    this.cargandoDetalle = true;
    this.consultaDetalle = this.asignaturaService
      .getAsignaturasPorPrograma(uuidPrograma)
      .pipe(finalize(() => (this.cargandoDetalle = false)))
      .subscribe({
        next: asignaturas => {
          this.asignaturasDetalle = asignaturas ?? [];
          this.actualizarOpcionesDetalle();
        },
        error: err => {
          this.errorDetalle = true;
          this.errorHandlerService.handleError(err, 'Error', 'No se pudieron cargar las asignaturas del programa');
        }
      });
  }

  cerrarCrear(visible: boolean): void {
    if (!visible && !this.enviando) this.dialogoCrear = false;
  }

  cerrarEditar(visible: boolean): void {
    if (!visible && !this.enviando) this.dialogoEditar = false;
  }

  cerrarDetalle(visible: boolean): void {
    if (!visible && !this.enviando && this.cambiando.size === 0) this.dialogoDetalle = false;
  }

  agregarMatricula(): void {
    if (this.enviando || !this.seleccionado) return;
    this.errores = erroresDeFila(this.nuevaMatricula, 'nueva-');
    if (Object.keys(this.errores).length > 0) return;
    const estudiante = this.seleccionado;
    this.enviar(
      this.estudianteService.agregarAsignaturaMatriculada(estudiante.uuidUsuario, comoPeticion(this.nuevaMatricula)),
      'La asignatura quedó matriculada.',
      'No se pudo agregar la asignatura',
      matricula => {
        estudiante.asignaturasMatriculadas = [...(estudiante.asignaturasMatriculadas ?? []), matricula];
        this.nuevaMatricula = filaVacia();
        this.actualizarOpcionesDetalle();
      }
    );
  }

  cambiarEstado(matricula: AsignaturaMatriculadaDTORespuesta): void {
    const estado = this.estadosNuevos[matricula.uuidAsignaturaMatriculada];
    if (!this.seleccionado || !estado || matricula.estado !== this.estadoActiva || this.cambiando.has(matricula.uuidAsignaturaMatriculada)) {
      return;
    }
    const estudiante = this.seleccionado;
    this.cambiando.add(matricula.uuidAsignaturaMatriculada);
    this.erroresServidor = [];
    this.estudianteService
      .cambiarEstadoAsignatura(estudiante.uuidUsuario, matricula.uuidAsignaturaMatriculada, { estado })
      .pipe(finalize(() => this.cambiando.delete(matricula.uuidAsignaturaMatriculada)))
      .subscribe({
        next: actualizada => {
          estudiante.asignaturasMatriculadas = estudiante.asignaturasMatriculadas.map(a =>
            a.uuidAsignaturaMatriculada === actualizada.uuidAsignaturaMatriculada ? actualizada : a
          );
          delete this.estadosNuevos[matricula.uuidAsignaturaMatriculada];
          this.actualizarOpcionesDetalle();
          this.toastService.showSuccess('Listo', `La asignatura ${actualizada.codigoAsignatura} quedó ${actualizada.estado}.`);
        },
        error: err => {
          this.erroresServidor = mensajesDeError(err);
          this.errorHandlerService.handleError(err, 'Error', 'No se pudo cambiar el estado');
        }
      });
  }

  alCargarExcel(): void {
    this.paginaActual = 1;
    this.cargar();
  }

  revalidarCrear(): void {
    if (Object.keys(this.errores).length > 0) this.errores = this.validarCreacion();
  }

  revalidarEditar(): void {
    if (Object.keys(this.errores).length > 0) this.errores = this.validarEdicion();
  }

  revalidarMatricula(): void {
    if (Object.keys(this.errores).length > 0) this.errores = erroresDeFila(this.nuevaMatricula, 'nueva-');
  }

  private actualizarOpcionesDetalle(): void {
    const activas = new Set(
      (this.seleccionado?.asignaturasMatriculadas ?? [])
        .filter(m => m.estado === this.estadoActiva)
        .map(m => (m.codigoAsignatura ?? '').trim().toUpperCase())
    );
    this.opcionesDetalle = opcionesDeAsignatura(
      this.asignaturasDetalle.filter(a => !activas.has(a.codigoAsignatura.trim().toUpperCase()))
    );
  }

  private peticionEdicion(): EstudianteActualizarDTOPeticion {
    const peticion: EstudianteActualizarDTOPeticion = {};
    for (const { campo } of LIMITES_ACADEMICOS) {
      const valor = this.edicion[campo].trim();
      if (valor !== '') peticion[campo] = valor;
    }
    return peticion;
  }

  private validarEdicion(): Record<string, string> {
    const errores = this.erroresAcademicos(this.edicion, false);
    if (Object.keys(this.peticionEdicion()).length === 0) errores['edicion'] = 'Escribe al menos un dato para cambiar.';
    return errores;
  }

  private aplicarPrograma(uuid: string | null): void {
    this.consultaAsignaturas?.unsubscribe();
    this.uuidPrograma = uuid;
    this.asignaturasPrograma = [];
    this.errorAsignaturas = false;
    this.cargandoAsignaturas = false;
    this.filasMatricula = [filaVacia()];
    this.actualizarOpciones();
    if (!uuid) return;
    this.cargandoAsignaturas = true;
    this.consultaAsignaturas = this.asignaturaService
      .getAsignaturasPorPrograma(uuid)
      .pipe(finalize(() => (this.cargandoAsignaturas = false)))
      .subscribe({
        next: asignaturas => {
          this.asignaturasPrograma = asignaturas ?? [];
          this.actualizarOpciones();
        },
        error: err => {
          this.errorAsignaturas = true;
          this.errorHandlerService.handleError(err, 'Error', 'No se pudieron cargar las asignaturas del programa');
        }
      });
  }

  private actualizarOpciones(): void {
    const todas = opcionesDeAsignatura(this.asignaturasPrograma);
    this.opcionesPorFila = this.filasMatricula.map((fila, indice) => {
      const usadas = new Set(this.filasMatricula.filter((_, i) => i !== indice).map(f => f.codigoAsignatura));
      return todas.filter(opcion => opcion.value === fila.codigoAsignatura || !usadas.has(opcion.value));
    });
  }

  private hayFilasLlenas(): boolean {
    return this.filasMatricula.some(fila => !!fila.codigoAsignatura || fila.grupo.trim() !== '');
  }

  private conProgramas(accion: () => void): void {
    if (this.programas.length > 0) {
      accion();
      return;
    }
    if (this.cargandoProgramas) return;
    this.cargandoProgramas = true;
    this.catalogoAcademicoService
      .getProgramas()
      .pipe(finalize(() => (this.cargandoProgramas = false)))
      .subscribe({
        next: programas => {
          this.programas = programas ?? [];
          this.opcionesPrograma = opcionesDePrograma(this.programas);
          this.nombresPrograma = nombresDePrograma(this.programas);
          if (this.programas.length === 0) {
            this.errorHandlerService.handleError({ message: 'No hay programas académicos registrados' }, 'Error', 'No se pudo abrir el formulario');
            return;
          }
          accion();
        },
        error: err => this.errorHandlerService.handleError(err, 'Error', 'No se pudieron cargar los programas académicos')
      });
  }

  private enviar<T>(llamada: Observable<T>, exito: string, fallo: string, alTerminar: (respuesta: T) => void): void {
    this.enviando = true;
    this.erroresServidor = [];
    llamada.pipe(finalize(() => (this.enviando = false))).subscribe({
      next: respuesta => {
        this.toastService.showSuccess('Listo', exito);
        alTerminar(respuesta);
      },
      error: err => {
        this.erroresServidor = mensajesDeError(err);
        this.errorHandlerService.handleError(err, 'Error', fallo);
      }
    });
  }

  private limpiarErrores(): void {
    this.errores = {};
    this.erroresServidor = [];
  }

  private validarCreacion(): Record<string, string> {
    const errores = { ...validarDatosUsuario(this.datosUsuario), ...this.erroresAcademicos(this.datosAcademicos, true) };
    if (!this.uuidPrograma) errores['programa'] = 'Elige el programa académico.';
    if (this.confirmandoCambioPrograma) errores['programa'] = 'Confirma o cancela el cambio de programa.';
    if (this.uuidPrograma && !this.cargandoAsignaturas && !this.errorAsignaturas && this.asignaturasPrograma.length === 0) {
      errores['asignaturas'] = 'El programa no tiene asignaturas registradas; créalas primero en la pestaña Asignaturas.';
    }
    if (this.filasMatricula.length === 0) errores['asignaturas'] = 'Registra al menos una asignatura matriculada.';
    const vistas = new Set<string>();
    this.filasMatricula.forEach((fila, i) => {
      Object.assign(errores, erroresDeFila(fila, `asignatura-${i}-`));
      if (fila.codigoAsignatura && vistas.has(fila.codigoAsignatura)) {
        errores[`asignatura-${i}-codigo`] = 'Esta asignatura ya está en otra fila.';
      }
      if (fila.codigoAsignatura) vistas.add(fila.codigoAsignatura);
    });
    return errores;
  }

  private erroresAcademicos(datos: DatosAcademicos, obligatorios: boolean): Record<string, string> {
    const errores: Record<string, string> = {};
    for (const limite of LIMITES_ACADEMICOS) {
      const error = errorDeTexto(datos[limite.campo], limite.maximo, obligatorios, limite.nombre, limite.femenino);
      if (error) errores[limite.campo] = error;
    }
    return errores;
  }
}
