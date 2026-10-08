export function leerCodigoError(err: unknown): number | undefined {
  if (!err || typeof err !== 'object' || !('error' in err)) return undefined;
  const cuerpo = (err as { error: unknown }).error;
  if (!cuerpo || typeof cuerpo !== 'object' || !('codigoError' in cuerpo)) return undefined;
  const valor = (cuerpo as { codigoError: unknown }).codigoError;
  if (typeof valor !== 'number' && typeof valor !== 'string') return undefined;
  if (typeof valor === 'string' && valor.trim() === '') return undefined;
  const codigo = Number(valor);
  return Number.isInteger(codigo) ? codigo : undefined;
}
