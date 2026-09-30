import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

/**
 * Componente de entrada de fecha.
 * Permite seleccionar o ingresar una fecha, con soporte para validación de obligatoriedad y emisión de cambios.
 * 
 * @autor Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-input-date-component',
  imports: [CommonModule, FormsModule],
  templateUrl: './input-date-component.html',
  styleUrl: './input-date-component.css'
})
export class InputDateComponent {

  /**
   * Etiqueta que se muestra junto al campo de fecha.
   */
  @Input() label = '';

  /**
   * Valor actual del campo de fecha.
   */
  @Input() value = '';

  /**
   * Indica si el campo de fecha es obligatorio.
   */
  @Input() required = false;

  /**
   * Evento que se emite cuando cambia el valor del campo de fecha.
   */
  @Output() valueChange = new EventEmitter<string>();

  /**
   * Indica si el campo ha sido tocado por el usuario.
   */
  touched = false;

  /**
   * Maneja el cambio en el input de fecha y emite el valor actualizado.
   * 
   * @param event Evento de cambio del input.
   */
  onChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.value = input.value;
    this.valueChange.emit(this.value);
  }

  /**
   * Determina si el campo es inválido según la obligatoriedad y si ha sido tocado.
   * 
   * @returns `true` si el campo es obligatorio, está vacío y ha sido tocado; `false` en caso contrario.
   */
  isInvalid(): boolean {
    return this.required && !this.value && this.touched;
  }

  /**
   * Reinicia el valor del campo de fecha y marca el campo como no tocado.
   */
  reset(): void {
    this.value = '';
    this.valueChange.emit(this.value);
    this.touched = false;
  }

  /**
   * Asigna un valor al campo de fecha y emite el cambio.
   * 
   * @param value Nuevo valor de la fecha.
   */
  setValue(value: string): void {
    this.value = value;
    this.valueChange.emit(this.value);
  }
}
