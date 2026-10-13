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

export function claseInsigniaDecisionDecano(etiqueta: string | null | undefined): string | null {
  if (esEtiqueta(etiqueta, ETIQUETAS_DECISION_DECANO.APROBADA)) return 'text-bg-success';
  if (esEtiqueta(etiqueta, ETIQUETAS_DECISION_DECANO.RECHAZADA)) return 'text-bg-danger';
  return null;
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
