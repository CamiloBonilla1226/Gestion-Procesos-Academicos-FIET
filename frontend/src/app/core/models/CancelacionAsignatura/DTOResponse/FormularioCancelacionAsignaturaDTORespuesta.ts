import { AsignaturaElegibleDTORespuesta } from './AsignaturaElegibleDTORespuesta';
import { SoportePermitidoDTORespuesta } from './SoportePermitidoDTORespuesta';

export interface FormularioCancelacionAsignaturaDTORespuesta {
  codigoEstudiantil?: string | null;
  asignaturas: AsignaturaElegibleDTORespuesta[];
  soportes: SoportePermitidoDTORespuesta[];
}
