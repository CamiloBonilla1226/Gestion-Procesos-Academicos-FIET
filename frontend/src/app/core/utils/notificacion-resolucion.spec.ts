import {
  ahoraEnColombia,
  errorDeMensajeInicial,
  errorDeTextoComunicacion,
  erroresDeProgramacion,
  erroresDeTextos,
  fechaCarta,
  fechaHoraLocal,
  filasDeTexto,
  parrafosConMarcas,
  programacionAPeticion,
  programacionDesde,
  textoFechaHora,
  valoresComunicacionEstudiante
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

  it('el mensaje inmediato siempre es obligatorio y admite 2000 caracteres', () => {
    const base = { textoComunicacion: 'a', textoDarca: 'b', programacion: valida };
    expect(errorDeMensajeInicial({ ...base, mensaje: '' })).toContain('siempre se envía');
    expect(errorDeMensajeInicial({ ...base, mensaje: '  ' })).toContain('Escribe');
    expect(errorDeMensajeInicial({ ...base, mensaje: 'x'.repeat(2001) })).toContain('2000');
    expect(errorDeMensajeInicial({ ...base, mensaje: 'x'.repeat(2000) })).toBe('');
  });

  it('los textos para el estudiante y DARCA son obligatorios y admiten 4000 caracteres', () => {
    expect(errorDeTextoComunicacion(' ', 'DARCA')).toContain('DARCA');
    expect(errorDeTextoComunicacion('x'.repeat(4001), 'DARCA')).toContain('4000');
    expect(errorDeTextoComunicacion('x'.repeat(4000), 'DARCA')).toBe('');
    const errores = erroresDeTextos({ mensaje: 'm', textoComunicacion: '', textoDarca: '', programacion: valida });
    expect(Object.keys(errores).sort()).toEqual(['textoComunicacion', 'textoDarca']);
  });

  it('la vista previa reemplaza las marcas conocidas en negrilla y deja las desconocidas', () => {
    const valores = valoresComunicacionEstudiante({ ...valida, numero: ' ' }, 'Ana Muñoz');
    const parrafos = parrafosConMarcas('Hola {nombre}.\r\n\r\nResolución {numero} de {anio}: se {decision} {otra}', valores);
    expect(parrafos.length).toBe(2);
    expect(parrafos[0]).toEqual([{ texto: 'Hola ', resaltado: false }, { texto: 'Ana Muñoz', resaltado: true }, { texto: '.', resaltado: false }]);
    expect(parrafos[1].map(f => f.texto).join('')).toBe('Resolución ___ de 2026: se aprueba la {otra}');
    expect(parrafos[1].find(f => f.texto === '{otra}')?.resaltado).toBeFalse();
    expect(parrafosConMarcas('{fecha}', { fecha: 'hoy' }, false)).toEqual([[{ texto: 'hoy', resaltado: false }]]);
  });

  it('la fecha de la carta sale en espanol y el cuadro crece con el texto', () => {
    expect(fechaCarta('2026-10-21T08:30')).toBe('21 de octubre de 2026');
    expect(fechaCarta('')).toBe('___');
    expect(filasDeTexto('')).toBe(6);
    expect(filasDeTexto('a\n'.repeat(20))).toBeGreaterThan(20);
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
