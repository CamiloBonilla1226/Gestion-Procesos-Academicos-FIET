import { SolicitudAcademicaResumenDTORespuesta } from '../models/SolicitudAcademica/DTOResponse/SolicitudAcademicaResumenDTORespuesta';

export const TODOS_LOS_TIPOS = 'TODOS';

export function normalizarTexto(valor: string | null | undefined): string {
  return (valor ?? '')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .trim();
}

export type CampoOrden = 'fecha' | 'estado';

export type DireccionOrden = 'asc' | 'desc';

export function filtrarSolicitudes(
  solicitudes: SolicitudAcademicaResumenDTORespuesta[],
  tipo: string | null | undefined,
  texto: string | null | undefined,
  estado: string | null | undefined = null,
  codigo: string | null | undefined = null
): SolicitudAcademicaResumenDTORespuesta[] {
  const buscado = normalizarTexto(texto);
  const codigoBuscado = normalizarTexto(codigo);
  const filtrarTipo = !!tipo && tipo !== TODOS_LOS_TIPOS;
  const filtrarEstado = !!estado && estado !== TODOS_LOS_TIPOS;
  return solicitudes.filter(solicitud => {
    if (filtrarTipo && solicitud.tipoSolicitud !== tipo) return false;
    if (filtrarEstado && solicitud.etiqueta !== estado) return false;
    if (codigoBuscado && !normalizarTexto(solicitud.codigoEstudiantil).includes(codigoBuscado)) return false;
    if (!buscado) return true;
    return [solicitud.radicado, solicitud.nombreEstudiante, solicitud.codigoEstudiantil, solicitud.numeroDocumento]
      .some(campo => normalizarTexto(campo).includes(buscado));
  });
}

export function etiquetasDisponibles(solicitudes: SolicitudAcademicaResumenDTORespuesta[]): string[] {
  return [...new Set(solicitudes.map(solicitud => solicitud.etiqueta).filter(etiqueta => !!etiqueta))]
    .sort((a, b) => a.localeCompare(b, 'es'));
}

export function ordenarSolicitudes(
  solicitudes: SolicitudAcademicaResumenDTORespuesta[],
  campo: CampoOrden | null,
  direccion: DireccionOrden
): SolicitudAcademicaResumenDTORespuesta[] {
  if (!campo) return solicitudes;
  const signo = direccion === 'asc' ? 1 : -1;
  const valor = (solicitud: SolicitudAcademicaResumenDTORespuesta) =>
    campo === 'fecha' ? solicitud.fechaCreacion ?? '' : normalizarTexto(solicitud.etiqueta);
  return [...solicitudes].sort((a, b) => signo * valor(a).localeCompare(valor(b)));
}

export function totalPaginas(total: number, tamanio: number): number {
  return Math.max(1, Math.ceil(total / tamanio));
}

export function paginar<T>(elementos: T[], pagina: number, tamanio: number): T[] {
  const actual = Math.min(Math.max(1, pagina), totalPaginas(elementos.length, tamanio));
  const inicio = (actual - 1) * tamanio;
  return elementos.slice(inicio, inicio + tamanio);
}
