import { AsignaturaSupletorioDTORespuesta } from './AsignaturaSupletorioDTORespuesta';
import { CruceSupletorioDTORespuesta } from './CruceSupletorioDTORespuesta';
import { SolicitudAcademicaDetalleDTORespuesta } from '../../SolicitudAcademica/DTOResponse/SolicitudAcademicaDetalleDTORespuesta';

export interface ExamenSupletorioDetalleDTORespuesta {
  solicitud: SolicitudAcademicaDetalleDTORespuesta;
  asignatura: AsignaturaSupletorioDTORespuesta;
  tipoCausa: string;
  fechaExamenNoPresentado: string;
  cruce: CruceSupletorioDTORespuesta | null;
  fechaAcordadaExamen: string | null;
}
