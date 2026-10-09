import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { HttpErrorResponse, provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Subject, of, throwError } from 'rxjs';
import { EstCancelacionAsignaturaContentComponent } from './est-cancelacion-asignatura-content-component';
import { CancelacionAsignaturaService } from '../../../../core/services/cancelacion-asignatura-service';
import { ErrorHandlerService } from '../../../../core/services/error-handler-service';
import { ToastService } from '../../../../core/services/toast-service';
import { FormularioCancelacionAsignaturaDTORespuesta } from '../../../../core/models/CancelacionAsignatura/DTOResponse/FormularioCancelacionAsignaturaDTORespuesta';
import { InputAnexoUploadComponent } from '../../../inputs/input-anexo-upload-component/input-anexo-upload-component';
import { environment } from '../../../../../enviroments/environment';

function formulario(asignaturas = 3): FormularioCancelacionAsignaturaDTORespuesta {
  return {
    asignaturas: Array.from({ length: asignaturas }, (_, i) => ({
      uuidAsignaturaMatriculada: `am-${i + 1}`,
      codigoAsignatura: `A${i + 1}`,
      nombreAsignatura: `Asignatura ${i + 1}`,
      grupo: 'A'
    })),
    soportes: [{
      uuidTipoAnexoAcademico: null, nombre: 'Soporte libre', formatosPermitidos: 'pdf,jpg,jpeg,png',
      tamanioMaximoBytes: 5242880, obligatorio: false
    }]
  };
}

function archivo(nombre: string, bytes = 10): File {
  return new File([new Uint8Array(bytes)], nombre);
}

function marcar(marcada = true): Event {
  return { target: { checked: marcada } } as unknown as Event;
}

