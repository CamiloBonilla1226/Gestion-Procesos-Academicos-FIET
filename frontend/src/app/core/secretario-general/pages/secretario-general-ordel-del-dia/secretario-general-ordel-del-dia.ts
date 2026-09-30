import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { PageComponent } from '../../../../shared/pages/page-component/page-component';
import { HeaderMainComponent } from '../../../../shared/headers/header-main-component/header-main-component';
import { BreadcrumbComponent } from '../../../../shared/breadcrumb/breadcrumb-component/breadcrumb-component';
import { SideBarSecretarioGeneralComponent } from '../../components/side-bar-secretario-general-component/side-bar-secretario-general-component';
import { OrdenDelDiaComponent } from '../../../../shared/pages/orden-del-dia-component/orden-del-dia-component';

/**
 * Componente principal para la gestión del Orden del Día
 * del usuario Secretario General.
 *
 * @author Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-secretario-general-ordel-del-dia',
  imports: [CommonModule, PageComponent],
  templateUrl: './secretario-general-ordel-del-dia.html',
  styleUrl: './secretario-general-ordel-del-dia.css'
})
export class SecretarioGeneralOrdelDelDia {

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
   * Componente principal que gestiona el Orden del Día.
   */
  main = OrdenDelDiaComponent;
}
