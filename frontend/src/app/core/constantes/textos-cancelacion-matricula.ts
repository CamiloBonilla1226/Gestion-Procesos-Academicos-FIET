export interface ParrafoNormativa {
  encabezado: string;
  texto: string;
  literales?: string[];
}

export const VIGENCIA_PAZ_Y_SALVOS = 'La vigencia de los PAZ Y SALVOS es de 5 días calendario a partir de la fecha de expedición.';

export const NOTA_SIMCA = 'NOTA: La información de faltas y notas corresponde a la registrada en el Sistema Integrado de Matrícula y Control Académico – SIMCA a la fecha en que se recibe la solicitud de cancelación.';

export const TITULO_NORMATIVA = 'Normativa aplicable';

export const NORMATIVA_CANCELACION_MATRICULA: ParrafoNormativa[] = [
  {
    encabezado: 'ARTÍCULO 1º:',
    texto: 'Cuando un estudiante desee CANCELAR LA MATRÍCULA, podrá solicitarlo por escrito al Decano de la respectiva facultad, hasta el ÚLTIMO DÍA DE CLASES del periodo lectivo correspondiente, exponiendo los motivos que lo inducen a ello y adjuntando el paz y salvo general de la Universidad, así como el carné estudiantil.'
  },
  {
    encabezado: 'PARÁGRAFO:',
    texto: 'El estudiante que solicite la cancelación de la matrícula deberá diligenciar previamente ante la División de Salud Integral de la Vicerrectoría de Cultura y Bienestar el cupón denominado: “Confirmación de la Intervención Psicosocial”.'
  },
  {
    encabezado: 'ARTÍCULO 2:',
    texto: 'La situación académica del estudiante que cancela su matrícula será establecida por el Decano mediante Resolución motivada, teniendo en cuenta los siguientes criterios:',
    literales: [
      'a. Se consideran aprobadas las asignaturas que tengan nota definitiva igual o superior a tres punto cero (3.0).',
      'b. Se consideran perdidas las asignaturas que en el momento de la cancelación hayan sido perdidas por faltas de asistencia.',
      'c. Se consideran perdidas las asignaturas en las que el promedio ponderado de las notas de los exámenes parciales realizados hasta el momento de la solicitud de cancelación sea inferior a dos punto cero (2.0), o que tengan nota final menor a tres punto cero (3.0).',
      'd. Se consideran como no cursadas las restantes.'
    ]
  },
  {
    encabezado: 'ARTÍCULO 3:',
    texto: 'El estudiante de primer periodo de carrera que cancele la matrícula o haya perdido en forma definitiva más del 50% de las asignaturas matriculadas en el respectivo periodo académico, sólo podrá ingresar de nuevo al programa sometiéndose al proceso de admisión vigente.'
  },
  {
    encabezado: 'ARTÍCULO 4:',
    texto: 'El estudiante que se retire del programa sin la cancelación de matrícula a que se refiere el Artículo 1 de este acuerdo, será calificado con nota cero punto cero (0.0) en los exámenes dejados de presentar, y le serán anotadas las faltas de asistencia en todas las asignaturas.'
  }
];
