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
import { FuncionarioAcademicoActualizarDTOPeticion } from '../../../../core/models/FuncionarioAcademico/DTORequest/FuncionarioAcademicoActualizarDTOPeticion';
import {
  CAMPOS_PERSONALES,
  DatosUsuarioAcademico,
  cambiosDeTexto,
  datosUsuarioDe,
  datosUsuarioRecortados,
  datosUsuarioVacios,
  mensajesDeError,
  totalDePaginas,
  validarCambiosPersonales,
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
  readonly encabezados: TableHeader[] = ['Nombres', 'Apellidos', 'Cargo', 'Correo electrónico', 'Procesos a cargo'].map(
    title => ({ title })
  );
  readonly encabezadosExcel = ENCABEZADOS_EXCEL_FUNCIONARIOS;
  readonly cargarExcel = (archivo: File) => this.funcionarioAcademicoService.crearFuncionariosAcademicosDesdeArchivo(archivo);

  filtro = { nombre: '', apellido: '', cargo: '' };
  funcionarios: FuncionarioAcademicoDTORespuesta[] = [];
  filas: Record<string, string>[] = [];
  paginaActual = 1;
  totalPaginas = 1;
  totalElementos = 0;
  cargando = false;
  errorCarga = false;

  dialogoCrear = false;
  datosUsuario: DatosUsuarioAcademico = datosUsuarioVacios();
  cargo = '';

  dialogoEditar = false;
  seleccionado: FuncionarioAcademicoDTORespuesta | null = null;
  datosEdicion: DatosUsuarioAcademico = datosUsuarioVacios();
  cargoEditado = '';

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
            'Cargo': f.cargo,
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
    this.cargo = '';
    this.errores = {};
    this.erroresServidor = [];
    this.dialogoCrear = true;
  }

  crear(): void {
    if (this.enviando) return;
    this.errores = this.validarCreacion();
    if (Object.keys(this.errores).length > 0) return;
    this.enviar(
      this.funcionarioAcademicoService.crearFuncionarioAcademico({
        ...datosUsuarioRecortados(this.datosUsuario),
        cargo: this.cargo.trim()
      }),
      'El funcionario académico quedó creado.',
      'No se pudo crear el funcionario académico',
      () => (this.dialogoCrear = false)
    );
  }

  abrirEditar(fila: Record<string, string>): void {
    this.seleccionado = this.funcionarios.find(f => f.uuidUsuario === fila['uuid']) ?? null;
    if (!this.seleccionado) return;
    this.datosEdicion = datosUsuarioDe(this.seleccionado);
    this.cargoEditado = this.seleccionado.cargo ?? '';
    this.errores = {};
    this.erroresServidor = [];
    this.dialogoEditar = true;
  }

  editar(): void {
    if (this.enviando || !this.seleccionado) return;
    this.errores = this.validarEdicion();
    if (Object.keys(this.errores).length > 0) return;
    this.enviar(
      this.funcionarioAcademicoService.actualizarFuncionarioAcademico(this.seleccionado.uuidUsuario, this.peticionEdicion()),
      'El funcionario académico quedó actualizado.',
      'No se pudo actualizar el funcionario académico',
      () => (this.dialogoEditar = false)
    );
  }

  revalidarCrear(): void {
    if (Object.keys(this.errores).length > 0) this.errores = this.validarCreacion();
  }

  revalidarEditar(): void {
    if (Object.keys(this.errores).length > 0) this.errores = this.validarEdicion();
  }

  private validarCreacion(): Record<string, string> {
    const errores = validarDatosUsuario(this.datosUsuario);
    const errorCargo = errorDeTexto(this.cargo, 100, true, 'El cargo');
    if (errorCargo) errores['cargo'] = errorCargo;
    return errores;
  }

  get procesosSeleccionado(): string {
    return (this.seleccionado?.tiposSolicitud ?? []).map(t => t.nombre).join(', ') || 'Ninguno';
  }

  private peticionEdicion(): FuncionarioAcademicoActualizarDTOPeticion {
    if (!this.seleccionado) return {};
    return {
      ...cambiosDeTexto(CAMPOS_PERSONALES, this.seleccionado, this.datosEdicion),
      ...cambiosDeTexto(['cargo'], { cargo: this.seleccionado.cargo }, { cargo: this.cargoEditado })
    };
  }

  private validarEdicion(): Record<string, string> {
    const peticion = this.peticionEdicion();
    const errores = validarCambiosPersonales(peticion);
    const errorCargo = errorDeTexto(peticion.cargo, 100, false, 'El cargo');
    if (errorCargo) errores['cargo'] = errorCargo;
    if (Object.keys(peticion).length === 0) errores['edicion'] = 'No cambiaste ningún dato.';
    return errores;
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
