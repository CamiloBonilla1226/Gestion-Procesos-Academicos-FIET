import { ACCIONES_ACADEMICAS, AccionAcademica } from '../constantes/procesos-academicos';

export type DecisionDecano = 'aprobar' | 'rechazar';

export const ETIQUETA_ENVIAR_DECISION = 'Enviar decisión';

export const ACCIONES_DECISION_DECANO: AccionAcademica[] = [ACCIONES_ACADEMICAS.APROBAR_DECANO, ACCIONES_ACADEMICAS.RECHAZAR_DECANO];

export function esAccionDecisionDecano(accion: AccionAcademica | null | undefined): boolean {
  return !!accion && ACCIONES_DECISION_DECANO.includes(accion);
}

export function accionDeDecision(decision: DecisionDecano | null | undefined): AccionAcademica | null {
  if (decision === 'aprobar') return ACCIONES_ACADEMICAS.APROBAR_DECANO;
  if (decision === 'rechazar') return ACCIONES_ACADEMICAS.RECHAZAR_DECANO;
  return null;
}

export function decisionCompleta(decisiones: (boolean | null | undefined)[]): boolean {
  return decisiones.length > 0 && decisiones.every(decision => decision === true || decision === false);
}

export function accionDeDecisiones(decisiones: (boolean | null | undefined)[]): AccionAcademica | null {
  if (!decisionCompleta(decisiones)) return null;
  return decisiones.some(decision => decision === true) ? ACCIONES_ACADEMICAS.APROBAR_DECANO : ACCIONES_ACADEMICAS.RECHAZAR_DECANO;
}
