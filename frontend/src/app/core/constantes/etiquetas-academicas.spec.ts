import {
  campoObservacion,
  claseInsigniaDecisionDecano,
  claseInsigniaEtiquetaEstudiante,
  esEtiqueta,
  etiquetaCampoObservacion
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

  it('la insignia de decision del Decano solo aplica a sus dos etiquetas', () => {
    expect(claseInsigniaDecisionDecano('Aprobada por el Decano')).toBe('text-bg-success');
    expect(claseInsigniaDecisionDecano('Rechazada por el Decano')).toBe('text-bg-danger');
    expect(claseInsigniaDecisionDecano('Pendiente')).toBeNull();
  });

  it('define el campo de observacion de cada accion', () => {
    expect(etiquetaCampoObservacion(campoObservacion('REMITIR_DECANO')!)).toBe('Observación para el Decano (opcional)');
    expect(etiquetaCampoObservacion(campoObservacion('RECHAZAR_FUNCIONARIO')!)).toBe('Observación');
    expect(campoObservacion('RECHAZAR_DECANO')!.aviso).toBe('Esta observación solo será visible para el Funcionario Académico.');
    expect(campoObservacion('RECHAZAR_DECANO')!.obligatoria).toBeFalse();
    expect(etiquetaCampoObservacion(campoObservacion('ENVIAR_RESPUESTA')!)).toBe('Mensaje para el estudiante (opcional)');
    expect(campoObservacion('ENVIAR_RECIBO')).toBeNull();
    expect(campoObservacion(null)).toBeNull();
  });
});
