import { DecisionAsignaturaDTOPeticion } from './DecisionAsignaturaDTOPeticion';

export interface AprobacionCancelacionAsignaturaDTOPeticion {
  decisiones: DecisionAsignaturaDTOPeticion[];
  observacion?: string | null;
}
