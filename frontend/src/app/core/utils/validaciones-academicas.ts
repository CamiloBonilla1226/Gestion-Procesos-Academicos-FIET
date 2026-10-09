import { TipoAnexoDTORespuesta } from '../models/TipoSolicitud/DTOResponse/TipoAnexoDTORespuesta';
import {
  FALTAS_MINIMAS,
  NOTA_MAXIMA,
  NOTA_MINIMA,
  TAMANIO_MAXIMO_ARCHIVO_BYTES
} from '../constantes/procesos-academicos';
import { formatearTamanio } from './formato';

const EQUIVALENTES: Record<string, string[]> = {
  jpg: ['jpg', 'jpeg'],
  jpeg: ['jpg', 'jpeg']
};

export function listaFormatos(formatos: string | null | undefined): string[] {
  const lista = (formatos ?? '')
    .split(',')
    .map(formato => formato.trim().toLowerCase())
    .filter(formato => formato !== '');
  const ampliada = lista.flatMap(formato => EQUIVALENTES[formato] ?? [formato]);
  return [...new Set(ampliada)];
}

export function extensionDe(nombreArchivo: string): string {
  const punto = nombreArchivo.lastIndexOf('.');
  return punto < 0 ? '' : nombreArchivo.slice(punto + 1).trim().toLowerCase();
}

export function extensionPermitida(nombreArchivo: string, formatos: string | null | undefined): boolean {
  const permitidos = listaFormatos(formatos);
  if (permitidos.length === 0) return true;
  return permitidos.includes(extensionDe(nombreArchivo));
}

export function errorDeArchivo(
  archivo: File,
  formatos: string | null | undefined,
  maximoBytes: number = TAMANIO_MAXIMO_ARCHIVO_BYTES
): string | null {
  const maximo = Math.min(maximoBytes > 0 ? maximoBytes : TAMANIO_MAXIMO_ARCHIVO_BYTES, TAMANIO_MAXIMO_ARCHIVO_BYTES);
  if (archivo.size === 0) return 'El archivo está vacío.';
  if (archivo.size > maximo) return `El archivo supera el máximo de ${textoMaximo(maximo)}.`;
  if (!extensionPermitida(archivo.name, formatos)) {
    return `Formato no permitido. Se aceptan: ${listaFormatos(formatos).join(', ')}.`;
  }
  return null;
}

function textoMaximo(bytes: number): string {
  const megabyte = 1024 * 1024;
  return bytes % megabyte === 0 ? `${bytes / megabyte} MB` : formatearTamanio(bytes);
}

export function errorDeTexto(
  valor: string | null | undefined,
  maximo: number,
  obligatorio: boolean,
  campo: string,
  femenino = false
): string | null {
  const texto = (valor ?? '').trim();
  if (obligatorio && texto === '') return `${campo} es ${femenino ? 'obligatoria' : 'obligatorio'}.`;
  if (texto.length > maximo) return `${campo} supera los ${maximo} caracteres permitidos.`;
  return null;
}

export function leerNota(valor: string | null | undefined): number | null {
  const texto = (valor ?? '').trim().replace(',', '.');
  if (!/^\d(\.\d)?$/.test(texto)) return null;
  const nota = Number(texto);
  return nota >= NOTA_MINIMA && nota <= NOTA_MAXIMA ? nota : null;
}

export function leerFaltas(valor: string | null | undefined): number | null {
  const texto = (valor ?? '').trim();
  if (!/^\d+$/.test(texto)) return null;
  const faltas = Number(texto);
  return Number.isSafeInteger(faltas) && faltas >= FALTAS_MINIMAS ? faltas : null;
}

export function esFechaValida(valor: string | null | undefined): boolean {
  const partes = /^(\d{4})-(\d{2})-(\d{2})$/.exec((valor ?? '').trim());
  if (!partes) return false;
  const [anio, mes, dia] = partes.slice(1).map(Number);
  const fecha = new Date(Date.UTC(anio, mes - 1, dia));
  return fecha.getUTCFullYear() === anio && fecha.getUTCMonth() === mes - 1 && fecha.getUTCDate() === dia;
}

export function esHoraValida(valor: string | null | undefined): boolean {
  return /^([01]\d|2[0-3]):[0-5]\d$/.test((valor ?? '').trim());
}

export function fechaDeHoy(hoy: Date = new Date()): string {
  const dosDigitos = (valor: number) => String(valor).padStart(2, '0');
  return `${hoy.getFullYear()}-${dosDigitos(hoy.getMonth() + 1)}-${dosDigitos(hoy.getDate())}`;
}

export function comoTipoAnexo(
  uuid: string,
  nombre: string,
  formatos: string | null | undefined,
  obligatorio: boolean
): TipoAnexoDTORespuesta {
  return {
    uuidTipoAnexo: uuid,
    nombre,
    descripcion: '',
    formato: listaFormatos(formatos).join(','),
    obligatoriedad: obligatorio
  };
}

export function erroresDeCampos(err: unknown): Record<string, string> {
  if (!err || typeof err !== 'object' || !('status' in err) || (err as { status: unknown }).status !== 400) return {};
  const cuerpo = (err as { error?: unknown }).error;
  if (!cuerpo || typeof cuerpo !== 'object') return {};
  const errores: Record<string, string> = {};
  for (const [campo, mensaje] of Object.entries(cuerpo)) {
    if (typeof mensaje === 'string') errores[campo] = mensaje;
  }
  return errores;
}
