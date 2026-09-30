import { Component } from '@angular/core';
import { SidebarComponent } from '../../../../shared/sidebars/sidebar-component/sidebar-component';
import { Router } from '@angular/router';

/**
 * Componente de barra lateral para el rol funcionario.
 *
 * @author Julian David Camacho Erazo
 */
@Component({
  selector: 'app-side-bar-funcionario-component',
  imports: [SidebarComponent],
  templateUrl: './side-bar-funcionario-component.html',
  styleUrl: './side-bar-funcionario-component.css',
})
export class SideBarFuncionarioComponent {
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
