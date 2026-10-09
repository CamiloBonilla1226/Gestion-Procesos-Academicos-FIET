export const ETAPAS_ACADEMICAS = {
  RADICADA: 'RADICADA',
  EN_REVISION_DECANO: 'EN_REVISION_DECANO',
  APROBADA_POR_DECANO: 'APROBADA_POR_DECANO',
  RECHAZADA_POR_DECANO: 'RECHAZADA_POR_DECANO',
  PENDIENTE_PAGO: 'PENDIENTE_PAGO',
  EN_VERIFICACION_PAGO: 'EN_VERIFICACION_PAGO',
  APROBADA: 'APROBADA',
  RECHAZADA: 'RECHAZADA'
} as const;

export type EtapaAcademica = (typeof ETAPAS_ACADEMICAS)[keyof typeof ETAPAS_ACADEMICAS];

export const ETAPAS_FINALES: readonly string[] = [ETAPAS_ACADEMICAS.APROBADA, ETAPAS_ACADEMICAS.RECHAZADA];

export const ACCIONES_ACADEMICAS = {
  RADICAR: 'RADICAR',
  RECHAZAR_FUNCIONARIO: 'RECHAZAR_FUNCIONARIO',
  REMITIR_DECANO: 'REMITIR_DECANO',
  APROBAR_DECANO: 'APROBAR_DECANO',
  RECHAZAR_DECANO: 'RECHAZAR_DECANO',
  ENVIAR_RESPUESTA: 'ENVIAR_RESPUESTA',
  ENVIAR_RECIBO: 'ENVIAR_RECIBO',
  SUBIR_COMPROBANTE: 'SUBIR_COMPROBANTE',
  APROBAR_COMPROBANTE: 'APROBAR_COMPROBANTE',
  RECHAZAR_COMPROBANTE: 'RECHAZAR_COMPROBANTE'
} as const;

export type AccionAcademica = (typeof ACCIONES_ACADEMICAS)[keyof typeof ACCIONES_ACADEMICAS];

export const TIPOS_SOLICITUD_ACADEMICA = {
  CANCELACION_MATRICULA: 'Cancelación de Matrícula',
  CANCELACION_ASIGNATURA: 'Cancelación de Asignatura',
  EXAMEN_SUPLETORIO: 'Examen Supletorio'
} as const;

export type TipoSolicitudAcademica = (typeof TIPOS_SOLICITUD_ACADEMICA)[keyof typeof TIPOS_SOLICITUD_ACADEMICA];

export const PROCESOS_ACADEMICOS = {
  CANCELACION_MATRICULA: 'cancelaciones-matricula',
  CANCELACION_ASIGNATURA: 'cancelaciones-asignatura',
  EXAMEN_SUPLETORIO: 'examenes-supletorios'
} as const;

export type ProcesoAcademico = (typeof PROCESOS_ACADEMICOS)[keyof typeof PROCESOS_ACADEMICOS];

const PROCESO_POR_TIPO: Record<string, ProcesoAcademico> = {
  [TIPOS_SOLICITUD_ACADEMICA.CANCELACION_MATRICULA]: PROCESOS_ACADEMICOS.CANCELACION_MATRICULA,
  [TIPOS_SOLICITUD_ACADEMICA.CANCELACION_ASIGNATURA]: PROCESOS_ACADEMICOS.CANCELACION_ASIGNATURA,
  [TIPOS_SOLICITUD_ACADEMICA.EXAMEN_SUPLETORIO]: PROCESOS_ACADEMICOS.EXAMEN_SUPLETORIO
};

export function procesoDeTipoSolicitud(nombreTipo: string | null | undefined): ProcesoAcademico | undefined {
  if (!nombreTipo) return undefined;
  return PROCESO_POR_TIPO[nombreTipo.trim().normalize('NFC')];
}

export const ROLES_ACADEMICOS = {
  ESTUDIANTE: 'Estudiante',
  FUNCIONARIO_ACADEMICO: 'Funcionario Académico',
  DECANO: 'Decano',
  SECRETARIO_GENERAL: 'Secretario General'
} as const;

export const ROLES_ETIQUETA = {
  ESTUDIANTE: 'ESTUDIANTE',
  FUNCIONARIO: 'FUNCIONARIO',
  DECANO: 'DECANO'
} as const;

export type RolEtiqueta = (typeof ROLES_ETIQUETA)[keyof typeof ROLES_ETIQUETA];

export const RUTAS_SOLICITUDES_POR_ROL: Record<RolEtiqueta, string> = {
  [ROLES_ETIQUETA.ESTUDIANTE]: '/estudiante/solicitudes',
  [ROLES_ETIQUETA.FUNCIONARIO]: '/funcionario-academico/solicitudes',
  [ROLES_ETIQUETA.DECANO]: '/decano/solicitudes'
};

export const TEXTOS_ACCIONES: Record<AccionAcademica, string> = {
  [ACCIONES_ACADEMICAS.RADICAR]: 'Radicación de la solicitud',
  [ACCIONES_ACADEMICAS.RECHAZAR_FUNCIONARIO]: 'Rechazo del Funcionario Académico',
  [ACCIONES_ACADEMICAS.REMITIR_DECANO]: 'Remisión al Decano',
  [ACCIONES_ACADEMICAS.APROBAR_DECANO]: 'Aprobación del Decano',
  [ACCIONES_ACADEMICAS.RECHAZAR_DECANO]: 'Rechazo del Decano',
  [ACCIONES_ACADEMICAS.ENVIAR_RESPUESTA]: 'Envío de la respuesta al estudiante',
  [ACCIONES_ACADEMICAS.ENVIAR_RECIBO]: 'Envío del recibo de pago',
  [ACCIONES_ACADEMICAS.SUBIR_COMPROBANTE]: 'Envío del comprobante de pago',
  [ACCIONES_ACADEMICAS.APROBAR_COMPROBANTE]: 'Aprobación del comprobante de pago',
  [ACCIONES_ACADEMICAS.RECHAZAR_COMPROBANTE]: 'Rechazo del comprobante de pago'
};

