import { CommonModule } from '@angular/common';
import { Component, Input, Output, EventEmitter } from '@angular/core';
import { FormsModule } from '@angular/forms';

/**
 * Componente de selección desplegable (select).
 * Permite seleccionar un valor de una lista de opciones, con soporte para validación de obligatoriedad y emisión de eventos.
 * 
 * @autor Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-input-select-component',
  imports: [CommonModule, FormsModule],
  templateUrl: './input-select-component.html',
  styleUrl: './input-select-component.css'
})
export class InputSelectComponent {
  /**
   * Etiqueta que se muestra junto al select.
   */
  @Input() label: string = ''; 

  /**
   * Lista de opciones disponibles para seleccionar.
   */
  @Input() options: { label: string; value: any }[] = [];

  /**
   * Valor actualmente seleccionado.
   */
  @Input() value: any; 

  /**
   * Indica si el campo es obligatorio.
   */
  @Input() required: boolean = false;

  /**
   * Evento que se emite cuando cambia el valor seleccionado.
   */
  @Output() valueChange = new EventEmitter<any>(); 

  /**
   * Indica si se debe forzar la validación del campo aunque no haya sido tocado.
   */
  @Input() forceValidation: boolean = false;

  /**
   * Indica si el campo ha sido tocado por el usuario.
   */
  touched: boolean = false;

  /**
   * Evento que se emite cuando se abre el select.
   */
  @Output() open = new EventEmitter<void>();

  /**
   * Maneja la apertura del select y emite el evento `open`.
   */
  onOpen() {
    this.open.emit();
  }
  
  /**
   * Maneja el cambio de valor del select y emite `valueChange`.
   * 
   * @param newValue Nuevo valor seleccionado.
   */
  onValueChange(newValue: any) {
    this.value = newValue;
    this.valueChange.emit(this.value);
  }

  /**
   * Determina si el campo es inválido según la obligatoriedad, el valor y el estado de toque o validación forzada.
   * 
   * @returns `true` si el campo es obligatorio y está vacío; `false` en caso contrario.
   */
  isInvalid(): boolean {
    const empty =
      this.value === null ||
      this.value === undefined ||
      (typeof this.value === 'string' && this.value.trim() === '');

    return this.required && empty && (this.touched || this.forceValidation);
  }

  /**
   * Función de comparación utilizada para determinar igualdad entre dos valores de opción.
   * 
   * @param o1 Primer objeto a comparar.
   * @param o2 Segundo objeto a comparar.
   * @returns `true` si los objetos son iguales, `false` en caso contrario.
   */
  compareFn = (o1: any, o2: any): boolean => {
    return JSON.stringify(o1) === JSON.stringify(o2);
  };

  /**
   * Reinicia el select, eliminando el valor seleccionado, restableciendo el estado de toque y la validación forzada.
   */
  public reset(): void {
    this.value = null;
    this.valueChange.emit(this.value);
    this.touched = false;
    this.forceValidation = false;
  }

  /**
   * Asigna un nuevo valor al select y emite el cambio.
   * 
   * @param newValue Nuevo valor a asignar.
   */
  public setValue(newValue: any): void {
    this.value = newValue;
    this.valueChange.emit(this.value);
  }
}
