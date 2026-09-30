import { Component, OnInit, AfterViewInit, ViewChild, TemplateRef } from '@angular/core';
import { TableGenericComponent } from '../../../table-generic-component/table-generic-component';
import { CommonModule } from '@angular/common';
import { CardMainComponent } from '../../../card-main-component/card-main-component';
import { ButtonComponent } from '../../../buttons/button-component/button-component';
import { Paginator } from "../../../paginator/paginator";
import { BarraBusquedaComponent } from '../../../search/barra-busqueda-component/barra-busqueda-component';
import { TipoSolicitudService } from '../../../../core/services/tipo-solicitud-service';
import { GenericDialogInfoComponent } from '../../../generic-dialog-info-component/generic-dialog-info-component';
import { TipoSolicitudDTORespuesta } from '../../../../core/models/TipoSolicitud/DTOResponse/TipoSolicitudDTORespuesta';
import { AuthService } from '../../../../core/services/auth-service';
import { EnviarSolicitudUsuarioFietComponent } from '../../../../core/usuario fiet/components/enviar-solicitud-usuario-fiet-component/enviar-solicitud-usuario-fiet-component';

/**
 * Componente que maneja las solicitudes privadas de un usuario Fiet.
 * Permite filtrar por tipo de solicitud, ver información detallada y enviar nuevas solicitudes.
 * Author: Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-usuario-fiet-solicitudes-privadas-content-component',
  imports: [
    CommonModule, 
    CardMainComponent,
    Paginator, 
    BarraBusquedaComponent, 
    ButtonComponent, 
    GenericDialogInfoComponent,
    EnviarSolicitudUsuarioFietComponent
  ],
  templateUrl: './usuario-fiet-solicitudes-privadas-content-component.html',
  styleUrls: ['./usuario-fiet-solicitudes-privadas-content-component.css']
})
export class UsuarioFietSolicitudesPrivadasContentComponent implements OnInit, AfterViewInit {
  
  /** Componente de tabla genérica para mostrar solicitudes */
  tableComponent = TableGenericComponent;

  /** Componente de pretitle (botón) */
  pretitleComponentComponent = ButtonComponent;

  /** Botones mostrados en la tarjeta principal */
  buttonsCard: any[] = [
    {
      imgUrl: 'buttons/campana.svg',
      color: '#1E257B',
      width: '20px',
      height: '20px',
      onClick: () => {
        this.enviarSolicitudVisible = true;
      }
    }
  ];

  /** Encabezados de la tabla */
  headers: any[] = [
    { title: 'Solicitud', headerTemplate: null }
  ];

  /** Datos paginados mostrados en la tabla */
  paginatedData: any[] = [];

  /** Total de elementos para la paginación */
  totalElements = 0;

  /** Total de páginas para la paginación */
  totalPages = 1;

  /** Página actual */
  currentPage = 1;

  /** Tamaño de página */
  pageSize = 5;

  /** Filtro por tipo de solicitud */
  tipoDeSolicitudFiltro: string = '';

  /** Usuario actual */
  usuario: any;

  /** Template de búsqueda de tipo de solicitud */
  @ViewChild('busquedaTipoSolicitud') busquedaTipoSolicitud!: TemplateRef<any>;

  /** Template de acciones en la tabla */
  @ViewChild('actionTemplate') actionTemplate!: TemplateRef<any>;

  /** Indica si el diálogo de enviar solicitud está visible */
  enviarSolicitudVisible = false;

  /** Indica si el diálogo de información está visible */
  tipoInfoDialogVisible = false;

  /** Objeto seleccionado para mostrar información detallada */
  selectedTipoInfo: any = null;

  constructor(private tipoSolicitudesService: TipoSolicitudService, 
              private authService: AuthService) {}

  /** Inicializa el componente y obtiene el usuario actual */
  ngOnInit(): void {
    this.authService.usuario$.subscribe(user => {
      this.usuario = user;
    });
    this.loadTiposSolicitudes();
  }

  /** Inicializa templates de encabezado de la tabla después de renderizar */
  ngAfterViewInit(): void {
    this.headers = [
      { title: 'Solicitud', headerTemplate: this.busquedaTipoSolicitud },
    ];
  }

  /**
   * Carga los tipos de solicitudes del usuario actual
   * @param page Número de página (por defecto 1)
   */
  loadTiposSolicitudes(page: number = 1): void {
    const backendPage = page - 1; 
    const perfilSolicitante = this.usuario?.roles[0].nombre;

    const observable =
      this.tipoDeSolicitudFiltro.trim() !== ''
        ? this.tipoSolicitudesService.getTiposSolicitudPorNombreYPerfilSolicitante(this.tipoDeSolicitudFiltro, perfilSolicitante, backendPage, this.pageSize)
        : this.tipoSolicitudesService.getTiposSolicitudPorPerfilSolicitante(perfilSolicitante, backendPage, this.pageSize);

    observable.subscribe({
      next: (respuesta) => {
        if (!respuesta.content.length) {
          this.paginatedData = [];
          this.totalElements = 0;
          this.totalPages = 1;
          this.currentPage = 1;
          return;
        }

        this.paginatedData = (respuesta.content || []).map((tipoSolicitud:any) => ({
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
   * Filtra la tabla por tipo de solicitud
   * @param filtro Texto del filtro
   */
  onBuscarTipoSolicitud(filtro: string): void {
    this.tipoDeSolicitudFiltro = filtro;
    this.currentPage = 1;
    this.loadTiposSolicitudes(1);
  }

  /**
   * Cambia la página actual en la tabla
   * @param page Número de página
   */
  onPageChange(page: number): void {
    if (page < 1 || page > this.totalPages) return;
    this.loadTiposSolicitudes(page);
  }

  /**
   * Muestra información detallada de un tipo de solicitud
   * @param tipoSolicitud Tipo de solicitud a mostrar
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