export const TEXTOS_ETAPAS: Record<EtapaAcademica, string> = {
  [ETAPAS_ACADEMICAS.RADICADA]: 'Radicada',
  [ETAPAS_ACADEMICAS.EN_REVISION_DECANO]: 'En revisión del Decano',
  [ETAPAS_ACADEMICAS.APROBADA_POR_DECANO]: 'Aprobada por el Decano',
  [ETAPAS_ACADEMICAS.RECHAZADA_POR_DECANO]: 'Rechazada por el Decano',
  [ETAPAS_ACADEMICAS.PENDIENTE_PAGO]: 'Pendiente de pago',
  [ETAPAS_ACADEMICAS.EN_VERIFICACION_PAGO]: 'En verificación de pago',
  [ETAPAS_ACADEMICAS.APROBADA]: 'Aprobada',
  [ETAPAS_ACADEMICAS.RECHAZADA]: 'Rechazada'
};

export function textoAccion(codigo: string | null | undefined): string {
  if (!codigo) return 'Acción sin registrar';
  return TEXTOS_ACCIONES[codigo as AccionAcademica] ?? codigo;
}

export function textoEtapa(codigo: string | null | undefined): string {
  if (!codigo) return 'Etapa no determinada';
  return TEXTOS_ETAPAS[codigo as EtapaAcademica] ?? codigo;
}

export function claseInsigniaEtapa(codigo: string | null | undefined): string {
  switch (codigo) {
    case ETAPAS_ACADEMICAS.APROBADA:
      return 'text-bg-success';
    case ETAPAS_ACADEMICAS.RECHAZADA:
      return 'text-bg-danger';
    case ETAPAS_ACADEMICAS.PENDIENTE_PAGO:
    case ETAPAS_ACADEMICAS.EN_VERIFICACION_PAGO:
      return 'text-bg-warning';
    default:
      return 'text-bg-secondary';
  }
}

export const CAUSAS_SUPLETORIO = {
  CRUCE: 'cruce',
  OTRA: 'otra'
} as const;

export type CausaSupletorio = (typeof CAUSAS_SUPLETORIO)[keyof typeof CAUSAS_SUPLETORIO];

export const ESTADOS_ASIGNATURA_MATRICULADA = {
  ACTIVA: 'activa',
  CANCELADA: 'cancelada',
  APROBADA: 'aprobada',
  PERDIDA: 'perdida'
} as const;

export const ANEXOS_PAGO_SUPLETORIO = {
  RECIBO: 'Recibo de pago',
  COMPROBANTE: 'Comprobante de pago'
} as const;

export const TAMANIO_MAXIMO_ARCHIVO_BYTES = 5242880;
export const MAXIMO_CARACTERES_MOTIVO = 255;
export const MAXIMO_CARACTERES_OBSERVACION = 500;
export const MAXIMO_CARACTERES_OBSERVACION_ASIGNATURA = 255;
export const PLAZO_SUPLETORIO_DIAS_HABILES = 3;
export const TAMANIO_MAXIMO_PETICION_BYTES = 20971520;
export const FORMATOS_SOPORTE_LIBRE = 'pdf,jpg,jpeg,png';
export const FORMATOS_RESOLUCION = 'pdf';
export const NOTA_MINIMA = 0;
export const NOTA_MAXIMA = 5;
export const FALTAS_MINIMAS = 0;
export const NOTA_MINIMA_PARA_CUMPLIR = 3;

export const TAMANIO_PAGINA_ADMINISTRACION = 10;

export const ENCABEZADOS_EXCEL_ESTUDIANTES = [
  'nombres', 'apellidos', 'tipoDocumento', 'numeroDocumento', 'telefono', 'correoElectronico', 'username', 'password',
  'codigoEstudiantil', 'programaAcademico', 'semestre', 'facultad', 'codigoAsignatura', 'nombreAsignatura', 'grupo'
];

export const ENCABEZADOS_EXCEL_FUNCIONARIOS = [
  'nombres', 'apellidos', 'tipoDocumento', 'numeroDocumento', 'telefono', 'correoElectronico', 'username', 'password',
  'dependencia'
];

export const OPCIONES_SI_NO: { label: string; value: boolean }[] = [
  { label: 'Sí', value: true },
  { label: 'No', value: false }
];

export const CODIGOS_ERROR = {
  GENERICO: 1,
  ENTIDAD_EXISTE: 2,
  ENTIDAD_NO_EXISTE: 3,
  REGLA_NEGOCIO_VIOLADA: 4,
  MAL_FORMATO: 6,
  SIN_INFORMACION: 7
} as const;

export const SIGNIFICADO_CODIGOS_ERROR: Record<number, string> = {
  [CODIGOS_ERROR.GENERICO]: 'Error genérico',
  [CODIGOS_ERROR.ENTIDAD_EXISTE]: 'La entidad ya existe',
  [CODIGOS_ERROR.ENTIDAD_NO_EXISTE]: 'La entidad no existe',
  [CODIGOS_ERROR.REGLA_NEGOCIO_VIOLADA]: 'Regla de negocio violada',
  [CODIGOS_ERROR.MAL_FORMATO]: 'Mal formato',
  [CODIGOS_ERROR.SIN_INFORMACION]: 'Sin información'
};
