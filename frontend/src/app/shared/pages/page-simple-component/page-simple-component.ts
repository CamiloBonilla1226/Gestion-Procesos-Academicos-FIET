import { CommonModule } from '@angular/common';
import { Component, Input, Type } from '@angular/core';
import { LayoutSimpleComponent } from '../../../layouts/layout-simple-component/layout-simple-component';

/**
 * Componente de página simple.
 * Permite mostrar una página con un layout simple y un contenido principal.
 * 
 * @autor Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-page-simple-component',
  imports: [CommonModule, LayoutSimpleComponent],
  templateUrl: './page-simple-component.html',
  styleUrl: './page-simple-component.css'
})
export class PageSimpleComponent {
  /**
   * Componente que se mostrará como contenido principal de la página.
   */
  @Input() mainComponent!: Type<any>;
}
