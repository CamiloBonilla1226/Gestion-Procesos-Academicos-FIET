import { SolicitudAcademicaResumenDTORespuesta } from '../models/SolicitudAcademica/DTOResponse/SolicitudAcademicaResumenDTORespuesta';

export const TODOS_LOS_TIPOS = 'TODOS';

export function normalizarTexto(valor: string | null | undefined): string {
  return (valor ?? '')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .trim();
}

export function filtrarSolicitudes(
  solicitudes: SolicitudAcademicaResumenDTORespuesta[],
  tipo: string | null | undefined,
  texto: string | null | undefined
): SolicitudAcademicaResumenDTORespuesta[] {
  const buscado = normalizarTexto(texto);
  const filtrarTipo = !!tipo && tipo !== TODOS_LOS_TIPOS;
  return solicitudes.filter(solicitud => {
    if (filtrarTipo && solicitud.tipoSolicitud !== tipo) return false;
    if (!buscado) return true;
    return [solicitud.radicado, solicitud.nombreEstudiante, solicitud.codigoEstudiantil]
      .some(campo => normalizarTexto(campo).includes(buscado));
  });
}

export function totalPaginas(total: number, tamanio: number): number {
  return Math.max(1, Math.ceil(total / tamanio));
}

export function paginar<T>(elementos: T[], pagina: number, tamanio: number): T[] {
  const actual = Math.min(Math.max(1, pagina), totalPaginas(elementos.length, tamanio));
  const inicio = (actual - 1) * tamanio;
  return elementos.slice(inicio, inicio + tamanio);
}
