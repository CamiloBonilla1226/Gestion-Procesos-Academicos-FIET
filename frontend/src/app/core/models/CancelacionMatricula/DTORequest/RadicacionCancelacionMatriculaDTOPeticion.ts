import { AnexoRadicacionDTOPeticion } from '../../SolicitudAcademica/DTORequest/AnexoRadicacionDTOPeticion';

export interface RadicacionCancelacionMatriculaDTOPeticion {
  motivo: string;
  anexos: AnexoRadicacionDTOPeticion[];
  soportes: File[];
}
