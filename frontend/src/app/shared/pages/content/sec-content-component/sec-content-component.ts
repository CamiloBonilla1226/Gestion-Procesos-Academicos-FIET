/**
 * Componente SecContentComponent
 * Gestiona las solicitudes desde la perspectiva del Secretario General.
 * Permite ver, actualizar y filtrar solicitudes, además de manejar paginación y anexos.
 * 
 * Author: Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
import { CommonModule } from '@angular/common';
import { Component, OnInit, ViewChild, TemplateRef } from '@angular/core';
import { CardMainComponent } from '../../../card-main-component/card-main-component';
import { TableGenericComponent } from '../../../table-generic-component/table-generic-component';
import { ButtonComponent } from '../../../buttons/button-component/button-component';
import { SolicitudesService } from '../../../../core/services/solicitudes-service';
import { Paginator } from '../../../paginator/paginator';
import { BarraBusquedaComponent } from '../../../search/barra-busqueda-component/barra-busqueda-component';
import { GenericDialogInfoComponent } from '../../../generic-dialog-info-component/generic-dialog-info-component';
import { SolicitudDTORespuesta } from '../../../../core/models/Solicitudes/DTOResponse/SolicitudDTORespuesta';
import { AnexosViewComponent } from '../../../others/anexos-view-component/anexos-view-component';
import { UsuariosService } from '../../../../core/services/usuarios-service';
import { InputTextComponent } from '../../../inputs/input-text-component/input-text-component';
import { InputSelectComponent } from '../../../inputs/input-select-component/input-select-component';
import { ToastService } from '../../../../core/services/toast-service';
import { ErrorHandlerService } from '../../../../core/services/error-handler-service';
import { GenericDialogFormComponent } from '../../../generic-dialog-form-component/generic-dialog-form-component';
import { InputTextTareaComponent } from '../../../inputs/input-text-tarea-component/input-text-tarea-component';
import { ESTADOS_SOLICITUD } from '../../../../core/constantes/constantes';

@Component({
  selector: 'app-sec-content-component',
  imports: [
    CommonModule,
    CardMainComponent,
    ButtonComponent,
    Paginator,
    BarraBusquedaComponent,
    GenericDialogInfoComponent,
    AnexosViewComponent,
    InputTextComponent,
    InputSelectComponent,
    GenericDialogFormComponent,
    InputTextTareaComponent
  ],
  templateUrl: './sec-content-component.html',
  styleUrl: './sec-content-component.css'
})
export class SecContentComponent implements OnInit {

  /** Flags de visibilidad de diálogos */
  solicitudInfoDialogVisible = false;
  dialogUsaComponente: boolean = false;
  actualizarDialogVisible = false;
  solicitudAnexos: any[] = [];

  /** Objeto seleccionado para mostrar información */
  selectedSolicitudInfo: any = null;

  /** Componentes de CardMain */
  tableComponent = TableGenericComponent;
  pretitleComponentComponent = ButtonComponent;

  /** Datos paginados */
  paginatedData: any[] = [];

  /** Objeto temporal para solicitud que se va a actualizar */
  solicitudUpdate: any = {};
  solicitudFiltro: string = '';

  /** Objeto temporal para actualizar solicitud */
  solicitudActualizar: any = {};

  /** Flag para mostrar diálogo de actualización */
  actualizarSolicitudDialogVisible = false;

  /** Lista de funcionarios para select */
  funcionariosOptions: { label: string; value: string }[] = [];

  /** Estados posibles de la solicitud */
  estadosSolicitud = ESTADOS_SOLICITUD;

  /** Paginación */
  currentPage = 1;
  pageSize = 5;
  totalPages = 1;
  totalElements = 0;

  /** Header con filtro incrustado */
  @ViewChild('headerSolicitud') headerSolicitud!: TemplateRef<any>;

  /** Inputs de actualización */
  @ViewChild('inputNombreActualizar') inputNombreActualizar!: InputTextComponent;
  @ViewChild('inputEstadoActualizar') inputEstadoActualizar!: InputSelectComponent;
  @ViewChild('inputFuncionarioActualizar') inputFuncionarioActualizar!: InputSelectComponent;
  @ViewChild('inputDescripcionActualizar') inputDescripcionActualizar!: InputTextComponent;
  @ViewChild('inputConsecutivoActualizar') inputConsecutivoActualizar!: InputTextComponent;

  /** Cabeceras de tabla */
  headers: any[] = [
    { title: 'Solicitud', headerTemplate: null },
    { title: 'Responsable', headerTemplate: null },
    { title: 'Estado', headerTemplate: null }
  ];

  constructor(
    private solicitudesService: SolicitudesService,
    private usuariosService: UsuariosService,
    private toastService: ToastService,
    private errorHandlerService: ErrorHandlerService
  ) {}

  /** Inicializa la carga de solicitudes y funcionarios */
  ngOnInit(): void {
    this.loadSolicitudes();
    this.loadFuncionarios();
  }

  /** Asigna templates de headers después de inicializar la vista */
  ngAfterViewInit(): void {
    this.headers = [
      { title: 'Solicitud', headerTemplate: this.headerSolicitud },
      { title: 'Responsable', headerTemplate: null },
      { title: 'Estado', headerTemplate: null }
    ];
  }

  /**
   * Carga solicitudes desde el servicio con paginación y filtro por nombre
   * @param page Número de página (1 por defecto)
   */
  loadSolicitudes(page: number = 1): void {
    const backendPage = page - 1;

    const observable = this.solicitudFiltro && this.solicitudFiltro.trim() !== ''
      ? this.solicitudesService.buscarSolicitudesPorNombre(this.solicitudFiltro.trim(), backendPage, this.pageSize)
      : this.solicitudesService.getSolicitudesPaginado(backendPage, this.pageSize);

    observable.subscribe({
      next: (respuesta: any) => {
        const content = respuesta.content || [];
        if (content.length === 0) {
          this.paginatedData = [];
          this.totalElements = 0;
          this.totalPages = 1;
          this.currentPage = 1;
          return;
        }

        this.paginatedData = content.map((s: any) => ({
          ...s,
          Solicitud: s.nombre,
          Responsable: `${s.objFuncionario?.nombres} ${s.objFuncionario?.apellidos}`.trim(),
          Estado: s.estado
        }));

        this.totalElements = respuesta.totalElements ?? 0;
        this.totalPages = this.totalElements > 0 ? Math.ceil(this.totalElements / this.pageSize) : 1;
        this.currentPage = Math.min(page, this.totalPages);
      },
      error: (err) => {
        console.error('Error cargando Solicitudes', err);
        this.paginatedData = [];
        this.totalElements = 0;
        this.totalPages = 1;
        this.currentPage = 1;
      }
    });
  }

  /** Carga la lista de funcionarios para el select */
  loadFuncionarios(): void {
    this.usuariosService.getFuncionarios().subscribe({
      next: (respuesta: any[]) => {
        this.funcionariosOptions = respuesta.map(f => ({
          label: `${f.nombres} ${f.apellidos}`,
          value: f.uuidUsuario
        }));
      },
      error: (err) => {
        console.error('Error cargando funcionarios:', err);
        this.funcionariosOptions = [];
      }
    });
  }

  /**
   * Abre el diálogo de información de una solicitud
   * @param solicitud Solicitud seleccionada
   */
  protected verMasInfo(solicitud: SolicitudDTORespuesta): void {
    this.solicitudesService.getSolicitud(solicitud.uuidSolicitud).subscribe({
      next: (solicitudDetallada) => {
        const tieneAnexos = solicitudDetallada.anexos && solicitudDetallada.anexos.length > 0;

        this.selectedSolicitudInfo = {
          Uuid_solicitud: solicitudDetallada.uuidSolicitud,
          Nombre: solicitudDetallada.nombre || 'N/A',
          Descripción: solicitudDetallada.descripcion,
          Estado: solicitudDetallada.estado,
          Consecutivo: solicitudDetallada.consecutivo || 'Sin consecutivo',
          Orden_del_Día: solicitudDetallada.ordenDelDia
            ? (solicitudDetallada.ordenDelDia.nombre || '')
            : 'Sin orden del día asignado',
          Tipo_de_Solicitud: solicitudDetallada.tipoSolicitud.nombre,
          Solicitante: solicitudDetallada.informacionSolicitante.nombres + " " +
                      solicitudDetallada.informacionSolicitante.apellidos,
          Identificacion: solicitudDetallada.informacionSolicitante.tipoDocumento + " " +
                          solicitudDetallada.informacionSolicitante.numeroDocumento,
          Contacto: solicitudDetallada.informacionSolicitante.correoElectronico + " / " +
                    solicitudDetallada.informacionSolicitante.telefono
        };

        this.solicitudAnexos = solicitudDetallada.anexos || [];
        this.dialogUsaComponente = tieneAnexos;
        this.solicitudInfoDialogVisible = true;
      }
    });
  }

  /** Abre el modal para actualizar una solicitud */
  abrirModalActualizarSolicitud(row: any): void {
    this.solicitudActualizar = {
      uuidSolicitud: row.uuidSolicitud,
      nombre: row.nombre,
      descripcion: row.descripcion,
      estado: row.estado,
      uuidFuncionario: row.objFuncionario?.uuidUsuario || null,
      consecutivo: row.consecutivo,
      uuidOrdenDelDia: row.ordenDelDia?.uuidOrdenDelDia || null
    };
    this.actualizarSolicitudDialogVisible = true;
  }

  /** Guarda los cambios realizados a la solicitud */
  guardarSolicitudActualizada(): void {
    this.inputNombreActualizar.touched = true;
    this.inputEstadoActualizar.touched = true;
    this.inputFuncionarioActualizar.touched = true;

    if (
      this.inputNombreActualizar.isInvalid() ||
      this.inputEstadoActualizar.isInvalid() ||
      this.inputFuncionarioActualizar.isInvalid()
    ) {
      this.toastService.showError('Error', 'Completa todos los campos requeridos.');
      return;
    }

    const peticion: any = {
      nombre: this.solicitudActualizar.nombre,
      consecutivo: this.solicitudActualizar.consecutivo,
      descripcion: this.solicitudActualizar.descripcion,
      estado: this.solicitudActualizar.estado,
      uuidFuncionario: this.solicitudActualizar.uuidFuncionario,
      uuidOrdenDelDia: this.solicitudActualizar.uuidOrdenDelDia
    };

    this.solicitudesService.actualizarSolicitud(this.solicitudActualizar.uuidSolicitud, peticion)
      .subscribe({
        next: () => {
          this.toastService.showSuccess('Solicitud actualizada', 'Cambios guardados correctamente.');
          this.actualizarSolicitudDialogVisible = false;
          this.loadSolicitudes();
          this.resetFormulariosActualizar();
        },
        error: (err) => {
          this.actualizarSolicitudDialogVisible = false;
          this.loadSolicitudes();
          this.resetFormulariosActualizar();
          this.errorHandlerService.handleError(err, 'Error actualizando la solicitud');
        }
      });
  }

  /** Reinicia los formularios de actualización */
  private resetFormulariosActualizar(): void {
    this.solicitudActualizar = {};
    this.inputNombreActualizar?.reset();
    this.inputDescripcionActualizar?.reset();
    this.inputConsecutivoActualizar?.reset();
    this.inputEstadoActualizar?.reset();
    this.inputFuncionarioActualizar?.reset();
  }

  /**
   * Filtra las solicitudes por nombre
   * @param value Texto de búsqueda
   */
  buscarSolicitudes(value: string): void {
    this.solicitudFiltro = value ?? '';
    this.currentPage = 1;
    this.loadSolicitudes(1);
  }

  /**
   * Cambia la página actual de la tabla
   * @param page Número de página seleccionado
   */
  onPageChange(page: number): void {
    if (page < 1 || page > this.totalPages) return;
    this.loadSolicitudes(page);
  }
}
