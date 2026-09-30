/**
 * Componente de gestión de Orden del Día.
 * Permite visualizar, crear, actualizar y filtrar órdenes del día con paginación.
 * También ofrece un visualizador de orden del día individual.
 * 
 * @autor Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
import { Component, OnInit, ViewChild, TemplateRef, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SolicitudesService } from '../../../../core/services/solicitudes-service';
import { TableGenericComponent } from '../../../table-generic-component/table-generic-component';
import { ButtonComponent } from '../../../buttons/button-component/button-component';
import { CardMainComponent } from '../../../card-main-component/card-main-component';
import { Paginator } from '../../../paginator/paginator';
import { BarraBusquedaComponent } from '../../../search/barra-busqueda-component/barra-busqueda-component';
import { GenericDialogInfoComponent } from '../../../generic-dialog-info-component/generic-dialog-info-component';
import { GenericDialogFormComponent } from '../../../generic-dialog-form-component/generic-dialog-form-component';
import { InputTextComponent } from '../../../inputs/input-text-component/input-text-component';
import { OrdenDelDiaDTOPeticion } from '../../../../core/models/Solicitudes/DTORequest/OrdenDelDiaDTOPeticion';
import { OrdenDelDiaDTORespuesta } from '../../../../core/models/Solicitudes/DTOResponse/OrdenDelDiaDTORespuesta';
import { ToastService } from '../../../../core/services/toast-service';
import { ErrorHandlerService } from '../../../../core/services/error-handler-service';
import { InputTextTareaComponent } from '../../../inputs/input-text-tarea-component/input-text-tarea-component';
import { InputDateComponent } from '../../../inputs/input-date-component/input-date-component';
import { InputSelectComponent } from '../../../inputs/input-select-component/input-select-component';
import { ESTADO_BOOLEANO, ACTIVO, INACTIVO } from '../../../../core/constantes/constantes';
import { OrdenDelDiaVisualizadorComponent } from '../../../../core/secretario-general/components/orden-del-dia-visualizador-component/orden-del-dia-visualizador-component';

@Component({
  selector: 'app-orden-del-dia-content-component',
  imports: [
    CommonModule,
    CardMainComponent,
    Paginator,
    ButtonComponent,
    BarraBusquedaComponent,
    GenericDialogInfoComponent,
    GenericDialogFormComponent,
    InputTextComponent,
    InputTextTareaComponent,
    InputDateComponent,
    InputSelectComponent,
    OrdenDelDiaVisualizadorComponent
  ],
  templateUrl: './orden-del-dia-content-component.html',
  styleUrl: './orden-del-dia-content-component.css'
})
export class OrdenDelDiaContentComponent implements OnInit, AfterViewInit {

  /** Inputs para creación de orden */
  @ViewChild('inputNombre') inputNombre!: InputTextComponent;
  @ViewChild('inputDescripcion') inputDescripcion!: InputTextTareaComponent;
  @ViewChild('inputCiudad') inputCiudad!: InputTextComponent;
  @ViewChild('inputFecha') inputFecha!: InputDateComponent;
  @ViewChild('inputHoraInicio') inputHoraInicio!: InputTextComponent;
  @ViewChild('inputHoraFin') inputHoraFin!: InputTextComponent;
  @ViewChild('inputLugar') inputLugar!: InputTextComponent;
  @ViewChild('inputNumeroActa') inputNumeroActa!: InputTextComponent;

  /** Inputs para actualización de orden */
  @ViewChild('inputNombreActualizar') inputNombreActualizar!: InputTextComponent;
  @ViewChild('inputDescripcionActualizar') inputDescripcionActualizar!: InputTextTareaComponent;
  @ViewChild('inputCiudadActualizar') inputCiudadActualizar!: InputTextComponent;
  @ViewChild('inputFechaActualizar') inputFechaActualizar!: InputDateComponent;
  @ViewChild('inputHoraInicioActualizar') inputHoraInicioActualizar!: InputTextComponent;
  @ViewChild('inputHoraFinActualizar') inputHoraFinActualizar!: InputTextComponent;
  @ViewChild('inputLugarActualizar') inputLugarActualizar!: InputTextComponent;
  @ViewChild('inputNumeroActaActualizar') inputNumeroActaActualizar!: InputTextComponent;
  @ViewChild('inputEstadoActualizar') inputEstadoActualizar!: InputSelectComponent;

  /** Template de búsqueda de número de acta */
  @ViewChild('busquedaNumeroActa') busquedaNumeroActa!: TemplateRef<any>;

  /** Flags de visibilidad de diálogos */
  ordenInfoDialogVisible = false;
  ordenFormDialogVisible = false;
  ordenFormActualizarDialogVisible = false;
  ordenDelDiaVisible: boolean = false;

  /** Listas y objetos seleccionados */
  selectedOrdenInfo: any = null;
  selectedOrdenForm: any = {};
  selectedOrdenActualizarForm: any = {};
  ordenSeleccionada: any;

  /** Paginación */
  currentPage = 1;
  pageSize = 5;
  totalElements = 0;
  totalPages = 1;
  busquedaActual = '';

  /** Componentes de tabla y pretitle */
  tableComponent = TableGenericComponent;
  pretitleComponentComponent = ButtonComponent;

  /** Botones en la tarjeta principal */
  buttonsCard: any[] = [];

  /** Encabezados de la tabla */
  headers = [
    { title: 'acta', headerTemplate: this.busquedaNumeroActa },
    { title: 'estado' }
  ];

  /** Datos de la página actual */
  paginatedData: any[] = [];

  /** Estados booleanos disponibles */
  estadosBooleano = ESTADO_BOOLEANO;

  constructor(
    private solicitudesService: SolicitudesService,
    private toastService: ToastService,
    private errorHandlerService: ErrorHandlerService
  ) {}

  /** Inicializa el componente y carga órdenes */
  ngOnInit(): void {
    this.cargarOrdenes();
    this.buttonsCard = [
      {
        imgUrl: 'buttons/add.svg',
        color: '#1E257B',
        width: '20px',
        height: '20px',
        onClick: () => this.crearOrden()
      }
    ];
  }

  /** Asigna templates a los encabezados después de renderizar */
  ngAfterViewInit(): void {
    this.headers = [
      { title: 'acta', headerTemplate: this.busquedaNumeroActa },
      { title: 'estado' }
    ];
  }

  /**
   * Abre el visualizador de orden del día
   * @param row Fila seleccionada
   */
  abrirOrdenDelDia(row: any) {
    this.solicitudesService.getOrdenDelDia(row.uuidOrdenDelDia).subscribe({
      next: (orden: OrdenDelDiaDTORespuesta) => {
        this.ordenSeleccionada = orden;
        this.ordenSeleccionada.estado = orden.estado ? ACTIVO : INACTIVO;
        this.ordenDelDiaVisible = true;
      }
    });
  }

  /**
   * Carga las órdenes del día desde el backend
   * @param page Página a cargar
   * @param filtro Filtro por número de acta
   */
  cargarOrdenes(page: number = 1, filtro: string = ''): void {
    const backendPage = page - 1;
    const observable = filtro
      ? this.solicitudesService.buscarOrdenesDelDia(filtro, backendPage, this.pageSize)
      : this.solicitudesService.getOrdenesDelDiaPaginado(backendPage, this.pageSize);

    observable.subscribe({
      next: (resp) => {
        if (!resp.content?.length) {
          this.paginatedData = [];
          this.totalElements = 0;
          return;
        }
        this.paginatedData = resp.content.map(o => ({
          uuidOrdenDelDia: o.uuidOrdenDelDia,
          nombre: o.nombre,
          acta: o.numeroActa,
          estado: o.estado ? ACTIVO : INACTIVO
        }));
        this.totalElements = resp.totalElements;
        this.totalPages = Math.ceil(this.totalElements / this.pageSize);
        this.currentPage = page;
        this.busquedaActual = filtro;
      },
      error: () => {
        this.paginatedData = [];
        this.totalElements = 0;
        this.totalPages = 0;
        this.currentPage = 1;
        this.busquedaActual = filtro;
      }
    });
  }

  /**
   * Filtra las órdenes por número de acta
   * @param numeroActa Número de acta a filtrar
   */
  onBuscarPorNumeroActa(numeroActa: string): void {
    this.currentPage = 1;
    this.cargarOrdenes(1, numeroActa);
  }

  /**
   * Cambia la página actual
   * @param page Número de página
   */
  onPageChange(page: number): void {
    this.cargarOrdenes(page, this.busquedaActual);
  }

  /** Inicializa el formulario para crear una nueva orden */
  crearOrden(): void {
    this.selectedOrdenForm = {};
    this.inputNombre.reset();
    this.inputDescripcion.reset();
    this.inputCiudad.reset();
    this.inputFecha.reset();
    this.inputHoraInicio.reset();
    this.inputHoraFin.reset();
    this.inputLugar.reset();
    this.inputNumeroActa.reset();
    this.ordenFormDialogVisible = true;
  }

  /** Guarda la nueva orden en el backend */
  guardarOrden(): void {
    this.inputNombre.touched = true;
    this.inputNumeroActa.touched = true;

    if (this.inputNombre.isInvalid() || this.inputNumeroActa.isInvalid()) {
      this.toastService.showError('Error', 'Complete los campos requeridos');
      return;
    }

    const peticion: OrdenDelDiaDTOPeticion = {
      nombre: this.selectedOrdenForm.nombre,
      descripcion: this.selectedOrdenForm.descripcion,
      ciudad: this.selectedOrdenForm.ciudad,
      fecha: this.selectedOrdenForm.fecha,
      horaInicio: this.selectedOrdenForm.horaInicio,
      horaFin: this.selectedOrdenForm.horaFin,
      lugarReunion: this.selectedOrdenForm.lugarReunion,
      numeroActa: this.selectedOrdenForm.numeroActa,
      estado: true
    };

    this.solicitudesService.crearOrdenDelDia(peticion).subscribe({
      next: () => {
        this.toastService.showSuccess('Éxito', 'Orden del día creada');
        this.ordenFormDialogVisible = false;
        this.cargarOrdenes();
      },
      error: err => this.errorHandlerService.handleError(err, 'Error creando orden')
    });
  }

  /**
   * Abre el formulario de actualización con datos precargados
   * @param row Fila seleccionada
   */
  abrirActualizarOrden(row: any): void {
    this.solicitudesService.getOrdenDelDia(row.uuidOrdenDelDia).subscribe({
      next: (orden: OrdenDelDiaDTORespuesta) => {
        this.selectedOrdenActualizarForm = orden;
        this.selectedOrdenActualizarForm.estado = orden.estado ? ACTIVO : INACTIVO;
        this.ordenFormActualizarDialogVisible = true;
      }
    });
  }

  /** Guarda los cambios de la orden actualizada */
  guardarOrdenActualizada(): void {
    this.inputNombreActualizar.touched = true;
    this.inputNumeroActaActualizar.touched = true;

    if (this.inputNombreActualizar.isInvalid() || this.inputNumeroActaActualizar.isInvalid()) {
      this.toastService.showError('Error', 'Complete los campos requeridos');
      return;
    }

    const peticion: OrdenDelDiaDTOPeticion = {
      nombre: this.selectedOrdenActualizarForm.nombre,
      descripcion: this.selectedOrdenActualizarForm.descripcion,
      ciudad: this.selectedOrdenActualizarForm.ciudad,
      fecha: this.selectedOrdenActualizarForm.fecha,
      horaInicio: this.selectedOrdenActualizarForm.horaInicio,
      horaFin: this.selectedOrdenActualizarForm.horaFin,
      lugarReunion: this.selectedOrdenActualizarForm.lugarReunion,
      numeroActa: this.selectedOrdenActualizarForm.numeroActa,
      estado: this.selectedOrdenActualizarForm.estado === ACTIVO,
    };

    this.solicitudesService.actualizarOrdenDelDia(
      this.selectedOrdenActualizarForm.uuidOrdenDelDia,
      peticion
    ).subscribe({
      next: () => {
        this.toastService.showSuccess('Éxito', 'Orden actualizada');
        this.ordenFormActualizarDialogVisible = false;
        this.cargarOrdenes(this.currentPage, this.busquedaActual);
      },
      error: err => this.errorHandlerService.handleError(err, 'Error actualizando orden')
    });
  }

  /**
   * Muestra información detallada de la orden del día
   * @param row Fila seleccionada
   */
  verMasInfo(row: any): void {
    this.solicitudesService.getOrdenDelDia(row.uuidOrdenDelDia).subscribe({
      next: (orden: OrdenDelDiaDTORespuesta) => {
        this.selectedOrdenInfo = {
          Uuid: orden.uuidOrdenDelDia,
          Nombre: orden.nombre,
          Descripción: orden.descripcion,
          Ciudad: orden.ciudad,
          Fecha: orden.fecha,
          Hora_Inicio: orden.horaInicio,
          Hora_Fin: orden.horaFin,
          Lugar: orden.lugarReunion,
          Acta: orden.numeroActa,
          Estado: orden.estado ? ACTIVO : INACTIVO
        };
        this.ordenInfoDialogVisible = true;
      }
    });
  }
}
