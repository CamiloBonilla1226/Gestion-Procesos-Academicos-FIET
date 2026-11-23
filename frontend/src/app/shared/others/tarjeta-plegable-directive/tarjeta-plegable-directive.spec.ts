import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TarjetaPlegableDirective } from './tarjeta-plegable-directive';

@Component({
  imports: [TarjetaPlegableDirective],
  template: `
    <section class="tarjeta" data-a [appTarjetaPlegable]="activa" plegadaAlInicio>
      <header class="tarjeta-head"><h3>Motivo</h3></header>
      <div class="tarjeta-body">texto</div>
    </section>
    <section class="tarjeta" data-b appTarjetaPlegable>
      <header class="tarjeta-head"><h3>Datos</h3></header>
      <div class="tarjeta-body"><button type="button">dentro</button></div>
    </section>
  `
})
class AnfitrionComponent {
  activa = true;
}

describe('TarjetaPlegableDirective', () => {
  let fixture: ComponentFixture<AnfitrionComponent>;

  const seccion = (dato: string): HTMLElement => fixture.nativeElement.querySelector(`[${dato}]`);
  const encabezado = (dato: string): HTMLElement => seccion(dato).querySelector('.tarjeta-head') as HTMLElement;

  beforeEach(() => {
    fixture = TestBed.createComponent(AnfitrionComponent);
    fixture.detectChanges();
  });

  it('la tarjeta marcada llega plegada y la otra abierta', () => {
    expect(seccion('data-a').classList).toContain('plegada');
    expect(encabezado('data-a').getAttribute('aria-expanded')).toBe('false');
    expect(seccion('data-b').classList).toContain('plegable');
    expect(seccion('data-b').classList).not.toContain('plegada');
    expect(encabezado('data-b').getAttribute('role')).toBe('button');
  });

  it('un clic en el titulo alterna la tarjeta y un clic en el cuerpo no', () => {
    encabezado('data-a').querySelector('h3')!.click();
    fixture.detectChanges();
    expect(seccion('data-a').classList).not.toContain('plegada');
    expect(encabezado('data-a').getAttribute('aria-expanded')).toBe('true');

    (seccion('data-b').querySelector('button') as HTMLButtonElement).click();
    fixture.detectChanges();
    expect(seccion('data-b').classList).not.toContain('plegada');

    encabezado('data-b').click();
    fixture.detectChanges();
    expect(seccion('data-b').classList).toContain('plegada');
  });

  it('Enter sobre el titulo alterna la tarjeta', () => {
    encabezado('data-b').dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', bubbles: true }));
    fixture.detectChanges();
    expect(seccion('data-b').classList).toContain('plegada');
  });

  it('desactivada no se pliega ni marca el titulo como boton', () => {
    fixture.componentInstance.activa = false;
    fixture.detectChanges();
    expect(seccion('data-a').classList).not.toContain('plegable');
    expect(seccion('data-a').classList).not.toContain('plegada');
    expect(encabezado('data-a').hasAttribute('role')).toBeFalse();

    encabezado('data-a').click();
    fixture.detectChanges();
    expect(seccion('data-a').classList).not.toContain('plegada');
  });
});
