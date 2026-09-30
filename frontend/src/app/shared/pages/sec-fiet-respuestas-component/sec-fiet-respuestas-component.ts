import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { ContentComponent } from '../content/content-component/content-component';
import { SecFietRespuestasContentComponent } from '../content/sec-fiet-respuestas-content-component/sec-fiet-respuestas-content-component';

/**
 * Componente de visualización de respuestas.
 * Permite mostrar el contenido específico de las respuestas mediante un componente hijo.
 * 
 * @autor Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-sec-fiet-respuestas-component',
  imports: [CommonModule, ContentComponent],
  templateUrl: './sec-fiet-respuestas-component.html',
  styleUrl: './sec-fiet-respuestas-component.css'
})
export class SecFietRespuestasComponent {
  /**
     * Componente de contenido que se mostrará dentro del componente principal.
     */
    content = SecFietRespuestasContentComponent;
}
