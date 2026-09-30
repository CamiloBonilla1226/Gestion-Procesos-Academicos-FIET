import { Component, Input, Output, EventEmitter } from '@angular/core';
import { FloatLabelModule } from 'primeng/floatlabel';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

/**
 * Componente de entrada de contraseña.
 * Permite ingresar una contraseña con soporte para validación de obligatoriedad y emisión de cambios.
 * 
 * @autor Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-input-password-component',
  imports: [CommonModule, FloatLabelModule, FormsModule],
  templateUrl: './input-password-component.html',
  styleUrl: './input-password-component.css'
})
export class InputPasswordComponent {
  /**
   * Etiqueta que se muestra junto al campo de contraseña.
   */
  @Input() label: string = '';    

  /**
   * Valor actual de la contraseña.
   */
  @Input() value: string = '';     

  /**
   * Indica si el campo de contraseña es obligatorio.
   */
  @Input() required: boolean = false;

  /**
   * Evento que se emite cuando cambia el valor de la contraseña.
   */
  @Output() valueChange = new EventEmitter<string>(); 

  /**
   * Indica si el campo ha sido tocado por el usuario.
   */
  touched: boolean = false;
  
  /**
   * Maneja el cambio en el input de contraseña y emite el valor actualizado.
   * 
   * @param event Evento de cambio del input.
   */
  onValueChange(event: Event) {
    const input = event.target as HTMLInputElement;
    this.value = input.value;
    this.valueChange.emit(this.value);
  }

  /**
   * Determina si el campo de contraseña es inválido según la obligatoriedad y si ha sido tocado.
   * 
   * @returns `true` si el campo es obligatorio, está vacío y ha sido tocado; `false` en caso contrario.
   */
  isInvalid(): boolean {
    return this.required && (!this.value || this.value === '') && this.touched;
  }

  /**
   * Reinicia el valor del campo de contraseña y marca el campo como no tocado.
   */
  public reset(): void {
    this.value = '';
    this.valueChange.emit(this.value);
    this.touched = false;
  }

  /**
   * Asigna un nuevo valor al campo de contraseña y emite el cambio.
   * 
   * @param newValue Nuevo valor de la contraseña.
   */
  public setValue(newValue: string): void {
    this.value = newValue;
    this.valueChange.emit(this.value);
  }
}
