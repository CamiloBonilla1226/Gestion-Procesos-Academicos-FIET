import { AnexoRadicacionDTOPeticion } from '../../SolicitudAcademica/DTORequest/AnexoRadicacionDTOPeticion';

export interface RadicacionExamenSupletorioDTOPeticion {
  asignaturaMatriculada: string;
  fechaExamenNoPresentado: string;
  tipoCausa: string;
  asignaturaCruzada?: string | null;
  fechaExamenCruzada?: string | null;
  horaExamenCruzada?: string | null;
  anexos: AnexoRadicacionDTOPeticion[];
}
