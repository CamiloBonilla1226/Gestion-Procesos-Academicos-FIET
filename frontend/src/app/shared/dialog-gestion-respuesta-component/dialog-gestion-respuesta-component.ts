import { Component, EventEmitter, Input, Output, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DialogModule } from 'primeng/dialog';
import { RespuestasService } from '../../core/services/respuesta-service';
import { DescargarRespuestaComponent } from '../descargar-respuesta-component/descargar-respuesta-component';
import { CargarRespuestaComponent } from '../cargar-respuesta-component/cargar-respuesta-component';
import { SimpleButtonComponent } from '../buttons/simple-button-component/simple-button-component';
import { ToastService } from '../../core/services/toast-service';
import { ErrorHandlerService } from '../../core/services/error-handler-service';
import { SolicitudesService } from '../../core/services/solicitudes-service';
import { RespuestaDTORespuesta } from '../../core/models/Respuesta/DTOResponse/RespuestaDTORespuesta';
import { SolicitudDTORespuesta } from '../../core/models/Solicitudes/DTOResponse/SolicitudDTORespuesta';
import { SolicitudActualizarDTOPeticion } from '../../core/models/Solicitudes/DTORequest/SolicitudActualizarDTOPeticion';
import { AGREGADO_EN_EL_ORDEN_DEL_DIA, RESPONDIDA } from '../../core/constantes/constantes';
import { switchMap, of, Observable, map } from 'rxjs';

/**
 * Componente de diálogo para la gestión de respuestas.
 * Permite cargar, eliminar y visualizar respuestas de solicitudes.
 * Actualiza automáticamente el estado de la solicitud asociada según las acciones realizadas.
 * 
 * Soporta notificación de éxito y manejo de errores mediante servicios dedicados.
 * 
 * @author Julian David Camacho Erazo
 * {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-dialog-gestion-respuesta-component',
  imports: [
    CommonModule,
    DialogModule,
    DescargarRespuestaComponent,
    CargarRespuestaComponent,
    SimpleButtonComponent
  ],
  templateUrl: './dialog-gestion-respuesta-component.html',
  styleUrl: './dialog-gestion-respuesta-component.css'
})
export class DialogGestionRespuestaComponent implements OnChanges {

  /** Indica si el diálogo es visible */
  @Input() visible = false;

  /** UUID de la respuesta a gestionar */
  @Input() uuidRespuesta!: string;

  /** Evento que emite cambios de visibilidad */
  @Output() visibleChange = new EventEmitter<boolean>();

  /** Evento que se emite cuando se modifica una respuesta */
  @Output() respuestaModificada = new EventEmitter<void>();

  /** Respuesta cargada actualmente */
  respuesta: any = null;

  /** Indica si el componente está cargando datos */
  cargando = false;

  /**
   * Constructor del componente
   * @param respuestasService Servicio para operaciones sobre respuestas
   * @param solicitudesService Servicio para operaciones sobre solicitudes
   * @param toastService Servicio para mostrar mensajes emergentes
   * @param errorHandlerService Servicio para manejo de errores
   */
  constructor(
    private respuestasService: RespuestasService,
    private solicitudesService: SolicitudesService,
    private toastService: ToastService,
    private errorHandlerService: ErrorHandlerService  
  ) {}

  /** Detecta cambios en las propiedades de entrada y carga la respuesta si el diálogo está visible */
  ngOnChanges(): void {
    if (this.visible && this.uuidRespuesta) {
      this.cargarRespuesta();
    }
  }

  /** Carga la respuesta asociada al UUID proporcionado */
  private cargarRespuesta(): void {
    this.cargando = true;
    this.respuestasService.getRespuesta(this.uuidRespuesta).subscribe({
      next: resp => {
        this.respuesta = resp;
        this.cargando = false;
      },
      error: () => {
        this.respuesta = null;
        this.cargando = false;
      }
    });
  }

  /** Cierra el diálogo y emite el cambio de visibilidad */
  cerrar(): void {
    this.visible = false;
    this.visibleChange.emit(false);
  }

  /**
   * Maneja la carga de un archivo de respuesta.
   * Actualiza el estado de la solicitud a RESPONDIDA y notifica éxito.
   * @param file Archivo seleccionado
   */
  onArchivoSeleccionado(file: File): void {
    this.respuestasService.responderSolicitud(this.uuidRespuesta, file)
      .pipe(
        switchMap(() => this.actualizarEstadoSolicitud(this.uuidRespuesta, RESPONDIDA))
      )
      .subscribe({
        next: () => {
          this.cargarRespuesta();
          this.toastService.showSuccess('Éxito', 'Se ha respondido a la solicitud');
          this.respuestaModificada.emit();
        },
        error: err => {
          this.errorHandlerService.handleError(err, 'Error Cargando Respuesta', 'No se pudo cargar la respuesta');
        }
      });
  }

  /**
   * Maneja la eliminación de un archivo de respuesta.
   * Actualiza el estado de la solicitud a AGREGADO_EN_EL_ORDEN_DEL_DIA y notifica éxito.
   */
  onArchivoEliminado(): void {
    this.respuestasService.eliminarArchivoRespuesta(this.uuidRespuesta)
      .pipe(
        switchMap(() => this.actualizarEstadoSolicitud(this.uuidRespuesta, AGREGADO_EN_EL_ORDEN_DEL_DIA))
      )
      .subscribe({
        next: () => {
          this.cargarRespuesta();
          this.toastService.showSuccess('Éxito', 'Se ha eliminado la respuesta');
          this.respuestaModificada.emit();
        },
        error: err => {
          this.errorHandlerService.handleError(err, 'Error Eliminando Respuesta', 'No se pudo eliminar la respuesta');
        }
      });
  }

  /**
   * Actualiza el estado de la solicitud asociada a una respuesta.
   * @param uuidRespuesta UUID de la respuesta
   * @param nuevoEstado Nuevo estado de la solicitud
   * @returns Observable<void> que indica la finalización de la operación
   */
  private actualizarEstadoSolicitud(uuidRespuesta: string, nuevoEstado: string): Observable<void> {
    return this.respuestasService.getRespuesta(uuidRespuesta).pipe(
      switchMap((resp: RespuestaDTORespuesta) => {
        const uuidSolicitud = resp.solicitud.uuidSolicitud;
        if (!uuidSolicitud) return of(void 0);

        return this.solicitudesService.getSolicitud(uuidSolicitud).pipe(
          switchMap((solicitud: SolicitudDTORespuesta) => {
            const solicitudActualizar: SolicitudActualizarDTOPeticion = {
              consecutivo: solicitud.consecutivo,
              nombre: solicitud.nombre,
              descripcion: solicitud.descripcion,
              estado: nuevoEstado,
              uuidFuncionario: solicitud.objFuncionario.uuidUsuario,
              uuidOrdenDelDia: solicitud.ordenDelDia.uuidOrdenDelDia
            };
            return this.solicitudesService.actualizarSolicitud(solicitud.uuidSolicitud, solicitudActualizar);
          })
        );
      }),
      map(() => void 0) 
    );
  }
}
