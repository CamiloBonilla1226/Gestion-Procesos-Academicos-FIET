import { ProgramacionResolucionDTOPeticion } from '../models/CancelacionAsignatura/DTORequest/ProgramacionResolucionDTOPeticion';
import { NotificacionResolucionDTORespuesta } from '../models/CancelacionAsignatura/DTOResponse/NotificacionResolucionDTORespuesta';

export interface ProgramacionEnEdicion {
  fecha: string;
  numero: string;
  anio: string;
  decision: string;
}

export interface ComunicacionEnEdicion {
  mensaje: string;
  textoComunicacion: string;
  textoDarca: string;
  programacion: ProgramacionEnEdicion;
}

export interface FragmentoVista {
  texto: string;
  resaltado: boolean;
}

export type ParrafoVista = FragmentoVista[];

export const MAXIMO_NUMERO_RESOLUCION = 20;
export const MAXIMO_DECISION_RESOLUCION = 60;
export const MAXIMO_MENSAJE_INICIAL = 2000;
export const MAXIMO_TEXTO_COMUNICACION = 4000;
export const MARCAS_COMUNICACION_ESTUDIANTE = ['numero', 'anio', 'nombre', 'decision'];
export const MARCAS_DARCA = ['fecha'];
export const ANIO_MINIMO_RESOLUCION = 2000;
export const ANIO_MAXIMO_RESOLUCION = 2100;
export const ZONA_COLOMBIA = 'America/Bogota';

export const DECISIONES_RESOLUCION = ['aprueba la', 'aprueba parcialmente la', 'rechaza la'];

export const ESTADOS_NOTIFICACION: Record<NotificacionResolucionDTORespuesta['estado'], string> = {
  PROGRAMADA: 'Programada',
  EN_PROCESO: 'En envío',
  ENVIADA: 'Enviada',
  FALLIDA: 'Fallida'
};

export const NOTA_RETENCION_ESTUDIANTE =
  'Hasta la fecha y hora programadas el estudiante verá la solicitud en trámite, sin la respuesta ni la Resolución.';

const FORMATO_FECHA_HORA = /^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})/;

export function ahoraEnColombia(fecha: Date = new Date()): string {
  const partes = new Intl.DateTimeFormat('en-CA', {
    timeZone: ZONA_COLOMBIA,
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hourCycle: 'h23'
  }).formatToParts(fecha);
  const valor = (tipo: string) => partes.find(parte => parte.type === tipo)?.value ?? '00';
  return `${valor('year')}-${valor('month')}-${valor('day')}T${valor('hour')}:${valor('minute')}`;
}

export function fechaHoraLocal(valor: string | null | undefined): string {
  const coincidencia = FORMATO_FECHA_HORA.exec(valor ?? '');
  return coincidencia ? coincidencia[0] : '';
}

