import {
  ahoraEnColombia,
  errorDeMensajeInicial,
  erroresDeProgramacion,
  fechaHoraLocal,
  programacionAPeticion,
  programacionDesde,
  textoFechaHora
} from './notificacion-resolucion';
import { NotificacionResolucionDTORespuesta } from '../models/CancelacionAsignatura/DTOResponse/NotificacionResolucionDTORespuesta';

describe('notificacion-resolucion', () => {
  const valida = { fecha: '2026-10-21T08:30', numero: '123-26', anio: '2026', decision: 'aprueba la' };

  it('la hora actual se toma en Colombia aunque el navegador este en otra zona', () => {
    expect(ahoraEnColombia(new Date('2026-10-21T03:15:00Z'))).toBe('2026-10-20T22:15');
    expect(ahoraEnColombia(new Date('2026-10-21T15:00:00Z'))).toBe('2026-10-21T10:00');
  });

  it('la fecha debe ser posterior a ahora en Colombia y los demas datos son obligatorios', () => {
    expect(erroresDeProgramacion(valida, '2026-10-21T08:29')).toEqual({});
    expect(erroresDeProgramacion(valida, '2026-10-21T08:30')['fecha']).toContain('posteriores');
    expect(erroresDeProgramacion({ ...valida, fecha: '' }, '2026-10-20T00:00')['fecha']).toContain('Elige');
    const vacia = erroresDeProgramacion({ fecha: '2099-01-01T00:00', numero: ' ', anio: '26', decision: '' }, '2026-10-20T00:00');
    expect(Object.keys(vacia).sort()).toEqual(['anio', 'decision', 'numero']);
    expect(erroresDeProgramacion({ ...valida, numero: '1'.repeat(21) }, '2026-01-01T00:00')['numero']).toContain('20');
    expect(erroresDeProgramacion({ ...valida, decision: 'd'.repeat(61) }, '2026-01-01T00:00')['decision']).toContain('60');
    expect(erroresDeProgramacion({ ...valida, anio: '1999' }, '2026-01-01T00:00')['anio']).toContain('2000');
  });

  it('el mensaje solo se valida si la casilla esta marcada', () => {
    const programacion = valida;
    expect(errorDeMensajeInicial({ enviarMensaje: false, mensaje: '', programacion })).toBe('');
    expect(errorDeMensajeInicial({ enviarMensaje: true, mensaje: '  ', programacion })).toContain('Escribe');
    expect(errorDeMensajeInicial({ enviarMensaje: true, mensaje: 'x'.repeat(2001), programacion })).toContain('2000');
    expect(errorDeMensajeInicial({ enviarMensaje: true, mensaje: 'x'.repeat(2000), programacion })).toBe('');
  });

  it('arma la peticion recortada y lee la programacion guardada', () => {
    expect(programacionAPeticion({ fecha: '2026-10-21T08:30', numero: ' 123-26 ', anio: ' 2026 ', decision: ' aprueba la ' })).toEqual({
      fechaProgramada: '2026-10-21T08:30', numeroResolucion: '123-26', anioResolucion: 2026, decision: 'aprueba la'
    });
    const guardada = { fechaProgramada: '2026-10-21T08:30:00', numeroResolucion: '7', anioResolucion: 2026, decision: 'rechaza la' } as NotificacionResolucionDTORespuesta;
    expect(programacionDesde(guardada)).toEqual({ fecha: '2026-10-21T08:30', numero: '7', anio: '2026', decision: 'rechaza la' });
    expect(fechaHoraLocal('basura')).toBe('');
  });

  it('muestra la fecha y hora en espanol con la hora tal como se programo', () => {
    const texto = textoFechaHora('2026-10-21T08:05:00');
    expect(texto).toContain('21 de octubre de 2026');
    expect(texto).toContain('miércoles');
    expect(texto.endsWith('08:05')).toBeTrue();
    expect(textoFechaHora(null)).toBe('');
  });
});
