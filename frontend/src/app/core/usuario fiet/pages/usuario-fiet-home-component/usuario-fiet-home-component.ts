import { Component } from '@angular/core';
import { SideBarUsuarioFietComponent } from '../../components/side-bar-usuario-fiet-component/side-bar-usuario-fiet-component';
import { HeaderMainComponent } from '../../../../shared/headers/header-main-component/header-main-component';
import { BreadcrumbComponent } from '../../../../shared/breadcrumb/breadcrumb-component/breadcrumb-component';
import { PageComponent } from '../../../../shared/pages/page-component/page-component';
import { InfoUsuarioComponent } from '../../../../shared/pages/info-usuario-component/info-usuario-component';

/**
 * Componente principal de inicio para el usuario FIET.
 * Define la estructura base de la página principal del usuario.
 *
 * @author Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-usuario-fiet-home-component',
  imports: [PageComponent],
  templateUrl: './usuario-fiet-home-component.html',
  styleUrl: './usuario-fiet-home-component.css'
})
export class UsuarioFietHomeComponent {

  /**
   * Componente del sidebar del usuario FIET.
   */
  sidebar = SideBarUsuarioFietComponent;

  /**
   * Componente del encabezado principal.
   */
  header = HeaderMainComponent;

  /**
   * Componente de navegación tipo breadcrumb.
   */
  breadcrumb = BreadcrumbComponent

  /**
   * Componente principal que muestra la información del usuario.
   */
  main = InfoUsuarioComponent
}
