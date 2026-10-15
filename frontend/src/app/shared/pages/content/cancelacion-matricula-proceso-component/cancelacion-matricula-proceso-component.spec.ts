import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpErrorResponse, provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { Subject, of, throwError } from 'rxjs';
import { CancelacionMatriculaProcesoComponent } from './cancelacion-matricula-proceso-component';
import { CancelacionMatriculaService } from '../../../../core/services/cancelacion-matricula-service';
import { SolicitudAcademicaService } from '../../../../core/services/solicitud-academica-service';
import { CatalogoAcademicoService } from '../../../../core/services/catalogo-academico-service';
import { ErrorHandlerService } from '../../../../core/services/error-handler-service';
import { ToastService } from '../../../../core/services/toast-service';
import { SolicitudAcademicaDetalleDTORespuesta } from '../../../../core/models/SolicitudAcademica/DTOResponse/SolicitudAcademicaDetalleDTORespuesta';
import { AnexoAcademicoDTORespuesta } from '../../../../core/models/SolicitudAcademica/DTOResponse/AnexoAcademicoDTORespuesta';
import { CancelacionMatriculaDetalleDTORespuesta } from '../../../../core/models/CancelacionMatricula/DTOResponse/CancelacionMatriculaDetalleDTORespuesta';
import { InputAnexoUploadComponent } from '../../../inputs/input-anexo-upload-component/input-anexo-upload-component';
import { environment } from '../../../../../enviroments/environment';
import { NOTA_SIMCA } from '../../../../core/constantes/textos-cancelacion-matricula';

function solicitud(acciones: string[], tieneResolucion = false, etapaCodigo = 'RADICADA'): SolicitudAcademicaDetalleDTORespuesta {
  return {
    uuidSolicitudAcademica: 's-1', radicado: '2026-CM-0001', uuidTipoSolicitudAcademica: 't',
    tipoSolicitud: 'Cancelación de Matrícula', fechaCreacion: '2026-10-08T10:00:00', etiqueta: 'Pendiente', etapaCodigo,
    estudiante: {
      uuidUsuario: 'e', nombres: 'María', apellidos: 'Pérez', codigoEstudiantil: '1', programaAcademico: 'P',
      semestre: '1', correoElectronico: 'm@u.co'
    },
    anexos: [], tieneResolucion, puedeDescargarResolucion: tieneResolucion, accionesDisponibles: acciones
  };
}

const DETALLE: CancelacionMatriculaDetalleDTORespuesta = {
  solicitud: solicitud([]),
  motivoCancelacion: 'Motivos de salud',
  asignaturas: [
    { uuidAsignaturaSolicitud: 'as-1', codigoAsignatura: 'A1', nombreAsignatura: 'Cálculo', numeroFaltas: null, nota: null, situacionMatricula: null, situacionCancelar: null },
    { uuidAsignaturaSolicitud: 'as-2', codigoAsignatura: 'A2', nombreAsignatura: 'Física', numeroFaltas: '2', nota: '3.5',
      situacionMatricula: { uuidSituacionAcademica: 'r1', codigo: 'R1', nombre: 'Aprobada' }, situacionCancelar: null }
  ]
};

