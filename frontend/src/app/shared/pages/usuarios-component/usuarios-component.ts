import { Component } from '@angular/core';
import { ContentComponent } from '../content/content-component/content-component';
import { CommonModule } from '@angular/common';
import { UsuariosContentComponent } from '../content/usuarios-content-component/usuarios-content-component';

/**
 * Componente de gestión de usuarios.
 * Permite mostrar el contenido específico de usuarios mediante un componente hijo.
 * 
 * @autor Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-usuarios-component',
  imports: [CommonModule, ContentComponent],
  templateUrl: './usuarios-component.html',
  styleUrl: './usuarios-component.css'
})
export class UsuariosComponent {
  /**
   * Componente de contenido que se mostrará dentro del componente principal.
   */
  content = UsuariosContentComponent; 
}
