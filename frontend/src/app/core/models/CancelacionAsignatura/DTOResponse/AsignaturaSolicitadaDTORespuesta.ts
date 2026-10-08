import { SituacionAsignaturaDTORespuesta } from '../../CancelacionMatricula/DTOResponse/SituacionAsignaturaDTORespuesta';

export interface AsignaturaSolicitadaDTORespuesta {
  uuidAsignaturaSolicitud: string;
  codigoAsignatura: string;
  nombreAsignatura: string;
  numeroFaltas: number | null;
  nota: number | null;
  situacionMatricula: SituacionAsignaturaDTORespuesta | null;
  cumpleCondiciones: boolean | null;
  observacionEvaluacion: string | null;
  aprobadaPorDecano: boolean | null;
  observacionDecision: string | null;
  situacionCancelar: SituacionAsignaturaDTORespuesta | null;
}
