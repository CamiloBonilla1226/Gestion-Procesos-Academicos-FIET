import { Component, Input, Output, EventEmitter } from '@angular/core';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { CommonModule } from '@angular/common';
import { SimpleButtonComponent } from '../buttons/simple-button-component/simple-button-component';

/**
 * Componente genérico para mostrar información en un diálogo modal.
 * Permite mostrar un título, contenido basado en un objeto y contenido personalizado opcional.
 * 
 * @autor Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-generic-dialog-info-component',
  imports: [CommonModule, DialogModule, ButtonModule, SimpleButtonComponent],
  templateUrl: './generic-dialog-info-component.html',
  styleUrl: './generic-dialog-info-component.css'
})
export class GenericDialogInfoComponent {
  @Input() visible: boolean = false;       
  @Input() title: string = ''; 
  @Input() data: Record<string, any> = {};
  @Input() showCustomContent: boolean = false;

  @Output() visibleChange = new EventEmitter<boolean>(); 

  /**
   * Cierra el diálogo emitiendo visibleChange como false
   */
  close() {
    this.visibleChange.emit(false);
  }

  /**
   * Obtiene las llaves de un objeto
   * @param obj Objeto del cual obtener las llaves
   * @returns Array de llaves del objeto
   */
  objectKeys(obj: any): string[] {
    return obj ? Object.keys(obj) : [];
  }

  /**
   * Formatea una llave de objeto a un formato legible
   * Reemplaza guiones bajos por espacios y capitaliza cada palabra
   * @param key La llave a formatear
   * @returns La llave formateada
   */
  formatKey(key: string): string {
    return key
      .replace(/_/g, ' ')             
      .replace(/\w\S*/g, (w) =>      
        w.charAt(0).toUpperCase() + w.slice(1).toLowerCase()
      );
  }
}
