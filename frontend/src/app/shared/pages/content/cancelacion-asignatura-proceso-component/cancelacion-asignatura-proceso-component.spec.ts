import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpErrorResponse, provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { Subject, of, throwError } from 'rxjs';
import { CancelacionAsignaturaProcesoComponent } from './cancelacion-asignatura-proceso-component';
import { CancelacionAsignaturaService } from '../../../../core/services/cancelacion-asignatura-service';
import { SolicitudAcademicaService } from '../../../../core/services/solicitud-academica-service';
import { CatalogoAcademicoService } from '../../../../core/services/catalogo-academico-service';
import { ErrorHandlerService } from '../../../../core/services/error-handler-service';
import { ToastService } from '../../../../core/services/toast-service';
import { SolicitudAcademicaDetalleDTORespuesta } from '../../../../core/models/SolicitudAcademica/DTOResponse/SolicitudAcademicaDetalleDTORespuesta';
import { CancelacionAsignaturaDetalleDTORespuesta } from '../../../../core/models/CancelacionAsignatura/DTOResponse/CancelacionAsignaturaDetalleDTORespuesta';
import { AsignaturaSolicitadaDTORespuesta } from '../../../../core/models/CancelacionAsignatura/DTOResponse/AsignaturaSolicitadaDTORespuesta';
import { InputAnexoUploadComponent } from '../../../inputs/input-anexo-upload-component/input-anexo-upload-component';
import { environment } from '../../../../../enviroments/environment';

function solicitud(acciones: string[], etapaCodigo = 'RADICADA', tieneResolucion = false): SolicitudAcademicaDetalleDTORespuesta {
  return {
    uuidSolicitudAcademica: 's-1', radicado: '2026-CA-0001', uuidTipoSolicitudAcademica: 't',
    tipoSolicitud: 'Cancelación de Asignatura', fechaCreacion: '2026-10-08T10:00:00', etiqueta: 'Pendiente', etapaCodigo,
    estudiante: {
      uuidUsuario: 'e', nombres: 'María', apellidos: 'Pérez', codigoEstudiantil: '1', programaAcademico: 'P',
      semestre: '1', correoElectronico: 'm@u.co'
    },
    anexos: [], tieneResolucion, puedeDescargarResolucion: tieneResolucion, accionesDisponibles: acciones
  };
}

function soloCodigos(codigo: string, nombre: string, uuid: string): AsignaturaSolicitadaDTORespuesta {
  return {
    uuidAsignaturaSolicitud: uuid, codigoAsignatura: codigo, nombreAsignatura: nombre, numeroFaltas: null, nota: null,
    situacionMatricula: null, cumpleCondiciones: null, observacionEvaluacion: null, aprobadaPorDecano: null,
    observacionDecision: null, situacionCancelar: null
  };
}

function detalle(asignaturas: AsignaturaSolicitadaDTORespuesta[]): CancelacionAsignaturaDetalleDTORespuesta {
  return { solicitud: solicitud([]), motivoCancelacion: 'Cruce laboral', asignaturas };
}

const EVALUADAS: AsignaturaSolicitadaDTORespuesta[] = [
  {
    ...soloCodigos('A1', 'Cálculo', 'as-1'), numeroFaltas: 1, nota: 3.8,
    situacionMatricula: { uuidSituacionAcademica: 'r0', codigo: 'R0', nombre: 'No cursada' },
    cumpleCondiciones: true, observacionEvaluacion: null
  },
  {
    ...soloCodigos('A2', 'Física', 'as-2'), numeroFaltas: 6, nota: 2.4,
    situacionMatricula: { uuidSituacionAcademica: 'r2', codigo: 'R2', nombre: 'Perdida' },
    cumpleCondiciones: false, observacionEvaluacion: 'Nota menor a 3.0'
  }
];

