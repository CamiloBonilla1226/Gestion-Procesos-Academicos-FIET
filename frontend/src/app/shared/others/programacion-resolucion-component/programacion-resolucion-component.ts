import { Component, Input } from '@angular/core';
import { InputDateComponent } from '../../inputs/input-date-component/input-date-component';
import { InputTextComponent } from '../../inputs/input-text-component/input-text-component';
import { InputTextTareaComponent } from '../../inputs/input-text-tarea-component/input-text-tarea-component';
import { ProgramacionEnEdicion, ahoraEnColombia, textoFechaHora } from '../../../core/utils/notificacion-resolucion';

@Component({
  selector: 'app-programacion-resolucion-component',
  imports: [InputDateComponent, InputTextComponent, InputTextTareaComponent],
  templateUrl: './programacion-resolucion-component.html',
  styleUrls: ['../../pages/content/estilos-academicos.css', './programacion-resolucion-component.css']
})
export class ProgramacionResolucionComponent {
  @Input({ required: true }) programacion!: ProgramacionEnEdicion;
  @Input() prefijo = '';
  @Input() errores: Record<string, string> = {};

  readonly minimo = ahoraEnColombia();

  get textoFecha(): string {
    return textoFechaHora(this.programacion?.fecha);
  }

  get vistaPrevia(): string {
    const numero = this.programacion.numero.trim() || '___';
    const anio = this.programacion.anio.trim() || '____';
    const decision = this.programacion.decision.trim() || '___';
    return `Resolución No. ${this.prefijo}${numero} de ${anio}, la misma por la cual se ${decision} cancelación de asignatura.`;
  }
}
