import { ProgramaAcademicoDTORespuesta } from '../models/CatalogoAcademico/DTOResponse/ProgramaAcademicoDTORespuesta';
import { AsignaturaDTORespuesta } from '../models/Asignatura/DTOResponse/AsignaturaDTORespuesta';

export interface OpcionSelect {
  label: string;
  value: string;
}

export function opcionesDePrograma(programas: ProgramaAcademicoDTORespuesta[]): OpcionSelect[] {
  return programas.map(p => ({ label: `${p.nombre} (${p.sigla})`, value: p.uuidProgramaAcademico }));
}

export function nombresDePrograma(programas: ProgramaAcademicoDTORespuesta[]): string[] {
  return programas.map(p => p.nombre);
}

export function opcionesDeAsignatura(asignaturas: AsignaturaDTORespuesta[]): OpcionSelect[] {
  return asignaturas.map(a => ({ label: `${a.codigoAsignatura} - ${a.nombreAsignatura}`, value: a.codigoAsignatura }));
}
