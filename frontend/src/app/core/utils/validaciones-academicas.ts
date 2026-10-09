import { TipoAnexoDTORespuesta } from '../models/TipoSolicitud/DTOResponse/TipoAnexoDTORespuesta';
import {
  FALTAS_MINIMAS,
  NOTA_MAXIMA,
  NOTA_MINIMA,
  TAMANIO_MAXIMO_ARCHIVO_BYTES
} from '../constantes/procesos-academicos';

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

export function errorDeArchivo(archivo: File, formatos: string | null | undefined): string | null {
  if (archivo.size === 0) return 'El archivo está vacío.';
  if (archivo.size > TAMANIO_MAXIMO_ARCHIVO_BYTES) return 'El archivo supera el máximo de 5 MB.';
  if (!extensionPermitida(archivo.name, formatos)) {
    return `Formato no permitido. Se aceptan: ${listaFormatos(formatos).join(', ')}.`;
  }
  return null;
}

export function errorDeTexto(
  valor: string | null | undefined,
  maximo: number,
  obligatorio: boolean,
  campo: string
): string | null {
  const texto = (valor ?? '').trim();
  if (obligatorio && texto === '') return `${campo} es obligatorio.`;
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