describe('CancelacionMatriculaProcesoComponent', () => {
  let cancelaciones: jasmine.SpyObj<CancelacionMatriculaService>;
  let solicitudes: jasmine.SpyObj<SolicitudAcademicaService>;
  let catalogo: jasmine.SpyObj<CatalogoAcademicoService>;
  let errores: jasmine.SpyObj<ErrorHandlerService>;
  let toast: jasmine.SpyObj<ToastService>;
  let recargar: jasmine.Spy;
  const entrada = { reset: jasmine.createSpy('reset') } as unknown as InputAnexoUploadComponent;

  beforeEach(() => {
    cancelaciones = jasmine.createSpyObj<CancelacionMatriculaService>('CancelacionMatriculaService', [
      'getDetalle', 'rechazarPorFuncionario', 'remitirADecano', 'enviarRespuesta', 'aprobarPorDecano', 'rechazarPorDecano'
    ]);
    solicitudes = jasmine.createSpyObj<SolicitudAcademicaService>('SolicitudAcademicaService', ['adjuntarResolucion', 'adjuntarAnexo']);
    catalogo = jasmine.createSpyObj<CatalogoAcademicoService>('CatalogoAcademicoService', ['getSituaciones', 'getTiposAnexoPorTipo']);
    errores = jasmine.createSpyObj<ErrorHandlerService>('ErrorHandlerService', ['handleError']);
    toast = jasmine.createSpyObj<ToastService>('ToastService', ['showSuccess', 'showError']);
    recargar = jasmine.createSpy('recargar');
    cancelaciones.getDetalle.and.returnValue(of(DETALLE));
    catalogo.getTiposAnexoPorTipo.and.returnValue(of([
      { uuidTipoAnexoAcademico: 'u-paz', uuidTipoSolicitudAcademica: 't', nombre: 'Paz y salvo - División Financiera', formatosPermitidos: 'pdf', obligatorio: true },
      { uuidTipoAnexoAcademico: 'u-adicional', uuidTipoSolicitudAcademica: 't', nombre: 'Documento adicional de la respuesta', formatosPermitidos: 'pdf', obligatorio: false }
    ]));
    catalogo.getSituaciones.and.returnValue(of([
      { uuidSituacionAcademica: 'r0', codigo: 'R0', nombre: 'No cursada' },
      { uuidSituacionAcademica: 'r1', codigo: 'R1', nombre: 'Aprobada' }
    ]));
    TestBed.configureTestingModule({
      imports: [CancelacionMatriculaProcesoComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideNoopAnimations(),
        { provide: CancelacionMatriculaService, useValue: cancelaciones },
        { provide: SolicitudAcademicaService, useValue: solicitudes },
        { provide: CatalogoAcademicoService, useValue: catalogo },
        { provide: ErrorHandlerService, useValue: errores },
        { provide: ToastService, useValue: toast }
      ]
    });
  });

  function crear(datos: SolicitudAcademicaDetalleDTORespuesta, rol = 'FUNCIONARIO'): ComponentFixture<CancelacionMatriculaProcesoComponent> {
    const fixture = TestBed.createComponent(CancelacionMatriculaProcesoComponent);
    fixture.componentRef.setInput('uuidSolicitud', 's-1');
    fixture.componentRef.setInput('solicitud', datos);
    fixture.componentRef.setInput('rol', rol);
    fixture.componentRef.setInput('recargar', recargar);
    fixture.detectChanges();
    return fixture;
  }

  function botones(fixture: ComponentFixture<CancelacionMatriculaProcesoComponent>): string[] {
    return Array.from(fixture.nativeElement.querySelectorAll('[data-accion]')).map((b: any) => b.getAttribute('data-accion'));
  }

  it('muestra los datos propios de la cancelacion', () => {
    const fixture = crear(solicitud([]), 'ESTUDIANTE');
    expect(cancelaciones.getDetalle).toHaveBeenCalledWith('s-1');
    expect(fixture.nativeElement.querySelector('[data-motivo]').textContent).toContain('Motivos de salud');
    const filas = fixture.nativeElement.querySelectorAll('tbody tr');
    expect(filas.length).toBe(2);
    expect(filas[1].textContent).toContain('3.5');
    expect(filas[1].textContent).toContain('R1 - Aprobada');
    expect(filas[0].textContent).toContain('—');
    expect(fixture.nativeElement.querySelector('[data-acciones]')).toBeNull();
    expect(fixture.nativeElement.querySelector('[data-tabla-asignaturas] input, [data-tabla-asignaturas] select')).toBeNull();
  });

  it('el Funcionario y el Decano ven la nota de SIMCA encima de la tabla de asignaturas', () => {
    for (const rol of ['FUNCIONARIO', 'DECANO']) {
      const fixture = crear(solicitud(rol === 'FUNCIONARIO' ? ['REMITIR_DECANO'] : [], false, 'EN_REVISION_DECANO'), rol);
      const nota = fixture.nativeElement.querySelector('[data-nota="simca"]') as HTMLElement;
      expect(nota.textContent!.trim()).toBe(NOTA_SIMCA);
      expect(nota.textContent!.trim()).toBe('NOTA: La información de faltas y notas corresponde a la registrada en el Sistema Integrado de Matrícula y Control Académico – SIMCA a la fecha en que se recibe la solicitud de cancelación.');
      expect(nota.classList).toContain('nota-aviso');
      const tabla = fixture.nativeElement.querySelector('[data-tabla-asignaturas]') as HTMLElement;
      expect(nota.compareDocumentPosition(tabla) & Node.DOCUMENT_POSITION_FOLLOWING).toBeTruthy();
      expect(nota.closest('section')).toBe(tabla.closest('section'));
      fixture.destroy();
    }
  });

  it('el Estudiante no ve la nota de SIMCA', () => {
    const fixture = crear(solicitud([]), 'ESTUDIANTE');
    expect(fixture.nativeElement.querySelector('[data-tabla-asignaturas]')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('[data-nota="simca"]')).toBeNull();
    expect(fixture.nativeElement.textContent).not.toContain('SIMCA');
  });

  it('el Funcionario solo ve los botones que trae accionesDisponibles', () => {
    expect(botones(crear(solicitud(['RECHAZAR_FUNCIONARIO', 'REMITIR_DECANO'])))).toEqual(['RECHAZAR_FUNCIONARIO', 'REMITIR_DECANO']);
  });

  it('el Decano solo ve aprobar y rechazar si vienen en accionesDisponibles', () => {
    expect(botones(crear(solicitud(['APROBAR_DECANO', 'RECHAZAR_DECANO']), 'DECANO'))).toEqual(['APROBAR_DECANO', 'RECHAZAR_DECANO']);
    expect(botones(crear(solicitud(['APROBAR_DECANO']), 'DECANO'))).toEqual(['APROBAR_DECANO']);
  });

  it('no pinta acciones ajenas al proceso ni abre una accion que no esta disponible', () => {
    const fixture = crear(solicitud(['ENVIAR_RECIBO', 'ENVIAR_RESPUESTA']));
    expect(botones(fixture)).toEqual(['ENVIAR_RESPUESTA']);
    fixture.componentInstance.abrir('RECHAZAR_FUNCIONARIO');
    expect(fixture.componentInstance.dialogoVisible).toBeFalse();
  });

  it('el boton abre el dialogo de confirmacion y confirmar sin datos no envia nada', () => {
    const fixture = crear(solicitud(['RECHAZAR_FUNCIONARIO']));
    (fixture.nativeElement.querySelector('[data-accion="RECHAZAR_FUNCIONARIO"] button') as HTMLButtonElement).click();
    fixture.detectChanges();
    const componente = fixture.componentInstance;
    expect(componente.dialogoVisible).toBeTrue();
    expect(componente.tituloDialogo).toBe('Rechazar la solicitud');
    expect(document.querySelector('[data-dialogo]')).toBeTruthy();
    componente.confirmar();
    expect(componente.errores['observacion']).toBeTruthy();
    expect(componente.errores['resolucion']).toBeTruthy();
    expect(solicitudes.adjuntarResolucion).not.toHaveBeenCalled();
    expect(cancelaciones.rechazarPorFuncionario).not.toHaveBeenCalled();
  });

  it('rechazar sube primero el escaneo, luego rechaza y recarga', () => {
    const orden: string[] = [];
    solicitudes.adjuntarResolucion.and.callFake(() => { orden.push('resolucion'); return of({ uuidSolicitudAcademica: 's-1', nombreArchivo: 'r.pdf', fechaSubida: '' }); });
    cancelaciones.rechazarPorFuncionario.and.callFake(() => { orden.push('rechazar'); return of(DETALLE); });
    const componente = crear(solicitud(['RECHAZAR_FUNCIONARIO'])).componentInstance;
    componente.abrir('RECHAZAR_FUNCIONARIO');
    componente.observacion = 'Faltan paz y salvos vigentes';
    componente.alSeleccionarResolucion(new File(['%PDF-'], 'resolucion.pdf'), entrada);
    componente.confirmar();
    expect(orden).toEqual(['resolucion', 'rechazar']);
    expect(cancelaciones.rechazarPorFuncionario).toHaveBeenCalledWith('s-1', { observacion: 'Faltan paz y salvos vigentes' });
    expect(recargar).toHaveBeenCalled();
    expect(componente.dialogoVisible).toBeFalse();
  });

  it('con el escaneo ya cargado no exige subirlo de nuevo', () => {
    cancelaciones.rechazarPorFuncionario.and.returnValue(of(DETALLE));
    const componente = crear(solicitud(['RECHAZAR_FUNCIONARIO'], true)).componentInstance;
    componente.abrir('RECHAZAR_FUNCIONARIO');
    componente.observacion = 'No cumple';
    componente.confirmar();
    expect(solicitudes.adjuntarResolucion).not.toHaveBeenCalled();
    expect(cancelaciones.rechazarPorFuncionario).toHaveBeenCalled();
  });

  it('el escaneo solo admite PDF de hasta 5 MB', () => {
    const componente = crear(solicitud(['ENVIAR_RESPUESTA'])).componentInstance;
    componente.abrir('ENVIAR_RESPUESTA');
    componente.alSeleccionarResolucion(new File([new Uint8Array(5242881)], 'r.pdf'), entrada);
    expect(componente.archivoResolucion).toBeNull();
    expect(componente.errores['resolucion']).toContain('5 MB');
    componente.alSeleccionarResolucion(new File(['x'], 'r.png'), entrada);
    expect(componente.errores['resolucion']).toContain('pdf');
  });

  it('remitir acepta faltas y nota de texto y rechaza las de mas de 20 caracteres', () => {
    cancelaciones.remitirADecano.and.returnValue(of(DETALLE));
    const fixture = crear(solicitud(['REMITIR_DECANO']));
    const componente = fixture.componentInstance;
    componente.evaluaciones['as-1'] = { faltas: 'f'.repeat(21), nota: 'Sin nota', situacion: 'r0', cancelar: null };
    componente.abrir('REMITIR_DECANO');
    expect(componente.errores['faltas-as-1']).toContain('20');
    expect(componente.errores['nota-as-1']).toBeUndefined();
    expect(componente.dialogoVisible).toBeFalse();
    componente.evaluaciones['as-1'] = { faltas: 'No registra', nota: 'Sin nota', situacion: 'r0', cancelar: null };
    componente.abrir('REMITIR_DECANO');
    fixture.detectChanges();
    expect(componente.dialogoVisible).toBeTrue();
    expect(document.querySelectorAll('[data-campo-observacion]').length).toBe(1);
    expect(document.querySelector('[data-campo-observacion]')?.textContent).toContain('Observaciones para el Decano');
    expect(document.querySelector('[data-dialogo] [data-evaluacion]')).toBeNull();
    componente.observacion = ' Para revisar ';
    componente.confirmar();
    expect(cancelaciones.remitirADecano).toHaveBeenCalledWith('s-1', {
      observacion: 'Para revisar',
      evaluaciones: [
        { asignaturaSolicitudUuid: 'as-1', numeroFaltas: 'No registra', nota: 'Sin nota', situacionMatriculaUuid: 'r0', situacionCancelarUuid: null },
        { asignaturaSolicitudUuid: 'as-2', numeroFaltas: '2', nota: '3.5', situacionMatriculaUuid: 'r1', situacionCancelarUuid: null }
      ]
    });
  });

  it('remitir valida faltas y nota si llegan, deja la situacion opcional y envia los numeros', () => {
    cancelaciones.remitirADecano.and.returnValue(of(DETALLE));
    const componente = crear(solicitud(['REMITIR_DECANO'])).componentInstance;
    expect(catalogo.getSituaciones).toHaveBeenCalled();
    componente.evaluaciones['as-1'] = { faltas: '-1', nota: '5.5', situacion: null, cancelar: null };
    componente.abrir('REMITIR_DECANO');
    expect(componente.errores['faltas-as-1']).toBeTruthy();
    expect(componente.errores['nota-as-1']).toBeTruthy();
    expect(componente.errores['situacion-as-1']).toBeUndefined();
    expect(componente.dialogoVisible).toBeFalse();
    expect(cancelaciones.remitirADecano).not.toHaveBeenCalled();

    componente.evaluaciones['as-1'] = { faltas: '4', nota: '2,8', situacion: 'r0', cancelar: null };
    componente.abrir('REMITIR_DECANO');
    componente.confirmar();
    expect(cancelaciones.remitirADecano).toHaveBeenCalledWith('s-1', {
      observacion: null,
      evaluaciones: [
        { asignaturaSolicitudUuid: 'as-1', numeroFaltas: '4', nota: '2,8', situacionMatriculaUuid: 'r0', situacionCancelarUuid: null },
        { asignaturaSolicitudUuid: 'as-2', numeroFaltas: '2', nota: '3.5', situacionMatriculaUuid: 'r1', situacionCancelarUuid: null }
      ]
    });
    expect(recargar).toHaveBeenCalled();
  });

  it('remitir sin faltas, nota ni situacion abre el dialogo y envia los campos vacios como nulos', () => {
    cancelaciones.remitirADecano.and.returnValue(of(DETALLE));
    const componente = crear(solicitud(['REMITIR_DECANO'])).componentInstance;
    componente.evaluaciones['as-1'] = { faltas: '', nota: '  ', situacion: null, cancelar: null };
    componente.abrir('REMITIR_DECANO');
    expect(componente.errores).toEqual({});
    expect(componente.dialogoVisible).toBeTrue();
    componente.confirmar();
    expect(cancelaciones.remitirADecano.calls.mostRecent().args[1].evaluaciones[0]).toEqual({
      asignaturaSolicitudUuid: 'as-1', numeroFaltas: null, nota: null, situacionMatriculaUuid: null, situacionCancelarUuid: null
    });
  });

  it('el Decano aprueba sin registrar situaciones: solo envia la observacion', () => {
    cancelaciones.aprobarPorDecano.and.returnValue(of(DETALLE));
    const fixture = crear(solicitud(['APROBAR_DECANO'], false, 'EN_REVISION_DECANO'), 'DECANO');
    const componente = fixture.componentInstance;
    componente.abrir('APROBAR_DECANO');
    fixture.detectChanges();
    expect(componente.dialogoVisible).toBeTrue();
    expect(catalogo.getSituaciones).not.toHaveBeenCalled();
    expect(document.querySelector('[data-situacion-cancelar]')).toBeNull();
    expect(document.querySelector('[data-nota-situacion-decano]')?.textContent).toContain('registró el Funcionario Académico');
    componente.confirmar();
    expect(cancelaciones.aprobarPorDecano).toHaveBeenCalledWith('s-1', { observacion: null });
  });

  it('aprobar acepta una observacion opcional que solo ve el Funcionario', () => {
    cancelaciones.aprobarPorDecano.and.returnValue(of(DETALLE));
    const fixture = crear(solicitud(['APROBAR_DECANO'], false, 'EN_REVISION_DECANO'), 'DECANO');
    const componente = fixture.componentInstance;
    componente.abrir('APROBAR_DECANO');
    fixture.detectChanges();
    expect(document.querySelector('[data-aviso-observacion]')?.textContent).toContain('Esta observación solo será visible para el Funcionario Académico.');
    componente.observacion = 'Cumple todo';
    componente.confirmar();
    expect(cancelaciones.aprobarPorDecano.calls.mostRecent().args[1]?.observacion).toBe('Cumple todo');
  });

  it('el rechazo del Decano no exige observacion y avisa que solo la ve el Funcionario', () => {
    cancelaciones.rechazarPorDecano.and.returnValue(of(DETALLE));
    const fixture = crear(solicitud(['RECHAZAR_DECANO'], false, 'EN_REVISION_DECANO'), 'DECANO');
    const componente = fixture.componentInstance;
    componente.abrir('RECHAZAR_DECANO');
    fixture.detectChanges();
    const campo = document.querySelector('[data-campo-observacion]') as HTMLElement;
    expect(campo.textContent).not.toContain('*');
    expect(campo.querySelector('[data-aviso-observacion]')?.textContent?.trim()).toBe('Esta observación solo será visible para el Funcionario Académico.');
    componente.confirmar();
    expect(cancelaciones.rechazarPorDecano).toHaveBeenCalledWith('s-1', { observacion: null });
  });

  it('enviar la respuesta manda el mensaje para el estudiante de hasta 500 caracteres', () => {
    cancelaciones.enviarRespuesta.and.returnValue(of(DETALLE));
    const fixture = crear(solicitud(['ENVIAR_RESPUESTA'], true, 'RECHAZADA_POR_DECANO'));
    const componente = fixture.componentInstance;
    componente.abrir('ENVIAR_RESPUESTA');
    fixture.detectChanges();
    expect(document.querySelector('[data-campo-observacion]')?.textContent).toContain('Observación solo para el estudiante (se entrega como respuesta)');
    componente.observacion = 'x'.repeat(501);
    componente.confirmar();
    expect(componente.errores['observacion']).toContain('500');
    componente.observacion = 'Puede radicar de nuevo el próximo periodo';
    componente.confirmar();
    expect(cancelaciones.enviarRespuesta).toHaveBeenCalledWith('s-1', { observacion: 'Puede radicar de nuevo el próximo periodo' });
  });

  it('enviar la respuesta exige el escaneo y luego responde sin mensaje', () => {
    solicitudes.adjuntarResolucion.and.returnValue(of({ uuidSolicitudAcademica: 's-1', nombreArchivo: 'r.pdf', fechaSubida: '' }));
    cancelaciones.enviarRespuesta.and.returnValue(of(DETALLE));
    const componente = crear(solicitud(['ENVIAR_RESPUESTA'], false, 'APROBADA_POR_DECANO')).componentInstance;
    componente.abrir('ENVIAR_RESPUESTA');
    componente.confirmar();
    expect(componente.errores['resolucion']).toBeTruthy();
    expect(cancelaciones.enviarRespuesta).not.toHaveBeenCalled();
    componente.alSeleccionarResolucion(new File(['%PDF-'], 'resolucion.pdf'), entrada);
    componente.confirmar();
    expect(solicitudes.adjuntarResolucion).toHaveBeenCalledWith('s-1', jasmine.any(File));
    expect(cancelaciones.enviarRespuesta).toHaveBeenCalledWith('s-1', { observacion: null });
    expect(recargar).toHaveBeenCalled();
  });

  it('enviar la respuesta sube la Resolucion, despues el documento adicional y al final responde', () => {
    const orden: string[] = [];
    solicitudes.adjuntarResolucion.and.callFake(() => { orden.push('resolucion'); return of({ uuidSolicitudAcademica: 's-1', nombreArchivo: 'r.pdf', fechaSubida: '' }); });
    solicitudes.adjuntarAnexo.and.callFake(() => { orden.push('adicional'); return of({} as AnexoAcademicoDTORespuesta); });
    cancelaciones.enviarRespuesta.and.callFake(() => { orden.push('responder'); return of(DETALLE); });
    const fixture = crear(solicitud(['ENVIAR_RESPUESTA'], false, 'APROBADA_POR_DECANO'));
    const componente = fixture.componentInstance;
    componente.abrir('ENVIAR_RESPUESTA');
    fixture.detectChanges();
    expect(document.querySelector('[data-campo-documento-adicional]')?.textContent).toContain('Documento adicional (opcional)');
    componente.alSeleccionarResolucion(new File(['%PDF-'], 'resolucion.pdf'), entrada);
    const archivo = new File(['%PDF-'], 'acta.pdf');
    componente.archivoAdicional = archivo;
    componente.confirmar();
    expect(orden).toEqual(['resolucion', 'adicional', 'responder']);
    expect(catalogo.getTiposAnexoPorTipo).toHaveBeenCalledWith('t');
    expect(solicitudes.adjuntarAnexo).toHaveBeenCalledWith('s-1', { archivo, tipoAnexo: 'u-adicional' });
    expect(recargar).toHaveBeenCalled();
  });

  it('el rechazo del Funcionario tambien acepta el documento adicional y sin el no lo sube', () => {
    solicitudes.adjuntarAnexo.and.returnValue(of({} as AnexoAcademicoDTORespuesta));
    cancelaciones.rechazarPorFuncionario.and.returnValue(of(DETALLE));
    const componente = crear(solicitud(['RECHAZAR_FUNCIONARIO'], true)).componentInstance;
    componente.abrir('RECHAZAR_FUNCIONARIO');
    expect(componente.pideDocumentoAdicional).toBeTrue();
    componente.observacion = 'Faltan paz y salvos';
    componente.confirmar();
    expect(catalogo.getTiposAnexoPorTipo).not.toHaveBeenCalled();
    expect(solicitudes.adjuntarAnexo).not.toHaveBeenCalled();
    expect(cancelaciones.rechazarPorFuncionario).toHaveBeenCalled();
  });

  it('el documento adicional no se ofrece al remitir ni al Decano', () => {
    const remitir = crear(solicitud(['REMITIR_DECANO'])).componentInstance;
    remitir.abrir('REMITIR_DECANO');
    expect(remitir.pideDocumentoAdicional).toBeFalse();
    const decano = crear(solicitud(['APROBAR_DECANO']), 'DECANO').componentInstance;
    decano.abrir('APROBAR_DECANO');
    expect(decano.pideDocumentoAdicional).toBeFalse();
  });

  it('si falla la subida del documento adicional avisa al funcionario y no envia la respuesta', () => {
    const falla = new HttpErrorResponse({ status: 500, error: { codigoError: 4, mensaje: 'El anexo Documento adicional de la respuesta no se pudo validar...' } });
    solicitudes.adjuntarAnexo.and.returnValue(throwError(() => falla));
    const fixture = crear(solicitud(['ENVIAR_RESPUESTA'], true, 'APROBADA_POR_DECANO'));
    const componente = fixture.componentInstance;
    componente.abrir('ENVIAR_RESPUESTA');
    componente.archivoAdicional = new File(['%PDF-'], 'acta.pdf');
    componente.confirmar();
    fixture.detectChanges();
    expect(cancelaciones.enviarRespuesta).not.toHaveBeenCalled();
    expect(errores.handleError).toHaveBeenCalledWith(falla, 'Error', 'No se pudo subir el documento adicional');
    expect(componente.errorAdicional).toBe('El anexo Documento adicional de la respuesta no se pudo validar...');
    expect(document.querySelector('[data-error="documento-adicional"]')?.textContent).toContain('no se pudo validar');
    expect(componente.dialogoVisible).toBeTrue();
    expect(recargar).not.toHaveBeenCalled();
  });

  it('si la accion falla despues de subir el documento el aviso es el de la accion', () => {
    solicitudes.adjuntarAnexo.and.returnValue(of({} as AnexoAcademicoDTORespuesta));
    const falla = new HttpErrorResponse({ status: 500, error: { codigoError: 4, mensaje: 'Regla violada' } });
    cancelaciones.enviarRespuesta.and.returnValue(throwError(() => falla));
    const componente = crear(solicitud(['ENVIAR_RESPUESTA'], true, 'APROBADA_POR_DECANO')).componentInstance;
    componente.abrir('ENVIAR_RESPUESTA');
    componente.archivoAdicional = new File(['%PDF-'], 'acta.pdf');
    componente.confirmar();
    expect(errores.handleError).toHaveBeenCalledWith(falla, 'Error', 'No se pudo completar la acción');
    expect(componente.errorAdicional).toBe('');
    expect(componente.hayDocumentoAdicional).toBeTrue();
  });

  it('si el catalogo no tiene el tipo del documento adicional no envia la respuesta', () => {
    catalogo.getTiposAnexoPorTipo.and.returnValue(of([]));
    const componente = crear(solicitud(['ENVIAR_RESPUESTA'], true, 'APROBADA_POR_DECANO')).componentInstance;
    componente.abrir('ENVIAR_RESPUESTA');
    componente.archivoAdicional = new File(['%PDF-'], 'acta.pdf');
    componente.confirmar();
    expect(solicitudes.adjuntarAnexo).not.toHaveBeenCalled();
    expect(cancelaciones.enviarRespuesta).not.toHaveBeenCalled();
    expect(errores.handleError).toHaveBeenCalled();
    expect(componente.dialogoVisible).toBeTrue();
  });

  it('no permite un doble envio mientras la accion esta en curso', () => {
    const respuesta = new Subject<CancelacionMatriculaDetalleDTORespuesta>();
    cancelaciones.rechazarPorDecano.and.returnValue(respuesta);
    const componente = crear(solicitud(['RECHAZAR_DECANO']), 'DECANO').componentInstance;
    componente.abrir('RECHAZAR_DECANO');
    componente.observacion = 'No procede';
    componente.confirmar();
    componente.confirmar();
    expect(cancelaciones.rechazarPorDecano).toHaveBeenCalledTimes(1);
    expect(componente.enviando).toBeTrue();
    componente.cerrar(false);
    expect(componente.dialogoVisible).toBeTrue();
    respuesta.next(DETALLE);
    respuesta.complete();
    expect(componente.enviando).toBeFalse();
  });

  it('un error con codigoError deja el dialogo abierto y no recarga', () => {
    const error = new HttpErrorResponse({ status: 500, error: { codigoError: '4', mensaje: 'La acción REMITIR_DECANO no está permitida...' } });
    cancelaciones.rechazarPorDecano.and.returnValue(throwError(() => error));
    const componente = crear(solicitud(['RECHAZAR_DECANO']), 'DECANO').componentInstance;
    componente.abrir('RECHAZAR_DECANO');
    componente.observacion = 'No procede';
    componente.confirmar();
    expect(errores.handleError).toHaveBeenCalledWith(error, 'Error', 'No se pudo completar la acción');
    expect(componente.dialogoVisible).toBeTrue();
    expect(componente.enviando).toBeFalse();
    expect(recargar).not.toHaveBeenCalled();
  });

  it('un 400 con mapa de campos se muestra en el dialogo', () => {
    cancelaciones.remitirADecano.and.returnValue(throwError(() => new HttpErrorResponse({
      status: 400, error: { 'evaluaciones[0].nota': 'debe ser menor o igual a 5.0' }
    })));
    const fixture = crear(solicitud(['REMITIR_DECANO']));
    const componente = fixture.componentInstance;
    componente.evaluaciones['as-1'] = { faltas: '1', nota: '3.0', situacion: 'r0', cancelar: null };
    componente.abrir('REMITIR_DECANO');
    componente.confirmar();
    fixture.detectChanges();
    expect(componente.erroresServidor).toEqual(['evaluaciones[0].nota: debe ser menor o igual a 5.0']);
    expect(document.querySelector('[data-error="servidor"]')?.textContent).toContain('debe ser menor o igual a 5.0');
  });

  it('el Estudiante no ve las columnas sin ningun dato', () => {
    cancelaciones.getDetalle.and.returnValue(of({
      ...DETALLE,
      asignaturas: DETALLE.asignaturas.map(a => ({ ...a, numeroFaltas: null, nota: null, situacionMatricula: null }))
    }));
    const fixture = crear(solicitud([]), 'ESTUDIANTE');
    const encabezados = Array.from(fixture.nativeElement.querySelectorAll('thead th')).map((th: any) => th.textContent.trim());
    expect(encabezados).toEqual(['Código', 'Asignatura']);
    expect(fixture.nativeElement.querySelector('tbody').textContent).not.toContain('—');
  });

  it('el Funcionario sigue viendo todas las columnas aunque esten vacias', () => {
    cancelaciones.getDetalle.and.returnValue(of({
      ...DETALLE,
      asignaturas: DETALLE.asignaturas.map(a => ({ ...a, numeroFaltas: null, nota: null, situacionMatricula: null }))
    }));
    const fixture = crear(solicitud([]));
    expect(fixture.componentInstance.columnas.length).toBe(6);
    const grupos = Array.from(fixture.nativeElement.querySelectorAll('thead tr')).map((tr: any) =>
      Array.from(tr.querySelectorAll('th')).map((th: any) => [th.textContent.trim(), th.getAttribute('colspan'), th.getAttribute('rowspan')]));
    expect(grupos).toEqual([
      [['Código', null, '3'], ['Asignatura', null, '3'], ['Espacio a diligenciar por la Facultad a la que pertenece el estudiante', '4', null]],
      [['No. Faltas', null, '2'], ['Nota', null, '2'], ['Situación Académica de la(s) Asignatura(s)', '2', null]],
      [['En la Matrícula', null, null], ['Al Cancelar', null, null]]
    ]);
  });

  it('cada dialogo usa el texto de su accion en el boton', () => {
    const fixture = crear(solicitud(['RECHAZAR_FUNCIONARIO', 'REMITIR_DECANO']));
    fixture.componentInstance.abrir('RECHAZAR_FUNCIONARIO');
    fixture.detectChanges();
    expect((document.querySelector('[data-boton-dialogo] button')?.textContent ?? '').trim()).toBe('Rechazar solicitud');
    fixture.componentInstance.cerrar(false);
    fixture.componentInstance.evaluaciones['as-1'] = { faltas: '0', nota: '4.0', situacion: 'r0', cancelar: null };
    fixture.componentInstance.abrir('REMITIR_DECANO');
    fixture.detectChanges();
    expect((document.querySelector('[data-boton-dialogo] button')?.textContent ?? '').trim()).toBe('Remitir al Decano');
    expect(fixture.componentInstance.textoBotonDialogo).toBe('Remitir al Decano');
  });

  it('los rechazos van en contorno rojo y el resto en navy, en una tarjeta de acciones', () => {
    const fixture = crear(solicitud(['RECHAZAR_FUNCIONARIO', 'REMITIR_DECANO']));
    const rechazar = fixture.nativeElement.querySelector('[data-acciones] [data-accion="RECHAZAR_FUNCIONARIO"] button') as HTMLButtonElement;
    const remitir = fixture.nativeElement.querySelector('[data-acciones] [data-accion="REMITIR_DECANO"] button') as HTMLButtonElement;
    expect(rechazar.classList).toContain('contorno');
    expect(rechazar.textContent?.trim()).toBe('Rechazar solicitud');
    expect(remitir.classList).not.toContain('contorno');
    expect(remitir.textContent?.trim()).toBe('Remitir al Decano');
    expect(fixture.nativeElement.querySelector('[data-acciones]').classList).toContain('tarjeta-acciones');
  });

  it('el dialogo resume la solicitud, ofrece Cancelar y el Decano ve la nota al aprobar', () => {
    const fixture = crear(solicitud(['APROBAR_DECANO', 'RECHAZAR_DECANO'], false, 'EN_REVISION_DECANO'), 'DECANO');
    fixture.componentInstance.abrir('APROBAR_DECANO');
    fixture.detectChanges();
    const resumen = Array.from(document.querySelectorAll('[data-resumen-solicitud] dt')).map(dt => dt.textContent?.trim());
    expect(resumen).toEqual(['Estudiante', 'Código estudiantil', 'Tipo de proceso', 'Número de radicado']);
    expect(document.querySelector('[data-resumen-solicitud]')?.textContent).toContain('2026-CM-0001');
    expect(document.querySelector('[data-nota-aprobar]')?.textContent).toContain('Al aprobar, quedará registrada tu decisión.');
    expect(document.querySelector('[data-boton-cancelar] button')?.textContent?.trim()).toBe('Cancelar');

    (document.querySelector('[data-boton-cancelar] button') as HTMLButtonElement).click();
    fixture.detectChanges();
    expect(fixture.componentInstance.dialogoVisible).toBeFalse();
  });

  it('el Funcionario que puede remitir diligencia la tabla en linea y al cancelar queda de solo lectura', () => {
    const fixture = crear(solicitud(['RECHAZAR_FUNCIONARIO', 'REMITIR_DECANO']));
    const tabla = fixture.nativeElement.querySelector('[data-tabla-asignaturas]') as HTMLElement;
    expect(tabla.getAttribute('data-editable')).toBe('si');
    const fila = tabla.querySelector('[data-fila="as-1"]') as HTMLElement;
    expect(fila.querySelectorAll('input').length).toBe(2);
    expect(fila.querySelectorAll('select').length).toBe(2);
    const celdas = fila.querySelectorAll('td');
    expect(celdas[0].textContent?.trim()).toBe('A1');
    for (const campo of ['situacion', 'cancelar']) {
      const opciones = Array.from(fila.querySelectorAll(`[data-campo="${campo}"] option`)).map(o => o.textContent?.trim());
      expect(opciones).toEqual(['—', 'R0 - No cursada', 'R1 - Aprobada']);
    }

    const faltas = fila.querySelector('[data-campo="faltas"]') as HTMLInputElement;
    faltas.value = 'No registra';
    faltas.dispatchEvent(new Event('input'));
    const situacion = fila.querySelector('[data-campo="situacion"]') as HTMLSelectElement;
    situacion.value = 'r0';
    situacion.dispatchEvent(new Event('change'));
    expect(fixture.componentInstance.evaluaciones['as-1'].faltas).toBe('No registra');
    expect(fixture.componentInstance.evaluaciones['as-1'].situacion).toBe('r0');
    const cancelar = fila.querySelector('[data-campo="cancelar"]') as HTMLSelectElement;
    cancelar.value = 'r1';
    cancelar.dispatchEvent(new Event('change'));
    expect(fixture.componentInstance.evaluaciones['as-1'].cancelar).toBe('r1');
    const fila2 = tabla.querySelector('[data-fila="as-2"]') as HTMLElement;
    expect((fila2.querySelector('[data-campo="nota"]') as HTMLInputElement).value).toBe('3.5');
  });

  it('remitir envia la situacion al cancelar que registre el Funcionario', () => {
    cancelaciones.remitirADecano.and.returnValue(of(DETALLE));
    const componente = crear(solicitud(['REMITIR_DECANO'])).componentInstance;
    componente.evaluaciones['as-1'] = { faltas: '0', nota: '4.0', situacion: 'r0', cancelar: 'r1' };
    componente.abrir('REMITIR_DECANO');
    componente.confirmar();
    expect(cancelaciones.remitirADecano.calls.mostRecent().args[1].evaluaciones.map(e => e.situacionCancelarUuid)).toEqual(['r1', null]);
  });

  it('remitir con la tabla vacia abre el dialogo porque los datos de la Facultad son opcionales', () => {
    const fixture = crear(solicitud(['REMITIR_DECANO']));
    (fixture.nativeElement.querySelector('[data-accion="REMITIR_DECANO"] button') as HTMLButtonElement).click();
    fixture.detectChanges();
    expect(fixture.componentInstance.dialogoVisible).toBeTrue();
    expect(fixture.nativeElement.querySelectorAll('td.con-error').length).toBe(0);
    expect(fixture.nativeElement.querySelector('[data-error="tabla"]')).toBeNull();
  });

  it('remitir con un dato invalido resalta la celda con el mensaje y no abre el dialogo', () => {
    const fixture = crear(solicitud(['REMITIR_DECANO']));
    fixture.componentInstance.evaluaciones['as-1'].nota = '9';
    (fixture.nativeElement.querySelector('[data-accion="REMITIR_DECANO"] button') as HTMLButtonElement).click();
    fixture.detectChanges();
    expect(fixture.componentInstance.dialogoVisible).toBeFalse();
    const fila = fixture.nativeElement.querySelector('[data-fila="as-1"]') as HTMLElement;
    expect(fila.querySelectorAll('td.con-error').length).toBe(1);
    expect(fila.querySelector('[data-error-celda="nota"]')?.textContent).toContain('entre 0.0 y 5.0');
    expect(fila.querySelector('[data-campo="nota"]')?.getAttribute('aria-invalid')).toBe('true');
    expect(fixture.nativeElement.querySelector('[data-error="tabla"]')).toBeTruthy();
  });

  it('el Decano ve la tabla de solo lectura con el encabezado agrupado', () => {
    const fixture = crear(solicitud(['APROBAR_DECANO'], false, 'EN_REVISION_DECANO'), 'DECANO');
    const tabla = fixture.nativeElement.querySelector('[data-tabla-asignaturas]') as HTMLElement;
    expect(tabla.getAttribute('data-editable')).toBeNull();
    expect(tabla.querySelector('input, select')).toBeNull();
    expect(tabla.querySelectorAll('thead tr').length).toBe(3);
    expect(tabla.querySelector('[data-fila="as-2"]')?.textContent).toContain('R1 - Aprobada');
  });

  it('el mensaje para el estudiante lleva contador dentro del dialogo de respuesta', () => {
    const fixture = crear(solicitud(['ENVIAR_RESPUESTA'], true, 'APROBADA_POR_DECANO'));
    fixture.componentInstance.abrir('ENVIAR_RESPUESTA');
    fixture.componentInstance.observacion = 'Hola';
    fixture.detectChanges();
    expect(document.querySelector('[data-campo-observacion]')?.textContent).toContain('Observación solo para el estudiante (se entrega como respuesta)');
    expect(document.querySelector('[data-campo-observacion] [data-contador]')?.textContent?.trim()).toBe('4/500');
  });
});

