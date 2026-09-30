import { Component, Input, Output, EventEmitter } from '@angular/core';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { CommonModule } from '@angular/common';
import { SimpleButtonComponent } from '../buttons/simple-button-component/simple-button-component';

/**
 * Componente genérico para mostrar un diálogo con múltiples pasos (wizard).
 * Permite navegar entre pasos y validar la continuidad de cada uno.
 * 
 * @autor Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-generic-dialog-steps-form-component',
  imports: [CommonModule, DialogModule, ButtonModule, SimpleButtonComponent],
  templateUrl: './generic-dialog-steps-form-component.html',
  styleUrl: './generic-dialog-steps-form-component.css'
})
export class GenericDialogStepsFormComponent {
  private _visible: boolean = false;

  /** Título del diálogo */
  @Input() title: string = '';

  /**
   * Configuración de los pasos del diálogo
   * Cada paso puede definir un título, un template de contenido y una función opcional canContinue
   */
  @Input() steps: { 
    title: string; 
    contentTemplate: any;
    canContinue?: () => boolean;
  }[] = [];

  /** Emite cuando se cambia la visibilidad del diálogo */
  @Output() visibleChange = new EventEmitter<boolean>();

  /** Emite cuando se guarda el formulario */
  @Output() save = new EventEmitter<void>();

  /** Paso actual en el diálogo */
  currentStep = 0;

  /** Controla la visibilidad del diálogo y reinicia el paso actual al abrirse */
  @Input()
  set visible(value: boolean) {
    this._visible = value;
    if (value === true) 
      this.currentStep = 0;
  }

  get visible(): boolean {
    return this._visible;
  }

  /** Cierra el diálogo */
  close() {
    this.visibleChange.emit(false);
  }

  /** Avanza al siguiente paso si puede continuar */
  nextStep() {
    const current = this.steps[this.currentStep];

    if (current.canContinue && !current.canContinue()) 
      return;

    if (this.currentStep < this.steps.length - 1) 
      this.currentStep++;
  }

  /** Retrocede al paso anterior */
  prevStep() {
    if (this.currentStep > 0) {
      this.currentStep--;
    }
  }

  /** Emite la acción de guardar */
  onSave() {
    this.save.emit();
  }
}
