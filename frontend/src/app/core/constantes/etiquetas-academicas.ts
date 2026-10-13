import { ACCIONES_ACADEMICAS, AccionAcademica } from './procesos-academicos';

export const AVISO_OBSERVACION_DECANO = 'Esta observación solo será visible para el Funcionario Académico.';

export const ETIQUETAS_ESTUDIANTE = {
  EN_TRAMITE: 'En trámite',
  PENDIENTE_PAGO: 'Pendiente de pago',
  EN_VERIFICACION_PAGO: 'En verificación de pago',
  APROBADA: 'Aprobada',
  RECHAZADA: 'Rechazada'
} as const;

export const ETIQUETAS_FINALES_ESTUDIANTE: readonly string[] = [ETIQUETAS_ESTUDIANTE.APROBADA, ETIQUETAS_ESTUDIANTE.RECHAZADA];

export const ETIQUETAS_DECISION_DECANO = {
  APROBADA: 'Aprobada por el Decano',
  RECHAZADA: 'Rechazada por el Decano'
} as const;

function mismaEtiqueta(etiqueta: string | null | undefined, esperada: string): boolean {
  return (etiqueta ?? '').trim().normalize('NFC').toLowerCase() === esperada.normalize('NFC').toLowerCase();
}

export function esEtiqueta(etiqueta: string | null | undefined, ...esperadas: string[]): boolean {
  return esperadas.some(esperada => mismaEtiqueta(etiqueta, esperada));
}

export function claseInsigniaEtiquetaEstudiante(etiqueta: string | null | undefined): string {
  if (esEtiqueta(etiqueta, ETIQUETAS_ESTUDIANTE.APROBADA)) return 'text-bg-success';
  if (esEtiqueta(etiqueta, ETIQUETAS_ESTUDIANTE.RECHAZADA)) return 'text-bg-danger';
  if (esEtiqueta(etiqueta, ETIQUETAS_ESTUDIANTE.PENDIENTE_PAGO, ETIQUETAS_ESTUDIANTE.EN_VERIFICACION_PAGO)) return 'text-bg-warning';
  return 'text-bg-secondary';
}

export type CategoriaEstado = 'pendiente' | 'gestion' | 'notificar' | 'aprobada' | 'rechazada' | 'pago' | 'otro';

const CATEGORIAS_ESTADO: [CategoriaEstado, string[]][] = [
  ['aprobada', [ETIQUETAS_ESTUDIANTE.APROBADA, ETIQUETAS_DECISION_DECANO.APROBADA, 'Respondida']],
  ['rechazada', [ETIQUETAS_ESTUDIANTE.RECHAZADA, ETIQUETAS_DECISION_DECANO.RECHAZADA]],
  ['pago', [ETIQUETAS_ESTUDIANTE.PENDIENTE_PAGO]],
  ['notificar', ['Pendiente de Respuesta', 'Pendiente Notificar', 'Por notificar', 'Enviar Recibo']],
  ['gestion', [ETIQUETAS_ESTUDIANTE.EN_TRAMITE, ETIQUETAS_ESTUDIANTE.EN_VERIFICACION_PAGO, 'En Gestión', 'Pendiente de Verificación']],
  ['pendiente', ['Pendiente']]
];

export function categoriaEstado(etiqueta: string | null | undefined): CategoriaEstado {
  return CATEGORIAS_ESTADO.find(([, etiquetas]) => esEtiqueta(etiqueta, ...etiquetas))?.[0] ?? 'otro';
}

export interface CampoObservacion {
  etiqueta: string;
  obligatoria: boolean;
  aviso: string | null;
  esMensaje?: boolean;
}

const CAMPOS_OBSERVACION: Partial<Record<AccionAcademica, CampoObservacion>> = {
  [ACCIONES_ACADEMICAS.RECHAZAR_FUNCIONARIO]: { etiqueta: 'Observación', obligatoria: true, aviso: null },
  [ACCIONES_ACADEMICAS.REMITIR_DECANO]: {
    etiqueta: 'Observación para el Decano',
    obligatoria: false,
    aviso: 'El estudiante no verá esta observación.'
  },
  [ACCIONES_ACADEMICAS.APROBAR_DECANO]: { etiqueta: 'Observación', obligatoria: false, aviso: AVISO_OBSERVACION_DECANO },
  [ACCIONES_ACADEMICAS.RECHAZAR_DECANO]: { etiqueta: 'Observación', obligatoria: false, aviso: AVISO_OBSERVACION_DECANO },
  [ACCIONES_ACADEMICAS.ENVIAR_RESPUESTA]: {
    etiqueta: 'Mensaje para el estudiante',
    obligatoria: false,
    aviso: 'Es lo único que el estudiante recibe sobre el motivo de la decisión.',
    esMensaje: true
  },
  [ACCIONES_ACADEMICAS.RECHAZAR_COMPROBANTE]: { etiqueta: 'Observación', obligatoria: true, aviso: null }
};

export function campoObservacion(accion: AccionAcademica | null | undefined): CampoObservacion | null {
  return accion ? CAMPOS_OBSERVACION[accion] ?? null : null;
}

export function etiquetaCampoObservacion(campo: CampoObservacion): string {
  return campo.obligatoria ? campo.etiqueta : `${campo.etiqueta} (opcional)`;
}

const ESPACIO_FACULTAD = 'Espacio a diligenciar por la Facultad a la que pertenece el estudiante';
const SITUACION_ACADEMICA = 'Situación Académica de la(s) Asignatura(s)';

export const RUTAS_ENCABEZADO_ASIGNATURAS: Record<string, string[]> = {
  'Faltas': [ESPACIO_FACULTAD, 'No. Faltas'],
  'Nota': [ESPACIO_FACULTAD, 'Nota'],
  'Situación en la matrícula': [ESPACIO_FACULTAD, SITUACION_ACADEMICA, 'En la Matrícula'],
  'Situación al cancelar': [ESPACIO_FACULTAD, SITUACION_ACADEMICA, 'Al Cancelar']
};

export const NOTA_APROBAR_DECANO = 'Al aprobar, quedará registrada tu decisión. El Funcionario Académico se encargará de redactar la Resolución correspondiente (si aplica) y de enviar la respuesta al Estudiante.';
