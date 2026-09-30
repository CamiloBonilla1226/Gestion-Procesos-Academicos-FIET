import { Component, Input, Type } from '@angular/core';
import { FooterSimpleComponent } from '../../footers/footer-simple-component/footer-simple-component';
import { LayoutComponent } from '../../../layouts/layout-component/layout-component';
import { CommonModule } from '@angular/common';

/**
 * Componente de página genérica.
 * Permite estructurar una página con sidebar, header, breadcrumb, contenido principal y footer.
 * 
 * @autor Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-page-component',
  imports: [CommonModule, LayoutComponent, FooterSimpleComponent],
  templateUrl: './page-component.html',
  styleUrl: './page-component.css'
})
export class PageComponent {
  /**
   * Componente que se mostrará como sidebar.
   */
  @Input() sidebarComponent!: Type<any>;

  /**
   * Componente que se mostrará como header.
   */
  @Input() headerComponent!: Type<any>;

  /**
   * Componente que se mostrará como breadcrumb.
   */
  @Input() breadcrumbComponent!: Type<any>;

  /**
   * Datos o inputs que se pasarán al breadcrumbComponent.
   */
  @Input() breadcrumbInputs: any;

  /**
   * Componente que se mostrará como contenido principal de la página.
   */
  @Input() mainComponent!: Type<any>;
}
