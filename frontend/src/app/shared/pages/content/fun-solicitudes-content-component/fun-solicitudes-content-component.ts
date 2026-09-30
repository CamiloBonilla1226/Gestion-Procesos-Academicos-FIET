/**
 * Componente de solicitudes para funcionarios.
 * Permite listar, filtrar, paginar y actualizar solicitudes asignadas a un funcionario,
 * además de mostrar información detallada y anexos de cada solicitud.
 * 
 * @autor Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
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
import { InputTextComponent } from '../../../inputs/input-text-component/input-text-component';
import { InputSelectComponent } from '../../../inputs/input-select-component/input-select-component';
import { ToastService } from '../../../../core/services/toast-service';
import { ErrorHandlerService } from '../../../../core/services/error-handler-service';
import { GenericDialogFormComponent } from '../../../generic-dialog-form-component/generic-dialog-form-component';
import { InputTextTareaComponent } from '../../../inputs/input-text-tarea-component/input-text-tarea-component';
import { AuthService } from '../../../../core/services/auth-service';
import { ESTADOS_SOLICITUD } from '../../../../core/constantes/constantes';
import { EnviarSolicitudUsuarioPublicoComponent } from '../../../../core/usuario publico/components/enviar-solicitud-usuario-publico-component/enviar-solicitud-usuario-publico-component';

@Component({
  selector: 'app-fun-solicitudes-content-component',
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
    InputTextTareaComponent,
    EnviarSolicitudUsuarioPublicoComponent
  ],
  templateUrl: './fun-solicitudes-content-component.html',
  styleUrl: './fun-solicitudes-content-component.css'
})
export class FunSolicitudesContentComponent implements OnInit {
  /** Flag para mostrar el diálogo de información de solicitud */
  solicitudInfoDialogVisible = false;

  /** Flag para indicar si el diálogo usa un componente */
  dialogUsaComponente: boolean = false;

  /** Flag de visibilidad del diálogo de actualización */
  actualizarDialogVisible = false;

  /** Lista de anexos de la solicitud seleccionada */
  solicitudAnexos: any[] = [];

  /** Objeto seleccionado para mostrar información de la solicitud */
  selectedSolicitudInfo: any = null;

  /** Componente de tabla genérica */
  tableComponent = TableGenericComponent;

  /** Componente pre-title */
  pretitleComponentComponent = ButtonComponent;

  /** Datos paginados */
  paginatedData: any[] = [];

  /** Objeto temporal para actualizar solicitud */
  solicitudUpdate: any = {};

  /** Filtro de búsqueda */
  solicitudFiltro: string = '';

  /** Objeto temporal para la actualización de solicitud */
  solicitudActualizar: any = {};

  /** Flag para mostrar el diálogo de actualización */
  actualizarSolicitudDialogVisible = false;

  /** Flag para mostrar el diálogo de enviar solicitud */
  enviarSolicitudVisible = false;

  /** Paginación */
  currentPage = 1;
  pageSize = 5;
  totalPages = 1;
  totalElements = 0;

  /** Usuario actual */
  usuario: any;

  /** Botones del card principal */
  buttonsCard: any[] = [];

  /** Constantes de estados de solicitud */
  estadosSolicitud = ESTADOS_SOLICITUD;

  /** Header con filtro incrustado */
  @ViewChild('headerSolicitud') headerSolicitud!: TemplateRef<any>;

  @ViewChild('inputNombreActualizar') inputNombreActualizar!: InputTextComponent;
  @ViewChild('inputEstadoActualizar') inputEstadoActualizar!: InputSelectComponent;
  @ViewChild('inputDescripcionActualizar') inputDescripcionActualizar!: InputTextComponent;
  @ViewChild('inputConsecutivoActualizar') inputConsecutivoActualizar!: InputTextComponent;

  /** Encabezados de la tabla */
  headers: any[] = [
    { title: 'Solicitud', headerTemplate: null },
    { title: 'Estado', headerTemplate: null }
  ];

  constructor(
    private solicitudesService: SolicitudesService,
    private toastService: ToastService,
    private errorHandlerService: ErrorHandlerService,
    private authService: AuthService
  ) {}

  /** Inicializa el componente */
  ngOnInit(): void {
    this.buttonsCard = [
      {
        imgUrl: 'buttons/campana.svg',
        color: '#1E257B',
        width: '20px',
        height: '20px',
        onClick: () => this.onRealizarSolicitud()
      }
    ];

    this.authService.usuario$.subscribe(user => {
      this.usuario = user;
    });

    this.loadSolicitudes();
  }

  /** Inicializa headers de tabla después de renderizado */
  ngAfterViewInit(): void {
    this.headers = [
      { title: 'Solicitud', headerTemplate: this.headerSolicitud },
      { title: 'Estado', headerTemplate: null }
    ];
  }

  /** Abre el diálogo para realizar una nueva solicitud */
  onRealizarSolicitud(): void {
    this.enviarSolicitudVisible = true;
  }

  /**
   * Carga las solicitudes con paginación y filtros
   * @param page Página a cargar (por defecto 1)
   */
  loadSolicitudes(page: number = 1): void {
    const backendPage = page - 1;

    const observable = this.solicitudFiltro && this.solicitudFiltro.trim() !== ''
      ? this.solicitudesService.buscarSolicitudesPorNombreYFuncionario(this.usuario.uuidUsuario, this.solicitudFiltro.trim(), backendPage, this.pageSize)
      : this.solicitudesService.getSolicitudesPorFuncionario(this.usuario.uuidUsuario, backendPage, this.pageSize);

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
          Solicitud: s.nombre || s.titulo || s.asunto || '',
          Responsable: `${s.objFuncionario?.nombres} ${s.objFuncionario?.apellidos}`.trim(),
          Estado: s.estado || s.estadoSolicitud || ''
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

  /**
   * Abre el diálogo de información detallada de una solicitud
   * @param solicitud Solicitud a mostrar
   */
  protected verMasInfo(solicitud: SolicitudDTORespuesta): void {
    this.solicitudesService.getSolicitud(solicitud.uuidSolicitud).subscribe({
      next: (solicitudDetallada) => {
        const tieneAnexos = solicitudDetallada.anexos && solicitudDetallada.anexos.length > 0;

        this.selectedSolicitudInfo = {
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

  /**
   * Abre el modal de actualización de solicitud
   * @param row Fila de la tabla con los datos de la solicitud
   */
  abrirModalActualizarSolicitud(row: any): void {
    this.solicitudActualizar = {
      uuidSolicitud: row.uuidSolicitud,
      nombre: row.nombre,
      estado: row.estado,
      descripcion: row.descripcion,
      consecutivo: row.consecutivo,
      uuidOrdenDelDia: row.ordenDelDia?.uuidOrdenDelDia || null
    };
    this.actualizarSolicitudDialogVisible = true;
  }

  /** Guarda los cambios realizados a la solicitud seleccionada */
  guardarSolicitudActualizada(): void {
    this.inputNombreActualizar.touched = true;
    this.inputEstadoActualizar.touched = true;

    if (
      this.inputNombreActualizar.isInvalid() ||
      this.inputEstadoActualizar.isInvalid()
    ) {
      this.toastService.showError('Error', 'Completa todos los campos requeridos.');
      return;
    }

    const peticion: any = {
      nombre: this.solicitudActualizar.nombre,
      consecutivo: this.solicitudActualizar.consecutivo,
      descripcion: this.solicitudActualizar.descripcion,
      estado: this.solicitudActualizar.estado,
      uuidOrdenDelDia: this.solicitudActualizar.uuidOrdenDelDia || null
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

  /** Resetea los formularios de actualización */
  private resetFormulariosActualizar(): void {
    this.solicitudActualizar = {};
    this.inputNombreActualizar?.reset();
    this.inputDescripcionActualizar?.reset();
    this.inputConsecutivoActualizar?.reset();
    this.inputEstadoActualizar?.reset();
  }

  /**
   * Filtra las solicitudes según el valor ingresado
   * @param value Texto de búsqueda
   */
  buscarSolicitudes(value: string): void {
    this.solicitudFiltro = value ?? '';
    this.currentPage = 1;
    this.loadSolicitudes(1);
  }

  /**
   * Cambia la página actual y recarga las solicitudes
   * @param page Número de página
   */
  onPageChange(page: number): void {
    if (page < 1 || page > this.totalPages) return;
    this.loadSolicitudes(page);
  }
}
