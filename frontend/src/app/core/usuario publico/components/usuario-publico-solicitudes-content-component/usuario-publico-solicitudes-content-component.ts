import { Component, OnInit, AfterViewInit, TemplateRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TableGenericComponent } from '../../../../shared/table-generic-component/table-generic-component';
import { CardMainComponent } from '../../../../shared/card-main-component/card-main-component';
import { ButtonComponent } from '../../../../shared/buttons/button-component/button-component';
import { BarraBusquedaComponent } from '../../../../shared/search/barra-busqueda-component/barra-busqueda-component';
import { GenericDialogInfoComponent } from '../../../../shared/generic-dialog-info-component/generic-dialog-info-component';
import { Paginator } from '../../../../shared/paginator/paginator';
import { TipoSolicitudService } from '../../../../core/services/tipo-solicitud-service';
import { TipoSolicitudDTORespuesta } from '../../../../core/models/TipoSolicitud/DTOResponse/TipoSolicitudDTORespuesta';

/**
 * Componente encargado de mostrar los tipos de solicitud disponibles
 * para el usuario público, con paginación, búsqueda y visualización
 * de información detallada.
 *
 * @author Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-usuario-publico-solicitudes-content-component',
  standalone: true,
  imports: [
    CommonModule,
    CardMainComponent,
    ButtonComponent,
    BarraBusquedaComponent,
    GenericDialogInfoComponent,
    Paginator
  ],
  templateUrl: './usuario-publico-solicitudes-content-component.html',
  styleUrl: './usuario-publico-solicitudes-content-component.css'
})
export class UsuarioPublicoSolicitudesContentComponent implements OnInit, AfterViewInit {

  /**
   * Referencia al componente genérico de tabla
   */
  tableComponent = TableGenericComponent;

  /**
   * Filtro de búsqueda por nombre del tipo de solicitud
   */
  tipoDeSolicitudFiltro: string = '';

  /**
   * Definición de los encabezados de la tabla
   */
  headers: any[] = [
    { title: 'Solicitud', headerTemplate: null }
  ];

  /**
   * Datos paginados que se muestran en la tabla
   */
  paginatedData: any[] = [];

  /**
   * Total de elementos obtenidos desde el backend
   */
  totalElements = 0;

  /**
   * Total de páginas disponibles
   */
  totalPages = 1;

  /**
   * Página actual
   */
  currentPage = 1;

  /**
   * Tamaño de página
   */
  pageSize = 5;

  /**
   * Texto del filtro de búsqueda
   */
  filtroNombre = '';

  /**
   * Controla la visibilidad del diálogo de información
   */
  tipoInfoDialogVisible = false;

  /**
   * Información del tipo de solicitud seleccionado
   */
  selectedTipoInfo: any = null;

  /**
   * Template para el campo de búsqueda en el encabezado
   */
  @ViewChild('busquedaTipoSolicitud') busquedaTipoSolicitud!: TemplateRef<any>;

  /**
   * Template para acciones de la tabla
   */
  @ViewChild('actionTemplate') actionTemplate!: TemplateRef<any>;

  /**
   * Constructor del componente
   * @param tipoSolicitudesService Servicio para manejo de tipos de solicitud
   */
  constructor(private tipoSolicitudesService: TipoSolicitudService) {}

  /**
   * Inicializa el componente cargando los tipos de solicitud
   */
  ngOnInit(): void {
    this.loadTiposSolicitudes();
  }

  /**
   * Inicializa los encabezados una vez que la vista está cargada
   */
  ngAfterViewInit(): void {
    this.headers = [
      { title: 'Solicitud', headerTemplate: this.busquedaTipoSolicitud }
    ];
  }

  /**
   * Carga los tipos de solicitud según el perfil público,
   * aplicando filtro y paginación
   *
   * @param page Página actual
   */
  loadTiposSolicitudes(page: number = 1): void {
    const backendPage = page - 1;
    let perfilSolicitante = 'Solicitante Publico';

    const observable =
      this.tipoDeSolicitudFiltro.trim() !== ''
        ? this.tipoSolicitudesService.getTiposSolicitudPorNombreYPerfilSolicitante(
            this.tipoDeSolicitudFiltro,
            perfilSolicitante,
            backendPage,
            this.pageSize
          )
        : this.tipoSolicitudesService.getTiposSolicitudPorPerfilSolicitante(
            perfilSolicitante,
            backendPage,
            this.pageSize
          );

    observable.subscribe({
      next: (respuesta) => {
        if (respuesta.content.length === 0) {
          this.paginatedData = [];
          this.totalElements = 0;
          this.totalPages = 1;
          this.currentPage = 1;
          return;
        }

        this.paginatedData = (respuesta.content || []).map((tipoSolicitud: any) => ({
          ...tipoSolicitud,
          Solicitud: tipoSolicitud.nombre,
          Funcionario: `${tipoSolicitud.objFuncionarioEncargado?.nombres ?? ''} ${tipoSolicitud.objFuncionarioEncargado?.apellidos ?? ''}`.trim()
        }));

        this.totalElements = respuesta.totalElements ?? 0;
        this.totalPages = this.totalElements > 0 ? Math.ceil(this.totalElements / this.pageSize) : 1;
        this.currentPage = Math.min(page, this.totalPages);
      },
      error: () => {
        this.paginatedData = [];
        this.totalElements = 0;
        this.totalPages = 1;
        this.currentPage = 1;
      }
    });
  }

  /**
   * Ejecuta la búsqueda por nombre del tipo de solicitud
   *
   * @param filtro Texto ingresado por el usuario
   */
  onBuscarTipoSolicitud(filtro: string): void {
    this.tipoDeSolicitudFiltro = filtro;
    this.currentPage = 1;
    this.loadTiposSolicitudes(1);
  }

  /**
   * Maneja el cambio de página del paginador
   *
   * @param page Página seleccionada
   */
  onPageChange(page: number): void {
    if (page < 1 || page > this.totalPages) return;
    this.loadTiposSolicitudes(page);
  }

  /**
   * Obtiene y muestra información detallada de un tipo de solicitud
   *
   * @param tipoSolicitud Tipo de solicitud seleccionado
   */
  protected verMasInfo(tipoSolicitud: TipoSolicitudDTORespuesta): void {
    this.tipoSolicitudesService.getTipoSolicitud(tipoSolicitud.uuidTipoSolicitud).subscribe({
      next: (tipoSolicitudDetallado: TipoSolicitudDTORespuesta) => {
        const anexosTexto = (tipoSolicitudDetallado.anexos || [])
          .map(a =>
            `\n• ${a.nombre}\n${a.descripcion}\n${a.formato}\nObligatorio: ${a.obligatoriedad ? 'Sí ✅' : 'No ❌'}`
          )
          .join('\n');

        this.selectedTipoInfo = {
          Nombre: tipoSolicitudDetallado.nombre,
          Descripción: tipoSolicitudDetallado.descripcion,
          Anexos: anexosTexto || 'Sin anexos registrados',
          Funcionario_Responsable: `${tipoSolicitudDetallado.objFuncionarioEncargado?.nombres ?? ''} ${tipoSolicitudDetallado.objFuncionarioEncargado?.apellidos ?? ''}`.trim(),
          Correo_Electrónico: tipoSolicitudDetallado.objFuncionarioEncargado?.correoElectronico || 'N/A'
        };

        this.tipoInfoDialogVisible = true;
      },
      error: (err) => {
        console.error('Error cargando Tipo de Solicitud', err);
      }
    });
  }

}
