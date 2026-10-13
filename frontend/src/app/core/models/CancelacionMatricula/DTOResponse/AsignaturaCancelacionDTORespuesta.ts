import { SituacionAsignaturaDTORespuesta } from './SituacionAsignaturaDTORespuesta';

export interface AsignaturaCancelacionDTORespuesta {
  uuidAsignaturaSolicitud: string;
  codigoAsignatura: string;
  nombreAsignatura: string;
  numeroFaltas: string | null;
  nota: string | null;
  situacionMatricula: SituacionAsignaturaDTORespuesta | null;
  situacionCancelar: SituacionAsignaturaDTORespuesta | null;
}
