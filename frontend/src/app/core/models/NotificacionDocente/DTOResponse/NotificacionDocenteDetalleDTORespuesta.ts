import { NotificacionDocenteResumenDTORespuesta } from './NotificacionDocenteResumenDTORespuesta';
import { AnexoNotificacionDTORespuesta } from './AnexoNotificacionDTORespuesta';
import { AsignaturaSupletorioDTORespuesta } from '../../ExamenSupletorio/DTOResponse/AsignaturaSupletorioDTORespuesta';
import { CruceSupletorioDTORespuesta } from '../../ExamenSupletorio/DTOResponse/CruceSupletorioDTORespuesta';

export interface NotificacionDocenteDetalleDTORespuesta {
  notificacion: NotificacionDocenteResumenDTORespuesta;
  tipoSolicitud: string | null;
  fechaRadicacion: string | null;
  nombresEstudiante: string | null;
  apellidosEstudiante: string | null;
  codigoEstudiantil: string | null;
  programaAcademico: string | null;
  asignatura: AsignaturaSupletorioDTORespuesta | null;
  tipoCausa: string | null;
  fechaExamenNoPresentado: string | null;
  cruce: CruceSupletorioDTORespuesta | null;
  anexos: AnexoNotificacionDTORespuesta[];
}
