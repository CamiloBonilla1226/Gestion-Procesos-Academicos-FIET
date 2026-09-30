import { Component } from '@angular/core';
import { ContentComponent } from '../content/content-component/content-component';
import { CommonModule } from '@angular/common';
import { InfoUsuarioContentComponent } from '../content/info-usuario-content-component/info-usuario-content-component';

/**
 * Componente de información de usuario.
 * Permite mostrar el contenido específico de información del usuario mediante un componente hijo.
 * 
 * @autor Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-info-usuario-component',
  imports: [CommonModule, ContentComponent],
  templateUrl: './info-usuario-component.html',
  styleUrl: './info-usuario-component.css'
})
export class InfoUsuarioComponent {
  /**
   * Componente de contenido que se mostrará dentro del componente principal.
   */
  content = InfoUsuarioContentComponent; 
}
