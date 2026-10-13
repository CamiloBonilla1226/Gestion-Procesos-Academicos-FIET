import { TestBed } from '@angular/core/testing';
import { SimpleButtonComponent } from './simple-button-component';

describe('SimpleButtonComponent contorno', () => {
  function boton(contorno: boolean): HTMLButtonElement {
    const fixture = TestBed.createComponent(SimpleButtonComponent);
    fixture.componentRef.setInput('label', 'Rechazar');
    fixture.componentRef.setInput('color', '#D0463B');
    if (contorno) fixture.componentRef.setInput('contorno', true);
    fixture.detectChanges();
    return fixture.nativeElement.querySelector('button');
  }

  it('por defecto el color va de fondo como siempre', () => {
    const solido = boton(false);
    expect(solido.style.backgroundColor).toBe('rgb(208, 70, 59)');
    expect(solido.classList).not.toContain('contorno');
  });

  it('con contorno el fondo es blanco y el color va en borde y texto', () => {
    const contorno = boton(true);
    expect(contorno.classList).toContain('contorno');
    expect(contorno.style.backgroundColor).toBe('rgb(255, 255, 255)');
    expect(contorno.style.color).toBe('rgb(208, 70, 59)');
    expect(contorno.style.borderColor).toBe('rgb(208, 70, 59)');
  });
});
