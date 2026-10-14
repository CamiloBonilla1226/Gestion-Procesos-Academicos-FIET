import {
  campoObservacion,
  categoriaEstado,
  claseInsigniaEtiquetaEstudiante,
  esEtiqueta,
  etiquetaCampoObservacion,
  textoDecisionDecano
} from './etiquetas-academicas';

describe('etiquetas academicas', () => {
  it('la insignia del Estudiante sale solo de su etiqueta', () => {
    expect(claseInsigniaEtiquetaEstudiante('Aprobada')).toBe('text-bg-success');
    expect(claseInsigniaEtiquetaEstudiante('Rechazada')).toBe('text-bg-danger');
    expect(claseInsigniaEtiquetaEstudiante('Pendiente de pago')).toBe('text-bg-warning');
    expect(claseInsigniaEtiquetaEstudiante('En verificación de pago')).toBe('text-bg-warning');
    expect(claseInsigniaEtiquetaEstudiante('En trámite')).toBe('text-bg-secondary');
  });

  it('compara etiquetas sin importar mayusculas ni espacios', () => {
    expect(esEtiqueta(' aprobada ', 'Aprobada')).toBeTrue();
    expect(esEtiqueta(null, 'Aprobada')).toBeFalse();
  });

  it('clasifica cada etiqueta de estado para su icono', () => {
    expect(categoriaEstado('Pendiente')).toBe('pendiente');
    expect(categoriaEstado('En Gestión')).toBe('gestion');
    expect(categoriaEstado('En trámite')).toBe('gestion');
    expect(categoriaEstado('Pendiente de Verificación')).toBe('gestion');
    expect(categoriaEstado('En verificación de pago')).toBe('gestion');
    expect(categoriaEstado('Aprobada por el Decano')).toBe('aprobada');
    expect(categoriaEstado('Rechazada por el Decano')).toBe('rechazada');
    expect(categoriaEstado('Respondida')).toBe('aprobada');
    expect(categoriaEstado('Aprobada')).toBe('aprobada');
    expect(categoriaEstado('Rechazada')).toBe('rechazada');
    expect(categoriaEstado('Pendiente de pago')).toBe('pago');
    expect(categoriaEstado('Enviar Recibo')).toBe('notificar');
    expect(categoriaEstado('Pendiente Notificar')).toBe('notificar');
    expect(categoriaEstado('Recibo Pagado')).toBe('gestion');
    expect(categoriaEstado('Otra cosa')).toBe('otro');
    expect(categoriaEstado(null)).toBe('otro');
  });

  it('traduce la decision del Decano del resumen a texto legible', () => {
    expect(textoDecisionDecano('APROBADA')).toBe('Aprobada');
    expect(textoDecisionDecano('RECHAZADA')).toBe('Rechazada');
    expect(textoDecisionDecano('APROBADA_PARCIAL')).toBe('Aprobada parcial');
    expect(textoDecisionDecano(null)).toBe('—');
    expect(textoDecisionDecano('OTRA')).toBe('—');
  });

  it('define el campo de observacion de cada accion', () => {
    expect(etiquetaCampoObservacion(campoObservacion('REMITIR_DECANO')!)).toBe('Observaciones para el Decano');
    expect(campoObservacion('REMITIR_DECANO')!.obligatoria).toBeFalse();
    expect(campoObservacion('REMITIR_DECANO')!.aviso).toBeNull();
    expect(etiquetaCampoObservacion(campoObservacion('RECHAZAR_FUNCIONARIO')!)).toBe('Observación solo para el estudiante (se entrega como respuesta)');
    expect(campoObservacion('RECHAZAR_FUNCIONARIO')!.obligatoria).toBeTrue();
    expect(etiquetaCampoObservacion(campoObservacion('RECHAZAR_DECANO')!)).toBe('Observación (opcional)');
    expect(campoObservacion('RECHAZAR_DECANO')!.aviso).toBe('Esta observación solo será visible para el Funcionario Académico.');
    expect(campoObservacion('RECHAZAR_DECANO')!.obligatoria).toBeFalse();
    expect(etiquetaCampoObservacion(campoObservacion('ENVIAR_RESPUESTA')!)).toBe('Observación solo para el estudiante (se entrega como respuesta)');
    expect(campoObservacion('ENVIAR_RESPUESTA')!.obligatoria).toBeFalse();
    expect(etiquetaCampoObservacion(campoObservacion('RECHAZAR_COMPROBANTE')!)).toBe('Observación solo para el estudiante (se entrega como respuesta)');
    expect(campoObservacion('RECHAZAR_COMPROBANTE')!.obligatoria).toBeTrue();
    expect(campoObservacion('ENVIAR_RECIBO')).toBeNull();
    expect(campoObservacion(null)).toBeNull();
  });
});
