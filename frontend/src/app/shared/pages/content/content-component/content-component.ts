import { CommonModule } from '@angular/common';
import { Component, Input, Type } from '@angular/core';

/**
 * Componente genérico de contenido.
 * Permite mostrar un título y un componente dinámico como cuerpo del contenido.
 * 
 * @autor Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-content-component',
  imports: [CommonModule],
  templateUrl: './content-component.html',
  styleUrl: './content-component.css'
})
export class ContentComponent {
  /**
   * Título que se mostrará en el contenido.
   */
  @Input() title: string = '';

  /**
   * Componente dinámico que se mostrará como cuerpo del contenido.
   */
  @Input() bodyComponent!: Type<any>;
}
