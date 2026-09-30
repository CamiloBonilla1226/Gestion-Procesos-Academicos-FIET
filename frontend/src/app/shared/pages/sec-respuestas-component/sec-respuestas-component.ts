import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { ContentComponent } from '../content/content-component/content-component';
import { SecRespuestasContentComponent } from '../content/sec-respuestas-content-component/sec-respuestas-content-component';

/**
 * Componente de visualización de respuestas.
 * Permite mostrar el contenido específico de las respuestas mediante un componente hijo.
 * 
 * @autor Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-sec-respuestas-component',
  imports: [CommonModule, ContentComponent],
  templateUrl: './sec-respuestas-component.html',
  styleUrl: './sec-respuestas-component.css'
})
export class SecRespuestasComponent {
  /**
   * Componente de contenido que se mostrará dentro del componente principal.
   */
  content = SecRespuestasContentComponent;
}
