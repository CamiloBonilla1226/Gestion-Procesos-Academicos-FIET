export const CARPETA_FORMATOS = 'formatos';

export const FORMATO_SUPLETORIO = 'supletorio.docx';

export const FORMATOS_POR_ANEXO: Record<string, string> = {
  'Formato firmado por el docente que orienta la asignatura': FORMATO_SUPLETORIO
};

export function rutaFormato(archivo: string): string {
  return `${CARPETA_FORMATOS}/${archivo}`;
}
