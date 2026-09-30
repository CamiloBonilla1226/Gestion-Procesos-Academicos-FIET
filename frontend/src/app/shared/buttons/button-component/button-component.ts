import { CommonModule } from '@angular/common';
import { Component, Input, Output, EventEmitter } from '@angular/core';

/**
 * Componente genérico de botón reutilizable.
 * Permite personalizar dimensiones, color e icono,
 * y emite un evento cuando es presionado.
 *
 * @author Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-button-component',
  imports: [CommonModule],
  templateUrl: './button-component.html',
  styleUrl: './button-component.css'
})
export class ButtonComponent {

  /**
   * URL de la imagen o ícono que se mostrará en el botón.
   */
  @Input() imgUrl: string = '';

  /**
   * Color del botón.
   */
  @Input() color: string = '';

  /**
   * Ancho del botón.
   */
  @Input() width: string = '';

  /**
   * Alto del botón.
   */
  @Input() height: string = '';

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
