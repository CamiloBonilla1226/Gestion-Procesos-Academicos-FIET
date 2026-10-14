import { AnexoRequeridoDTORespuesta } from './AnexoRequeridoDTORespuesta';
import { AsignaturaFormularioDTORespuesta } from './AsignaturaFormularioDTORespuesta';

export interface FormularioCancelacionMatriculaDTORespuesta {
  codigoEstudiantil?: string | null;
  anexosRequeridos: AnexoRequeridoDTORespuesta[];
  asignaturas: AsignaturaFormularioDTORespuesta[];
}
