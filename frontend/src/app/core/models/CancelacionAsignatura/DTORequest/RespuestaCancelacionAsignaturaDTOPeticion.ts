import { AjusteAsignaturaDTOPeticion } from './AjusteAsignaturaDTOPeticion';
import { ProgramacionResolucionDTOPeticion } from './ProgramacionResolucionDTOPeticion';

export interface RespuestaCancelacionAsignaturaDTOPeticion {
  observacion?: string | null;
  ajustes?: AjusteAsignaturaDTOPeticion[] | null;
  enviarMensaje?: boolean;
  mensaje?: string | null;
  programacion?: ProgramacionResolucionDTOPeticion;
}
