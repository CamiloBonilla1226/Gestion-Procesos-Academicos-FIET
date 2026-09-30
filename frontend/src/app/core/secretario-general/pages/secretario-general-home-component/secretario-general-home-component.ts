import { Component } from '@angular/core';
import { SideBarSecretarioGeneralComponent } from '../../components/side-bar-secretario-general-component/side-bar-secretario-general-component';
import { HeaderMainComponent } from '../../../../shared/headers/header-main-component/header-main-component';
import { BreadcrumbComponent } from '../../../../shared/breadcrumb/breadcrumb-component/breadcrumb-component';
import { PageComponent } from '../../../../shared/pages/page-component/page-component';
import { InfoUsuarioComponent } from '../../../../shared/pages/info-usuario-component/info-usuario-component';

/**
 * Componente principal de inicio para el usuario Secretario General.
 * Organiza la estructura base de la página con barra lateral, encabezado,
 * navegación breadcrumb y contenido principal.
 *
 * @author Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-secretario-general-home-component',
  imports: [PageComponent],
  templateUrl: './secretario-general-home-component.html',
  styleUrl: './secretario-general-home-component.css'
})
export class SecretarioGeneralHomeComponent {

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
  breadcrumb = BreadcrumbComponent

  /**
   * Componente principal con la información del usuario.
   */
  main = InfoUsuarioComponent
}
