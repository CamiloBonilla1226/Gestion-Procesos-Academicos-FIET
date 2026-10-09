import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpErrorResponse, provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { Subject, of, throwError } from 'rxjs';
import { CancelacionMatriculaProcesoComponent } from './cancelacion-matricula-proceso-component';
import { CancelacionMatriculaService } from '../../../../core/services/cancelacion-matricula-service';
import { SolicitudAcademicaService } from '../../../../core/services/solicitud-academica-service';
import { CatalogoAcademicoService } from '../../../../core/services/catalogo-academico-service';
import { ErrorHandlerService } from '../../../../core/services/error-handler-service';
import { ToastService } from '../../../../core/services/toast-service';
import { SolicitudAcademicaDetalleDTORespuesta } from '../../../../core/models/SolicitudAcademica/DTOResponse/SolicitudAcademicaDetalleDTORespuesta';
import { CancelacionMatriculaDetalleDTORespuesta } from '../../../../core/models/CancelacionMatricula/DTOResponse/CancelacionMatriculaDetalleDTORespuesta';
import { InputAnexoUploadComponent } from '../../../inputs/input-anexo-upload-component/input-anexo-upload-component';

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
    { uuidAsignaturaSolicitud: 'as-2', codigoAsignatura: 'A2', nombreAsignatura: 'Física', numeroFaltas: 2, nota: 3.5,
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
    solicitudes = jasmine.createSpyObj<SolicitudAcademicaService>('SolicitudAcademicaService', ['adjuntarResolucion']);
    catalogo = jasmine.createSpyObj<CatalogoAcademicoService>('CatalogoAcademicoService', ['getSituaciones']);
    errores = jasmine.createSpyObj<ErrorHandlerService>('ErrorHandlerService', ['handleError']);
    toast = jasmine.createSpyObj<ToastService>('ToastService', ['showSuccess', 'showError']);
    recargar = jasmine.createSpy('recargar');
    cancelaciones.getDetalle.and.returnValue(of(DETALLE));
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
    expect(filas[1].textContent).toContain('3,5');
    expect(filas[1].textContent).toContain('R1 - Aprobada');
    expect(filas[0].textContent).toContain('Sin registrar');
    expect(fixture.nativeElement.querySelector('[data-acciones]')).toBeNull();
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

  it('remitir valida faltas, nota y situacion de cada asignatura y envia los numeros', () => {
    cancelaciones.remitirADecano.and.returnValue(of(DETALLE));
    const componente = crear(solicitud(['REMITIR_DECANO'])).componentInstance;
    componente.abrir('REMITIR_DECANO');
    expect(catalogo.getSituaciones).toHaveBeenCalled();
    componente.evaluaciones['as-1'] = { faltas: '-1', nota: '5.5', situacion: null };
    componente.confirmar();
    expect(componente.errores['faltas-as-1']).toBeTruthy();
    expect(componente.errores['nota-as-1']).toBeTruthy();
    expect(componente.errores['situacion-as-1']).toBeTruthy();
    expect(cancelaciones.remitirADecano).not.toHaveBeenCalled();

    componente.evaluaciones['as-1'] = { faltas: '4', nota: '2,8', situacion: 'r0' };
    componente.confirmar();
    expect(cancelaciones.remitirADecano).toHaveBeenCalledWith('s-1', {
      observacion: null,
      evaluaciones: [
        { asignaturaSolicitudUuid: 'as-1', numeroFaltas: 4, nota: 2.8, situacionMatriculaUuid: 'r0' },
        { asignaturaSolicitudUuid: 'as-2', numeroFaltas: 2, nota: 3.5, situacionMatriculaUuid: 'r1' }
      ]
    });
    expect(recargar).toHaveBeenCalled();
  });

  it('aprobar exige la situacion al cancelar de cada asignatura', () => {
    cancelaciones.aprobarPorDecano.and.returnValue(of(DETALLE));
    const componente = crear(solicitud(['APROBAR_DECANO'], false, 'EN_REVISION_DECANO'), 'DECANO').componentInstance;
    componente.abrir('APROBAR_DECANO');
    componente.situacionesCancelar['as-1'] = 'r0';
    componente.confirmar();
    expect(componente.errores['cancelar-as-2']).toBeTruthy();
    componente.situacionesCancelar['as-2'] = 'r1';
    componente.confirmar();
    expect(cancelaciones.aprobarPorDecano).toHaveBeenCalledWith('s-1', {
      situaciones: [
        { asignaturaSolicitudUuid: 'as-1', situacionCancelarUuid: 'r0' },
        { asignaturaSolicitudUuid: 'as-2', situacionCancelarUuid: 'r1' }
      ]
    });
  });

  it('el rechazo del Decano exige observacion', () => {
    cancelaciones.rechazarPorDecano.and.returnValue(of(DETALLE));
    const componente = crear(solicitud(['RECHAZAR_DECANO'], false, 'EN_REVISION_DECANO'), 'DECANO').componentInstance;
    componente.abrir('RECHAZAR_DECANO');
    componente.confirmar();
    expect(cancelaciones.rechazarPorDecano).not.toHaveBeenCalled();
    componente.observacion = 'No procede';
    componente.confirmar();
    expect(cancelaciones.rechazarPorDecano).toHaveBeenCalledWith('s-1', { observacion: 'No procede' });
  });

  it('enviar la respuesta exige el escaneo y luego responde sin cuerpo', () => {
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
    expect(cancelaciones.enviarRespuesta).toHaveBeenCalledWith('s-1');
    expect(recargar).toHaveBeenCalled();
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
    componente.abrir('REMITIR_DECANO');
    componente.evaluaciones['as-1'] = { faltas: '1', nota: '3.0', situacion: 'r0' };
    componente.confirmar();
    fixture.detectChanges();
    expect(componente.erroresServidor).toEqual(['evaluaciones[0].nota: debe ser menor o igual a 5.0']);
    expect(document.querySelector('[data-error="servidor"]')?.textContent).toContain('debe ser menor o igual a 5.0');
  });
});
