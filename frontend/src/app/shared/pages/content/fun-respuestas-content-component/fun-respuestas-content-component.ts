import { Component, OnInit, ViewChild, TemplateRef, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RespuestasService } from '../../../../core/services/respuesta-service';
import { TableGenericComponent } from '../../../table-generic-component/table-generic-component';
import { CardMainComponent } from '../../../card-main-component/card-main-component';
import { Paginator } from '../../../paginator/paginator';
import { BarraBusquedaComponent } from '../../../search/barra-busqueda-component/barra-busqueda-component';
import { ButtonComponent } from '../../../buttons/button-component/button-component';
import { GenericDialogInfoComponent } from '../../../generic-dialog-info-component/generic-dialog-info-component';
import { DialogGestionRespuestaComponent } from '../../../dialog-gestion-respuesta-component/dialog-gestion-respuesta-component';
import { DescargarRespuestaComponent } from '../../../descargar-respuesta-component/descargar-respuesta-component';
import { AuthService } from '../../../../core/services/auth-service';

/**
 * Componente encargado de gestionar y mostrar las respuestas de solicitudes.
 * Permite paginar, buscar, visualizar detalles y gestionar la carga de respuestas.
 *
 * @author Julian David Camacho Erazo
 * {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-fun-respuestas-content-component',
  imports: [
    CommonModule,
    CardMainComponent,
    Paginator,
    BarraBusquedaComponent,
    ButtonComponent,
    GenericDialogInfoComponent,
    DialogGestionRespuestaComponent,
    DescargarRespuestaComponent,
  ],
  templateUrl: './fun-respuestas-content-component.html',
  styleUrl: './fun-respuestas-content-component.css'
})
export class FunRespuestasContentComponent implements OnInit, AfterViewInit{
  /** Template para la búsqueda de solicitudes */
  @ViewChild('busquedaSolicitud') busquedaSolicitud!: TemplateRef<any>;

  /** Componente de tabla genérica */
  tableComponent = TableGenericComponent;

  /** Componente del botón en la cabecera */
  pretitleComponentComponent = ButtonComponent;

  /** Controla la visibilidad del diálogo de gestión */
  dialogGestionVisible = false;

  /** UUID de la respuesta seleccionada para gestión */
  selectedRespuestaUuid!: string;

  /** Encabezados de la tabla */
  headers = [{ title: 'solicitud', headerTemplate: this.busquedaSolicitud }, { title: 'estado' }];

  /** Datos paginados de las respuestas */
  paginatedData: any[] = [];

  /** Página actual */
  currentPage = 1;

  /** Tamaño de página para la paginación */
  pageSize = 5;

  /** Total de elementos obtenidos */
  totalElements = 0;

  /** Total de páginas calculadas */
  totalPages = 1;

  /** Búsqueda actual realizada */
  busquedaActual = '';

  /** Controla la visibilidad del diálogo de información detallada */
  respuestaInfoDialogVisible = false;

  /** Respuesta seleccionada para mostrar información detallada */
  selectedRespuestaInfo: any = null;

  /** URL del archivo de respuesta descargable */
  respuestaUrl: string | null = null;

  /** Nombre del archivo de respuesta descargable */
  nombreArchivoRespuesta: string | null = null;

  /** Usuario actual */
  usuario: any;

  constructor(
    private respuestasService: RespuestasService,
    private authService: AuthService
  )
    {}

  /** Inicializa la carga de respuestas al iniciar el componente */
  ngOnInit(): void {
    this.authService.usuario$.subscribe(user => {
      this.usuario = user;
    });

    this.cargarRespuestas();
  }

  /** Configura los encabezados de la tabla después de inicializar la vista */
  ngAfterViewInit(): void {
    this.headers = [
      { title: 'solicitud', headerTemplate: this.busquedaSolicitud },
      { title: 'estado' },
    ];
  }

  /**
   * Carga las respuestas desde el servicio con soporte para paginación y búsqueda.
   * @param page Página a cargar (por defecto 1)
   * @param busqueda Término de búsqueda para filtrar solicitudes
   */
  cargarRespuestas(page: number = 1, busqueda: string = ''): void {
    const backendPage = page - 1;

    const observable =
      busqueda && busqueda.trim()
        ? this.respuestasService.getRespuestasPorFuncionarioYNombreSolicitud(
            this.usuario.uuidUsuario,
            busqueda,
            backendPage,
            this.pageSize
          )
        : this.respuestasService.getRespuestasPorFuncionario(this.usuario.uuidUsuario, backendPage, this.pageSize);

    observable.subscribe({
      next: (resp) => {
        if (!resp.content || resp.content.length === 0) {
          this.paginatedData = [];
          this.totalElements = 0;
          this.totalPages = 0;
          this.currentPage = 1;
          this.busquedaActual = busqueda;
          return;
        }

        this.paginatedData = resp.content.map((r) => ({
          uuidRespuesta: r.uuidRespuesta,
          solicitud: r.solicitud?.nombre,
          estado: r.solicitud?.estado,
        }));

        this.totalElements = resp.totalElements;
        this.totalPages = Math.ceil(this.totalElements / this.pageSize);
        this.currentPage = Math.min(page, this.totalPages);
        this.busquedaActual = busqueda;
      },
      error: () => {
        this.paginatedData = [];
        this.totalElements = 0;
        this.totalPages = 0;
        this.currentPage = 1;
        this.busquedaActual = busqueda;
      },
    });
  }

  /**
   * Busca respuestas filtrando por el nombre de la solicitud.
   * Reinicia la paginación.
   * @param nombre Nombre de la solicitud a buscar
   */
  onBuscarSolicitud(nombre: string): void {
    this.currentPage = 1;
    this.cargarRespuestas(1, nombre);
  }

  /**
   * Cambia la página actual y carga los datos correspondientes.
   * @param page Número de página a mostrar
   */
  onPageChange(page: number): void {
    if (page < 1 || page > this.totalPages) return;
    this.cargarRespuestas(page, this.busquedaActual);
  }

  /**
   * Muestra información detallada de la respuesta seleccionada en un diálogo.
   * @param respuesta Respuesta seleccionada
   */
  verMasInfo(respuesta: any): void {
    this.respuestasService.getRespuesta(respuesta.uuidRespuesta).subscribe({
      next: (detalle) => {
        this.selectedRespuestaInfo = {
          Solicitante:
            detalle.solicitud.informacionSolicitante.nombres +
            ' ' +
            detalle.solicitud.informacionSolicitante.apellidos,
          Identificacion:
            detalle.solicitud.informacionSolicitante.tipoDocumento +
            ' ' +
            detalle.solicitud.informacionSolicitante.numeroDocumento,
          Contacto:
            detalle.solicitud.informacionSolicitante.correoElectronico +
            ' / ' +
            detalle.solicitud.informacionSolicitante.telefono,
          Tipo_Respuesta: detalle.tipoRespuesta,
          Consecutivo_FIET: detalle.consecutivoFiet,
          Respuesta_Consejo: detalle.respuestaConsejo,
          Indicaciones: detalle.indicaciones || 'Sin indicaciones',
        };

        if (detalle.urlRespuesta) {
          this.respuestaUrl = detalle.urlRespuesta;
          const partes = detalle.urlRespuesta.split('/');
          this.nombreArchivoRespuesta = partes.pop() ?? null;
        } else {
          this.respuestaUrl = null;
          this.nombreArchivoRespuesta = null;
        }

        this.respuestaInfoDialogVisible = true;
      },
      error: (err) => console.error('Error cargando detalle', err),
    });
  }

  /**
   * Abre el diálogo para cargar o gestionar la respuesta seleccionada.
   * @param row Fila de la tabla correspondiente a la respuesta
   */
  abrirCargarRespuesta(row: any): void {
    this.selectedRespuestaUuid = row.uuidRespuesta;
    this.dialogGestionVisible = true;
  }
}
