import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { PageComponent } from '../../../../shared/pages/page-component/page-component';
import { HeaderMainComponent } from '../../../../shared/headers/header-main-component/header-main-component';
import { BreadcrumbComponent } from '../../../../shared/breadcrumb/breadcrumb-component/breadcrumb-component';
import { SideBarSecretarioGeneralComponent } from '../../components/side-bar-secretario-general-component/side-bar-secretario-general-component';
import { SecSolicitudesComponent } from '../../../../shared/pages/sec-solicitudes-component/sec-solicitudes-component';

/**
 * Componente principal para la gestión de solicitudes
 * del usuario Secretario General.
 *
 * @author Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-secretario-general-solicitudes',
  imports: [CommonModule, PageComponent],
  templateUrl: './secretario-general-solicitudes.html',
  styleUrl: './secretario-general-solicitudes.css'
})
export class SecretarioGeneralSolicitudes {

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
   * Componente principal que muestra las solicitudes del Secretario General.
   */
  main = SecSolicitudesComponent;
}
