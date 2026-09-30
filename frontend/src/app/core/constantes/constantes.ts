/**
 * @author Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */

/**
 * Roles permitidos en el sistema.
 */
export const ROLES_FIET: string[] = [
  'Secretario General',
  'Funcionario',
  'Secretaria Decanatura FIET',
  'Coordinador Pregrado',
  'Coordinador Posgrados',
  'Jefe de Departamento',
  'Decano',
  'Docente'
];


/**
 * Secciones del Orden del Día.
 */
export const SECCIONES = [
    { label: 'Asuntos Decano', value: 'asuntos decano' },
    { label: 'Asuntos Pregrado', value: 'asuntos pregrado' },
    { label: 'Asuntos Posgrados', value: 'asuntos posgrados' },
    { label: 'Asuntos Delegados en Decano', value: 'asuntos delegados en decano' },
    { label: 'Solicitud Comisión Académica al Interior del País', value: 'solicitud comisión académica al interior del país' },
    { label: 'Solicitud Comisión Académica al Exterior del País', value: 'solicitud comisión académica al exterior del país' },
    { label: 'Informe de Comisión Académica', value: 'informe de comisión académica' },
    { label: 'Asuntos Varios', value: 'asuntos varios' }
  ];

/**
 * Tipos de Respuestas validas para las Solicitudes.
 */
export const TIPOS_RESPUESTAS = [
  { label: 'Oficio', value: 'Oficio' },
  { label: 'Resolución', value: 'Resolución' },
]

/**
 * Estados posibles de una solicitud.
 */
export const ESTADOS_SOLICITUD = [
  { label: 'SIN RESPONDER', value: 'SIN RESPONDER' },
  { label: 'RECHAZADO', value: 'RECHAZADO' },
  { label: 'PENDIENTE AL ORDEN DEL DÍA', value: 'PENDIENTE AL ORDEN DEL DÍA' },
  { label: 'AGREGADO EN EL ORDEN DEL DÍA', value: 'AGREGADO EN EL ORDEN DEL DÍA' },
  { label: 'RESPONDIDA', value: 'RESPONDIDA' }
];

/**
 * Estado pendiente de inclusión en el orden del día.
 */
export const PENDIENTE_AL_ORDEN_DEL_DIA = 'PENDIENTE AL ORDEN DEL DÍA';

/**
 * Estado agregado al orden del día.
 */
export const AGREGADO_EN_EL_ORDEN_DEL_DIA = 'AGREGADO EN EL ORDEN DEL DÍA';

/**
 * Estado de respondida una solicitud.
 */
export const RESPONDIDA = "RESPONDIDA";

/**
 * Estado activo.
 */
export const ACTIVO = '✅ Activo';

/**
 * Estado inactivo.
 */
export const INACTIVO = '❌ Inactivo';

/**
 * Estados booleanos representados como texto.
 */
export const ESTADO_BOOLEANO = [
  { label: '✅ Activo', value: '✅ Activo' },
  { label: '❌ Inactivo', value: '❌ Inactivo' }
];

/**
 * Formatos permitidos para archivos.
 */
export const FORMATOS = [
  { label: 'PDF', value: 'PDF' },
  { label: 'DOCX', value: 'DOCX' },
  { label: 'XLSX', value: 'XLSX' }
];

/**
 * Valores booleanos para selección.
 */
export const BOOLEANO = [
  { label: 'Sí', value: true },
  { label: 'No', value: false }
];

/**
 * Tipos de documento de identificación.
 */
export const TIPOS_DOCUMENTO = [
  { label: 'Cédula de ciudadanía', value: 'Cédula de ciudadanía' },
  { label: 'Tarjeta de identidad', value: 'Tarjeta de identidad' },
  { label: 'Cédula de extranjería', value: 'Cédula de extranjería' }
];

/**
 * Secciones del orden del día.
 */
export const SECCIONES_ORDEN_DIA = [
  { id: 'decano', label: 'asuntos decano' },
  { id: 'pregrado', label: 'asuntos pregrado' },
  { id: 'posgrados', label: 'asuntos posgrados' },
  { id: 'delegados', label: 'asuntos delegados en decano' },
  { id: 'comisionInterior', label: 'solicitud comisión académica al interior del país' },
  { id: 'comisionExterior', label: 'solicitud comisión académica al exterior del país' },
  { id: 'informe', label: 'informe de comisión académica' },
  { id: 'varios', label: 'asuntos varios' }
];
