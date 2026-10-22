import { Component, Input } from '@angular/core';
import { ProgramacionResolucionComponent } from '../programacion-resolucion-component/programacion-resolucion-component';
import { TextoComunicacionComponent } from '../texto-comunicacion-component/texto-comunicacion-component';
import {
  ComunicacionEnEdicion,
  MAXIMO_MENSAJE_INICIAL,
  MAXIMO_TEXTO_COMUNICACION,
  NOTA_RETENCION_ESTUDIANTE,
  fechaCarta,
  valoresComunicacionEstudiante
} from '../../../core/utils/notificacion-resolucion';

@Component({
  selector: 'app-comunicacion-respuesta-component',
  imports: [ProgramacionResolucionComponent, TextoComunicacionComponent],
  templateUrl: './comunicacion-respuesta-component.html',
  styleUrls: ['../../pages/content/estilos-academicos.css', './comunicacion-respuesta-component.css']
})
export class ComunicacionRespuestaComponent {
  @Input({ required: true }) comunicacion!: ComunicacionEnEdicion;
  @Input() sufijo = '';
  @Input() maximoMensaje = MAXIMO_MENSAJE_INICIAL;
  @Input() maximoTexto = MAXIMO_TEXTO_COMUNICACION;
  @Input() errores: Record<string, string> = {};
  @Input() nombreEstudiante = '';
  @Input() sugeridoComunicacion = '';
  @Input() sugeridoDarca = '';

  readonly notaRetencion = NOTA_RETENCION_ESTUDIANTE;
  readonly ayudaEstudiante =
    'Los datos entre llaves se llenan al enviar: {numero}, {anio}, {nombre} y {decision} (en negrilla). Si cambias el número, el año o la decisión arriba, el texto los toma solo.';
  readonly ayudaDarca = '{fecha} se reemplaza por la fecha en que sale el correo.';

  get valoresEstudiante(): Record<string, string> {
    return valoresComunicacionEstudiante(this.comunicacion.programacion, this.nombreEstudiante);
  }

  get valoresDarca(): Record<string, string> {
    return { fecha: fechaCarta(this.comunicacion.programacion.fecha) };
  }
}
