import { AnexoSupletorioDTORespuesta } from './AnexoSupletorioDTORespuesta';
import { AsignaturaSupletorioDTORespuesta } from './AsignaturaSupletorioDTORespuesta';

export interface FormularioExamenSupletorioDTORespuesta {
  asignaturas: AsignaturaSupletorioDTORespuesta[];
  causas: string[];
  anexos: AnexoSupletorioDTORespuesta[];
  plazoDiasHabiles: number;
}
