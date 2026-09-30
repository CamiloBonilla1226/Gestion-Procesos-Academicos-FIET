import { CommonModule } from '@angular/common';
import { Component, Input, Output, EventEmitter } from '@angular/core';

/**
 * Componente de paginación.
 * Permite navegar entre páginas y emite eventos cuando se cambia la página.
 * 
 * @autor Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-paginator',
  imports: [CommonModule],
  templateUrl: './paginator.html',
  styleUrl: './paginator.css'
})
export class Paginator {
  /**
   * Número total de páginas disponibles.
   */
  @Input() totalPages: number = 1;

  /**
   * Página actualmente activa.
   */
  @Input() currentPage: number = 1;

  /**
   * Evento que se emite cuando cambia la página.
   */
  @Output() pageChange = new EventEmitter<number>();

  /**
   * Número máximo de páginas visibles en la paginación.
   */
  maxVisiblePages = 5;

  /**
   * Array de páginas que se mostrarán en la paginación según la página actual y el máximo visible.
   */
  get totalPagesArray(): number[] {
    const pages: number[] = [];
    const half = Math.floor(this.maxVisiblePages / 2);

    let start = this.currentPage - half;
    let end = this.currentPage + half;

    if (start < 1) {
      start = 1;
      end = Math.min(this.maxVisiblePages, this.totalPages);
    }

    if (end > this.totalPages) {
      end = this.totalPages;
      start = Math.max(1, end - this.maxVisiblePages + 1);
    }

    for (let i = start; i <= end; i++) {
      pages.push(i);
    }

    return pages;
  }

  /**
   * Cambia a la página especificada y emite el evento `pageChange` si es válida.
   * 
   * @param page Número de página al que se desea navegar.
   */
  goToPage(page: number) {
    if (page >= 1 && page <= this.totalPages && page !== this.currentPage) 
      this.pageChange.emit(page);
  }
}
