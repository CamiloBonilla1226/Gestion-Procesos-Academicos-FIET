import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SideBarSecretarioGeneralComponent } from '../../components/side-bar-secretario-general-component/side-bar-secretario-general-component';
import { HeaderMainComponent } from '../../../../shared/headers/header-main-component/header-main-component';
import { BreadcrumbComponent } from '../../../../shared/breadcrumb/breadcrumb-component/breadcrumb-component';
import { PageComponent } from '../../../../shared/pages/page-component/page-component';
import { LogsComponent } from '../../../../shared/pages/logs-component/logs-component';

/**
 * Componente de visualización de registros (logs) para el usuario Secretario General.
 * Permite acceder y consultar los registros del sistema.
 *
 * @author Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-secretario-general-log-component',
  imports: [CommonModule, PageComponent],
  templateUrl: './secretario-general-log-component.html',
  styleUrl: './secretario-general-log-component.css'
})
export class SecretarioGeneralLogComponent {

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
   * Componente principal que muestra los registros del sistema.
   */
  main = LogsComponent
}
