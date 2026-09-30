import { Component, Input, Output, EventEmitter } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

/**
 * Componente de área de texto (textarea) para ingresar texto largo.
 * Permite ingresar varias líneas de texto, con soporte para validación de obligatoriedad y emisión de cambios.
 * 
 * @autor Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-input-text-tarea-component',
  imports: [CommonModule, FormsModule],
  templateUrl: './input-text-tarea-component.html',
  styleUrl: './input-text-tarea-component.css'
})
export class InputTextTareaComponent {
  /**
   * Etiqueta que se muestra junto al área de texto.
   */
  @Input() label: string = '';

  /**
   * Valor actual del área de texto.
   */
  @Input() value: string = '';

  /**
   * Indica si el campo es obligatorio.
   */
  @Input() required: boolean = false;

  /**
   * Indica si se debe forzar la validación aunque no haya sido tocado.
   */
  @Input() forceValidation: boolean = false;

  /**
   * Evento que se emite cuando cambia el valor del área de texto.
   */
  @Output() valueChange = new EventEmitter<string>();
  
  /**
   * Indica si el área de texto ha sido tocada por el usuario.
   */
  touched: boolean = false;

  /**
   * Maneja el cambio de valor en el área de texto y emite `valueChange`.
   * 
   * @param event Evento de cambio del textarea.
   */
  onValueChange(event: Event) {
    const input = event.target as HTMLTextAreaElement;
    this.value = input.value;
    this.valueChange.emit(this.value);
  }

  /**
   * Determina si el área de texto es inválida según la obligatoriedad y el estado de toque o validación forzada.
   * 
   * @returns `true` si el campo es obligatorio y está vacío; `false` en caso contrario.
   */
  isInvalid(): boolean {
    const empty = !this.value || this.value.trim() === '';
    return this.required && empty && (this.touched || this.forceValidation);
  }

  /**
   * Reinicia el área de texto, eliminando el valor y restableciendo el estado de toque y validación forzada.
   */
  public reset(): void {
    this.value = '';
    this.valueChange.emit(this.value);
    this.touched = false;
    this.forceValidation = false;
  }

  /**
   * Asigna un nuevo valor al área de texto y emite el cambio.
   * 
   * @param newValue Nuevo valor a asignar.
   */
  public setValue(newValue: string): void {
    this.value = newValue;
    this.valueChange.emit(this.value);
  }
}
