import { Component } from '@angular/core';
import { ContentComponent } from '../content/content-component/content-component';
import { CommonModule } from '@angular/common';
import { TipoSolicitudesContentComponent } from '../content/tipo-solicitudes-content-component/tipo-solicitudes-content-component';

/**
 * Componente de gestión de tipos de solicitudes.
 * Permite mostrar el contenido específico de tipos de solicitudes mediante un componente hijo.
 * 
 * @autor Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-tipo-solicitudes-component',
  imports: [CommonModule, ContentComponent],
  templateUrl: './tipo-solicitudes-component.html',
  styleUrl: './tipo-solicitudes-component.css'
})
export class TipoSolicitudesComponent {
  /**
   * Componente de contenido que se mostrará dentro del componente principal.
   */
  content = TipoSolicitudesContentComponent;
}
