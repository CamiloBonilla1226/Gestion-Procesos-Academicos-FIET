import { CommonModule } from '@angular/common';
import { Component, Input, Output, EventEmitter } from '@angular/core';

/**
 * Componente de botón simple reutilizable.
 * Permite configurar etiqueta, color, dimensiones y estado deshabilitado.
 * Emite un evento cuando es presionado.
 *
 * @author Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-simple-button-component',
  imports: [CommonModule],
  templateUrl: './simple-button-component.html',
  styleUrl: './simple-button-component.css'
})
export class SimpleButtonComponent {

  /**
   * Texto que se muestra dentro del botón.
   */
  @Input() label: string = '';

  /**
   * Color de fondo del botón.
   */
  @Input() color: string = '#1E257B';

  /**
   * Ancho del botón.
   */
  @Input() width: string = '';

  /**
   * Alto del botón.
   */
  @Input() height: string = '';

  /**
   * Indica si el botón está deshabilitado.
   */
  @Input() disabled: boolean = false;

  /**
   * Evento emitido cuando el botón es presionado.
   */
  @Output() clicked = new EventEmitter<void>();

  /**
   * Maneja el evento click del botón.
   * No emite el evento si el botón está deshabilitado.
   */
  onClick(): void {
    if (this.disabled) return;
    this.clicked.emit();
  }
}
