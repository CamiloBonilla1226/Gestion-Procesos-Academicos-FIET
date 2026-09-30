import { Component } from '@angular/core';
import { SidebarComponent } from '../../../../shared/sidebars/sidebar-component/sidebar-component';
import { Router } from '@angular/router';

/**
 * Componente del sidebar para el usuario FIET.
 * Permite la navegación entre las diferentes rutas disponibles para el usuario.
 *
 * @author Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-side-bar-usuario-fiet-component',
  imports: [SidebarComponent],
  templateUrl: './side-bar-usuario-fiet-component.html',
  styleUrl: './side-bar-usuario-fiet-component.css'
})
export class SideBarUsuarioFietComponent {
  
  constructor(private router: Router){}

  /**
   * Maneja el evento de clic en una opción del sidebar.
   * Navega a la ruta recibida como parámetro.
   *
   * @param route Ruta a la que se debe navegar
   */
  onSidebarButtonClick(route: string) {
    this.router.navigate([route]);
  }
}
