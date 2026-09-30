import { CommonModule } from '@angular/common';
import { Component, Input, Output, EventEmitter } from '@angular/core';

/**
 * Componente de grupo de botón.
 * Permite mostrar un botón con etiqueta e ícono,
 * y notifica al componente padre cuando es presionado.
 *
 * @author Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-button-group-component',
  imports: [CommonModule],
  templateUrl: './button-group-component.html',
  styleUrl: './button-group-component.css'
})
export class ButtonGroupComponent {

  /**
   * Texto que se muestra como etiqueta del botón.
   */
  @Input() label: string = '';

  /**
   * URL del ícono o imagen asociada al botón.
   */
  @Input() imgUrl: string = '';

  /**
   * Evento emitido cuando el botón es presionado.
   */
  @Output() clicked = new EventEmitter<void>();

  /**
   * Maneja el evento click del botón y emite el evento correspondiente.
   */
  onClick(): void {
    this.clicked.emit();
  }
}
