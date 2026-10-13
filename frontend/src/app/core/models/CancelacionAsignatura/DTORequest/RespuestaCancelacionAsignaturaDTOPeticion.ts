import { AjusteAsignaturaDTOPeticion } from './AjusteAsignaturaDTOPeticion';

export interface RespuestaCancelacionAsignaturaDTOPeticion {
  observacion?: string | null;
  ajustes?: AjusteAsignaturaDTOPeticion[] | null;
}
