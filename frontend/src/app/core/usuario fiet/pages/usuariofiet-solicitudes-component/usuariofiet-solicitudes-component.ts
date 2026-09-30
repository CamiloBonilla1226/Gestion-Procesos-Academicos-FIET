import { Component } from '@angular/core';
import { SideBarUsuarioFietComponent } from '../../components/side-bar-usuario-fiet-component/side-bar-usuario-fiet-component';
import { HeaderMainComponent } from '../../../../shared/headers/header-main-component/header-main-component';
import { BreadcrumbComponent } from '../../../../shared/breadcrumb/breadcrumb-component/breadcrumb-component';
import { PageComponent } from '../../../../shared/pages/page-component/page-component';
import { UsuarioFietSolicitudesPrivadasComponent } from '../../../../shared/pages/usuario-fiet-solicitudes-privadas-component/usuario-fiet-solicitudes-privadas-component';

/**
 * Componente que gestiona la vista de solicitudes privadas del usuario FIET.
 * Define la estructura de la página de solicitudes del usuario.
 *
 * @author Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-usuariofiet-solicitudes-component',
  imports: [PageComponent],
  templateUrl: './usuariofiet-solicitudes-component.html',
  styleUrl: './usuariofiet-solicitudes-component.css'
})
export class UsuariofietSolicitudesComponent {

  /**
   * Componente del sidebar para el usuario FIET.
   */
  sidebar = SideBarUsuarioFietComponent;

  /**
   * Componente del encabezado principal.
   */
  header = HeaderMainComponent;

  /**
   * Componente breadcrumb para la navegación.
   */
  breadcrumb = BreadcrumbComponent

  /**
   * Componente principal que muestra las solicitudes privadas del usuario FIET.
   */
  main = UsuarioFietSolicitudesPrivadasComponent
}
