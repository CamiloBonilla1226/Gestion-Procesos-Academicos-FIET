import { Component } from '@angular/core';
import { SidebarComponent } from '../../../../shared/sidebars/sidebar-component/sidebar-component';
import { Router } from '@angular/router';

/**
 * Componente de barra lateral para el usuario Secretario General.
 * Permite la navegación entre las diferentes vistas del sistema.
 *
 * @author Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-side-bar-secretario-general-component',
  imports: [SidebarComponent],
  templateUrl: './side-bar-secretario-general-component.html',
  styleUrl: './side-bar-secretario-general-component.css'
})
export class SideBarSecretarioGeneralComponent {

  /**
   * Constructor del componente.
   * @param router Servicio de enrutamiento de Angular
   */
  constructor(private router: Router){}

  /**
   * Maneja el evento de clic en las opciones de la barra lateral
   * y redirige a la ruta correspondiente.
   *
   * @param route Ruta a la cual se desea navegar
   */
  onSidebarButtonClick(route: string) {
    this.router.navigate([route]);
  }
}
