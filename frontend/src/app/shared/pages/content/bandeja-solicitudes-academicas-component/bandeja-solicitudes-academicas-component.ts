import { Component, Input, OnChanges, OnInit, SimpleChanges, TemplateRef, ViewChild } from '@angular/core';
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
  ETAPAS_ACADEMICAS,
  ETAPAS_FINALES,
  PARAMETRO_ASIGNATURA,
  ROLES_ETIQUETA,
  RUTAS_SOLICITUDES_POR_ROL,
  RolEtiqueta,
  TIPOS_SOLICITUD_ACADEMICA
} from '../../../../core/constantes/procesos-academicos';
import { CategoriaEstado, ETIQUETAS_ESTUDIANTE, categoriaEstado } from '../../../../core/constantes/etiquetas-academicas';
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

export type ModoBandeja = 'solicitudes' | 'respuestas';

const TITULO_RESPUESTAS = 'Solicitudes respondidas';
const ESTADOS_RESPUESTA: string[] = [ETIQUETAS_ESTUDIANTE.APROBADA, ETIQUETAS_ESTUDIANTE.RECHAZADA];

type FilaBandeja = SolicitudAcademicaResumenDTORespuesta & { asignaturaSolicitudUuid?: string };

function textoResultado(aprobada: boolean): string {
  return aprobada ? ETIQUETAS_ESTUDIANTE.APROBADA : ETIQUETAS_ESTUDIANTE.RECHAZADA;
}

@Component({
  selector: 'app-bandeja-solicitudes-academicas-component',
  imports: [TableGenericComponent, Paginator, BarraBusquedaComponent, SimpleButtonComponent],
  templateUrl: './bandeja-solicitudes-academicas-component.html',
  styleUrl: './bandeja-solicitudes-academicas-component.css'
})
export class BandejaSolicitudesAcademicasComponent implements OnInit, OnChanges {
  @Input({ required: true }) rol!: RolEtiqueta;
  @Input() modo: ModoBandeja = 'solicitudes';

  @ViewChild('celdaRadicado', { static: true }) celdaRadicado!: TemplateRef<unknown>;
  @ViewChild('celdaEstado', { static: true }) celdaEstado!: TemplateRef<unknown>;
  @ViewChild('ordenFecha', { static: true }) ordenFecha!: TemplateRef<unknown>;
  @ViewChild('ordenEstado', { static: true }) ordenEstado!: TemplateRef<unknown>;

  readonly tamanioPagina = 10;
  readonly todos = TODOS_LOS_TIPOS;
  readonly opcionesTipo = Object.values(TIPOS_SOLICITUD_ACADEMICA);

  headers: TableHeader[] = [];
  recibidas: SolicitudAcademicaResumenDTORespuesta[] = [];
  solicitudes: FilaBandeja[] = [];
  filtradas: FilaBandeja[] = [];
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
    return this.esRespuestas ? TITULO_RESPUESTAS : TITULOS_BANDEJA[this.rol];
  }

  get esRespuestas(): boolean {
    return this.rol === ROLES_ETIQUETA.FUNCIONARIO && this.modo === 'respuestas';
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

  ngOnChanges(changes: SimpleChanges): void {
    const cambio = changes['modo'];
    if (!cambio || cambio.firstChange || cambio.previousValue === cambio.currentValue) return;
    this.estadoFiltro = TODOS_LOS_TIPOS;
    this.aplicarModo();
  }

  cargarSolicitudes(): void {
    this.cargando = true;
    this.errorCarga = false;
    this.bandejaDelRol().subscribe({
      next: solicitudes => {
        this.recibidas = solicitudes ?? [];
        this.cargando = false;
        this.aplicarModo();
      },
      error: err => {
        this.recibidas = [];
        this.solicitudes = [];
        this.opcionesEstado = [];
        this.cargando = false;
        this.errorCarga = true;
        this.aplicarFiltros();
        this.errorHandlerService.handleError(err, 'Error', 'No se pudieron cargar las solicitudes');
      }
    });
  }

  private aplicarModo(): void {
    this.solicitudes = this.delModo(this.recibidas);
    this.opcionesEstado = this.esRespuestas ? [...ESTADOS_RESPUESTA] : etiquetasDisponibles(this.solicitudes);
    if (!this.opcionesEstado.includes(this.estadoFiltro)) this.estadoFiltro = TODOS_LOS_TIPOS;
    this.aplicarFiltros();
  }

  private delModo(solicitudes: SolicitudAcademicaResumenDTORespuesta[]): FilaBandeja[] {
    if (this.rol !== ROLES_ETIQUETA.FUNCIONARIO) return solicitudes;
    const esFinal = (solicitud: SolicitudAcademicaResumenDTORespuesta) => ETAPAS_FINALES.includes(solicitud.etapaCodigo);
    if (!this.esRespuestas) return solicitudes.filter(solicitud => !esFinal(solicitud));
    return solicitudes.filter(esFinal).flatMap(solicitud => this.filasDeRespuesta(solicitud));
  }

  private filasDeRespuesta(solicitud: SolicitudAcademicaResumenDTORespuesta): FilaBandeja[] {
    const porAsignatura = solicitud.tipoSolicitud === TIPOS_SOLICITUD_ACADEMICA.CANCELACION_ASIGNATURA;
    const resultados = porAsignatura ? solicitud.resultadosAsignaturas ?? [] : [];
    if (resultados.length === 0) {
      return [{ ...solicitud, etiqueta: textoResultado(solicitud.etapaCodigo === ETAPAS_ACADEMICAS.APROBADA) }];
    }
    return resultados.map(resultado => ({
      ...solicitud,
      etiqueta: textoResultado(resultado.aprobada),
      asignaturaSolicitudUuid: resultado.asignaturaSolicitudUuid
    }));
  }

  get totalSolicitudes(): number {
    return new Set(this.filtradas.map(fila => fila.uuidSolicitudAcademica)).size;
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
    const destino = [RUTAS_SOLICITUDES_POR_ROL[this.rol], fila['uuid']];
    if (!fila['asignatura']) {
      this.router.navigate(destino);
      return;
    }
    this.router.navigate(destino, { queryParams: { [PARAMETRO_ASIGNATURA]: fila['asignatura'] } });
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
    if (this.filtraPorCodigo) return [radicado, { title: 'Estudiante' }, { title: 'Documento' }, { title: 'Código' }, tipo, fecha, estado];
    return [radicado, { title: 'Estudiante' }, { title: 'Documento' }, tipo, fecha, estado];
  }

  private fila(solicitud: FilaBandeja): Record<string, string> {
    const asignatura = solicitud.asignaturaSolicitudUuid ?? '';
    return {
      uuid: solicitud.uuidSolicitudAcademica,
      asignatura,
      clave: asignatura ? `${solicitud.uuidSolicitudAcademica}:${asignatura}` : solicitud.uuidSolicitudAcademica,
      'Radicado': solicitud.radicado,
      'Estudiante': solicitud.nombreEstudiante,
      'Documento': solicitud.numeroDocumento ?? '',
      'Código': solicitud.codigoEstudiantil ?? '',
      'Tipo de proceso': solicitud.tipoSolicitud,
      [COLUMNA_FECHA]: formatearFechaHora(solicitud.fechaCreacion),
      [this.columnaEstado]: solicitud.etiqueta
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
