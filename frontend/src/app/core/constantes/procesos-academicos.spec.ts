import {
  ACCIONES_ACADEMICAS,
  ETAPAS_ACADEMICAS,
  PROCESOS_ACADEMICOS,
  TEXTOS_ACCIONES,
  TEXTOS_ETAPAS,
  claseInsigniaEtapa,
  procesoDeTipoSolicitud,
  textoAccion,
  textoEtapa
} from './procesos-academicos';

describe('Textos de acciones y etapas', () => {
  const accionesDelContrato = [
    'RADICAR', 'RECHAZAR_FUNCIONARIO', 'REMITIR_DECANO', 'APROBAR_DECANO', 'RECHAZAR_DECANO', 'ENVIAR_RESPUESTA',
    'ENVIAR_RECIBO', 'SUBIR_COMPROBANTE', 'APROBAR_COMPROBANTE', 'RECHAZAR_COMPROBANTE'
  ];
  const etapasDelContrato = [
    'RADICADA', 'EN_REVISION_DECANO', 'APROBADA_POR_DECANO', 'RECHAZADA_POR_DECANO', 'PENDIENTE_PAGO',
    'EN_VERIFICACION_PAGO', 'APROBADA', 'RECHAZADA'
  ];

  it('las constantes coinciden con las acciones y etapas del contrato', () => {
    const acciones: string[] = Object.values(ACCIONES_ACADEMICAS);
    const etapas: string[] = Object.values(ETAPAS_ACADEMICAS);
    expect(acciones.sort()).toEqual([...accionesDelContrato].sort());
    expect(etapas.sort()).toEqual([...etapasDelContrato].sort());
  });

  it('toda accion del contrato tiene un texto legible distinto de su codigo', () => {
    for (const accion of accionesDelContrato) {
      expect(TEXTOS_ACCIONES[accion as keyof typeof TEXTOS_ACCIONES]).toBeTruthy();
      expect(textoAccion(accion)).not.toBe(accion);
    }
  });

  it('toda etapa del contrato tiene un texto legible distinto de su codigo', () => {
    for (const etapa of etapasDelContrato) {
      expect(TEXTOS_ETAPAS[etapa as keyof typeof TEXTOS_ETAPAS]).toBeTruthy();
      expect(textoEtapa(etapa)).not.toBe(etapa);
    }
  });

  it('una etapa nula o desconocida no rompe el texto', () => {
    expect(textoEtapa(null)).toBe('Etapa no determinada');
    expect(textoEtapa('OTRA')).toBe('OTRA');
    expect(textoAccion(undefined)).toBe('Acción sin registrar');
  });

  it('la insignia distingue aprobada, rechazada, pago y el resto', () => {
    expect(claseInsigniaEtapa('APROBADA')).toBe('text-bg-success');
    expect(claseInsigniaEtapa('RECHAZADA')).toBe('text-bg-danger');
    expect(claseInsigniaEtapa('PENDIENTE_PAGO')).toBe('text-bg-warning');
    expect(claseInsigniaEtapa('EN_VERIFICACION_PAGO')).toBe('text-bg-warning');
    expect(claseInsigniaEtapa('RADICADA')).toBe('text-bg-secondary');
    expect(claseInsigniaEtapa(null)).toBe('text-bg-secondary');
  });

  it('el proceso sale del nombre exacto del tipo de solicitud', () => {
    expect(procesoDeTipoSolicitud('Cancelación de Matrícula')).toBe(PROCESOS_ACADEMICOS.CANCELACION_MATRICULA);
    expect(procesoDeTipoSolicitud('Cancelación de Asignatura')).toBe(PROCESOS_ACADEMICOS.CANCELACION_ASIGNATURA);
    expect(procesoDeTipoSolicitud(' Examen Supletorio ')).toBe(PROCESOS_ACADEMICOS.EXAMEN_SUPLETORIO);
    expect(procesoDeTipoSolicitud('Cancelacion de Matricula')).toBeUndefined();
    expect(procesoDeTipoSolicitud(null)).toBeUndefined();
  });
});
