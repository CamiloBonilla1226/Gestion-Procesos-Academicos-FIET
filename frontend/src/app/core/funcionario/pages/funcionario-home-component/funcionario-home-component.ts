import { Component } from '@angular/core';
import { PageComponent } from '../../../../shared/pages/page-component/page-component';
import { SideBarFuncionarioComponent } from '../../components/side-bar-funcionario-component/side-bar-funcionario-component';
import { BreadcrumbComponent } from '../../../../shared/breadcrumb/breadcrumb-component/breadcrumb-component';
import { InfoUsuarioComponent } from '../../../../shared/pages/info-usuario-component/info-usuario-component';
import { HeaderMainComponent } from '../../../../shared/headers/header-main-component/header-main-component';

/**
 * Componente principal de inicio para el usuario funcionario.
 *
 * @author Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-funcionario-home-component',
  imports: [PageComponent],
  templateUrl: './funcionario-home-component.html',
  styleUrl: './funcionario-home-component.css'
})
export class FuncionarioHomeComponent {
  /**
   * Componente de barra lateral.
   */
  sidebar = SideBarFuncionarioComponent;

  /**
   * Componente de encabezado principal.
   */
  header = HeaderMainComponent;

  /**
   * Componente de navegación breadcrumb.
   */
  breadcrumb = BreadcrumbComponent;

  /**
   * Componente principal de información del usuario.
   */
  main = InfoUsuarioComponent;
}
