import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { PageComponent } from '../../../../shared/pages/page-component/page-component';
import { SideBarSecretarioGeneralComponent } from '../../components/side-bar-secretario-general-component/side-bar-secretario-general-component';
import { HeaderMainComponent } from '../../../../shared/headers/header-main-component/header-main-component';
import { BreadcrumbComponent } from '../../../../shared/breadcrumb/breadcrumb-component/breadcrumb-component';
import { SecRespuestasComponent } from '../../../../shared/pages/sec-respuestas-component/sec-respuestas-component';


/**
 * Componente principal para la gestión del Respuestas
 * del usuario Secretario General.
 *
 * @author Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-secretario-general-respuestas-component',
  imports: [CommonModule, PageComponent],
  templateUrl: './secretario-general-respuestas-component.html',
  styleUrl: './secretario-general-respuestas-component.css'
})
export class SecretarioGeneralRespuestasComponent {

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
   * Componente principal que gestiona las respuestas.
   */
  main = SecRespuestasComponent;
}
