import { AnexoRequeridoDTORespuesta } from './AnexoRequeridoDTORespuesta';
import { AsignaturaFormularioDTORespuesta } from './AsignaturaFormularioDTORespuesta';

export interface FormularioCancelacionMatriculaDTORespuesta {
  anexosRequeridos: AnexoRequeridoDTORespuesta[];
  asignaturas: AsignaturaFormularioDTORespuesta[];
}
