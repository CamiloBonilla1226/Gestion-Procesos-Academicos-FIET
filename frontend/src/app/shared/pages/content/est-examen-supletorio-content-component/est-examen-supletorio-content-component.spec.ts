import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { HttpErrorResponse, provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { EstExamenSupletorioContentComponent } from './est-examen-supletorio-content-component';
import { ErrorHandlerService } from '../../../../core/services/error-handler-service';
import { ToastService } from '../../../../core/services/toast-service';
import { FormularioExamenSupletorioDTORespuesta } from '../../../../core/models/ExamenSupletorio/DTOResponse/FormularioExamenSupletorioDTORespuesta';
import { InputAnexoUploadComponent } from '../../../inputs/input-anexo-upload-component/input-anexo-upload-component';
import { fechaDeHoy } from '../../../../core/utils/validaciones-academicas';
import { environment } from '../../../../../enviroments/environment';

const URL = `${environment.apiUrl}/examenes-supletorios`;

function formulario(asignaturas = 2): FormularioExamenSupletorioDTORespuesta {
  return {
    asignaturas: Array.from({ length: asignaturas }, (_, i) => ({
      uuidAsignaturaMatriculada: `am-${i + 1}`, codigoAsignatura: `A${i + 1}`, nombreAsignatura: `Asignatura ${i + 1}`, grupo: 'A'
    })),
    causas: ['cruce', 'otra'],
    anexos: [
      { uuidTipoAnexoAcademico: 'for23', nombre: 'Formato PM-FO-4-FOR-23', formatosPermitidos: 'pdf,jpg,png', tamanioMaximoBytes: 5242880, causas: ['cruce', 'otra'] },
      { uuidTipoAnexoAcademico: 'just', nombre: 'Soporte de la justificación', formatosPermitidos: 'pdf,jpg,png', tamanioMaximoBytes: 5242880, causas: ['otra'] },
      { uuidTipoAnexoAcademico: 'doc', nombre: 'Formato del docente cruzado', formatosPermitidos: 'pdf,jpg,png', tamanioMaximoBytes: 5242880, causas: ['cruce'] }
    ],
    plazoDiasHabiles: 3
  };
}

function archivo(nombre: string, bytes = 10): File {
  return new File([new Uint8Array(bytes)], nombre);
}

function ayer(): string {
  const fecha = new Date();
  fecha.setDate(fecha.getDate() - 1);
  return fechaDeHoy(fecha);
}

function manana(): string {
  const fecha = new Date();
  fecha.setDate(fecha.getDate() + 1);
  return fechaDeHoy(fecha);
}

describe('EstExamenSupletorioContentComponent', () => {
  let http: HttpTestingController;
  let errores: jasmine.SpyObj<ErrorHandlerService>;
  let toast: jasmine.SpyObj<ToastService>;
  let router: jasmine.SpyObj<Router>;
  const entrada = { reset: () => undefined } as unknown as InputAnexoUploadComponent;

  beforeEach(() => {
    errores = jasmine.createSpyObj<ErrorHandlerService>('ErrorHandlerService', ['handleError']);
    toast = jasmine.createSpyObj<ToastService>('ToastService', ['showError', 'showSuccess']);
    router = jasmine.createSpyObj<Router>('Router', ['navigate']);
    TestBed.configureTestingModule({
      imports: [EstExamenSupletorioContentComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: ErrorHandlerService, useValue: errores },
        { provide: ToastService, useValue: toast },
        { provide: Router, useValue: router }
      ]
    });
    http = TestBed.inject(HttpTestingController);
  });

  function crear(datos = formulario()): ComponentFixture<EstExamenSupletorioContentComponent> {
    const fixture = TestBed.createComponent(EstExamenSupletorioContentComponent);
    fixture.detectChanges();
    http.expectOne(`${URL}/formulario`).flush(datos);
    fixture.detectChanges();
    return fixture;
  }

  function anexosVisibles(fixture: ComponentFixture<EstExamenSupletorioContentComponent>): string[] {
    fixture.detectChanges();
    return Array.from(fixture.nativeElement.querySelectorAll('[data-campo-anexo]')).map((e: any) => e.getAttribute('data-campo-anexo'));
  }

  function llenarCruce(componente: EstExamenSupletorioContentComponent): void {
    componente.alCambiarAsignatura('am-1');
    componente.alCambiarFechaExamen(ayer());
    componente.alCambiarCausa('cruce');
    componente.alCambiarAsignaturaCruzada('am-2');
    componente.alCambiarFechaCruce(ayer());
    componente.alCambiarHoraCruce('14:00');
    const campos = componente.camposVisibles;
    componente.alSeleccionarAnexo(campos.find(c => c.anexo.uuidTipoAnexoAcademico === 'for23')!, archivo('for23.pdf'), entrada);
    componente.alSeleccionarAnexo(campos.find(c => c.anexo.uuidTipoAnexoAcademico === 'doc')!, archivo('docente.png'), entrada);
  }

  function partes(req: { request: { body: unknown } }): string[] {
    return Array.from((req.request.body as FormData).keys());
  }

  it('muestra el plazo informativo, las asignaturas y el FOR-23 antes de elegir la causa', () => {
    const fixture = crear();
    expect(fixture.nativeElement.querySelector('[data-plazo]').textContent).toContain('3 días hábiles');
    expect(fixture.componentInstance.opcionesAsignatura.map(o => o.label)).toEqual(['A1 - Asignatura 1 (grupo A)', 'A2 - Asignatura 2 (grupo A)']);
    expect(anexosVisibles(fixture)).toEqual(['for23']);
    expect(fixture.nativeElement.querySelector('[data-seccion-cruce]')).toBeNull();
  });

  it('muestra los anexos y los campos de cruce segun la causa', () => {
    const fixture = crear();
    fixture.componentInstance.alCambiarCausa('cruce');
    expect(anexosVisibles(fixture)).toEqual(['for23', 'doc']);
    expect(fixture.nativeElement.querySelector('[data-seccion-cruce]')).toBeTruthy();
    fixture.componentInstance.alCambiarCausa('otra');
    expect(anexosVisibles(fixture)).toEqual(['for23', 'just']);
    expect(fixture.nativeElement.querySelector('[data-seccion-cruce]')).toBeNull();
  });

  it('al cambiar de causa descarta los archivos y los datos de la causa anterior', () => {
    const componente = crear().componentInstance;
    llenarCruce(componente);
    expect(componente.archivos['doc']?.name).toBe('docente.png');
    componente.alCambiarCausa('otra');
    expect(componente.archivos['doc']).toBeUndefined();
    expect(componente.archivos['for23']?.name).toBe('for23.pdf');
    expect(componente.asignaturaCruzada).toBeNull();
    expect(componente.fechaCruce).toBe('');
    expect(componente.horaCruce).toBe('');
  });

  it('el cruce no ofrece la asignatura del examen no presentado', () => {
    const componente = crear().componentInstance;
    componente.alCambiarAsignatura('am-1');
    expect(componente.opcionesCruce.map(o => o.value)).toEqual(['am-2']);
  });

  it('exige los campos obligatorios y los anexos de la causa sin enviar nada', () => {
    const componente = crear().componentInstance;
    componente.radicar();
    expect(Object.keys(componente.errores).sort()).toEqual(['asignatura', 'causa', 'fechaExamen', 'for23']);
    componente.alCambiarCausa('cruce');
    expect(Object.keys(componente.errores).sort()).toEqual(['asignatura', 'asignaturaCruzada', 'doc', 'fechaCruce', 'fechaExamen', 'for23', 'horaCruce']);
    http.expectNone(URL);
    expect(toast.showError).toHaveBeenCalled();
  });

  it('rechaza una fecha futura, la misma asignatura cruzada y una hora sin formato HH:mm', () => {
    const componente = crear().componentInstance;
    llenarCruce(componente);
    componente.alCambiarFechaExamen(manana());
    componente.alCambiarAsignaturaCruzada('am-1');
    componente.alCambiarHoraCruce('2pm');
    componente.radicar();
    expect(componente.errores['fechaExamen']).toContain('posterior a hoy');
    expect(componente.errores['asignaturaCruzada']).toContain('distinta');
    expect(componente.errores['horaCruce']).toContain('HH:mm');
    http.expectNone(URL);
  });

  it('valida cada archivo con su formato y tamaño', () => {
    const componente = crear().componentInstance;
    componente.alCambiarCausa('otra');
    const for23 = componente.camposVisibles[0];
    componente.alSeleccionarAnexo(for23, archivo('for23.docx'), entrada);
    expect(componente.errores['for23']).toContain('Formato no permitido');
    componente.alSeleccionarAnexo(for23, archivo('for23.pdf', 5242881), entrada);
    expect(componente.errores['for23']).toContain('5 MB');
    expect(componente.archivos['for23']).toBeNull();
  });

  it('con cruce envia los datos del cruce y solo el FOR-23 y el formato del docente', () => {
    const componente = crear().componentInstance;
    llenarCruce(componente);
    componente.radicar();
    const req = http.expectOne(URL);
    expect(req.request.method).toBe('POST');
    expect(partes(req)).toEqual([
      'asignaturaMatriculada', 'fechaExamenNoPresentado', 'tipoCausa', 'asignaturaCruzada', 'fechaExamenCruzada', 'horaExamenCruzada', 'for23', 'doc'
    ]);
    const cuerpo = req.request.body as FormData;
    expect(cuerpo.get('tipoCausa')).toBe('cruce');
    expect(cuerpo.get('asignaturaCruzada')).toBe('am-2');
    expect(cuerpo.get('horaExamenCruzada')).toBe('14:00');
    expect(req.request.headers.has('Content-Type')).toBeFalse();
    req.flush({ uuidSolicitudAcademica: 's-1', radicado: '2026-ES-0001' });
  });

  it('con otra no envia datos de cruce ni el anexo del cruce', () => {
    const componente = crear().componentInstance;
    llenarCruce(componente);
    componente.alCambiarCausa('otra');
    componente.alSeleccionarAnexo(componente.camposVisibles[1], archivo('justificacion.pdf'), entrada);
    componente.radicar();
    const req = http.expectOne(URL);
    expect(partes(req)).toEqual(['asignaturaMatriculada', 'fechaExamenNoPresentado', 'tipoCausa', 'for23', 'just']);
    expect((req.request.body as FormData).get('fechaExamenNoPresentado')).toBe(ayer());
    req.flush({ uuidSolicitudAcademica: 's-1', radicado: '2026-ES-0001' });
  });

  it('tras radicar muestra el radicado en lugar del formulario y navega al detalle', () => {
    const fixture = crear();
    llenarCruce(fixture.componentInstance);
    fixture.componentInstance.radicar();
    http.expectOne(URL).flush({ uuidSolicitudAcademica: 's-7', radicado: '2026-ES-0007' });
    fixture.detectChanges();
    expect(toast.showSuccess).toHaveBeenCalledWith('Solicitud radicada', 'Radicado 2026-ES-0007');
    expect(router.navigate).toHaveBeenCalledWith(['/estudiante/solicitudes', 's-7']);
    expect(fixture.nativeElement.querySelector('[data-estado="radicada"]').textContent).toContain('2026-ES-0007');
    expect(fixture.nativeElement.querySelector('[data-boton-radicar]')).toBeNull();
  });

  it('un 500 con codigoError va al ErrorHandlerService, no navega y deja reintentar', () => {
    const fixture = crear();
    llenarCruce(fixture.componentInstance);
    fixture.componentInstance.radicar();
    http.expectOne(URL).flush(
      { codigoError: '4', mensaje: 'Ya tienes una solicitud de Examen Supletorio en curso (2026-ES-0001)...' },
      { status: 500, statusText: 'Error' }
    );
    fixture.detectChanges();
    const error = errores.handleError.calls.mostRecent().args[0] as HttpErrorResponse;
    expect(error.error.mensaje).toContain('Ya tienes una solicitud');
    expect(errores.handleError.calls.mostRecent().args.slice(1)).toEqual(['Error', 'No se pudo radicar la solicitud']);
    expect(router.navigate).not.toHaveBeenCalled();
    expect(fixture.componentInstance.enviando).toBeFalse();
    expect(fixture.nativeElement.querySelector('[data-boton-radicar] button').disabled).toBeFalse();
  });

  it('no permite un doble envio mientras el POST esta en curso', () => {
    const componente = crear().componentInstance;
    llenarCruce(componente);
    componente.radicar();
    componente.radicar();
    expect(http.match(URL).length).toBe(1);
  });

  it('sin asignaturas activas avisa y no deja radicar', () => {
    const fixture = crear(formulario(0));
    expect(fixture.nativeElement.querySelector('[data-estado="sin-asignaturas"]')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('[data-boton-radicar] button').disabled).toBeTrue();
  });
});
