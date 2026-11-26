export interface AnexoCatalogo {
  texto: string;
  formato?: string;
}

export interface TramiteCatalogo {
  titulo: string;
  icono: string;
  ruta: string;
  descripcion: string;
  requisitos: string[];
  anexos: AnexoCatalogo[];
}

export const TRAMITES_ESTUDIANTE: TramiteCatalogo[] = [
  {
    titulo: 'Cancelación de Matrícula',
    icono: 'buttons/folder.svg',
    ruta: '/estudiante/cancelacion-matricula',
    descripcion: 'Retiro total de las asignaturas matriculadas en el periodo académico vigente. El estudiante deja de cursar el semestre.',
    requisitos: [
      'Estar matriculado en el periodo académico vigente.',
      'Exponer y sustentar el motivo de la solicitud.',
      'Encontrarse a paz y salvo con la Universidad.'
    ],
    anexos: [
      { texto: 'Formato oficial PA-GA-4.2-FOR-14, que el sistema genera al enviar la solicitud: se imprime, se firma y se sube escaneado.' },
      { texto: 'Paz y salvo de Bibliotecas, Deportes y Recreación, Salud Integral y División Financiera.' },
      { texto: 'Cupón de Confirmación de la Intervención Psicosocial.' },
      { texto: 'Carné estudiantil o constancia de no trámite.' }
    ]
  },
  {
    titulo: 'Cancelación de Asignatura',
    icono: 'buttons/remove_selection.svg',
    ruta: '/estudiante/cancelacion-asignatura',
    descripcion: 'Retiro de una o varias asignaturas del periodo académico vigente. El resto de la matrícula permanece activa.',
    requisitos: [
      'No cursar la asignatura en calidad de repitente.',
      'Que la asignatura no sea co-requisito de otra en curso.',
      'Contar con nota promedio igual o superior a 3.0.'
    ],
    anexos: [
      { texto: 'Formato oficial PA-GA-4.2-FOR-9, que el sistema genera al enviar la solicitud: se imprime, se firma y se sube escaneado.' },
      { texto: 'Además del formato no se exigen otros documentos; solo se requiere indicar el motivo.' },
      { texto: 'Soporte que respalde el motivo, de forma opcional.' }
    ]
  },
  {
    titulo: 'Examen Supletorio',
    icono: 'buttons/historial.svg',
    ruta: '/estudiante/examen-supletorio',
    descripcion: 'Autorización para presentar en fecha distinta un examen parcial, final o de habilitación no presentado en la fecha programada.',
    requisitos: [
      'Justificar la inasistencia o acreditar cruce con otro examen.',
      'Contar con la firma del docente que orienta la asignatura.'
    ],
    anexos: [
      { texto: 'Formato oficial PM-FO-4-FOR-23, que el sistema genera al enviar la solicitud: se imprime, lo firman el estudiante y el docente de la asignatura (y el de la asignatura cruzada, si hay cruce) y se sube escaneado.' },
      { texto: 'Soporte de la justificación, solo si la causa no es un cruce de exámenes.' }
    ]
  }
];
