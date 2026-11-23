import { accionDeDecision, accionDeDecisiones, decisionCompleta, esAccionDecisionDecano } from './decision-decano';

describe('decision-decano', () => {
  it('sin decision no hay accion; aprobar y rechazar dan su accion', () => {
    expect(accionDeDecision(null)).toBeNull();
    expect(accionDeDecision(undefined)).toBeNull();
    expect(accionDeDecision('aprobar')).toBe('APROBAR_DECANO');
    expect(accionDeDecision('rechazar')).toBe('RECHAZAR_DECANO');
  });

  it('la decision por asignatura esta completa solo si todas tienen valor', () => {
    expect(decisionCompleta([])).toBeFalse();
    expect(decisionCompleta([true, null])).toBeFalse();
    expect(decisionCompleta([true, undefined])).toBeFalse();
    expect(decisionCompleta([true, false])).toBeTrue();
  });

  it('con alguna aprobada aprueba, con todas rechazadas rechaza y sin completar no hay accion', () => {
    expect(accionDeDecisiones([true, null])).toBeNull();
    expect(accionDeDecisiones([false, true])).toBe('APROBAR_DECANO');
    expect(accionDeDecisiones([false, false])).toBe('RECHAZAR_DECANO');
  });

  it('reconoce solo las acciones de decision del Decano', () => {
    expect(esAccionDecisionDecano('APROBAR_DECANO')).toBeTrue();
    expect(esAccionDecisionDecano('RECHAZAR_DECANO')).toBeTrue();
    expect(esAccionDecisionDecano('REMITIR_DECANO')).toBeFalse();
    expect(esAccionDecisionDecano(null)).toBeFalse();
  });
});
