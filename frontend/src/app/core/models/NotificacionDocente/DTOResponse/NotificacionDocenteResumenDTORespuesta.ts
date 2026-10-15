export interface NotificacionDocenteResumenDTORespuesta {
  uuidNotificacion: string;
  titulo: string;
  uuidSolicitudAcademica: string;
  radicado: string | null;
  estudiante: string | null;
  asignatura: string | null;
  resultado: string | null;
  fechaCreacion: string;
  fechaLectura: string | null;
  leida: boolean;
}
