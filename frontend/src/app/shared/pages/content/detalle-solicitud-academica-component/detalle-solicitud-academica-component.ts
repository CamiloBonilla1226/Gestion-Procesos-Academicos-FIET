import { Component, DestroyRef, Input, OnInit, Type, inject } from '@angular/core';
import { NgClass, NgComponentOutlet } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize, forkJoin } from 'rxjs';
import { SimpleButtonComponent } from '../../../buttons/simple-button-component/simple-button-component';
import { AnexosAcademicosComponent } from '../../../others/anexos-academicos-component/anexos-academicos-component';
import { SolicitudAcademicaService } from '../../../../core/services/solicitud-academica-service';
import { ErrorHandlerService } from '../../../../core/services/error-handler-service';
import { SolicitudAcademicaDetalleDTORespuesta } from '../../../../core/models/SolicitudAcademica/DTOResponse/SolicitudAcademicaDetalleDTORespuesta';
import { HistorialSolicitudAcademicaDTORespuesta } from '../../../../core/models/SolicitudAcademica/DTOResponse/HistorialSolicitudAcademicaDTORespuesta';
import {
  ACCIONES_ACADEMICAS,
  CODIGOS_ERROR,
  ETAPAS_FINALES,
  PROCESOS_ACADEMICOS,
  ProcesoAcademico,
  ROLES_ETIQUETA,
  RUTA_RESPUESTAS_FUNCIONARIO,
  RUTAS_SOLICITUDES_POR_ROL,
  RolEtiqueta,
  claseInsigniaEtapa,
  procesoDeTipoSolicitud,
  textoAccion,
  textoEtapa
} from '../../../../core/constantes/procesos-academicos';
import { CategoriaEstado, ETIQUETAS_DECISION_DECANO, categoriaEstado } from '../../../../core/constantes/etiquetas-academicas';

export interface DecisionDecano {
  aprobada: boolean;
  etiqueta: string;
  observacion: string | null;
  fecha: string;
}

const ACCIONES_DECISION_DECANO: string[] = [ACCIONES_ACADEMICAS.APROBAR_DECANO, ACCIONES_ACADEMICAS.RECHAZAR_DECANO];

const ACCIONES_MENSAJE_ESTUDIANTE: string[] = [
  ACCIONES_ACADEMICAS.ENVIAR_RESPUESTA,
  ACCIONES_ACADEMICAS.RECHAZAR_FUNCIONARIO,
  ACCIONES_ACADEMICAS.RECHAZAR_COMPROBANTE,
  ACCIONES_ACADEMICAS.APROBAR_COMPROBANTE
];

export interface DatoGeneral {
  etiqueta: string;
  valor: string;
}

const TITULOS_ANEXOS: Partial<Record<ProcesoAcademico, string>> = {
  [PROCESOS_ACADEMICOS.CANCELACION_MATRICULA]: 'Documentación requerida',
  [PROCESOS_ACADEMICOS.CANCELACION_ASIGNATURA]: 'Soporte adjunto',
  [PROCESOS_ACADEMICOS.EXAMEN_SUPLETORIO]: 'Anexos adjuntos'
};
import { leerCodigoError } from '../../../../core/utils/errores-http';
import { guardarArchivo } from '../../../../core/utils/descargas';
import { formatearFechaHora } from '../../../../core/utils/formato';
import { COMPONENTES_PROCESO, EntradasComponenteProceso } from './componentes-proceso';

@Component({
  selector: 'app-detalle-solicitud-academica-component',
  imports: [NgClass, NgComponentOutlet, SimpleButtonComponent, AnexosAcademicosComponent],
  templateUrl: './detalle-solicitud-academica-component.html',
  styleUrls: ['../estilos-academicos.css', './detalle-solicitud-academica-component.css']
})
export class DetalleSolicitudAcademicaComponent implements OnInit {
  @Input({ required: true }) rol!: RolEtiqueta;

  uuidSolicitud = '';
  solicitud: SolicitudAcademicaDetalleDTORespuesta | null = null;
  historial: HistorialSolicitudAcademicaDTORespuesta[] = [];
  cargando = false;
  noEncontrada = false;
  errorCarga = false;
  descargandoResolucion = false;
  proceso: ProcesoAcademico | undefined;
  componenteProceso: Type<unknown> | null = null;
  entradasProceso: Record<string, unknown> | null = null;

  readonly formatearFechaHora = formatearFechaHora;
  readonly textoAccion = textoAccion;
  readonly textoEtapa = textoEtapa;
  readonly claseInsigniaEtapa = claseInsigniaEtapa;

