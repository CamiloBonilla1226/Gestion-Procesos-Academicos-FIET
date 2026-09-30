import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SideBarSecretarioGeneralComponent } from '../../components/side-bar-secretario-general-component/side-bar-secretario-general-component';
import { HeaderMainComponent } from '../../../../shared/headers/header-main-component/header-main-component';
import { BreadcrumbComponent } from '../../../../shared/breadcrumb/breadcrumb-component/breadcrumb-component';
import { PageComponent } from '../../../../shared/pages/page-component/page-component';
import { UsuariosComponent } from '../../../../shared/pages/usuarios-component/usuarios-component';

/**
 * Componente principal para la gestión de usuarios del sistema
 * para el usuario Secretario General.
 *
 * @author Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-secretario-general-usuarios-component',
  imports: [CommonModule, PageComponent],
  templateUrl: './secretario-general-usuarios-component.html',
  styleUrl: './secretario-general-usuarios-component.css'
})
export class SecretarioGeneralUsuariosComponent {

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
   * Componente principal que gestiona los usuarios del sistema.
   */
  main = UsuariosComponent;
}
