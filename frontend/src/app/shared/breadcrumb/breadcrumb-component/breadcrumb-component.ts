import { Component, Input } from '@angular/core';

/**
 * Componente breadcrumb encargado de mostrar la ruta de navegación
 * actual dentro de la aplicación según el rol y las secciones visitadas.
 *
 * @author Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-breadcrumb-component',
  imports: [],
  templateUrl: './breadcrumb-component.html',
  styleUrl: './breadcrumb-component.css'
})
export class BreadcrumbComponent {

  /**
   * Nombre del rol actual del usuario.
   */
  @Input() role: string = '';

  /**
   * Lista de elementos que conforman la ruta de navegación (breadcrumb).
   * Cada elemento puede incluir una etiqueta y una URL opcional.
   */
  @Input() breadcrumbs: { label: string, url?: string }[] = [];
}
