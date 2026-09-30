import { CommonModule } from '@angular/common';
import { Component, Input, Type } from '@angular/core';
import { ButtonComponent } from '../buttons/button-component/button-component';

/**
 * Configuración de un botón en el CardMainComponent.
 */
export interface ButtonConfig {
  imgUrl: string;
  color: string;
  width: string;
  height: string;
  onClick: () => void;
}

/**
 * Componente de tarjeta principal que permite mostrar un título, botones,
 * y componentes dinámicos tanto en la parte superior (preTitleComponent) como en el cuerpo (bodyComponent).
 * Se pueden pasar entradas dinámicas a los componentes hijos.
 * 
 * @autor Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-card-main-component',
  imports: [CommonModule, ButtonComponent],
  templateUrl: './card-main-component.html',
  styleUrl: './card-main-component.css'
})
export class CardMainComponent {

  /**
   * Componente que se renderiza antes del título (opcional).
   */
  @Input() preTitleComponent: Type<any> | null = null;

  /**
   * Título del card.
   */
  @Input() title: string = '';

  /**
   * Lista de botones configurables que se muestran en la cabecera del card.
   */
  @Input() buttons: ButtonConfig[] = [];

  /**
   * Componente que se renderiza en el cuerpo del card (opcional).
   */
  @Input() bodyComponent: Type<any> | null = null;

  /**
   * Inputs dinámicos que se pasan al componente del cuerpo.
   */
  @Input() bodyInputs: Record<string, any> = {};

  /**
   * Inputs dinámicos que se pasan al componente preTitle.
   */
  @Input() preTitleInputs: Record<string, any> = {};
}
