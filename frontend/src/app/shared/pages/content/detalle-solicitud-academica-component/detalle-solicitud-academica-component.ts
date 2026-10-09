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
  CODIGOS_ERROR,
  ProcesoAcademico,
  RUTAS_SOLICITUDES_POR_ROL,
  RolEtiqueta,
  claseInsigniaEtapa,
  procesoDeTipoSolicitud,
  textoAccion,
  textoEtapa
} from '../../../../core/constantes/procesos-academicos';
import { leerCodigoError } from '../../../../core/utils/errores-http';
import { guardarArchivo } from '../../../../core/utils/descargas';
import { formatearFechaHora } from '../../../../core/utils/formato';
import { COMPONENTES_PROCESO, EntradasComponenteProceso } from './componentes-proceso';

@Component({
  selector: 'app-detalle-solicitud-academica-component',
  imports: [NgClass, NgComponentOutlet, SimpleButtonComponent, AnexosAcademicosComponent],
  templateUrl: './detalle-solicitud-academica-component.html'
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
    return RUTAS_SOLICITUDES_POR_ROL[this.rol];
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
