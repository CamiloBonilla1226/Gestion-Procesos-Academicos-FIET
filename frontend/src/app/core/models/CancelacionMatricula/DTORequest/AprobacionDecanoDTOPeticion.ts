import { SituacionCancelarDTOPeticion } from './SituacionCancelarDTOPeticion';

export interface AprobacionDecanoDTOPeticion {
  situaciones: SituacionCancelarDTOPeticion[];
  observacion?: string | null;
}
