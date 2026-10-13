import { SolicitudAcademicaResumenDTORespuesta } from '../models/SolicitudAcademica/DTOResponse/SolicitudAcademicaResumenDTORespuesta';
import {
  TODOS_LOS_TIPOS,
  etiquetasDisponibles,
  filtrarSolicitudes,
  ordenarSolicitudes,
  paginar,
  totalPaginas
} from './bandeja-solicitudes';

function solicitud(radicado: string, tipo: string, nombre: string, codigo: string): SolicitudAcademicaResumenDTORespuesta {
  return {
    uuidSolicitudAcademica: 'uuid-' + radicado,
    radicado,
    uuidTipoSolicitudAcademica: 't',
    tipoSolicitud: tipo,
    fechaCreacion: '2026-10-08T10:00:00',
    etiqueta: 'Pendiente',
    etapaCodigo: 'RADICADA',
    nombreEstudiante: nombre,
    codigoEstudiantil: codigo
  };
}

describe('filtrarSolicitudes', () => {
  const lista = [
    solicitud('2026-CM-0001', 'Cancelación de Matrícula', 'María José Pérez', '104618021'),
    solicitud('2026-CA-0001', 'Cancelación de Asignatura', 'Andrés Gómez', '104618022'),
    solicitud('2026-ES-0001', 'Examen Supletorio', 'Lucía Muñoz', '104618023'),
    solicitud('2026-CA-0002', 'Cancelación de Asignatura', 'Juan Ruiz', '104618024')
  ];

  it('sin filtros devuelve todas en el mismo orden', () => {
    expect(filtrarSolicitudes(lista, TODOS_LOS_TIPOS, '')).toEqual(lista);
    expect(filtrarSolicitudes(lista, null, null)).toEqual(lista);
  });

  it('filtra por tipo exacto', () => {
    const resultado = filtrarSolicitudes(lista, 'Cancelación de Asignatura', '');
    expect(resultado.map(s => s.radicado)).toEqual(['2026-CA-0001', '2026-CA-0002']);
  });

  it('busca en radicado, nombre y codigo sin distinguir mayusculas ni tildes', () => {
    expect(filtrarSolicitudes(lista, TODOS_LOS_TIPOS, 'es-0001').map(s => s.radicado)).toEqual(['2026-ES-0001']);
    expect(filtrarSolicitudes(lista, TODOS_LOS_TIPOS, 'maria jose').map(s => s.radicado)).toEqual(['2026-CM-0001']);
    expect(filtrarSolicitudes(lista, TODOS_LOS_TIPOS, 'MUNOZ').map(s => s.radicado)).toEqual(['2026-ES-0001']);
    expect(filtrarSolicitudes(lista, TODOS_LOS_TIPOS, '618024').map(s => s.radicado)).toEqual(['2026-CA-0002']);
  });

  it('combina tipo y texto', () => {
    expect(filtrarSolicitudes(lista, 'Cancelación de Asignatura', 'juan').map(s => s.radicado)).toEqual(['2026-CA-0002']);
    expect(filtrarSolicitudes(lista, 'Examen Supletorio', 'juan')).toEqual([]);
  });

  it('filtra por la etiqueta exacta del estado', () => {
    const conEstados = lista.map((s, i) => ({ ...s, etiqueta: i % 2 === 0 ? 'Pendiente' : 'En Gestión' }));
    expect(filtrarSolicitudes(conEstados, TODOS_LOS_TIPOS, '', 'En Gestión').map(s => s.radicado))
      .toEqual(['2026-CA-0001', '2026-CA-0002']);
    expect(filtrarSolicitudes(conEstados, TODOS_LOS_TIPOS, '', TODOS_LOS_TIPOS).length).toBe(4);
  });
});

describe('opciones y orden de la bandeja', () => {
  const lista = [
    { ...solicitud('2026-CA-0001', 'Cancelación de Asignatura', 'A', '1'), etiqueta: 'Pendiente', fechaCreacion: '2026-10-02T10:00:00' },
    { ...solicitud('2026-CA-0002', 'Cancelación de Asignatura', 'B', '2'), etiqueta: 'En Gestión', fechaCreacion: '2026-10-01T10:00:00' },
    { ...solicitud('2026-CA-0003', 'Cancelación de Asignatura', 'C', '3'), etiqueta: 'Pendiente', fechaCreacion: '2026-10-03T10:00:00' }
  ];

  it('lista las etiquetas presentes sin repetir y en orden', () => {
    expect(etiquetasDisponibles(lista)).toEqual(['En Gestión', 'Pendiente']);
  });

  it('ordena por fecha y por estado sin cambiar la lista original', () => {
    expect(ordenarSolicitudes(lista, 'fecha', 'asc').map(s => s.radicado)).toEqual(['2026-CA-0002', '2026-CA-0001', '2026-CA-0003']);
    expect(ordenarSolicitudes(lista, 'fecha', 'desc').map(s => s.radicado)).toEqual(['2026-CA-0003', '2026-CA-0001', '2026-CA-0002']);
    expect(ordenarSolicitudes(lista, 'estado', 'asc')[0].etiqueta).toBe('En Gestión');
    expect(ordenarSolicitudes(lista, null, 'asc')).toBe(lista);
    expect(lista[0].radicado).toBe('2026-CA-0001');
  });
});

describe('paginar', () => {
  const numeros = Array.from({ length: 23 }, (_, i) => i + 1);

  it('calcula el total de paginas con minimo una', () => {
    expect(totalPaginas(23, 10)).toBe(3);
    expect(totalPaginas(20, 10)).toBe(2);
    expect(totalPaginas(0, 10)).toBe(1);
  });

  it('corta la pagina pedida empezando en 1', () => {
    expect(paginar(numeros, 1, 10)).toEqual([1, 2, 3, 4, 5, 6, 7, 8, 9, 10]);
    expect(paginar(numeros, 3, 10)).toEqual([21, 22, 23]);
  });

  it('ajusta paginas fuera de rango', () => {
    expect(paginar(numeros, 0, 10)[0]).toBe(1);
    expect(paginar(numeros, 9, 10)).toEqual([21, 22, 23]);
    expect(paginar([], 1, 10)).toEqual([]);
  });
});
