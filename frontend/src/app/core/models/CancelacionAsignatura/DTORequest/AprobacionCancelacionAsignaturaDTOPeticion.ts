import { DecisionAsignaturaDTOPeticion } from './DecisionAsignaturaDTOPeticion';

export interface AprobacionCancelacionAsignaturaDTOPeticion {
  aceptarEvaluacion?: boolean | null;
  decisiones?: DecisionAsignaturaDTOPeticion[] | null;
  observacion?: string | null;
}