  private destroyRef = inject(DestroyRef);

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private solicitudAcademicaService: SolicitudAcademicaService,
    private errorHandlerService: ErrorHandlerService
  ) {}

  ngOnInit(): void {
    this.route.paramMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(parametros => {
      this.uuidSolicitud = parametros.get('uuid') ?? '';
      this.cargar();
    });
  }

  get rutaBandeja(): string {
    const respondida = !!this.solicitud && ETAPAS_FINALES.includes(this.solicitud.etapaCodigo);
    if (this.rol === ROLES_ETIQUETA.FUNCIONARIO && respondida) return RUTA_RESPUESTAS_FUNCIONARIO;
    return RUTAS_SOLICITUDES_POR_ROL[this.rol];
  }

  get esEstudiante(): boolean {
    return this.rol === ROLES_ETIQUETA.ESTUDIANTE;
  }

  get categoriaEstado(): CategoriaEstado {
    return categoriaEstado(this.solicitud?.etiqueta);
  }

  get tituloAnexos(): string {
    const proceso = this.solicitud ? procesoDeTipoSolicitud(this.solicitud.tipoSolicitud) : undefined;
    return (proceso && TITULOS_ANEXOS[proceso]) || 'Anexos';
  }

  get datosGenerales(): DatoGeneral[] {
    const estudiante = this.solicitud?.estudiante;
    if (!this.solicitud || !estudiante) return [];
    return [
      { etiqueta: 'Nombre', valor: `${estudiante.nombres ?? ''} ${estudiante.apellidos ?? ''}`.trim() },
      { etiqueta: 'Número de CC', valor: this.esEstudiante ? '' : this.solicitud.numeroDocumento ?? '' },
      { etiqueta: 'Código estudiantil', valor: this.solicitud.codigoEstudiantil ?? estudiante.codigoEstudiantil ?? '' },
      { etiqueta: 'Programa académico', valor: estudiante.programaAcademico ?? '' },
      { etiqueta: 'Semestre', valor: estudiante.semestre ?? '' },
      { etiqueta: 'Correo electrónico', valor: estudiante.correoElectronico ?? '' },
      { etiqueta: 'Fecha de la solicitud', valor: this.solicitud.fechaCreacion ? formatearFechaHora(this.solicitud.fechaCreacion) : '' }
    ].filter(dato => `${dato.valor}`.trim() !== '');
  }

  get observacionRemision(): string | null {
    if (this.esEstudiante) return null;
    return this.ultimaObservacion(ACCIONES_ACADEMICAS.REMITIR_DECANO);
  }

  get mensajeEstudiante(): string | null {
    return this.ultimaObservacion(...ACCIONES_MENSAJE_ESTUDIANTE);
  }

  get decisionDecano(): DecisionDecano | null {
    if (this.rol !== ROLES_ETIQUETA.FUNCIONARIO && this.rol !== ROLES_ETIQUETA.DECANO) return null;
    const fila = [...this.historial].reverse().find(entrada => ACCIONES_DECISION_DECANO.includes(entrada.accion));
    if (!fila) return null;
    const aprobada = fila.accion === ACCIONES_ACADEMICAS.APROBAR_DECANO;
    const observacion = (fila.observaciones ?? '').trim();
    return {
      aprobada,
      etiqueta: aprobada ? ETIQUETAS_DECISION_DECANO.APROBADA : ETIQUETAS_DECISION_DECANO.RECHAZADA,
      observacion: observacion === '' ? null : observacion,
      fecha: fila.fecha
    };
  }

  cargar(): void {
    this.cargando = true;
    this.noEncontrada = false;
    this.errorCarga = false;
    forkJoin({
      solicitud: this.solicitudAcademicaService.getSolicitud(this.uuidSolicitud),
      historial: this.solicitudAcademicaService.getHistorial(this.uuidSolicitud)
    })
      .pipe(finalize(() => (this.cargando = false)))
      .subscribe({
        next: ({ solicitud, historial }) => {
          this.solicitud = solicitud;
          this.historial = this.ordenarHistorial(historial ?? []);
          this.prepararProceso(solicitud);
        },
        error: err => {
          this.solicitud = null;
          this.historial = [];
          this.limpiarProceso();
          if (leerCodigoError(err) === CODIGOS_ERROR.ENTIDAD_NO_EXISTE) {
            this.noEncontrada = true;
            return;
          }
          this.errorCarga = true;
          this.errorHandlerService.handleError(err, 'Error', 'No se pudo cargar la solicitud');
        }
      });
  }

  volverABandeja(evento: Event): void {
    evento.preventDefault();
    this.router.navigate([this.rutaBandeja]);
  }

  nombreUsuario(entrada: HistorialSolicitudAcademicaDTORespuesta): string {
    return `${entrada.nombresUsuario ?? ''} ${entrada.apellidosUsuario ?? ''}`.trim();
  }

  descargarResolucion(): void {
    if (!this.solicitud || this.descargandoResolucion) return;
    const radicado = this.solicitud.radicado;
    this.descargandoResolucion = true;
    this.solicitudAcademicaService
      .descargarResolucion(this.uuidSolicitud)
      .pipe(finalize(() => (this.descargandoResolucion = false)))
      .subscribe({
        next: blob => guardarArchivo(blob, `Resolucion-${radicado}.pdf`),
        error: err => this.errorHandlerService.handleError(err, 'Error', 'No se pudo descargar la Resolución')
      });
  }

  private ultimaObservacion(...acciones: string[]): string | null {
    const fila = [...this.historial].reverse().find(entrada => acciones.includes(entrada.accion));
    const texto = (fila?.observaciones ?? '').trim();
    return texto === '' ? null : texto;
  }

  private ordenarHistorial(historial: HistorialSolicitudAcademicaDTORespuesta[]): HistorialSolicitudAcademicaDTORespuesta[] {
    return [...historial].sort((a, b) => (a.fecha ?? '').localeCompare(b.fecha ?? ''));
  }

  private prepararProceso(solicitud: SolicitudAcademicaDetalleDTORespuesta): void {
    this.limpiarProceso();
    this.proceso = procesoDeTipoSolicitud(solicitud.tipoSolicitud);
    const cargador = this.proceso ? COMPONENTES_PROCESO[this.proceso] : undefined;
    if (!cargador) return;
    const entradas: EntradasComponenteProceso = {
      uuidSolicitud: this.uuidSolicitud,
      solicitud,
      rol: this.rol,
      recargar: () => this.cargar()
    };
    cargador().then(componente => {
      if (this.solicitud !== solicitud) return;
      this.entradasProceso = { ...entradas };
      this.componenteProceso = componente;
    });
  }

  private limpiarProceso(): void {
    this.proceso = undefined;
    this.componenteProceso = null;
    this.entradasProceso = null;
  }
}
