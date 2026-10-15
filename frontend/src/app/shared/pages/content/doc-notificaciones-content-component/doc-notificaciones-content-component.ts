import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { SimpleButtonComponent } from '../../../buttons/simple-button-component/simple-button-component';
import { NotificacionDocenteService } from '../../../../core/services/notificacion-docente-service';
import { ErrorHandlerService } from '../../../../core/services/error-handler-service';
import { NotificacionDocenteResumenDTORespuesta } from '../../../../core/models/NotificacionDocente/DTOResponse/NotificacionDocenteResumenDTORespuesta';
import { categoriaEstado } from '../../../../core/constantes/etiquetas-academicas';
import { formatearFechaHora } from '../../../../core/utils/formato';

export const RUTA_NOTIFICACIONES_DOCENTE = '/docente/notificaciones';

@Component({
  selector: 'app-doc-notificaciones-content-component',
  imports: [SimpleButtonComponent],
  templateUrl: './doc-notificaciones-content-component.html',
  styleUrls: ['../estilos-academicos.css', '../tabla-asignaturas.css', './doc-notificaciones-content-component.css']
})
export class DocNotificacionesContentComponent implements OnInit {
  readonly formatearFechaHora = formatearFechaHora;
  readonly categoriaEstado = categoriaEstado;

  notificaciones: NotificacionDocenteResumenDTORespuesta[] = [];
  cargando = false;
  errorCarga = false;

  constructor(
    private notificacionDocenteService: NotificacionDocenteService,
    private errorHandlerService: ErrorHandlerService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.cargar();
  }

  get sinLeer(): number {
    return this.notificaciones.filter(n => !n.leida).length;
  }

  cargar(): void {
    this.cargando = true;
    this.errorCarga = false;
    this.notificacionDocenteService
      .getMisNotificaciones()
      .pipe(finalize(() => (this.cargando = false)))
      .subscribe({
        next: lista => (this.notificaciones = lista ?? []),
        error: err => {
          this.notificaciones = [];
          this.errorCarga = true;
          this.errorHandlerService.handleError(err, 'Error', 'No se pudieron cargar las notificaciones');
        }
      });
  }

  rutaDe(notificacion: NotificacionDocenteResumenDTORespuesta): string {
    return `${RUTA_NOTIFICACIONES_DOCENTE}/${notificacion.uuidNotificacion}`;
  }

  abrir(evento: Event, notificacion: NotificacionDocenteResumenDTORespuesta): void {
    evento.preventDefault();
    this.router.navigateByUrl(this.rutaDe(notificacion));
  }
}
