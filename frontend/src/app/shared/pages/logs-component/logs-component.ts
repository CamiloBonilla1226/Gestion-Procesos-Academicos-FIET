import { Component } from '@angular/core';
import { ContentComponent } from '../content/content-component/content-component';
import { CommonModule } from '@angular/common';
import { LogContentComponent } from '../content/log-content-component/log-content-component';

/**
 * Componente de visualización de logs.
 * Permite mostrar el contenido específico de logs mediante un componente hijo.
 * 
 * @autor Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-logs-component',
  imports: [CommonModule, ContentComponent],
  templateUrl: './logs-component.html',
  styleUrl: './logs-component.css'
})
export class LogsComponent {
  /**
   * Componente de contenido que se mostrará dentro del componente principal.
   */
  content = LogContentComponent;
}
