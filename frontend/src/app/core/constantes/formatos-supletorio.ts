export const CARPETA_FORMATOS = 'formatos';

export const FORMATO_FIRMA_DOCENTE = 'formato-firma-docente.doc';

export const FORMATO_FIRMA_DOCENTE_CRUCE = 'formato-firma-docente-cruce.doc';

export const FORMATOS_POR_ANEXO: Record<string, string> = {
  'Formato firmado por el docente que orienta la asignatura': FORMATO_FIRMA_DOCENTE,
  'Formato firmado por el docente de la asignatura con la que se cruza': FORMATO_FIRMA_DOCENTE_CRUCE
};

export function rutaFormato(archivo: string): string {
  return `${CARPETA_FORMATOS}/${archivo}`;
}