describe('EstCancelacionAsignaturaContentComponent', () => {
  let servicio: jasmine.SpyObj<CancelacionAsignaturaService>;
  let errores: jasmine.SpyObj<ErrorHandlerService>;
  let toast: jasmine.SpyObj<ToastService>;
  let router: jasmine.SpyObj<Router>;
  const entrada = { reset: jasmine.createSpy('reset') } as unknown as InputAnexoUploadComponent;

  beforeEach(() => {
    servicio = jasmine.createSpyObj<CancelacionAsignaturaService>('CancelacionAsignaturaService', ['getFormulario', 'radicar']);
    errores = jasmine.createSpyObj<ErrorHandlerService>('ErrorHandlerService', ['handleError']);
    toast = jasmine.createSpyObj<ToastService>('ToastService', ['showError', 'showSuccess']);
    router = jasmine.createSpyObj<Router>('Router', ['navigate']);
    TestBed.configureTestingModule({
      imports: [EstCancelacionAsignaturaContentComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: CancelacionAsignaturaService, useValue: servicio },
        { provide: ErrorHandlerService, useValue: errores },
        { provide: ToastService, useValue: toast },
        { provide: Router, useValue: router }
      ]
    });
  });

  function crear(datos = formulario()): ComponentFixture<EstCancelacionAsignaturaContentComponent> {
    servicio.getFormulario.and.returnValue(of(datos));
    const fixture = TestBed.createComponent(EstCancelacionAsignaturaContentComponent);
    fixture.detectChanges();
    return fixture;
  }

  function botonRadicar(fixture: ComponentFixture<EstCancelacionAsignaturaContentComponent>): HTMLButtonElement {
    return fixture.nativeElement.querySelector('[data-boton-radicar] button');
  }

  it('muestra las asignaturas activas con su grupo y no trae la advertencia de primer periodo', () => {
    const fixture = crear();
    const filas = fixture.nativeElement.querySelectorAll('[data-asignatura]');
    expect(filas.length).toBe(3);
    expect(filas[0].textContent).toContain('A1 - Asignatura 1 (grupo A)');
    expect(fixture.nativeElement.textContent).not.toContain('primer periodo');
  });

  it('sin asignaturas activas avisa y deshabilita el envio', () => {
    const fixture = crear(formulario(0));
    expect(fixture.nativeElement.querySelector('[data-estado="sin-asignaturas"]')).toBeTruthy();
    expect(botonRadicar(fixture).disabled).toBeTrue();
  });

  it('exige motivo y al menos una asignatura', () => {
    const fixture = crear();
    fixture.componentInstance.radicar();
    fixture.detectChanges();
    expect(Object.keys(fixture.componentInstance.errores).sort()).toEqual(['asignaturas', 'motivo']);
    expect(fixture.nativeElement.querySelector('[data-error="asignaturas"]').textContent).toContain('al menos una');
    expect(servicio.radicar).not.toHaveBeenCalled();
  });

  it('el motivo admite maximo 255 caracteres', () => {
    const componente = crear().componentInstance;
    componente.alCambiarSeleccion('am-1', marcar());
    componente.alCambiarMotivo('a'.repeat(256));
    componente.radicar();
    expect(componente.errores['motivo']).toContain('255');
    expect(servicio.radicar).not.toHaveBeenCalled();
  });

  it('valida tamano y formato de los soportes, con jpg y jpeg equivalentes', () => {
    const componente = crear().componentInstance;
    componente.alAgregarSoporte(archivo('grande.pdf', 5242881), entrada);
    expect(componente.errores['soporte']).toContain('5 MB');
    componente.alAgregarSoporte(archivo('carta.docx'), entrada);
    expect(componente.errores['soporte']).toContain('Formato no permitido');
    componente.alAgregarSoporte(archivo('foto.jpeg'), entrada);
    componente.alAgregarSoporte(archivo('foto.jpg'), entrada);
    expect(componente.soportes.map(s => s.name)).toEqual(['foto.jpeg', 'foto.jpg']);
    expect(componente.errores['soporte']).toBeUndefined();
  });

  it('no deja pasar de 20 MB entre todos los archivos', () => {
    const componente = crear().componentInstance;
    componente.alCambiarSeleccion('am-1', marcar());
    componente.alCambiarMotivo('Cruce de horario laboral');
    for (let i = 0; i < 5; i++) componente.alAgregarSoporte(archivo(`s${i}.pdf`, 5000000), entrada);
    componente.radicar();
    expect(componente.errores['total']).toContain('20 MB');
    expect(servicio.radicar).not.toHaveBeenCalled();
  });

  it('radica con el motivo, las asignaturas elegidas en orden y los soportes, y navega al detalle', () => {
    servicio.radicar.and.returnValue(of({ uuidSolicitudAcademica: 's-3', radicado: '2026-CA-0003' }));
    const componente = crear().componentInstance;
    componente.alCambiarSeleccion('am-3', marcar());
    componente.alCambiarSeleccion('am-1', marcar());
    componente.alCambiarSeleccion('am-2', marcar());
    componente.alCambiarSeleccion('am-2', marcar(false));
    componente.alCambiarMotivo('  Cruce de horario laboral  ');
    componente.alAgregarSoporte(archivo('certificado.pdf'), entrada);
    componente.radicar();
    expect(servicio.radicar).toHaveBeenCalledWith({
      motivo: 'Cruce de horario laboral',
      asignaturas: ['am-1', 'am-3'],
      soportes: [jasmine.any(File)]
    });
    expect(toast.showSuccess).toHaveBeenCalledWith('Solicitud radicada', 'Radicado 2026-CA-0003');
    expect(router.navigate).toHaveBeenCalledWith(['/estudiante/solicitudes', 's-3']);
  });

  it('no permite un doble envio mientras el POST esta en curso', () => {
    const respuesta = new Subject<{ uuidSolicitudAcademica: string; radicado: string }>();
    servicio.radicar.and.returnValue(respuesta);
    const fixture = crear();
    const componente = fixture.componentInstance;
    componente.alCambiarSeleccion('am-1', marcar());
    componente.alCambiarMotivo('Motivo');
    componente.radicar();
    componente.radicar();
    fixture.detectChanges();
    expect(servicio.radicar).toHaveBeenCalledTimes(1);
    expect(botonRadicar(fixture).disabled).toBeTrue();
    respuesta.error(new HttpErrorResponse({ status: 500 }));
    fixture.detectChanges();
    expect(botonRadicar(fixture).disabled).toBeFalse();
  });

  it('muestra el mapa de campos de un 400 junto a cada campo', () => {
    servicio.radicar.and.returnValue(throwError(() => new HttpErrorResponse({
      status: 400, error: { motivo: 'no debe estar vacío', asignaturas: 'lista inválida', extra: 'otro problema' }
    })));
    const fixture = crear();
    const componente = fixture.componentInstance;
    componente.alCambiarSeleccion('am-1', marcar());
    componente.alCambiarMotivo('Motivo');
    componente.radicar();
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[data-error="motivo"]').textContent).toContain('no debe estar vacío');
    expect(fixture.nativeElement.querySelector('[data-error="asignaturas"]').textContent).toContain('lista inválida');
    expect(fixture.nativeElement.querySelector('[data-error="servidor"]').textContent).toContain('extra: otro problema');
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('un error con codigoError va al ErrorHandlerService', () => {
    const error = new HttpErrorResponse({ status: 500, error: { codigoError: '4', mensaje: 'Ya tienes una solicitud de Cancelación de Asignatura en curso...' } });
    servicio.radicar.and.returnValue(throwError(() => error));
    const componente = crear().componentInstance;
    componente.alCambiarSeleccion('am-1', marcar());
    componente.alCambiarMotivo('Motivo');
    componente.radicar();
    expect(errores.handleError).toHaveBeenCalledWith(error, 'Error', 'No se pudo radicar la solicitud');
    expect(router.navigate).not.toHaveBeenCalled();
  });
});

describe('EstCancelacionAsignaturaContentComponent con el servicio real', () => {
  it('envia solo motivo, asignaturas y soporte en el multipart', () => {
    TestBed.configureTestingModule({
      imports: [EstCancelacionAsignaturaContentComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: ErrorHandlerService, useValue: jasmine.createSpyObj('ErrorHandlerService', ['handleError']) },
        { provide: ToastService, useValue: jasmine.createSpyObj('ToastService', ['showError', 'showSuccess']) },
        { provide: Router, useValue: jasmine.createSpyObj('Router', ['navigate']) }
      ]
    });
    const http = TestBed.inject(HttpTestingController);
    const fixture = TestBed.createComponent(EstCancelacionAsignaturaContentComponent);
    fixture.detectChanges();
    http.expectOne(`${environment.apiUrl}/cancelaciones-asignatura/formulario`).flush(formulario());
    const componente = fixture.componentInstance;
    const entrada = { reset: () => undefined } as unknown as InputAnexoUploadComponent;
    componente.alCambiarSeleccion('am-1', marcar());
    componente.alCambiarSeleccion('am-2', marcar());
    componente.alCambiarMotivo('Motivo');
    componente.alAgregarSoporte(archivo('a.pdf'), entrada);
    componente.alAgregarSoporte(archivo('b.png'), entrada);
    componente.radicar();
    const req = http.expectOne(`${environment.apiUrl}/cancelaciones-asignatura`);
    const cuerpo = req.request.body as FormData;
    expect(Array.from(cuerpo.keys())).toEqual(['motivo', 'asignaturas', 'asignaturas', 'soporte', 'soporte']);
    expect(cuerpo.getAll('asignaturas')).toEqual(['am-1', 'am-2']);
    expect(req.request.headers.has('Content-Type')).toBeFalse();
    req.flush({ uuidSolicitudAcademica: 's', radicado: '2026-CA-0001' });
  });
});