describe('CancelacionAsignaturaProcesoComponent', () => {
  let cancelaciones: jasmine.SpyObj<CancelacionAsignaturaService>;
  let solicitudes: jasmine.SpyObj<SolicitudAcademicaService>;
  let catalogo: jasmine.SpyObj<CatalogoAcademicoService>;
  let errores: jasmine.SpyObj<ErrorHandlerService>;
  let recargar: jasmine.Spy;
  const entrada = { reset: jasmine.createSpy('reset') } as unknown as InputAnexoUploadComponent;

  beforeEach(() => {
    cancelaciones = jasmine.createSpyObj<CancelacionAsignaturaService>('CancelacionAsignaturaService', [
      'getDetalle', 'rechazarPorFuncionario', 'remitirADecano', 'enviarRespuesta', 'aprobarPorDecano', 'rechazarPorDecano'
    ]);
    solicitudes = jasmine.createSpyObj<SolicitudAcademicaService>('SolicitudAcademicaService', ['adjuntarResolucion']);
    catalogo = jasmine.createSpyObj<CatalogoAcademicoService>('CatalogoAcademicoService', ['getSituaciones']);
    errores = jasmine.createSpyObj<ErrorHandlerService>('ErrorHandlerService', ['handleError']);
    recargar = jasmine.createSpy('recargar');
    catalogo.getSituaciones.and.returnValue(of([
      { uuidSituacionAcademica: 'r0', codigo: 'R0', nombre: 'No cursada' },
      { uuidSituacionAcademica: 'r3', codigo: 'R3', nombre: 'Cancelada' }
    ]));
    TestBed.configureTestingModule({
      imports: [CancelacionAsignaturaProcesoComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideNoopAnimations(),
        { provide: CancelacionAsignaturaService, useValue: cancelaciones },
        { provide: SolicitudAcademicaService, useValue: solicitudes },
        { provide: CatalogoAcademicoService, useValue: catalogo },
        { provide: ErrorHandlerService, useValue: errores },
        { provide: ToastService, useValue: jasmine.createSpyObj('ToastService', ['showSuccess', 'showError']) }
      ]
    });
  });

  function crear(
    datos: SolicitudAcademicaDetalleDTORespuesta,
    asignaturas: AsignaturaSolicitadaDTORespuesta[],
    rol = 'FUNCIONARIO'
  ): ComponentFixture<CancelacionAsignaturaProcesoComponent> {
    cancelaciones.getDetalle.and.returnValue(of(detalle(asignaturas)));
    const fixture = TestBed.createComponent(CancelacionAsignaturaProcesoComponent);
    fixture.componentRef.setInput('uuidSolicitud', 's-1');
    fixture.componentRef.setInput('solicitud', datos);
    fixture.componentRef.setInput('rol', rol);
    fixture.componentRef.setInput('recargar', recargar);
    fixture.detectChanges();
    return fixture;
  }

  function encabezados(fixture: ComponentFixture<CancelacionAsignaturaProcesoComponent>): string[] {
    return Array.from(fixture.nativeElement.querySelectorAll('thead th')).map((th: any) => th.textContent.trim());
  }

  function botones(fixture: ComponentFixture<CancelacionAsignaturaProcesoComponent>): string[] {
    return Array.from(fixture.nativeElement.querySelectorAll('[data-accion]')).map((b: any) => b.getAttribute('data-accion'));
  }

  describe('Estudiante', () => {
    it('antes de la etapa final solo ve codigo y nombre de cada asignatura', () => {
      const fixture = crear(solicitud([], 'EN_REVISION_DECANO'), [soloCodigos('A1', 'Cálculo', 'as-1'), soloCodigos('A2', 'Física', 'as-2')], 'ESTUDIANTE');
      expect(encabezados(fixture)).toEqual(['Código', 'Asignatura']);
      const texto = fixture.nativeElement.textContent;
      expect(texto).toContain('Cálculo');
      expect(texto).not.toContain('null');
      expect(texto).not.toContain('Sin registrar');
      expect(fixture.nativeElement.querySelector('[data-acciones]')).toBeNull();
    });

    it('aunque el backend enviara la evaluacion, no la muestra', () => {
      const fixture = crear(solicitud([], 'APROBADA'), EVALUADAS, 'ESTUDIANTE');
      const columnas = encabezados(fixture);
      for (const prohibida of ['Faltas', 'Nota', 'Situación en la matrícula', 'Cumple condiciones', 'Observación de la evaluación', 'Situación al cancelar']) {
        expect(columnas).not.toContain(prohibida);
      }
      expect(fixture.nativeElement.textContent).not.toContain('Nota menor a 3.0');
    });

    it('en la etapa final ve ademas la decision del Decano y su observacion', () => {
      const asignaturas = [
        { ...soloCodigos('A1', 'Cálculo', 'as-1'), aprobadaPorDecano: true },
        { ...soloCodigos('A2', 'Física', 'as-2'), aprobadaPorDecano: false, observacionDecision: 'No cumple las condiciones' }
      ];
      const fixture = crear(solicitud([], 'APROBADA'), asignaturas, 'ESTUDIANTE');
      expect(encabezados(fixture)).toEqual(['Código', 'Asignatura', 'Decisión del Decano', 'Observación de la decisión']);
      const filas = fixture.nativeElement.querySelectorAll('tbody tr');
      expect(filas[0].textContent).toContain('Aprobada');
      expect(filas[1].textContent).toContain('No aprobada');
      expect(filas[1].textContent).toContain('No cumple las condiciones');
    });

    it('en la etapa final sin decisiones no pinta columnas vacias', () => {
      const fixture = crear(solicitud([], 'RECHAZADA'), [soloCodigos('A1', 'Cálculo', 'as-1')], 'ESTUDIANTE');
      expect(encabezados(fixture)).toEqual(['Código', 'Asignatura']);
      expect(fixture.nativeElement.textContent).not.toContain('null');
    });
  });

  it('el Funcionario y el Decano ven la evaluacion completa', () => {
    const fixture = crear(solicitud([], 'EN_REVISION_DECANO'), EVALUADAS, 'DECANO');
    expect(encabezados(fixture)).toEqual([
      'Código', 'Asignatura', 'Faltas', 'Nota', 'Situación en la matrícula', 'Cumple condiciones', 'Observación de la evaluación'
    ]);
    expect(fixture.nativeElement.querySelectorAll('tbody tr')[1].textContent).toContain('2,4');
  });

  it('solo pinta los botones que vienen en accionesDisponibles', () => {
    expect(botones(crear(solicitud(['RECHAZAR_FUNCIONARIO', 'REMITIR_DECANO']), EVALUADAS))).toEqual(['RECHAZAR_FUNCIONARIO', 'REMITIR_DECANO']);
    expect(botones(crear(solicitud(['APROBAR_DECANO', 'RECHAZAR_DECANO'], 'EN_REVISION_DECANO'), EVALUADAS, 'DECANO'))).toEqual(['APROBAR_DECANO', 'RECHAZAR_DECANO']);
    expect(botones(crear(solicitud(['ENVIAR_RECIBO']), EVALUADAS))).toEqual([]);
  });

  it('el boton abre el dialogo de confirmacion con su titulo', () => {
    const fixture = crear(solicitud(['REMITIR_DECANO']), [soloCodigos('A1', 'Cálculo', 'as-1')]);
    (fixture.nativeElement.querySelector('[data-accion="REMITIR_DECANO"] button') as HTMLButtonElement).click();
    fixture.detectChanges();
    expect(fixture.componentInstance.dialogoVisible).toBeTrue();
    expect(fixture.componentInstance.tituloDialogo).toBe('Remitir la solicitud al Decano');
    expect(document.querySelector('[data-evaluacion="as-1"]')).toBeTruthy();
  });

  describe('remitir al Decano', () => {
    function abrirRemision() {
      const fixture = crear(solicitud(['REMITIR_DECANO']), [soloCodigos('A1', 'Cálculo', 'as-1'), soloCodigos('A2', 'Física', 'as-2')]);
      fixture.componentInstance.abrir('REMITIR_DECANO');
      return fixture;
    }

    it('con nota menor a 3.0 no se puede marcar que cumple', () => {
      const componente = abrirRemision().componentInstance;
      componente.evaluaciones['as-1'] = { faltas: '0', nota: '2.9', situacion: 'r0', cumple: true, observacion: '' };
      componente.evaluaciones['as-2'] = { faltas: '0', nota: '4.0', situacion: 'r0', cumple: true, observacion: '' };
      componente.confirmar();
      expect(componente.errores['cumple-as-1']).toContain('menor a 3.0');
      expect(componente.errores['cumple-as-2']).toBeUndefined();
      expect(cancelaciones.remitirADecano).not.toHaveBeenCalled();
    });

    it('si no cumple exige la observacion de la evaluacion de hasta 255 caracteres', () => {
      const componente = abrirRemision().componentInstance;
      componente.evaluaciones['as-1'] = { faltas: '0', nota: '4.0', situacion: 'r0', cumple: true, observacion: '' };
      componente.evaluaciones['as-2'] = { faltas: '0', nota: '2.0', situacion: 'r0', cumple: false, observacion: '' };
      componente.confirmar();
      expect(componente.errores['observacion-as-2']).toContain('obligatori');
      componente.evaluaciones['as-2'].observacion = 'a'.repeat(256);
      componente.confirmar();
      expect(componente.errores['observacion-as-2']).toContain('255');
      expect(cancelaciones.remitirADecano).not.toHaveBeenCalled();
    });

    it('si ninguna cumple avisa que debe rechazar y no envia', () => {
      const fixture = abrirRemision();
      const componente = fixture.componentInstance;
      componente.evaluaciones['as-1'] = { faltas: '0', nota: '2.0', situacion: 'r0', cumple: false, observacion: 'Repitente' };
      componente.evaluaciones['as-2'] = { faltas: '0', nota: '2.0', situacion: 'r0', cumple: false, observacion: 'Correquisito' };
      fixture.detectChanges();
      expect(document.querySelector('[data-error="ninguna"]')?.textContent).toContain('debes rechazarla');
      componente.confirmar();
      expect(componente.errores['ninguna']).toBeTruthy();
      expect(cancelaciones.remitirADecano).not.toHaveBeenCalled();
    });

    it('envia el cuerpo exacto de la remision', () => {
      cancelaciones.remitirADecano.and.returnValue(of(detalle(EVALUADAS)));
      const componente = abrirRemision().componentInstance;
      componente.evaluaciones['as-1'] = { faltas: '2', nota: '3,5', situacion: 'r0', cumple: true, observacion: '  ' };
      componente.evaluaciones['as-2'] = { faltas: '7', nota: '2.4', situacion: 'r3', cumple: false, observacion: ' Repitente ' };
      componente.observacion = 'Revisado';
      componente.confirmar();
      expect(cancelaciones.remitirADecano).toHaveBeenCalledWith('s-1', {
        observacion: 'Revisado',
        evaluaciones: [
          { asignaturaSolicitudUuid: 'as-1', numeroFaltas: 2, nota: 3.5, situacionMatriculaUuid: 'r0', cumpleCondiciones: true, observacionEvaluacion: null },
          { asignaturaSolicitudUuid: 'as-2', numeroFaltas: 7, nota: 2.4, situacionMatriculaUuid: 'r3', cumpleCondiciones: false, observacionEvaluacion: 'Repitente' }
        ]
      });
      expect(recargar).toHaveBeenCalled();
    });

    it('la observacion general es opcional y de hasta 500 caracteres', () => {
      const componente = abrirRemision().componentInstance;
      componente.evaluaciones['as-1'] = { faltas: '0', nota: '4.0', situacion: 'r0', cumple: true, observacion: '' };
      componente.evaluaciones['as-2'] = { faltas: '0', nota: '4.0', situacion: 'r0', cumple: true, observacion: '' };
      componente.observacion = 'a'.repeat(501);
      componente.confirmar();
      expect(componente.errores['observacion']).toContain('500');
    });
  });

  describe('decision del Decano', () => {
    function abrirDecision() {
      const fixture = crear(solicitud(['APROBAR_DECANO', 'RECHAZAR_DECANO'], 'EN_REVISION_DECANO'), EVALUADAS, 'DECANO');
      fixture.componentInstance.abrir('APROBAR_DECANO');
      fixture.detectChanges();
      return fixture;
    }

    it('muestra la evaluacion del Funcionario y propone No para la que no cumple', () => {
      const fixture = abrirDecision();
      const evaluacion = document.querySelector('[data-decision="as-2"] [data-evaluacion-funcionario]')?.textContent ?? '';
      expect(evaluacion).toContain('Nota: 2,4');
      expect(evaluacion).toContain('Cumple condiciones: No');
      expect(evaluacion).toContain('Nota menor a 3.0');
      expect(fixture.componentInstance.decisiones['as-1'].aprobada).toBeNull();
      expect(fixture.componentInstance.decisiones['as-2'].aprobada).toBeFalse();
    });

    it('no se puede aprobar una asignatura que no cumple', () => {
      const componente = abrirDecision().componentInstance;
      componente.decisiones['as-1'] = { aprobada: true, situacion: 'r3', observacion: '' };
      componente.decisiones['as-2'] = { aprobada: true, situacion: 'r3', observacion: '' };
      componente.confirmar();
      expect(componente.errores['decision-as-2']).toContain('no cumple');
      expect(cancelaciones.aprobarPorDecano).not.toHaveBeenCalled();
    });

    it('la aprobada exige situacion al cancelar y la rechazada observacion', () => {
      const componente = abrirDecision().componentInstance;
      componente.decisiones['as-1'] = { aprobada: true, situacion: null, observacion: '' };
      componente.decisiones['as-2'] = { aprobada: false, situacion: null, observacion: '' };
      componente.confirmar();
      expect(componente.errores['cancelar-as-1']).toBeTruthy();
      expect(componente.errores['observacion-as-2']).toContain('obligatori');
      expect(cancelaciones.aprobarPorDecano).not.toHaveBeenCalled();
    });

    it('si ninguna queda aprobada avisa que use el rechazo completo', () => {
      const fixture = abrirDecision();
      const componente = fixture.componentInstance;
      componente.decisiones['as-1'] = { aprobada: false, situacion: null, observacion: 'No procede' };
      componente.decisiones['as-2'] = { aprobada: false, situacion: null, observacion: 'No cumple' };
      fixture.detectChanges();
      expect(document.querySelector('[data-error="ninguna"]')?.textContent).toContain('Rechazar solicitud completa');
      componente.confirmar();
      expect(componente.errores['ninguna']).toBeTruthy();
      expect(cancelaciones.aprobarPorDecano).not.toHaveBeenCalled();
    });

    it('envia el cuerpo exacto de la aprobacion parcial', () => {
      cancelaciones.aprobarPorDecano.and.returnValue(of(detalle(EVALUADAS)));
      const componente = abrirDecision().componentInstance;
      componente.decisiones['as-1'] = { aprobada: true, situacion: 'r3', observacion: '' };
      componente.decisiones['as-2'] = { aprobada: false, situacion: 'r3', observacion: 'No cumple las condiciones' };
      componente.confirmar();
      expect(cancelaciones.aprobarPorDecano).toHaveBeenCalledWith('s-1', {
        decisiones: [
          { asignaturaSolicitudUuid: 'as-1', aprobada: true, situacionCancelarUuid: 'r3', observacionDecision: null },
          { asignaturaSolicitudUuid: 'as-2', aprobada: false, situacionCancelarUuid: null, observacionDecision: 'No cumple las condiciones' }
        ]
      });
      expect(recargar).toHaveBeenCalled();
    });

    it('el rechazo completo exige observacion de hasta 500', () => {
      cancelaciones.rechazarPorDecano.and.returnValue(of(detalle(EVALUADAS)));
      const componente = crear(solicitud(['RECHAZAR_DECANO'], 'EN_REVISION_DECANO'), EVALUADAS, 'DECANO').componentInstance;
      componente.abrir('RECHAZAR_DECANO');
      componente.confirmar();
      expect(componente.errores['observacion']).toBeTruthy();
      componente.observacion = 'No procede';
      componente.confirmar();
      expect(cancelaciones.rechazarPorDecano).toHaveBeenCalledWith('s-1', { observacion: 'No procede' });
    });
  });

  it('rechazar en el Funcionario sube el escaneo antes y exige observacion', () => {
    const orden: string[] = [];
    solicitudes.adjuntarResolucion.and.callFake(() => { orden.push('resolucion'); return of({ uuidSolicitudAcademica: 's-1', nombreArchivo: 'r.pdf', fechaSubida: '' }); });
    cancelaciones.rechazarPorFuncionario.and.callFake(() => { orden.push('rechazar'); return of(detalle(EVALUADAS)); });
    const componente = crear(solicitud(['RECHAZAR_FUNCIONARIO']), EVALUADAS).componentInstance;
    componente.abrir('RECHAZAR_FUNCIONARIO');
    componente.confirmar();
    expect(componente.errores['observacion']).toBeTruthy();
    expect(componente.errores['resolucion']).toBeTruthy();
    componente.observacion = 'Ninguna cumple';
    componente.alSeleccionarResolucion(new File(['%PDF-'], 'resolucion.pdf'), entrada);
    componente.confirmar();
    expect(orden).toEqual(['resolucion', 'rechazar']);
    expect(recargar).toHaveBeenCalled();
  });

  it('enviar la respuesta sube el escaneo y responde sin cuerpo', () => {
    solicitudes.adjuntarResolucion.and.returnValue(of({ uuidSolicitudAcademica: 's-1', nombreArchivo: 'r.pdf', fechaSubida: '' }));
    cancelaciones.enviarRespuesta.and.returnValue(of(detalle(EVALUADAS)));
    const componente = crear(solicitud(['ENVIAR_RESPUESTA'], 'APROBADA_POR_DECANO'), EVALUADAS).componentInstance;
    componente.abrir('ENVIAR_RESPUESTA');
    componente.alSeleccionarResolucion(new File(['%PDF-'], 'resolucion.pdf'), entrada);
    componente.confirmar();
    expect(cancelaciones.enviarRespuesta).toHaveBeenCalledWith('s-1');
  });

  it('no permite un doble envio y un error con codigoError deja el dialogo abierto', () => {
    const respuesta = new Subject<CancelacionAsignaturaDetalleDTORespuesta>();
    cancelaciones.rechazarPorDecano.and.returnValue(respuesta);
    const componente = crear(solicitud(['RECHAZAR_DECANO'], 'EN_REVISION_DECANO'), EVALUADAS, 'DECANO').componentInstance;
    componente.abrir('RECHAZAR_DECANO');
    componente.observacion = 'No procede';
    componente.confirmar();
    componente.confirmar();
    expect(cancelaciones.rechazarPorDecano).toHaveBeenCalledTimes(1);
    const error = new HttpErrorResponse({ status: 500, error: { codigoError: '4', mensaje: 'La acción no está permitida...' } });
    respuesta.error(error);
    expect(errores.handleError).toHaveBeenCalledWith(error, 'Error', 'No se pudo completar la acción');
    expect(componente.dialogoVisible).toBeTrue();
    expect(componente.enviando).toBeFalse();
    expect(recargar).not.toHaveBeenCalled();
  });
});

