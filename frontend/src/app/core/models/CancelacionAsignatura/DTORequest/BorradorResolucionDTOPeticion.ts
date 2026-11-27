import { FilaResolucionDTO } from '../DTOResponse/BorradorResolucionDTORespuesta';
import { AjusteAsignaturaDTOPeticion } from './AjusteAsignaturaDTOPeticion';

export interface BorradoresResolucionDTOPeticion {
  ajustes: AjusteAsignaturaDTOPeticion[];
}

export interface BorradorResolucionDTOPeticion {
  asignaturaSolicitudUuid: string;
  numero: string;
  fecha: string;
  asunto: string;
  preambulo: string;
  considerandos: string;
  articuloPrimero: string;
  filas: FilaResolucionDTO[];
  articulosSiguientes: string;
  firmante: string;
  cargoFirmante: string;
  diligencia: string;
  proyecto: string;
  reviso: string;
}
