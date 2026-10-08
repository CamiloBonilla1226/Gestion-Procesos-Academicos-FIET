import { AsignaturaCancelacionDTORespuesta } from './AsignaturaCancelacionDTORespuesta';
import { SolicitudAcademicaDetalleDTORespuesta } from '../../SolicitudAcademica/DTOResponse/SolicitudAcademicaDetalleDTORespuesta';

export interface CancelacionMatriculaDetalleDTORespuesta {
  solicitud: SolicitudAcademicaDetalleDTORespuesta;
  motivoCancelacion: string;
  asignaturas: AsignaturaCancelacionDTORespuesta[];
}
