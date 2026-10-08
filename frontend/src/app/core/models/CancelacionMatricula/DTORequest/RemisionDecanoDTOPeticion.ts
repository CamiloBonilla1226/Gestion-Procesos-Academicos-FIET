import { EvaluacionAsignaturaDTOPeticion } from './EvaluacionAsignaturaDTOPeticion';

export interface RemisionDecanoDTOPeticion {
  observacion?: string | null;
  evaluaciones: EvaluacionAsignaturaDTOPeticion[];
}
