import { AsignaturaElegibleDTORespuesta } from './AsignaturaElegibleDTORespuesta';
import { SoportePermitidoDTORespuesta } from './SoportePermitidoDTORespuesta';

export interface FormularioCancelacionAsignaturaDTORespuesta {
  asignaturas: AsignaturaElegibleDTORespuesta[];
  soportes: SoportePermitidoDTORespuesta[];
}
