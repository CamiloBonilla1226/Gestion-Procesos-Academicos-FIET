import { Component, Input, Output, EventEmitter } from '@angular/core';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { CommonModule } from '@angular/common';
import { SimpleButtonComponent } from '../buttons/simple-button-component/simple-button-component';

/**
 * Componente genérico de diálogo con formulario.
 * Permite mostrar un modal con título, contenido y botones de acción.
 * 
 * @autor Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-generic-dialog-form-component',
  imports: [CommonModule, DialogModule, ButtonModule, SimpleButtonComponent],
  templateUrl: './generic-dialog-form-component.html',
  styleUrl: './generic-dialog-form-component.css'
})
export class GenericDialogFormComponent {
  @Input() visible: boolean = false;
  @Input() title: string = '';
  @Output() visibleChange = new EventEmitter<boolean>();
  @Output() save = new EventEmitter<void>();

  /**
   * Cierra el diálogo emitiendo visibleChange como false
   */
  close() {
    this.visibleChange.emit(false);
  }

  /**
   * Dispara el evento save al presionar el botón de guardar
   */
  onSave() {
    this.save.emit(); 
  }
}
