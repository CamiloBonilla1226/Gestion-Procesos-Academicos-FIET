import { Component } from '@angular/core';
import { PageComponent } from '../../../../shared/pages/page-component/page-component';
import { SideBarSecretariaFietComponent } from '../../components/side-bar-secretaria-fiet-component/side-bar-secretaria-fiet-component';
import { BreadcrumbComponent } from '../../../../shared/breadcrumb/breadcrumb-component/breadcrumb-component';
import { InfoUsuarioComponent } from '../../../../shared/pages/info-usuario-component/info-usuario-component';
import { HeaderMainComponent } from '../../../../shared/headers/header-main-component/header-main-component';

/**
 * Componente principal de inicio para el usuario secretaria FIET.
 *
 * @author Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-secretaria-fiet-home-component',
  imports: [PageComponent],
  templateUrl: './secretaria-fiet-home-component.html',
  styleUrl: './secretaria-fiet-home-component.css'
})
export class SecretariaFietHomeComponent {
  /**
     * Componente de barra lateral.
     */
    sidebar = SideBarSecretariaFietComponent;
  
    /**
     * Componente de encabezado principal.
     */
    header = HeaderMainComponent;
  
    /**
     * Componente de navegación breadcrumb.
     */
    breadcrumb = BreadcrumbComponent;
  
    /**
     * Componente principal de información del usuario.
     */
    main = InfoUsuarioComponent;
}
