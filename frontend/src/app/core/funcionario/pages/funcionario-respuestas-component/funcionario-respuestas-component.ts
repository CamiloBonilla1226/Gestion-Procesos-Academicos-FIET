import { Component } from '@angular/core';
import { SideBarFuncionarioComponent } from '../../components/side-bar-funcionario-component/side-bar-funcionario-component';
import { HeaderMainComponent } from '../../../../shared/headers/header-main-component/header-main-component';
import { BreadcrumbComponent } from '../../../../shared/breadcrumb/breadcrumb-component/breadcrumb-component';
import { FunRespuestasComponent } from '../../../../shared/pages/fun-respuestas-component/fun-respuestas-component';
import { CommonModule } from '@angular/common';
import { PageComponent } from '../../../../shared/pages/page-component/page-component';

/**
 * Componente principal para la gestión de solicitudes del usuario funcionario.
 *
 * @author Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-funcionario-respuestas-component',
  imports: [PageComponent, CommonModule],
  templateUrl: './funcionario-respuestas-component.html',
  styleUrl: './funcionario-respuestas-component.css'
})
export class FuncionarioRespuestasComponent {
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
   * Componente principal que muestra las respuestas del funcionario.
   */
  main = FunRespuestasComponent;
}
