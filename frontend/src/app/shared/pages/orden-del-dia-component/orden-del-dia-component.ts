import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { ContentComponent } from '../content/content-component/content-component';
import { OrdenDelDiaContentComponent } from '../content/orden-del-dia-content-component/orden-del-dia-content-component';

/**
 * Componente de visualización de la orden del día.
 * Permite mostrar el contenido específico de la orden del día mediante un componente hijo.
 * 
 * @autor Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-orden-del-dia-component',
  imports: [CommonModule, ContentComponent],
  templateUrl: './orden-del-dia-component.html',
  styleUrl: './orden-del-dia-component.css'
})
export class OrdenDelDiaComponent {
  /**
   * Componente de contenido que se mostrará dentro del componente principal.
   */
  content = OrdenDelDiaContentComponent;
}
