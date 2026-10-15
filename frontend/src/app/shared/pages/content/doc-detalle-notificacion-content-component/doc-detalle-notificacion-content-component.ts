import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { SimpleButtonComponent } from '../../../buttons/simple-button-component/simple-button-component';
import { NotificacionDocenteService } from '../../../../core/services/notificacion-docente-service';
import { ErrorHandlerService } from '../../../../core/services/error-handler-service';
import { NotificacionDocenteDetalleDTORespuesta } from '../../../../core/models/NotificacionDocente/DTOResponse/NotificacionDocenteDetalleDTORespuesta';
import { AnexoNotificacionDTORespuesta } from '../../../../core/models/NotificacionDocente/DTOResponse/AnexoNotificacionDTORespuesta';
import { CAUSAS_SUPLETORIO, CODIGOS_ERROR } from '../../../../core/constantes/procesos-academicos';
import { categoriaEstado } from '../../../../core/constantes/etiquetas-academicas';
import { formatearFecha, formatearFechaHora } from '../../../../core/utils/formato';
import { guardarArchivo } from '../../../../core/utils/descargas';
import { leerCodigoError } from '../../../../core/utils/errores-http';
import { RUTA_NOTIFICACIONES_DOCENTE } from '../doc-notificaciones-content-component/doc-notificaciones-content-component';

const TEXTOS_CAUSA: Record<string, string> = {
  [CAUSAS_SUPLETORIO.CRUCE]: 'Cruce con el examen de otra asignatura',
  [CAUSAS_SUPLETORIO.OTRA]: 'Otra causa justificada'
};

@Component({
  selector: 'app-doc-detalle-notificacion-content-component',
  imports: [SimpleButtonComponent],
  templateUrl: './doc-detalle-notificacion-content-component.html',
  styleUrls: ['../estilos-academicos.css']
})
export class DocDetalleNotificacionContentComponent implements OnInit {
  readonly formatearFecha = formatearFecha;
  readonly formatearFechaHora = formatearFechaHora;
  readonly rutaLista = RUTA_NOTIFICACIONES_DOCENTE;

  uuidNotificacion = '';
  detalle: NotificacionDocenteDetalleDTORespuesta | null = null;
  cargando = false;
  errorCarga = false;
  noEncontrada = false;
  descargando: string | null = null;

  private destroyRef = inject(DestroyRef);

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private notificacionDocenteService: NotificacionDocenteService,
    private errorHandlerService: ErrorHandlerService
  ) {}

  ngOnInit(): void {
    this.route.paramMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(parametros => {
      this.uuidNotificacion = parametros.get('uuid') ?? '';
      this.cargar();
    });
  }

  get categoria(): string {
    return categoriaEstado(this.detalle?.notificacion.resultado);
  }

  get nombreEstudiante(): string {
    return `${this.detalle?.nombresEstudiante ?? ''} ${this.detalle?.apellidosEstudiante ?? ''}`.trim() || '—';
  }

  textoCausa(causa: string | null | undefined): string {
    return TEXTOS_CAUSA[(causa ?? '').toLowerCase()] ?? causa ?? '—';
  }

  cargar(): void {
    if (!this.uuidNotificacion) return;
    this.cargando = true;
    this.errorCarga = false;
    this.noEncontrada = false;
    this.notificacionDocenteService
      .marcarLeida(this.uuidNotificacion)
      .pipe(finalize(() => (this.cargando = false)))
      .subscribe({
        next: detalle => (this.detalle = detalle),
        error: err => {
          this.detalle = null;
          if (leerCodigoError(err) === CODIGOS_ERROR.ENTIDAD_NO_EXISTE) {
            this.noEncontrada = true;
            return;
          }
          this.errorCarga = true;
          this.errorHandlerService.handleError(err, 'Error', 'No se pudo cargar la notificación');
        }
      });
  }

  descargar(anexo: AnexoNotificacionDTORespuesta): void {
    if (this.descargando) return;
    this.descargando = anexo.uuidAnexoAcademico;
    this.notificacionDocenteService
      .descargarAnexo(this.uuidNotificacion, anexo.uuidAnexoAcademico)
      .pipe(finalize(() => (this.descargando = null)))
      .subscribe({
        next: blob => guardarArchivo(blob, anexo.nombreArchivo),
        error: err => this.errorHandlerService.handleError(err, 'Error', 'No se pudo descargar el anexo')
      });
  }

  volver(evento: Event): void {
    evento.preventDefault();
    this.router.navigateByUrl(this.rutaLista);
  }
}
