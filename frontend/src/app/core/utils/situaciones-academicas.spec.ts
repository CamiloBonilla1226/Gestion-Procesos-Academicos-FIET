import { AMBITOS_SITUACION, opcionesDeSituacion, textoSituacion } from './situaciones-academicas';
import { SituacionAcademicaAsignaturaDTORespuesta } from '../models/CatalogoAcademico/DTOResponse/SituacionAcademicaAsignaturaDTORespuesta';

const CATALOGO: SituacionAcademicaAsignaturaDTORespuesta[] = [
  { uuidSituacionAcademica: 'c1', codigo: 'C1', nombre: 'No vista', ambito: 'CANCELACION' },
  { uuidSituacionAcademica: 'c2', codigo: 'C2', nombre: 'Perdida', ambito: 'CANCELACION' },
  { uuidSituacionAcademica: 'r0', codigo: 'R0', nombre: 'No cursada', ambito: null },
  { uuidSituacionAcademica: 'r1', codigo: 'R1', nombre: 'Cursada por primera vez', ambito: 'MATRICULA' },
  { uuidSituacionAcademica: 'r1r', codigo: 'R1R', nombre: 'Recursada por primera vez', ambito: 'MATRICULA' },
  { uuidSituacionAcademica: 'r2', codigo: 'R2', nombre: 'Cursada por segunda vez', ambito: 'MATRICULA' },
  { uuidSituacionAcademica: 'r3', codigo: 'R3', nombre: 'Cursada por tercera vez', ambito: 'MATRICULA' }
];

describe('situaciones-academicas', () => {
  it('la situacion de la asignatura ofrece exactamente sus cuatro opciones, sin codigo ni la retirada', () => {
    expect(opcionesDeSituacion(CATALOGO, AMBITOS_SITUACION.MATRICULA).map(o => o.label)).toEqual([
      'Cursada por primera vez',
      'Recursada por primera vez',
      'Cursada por segunda vez',
      'Cursada por tercera vez'
    ]);
  });

  it('la situacion al cancelar ofrece solo No vista y Perdida', () => {
    expect(opcionesDeSituacion(CATALOGO, AMBITOS_SITUACION.CANCELACION)).toEqual([
      { label: 'No vista', value: 'c1' },
      { label: 'Perdida', value: 'c2' }
    ]);
    expect(opcionesDeSituacion(null, AMBITOS_SITUACION.CANCELACION)).toEqual([]);
  });

  it('el texto de una situacion es solo su nombre y sin dato es nulo', () => {
    expect(textoSituacion({ nombre: 'Perdida' })).toBe('Perdida');
    expect(textoSituacion(null)).toBeNull();
  });
});
