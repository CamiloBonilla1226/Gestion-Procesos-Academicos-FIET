import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SideBarSecretarioGeneralComponent } from '../../components/side-bar-secretario-general-component/side-bar-secretario-general-component';
import { HeaderMainComponent } from '../../../../shared/headers/header-main-component/header-main-component';
import { BreadcrumbComponent } from '../../../../shared/breadcrumb/breadcrumb-component/breadcrumb-component';
import { PageComponent } from '../../../../shared/pages/page-component/page-component';
import { RolesComponent } from '../../../../shared/pages/roles-component/roles-component';

/**
 * Componente principal para la gestión de roles del sistema
 * para el usuario Secretario General.
 *
 * @author Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-secretario-general-roles-component',
  imports: [CommonModule, PageComponent],
  templateUrl: './secretario-general-roles-component.html',
  styleUrl: './secretario-general-roles-component.css'
})
export class SecretarioGeneralRolesComponent {

  /**
   * Componente de barra lateral del Secretario General.
   */
  sidebar = SideBarSecretarioGeneralComponent;

  /**
   * Componente de encabezado principal.
   */
  header = HeaderMainComponent;

  /**
   * Componente de navegación tipo breadcrumb.
   */
  breadcrumb = BreadcrumbComponent;

  /**
   * Componente principal que gestiona los roles del sistema.
   */
  main = RolesComponent;
}
