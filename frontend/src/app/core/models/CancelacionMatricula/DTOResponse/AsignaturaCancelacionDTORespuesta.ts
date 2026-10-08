import { SituacionAsignaturaDTORespuesta } from './SituacionAsignaturaDTORespuesta';

export interface AsignaturaCancelacionDTORespuesta {
  uuidAsignaturaSolicitud: string;
  codigoAsignatura: string;
  nombreAsignatura: string;
  numeroFaltas: number | null;
  nota: number | null;
  situacionMatricula: SituacionAsignaturaDTORespuesta | null;
  situacionCancelar: SituacionAsignaturaDTORespuesta | null;
}
