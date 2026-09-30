import { Component } from '@angular/core';
import { HeaderMainComponent } from '../../../../shared/headers/header-main-component/header-main-component';
import { BreadcrumbComponent } from '../../../../shared/breadcrumb/breadcrumb-component/breadcrumb-component';
import { FunRespuestasComponent } from '../../../../shared/pages/fun-respuestas-component/fun-respuestas-component';
import { CommonModule } from '@angular/common';
import { PageComponent } from '../../../../shared/pages/page-component/page-component';
import { SideBarSecretariaFietComponent } from '../../components/side-bar-secretaria-fiet-component/side-bar-secretaria-fiet-component';
import { SecFietRespuestasComponent } from '../../../../shared/pages/sec-fiet-respuestas-component/sec-fiet-respuestas-component';

/**
 * Componente principal para la gestión de solicitudes del usuario funcionario.
 *
 * @author Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-secretaria-fiet-respuestas-component',
  imports: [PageComponent, CommonModule],
  templateUrl: './secretaria-fiet-respuestas-component.html',
  styleUrl: './secretaria-fiet-respuestas-component.css'
})
export class SecretariaFietRespuestasComponent {
  /**
     * Componente de barra lateral del usuario secretaria fiet.
     */
    sidebar = SideBarSecretariaFietComponent;
  
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
    main = SecFietRespuestasComponent;
}
