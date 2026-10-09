import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { HttpErrorResponse, provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { Subject, of, throwError } from 'rxjs';
import { EstCancelacionMatriculaContentComponent } from './est-cancelacion-matricula-content-component';
import { CancelacionMatriculaService } from '../../../../core/services/cancelacion-matricula-service';
import { ErrorHandlerService } from '../../../../core/services/error-handler-service';
import { ToastService } from '../../../../core/services/toast-service';
import { FormularioCancelacionMatriculaDTORespuesta } from '../../../../core/models/CancelacionMatricula/DTOResponse/FormularioCancelacionMatriculaDTORespuesta';
import { InputAnexoUploadComponent } from '../../../inputs/input-anexo-upload-component/input-anexo-upload-component';

const NOMBRES = [
  'Paz y salvo - División de Bibliotecas',
  'Paz y salvo - División de Deportes y Recreación',
  'Paz y salvo - División de Salud Integral',
  'Cupón de Confirmación de la Intervención Psicosocial - División de Salud Integral',
  'Paz y salvo - División Financiera',
  'Carné estudiantil o constancia de no trámite - DARCA'
];

function formulario(asignaturas = 2): FormularioCancelacionMatriculaDTORespuesta {
  return {
    anexosRequeridos: NOMBRES.map((nombre, i) => ({
      uuidTipoAnexoAcademico: `t-${i + 1}`,
      nombre,
      formatosPermitidos: i === 5 ? 'pdf,jpg,jpeg,png' : 'pdf',
      obligatorio: true
    })),
    asignaturas: Array.from({ length: asignaturas }, (_, i) => ({ codigoAsignatura: `A${i + 1}`, nombreAsignatura: `Asignatura ${i + 1}` }))
  };
}

function archivo(nombre: string, bytes = 10): File {
  return new File([new Uint8Array(bytes)], nombre);
}

describe('EstCancelacionMatriculaContentComponent', () => {
  let servicio: jasmine.SpyObj<CancelacionMatriculaService>;
  let errores: jasmine.SpyObj<ErrorHandlerService>;
  let toast: jasmine.SpyObj<ToastService>;
  let router: jasmine.SpyObj<Router>;
  const entrada = { reset: jasmine.createSpy('reset') } as unknown as InputAnexoUploadComponent;

  beforeEach(() => {
    servicio = jasmine.createSpyObj<CancelacionMatriculaService>('CancelacionMatriculaService', ['getFormulario', 'radicar']);
    errores = jasmine.createSpyObj<ErrorHandlerService>('ErrorHandlerService', ['handleError']);
    toast = jasmine.createSpyObj<ToastService>('ToastService', ['showError', 'showSuccess']);
    router = jasmine.createSpyObj<Router>('Router', ['navigate']);
    TestBed.configureTestingModule({
      imports: [EstCancelacionMatriculaContentComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: CancelacionMatriculaService, useValue: servicio },
        { provide: ErrorHandlerService, useValue: errores },
        { provide: ToastService, useValue: toast },
        { provide: Router, useValue: router }
      ]
    });
  });

  function crear(datos = formulario()): ComponentFixture<EstCancelacionMatriculaContentComponent> {
    servicio.getFormulario.and.returnValue(of(datos));
    const fixture = TestBed.createComponent(EstCancelacionMatriculaContentComponent);
    fixture.detectChanges();
    return fixture;
  }

  function llenar(componente: EstCancelacionMatriculaContentComponent): void {
    componente.alCambiarMotivo('Problemas de salud');
    componente.campos.forEach((campo, i) => componente.alSeleccionarAnexo(campo, archivo(i === 5 ? 'carne.jpeg' : `anexo${i}.pdf`), entrada));
    componente.alAgregarSoporte(archivo('incapacidad.png'), entrada);
    componente.aceptaAdvertencia = true;
  }

  function botonRadicar(fixture: ComponentFixture<EstCancelacionMatriculaContentComponent>): HTMLButtonElement {
    return fixture.nativeElement.querySelector('[data-boton-radicar] button');
  }

  it('muestra un campo por cada anexo obligatorio y las asignaturas que se cancelan', () => {
    const fixture = crear();
    expect(fixture.nativeElement.querySelectorAll('[data-campo-anexo]').length).toBe(6);
    expect(fixture.nativeElement.querySelectorAll('[data-asignatura]').length).toBe(2);
    expect(fixture.nativeElement.textContent).toContain('Carné estudiantil o constancia de no trámite - DARCA');
    expect(botonRadicar(fixture).disabled).toBeFalse();
  });

  it('sin asignaturas activas avisa y no deja radicar', () => {
    const fixture = crear(formulario(0));
    expect(fixture.nativeElement.querySelector('[data-estado="sin-asignaturas"]')).toBeTruthy();
    expect(botonRadicar(fixture).disabled).toBeTrue();
    fixture.componentInstance.radicar();
    expect(servicio.radicar).not.toHaveBeenCalled();
  });

  it('no envia si faltan motivo, anexos o la confirmacion de la advertencia', () => {
    const fixture = crear();
    const componente = fixture.componentInstance;
    componente.radicar();
    fixture.detectChanges();
    expect(servicio.radicar).not.toHaveBeenCalled();
    expect(Object.keys(componente.errores).sort()).toEqual(['advertencia', 'motivo', 't-1', 't-2', 't-3', 't-4', 't-5', 't-6']);
    expect(fixture.nativeElement.querySelectorAll('[data-error-anexo]').length).toBe(6);
    expect(toast.showError).toHaveBeenCalled();
  });

  it('rechaza un anexo de mas de 5 MB y limpia el campo', () => {
    const componente = crear().componentInstance;
    (entrada.reset as jasmine.Spy).calls.reset();
    componente.alSeleccionarAnexo(componente.campos[0], archivo('grande.pdf', 5242881), entrada);
    expect(componente.archivos['t-1']).toBeNull();
    expect(componente.errores['t-1']).toContain('5 MB');
    expect(entrada.reset).toHaveBeenCalled();
  });

  it('acepta jpeg en DARCA y rechaza formatos no permitidos', () => {
    const componente = crear().componentInstance;
    componente.alSeleccionarAnexo(componente.campos[5], archivo('carne.jpeg'), entrada);
    expect(componente.archivos['t-6']?.name).toBe('carne.jpeg');
    componente.alSeleccionarAnexo(componente.campos[0], archivo('paz.docx'), entrada);
    expect(componente.archivos['t-1']).toBeNull();
    expect(componente.errores['t-1']).toContain('pdf');
  });

  it('el motivo admite maximo 255 caracteres', () => {
    const componente = crear().componentInstance;
    llenar(componente);
    componente.alCambiarMotivo('a'.repeat(256));
    componente.radicar();
    expect(componente.errores['motivo']).toContain('255');
    expect(servicio.radicar).not.toHaveBeenCalled();
  });

  it('radica con los seis anexos y los soportes, avisa el radicado y navega al detalle', () => {
    servicio.radicar.and.returnValue(of({ uuidSolicitudAcademica: 's-9', radicado: '2026-CM-0009' }));
    const componente = crear().componentInstance;
    llenar(componente);
    componente.radicar();
    const peticion = servicio.radicar.calls.mostRecent().args[0];
    expect(peticion.motivo).toBe('Problemas de salud');
    expect(peticion.anexos.map(a => a.uuidTipoAnexoAcademico)).toEqual(['t-1', 't-2', 't-3', 't-4', 't-5', 't-6']);
    expect(peticion.soportes.map(s => s.name)).toEqual(['incapacidad.png']);
    expect(toast.showSuccess).toHaveBeenCalledWith('Solicitud radicada', 'Radicado 2026-CM-0009');
    expect(router.navigate).toHaveBeenCalledWith(['/estudiante/solicitudes', 's-9']);
  });

  it('no permite un doble envio mientras el POST esta en curso', () => {
    const respuesta = new Subject<{ uuidSolicitudAcademica: string; radicado: string }>();
    servicio.radicar.and.returnValue(respuesta);
    const fixture = crear();
    llenar(fixture.componentInstance);
    fixture.componentInstance.radicar();
    fixture.componentInstance.radicar();
    fixture.detectChanges();
    expect(servicio.radicar).toHaveBeenCalledTimes(1);
    expect(botonRadicar(fixture).disabled).toBeTrue();
    expect(botonRadicar(fixture).textContent).toContain('Enviando...');
    respuesta.error(new HttpErrorResponse({ status: 500 }));
    fixture.detectChanges();
    expect(botonRadicar(fixture).disabled).toBeFalse();
  });

  it('muestra el mapa de campos de un 400 junto a cada campo', () => {
    servicio.radicar.and.returnValue(throwError(() => new HttpErrorResponse({
      status: 400,
      error: { motivo: 'no debe estar vacío', 't-2': 'archivo dañado', general: 'otro problema' }
    })));
    const fixture = crear();
    llenar(fixture.componentInstance);
    fixture.componentInstance.radicar();
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[data-error="motivo"]').textContent).toContain('no debe estar vacío');
    expect(fixture.nativeElement.querySelector('[data-campo-anexo="t-2"] [data-error-anexo]').textContent).toContain('archivo dañado');
    expect(fixture.nativeElement.querySelector('[data-error="servidor"]').textContent).toContain('general: otro problema');
    expect(errores.handleError).toHaveBeenCalled();
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('un error de negocio con codigoError va al ErrorHandlerService sin navegar', () => {
    const error = new HttpErrorResponse({
      status: 500,
      error: { codigoError: '4', mensaje: 'Ya tienes una solicitud de Cancelación de Matrícula en curso (2026-CM-0001)...' }
    });
    servicio.radicar.and.returnValue(throwError(() => error));
    const componente = crear().componentInstance;
    llenar(componente);
    componente.radicar();
    expect(errores.handleError).toHaveBeenCalledWith(error, 'Error', 'No se pudo radicar la solicitud');
    expect(componente.erroresServidor).toEqual({});
    expect(componente.enviando).toBeFalse();
    expect(router.navigate).not.toHaveBeenCalled();
  });
});
