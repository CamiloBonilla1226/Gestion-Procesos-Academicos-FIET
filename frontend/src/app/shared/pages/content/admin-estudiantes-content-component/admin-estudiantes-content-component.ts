import { Component, OnInit } from '@angular/core';
import { Observable, finalize } from 'rxjs';
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
import { ErrorHandlerService } from '../../../../core/services/error-handler-service';
import { ToastService } from '../../../../core/services/toast-service';
import { EstudianteDTORespuesta } from '../../../../core/models/Estudiante/DTOResponse/EstudianteDTORespuesta';
import { AsignaturaMatriculadaDTOPeticion } from '../../../../core/models/Estudiante/DTORequest/AsignaturaMatriculadaDTOPeticion';
import { AsignaturaMatriculadaDTORespuesta } from '../../../../core/models/Estudiante/DTOResponse/AsignaturaMatriculadaDTORespuesta';
import { EstudianteActualizarDTOPeticion } from '../../../../core/models/Estudiante/DTORequest/EstudianteActualizarDTOPeticion';
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

interface DatosAcademicos {
  codigoEstudiantil: string;
  programaAcademico: string;
  semestre: string;
  facultad: string;
}

const LIMITES_ACADEMICOS: { campo: keyof DatosAcademicos; maximo: number; nombre: string; femenino: boolean }[] = [
  { campo: 'codigoEstudiantil', maximo: 45, nombre: 'El código estudiantil', femenino: false },
  { campo: 'programaAcademico', maximo: 100, nombre: 'El programa académico', femenino: false },
  { campo: 'semestre', maximo: 10, nombre: 'El semestre', femenino: false },
  { campo: 'facultad', maximo: 100, nombre: 'La facultad', femenino: true }
];

function datosAcademicosVacios(): DatosAcademicos {
  return { codigoEstudiantil: '', programaAcademico: '', semestre: '', facultad: '' };
}

function asignaturaVacia(): AsignaturaMatriculadaDTOPeticion {
  return { codigoAsignatura: '', nombreAsignatura: '', grupo: '' };
}

function erroresDeAsignatura(asignatura: AsignaturaMatriculadaDTOPeticion, prefijo: string): Record<string, string> {
  const errores: Record<string, string> = {};
  const codigo = errorDeTexto(asignatura.codigoAsignatura, 45, true, 'El código');
  const nombre = errorDeTexto(asignatura.nombreAsignatura, 150, true, 'El nombre');
  const grupo = errorDeTexto(asignatura.grupo, 20, true, 'El grupo');
  if (codigo) errores[`${prefijo}codigo`] = codigo;
  if (nombre) errores[`${prefijo}nombre`] = nombre;
  if (grupo) errores[`${prefijo}grupo`] = grupo;
  return errores;
}

function recortarAsignatura(asignatura: AsignaturaMatriculadaDTOPeticion): AsignaturaMatriculadaDTOPeticion {
  return {
    codigoAsignatura: asignatura.codigoAsignatura.trim(),
    nombreAsignatura: asignatura.nombreAsignatura.trim(),
    grupo: asignatura.grupo.trim()
  };
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
export class AdminEstudiantesContentComponent implements OnInit {
  readonly encabezados: TableHeader[] = ['Código estudiantil', 'Nombres', 'Apellidos', 'Programa', 'Semestre'].map(title => ({ title }));
  readonly encabezadosExcel = ENCABEZADOS_EXCEL_ESTUDIANTES;
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

  dialogoCrear = false;
  datosUsuario: DatosUsuarioAcademico = datosUsuarioVacios();
  datosAcademicos: DatosAcademicos = datosAcademicosVacios();
  asignaturasNuevas: AsignaturaMatriculadaDTOPeticion[] = [asignaturaVacia()];

  dialogoEditar = false;
  edicion: DatosAcademicos = datosAcademicosVacios();

  dialogoExcel = false;

  dialogoDetalle = false;
  seleccionado: EstudianteDTORespuesta | null = null;
  nuevaMatricula: AsignaturaMatriculadaDTOPeticion = asignaturaVacia();
  estadosNuevos: Record<string, string | null> = {};
  cambiando = new Set<string>();

  errores: Record<string, string> = {};
  erroresServidor: string[] = [];
  enviando = false;

  constructor(
    private estudianteService: EstudianteService,
    private errorHandlerService: ErrorHandlerService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.cargar();
  }

  get nombreSeleccionado(): string {
    return this.seleccionado ? `${this.seleccionado.nombres} ${this.seleccionado.apellidos}` : '';
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
    this.datosUsuario = datosUsuarioVacios();
    this.datosAcademicos = datosAcademicosVacios();
    this.asignaturasNuevas = [asignaturaVacia()];
    this.limpiarErrores();
    this.dialogoCrear = true;
  }

  agregarFilaAsignatura(): void {
    this.asignaturasNuevas = [...this.asignaturasNuevas, asignaturaVacia()];
  }

  quitarFilaAsignatura(indice: number): void {
    this.asignaturasNuevas = this.asignaturasNuevas.filter((_, i) => i !== indice);
  }

  crear(): void {
    if (this.enviando) return;
    this.errores = this.validarCreacion();
    if (Object.keys(this.errores).length > 0) return;
    this.enviar(
      this.estudianteService.crearEstudiante({
        ...datosUsuarioRecortados(this.datosUsuario),
        codigoEstudiantil: this.datosAcademicos.codigoEstudiantil.trim(),
        programaAcademico: this.datosAcademicos.programaAcademico.trim(),
        semestre: this.datosAcademicos.semestre.trim(),
        facultad: this.datosAcademicos.facultad.trim(),
        asignaturas: this.asignaturasNuevas.map(recortarAsignatura)
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
    const peticion: EstudianteActualizarDTOPeticion = {};
    for (const { campo } of LIMITES_ACADEMICOS) {
      const valor = this.edicion[campo].trim();
      if (valor !== '') peticion[campo] = valor;
    }
    this.errores = this.erroresAcademicos(this.edicion, false);
    if (Object.keys(peticion).length === 0) this.errores['edicion'] = 'Escribe al menos un dato para cambiar.';
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
    this.nuevaMatricula = asignaturaVacia();
    this.estadosNuevos = {};
    this.limpiarErrores();
    this.dialogoDetalle = true;
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
    this.errores = erroresDeAsignatura(this.nuevaMatricula, 'nueva-');
    if (Object.keys(this.errores).length > 0) return;
    const estudiante = this.seleccionado;
    this.enviar(
      this.estudianteService.agregarAsignaturaMatriculada(estudiante.uuidUsuario, recortarAsignatura(this.nuevaMatricula)),
      'La asignatura quedó matriculada.',
      'No se pudo agregar la asignatura',
      matricula => {
        estudiante.asignaturasMatriculadas = [...(estudiante.asignaturasMatriculadas ?? []), matricula];
        this.nuevaMatricula = asignaturaVacia();
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
    if (this.asignaturasNuevas.length === 0) errores['asignaturas'] = 'Registra al menos una asignatura matriculada.';
    this.asignaturasNuevas.forEach((asignatura, i) => Object.assign(errores, erroresDeAsignatura(asignatura, `asignatura-${i}-`)));
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
