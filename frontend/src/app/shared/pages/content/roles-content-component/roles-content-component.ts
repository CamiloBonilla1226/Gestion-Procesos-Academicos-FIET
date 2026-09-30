/**
 * Componente RolesContentComponent
 * Gestiona la visualización, actualización y paginación de roles.
 * Muestra una tabla genérica con botones de acción, modales para ver o actualizar roles,
 * y soporte para ordenamiento por nombre y descripción.
 * 
 * @autor Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
import { Component, OnInit, ViewChild, TemplateRef, AfterViewInit } from '@angular/core';
import { TableGenericComponent } from '../../../table-generic-component/table-generic-component';
import { CommonModule } from '@angular/common';
import { CardMainComponent } from '../../../card-main-component/card-main-component';
import { ButtonComponent } from '../../../buttons/button-component/button-component';
import { RolesService } from '../../../../core/services/roles-service';
import { RolDTORespuesta } from '../../../../core/models/Rol/DTOResponse/RolDTORespuesta';
import { Paginator } from "../../../paginator/paginator";
import { GenericDialogInfoComponent } from '../../../generic-dialog-info-component/generic-dialog-info-component';
import { GenericDialogFormComponent } from '../../../generic-dialog-form-component/generic-dialog-form-component';
import { InputSelectComponent } from '../../../inputs/input-select-component/input-select-component';
import { RolDTOPeticion } from '../../../../core/models/Rol/DTORequest/RolDTOPeticion';
import { ToastService } from '../../../../core/services/toast-service';
import { ErrorHandlerService } from '../../../../core/services/error-handler-service';
import { InputTextTareaComponent } from '../../../inputs/input-text-tarea-component/input-text-tarea-component';
import { ESTADO_BOOLEANO, ACTIVO, INACTIVO } from '../../../../core/constantes/constantes';

@Component({
  selector: 'app-roles-content-component',
  imports: [
    CommonModule, 
    CardMainComponent, 
    Paginator, 
    ButtonComponent,
    GenericDialogInfoComponent, 
    GenericDialogFormComponent, 
    InputSelectComponent, 
    InputTextTareaComponent
  ],
  templateUrl: './roles-content-component.html',
  styleUrl: './roles-content-component.css'
})
export class RolesContentComponent implements OnInit, AfterViewInit {

  /** Referencias a inputs de formularios y templates de headers */
  @ViewChild('inputDescripcion') inputDescripcion!: InputTextTareaComponent;
  @ViewChild('inputEstado') inputEstado!: InputSelectComponent;
  @ViewChild('botonNombre') botonNombre!: TemplateRef<any>;
  @ViewChild('botonDescripcion') botonDescripcion!: TemplateRef<any>;

  /** Flags de visibilidad de diálogos */
  rolFormDialogVisible = false;
  rolInfoDialogVisible = false;

  /** Objeto temporal para formularios y para mostrar información */
  selectedRolForm: any = {};
  selectedRolInfo: any = null;

  /** Paginación */
  currentPage = 1;
  pageSize = 5;

  /** Estados booleanos disponibles */
  estadosBooleano = ESTADO_BOOLEANO;

  /** Control de ordenamiento */
  private nombreOrdenAsc = true;
  private descripcionOrdenAsc = true;

  /** Componentes utilizados en CardMainComponent */
  tableComponent = TableGenericComponent;
  pretitleComponentComponent = ButtonComponent;
  buttons: any[] = []; 

  /** Cabeceras y datos de la tabla */
  headers = [
    {title: 'nombre', headerTemplate: this.botonNombre}, 
    {title: 'descripcion', headerTemplate: this.botonDescripcion}, 
    {title: 'estado'}
  ];
  data: any[] = [];

  constructor(
    private rolesService: RolesService,
    private toastService: ToastService,
    private errorHandlerService: ErrorHandlerService
  ) {}

  /**
   * Ordena la tabla por el campo nombre
   */
  public ordenarPorNombre(): void {
    this.data.sort((a, b) => this.nombreOrdenAsc ? a.nombre.localeCompare(b.nombre) : b.nombre.localeCompare(a.nombre));
    this.nombreOrdenAsc = !this.nombreOrdenAsc;
  }

  /**
   * Ordena la tabla por el campo descripción
   */
  public ordenarPorDescripcion(): void {
    this.data.sort((a, b) => this.descripcionOrdenAsc ? a.descripcion.localeCompare(b.descripcion) : b.descripcion.localeCompare(a.descripcion));
    this.descripcionOrdenAsc = !this.descripcionOrdenAsc;
  }

  /** Inicializa la carga de roles */
  ngOnInit(): void {
    this.loadRoles();
  }

  /** Se ejecuta después de que la vista se inicializa, asigna templates a los headers */
  ngAfterViewInit(): void {
    this.headers = [
      { title: 'nombre', headerTemplate: this.botonNombre },
      { title: 'descripcion', headerTemplate: this.botonDescripcion },
      { title: 'estado' }
    ];
  }

  /** Carga los roles desde el servicio */
  loadRoles(): void {
    this.rolesService.getRoles().subscribe({
      next: (roles: RolDTORespuesta[]) => {
        this.data = roles.map(r => ({
          ...r,
          estado: r.estado ? ACTIVO : INACTIVO
        }));
      },
      error: (err) => {
        console.error('Error cargando roles', err);
      }
    });
  }

  /**
   * Abre el modal para actualizar un rol
   * @param rol Objeto del rol a actualizar
   */
  protected abrirModalActualizarRol(rol: any): void {
    this.selectedRolForm = { descripcion: '', estado: '', ...rol };
    this.rolFormDialogVisible = true;
  }

  /** Guarda los cambios realizados a un rol */
  guardarRolActualizado(): void {
    if (!this.selectedRolForm.uuidRol) return;

    this.inputDescripcion.touched = true;
    this.inputEstado.touched = true;

    if (this.inputDescripcion.isInvalid() || this.inputEstado.isInvalid()){
      this.toastService.showError('Error', 'Completa todos los campos requeridos.');
      return;
    }

    const rolActualizado: RolDTOPeticion = {
      descripcion: this.selectedRolForm.descripcion,
      estado: this.selectedRolForm.estado === ACTIVO
    };

    this.rolesService.actualizarRol(this.selectedRolForm.uuidRol, rolActualizado).subscribe({
      next: (updatedRol) => {
        const index = this.data.findIndex(r => r.uuidRol === updatedRol.uuidRol);
        if (index !== -1) {
          this.data[index] = { ...updatedRol, estado: updatedRol.estado ? ACTIVO : INACTIVO };
        }
        this.rolFormDialogVisible = false;
        this.toastService.showSuccess('Éxito', 'Rol actualizado correctamente');
      },
      error: (err) => {
        this.errorHandlerService.handleError(err, 'Error Actualizando el Rol', 'No se pudo actualizar el Rol');
      }
    });
  }

  /**
   * Abre el diálogo de información de un rol
   * @param rol Objeto del rol a visualizar
   */
  protected verMasInfo(rol: any): void {
    this.selectedRolInfo = rol;
    this.rolInfoDialogVisible = true;
  }

  /** Devuelve el número total de páginas según pageSize */
  get totalPages(): number {
    return this.data.length ? Math.ceil(this.data.length / this.pageSize) : 1;
  }

  /** Devuelve los datos paginados para la tabla */
  get paginatedData() {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.data.slice(start, start + this.pageSize);
  }

  /**
   * Actualiza la página actual al cambiar el paginador
   * @param page Número de página seleccionado
   */
  onPageChange(page: number) {
    this.currentPage = page;
  }
}
