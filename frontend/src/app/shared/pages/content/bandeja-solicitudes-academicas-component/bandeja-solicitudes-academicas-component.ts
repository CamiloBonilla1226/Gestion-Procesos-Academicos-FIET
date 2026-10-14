import { Component, Input, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { TableGenericComponent, TableHeader } from '../../../table-generic-component/table-generic-component';
import { Paginator } from '../../../paginator/paginator';
import { BarraBusquedaComponent } from '../../../search/barra-busqueda-component/barra-busqueda-component';
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
import { CategoriaEstado, categoriaEstado, textoDecisionDecano } from '../../../../core/constantes/etiquetas-academicas';
import {
  CampoOrden,
  DireccionOrden,
  TODOS_LOS_TIPOS,
  etiquetasDisponibles,
  filtrarSolicitudes,
  ordenarSolicitudes,
  paginar,
  totalPaginas
} from '../../../../core/utils/bandeja-solicitudes';
import { formatearFechaHora } from '../../../../core/utils/formato';

const TITULOS_BANDEJA: Record<RolEtiqueta, string> = {
  ESTUDIANTE: 'Mis trámites radicados',
  FUNCIONARIO: 'Gestión de solicitudes',
  DECANO: 'Solicitudes remitidas por el Funcionario Académico'
};

const COLUMNA_FECHA = 'Fecha de radicación';
const COLUMNA_DECISION_DECANO = 'Decisión del Decano';

@Component({
  selector: 'app-bandeja-solicitudes-academicas-component',
  imports: [TableGenericComponent, Paginator, BarraBusquedaComponent, SimpleButtonComponent],
  templateUrl: './bandeja-solicitudes-academicas-component.html',
  styleUrl: './bandeja-solicitudes-academicas-component.css'
})
export class BandejaSolicitudesAcademicasComponent implements OnInit {
  @Input({ required: true }) rol!: RolEtiqueta;

  @ViewChild('celdaRadicado', { static: true }) celdaRadicado!: TemplateRef<unknown>;
  @ViewChild('celdaEstado', { static: true }) celdaEstado!: TemplateRef<unknown>;
  @ViewChild('ordenFecha', { static: true }) ordenFecha!: TemplateRef<unknown>;
  @ViewChild('ordenEstado', { static: true }) ordenEstado!: TemplateRef<unknown>;

  readonly tamanioPagina = 10;
  readonly todos = TODOS_LOS_TIPOS;
  readonly opcionesTipo = Object.values(TIPOS_SOLICITUD_ACADEMICA);

  headers: TableHeader[] = [];
  solicitudes: SolicitudAcademicaResumenDTORespuesta[] = [];
  filtradas: SolicitudAcademicaResumenDTORespuesta[] = [];
  filas: Record<string, string>[] = [];
  opcionesEstado: string[] = [];
  tipoFiltro: string = TODOS_LOS_TIPOS;
  estadoFiltro: string = TODOS_LOS_TIPOS;
  textoFiltro = '';
  codigoFiltro = '';
  campoOrden: CampoOrden | null = null;
  direccionOrden: DireccionOrden = 'asc';
  paginaActual = 1;
  totalDePaginas = 1;
  cargando = false;
  errorCarga = false;

  constructor(
    private solicitudAcademicaService: SolicitudAcademicaService,
    private errorHandlerService: ErrorHandlerService,
    private router: Router
  ) {}

  get esEstudiante(): boolean {
    return this.rol === ROLES_ETIQUETA.ESTUDIANTE;
  }

  get tituloTarjeta(): string {
    return TITULOS_BANDEJA[this.rol];
  }

  get columnaEstado(): string {
    return this.rol === ROLES_ETIQUETA.DECANO ? 'Decisión' : 'Estado';
  }

  get filtraPorCodigo(): boolean {
    return this.rol === ROLES_ETIQUETA.FUNCIONARIO;
  }

  get hayFiltros(): boolean {
    return (
      this.tipoFiltro !== TODOS_LOS_TIPOS ||
      this.estadoFiltro !== TODOS_LOS_TIPOS ||
      !!this.textoFiltro.trim() ||
      !!this.codigoFiltro.trim()
    );
  }

  ngOnInit(): void {
    this.headers = this.columnas();
    this.cargarSolicitudes();
  }

  cargarSolicitudes(): void {
    this.cargando = true;
    this.errorCarga = false;
    this.bandejaDelRol().subscribe({
      next: solicitudes => {
        this.solicitudes = solicitudes ?? [];
        this.cargando = false;
        this.opcionesEstado = etiquetasDisponibles(this.solicitudes);
        this.aplicarFiltros();
      },
      error: err => {
        this.solicitudes = [];
        this.opcionesEstado = [];
        this.cargando = false;
        this.errorCarga = true;
        this.aplicarFiltros();
        this.errorHandlerService.handleError(err, 'Error', 'No se pudieron cargar las solicitudes');
      }
    });
  }

  onTipoChange(tipo: string | null): void {
    const nuevo = tipo || TODOS_LOS_TIPOS;
    if (nuevo === this.tipoFiltro) return;
    this.tipoFiltro = nuevo;
    this.aplicarFiltros();
  }

  onEstadoChange(estado: string | null): void {
    const nuevo = estado || TODOS_LOS_TIPOS;
    if (nuevo === this.estadoFiltro) return;
    this.estadoFiltro = nuevo;
    this.aplicarFiltros();
  }

  onBusquedaChange(texto: string): void {
    const nuevo = texto ?? '';
    if (nuevo === this.textoFiltro) return;
    this.textoFiltro = nuevo;
    this.aplicarFiltros();
  }

  onCodigoChange(codigo: string): void {
    const nuevo = codigo ?? '';
    if (nuevo === this.codigoFiltro) return;
    this.codigoFiltro = nuevo;
    this.aplicarFiltros();
  }

  limpiarFiltros(): void {
    this.tipoFiltro = TODOS_LOS_TIPOS;
    this.estadoFiltro = TODOS_LOS_TIPOS;
    this.textoFiltro = '';
    this.codigoFiltro = '';
    this.aplicarFiltros();
  }

  ordenarPor(campo: CampoOrden): void {
    this.direccionOrden = this.campoOrden === campo && this.direccionOrden === 'asc' ? 'desc' : 'asc';
    this.campoOrden = campo;
    this.aplicarFiltros();
  }

  onPageChange(pagina: number): void {
    this.paginaActual = pagina;
    this.actualizarPagina();
  }

  categoria(etiqueta: string): CategoriaEstado {
    return categoriaEstado(etiqueta);
  }

  verSolicitud(fila: Record<string, string>): void {
    this.router.navigate([RUTAS_SOLICITUDES_POR_ROL[this.rol], fila['uuid']]);
  }

  private aplicarFiltros(): void {
    const codigo = this.filtraPorCodigo ? this.codigoFiltro : null;
    const filtradas = filtrarSolicitudes(this.solicitudes, this.tipoFiltro, this.textoFiltro, this.estadoFiltro, codigo);
    this.filtradas = ordenarSolicitudes(filtradas, this.campoOrden, this.direccionOrden);
    this.paginaActual = 1;
    this.actualizarPagina();
  }

  private actualizarPagina(): void {
    this.totalDePaginas = totalPaginas(this.filtradas.length, this.tamanioPagina);
    this.paginaActual = Math.min(this.paginaActual, this.totalDePaginas);
    this.filas = paginar(this.filtradas, this.paginaActual, this.tamanioPagina).map(solicitud => this.fila(solicitud));
  }

  private columnas(): TableHeader[] {
    const radicado: TableHeader = { title: 'Radicado', cellTemplate: this.celdaRadicado };
    const tipo: TableHeader = { title: 'Tipo de proceso' };
    const fecha: TableHeader = { title: COLUMNA_FECHA, headerTemplate: this.ordenFecha };
    const estado: TableHeader = { title: this.columnaEstado, headerTemplate: this.ordenEstado, cellTemplate: this.celdaEstado };
    if (this.esEstudiante) return [radicado, tipo, fecha, estado];
    if (this.filtraPorCodigo) {
      return [radicado, { title: 'Estudiante' }, { title: 'Documento' }, { title: 'Código' }, tipo, fecha, estado, { title: COLUMNA_DECISION_DECANO }];
    }
    return [radicado, { title: 'Estudiante' }, { title: 'Documento' }, tipo, fecha, estado];
  }

  private fila(solicitud: SolicitudAcademicaResumenDTORespuesta): Record<string, string> {
    return {
      uuid: solicitud.uuidSolicitudAcademica,
      'Radicado': solicitud.radicado,
      'Estudiante': solicitud.nombreEstudiante,
      'Documento': solicitud.numeroDocumento ?? '',
      'Código': solicitud.codigoEstudiantil ?? '',
      'Tipo de proceso': solicitud.tipoSolicitud,
      [COLUMNA_FECHA]: formatearFechaHora(solicitud.fechaCreacion),
      [this.columnaEstado]: solicitud.etiqueta,
      [COLUMNA_DECISION_DECANO]: textoDecisionDecano(solicitud.decisionDecano)
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
