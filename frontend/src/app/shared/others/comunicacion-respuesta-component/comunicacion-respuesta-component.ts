import { Component, Input } from '@angular/core';
import { InputTextTareaComponent } from '../../inputs/input-text-tarea-component/input-text-tarea-component';
import { ProgramacionResolucionComponent } from '../programacion-resolucion-component/programacion-resolucion-component';
import {
  ComunicacionEnEdicion,
  MAXIMO_MENSAJE_INICIAL,
  NOTA_RETENCION_ESTUDIANTE
} from '../../../core/utils/notificacion-resolucion';

@Component({
  selector: 'app-comunicacion-respuesta-component',
  imports: [InputTextTareaComponent, ProgramacionResolucionComponent],
  templateUrl: './comunicacion-respuesta-component.html',
  styleUrls: ['../../pages/content/estilos-academicos.css', './comunicacion-respuesta-component.css']
})
export class ComunicacionRespuestaComponent {
  @Input({ required: true }) comunicacion!: ComunicacionEnEdicion;
  @Input() sufijo = '';
  @Input() maximoMensaje = MAXIMO_MENSAJE_INICIAL;
  @Input() errores: Record<string, string> = {};

  readonly notaRetencion = NOTA_RETENCION_ESTUDIANTE;

  cambiarEnvio(evento: Event): void {
    this.comunicacion.enviarMensaje = (evento.target as HTMLInputElement).checked;
  }
}
