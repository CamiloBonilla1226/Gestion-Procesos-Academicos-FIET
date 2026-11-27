import { BorradorResolucionDTORespuesta } from '../models/CancelacionAsignatura/DTOResponse/BorradorResolucionDTORespuesta';

export const MAXIMO_TEXTO_RESOLUCION = 20000;
export const MAXIMO_CAMPO_RESOLUCION = 300;
export const MAXIMO_CELDA_RESOLUCION = 120;
export const MAXIMO_NUMERO_RESOLUCION = 40;

const NUMERO_VALIDO = /^[0-9A-Za-z-]*$/;
const FECHA_VALIDA = /^\d{4}-\d{2}-\d{2}$/;
const PERMITIDOS_FUERA_DE_LATIN = '€‚ƒ„…†‡ˆ‰Š‹ŒŽ‘’“”•–—˜™š›œžŸ';

const TEXTOS: { campo: keyof BorradorResolucionDTORespuesta; nombre: string; obligatorio: boolean }[] = [
  { campo: 'preambulo', nombre: 'El preámbulo', obligatorio: true },
  { campo: 'considerandos', nombre: 'Los considerandos', obligatorio: true },
  { campo: 'articuloPrimero', nombre: 'El artículo primero', obligatorio: true },
  { campo: 'articulosSiguientes', nombre: 'Los demás artículos', obligatorio: true },
  { campo: 'diligencia', nombre: 'La diligencia de notificación', obligatorio: false }
];

const CAMPOS: { campo: keyof BorradorResolucionDTORespuesta; nombre: string; obligatorio: boolean }[] = [
  { campo: 'asunto', nombre: 'El asunto', obligatorio: true },
  { campo: 'firmante', nombre: 'El nombre de quien firma', obligatorio: true },
  { campo: 'cargoFirmante', nombre: 'El cargo de quien firma', obligatorio: true },
  { campo: 'proyecto', nombre: 'El campo Proyectó', obligatorio: false },
  { campo: 'reviso', nombre: 'El campo Revisó', obligatorio: false }
];

export function caracteresNoImprimibles(texto: string): string[] {
  const encontrados = new Set<string>();
  for (const caracter of texto ?? '') {
    const codigo = caracter.codePointAt(0) ?? 0;
    const imprimible = caracter === '\n' || caracter === '\r' || caracter === '\t' || (codigo >= 32 && codigo <= 126)
      || (codigo >= 160 && codigo <= 255) || PERMITIDOS_FUERA_DE_LATIN.includes(caracter);
    if (!imprimible) encontrados.add(caracter);
  }
  return [...encontrados];
}

function errorDeTextoResolucion(valor: string, nombre: string, maximo: number, obligatorio: boolean): string | null {
  const texto = valor ?? '';
  if (obligatorio && texto.trim() === '') return `${nombre} es obligatorio.`;
  if (texto.length > maximo) return `${nombre} supera los ${maximo} caracteres permitidos.`;
  if ((texto.match(/\*\*/g) ?? []).length % 2 !== 0) return `${nombre} tiene un marcador de negrilla (**) sin cerrar.`;
  const raros = caracteresNoImprimibles(texto);
  if (raros.length > 0) return `${nombre} contiene caracteres que no se pueden imprimir en el PDF: ${raros.join(' ')}. Se deben retirar.`;
  return null;
}

export function erroresDeResolucion(borrador: BorradorResolucionDTORespuesta, exigeNumero: boolean): Record<string, string> {
  const errores: Record<string, string> = {};
  const numero = (borrador.numero ?? '').trim();
  if (exigeNumero && numero === '') errores['numero'] = 'Se debe registrar el número de la Resolución antes de descargarla.';
  else if (numero.length > MAXIMO_NUMERO_RESOLUCION) errores['numero'] = `El número no puede superar los ${MAXIMO_NUMERO_RESOLUCION} caracteres.`;
  else if (!NUMERO_VALIDO.test(numero)) errores['numero'] = 'El número solo admite dígitos, letras y guiones, sin espacios.';

  const fecha = borrador.fecha ?? '';
  const anio = Number(fecha.substring(0, 4));
  if (!FECHA_VALIDA.test(fecha) || Number.isNaN(new Date(`${fecha}T00:00:00`).getTime())) errores['fecha'] = 'Se debe seleccionar una fecha de expedición válida.';
  else if (anio < 2000 || anio > 2099) errores['fecha'] = 'La fecha de expedición debe estar entre los años 2000 y 2099.';

  for (const { campo, nombre, obligatorio } of TEXTOS) {
    const error = errorDeTextoResolucion(String(borrador[campo] ?? ''), nombre, MAXIMO_TEXTO_RESOLUCION, obligatorio);
    if (error) errores[campo] = error;
  }
  for (const { campo, nombre, obligatorio } of CAMPOS) {
    const error = errorDeTextoResolucion(String(borrador[campo] ?? ''), nombre, MAXIMO_CAMPO_RESOLUCION, obligatorio);
    if (error) errores[campo] = error;
  }
  (borrador.filas ?? []).forEach((fila, indice) => {
    for (const [clave, valor] of Object.entries(fila)) {
      const error = errorDeTextoResolucion(String(valor ?? ''), 'La celda', MAXIMO_CELDA_RESOLUCION, clave === 'asignatura');
      if (error) errores[`fila-${indice}-${clave}`] = error;
    }
  });
  return errores;
}
