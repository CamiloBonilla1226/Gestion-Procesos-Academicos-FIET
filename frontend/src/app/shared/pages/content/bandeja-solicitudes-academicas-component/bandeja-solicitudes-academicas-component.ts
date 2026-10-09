import { Component, Input, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { TableGenericComponent, TableHeader } from '../../../table-generic-component/table-generic-component';
import { Paginator } from '../../../paginator/paginator';
import { BarraBusquedaComponent } from '../../../search/barra-busqueda-component/barra-busqueda-component';
import { InputSelectComponent } from '../../../inputs/input-select-component/input-select-component';
import { SimpleButtonComponent } from '../../../buttons/simple-button-component/simple-button-component';
import { SolicitudAcademicaService } from '../../../../core/services/solicitud-academica-service';
import { ErrorHandlerService } from '../../../../core/services/error-handler-service';
import { SolicitudAcademicaResumenDTORespuesta } from '../../../../core/models/SolicitudAcademica/DTOResponse/SolicitudAcademicaResumenDTORespuesta';
import {
  ROLES_ETIQUETA,
  RUTAS_SOLICITUDES_POR_ROL,
  RolEtiqueta,
  TIPOS_SOLICITUD_ACADEMICA
} from '../../../../core/constantes/procesos-academicos';
import { TODOS_LOS_TIPOS, filtrarSolicitudes, paginar, totalPaginas } from '../../../../core/utils/bandeja-solicitudes';
import { formatearFechaHora } from '../../../../core/utils/formato';

@Component({
  selector: 'app-bandeja-solicitudes-academicas-component',
  imports: [TableGenericComponent, Paginator, BarraBusquedaComponent, InputSelectComponent, SimpleButtonComponent],
  templateUrl: './bandeja-solicitudes-academicas-component.html'
})
export class BandejaSolicitudesAcademicasComponent implements OnInit {
  @Input({ required: true }) rol!: RolEtiqueta;

  readonly tamanioPagina = 10;

  readonly opcionesTipo = [
    { label: 'Todos', value: TODOS_LOS_TIPOS },
    ...Object.values(TIPOS_SOLICITUD_ACADEMICA).map(tipo => ({ label: tipo, value: tipo }))
  ];

  headers: TableHeader[] = [];
  solicitudes: SolicitudAcademicaResumenDTORespuesta[] = [];
  filtradas: SolicitudAcademicaResumenDTORespuesta[] = [];
  filas: Record<string, string>[] = [];
  tipoFiltro: string = TODOS_LOS_TIPOS;
  textoFiltro = '';
  paginaActual = 1;
  totalDePaginas = 1;
  cargando = false;
  errorCarga = false;

  constructor(
    private solicitudAcademicaService: SolicitudAcademicaService,
    private errorHandlerService: ErrorHandlerService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.headers = this.columnas().map(title => ({ title }));
    this.cargarSolicitudes();
  }

  cargarSolicitudes(): void {
    this.cargando = true;
    this.errorCarga = false;
    this.bandejaDelRol().subscribe({
      next: solicitudes => {
        this.solicitudes = solicitudes ?? [];
        this.cargando = false;
        this.aplicarFiltros();
      },
      error: err => {
        this.solicitudes = [];
        this.cargando = false;
        this.errorCarga = true;
        this.aplicarFiltros();
        this.errorHandlerService.handleError(err, 'Error', 'No se pudieron cargar las solicitudes');
      }
    });
  }

  onTipoChange(tipo: string | null): void {
    const nuevo = tipo ?? TODOS_LOS_TIPOS;
    if (nuevo === this.tipoFiltro) return;
    this.tipoFiltro = nuevo;
    this.aplicarFiltros();
  }

  onBusquedaChange(texto: string): void {
    const nuevo = texto ?? '';
    if (nuevo === this.textoFiltro) return;
    this.textoFiltro = nuevo;
    this.aplicarFiltros();
  }

  onPageChange(pagina: number): void {
    this.paginaActual = pagina;
    this.actualizarPagina();
  }

  verSolicitud(fila: Record<string, string>): void {
    this.router.navigate([RUTAS_SOLICITUDES_POR_ROL[this.rol], fila['uuid']]);
  }

  private aplicarFiltros(): void {
    this.filtradas = filtrarSolicitudes(this.solicitudes, this.tipoFiltro, this.textoFiltro);
    this.paginaActual = 1;
    this.actualizarPagina();
  }

  private actualizarPagina(): void {
    this.totalDePaginas = totalPaginas(this.filtradas.length, this.tamanioPagina);
    this.paginaActual = Math.min(this.paginaActual, this.totalDePaginas);
    this.filas = paginar(this.filtradas, this.paginaActual, this.tamanioPagina).map(solicitud => this.fila(solicitud));
  }

  private columnas(): string[] {
    const comunes = ['Radicado', 'Tipo de solicitud', 'Fecha', 'Estado'];
    if (this.rol === ROLES_ETIQUETA.ESTUDIANTE) return comunes;
    return [...comunes, 'Estudiante', 'Código estudiantil'];
  }

  private fila(solicitud: SolicitudAcademicaResumenDTORespuesta): Record<string, string> {
    return {
      uuid: solicitud.uuidSolicitudAcademica,
      'Radicado': solicitud.radicado,
      'Tipo de solicitud': solicitud.tipoSolicitud,
      'Fecha': formatearFechaHora(solicitud.fechaCreacion),
      'Estado': solicitud.etiqueta,
      'Estudiante': solicitud.nombreEstudiante,
      'Código estudiantil': solicitud.codigoEstudiantil
    };
  }

  private bandejaDelRol(): Observable<SolicitudAcademicaResumenDTORespuesta[]> {
    switch (this.rol) {
      case ROLES_ETIQUETA.ESTUDIANTE:
        return this.solicitudAcademicaService.getSolicitudesEstudiante();
      case ROLES_ETIQUETA.FUNCIONARIO:
        return this.solicitudAcademicaService.getSolicitudesFuncionario();
      default:
        return this.solicitudAcademicaService.getSolicitudesDecano();
    }
  }
}
