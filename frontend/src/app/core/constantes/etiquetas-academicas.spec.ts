import {
  campoObservacion,
  categoriaEstado,
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
    expect(categoriaEstado('Otra cosa')).toBe('otro');
    expect(categoriaEstado(null)).toBe('otro');
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
