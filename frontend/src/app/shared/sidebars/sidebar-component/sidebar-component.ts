import { CommonModule } from '@angular/common';
import { Component, Input, Output, EventEmitter } from '@angular/core';
import { ButtonGroupComponent } from '../../buttons/button-group-component/button-group-component';

/**
 * Componente de barra lateral (sidebar) con botones.
 * Permite mostrar un grupo de botones y emitir eventos al hacer clic en cada uno.
 * 
 * @autor Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-sidebar-component',
  imports: [CommonModule, ButtonGroupComponent],
  templateUrl: './sidebar-component.html',
  styleUrl: './sidebar-component.css'
})
export class SidebarComponent {
  /**
   * Lista de botones a mostrar en la barra lateral.
   * Cada botón puede tener una etiqueta, una imagen opcional y una ruta opcional.
   */
  @Input() buttons: { label: string, imgUrl?: string, route?: string }[] = [];

  /**
   * Evento que se emite cuando se hace clic en un botón.
   * Emite la ruta asociada al botón, si existe.
   */
  @Output() buttonClicked = new EventEmitter<string>();

  /**
   * Maneja el clic en un botón y emite la ruta correspondiente.
   * 
   * @param route Ruta asociada al botón clickeado.
   */
  onButtonClick(route?: string) {
    if(route) this.buttonClicked.emit(route);
  }
}
