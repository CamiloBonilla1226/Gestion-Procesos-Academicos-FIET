import { Component } from '@angular/core';
import { ContentComponent } from '../content/content-component/content-component';
import { CommonModule } from '@angular/common';
import { RolesContentComponent } from '../content/roles-content-component/roles-content-component';

/**
 * Componente de gestión de roles.
 * Permite mostrar el contenido específico de roles mediante un componente hijo.
 * 
 * @autor Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-roles-component',
  imports: [CommonModule, ContentComponent],
  templateUrl: './roles-component.html',
  styleUrl: './roles-component.css'
})
export class RolesComponent {
  /**
   * Componente de contenido que se mostrará dentro del componente principal.
   */
  content = RolesContentComponent; 
}