describe('CancelacionMatriculaProcesoComponent con el catalogo real', () => {
  const URL_SITUACIONES = `${environment.apiUrl}/catalogos-academicos/situaciones`;
  let http: HttpTestingController;
  let toast: jasmine.SpyObj<ToastService>;
  let errores: ErrorHandlerService;

  beforeEach(() => {
    const cancelaciones = jasmine.createSpyObj<CancelacionMatriculaService>('CancelacionMatriculaService', ['getDetalle']);
    cancelaciones.getDetalle.and.returnValue(of(DETALLE));
    toast = jasmine.createSpyObj<ToastService>('ToastService', ['showSuccess', 'showError']);
    TestBed.configureTestingModule({
      imports: [CancelacionMatriculaProcesoComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideNoopAnimations(),
        { provide: CancelacionMatriculaService, useValue: cancelaciones },
        { provide: ToastService, useValue: toast }
      ]
    });
    http = TestBed.inject(HttpTestingController);
    errores = TestBed.inject(ErrorHandlerService);
    spyOn(errores, 'handleError').and.callThrough();
    spyOn(console, 'error');
  });

  function crearConRol(acciones: string[], etapa: string, rol: string): ComponentFixture<CancelacionMatriculaProcesoComponent> {
    const fixture = TestBed.createComponent(CancelacionMatriculaProcesoComponent);
    fixture.componentRef.setInput('uuidSolicitud', 's-1');
    fixture.componentRef.setInput('solicitud', solicitud(acciones, false, etapa));
    fixture.componentRef.setInput('rol', rol);
    fixture.componentRef.setInput('recargar', () => undefined);
    fixture.detectChanges();
    return fixture;
  }

  function clic(fixture: ComponentFixture<CancelacionMatriculaProcesoComponent>, accion: string): void {
    (fixture.nativeElement.querySelector(`[data-accion="${accion}"] button`) as HTMLButtonElement).click();
    fixture.detectChanges();
  }

  it('con rol Decano abre el dialogo de aprobar sin pedir el catalogo de situaciones', () => {
    const fixture = crearConRol(['APROBAR_DECANO', 'RECHAZAR_DECANO'], 'EN_REVISION_DECANO', 'DECANO');
    clic(fixture, 'APROBAR_DECANO');
    http.expectNone(URL_SITUACIONES);
    expect(fixture.componentInstance.dialogoVisible).toBeTrue();
    expect(document.querySelector('[data-situacion-cancelar]')).toBeNull();
    expect(errores.handleError).not.toHaveBeenCalled();
  });

  it('si el catalogo falla al remitir muestra el error y no abre el dialogo', () => {
    const fixture = crearConRol(['REMITIR_DECANO', 'RECHAZAR_FUNCIONARIO'], 'RADICADA', 'FUNCIONARIO');
    http.expectOne(URL_SITUACIONES).flush({ codigoError: '1', mensaje: 'Error interno' }, { status: 500, statusText: 'Error' });
    fixture.detectChanges();
    clic(fixture, 'REMITIR_DECANO');
    http.expectOne(URL_SITUACIONES).flush({ codigoError: '1', mensaje: 'Error interno' }, { status: 500, statusText: 'Error' });
    fixture.detectChanges();
    expect(fixture.componentInstance.dialogoVisible).toBeFalse();
    expect(fixture.componentInstance.accionActiva).toBeNull();
    expect(toast.showError).toHaveBeenCalledWith('Error', 'No se pudieron cargar las situaciones académicas. Error interno');
  });

  it('si el catalogo viene vacio al remitir muestra el error y no abre el dialogo', () => {
    const fixture = crearConRol(['REMITIR_DECANO', 'RECHAZAR_FUNCIONARIO'], 'RADICADA', 'FUNCIONARIO');
    http.expectOne(URL_SITUACIONES).flush([]);
    fixture.detectChanges();
    clic(fixture, 'REMITIR_DECANO');
    http.expectOne(URL_SITUACIONES).flush([]);
    fixture.detectChanges();
    expect(fixture.componentInstance.dialogoVisible).toBeFalse();
    expect(toast.showError).toHaveBeenCalledWith(
      'Error',
      'No se pudieron cargar las situaciones académicas. El catálogo de situaciones académicas está vacío'
    );
  });
});
