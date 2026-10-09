const FECHA_HORA = /^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})(?::\d{2}(?:\.\d+)?)?$/;
const FECHA = /^(\d{4})-(\d{2})-(\d{2})$/;

export function formatearFechaHora(valor: string | null | undefined): string {
  if (!valor) return '';
  const partes = FECHA_HORA.exec(valor.trim());
  if (!partes) return valor;
  const [, anio, mes, dia, hora, minuto] = partes;
  return `${dia}/${mes}/${anio} ${hora}:${minuto}`;
}

export function formatearFecha(valor: string | null | undefined): string {
  if (!valor) return '';
  const partes = FECHA.exec(valor.trim());
  if (!partes) return valor;
  const [, anio, mes, dia] = partes;
  return `${dia}/${mes}/${anio}`;
}

export function formatearTamanio(bytes: number | null | undefined): string {
  if (bytes === null || bytes === undefined || !Number.isFinite(bytes) || bytes < 0) return '';
  const megabyte = 1024 * 1024;
  if (bytes >= megabyte) return `${conDecimal(bytes / megabyte)} MB`;
  return `${conDecimal(bytes / 1024)} KB`;
}

function conDecimal(valor: number): string {
  return valor.toFixed(1).replace('.', ',');
}
