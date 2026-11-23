import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DecisionDecanoComponent } from './decision-decano-component';

describe('DecisionDecanoComponent', () => {
  let fixture: ComponentFixture<DecisionDecanoComponent>;

  function opcion(valor: string): HTMLInputElement {
    return fixture.nativeElement.querySelector(`[data-opcion="${valor}"]`);
  }

  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [DecisionDecanoComponent] });
    fixture = TestBed.createComponent(DecisionDecanoComponent);
    fixture.componentRef.setInput('textoAprobar', 'Aprobar la cancelación');
    fixture.componentRef.setInput('textoRechazar', 'Rechazar la cancelación');
    fixture.detectChanges();
  });

  it('no trae ninguna opcion marcada y avisa que hay que elegir', () => {
    expect(opcion('aprobar').checked).toBeFalse();
    expect(opcion('rechazar').checked).toBeFalse();
    expect(fixture.nativeElement.textContent).toContain('Aprobar la cancelación');
    expect(fixture.nativeElement.textContent).toContain('Rechazar la cancelación');
    expect(fixture.nativeElement.querySelector('[data-ayuda-decision]')?.textContent).toContain('Enviar decisión');
  });

  it('al elegir emite la decision y con una decision marcada ya no muestra la ayuda', () => {
    const emitidas: string[] = [];
    fixture.componentInstance.decisionChange.subscribe(d => emitidas.push(d));
    opcion('rechazar').click();
    opcion('aprobar').click();
    expect(emitidas).toEqual(['rechazar', 'aprobar']);

    fixture.componentRef.setInput('decision', 'rechazar');
    fixture.detectChanges();
    expect(opcion('rechazar').checked).toBeTrue();
    expect(fixture.nativeElement.querySelector('[data-ayuda-decision]')).toBeNull();
  });

  it('deshabilitado no deja elegir', () => {
    const emitidas: string[] = [];
    fixture.componentInstance.decisionChange.subscribe(d => emitidas.push(d));
    fixture.componentRef.setInput('deshabilitado', true);
    fixture.detectChanges();
    expect(opcion('aprobar').disabled).toBeTrue();
    fixture.componentInstance.elegir('aprobar');
    expect(emitidas).toEqual([]);
  });
});
