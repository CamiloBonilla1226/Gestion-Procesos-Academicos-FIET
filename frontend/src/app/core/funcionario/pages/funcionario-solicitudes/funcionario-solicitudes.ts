import { Component } from '@angular/core';
import { PageComponent } from '../../../../shared/pages/page-component/page-component';
import { CommonModule } from '@angular/common';
import { SideBarFuncionarioComponent } from '../../components/side-bar-funcionario-component/side-bar-funcionario-component';
import { HeaderMainComponent } from '../../../../shared/headers/header-main-component/header-main-component';
import { BreadcrumbComponent } from '../../../../shared/breadcrumb/breadcrumb-component/breadcrumb-component';
import { FunSolicitudesComponent } from '../../../../shared/pages/fun-solicitudes-component/fun-solicitudes-component';

/**
 * Componente principal para la gestión de solicitudes del usuario funcionario.
 *
 * @author Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-funcionario-solicitudes',
  imports: [PageComponent, CommonModule],
  templateUrl: './funcionario-solicitudes.html',
  styleUrl: './funcionario-solicitudes.css',
})
export class FuncionarioSolicitudes {
  /**
   * Componente de barra lateral del usuario funcionario.
   */
  sidebar = SideBarFuncionarioComponent;

  /**
   * Componente de encabezado principal.
   */
  header = HeaderMainComponent;

  /**
   * Componente de navegación tipo breadcrumb.
   */
  breadcrumb = BreadcrumbComponent;

  /**
   * Componente principal que muestra las solicitudes del funcionario.
   */
  main = FunSolicitudesComponent;
}