describe('CancelacionAsignaturaProcesoComponent con el catalogo real', () => {
  const URL_SITUACIONES = `${environment.apiUrl}/catalogos-academicos/situaciones`;
  let http: HttpTestingController;
  let toast: jasmine.SpyObj<ToastService>;
  let errores: ErrorHandlerService;

  beforeEach(() => {
    const cancelaciones = jasmine.createSpyObj<CancelacionAsignaturaService>('CancelacionAsignaturaService', ['getDetalle']);
    cancelaciones.getDetalle.and.returnValue(of(detalle(EVALUADAS)));
    toast = jasmine.createSpyObj<ToastService>('ToastService', ['showSuccess', 'showError']);
    TestBed.configureTestingModule({
      imports: [CancelacionAsignaturaProcesoComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideNoopAnimations(),
        { provide: CancelacionAsignaturaService, useValue: cancelaciones },
        { provide: ToastService, useValue: toast }
      ]
    });
    http = TestBed.inject(HttpTestingController);
    errores = TestBed.inject(ErrorHandlerService);
    spyOn(errores, 'handleError').and.callThrough();
    spyOn(console, 'error');
  });

  function clicEnDecidir(): ComponentFixture<CancelacionAsignaturaProcesoComponent> {
    const fixture = TestBed.createComponent(CancelacionAsignaturaProcesoComponent);
    fixture.componentRef.setInput('uuidSolicitud', 's-1');
    fixture.componentRef.setInput('solicitud', solicitud(['APROBAR_DECANO', 'RECHAZAR_DECANO'], 'EN_REVISION_DECANO'));
    fixture.componentRef.setInput('rol', 'DECANO');
    fixture.componentRef.setInput('recargar', () => undefined);
    fixture.detectChanges();
    (fixture.nativeElement.querySelector('[data-accion="APROBAR_DECANO"] button') as HTMLButtonElement).click();
    fixture.detectChanges();
    return fixture;
  }

  it('con rol Decano pide el catalogo de situaciones y abre el dialogo de decidir por asignatura', () => {
    const fixture = clicEnDecidir();
    const componente = fixture.componentInstance;
    const peticion = http.expectOne(URL_SITUACIONES);
    expect(peticion.request.method).toBe('GET');
    expect(componente.dialogoVisible).toBeFalse();
    peticion.flush([{ uuidSituacionAcademica: 'r3', codigo: 'R3', nombre: 'Cancelada' }]);
    fixture.detectChanges();
    expect(componente.dialogoVisible).toBeTrue();
    expect(componente.opcionesSituacion).toEqual([{ label: 'R3 - Cancelada', value: 'r3' }]);
    expect(document.querySelector('[data-decision="as-1"]')).toBeTruthy();
    expect(errores.handleError).not.toHaveBeenCalled();
  });

  it('si el catalogo falla muestra el error y no abre el dialogo', () => {
    const fixture = clicEnDecidir();
    http.expectOne(URL_SITUACIONES).flush({ codigoError: '1', mensaje: 'Error interno' }, { status: 500, statusText: 'Error' });
    fixture.detectChanges();
    expect(fixture.componentInstance.dialogoVisible).toBeFalse();
    expect(fixture.componentInstance.accionActiva).toBeNull();
    expect(toast.showError).toHaveBeenCalledWith('Error', 'No se pudieron cargar las situaciones académicas. Error interno');
  });

  it('si el catalogo viene vacio muestra el error y no abre el dialogo', () => {
    const fixture = clicEnDecidir();
    http.expectOne(URL_SITUACIONES).flush([]);
    fixture.detectChanges();
    expect(fixture.componentInstance.dialogoVisible).toBeFalse();
    expect(toast.showError).toHaveBeenCalledWith(
      'Error',
      'No se pudieron cargar las situaciones académicas. El catálogo de situaciones académicas está vacío'
    );
  });
});
