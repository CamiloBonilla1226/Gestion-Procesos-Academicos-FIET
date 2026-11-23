import { Component, EventEmitter, Input, Output } from '@angular/core';
import { DecisionDecano, ETIQUETA_ENVIAR_DECISION } from '../../../core/utils/decision-decano';

@Component({
  selector: 'app-decision-decano-component',
  templateUrl: './decision-decano-component.html',
  styleUrl: './decision-decano-component.css'
})
export class DecisionDecanoComponent {
  @Input() nombre = 'decision-decano';
  @Input() textoAprobar = 'Aprobar la solicitud';
  @Input() textoRechazar = 'Rechazar la solicitud';
  @Input() decision: DecisionDecano | null = null;
  @Input() deshabilitado = false;
  @Output() decisionChange = new EventEmitter<DecisionDecano>();

  readonly etiquetaEnviar = ETIQUETA_ENVIAR_DECISION;

  elegir(decision: DecisionDecano): void {
    if (this.deshabilitado) return;
    this.decisionChange.emit(decision);
  }
}
