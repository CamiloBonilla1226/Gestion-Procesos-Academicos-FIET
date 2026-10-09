import { formatearFecha, formatearFechaHora, formatearTamanio } from './formato';

describe('formatearFechaHora', () => {
  it('formatea una fecha y hora del servidor', () => {
    expect(formatearFechaHora('2026-10-08T14:35:20')).toBe('08/10/2026 14:35');
  });

  it('respeta la medianoche sin convertir zona horaria', () => {
    expect(formatearFechaHora('2026-03-01T00:00:00')).toBe('01/03/2026 00:00');
  });

  it('respeta el fin de año sin pasar al año siguiente', () => {
    expect(formatearFechaHora('2026-12-31T23:59:59')).toBe('31/12/2026 23:59');
  });

  it('acepta horas sin segundos y con fracciones de segundo', () => {
    expect(formatearFechaHora('2027-01-01T07:05')).toBe('01/01/2027 07:05');
    expect(formatearFechaHora('2026-02-28T09:10:11.123456')).toBe('28/02/2026 09:10');
  });

  it('deja intacto un valor que no reconoce y vacio un nulo', () => {
    expect(formatearFechaHora('ayer')).toBe('ayer');
    expect(formatearFechaHora(null)).toBe('');
    expect(formatearFechaHora(undefined)).toBe('');
  });
});

describe('formatearFecha', () => {
  it('formatea AAAA-MM-DD como dd/MM/aaaa', () => {
    expect(formatearFecha('2026-12-31')).toBe('31/12/2026');
    expect(formatearFecha('')).toBe('');
  });
});

describe('formatearTamanio', () => {
  it('muestra KB por debajo de un mega', () => {
    expect(formatearTamanio(0)).toBe('0,0 KB');
    expect(formatearTamanio(512)).toBe('0,5 KB');
    expect(formatearTamanio(1024)).toBe('1,0 KB');
    expect(formatearTamanio(250880)).toBe('245,0 KB');
  });

  it('muestra MB desde un mega', () => {
    expect(formatearTamanio(1048576)).toBe('1,0 MB');
    expect(formatearTamanio(1572864)).toBe('1,5 MB');
    expect(formatearTamanio(5242880)).toBe('5,0 MB');
  });

  it('devuelve vacio para valores invalidos', () => {
    expect(formatearTamanio(null)).toBe('');
    expect(formatearTamanio(-1)).toBe('');
    expect(formatearTamanio(Number.NaN)).toBe('');
  });
});
