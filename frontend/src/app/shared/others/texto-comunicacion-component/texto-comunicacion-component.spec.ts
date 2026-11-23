import { TestBed } from '@angular/core/testing';
import { TextoComunicacionComponent } from './texto-comunicacion-component';

describe('TextoComunicacionComponent', () => {
  function crear(texto: string, sugerido = '') {
    TestBed.configureTestingModule({ imports: [TextoComunicacionComponent] });
    const fixture = TestBed.createComponent(TextoComunicacionComponent);
    fixture.componentRef.setInput('etiqueta', 'Mensaje');
    fixture.componentRef.setInput('texto', texto);
    fixture.componentRef.setInput('valores', { numero: '7' });
    fixture.componentRef.setInput('sugerido', sugerido);
    fixture.detectChanges();
    return fixture;
  }

  it('el cuadro crece con el texto para verse completo', () => {
    const fixture = crear(Array.from({ length: 15 }, (_, i) => `Linea ${i}`).join('\n'));
    const area = fixture.nativeElement.querySelector('textarea') as HTMLTextAreaElement;
    expect(area.rows).toBeGreaterThanOrEqual(16);
    expect(fixture.nativeElement.querySelector('[data-contador-texto]').textContent).toContain('/4000');
  });

  it('emite el texto editado y puede volver al sugerido', () => {
    const fixture = crear('Hola {numero}', 'Sugerido');
    const emitidos: string[] = [];
    fixture.componentInstance.textoChange.subscribe(valor => emitidos.push(valor));
    expect(fixture.nativeElement.querySelector('[data-vista-texto] strong').textContent).toBe('7');

    const area = fixture.nativeElement.querySelector('textarea') as HTMLTextAreaElement;
    area.value = 'Otro';
    area.dispatchEvent(new Event('input'));
    (fixture.nativeElement.querySelector('[data-restablecer]') as HTMLButtonElement).click();
    fixture.detectChanges();

    expect(emitidos).toEqual(['Otro', 'Sugerido']);
    expect(fixture.nativeElement.querySelector('[data-restablecer]')).toBeNull();
  });
});
