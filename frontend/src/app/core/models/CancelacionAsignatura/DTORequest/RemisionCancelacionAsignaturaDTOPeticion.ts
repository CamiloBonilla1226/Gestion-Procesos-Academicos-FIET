import { EvaluacionCancelacionAsignaturaDTOPeticion } from './EvaluacionCancelacionAsignaturaDTOPeticion';

export interface RemisionCancelacionAsignaturaDTOPeticion {
  observacion?: string | null;
  evaluaciones: EvaluacionCancelacionAsignaturaDTOPeticion[];
}
