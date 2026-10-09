import { Component, OnInit } from '@angular/core';
import { finalize } from 'rxjs';
import { TableGenericComponent, TableHeader } from '../../../table-generic-component/table-generic-component';
import { SimpleButtonComponent } from '../../../buttons/simple-button-component/simple-button-component';
import { GenericDialogFormComponent } from '../../../generic-dialog-form-component/generic-dialog-form-component';
import { InputSelectComponent } from '../../../inputs/input-select-component/input-select-component';
import { CatalogoAcademicoService } from '../../../../core/services/catalogo-academico-service';
import { FuncionarioAcademicoService } from '../../../../core/services/funcionario-academico-service';
import { ErrorHandlerService } from '../../../../core/services/error-handler-service';
import { ToastService } from '../../../../core/services/toast-service';
import { TipoSolicitudAcademicaDTORespuesta } from '../../../../core/models/CatalogoAcademico/DTOResponse/TipoSolicitudAcademicaDTORespuesta';
import { mensajesDeError } from '../../../../core/utils/usuarios-academicos';

const MAXIMO_FUNCIONARIOS_EN_LISTA = 100;

@Component({
  selector: 'app-admin-responsables-content-component',
  imports: [TableGenericComponent, SimpleButtonComponent, GenericDialogFormComponent, InputSelectComponent],
  templateUrl: './admin-responsables-content-component.html'
})
export class AdminResponsablesContentComponent implements OnInit {
  readonly encabezados: TableHeader[] = [{ title: 'Proceso' }, { title: 'Responsable actual' }];

  tipos: TipoSolicitudAcademicaDTORespuesta[] = [];
  filas: Record<string, string>[] = [];
  cargando = false;
  errorCarga = false;

  dialogoVisible = false;
  seleccionado: TipoSolicitudAcademicaDTORespuesta | null = null;
  opcionesFuncionario: { label: string; value: string }[] = [];
  funcionarioElegido: string | null = null;
  cargandoFuncionarios = false;
  hayMasFuncionarios = false;
  error = '';
  erroresServidor: string[] = [];
  enviando = false;

  constructor(
    private catalogoAcademicoService: CatalogoAcademicoService,
    private funcionarioAcademicoService: FuncionarioAcademicoService,
    private errorHandlerService: ErrorHandlerService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando = true;
    this.errorCarga = false;
    this.catalogoAcademicoService
      .getTiposSolicitud()
      .pipe(finalize(() => (this.cargando = false)))
      .subscribe({
        next: tipos => {
          this.tipos = tipos ?? [];
          this.filas = this.tipos.map(t => ({
            uuid: t.uuidTipoSolicitudAcademica,
            'Proceso': t.nombre,
            'Responsable actual': t.nombreFuncionarioAcademico || 'Sin responsable'
          }));
        },
        error: err => {
          this.tipos = [];
          this.filas = [];
          this.errorCarga = true;
          this.errorHandlerService.handleError(err, 'Error', 'No se pudieron cargar los tipos de solicitud');
        }
      });
  }

  abrir(fila: Record<string, string>): void {
    this.seleccionado = this.tipos.find(t => t.uuidTipoSolicitudAcademica === fila['uuid']) ?? null;
    if (!this.seleccionado || this.cargandoFuncionarios) return;
    this.funcionarioElegido = this.seleccionado.uuidFuncionarioAcademico;
    this.error = '';
    this.erroresServidor = [];
    this.cargandoFuncionarios = true;
    this.funcionarioAcademicoService
      .getFuncionariosAcademicosPaginado(0, MAXIMO_FUNCIONARIOS_EN_LISTA)
      .pipe(finalize(() => (this.cargandoFuncionarios = false)))
      .subscribe({
        next: pagina => {
          const funcionarios = pagina.content ?? [];
          this.hayMasFuncionarios = (pagina.totalElements ?? 0) > funcionarios.length;
          this.opcionesFuncionario = funcionarios.map(f => ({
            label: `${f.apellidos} ${f.nombres} - ${f.dependencia}`,
            value: f.uuidUsuario
          }));
          if (this.opcionesFuncionario.length === 0) {
            this.errorHandlerService.handleError(
              { message: 'No hay funcionarios académicos registrados' },
              'Error',
              'No se puede cambiar el responsable'
            );
            return;
          }
          this.dialogoVisible = true;
        },
        error: err => this.errorHandlerService.handleError(err, 'Error', 'No se pudieron cargar los funcionarios académicos')
      });
  }

  cerrar(visible: boolean): void {
    if (!visible && !this.enviando) this.dialogoVisible = false;
  }

  guardar(): void {
    if (this.enviando || !this.seleccionado) return;
    if (!this.funcionarioElegido) {
      this.error = 'Elige el funcionario académico responsable.';
      return;
    }
    if (this.funcionarioElegido === this.seleccionado.uuidFuncionarioAcademico) {
      this.error = 'Ese funcionario ya es el responsable de este proceso.';
      return;
    }
    this.error = '';
    this.erroresServidor = [];
    this.enviando = true;
    this.catalogoAcademicoService
      .asignarFuncionarioAcademico(this.seleccionado.uuidTipoSolicitudAcademica, { funcionarioUuid: this.funcionarioElegido })
      .pipe(finalize(() => (this.enviando = false)))
      .subscribe({
        next: actualizado => {
          this.toastService.showSuccess('Listo', `${actualizado.nombre} quedó a cargo de ${actualizado.nombreFuncionarioAcademico ?? 'el funcionario elegido'}.`);
          this.dialogoVisible = false;
          this.cargar();
        },
        error: err => {
          this.erroresServidor = mensajesDeError(err);
          this.errorHandlerService.handleError(err, 'Error', 'No se pudo cambiar el responsable');
        }
      });
  }
}
