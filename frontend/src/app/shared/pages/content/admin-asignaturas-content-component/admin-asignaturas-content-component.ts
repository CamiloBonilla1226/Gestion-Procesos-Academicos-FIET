import { Component, OnInit } from '@angular/core';
import { finalize } from 'rxjs';
import { TableGenericComponent, TableHeader } from '../../../table-generic-component/table-generic-component';
import { Paginator } from '../../../paginator/paginator';
import { BarraBusquedaComponent } from '../../../search/barra-busqueda-component/barra-busqueda-component';
import { SimpleButtonComponent } from '../../../buttons/simple-button-component/simple-button-component';
import { GenericDialogFormComponent } from '../../../generic-dialog-form-component/generic-dialog-form-component';
import { InputTextComponent } from '../../../inputs/input-text-component/input-text-component';
import { InputSelectComponent } from '../../../inputs/input-select-component/input-select-component';
import { CargaExcelAcademicaComponent } from '../../../others/carga-excel-academica-component/carga-excel-academica-component';
import { AsignaturaService } from '../../../../core/services/asignatura-service';
import { CatalogoAcademicoService } from '../../../../core/services/catalogo-academico-service';
import { ErrorHandlerService } from '../../../../core/services/error-handler-service';
import { ToastService } from '../../../../core/services/toast-service';
import { AsignaturaDTORespuesta } from '../../../../core/models/Asignatura/DTOResponse/AsignaturaDTORespuesta';
import { ProgramaAcademicoDTORespuesta } from '../../../../core/models/CatalogoAcademico/DTOResponse/ProgramaAcademicoDTORespuesta';
import { ENCABEZADOS_EXCEL_ASIGNATURAS, TAMANIO_PAGINA_ADMINISTRACION } from '../../../../core/constantes/procesos-academicos';
import { errorDeTexto } from '../../../../core/utils/validaciones-academicas';
import { mensajesDeError, totalDePaginas } from '../../../../core/utils/usuarios-academicos';
import { OpcionSelect, nombresDePrograma, opcionesDePrograma } from '../../../../core/utils/programas-academicos';

@Component({
  selector: 'app-admin-asignaturas-content-component',
  imports: [
    TableGenericComponent,
    Paginator,
    BarraBusquedaComponent,
    SimpleButtonComponent,
    GenericDialogFormComponent,
    InputTextComponent,
    InputSelectComponent,
    CargaExcelAcademicaComponent
  ],
  templateUrl: './admin-asignaturas-content-component.html'
})
export class AdminAsignaturasContentComponent implements OnInit {
  readonly encabezados: TableHeader[] = [{ title: 'Código' }, { title: 'Nombre' }, { title: 'Programa' }];
  readonly encabezadosExcel = ENCABEZADOS_EXCEL_ASIGNATURAS;
  readonly reglasExcel = [
    'Una fila por asignatura. El código no va en el archivo: el sistema lo genera con la sigla del programa y el siguiente consecutivo (por ejemplo ISI-001).',
    'programaAcademico debe ser el nombre exacto de uno de los programas de la lista, sin importar mayúsculas.',
    'El nombre no se puede repetir dentro del mismo programa. Si una fila falla no se crea ninguna asignatura.',
    'La lectura termina en la primera fila vacía.'
  ];
  readonly cargarExcel = (archivo: File) => this.asignaturaService.crearAsignaturasDesdeArchivo(archivo);

  texto = '';
  programaFiltro: string | null = '';
  programas: ProgramaAcademicoDTORespuesta[] = [];
  opcionesPrograma: OpcionSelect[] = [];
  opcionesFiltro: OpcionSelect[] = [{ label: 'Todos', value: '' }];
  nombresPrograma: string[] = [];
  cargandoProgramas = false;

  asignaturas: AsignaturaDTORespuesta[] = [];
  filas: Record<string, string>[] = [];
  paginaActual = 1;
  totalPaginas = 1;
  totalElementos = 0;
  cargando = false;
  errorCarga = false;

  dialogoVisible = false;
  dialogoExcel = false;
  editando: AsignaturaDTORespuesta | null = null;
  nombre = '';
  uuidPrograma: string | null = null;
  errores: Record<string, string> = {};
  erroresServidor: string[] = [];
  enviando = false;

  constructor(
    private asignaturaService: AsignaturaService,
    private catalogoAcademicoService: CatalogoAcademicoService,
    private errorHandlerService: ErrorHandlerService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.cargarProgramas();
    this.cargar();
  }

