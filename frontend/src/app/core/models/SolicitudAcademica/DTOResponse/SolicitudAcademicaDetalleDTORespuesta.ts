import { AnexoAcademicoDTORespuesta } from './AnexoAcademicoDTORespuesta';
import { EstudianteSolicitudDTORespuesta } from './EstudianteSolicitudDTORespuesta';

export interface SolicitudAcademicaDetalleDTORespuesta {
  uuidSolicitudAcademica: string;
  radicado: string;
  uuidTipoSolicitudAcademica: string;
  tipoSolicitud: string;
  fechaCreacion: string;
  etiqueta: string;
  etapaCodigo: string;
  estudiante: EstudianteSolicitudDTORespuesta;
  anexos: AnexoAcademicoDTORespuesta[];
  tieneResolucion: boolean;
  puedeDescargarResolucion: boolean;
  accionesDisponibles: string[];
}
