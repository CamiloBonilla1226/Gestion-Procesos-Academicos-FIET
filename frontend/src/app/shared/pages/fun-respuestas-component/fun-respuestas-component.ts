import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { ContentComponent } from '../content/content-component/content-component';
import { FunRespuestasContentComponent } from '../content/fun-respuestas-content-component/fun-respuestas-content-component';

/**
 * Componente de visualización de respuestas.
 * Permite mostrar el contenido específico de las respuestas mediante un componente hijo.
 * 
 * @autor Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-fun-respuestas-component',
  imports: [CommonModule, ContentComponent],
  templateUrl: './fun-respuestas-component.html',
  styleUrl: './fun-respuestas-component.css'
})
export class FunRespuestasComponent {
  /**
   * Componente de contenido que se mostrará dentro del componente principal.
   */
  content = FunRespuestasContentComponent;
}
