import { AsignaturaSolicitadaDTORespuesta } from './AsignaturaSolicitadaDTORespuesta';
import { SolicitudAcademicaDetalleDTORespuesta } from '../../SolicitudAcademica/DTOResponse/SolicitudAcademicaDetalleDTORespuesta';

export interface CancelacionAsignaturaDetalleDTORespuesta {
  solicitud: SolicitudAcademicaDetalleDTORespuesta;
  motivoCancelacion: string;
  asignaturas: AsignaturaSolicitadaDTORespuesta[];
}
