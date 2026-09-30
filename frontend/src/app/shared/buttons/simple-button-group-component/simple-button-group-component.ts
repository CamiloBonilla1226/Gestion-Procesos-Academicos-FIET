import { CommonModule } from '@angular/common';
import { Component, Input, Output, EventEmitter } from '@angular/core';

/**
 * Componente de botón grupal simple.
 * Permite mostrar un botón con texto opcional, ícono, estilos personalizados
 * y control de estado deshabilitado.
 * Emite un evento cuando es presionado.
 *
 * @author Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-simple-button-group-component',
  imports: [CommonModule],
  templateUrl: './simple-button-group-component.html',
  styleUrl: './simple-button-group-component.css'
})
export class SimpleButtonGroupComponent {

  /**
   * Texto que se muestra en el botón.
   */
  @Input() label: string = '';

  /**
   * URL de la imagen o ícono del botón.
   * Es opcional.
   */
  @Input() imgUrl?: string;

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
