import { TestBed } from '@angular/core/testing';
import { StepperFormularioComponent } from './stepper-formulario-component';

describe('StepperFormularioComponent', () => {
  function estados(pasos: { titulo: string; completo: boolean }[]): (string | null)[] {
    const fixture = TestBed.createComponent(StepperFormularioComponent);
    fixture.componentRef.setInput('pasos', pasos);
    fixture.detectChanges();
    return Array.from(fixture.nativeElement.querySelectorAll('li')).map((li: any) => li.getAttribute('data-paso'));
  }

  it('marca hechos en verde, el primero pendiente como activo y el resto pendientes', () => {
    expect(estados([
      { titulo: 'Datos', completo: true },
      { titulo: 'Motivo', completo: false },
      { titulo: 'Anexos', completo: false }
    ])).toEqual(['hecho', 'activo', 'pendiente']);
  });

  it('con todo completo no deja ningun paso activo ni en rojo', () => {
    expect(estados([{ titulo: 'Datos', completo: true }, { titulo: 'Motivo', completo: true }])).toEqual(['hecho', 'hecho']);
  });

  it('numera los pasos y marca el actual para lectores de pantalla', () => {
    const fixture = TestBed.createComponent(StepperFormularioComponent);
    fixture.componentRef.setInput('pasos', [{ titulo: 'Datos', completo: false }, { titulo: 'Motivo', completo: false }]);
    fixture.detectChanges();
    const items = fixture.nativeElement.querySelectorAll('li');
    expect(items[0].getAttribute('aria-current')).toBe('step');
    expect(items[1].querySelector('.numero').textContent.trim()).toBe('2');
    expect(items[1].querySelector('.titulo').textContent.trim()).toBe('Motivo');
  });
});
