import { AfterViewInit, Directive, ElementRef, Input, OnChanges, Renderer2, booleanAttribute } from '@angular/core';

@Directive({
  selector: '[appTarjetaPlegable]',
  host: {
    '[class.plegable]': 'activa',
    '[class.plegada]': 'activa && plegada',
    '(click)': 'alHacerClic($event)',
    '(keydown)': 'alPresionarTecla($event)'
  }
})
export class TarjetaPlegableDirective implements OnChanges, AfterViewInit {
  @Input({ alias: 'appTarjetaPlegable', transform: booleanAttribute }) activa = true;
  @Input({ transform: booleanAttribute }) plegadaAlInicio = false;

  plegada = false;
  private estadoInicialAplicado = false;

  constructor(private host: ElementRef<HTMLElement>, private renderer: Renderer2) {}

  ngOnChanges(): void {
    if (this.activa && !this.estadoInicialAplicado) {
      this.plegada = this.plegadaAlInicio;
      this.estadoInicialAplicado = true;
    }
    this.marcarEncabezado();
  }

  ngAfterViewInit(): void {
    this.marcarEncabezado();
  }

  alternar(): void {
    if (!this.activa) return;
    this.plegada = !this.plegada;
    this.marcarEncabezado();
  }

  alHacerClic(evento: Event): void {
    const encabezado = this.encabezado();
    if (encabezado && evento.target instanceof Node && encabezado.contains(evento.target)) this.alternar();
  }

  alPresionarTecla(evento: KeyboardEvent): void {
    if (evento.key !== 'Enter' && evento.key !== ' ') return;
    if (!this.activa || evento.target !== this.encabezado()) return;
    evento.preventDefault();
    this.alternar();
  }

  private encabezado(): HTMLElement | null {
    const hijos = Array.from(this.host.nativeElement.children ?? []);
    return (hijos.find(hijo => hijo.classList?.contains('tarjeta-head')) as HTMLElement | undefined) ?? null;
  }

  private marcarEncabezado(): void {
    const encabezado = this.encabezado();
    if (!encabezado) return;
    if (!this.activa) {
      for (const atributo of ['role', 'tabindex', 'aria-expanded', 'title']) this.renderer.removeAttribute(encabezado, atributo);
      return;
    }
    this.renderer.setAttribute(encabezado, 'role', 'button');
    this.renderer.setAttribute(encabezado, 'tabindex', '0');
    this.renderer.setAttribute(encabezado, 'aria-expanded', String(!this.plegada));
    this.renderer.setAttribute(encabezado, 'title', this.plegada ? 'Mostrar sección' : 'Ocultar sección');
  }
}