export function textoFechaHora(valor: string | null | undefined): string {
  const coincidencia = FORMATO_FECHA_HORA.exec(valor ?? '');
  if (!coincidencia) return '';
  const [, anio, mes, dia, hora, minuto] = coincidencia.map(Number);
  const fecha = new Date(anio, mes - 1, dia, hora, minuto);
  const dias = new Intl.DateTimeFormat('es-CO', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' }).format(fecha);
  return `${dias}, ${String(hora).padStart(2, '0')}:${String(minuto).padStart(2, '0')}`;
}

export function programacionVacia(anio: number, decision: string): ProgramacionEnEdicion {
  return { fecha: '', numero: '', anio: String(anio), decision };
}

export function programacionDesde(notificacion: NotificacionResolucionDTORespuesta): ProgramacionEnEdicion {
  return {
    fecha: fechaHoraLocal(notificacion.fechaProgramada),
    numero: notificacion.numeroResolucion ?? '',
    anio: notificacion.anioResolucion === null || notificacion.anioResolucion === undefined ? '' : String(notificacion.anioResolucion),
    decision: notificacion.decision ?? ''
  };
}

export function erroresDeProgramacion(programacion: ProgramacionEnEdicion, ahora: string = ahoraEnColombia()): Record<string, string> {
  const errores: Record<string, string> = {};
  const fecha = fechaHoraLocal(programacion.fecha);
  if (!fecha) errores['fecha'] = 'Elige la fecha y la hora de la comunicación.';
  else if (fecha <= ahora) errores['fecha'] = 'La fecha y hora deben ser posteriores a la hora actual de Colombia.';
  const numero = programacion.numero.trim();
  if (!numero) errores['numero'] = 'El número de la Resolución es obligatorio.';
  else if (numero.length > MAXIMO_NUMERO_RESOLUCION) errores['numero'] = `El número admite máximo ${MAXIMO_NUMERO_RESOLUCION} caracteres.`;
  const anio = Number(programacion.anio.trim());
  if (!/^\d{4}$/.test(programacion.anio.trim()) || anio < ANIO_MINIMO_RESOLUCION || anio > ANIO_MAXIMO_RESOLUCION) {
    errores['anio'] = `El año debe estar entre ${ANIO_MINIMO_RESOLUCION} y ${ANIO_MAXIMO_RESOLUCION}.`;
  }
  const decision = programacion.decision.trim();
  if (!decision) errores['decision'] = 'La decisión es obligatoria.';
  else if (decision.length > MAXIMO_DECISION_RESOLUCION) errores['decision'] = `La decisión admite máximo ${MAXIMO_DECISION_RESOLUCION} caracteres.`;
  return errores;
}

export function errorDeMensajeInicial(comunicacion: ComunicacionEnEdicion, maximo: number = MAXIMO_MENSAJE_INICIAL): string {
  const mensaje = comunicacion.mensaje.trim();
  if (!mensaje) return 'Escribe el mensaje inmediato al estudiante; siempre se envía.';
  if (mensaje.length > maximo) return `El mensaje admite máximo ${maximo} caracteres.`;
  return '';
}

export function errorDeTextoComunicacion(texto: string, destinatario: string, maximo: number = MAXIMO_TEXTO_COMUNICACION): string {
  const limpio = texto.trim();
  if (!limpio) return `Escribe el texto que recibirá ${destinatario}.`;
  if (limpio.length > maximo) return `El texto admite máximo ${maximo} caracteres.`;
  return '';
}

export function erroresDeTextos(comunicacion: ComunicacionEnEdicion, maximo: number = MAXIMO_TEXTO_COMUNICACION): Record<string, string> {
  const errores: Record<string, string> = {};
  const estudiante = errorDeTextoComunicacion(comunicacion.textoComunicacion, 'el estudiante', maximo);
  const darca = errorDeTextoComunicacion(comunicacion.textoDarca, 'DARCA', maximo);
  if (estudiante) errores['textoComunicacion'] = estudiante;
  if (darca) errores['textoDarca'] = darca;
  return errores;
}

export function parrafosConMarcas(texto: string, valores: Record<string, string>, resaltar = true): ParrafoVista[] {
  return texto
    .replace(/\r\n?/g, '\n')
    .trim()
    .split(/\n\s*\n/)
    .filter(parrafo => parrafo.trim() !== '')
    .map(parrafo => {
      const fragmentos: FragmentoVista[] = [];
      let desde = 0;
      for (const marca of parrafo.matchAll(/\{(\w+)\}/g)) {
        const conocida = Object.prototype.hasOwnProperty.call(valores, marca[1]);
        if (marca.index! > desde) fragmentos.push({ texto: parrafo.slice(desde, marca.index), resaltado: false });
        fragmentos.push({ texto: conocida ? valores[marca[1]] : marca[0], resaltado: conocida && resaltar });
        desde = marca.index! + marca[0].length;
      }
      if (desde < parrafo.length) fragmentos.push({ texto: parrafo.slice(desde), resaltado: false });
      return fragmentos;
    });
}

export function valoresComunicacionEstudiante(programacion: ProgramacionEnEdicion, nombre: string): Record<string, string> {
  const vacio = '___';
  return {
    numero: programacion.numero.trim() || vacio,
    anio: programacion.anio.trim() || vacio,
    nombre: nombre.trim() || vacio,
    decision: programacion.decision.trim() || vacio
  };
}

export function fechaCarta(valor: string | null | undefined): string {
  const coincidencia = FORMATO_FECHA_HORA.exec(valor ?? '');
  if (!coincidencia) return '___';
  const [, anio, mes, dia] = coincidencia.map(Number);
  return new Intl.DateTimeFormat('es-CO', { day: 'numeric', month: 'long', year: 'numeric' }).format(new Date(anio, mes - 1, dia));
}

export function filasDeTexto(texto: string, minimo = 6, anchoLinea = 90): number {
  return Math.max(minimo, texto.split('\n').reduce((total, linea) => total + Math.max(1, Math.ceil(linea.length / anchoLinea)), 0) + 1);
}

export function programacionAPeticion(programacion: ProgramacionEnEdicion): ProgramacionResolucionDTOPeticion {
  return {
    fechaProgramada: fechaHoraLocal(programacion.fecha),
    numeroResolucion: programacion.numero.trim(),
    anioResolucion: Number(programacion.anio.trim()),
    decision: programacion.decision.trim()
  };
}
