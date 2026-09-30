import { CommonModule } from '@angular/common';
import { Component, Input, TemplateRef } from '@angular/core';

/**
 * Interfaz que define la estructura de los encabezados de la tabla.
 */
export interface TableHeader {
  /**
   * Título del encabezado.
   */
  title: string;

  /**
   * Template opcional para personalizar el contenido del encabezado.
   */
  headerTemplate?: TemplateRef<any>;
}

/**
 * Componente de tabla genérica.
 * Permite mostrar datos dinámicos con encabezados configurables y plantillas de acción.
 * 
 * @autor Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-table-generic-component',
  imports: [CommonModule],
  templateUrl: './table-generic-component.html',
  styleUrl: './table-generic-component.css'
})
export class TableGenericComponent {
  /**
   * Lista de encabezados de la tabla.
   */
  @Input() headers: TableHeader[] = [];

  /**
   * Datos a mostrar en la tabla.
   */
  @Input() data: any[] = [];

  /**
   * Template para mostrar acciones personalizadas por fila.
   */
  @Input() actionTemplate!: TemplateRef<any>; 
}
