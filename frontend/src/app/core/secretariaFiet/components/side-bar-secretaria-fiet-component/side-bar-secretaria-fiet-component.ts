import { Component } from '@angular/core';
import { SidebarComponent } from '../../../../shared/sidebars/sidebar-component/sidebar-component';
import { Router } from '@angular/router';

/**
 * Componente de barra lateral para el rol funcionario.
 *
 * @author Julian David Camacho Erazo
 */
@Component({
  selector: 'app-side-bar-secretaria-fiet-component',
  imports: [SidebarComponent],
  templateUrl: './side-bar-secretaria-fiet-component.html',
  styleUrl: './side-bar-secretaria-fiet-component.css'
})
export class SideBarSecretariaFietComponent {
  constructor(private router: Router) {}

  /**
   * Navega a la ruta seleccionada desde la barra lateral.
   *
   * @param route ruta destino
   */
  onSidebarButtonClick(route: string) {
    this.router.navigate([route]);
  }
}
