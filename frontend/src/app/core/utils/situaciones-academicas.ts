import { SituacionAcademicaAsignaturaDTORespuesta } from '../models/CatalogoAcademico/DTOResponse/SituacionAcademicaAsignaturaDTORespuesta';

export const AMBITOS_SITUACION = {
  MATRICULA: 'MATRICULA',
  CANCELACION: 'CANCELACION'
} as const;

export type AmbitoSituacion = (typeof AMBITOS_SITUACION)[keyof typeof AMBITOS_SITUACION];

export interface OpcionSituacion {
  label: string;
  value: string;
}

export function opcionesDeSituacion(
  situaciones: SituacionAcademicaAsignaturaDTORespuesta[] | null | undefined,
  ambito: AmbitoSituacion
): OpcionSituacion[] {
  return (situaciones ?? []).filter(s => s.ambito === ambito).map(s => ({ label: s.nombre, value: s.uuidSituacionAcademica }));
}

export function textoSituacion(situacion: { nombre: string } | null | undefined): string | null {
  return situacion?.nombre ?? null;
}
