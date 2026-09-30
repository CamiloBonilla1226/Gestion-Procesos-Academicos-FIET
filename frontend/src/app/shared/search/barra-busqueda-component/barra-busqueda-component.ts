import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms'; 

/**
 * Componente de barra de búsqueda.
 * Permite ingresar texto para filtrar o buscar, emitiendo eventos al cambiar el valor.
 * 
 * @autor Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-barra-busqueda-component',
  imports: [CommonModule, FormsModule],
  templateUrl: './barra-busqueda-component.html',
  styleUrl: './barra-busqueda-component.css'
})
export class BarraBusquedaComponent {
  /**
   * Texto que se muestra como placeholder en el input.
   */
  @Input() placeholder: string = 'Buscar...';

  /**
   * Valor actual del campo de búsqueda.
   */
  @Input() value: string = ''; 

  /**
   * Evento que se emite cuando cambia el valor del campo de búsqueda.
   */
  @Output() valueChange = new EventEmitter<string>(); 

  /**
   * Inicializa el componente y emite el valor inicial.
   */
  ngOnInit(): void {
    this.valueChange.emit(this.value);
  }

  /**
   * Maneja los cambios en el input y emite el valor actualizado.
   */
  onInputChange() {
    this.valueChange.emit(this.value);
  }
}
