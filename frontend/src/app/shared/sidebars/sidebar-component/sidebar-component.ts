import { CommonModule } from '@angular/common';
import { Component, Input, Output, EventEmitter } from '@angular/core';
import { Router } from '@angular/router';

export interface BotonSidebar {
  label: string;
  imgUrl?: string;
  route?: string;
  children?: BotonSidebar[];
}

/**
 * Componente de barra lateral (sidebar) con botones.
 * Permite mostrar un grupo de botones y emitir eventos al hacer clic en cada uno.
 *
 * @autor Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-sidebar-component',
  imports: [CommonModule],
  templateUrl: './sidebar-component.html',
  styleUrl: './sidebar-component.css'
})
export class SidebarComponent {
  /**
   * Lista de botones a mostrar en la barra lateral.
   * Cada botón puede tener una etiqueta, una imagen opcional y una ruta opcional.
   */
  @Input() buttons: BotonSidebar[] = [];

  /**
   * Evento que se emite cuando se hace clic en un botón.
   * Emite la ruta asociada al botón, si existe.
   */
  @Output() buttonClicked = new EventEmitter<string>();

  private grupos = new Map<string, boolean>();

  constructor(private router: Router) {}

  /**
   * Maneja el clic en un botón y emite la ruta correspondiente.
   *
   * @param route Ruta asociada al botón clickeado.
   */
  onButtonClick(route?: string) {
    if(route) this.buttonClicked.emit(route);
  }

  texto(label: string): string {
    return label.replace(/^\|\s*/, '');
  }

  esActivo(route?: string): boolean {
    return !!route && this.normalizar(route) === this.rutaActiva();
  }

  grupoActivo(boton: BotonSidebar): boolean {
    return (boton.children ?? []).some(hijo => this.esActivo(hijo.route));
  }

  estaAbierto(boton: BotonSidebar): boolean {
    return this.grupos.get(boton.label) ?? this.grupoActivo(boton);
  }

  alternar(boton: BotonSidebar): void {
    this.grupos.set(boton.label, !this.estaAbierto(boton));
  }

  private rutaActiva(): string | null {
    const actual = this.normalizar(this.router.url.split(/[?#]/)[0]);
    const rutas = this.buttons
      .flatMap(boton => [boton, ...(boton.children ?? [])])
      .map(boton => boton.route)
      .filter((ruta): ruta is string => !!ruta)
      .map(ruta => this.normalizar(ruta))
      .filter(ruta => actual === ruta || actual.startsWith(`${ruta}/`));
    return rutas.sort((a, b) => b.length - a.length)[0] ?? null;
  }

  private normalizar(ruta: string): string {
    const conBarra = ruta.startsWith('/') ? ruta : `/${ruta}`;
    return conBarra.length > 1 ? conBarra.replace(/\/+$/, '') : conBarra;
  }
}
