/**
 * Componente de logs.
 * Muestra un historial de logs con paginación y búsqueda por responsable y fecha.
 * Permite visualizar las acciones y resultados realizados en el sistema.
 * 
 * @autor Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
import { Component, OnInit, ViewChild, TemplateRef } from '@angular/core';
import { TableGenericComponent } from '../../../table-generic-component/table-generic-component';
import { CommonModule } from '@angular/common';
import { CardMainComponent } from '../../../card-main-component/card-main-component';
import { ButtonComponent } from '../../../buttons/button-component/button-component';
import { LogService } from '../../../../core/services/log-service';
import { Paginator } from "../../../paginator/paginator";
import { BarraBusquedaComponent } from '../../../search/barra-busqueda-component/barra-busqueda-component';

@Component({
  selector: 'app-log-content-component',
  imports: [CommonModule, CardMainComponent, Paginator, BarraBusquedaComponent],
  templateUrl: './log-content-component.html',
  styleUrl: './log-content-component.css'
})
export class LogContentComponent implements OnInit {
  /** Página actual */
  currentPage = 1;

  /** Tamaño de página */
  pageSize = 5;

  /** Total de elementos */
  totalElements = 0;

  /** Total de páginas */
  totalPages = 1;

  /** Filtro de responsable */
  responsableFiltro: string = '';

  /** Filtro de fecha */
  fechaFiltro: string = '';

  /** Componente de tabla genérica */
  tableComponent = TableGenericComponent;

  /** Componente pre-title */
  pretitleComponentComponent = ButtonComponent;

  /** Encabezados de la tabla */
  headers: any[] = [
    { title: 'Responsable', headerTemplate: null },
    { title: 'Fecha', headerTemplate: null },
    { title: 'Acción' },
    { title: 'Resultado' }
  ];

  /** Template de búsqueda para responsable */
  @ViewChild('busquedaResponsable') busquedaResponsable!: TemplateRef<any>;

  /** Template de búsqueda para fecha */
  @ViewChild('busquedaFecha') busquedaFecha!: TemplateRef<any>;

  /** Datos de la página actual */
  paginatedData: any[] = [];

  constructor(private logService: LogService) {}

  /** Inicializa el componente y carga los logs */
  ngOnInit(): void {
    this.loadLogs();
  }

  /** Asigna los templates de búsqueda a los encabezados después de renderizado */
  ngAfterViewInit(): void {
    this.headers = [
      { title: 'Responsable', headerTemplate: this.busquedaResponsable },
      { title: 'Fecha', headerTemplate: this.busquedaFecha },
      { title: 'Acción' },
      { title: 'Resultado' }
    ];
  }

  /**
   * Carga los logs desde el servicio con paginación y filtros
   * @param page Página a cargar (por defecto 1)
   */
  loadLogs(page: number = 1): void {
    const backendPage = page - 1; 

    const observable =
      this.responsableFiltro.trim() !== '' || this.fechaFiltro.trim() !== ''
        ? this.logService.getLogsFiltrados(this.responsableFiltro, this.fechaFiltro, backendPage, this.pageSize)
        : this.logService.getLogsPaginado(backendPage, this.pageSize);

    observable.subscribe({
      next: (respuesta) => {
        this.paginatedData = (respuesta.content || []).map((log: any) => ({
          Responsable: `${log.objUsuarioLog?.nombres ?? ''} ${log.objUsuarioLog?.apellidos ?? ''}`.trim(),
          Fecha: log.fecha,
          Acción: log.accion,
          Resultado: log.resultado
        }));

        this.totalElements = respuesta.totalElements ?? 0;
        this.totalPages = this.totalElements > 0 ? Math.ceil(this.totalElements / this.pageSize) : 1;
        this.currentPage = Math.min(page, this.totalPages);
      },
      error: (err) => console.error('Error cargando logs', err)
    });
  }

  /**
   * Filtra los logs por responsable
   * @param responsable Texto a filtrar
   */
  onBuscarResponsable(responsable: string): void {
    this.responsableFiltro = responsable;
    this.currentPage = 1;
    this.loadLogs(1);
  }

  /**
   * Filtra los logs por fecha
   * @param fecha Fecha a filtrar
   */
  onBuscarFecha(fecha: string): void {
    this.fechaFiltro = fecha;
    this.currentPage = 1;
    this.loadLogs(1);
  }

  /**
   * Cambia la página actual y recarga los logs
   * @param page Número de página
   */
  onPageChange(page: number): void {
    if (page < 1 || page > this.totalPages) return;
    this.loadLogs(page);
  }
}
