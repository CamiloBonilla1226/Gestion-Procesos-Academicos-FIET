import { Component, OnInit } from '@angular/core';
import { Observable, finalize } from 'rxjs';
import { TableGenericComponent, TableHeader } from '../../../table-generic-component/table-generic-component';
import { Paginator } from '../../../paginator/paginator';
import { SimpleButtonComponent } from '../../../buttons/simple-button-component/simple-button-component';
import { GenericDialogFormComponent } from '../../../generic-dialog-form-component/generic-dialog-form-component';
import { InputTextComponent } from '../../../inputs/input-text-component/input-text-component';
import { DatosUsuarioAcademicoComponent } from '../../../others/datos-usuario-academico-component/datos-usuario-academico-component';
import { CargaExcelAcademicaComponent } from '../../../others/carga-excel-academica-component/carga-excel-academica-component';
import { FuncionarioAcademicoService } from '../../../../core/services/funcionario-academico-service';
import { ErrorHandlerService } from '../../../../core/services/error-handler-service';
import { ToastService } from '../../../../core/services/toast-service';
import { FuncionarioAcademicoDTORespuesta } from '../../../../core/models/FuncionarioAcademico/DTOResponse/FuncionarioAcademicoDTORespuesta';
import { ENCABEZADOS_EXCEL_FUNCIONARIOS, TAMANIO_PAGINA_ADMINISTRACION } from '../../../../core/constantes/procesos-academicos';
import { errorDeTexto } from '../../../../core/utils/validaciones-academicas';
import {
  DatosUsuarioAcademico,
  datosUsuarioRecortados,
  datosUsuarioVacios,
  mensajesDeError,
  totalDePaginas,
  validarDatosUsuario
} from '../../../../core/utils/usuarios-academicos';

@Component({
  selector: 'app-admin-funcionarios-content-component',
  imports: [
    TableGenericComponent,
    Paginator,
    SimpleButtonComponent,
    GenericDialogFormComponent,
    InputTextComponent,
    DatosUsuarioAcademicoComponent,
    CargaExcelAcademicaComponent
  ],
  templateUrl: './admin-funcionarios-content-component.html'
})
export class AdminFuncionariosContentComponent implements OnInit {
  readonly encabezados: TableHeader[] = ['Nombres', 'Apellidos', 'Dependencia', 'Correo electrónico', 'Procesos a cargo'].map(
    title => ({ title })
  );
  readonly encabezadosExcel = ENCABEZADOS_EXCEL_FUNCIONARIOS;
  readonly cargarExcel = (archivo: File) => this.funcionarioAcademicoService.crearFuncionariosAcademicosDesdeArchivo(archivo);

  filtro = { nombre: '', apellido: '', dependencia: '' };
  funcionarios: FuncionarioAcademicoDTORespuesta[] = [];
  filas: Record<string, string>[] = [];
  paginaActual = 1;
  totalPaginas = 1;
  totalElementos = 0;
  cargando = false;
  errorCarga = false;

  dialogoCrear = false;
  datosUsuario: DatosUsuarioAcademico = datosUsuarioVacios();
  dependencia = '';

  dialogoEditar = false;
  seleccionado: FuncionarioAcademicoDTORespuesta | null = null;
  dependenciaEditada = '';

  dialogoExcel = false;

  errores: Record<string, string> = {};
  erroresServidor: string[] = [];
  enviando = false;

  constructor(
    private funcionarioAcademicoService: FuncionarioAcademicoService,
    private errorHandlerService: ErrorHandlerService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.cargar();
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
    this.funcionarioAcademicoService
      .getFuncionariosAcademicosFiltrados(this.filtro, this.paginaActual - 1, TAMANIO_PAGINA_ADMINISTRACION)
      .pipe(finalize(() => (this.cargando = false)))
      .subscribe({
        next: pagina => {
          this.funcionarios = pagina.content ?? [];
          this.totalElementos = pagina.totalElements ?? 0;
          this.totalPaginas = totalDePaginas(this.totalElementos, TAMANIO_PAGINA_ADMINISTRACION);
          this.filas = this.funcionarios.map(f => ({
            uuid: f.uuidUsuario,
            'Nombres': f.nombres,
            'Apellidos': f.apellidos,
            'Dependencia': f.dependencia,
            'Correo electrónico': f.correoElectronico,
            'Procesos a cargo': (f.tiposSolicitud ?? []).map(t => t.nombre).join(', ') || 'Ninguno'
          }));
        },
        error: err => {
          this.funcionarios = [];
          this.filas = [];
          this.errorCarga = true;
          this.errorHandlerService.handleError(err, 'Error', 'No se pudieron cargar los funcionarios académicos');
        }
      });
  }

  abrirCrear(): void {
    this.datosUsuario = datosUsuarioVacios();
    this.dependencia = '';
    this.errores = {};
    this.erroresServidor = [];
    this.dialogoCrear = true;
  }

  crear(): void {
    if (this.enviando) return;
    this.errores = validarDatosUsuario(this.datosUsuario);
    const errorDependencia = errorDeTexto(this.dependencia, 100, true, 'La dependencia', true);
    if (errorDependencia) this.errores['dependencia'] = errorDependencia;
    if (Object.keys(this.errores).length > 0) return;
    this.enviar(
      this.funcionarioAcademicoService.crearFuncionarioAcademico({
        ...datosUsuarioRecortados(this.datosUsuario),
        dependencia: this.dependencia.trim()
      }),
      'El funcionario académico quedó creado.',
      'No se pudo crear el funcionario académico',
      () => (this.dialogoCrear = false)
    );
  }

  abrirEditar(fila: Record<string, string>): void {
    this.seleccionado = this.funcionarios.find(f => f.uuidUsuario === fila['uuid']) ?? null;
    if (!this.seleccionado) return;
    this.dependenciaEditada = this.seleccionado.dependencia ?? '';
    this.errores = {};
    this.erroresServidor = [];
    this.dialogoEditar = true;
  }

  editar(): void {
    if (this.enviando || !this.seleccionado) return;
    const error = errorDeTexto(this.dependenciaEditada, 100, true, 'La dependencia', true);
    this.errores = error ? { dependencia: error } : {};
    if (error) return;
    this.enviar(
      this.funcionarioAcademicoService.actualizarFuncionarioAcademico(this.seleccionado.uuidUsuario, {
        dependencia: this.dependenciaEditada.trim()
      }),
      'La dependencia quedó actualizada.',
      'No se pudo actualizar el funcionario académico',
      () => (this.dialogoEditar = false)
    );
  }

  cerrarCrear(visible: boolean): void {
    if (!visible && !this.enviando) this.dialogoCrear = false;
  }

  cerrarEditar(visible: boolean): void {
    if (!visible && !this.enviando) this.dialogoEditar = false;
  }

  alCargarExcel(): void {
    this.paginaActual = 1;
    this.cargar();
  }

  private enviar<T>(llamada: Observable<T>, exito: string, fallo: string, alTerminar: () => void): void {
    this.enviando = true;
    this.erroresServidor = [];
    llamada.pipe(finalize(() => (this.enviando = false))).subscribe({
      next: () => {
        this.toastService.showSuccess('Listo', exito);
        alTerminar();
        this.cargar();
      },
      error: err => {
        this.erroresServidor = mensajesDeError(err);
        this.errorHandlerService.handleError(err, 'Error', fallo);
      }
    });
  }
}
