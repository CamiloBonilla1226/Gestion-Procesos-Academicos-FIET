import { TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { GenericDialogFormComponent } from './generic-dialog-form-component';

describe('GenericDialogFormComponent cancelar y Escape', () => {
  function crear(entradas: Record<string, unknown>) {
    TestBed.configureTestingModule({ imports: [GenericDialogFormComponent], providers: [provideNoopAnimations()] });
    const fixture = TestBed.createComponent(GenericDialogFormComponent);
    fixture.componentRef.setInput('visible', true);
    fixture.componentRef.setInput('title', 'Aprobar');
    for (const [nombre, valor] of Object.entries(entradas)) fixture.componentRef.setInput(nombre, valor);
    fixture.detectChanges();
    const cerrados: boolean[] = [];
    fixture.componentInstance.visibleChange.subscribe(valor => cerrados.push(valor));
    return { fixture, cerrados };
  }

  function escape(): void {
    const contenido = document.querySelector('.dialog-content') as HTMLElement;
    contenido.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }));
  }

  it('sin las entradas nuevas no hay Cancelar y Escape no cierra', () => {
    const { cerrados } = crear({});
    expect(document.querySelector('[data-boton-cancelar]')).toBeNull();
    escape();
    expect(cerrados).toEqual([]);
  });

  it('con textoCancelar y cerrarConEscape cierra con Cancelar, con Escape y con la equis', () => {
    const { cerrados } = crear({ textoCancelar: 'Cancelar', cerrarConEscape: true });
    (document.querySelector('[data-boton-cancelar] button') as HTMLButtonElement).click();
    escape();
    const equis = document.querySelector('.close-btn') as HTMLButtonElement;
    expect(equis.getAttribute('aria-label')).toBe('Cerrar');
    equis.click();
    expect(cerrados).toEqual([false, false, false]);
  });
});
