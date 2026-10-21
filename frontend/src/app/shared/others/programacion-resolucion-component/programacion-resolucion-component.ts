import { Component, Input } from '@angular/core';
import { InputDateComponent } from '../../inputs/input-date-component/input-date-component';
import { InputTextComponent } from '../../inputs/input-text-component/input-text-component';
import { ProgramacionEnEdicion, ahoraEnColombia, textoFechaHora } from '../../../core/utils/notificacion-resolucion';

@Component({
  selector: 'app-programacion-resolucion-component',
  imports: [InputDateComponent, InputTextComponent],
  templateUrl: './programacion-resolucion-component.html',
  styleUrl: '../../pages/content/estilos-academicos.css'
})
export class ProgramacionResolucionComponent {
  @Input({ required: true }) programacion!: ProgramacionEnEdicion;
  @Input() sufijo = '';
  @Input() errores: Record<string, string> = {};

  readonly minimo = ahoraEnColombia();

  get textoFecha(): string {
    return textoFechaHora(this.programacion?.fecha);
  }

  get vistaPrevia(): string {
    const numero = this.programacion.numero.trim() || '___';
    const anio = this.programacion.anio.trim() || '____';
    const decision = this.programacion.decision.trim() || '___';
    return `Resolución No. ${numero}${this.sufijo} de ${anio}, la misma por la cual se ${decision} cancelación de asignatura.`;
  }
}
