import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { ContentComponent } from '../content/content-component/content-component';
import { FunSolicitudesContentComponent } from '../content/fun-solicitudes-content-component/fun-solicitudes-content-component';

/**
 * Componente de funcionalidad de solicitudes para funcionarios.
 * Permite mostrar el contenido específico de solicitudes mediante un componente hijo.
 * 
 * @autor Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-fun-solicitudes-component',
  imports: [CommonModule, ContentComponent],
  templateUrl: './fun-solicitudes-component.html',
  styleUrl: './fun-solicitudes-component.css'
})
export class FunSolicitudesComponent {
  /**
   * Componente de contenido que se mostrará dentro del componente principal.
   */
  content = FunSolicitudesContentComponent;
}
