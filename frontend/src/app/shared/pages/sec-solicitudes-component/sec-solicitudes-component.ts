import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { ContentComponent } from '../content/content-component/content-component';
import { SecContentComponent } from '../content/sec-content-component/sec-content-component';

/**
 * Componente de solicitudes para Secretaría General.
 * Permite mostrar el contenido específico de solicitudes mediante un componente hijo.
 * 
 * @autor Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-sec-solicitudes-component',
  imports: [CommonModule, ContentComponent],
  templateUrl: './sec-solicitudes-component.html',
  styleUrl: './sec-solicitudes-component.css'
})
export class SecSolicitudesComponent {
  /**
   * Componente de contenido que se mostrará dentro del componente principal.
   */
  content = SecContentComponent;
}
