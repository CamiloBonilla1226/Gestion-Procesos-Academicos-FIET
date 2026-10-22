import { AjusteAsignaturaDTOPeticion } from './AjusteAsignaturaDTOPeticion';
import { ProgramacionResolucionDTOPeticion } from './ProgramacionResolucionDTOPeticion';

export interface RespuestaCancelacionAsignaturaDTOPeticion {
  observacion?: string | null;
  ajustes?: AjusteAsignaturaDTOPeticion[] | null;
  enviarMensaje?: boolean;
  mensaje?: string | null;
  textoComunicacion?: string | null;
  textoDarca?: string | null;
  programacion?: ProgramacionResolucionDTOPeticion;
}
