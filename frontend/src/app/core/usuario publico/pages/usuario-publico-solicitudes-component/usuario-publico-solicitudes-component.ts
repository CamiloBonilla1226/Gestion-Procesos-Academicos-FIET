import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { PageSimpleComponent } from '../../../../shared/pages/page-simple-component/page-simple-component';
import { UsuarioPublicoSolicitudesContentComponent } from '../../components/usuario-publico-solicitudes-content-component/usuario-publico-solicitudes-content-component';

/**
 * Componente contenedor de la vista principal de solicitudes
 * para el usuario público.
 *
 * Se encarga de definir qué componente se renderiza
 * como contenido principal dentro de la página simple.
 *
 * @author Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-usuario-publico-solicitudes-component',
  imports: [CommonModule, PageSimpleComponent],
  templateUrl: './usuario-publico-solicitudes-component.html',
  styleUrl: './usuario-publico-solicitudes-component.css'
})
export class UsuarioPublicoSolicitudesComponent {
  /**
   * Componente principal que se mostrará dentro de la página
   * correspondiente a las solicitudes del usuario público
   */
  main = UsuarioPublicoSolicitudesContentComponent;
}
