import { TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { GenericDialogFormComponent } from './generic-dialog-form-component';

describe('GenericDialogFormComponent texto del boton', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [GenericDialogFormComponent], providers: [provideNoopAnimations()] });
  });

  function boton(texto?: string): HTMLButtonElement {
    const fixture = TestBed.createComponent(GenericDialogFormComponent);
    fixture.componentRef.setInput('visible', true);
    if (texto !== undefined) fixture.componentRef.setInput('textoBoton', texto);
    fixture.detectChanges();
    return document.querySelector('[data-boton-dialogo] button') as HTMLButtonElement;
  }

  it('sin el input sigue mostrando Guardar con el ancho de siempre', () => {
    const elemento = boton();
    expect(elemento.textContent?.trim()).toBe('Guardar');
    expect(elemento.style.width).toBe('70px');
  });

  it('con el input muestra el texto de la accion y ajusta el ancho', () => {
    const elemento = boton('Remitir al Decano');
    expect(elemento.textContent?.trim()).toBe('Remitir al Decano');
    expect(elemento.style.width).toBe('auto');
  });
});
