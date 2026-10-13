import { SituacionAsignaturaDTORespuesta } from '../../CancelacionMatricula/DTOResponse/SituacionAsignaturaDTORespuesta';

export interface AsignaturaSolicitadaDTORespuesta {
  uuidAsignaturaSolicitud: string;
  codigoAsignatura: string;
  nombreAsignatura: string;
  numeroFaltas: string | null;
  nota: string | null;
  situacionMatricula: SituacionAsignaturaDTORespuesta | null;
  cumpleCondiciones: boolean | null;
  observacionEvaluacion: string | null;
  aprobadaPorDecano: boolean | null;
  observacionDecision: string | null;
  decisionCambiadaPorDecano?: boolean | null;
  situacionCancelar: SituacionAsignaturaDTORespuesta | null;
}
