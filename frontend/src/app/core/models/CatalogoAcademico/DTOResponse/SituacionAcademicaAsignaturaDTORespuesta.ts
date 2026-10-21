export interface SituacionAcademicaAsignaturaDTORespuesta {
  uuidSituacionAcademica: string;
  codigo: string;
  nombre: string;
  ambito: 'MATRICULA' | 'CANCELACION' | null;
}
