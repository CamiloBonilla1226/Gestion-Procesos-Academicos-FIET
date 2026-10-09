import { Component, OnInit } from '@angular/core';
import { finalize } from 'rxjs';
import { TableGenericComponent, TableHeader } from '../../../table-generic-component/table-generic-component';
import { Paginator } from '../../../paginator/paginator';
import { BarraBusquedaComponent } from '../../../search/barra-busqueda-component/barra-busqueda-component';
import { SimpleButtonComponent } from '../../../buttons/simple-button-component/simple-button-component';
import { GenericDialogFormComponent } from '../../../generic-dialog-form-component/generic-dialog-form-component';
import { InputTextComponent } from '../../../inputs/input-text-component/input-text-component';
import { AsignaturaService } from '../../../../core/services/asignatura-service';
import { ErrorHandlerService } from '../../../../core/services/error-handler-service';
import { ToastService } from '../../../../core/services/toast-service';
import { AsignaturaDTORespuesta } from '../../../../core/models/Asignatura/DTOResponse/AsignaturaDTORespuesta';
import { TAMANIO_PAGINA_ADMINISTRACION } from '../../../../core/constantes/procesos-academicos';
import { errorDeTexto } from '../../../../core/utils/validaciones-academicas';
import { mensajesDeError, totalDePaginas } from '../../../../core/utils/usuarios-academicos';

@Component({
  selector: 'app-admin-asignaturas-content-component',
  imports: [TableGenericComponent, Paginator, BarraBusquedaComponent, SimpleButtonComponent, GenericDialogFormComponent, InputTextComponent],
  templateUrl: './admin-asignaturas-content-component.html'
})
export class AdminAsignaturasContentComponent implements OnInit {
  readonly encabezados: TableHeader[] = [{ title: 'Código' }, { title: 'Nombre' }];

  texto = '';
  asignaturas: AsignaturaDTORespuesta[] = [];
  filas: Record<string, string>[] = [];
  paginaActual = 1;
  totalPaginas = 1;
  totalElementos = 0;
  cargando = false;
  errorCarga = false;

  dialogoVisible = false;
  editando: AsignaturaDTORespuesta | null = null;
  codigo = '';
  nombre = '';
  errores: Record<string, string> = {};
  erroresServidor: string[] = [];
  enviando = false;

  constructor(
    private asignaturaService: AsignaturaService,
    private errorHandlerService: ErrorHandlerService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
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
      .getAsignaturasFiltradas(this.texto, this.paginaActual - 1, TAMANIO_PAGINA_ADMINISTRACION)
      .pipe(finalize(() => (this.cargando = false)))
      .subscribe({
        next: pagina => {
          this.asignaturas = pagina.content ?? [];
          this.totalElementos = pagina.totalElements ?? 0;
          this.totalPaginas = totalDePaginas(this.totalElementos, TAMANIO_PAGINA_ADMINISTRACION);
          this.filas = this.asignaturas.map(a => ({ uuid: a.uuidAsignatura, 'Código': a.codigoAsignatura, 'Nombre': a.nombreAsignatura }));
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
    this.abrir(null);
  }

  abrirEdicion(fila: Record<string, string>): void {
    this.abrir(this.asignaturas.find(a => a.uuidAsignatura === fila['uuid']) ?? null);
  }

  cerrar(visible: boolean): void {
    if (visible || this.enviando) return;
    this.dialogoVisible = false;
  }

  guardar(): void {
    if (this.enviando) return;
    this.errores = this.validar();
    if (Object.keys(this.errores).length > 0) return;
    const peticion = { codigoAsignatura: this.codigo.trim(), nombreAsignatura: this.nombre.trim() };
    const llamada = this.editando
      ? this.asignaturaService.actualizarAsignatura(this.editando.uuidAsignatura, peticion)
      : this.asignaturaService.crearAsignatura(peticion);
    this.enviando = true;
    this.erroresServidor = [];
    llamada.pipe(finalize(() => (this.enviando = false))).subscribe({
      next: () => {
        this.toastService.showSuccess('Listo', this.editando ? 'La asignatura quedó actualizada.' : 'La asignatura quedó creada.');
        this.dialogoVisible = false;
        this.cargar();
      },
      error: err => {
        this.erroresServidor = mensajesDeError(err);
        this.errorHandlerService.handleError(err, 'Error', 'No se pudo guardar la asignatura');
      }
    });
  }

  private abrir(asignatura: AsignaturaDTORespuesta | null): void {
    this.editando = asignatura;
    this.codigo = asignatura?.codigoAsignatura ?? '';
    this.nombre = asignatura?.nombreAsignatura ?? '';
    this.errores = {};
    this.erroresServidor = [];
    this.dialogoVisible = true;
  }

  private validar(): Record<string, string> {
    const errores: Record<string, string> = {};
    const errorCodigo = errorDeTexto(this.codigo, 45, true, 'El código');
    const errorNombre = errorDeTexto(this.nombre, 150, true, 'El nombre');
    if (errorCodigo) errores['codigo'] = errorCodigo;
    if (errorNombre) errores['nombre'] = errorNombre;
    return errores;
  }
}
