import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { ContentComponent } from '../content/content-component/content-component';
import { UsuarioFietSolicitudesPrivadasContentComponent } from '../content/usuario-fiet-solicitudes-privadas-content-component/usuario-fiet-solicitudes-privadas-content-component';

/**
 * Componente de solicitudes privadas para usuarios FIET.
 * Permite mostrar el contenido específico de solicitudes privadas mediante un componente hijo.
 * 
 * @autor Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-usuario-fiet-solicitudes-privadas-component',
  imports: [CommonModule, ContentComponent],
  templateUrl: './usuario-fiet-solicitudes-privadas-component.html',
  styleUrl: './usuario-fiet-solicitudes-privadas-component.css'
})
export class UsuarioFietSolicitudesPrivadasComponent {
  /**
   * Componente de contenido que se mostrará dentro del componente principal.
   */
  content = UsuarioFietSolicitudesPrivadasContentComponent;
}