  get tituloDialogo(): string {
    return this.editando ? 'Editar asignatura' : 'Nueva asignatura';
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
    this.asignaturaService
      .getAsignaturasFiltradas(
        { texto: this.texto, uuidProgramaAcademico: this.programaFiltro },
        this.paginaActual - 1,
        TAMANIO_PAGINA_ADMINISTRACION
      )
      .pipe(finalize(() => (this.cargando = false)))
      .subscribe({
        next: pagina => {
          this.asignaturas = pagina.content ?? [];
          this.totalElementos = pagina.totalElements ?? 0;
          this.totalPaginas = totalDePaginas(this.totalElementos, TAMANIO_PAGINA_ADMINISTRACION);
          this.filas = this.asignaturas.map(a => ({
            uuid: a.uuidAsignatura,
            'Código': a.codigoAsignatura,
            'Nombre': a.nombreAsignatura,
            'Programa': a.programaAcademico
          }));
        },
        error: err => {
          this.asignaturas = [];
          this.filas = [];
          this.errorCarga = true;
          this.errorHandlerService.handleError(err, 'Error', 'No se pudieron cargar las asignaturas');
        }
      });
  }

  abrirNueva(): void {
    this.conProgramas(() => this.abrir(null));
  }

  abrirExcel(): void {
    this.conProgramas(() => (this.dialogoExcel = true));
  }

  abrirEdicion(fila: Record<string, string>): void {
    const asignatura = this.asignaturas.find(a => a.uuidAsignatura === fila['uuid']);
    if (asignatura) this.abrir(asignatura);
  }

  cerrar(visible: boolean): void {
    if (visible || this.enviando) return;
    this.dialogoVisible = false;
  }

  guardar(): void {
    if (this.enviando) return;
    this.errores = this.validar();
    if (Object.keys(this.errores).length > 0) return;
    const nombre = this.nombre.trim();
    const editando = this.editando;
    const llamada = editando
      ? this.asignaturaService.actualizarAsignatura(editando.uuidAsignatura, { nombreAsignatura: nombre })
      : this.asignaturaService.crearAsignatura({ nombreAsignatura: nombre, uuidProgramaAcademico: this.uuidPrograma ?? '' });
    this.enviando = true;
    this.erroresServidor = [];
    llamada.pipe(finalize(() => (this.enviando = false))).subscribe({
      next: asignatura => {
        this.toastService.showSuccess(
          'Listo',
          editando ? 'La asignatura quedó actualizada.' : `Asignatura creada con el código ${asignatura.codigoAsignatura}`
        );
        this.dialogoVisible = false;
        this.cargar();
      },
      error: err => {
        this.erroresServidor = mensajesDeError(err);
        this.errorHandlerService.handleError(err, 'Error', 'No se pudo guardar la asignatura');
      }
    });
  }

  alCargarExcel(): void {
    this.paginaActual = 1;
    this.cargar();
  }

  private abrir(asignatura: AsignaturaDTORespuesta | null): void {
    this.editando = asignatura;
    this.nombre = asignatura?.nombreAsignatura ?? '';
    this.uuidPrograma = asignatura?.uuidProgramaAcademico ?? null;
    this.errores = {};
    this.erroresServidor = [];
    this.dialogoVisible = true;
  }

  private validar(): Record<string, string> {
    const errores: Record<string, string> = {};
    const errorNombre = errorDeTexto(this.nombre, 150, true, 'El nombre');
    if (errorNombre) errores['nombre'] = errorNombre;
    if (!this.editando && !this.uuidPrograma) errores['programa'] = 'Elige el programa académico.';
    return errores;
  }

  private cargarProgramas(alTerminar?: () => void): void {
    this.cargandoProgramas = true;
    this.catalogoAcademicoService
      .getProgramas()
      .pipe(finalize(() => (this.cargandoProgramas = false)))
      .subscribe({
        next: programas => {
          this.programas = programas ?? [];
          this.opcionesPrograma = opcionesDePrograma(this.programas);
          this.opcionesFiltro = [{ label: 'Todos', value: '' }, ...this.opcionesPrograma];
          this.nombresPrograma = nombresDePrograma(this.programas);
          if (!alTerminar) return;
          if (this.programas.length === 0) {
            this.errorHandlerService.handleError({ message: 'No hay programas académicos registrados' }, 'Error', 'No se pudo abrir el formulario');
            return;
          }
          alTerminar();
        },
        error: err => this.errorHandlerService.handleError(err, 'Error', 'No se pudieron cargar los programas académicos')
      });
  }

  private conProgramas(accion: () => void): void {
    if (this.programas.length > 0) {
      accion();
      return;
    }
    if (!this.cargandoProgramas) this.cargarProgramas(accion);
  }
}
