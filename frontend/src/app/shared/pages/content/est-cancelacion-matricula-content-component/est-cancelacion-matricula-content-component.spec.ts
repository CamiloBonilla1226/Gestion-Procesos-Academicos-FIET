import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { HttpErrorResponse, provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { Subject, of, throwError } from 'rxjs';
import { EstCancelacionMatriculaContentComponent } from './est-cancelacion-matricula-content-component';
import { CancelacionMatriculaService } from '../../../../core/services/cancelacion-matricula-service';
import { ErrorHandlerService } from '../../../../core/services/error-handler-service';
import { ToastService } from '../../../../core/services/toast-service';
import { FormularioCancelacionMatriculaDTORespuesta } from '../../../../core/models/CancelacionMatricula/DTOResponse/FormularioCancelacionMatriculaDTORespuesta';
import { InputAnexoUploadComponent } from '../../../inputs/input-anexo-upload-component/input-anexo-upload-component';
import { VIGENCIA_PAZ_Y_SALVOS } from '../../../../core/constantes/textos-cancelacion-matricula';

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

  it('la seccion de anexos avisa la vigencia de los paz y salvos antes de los campos de carga', () => {
    const fixture = crear();
    const nota = fixture.nativeElement.querySelector('[data-nota="vigencia-paz-y-salvos"]') as HTMLElement;
    expect(nota.textContent!.trim()).toBe(VIGENCIA_PAZ_Y_SALVOS);
    expect(nota.classList).toContain('nota-aviso');
    const primerCampo = fixture.nativeElement.querySelector('[data-campo-anexo]') as HTMLElement;
    expect(nota.parentElement).toBe(primerCampo.parentElement);
    expect(nota.compareDocumentPosition(primerCampo) & Node.DOCUMENT_POSITION_FOLLOWING).toBeTruthy();
    expect(fixture.nativeElement.textContent).not.toContain('SIMCA');
  });

  it('al final del formulario muestra la normativa aplicable con sus encabezados en negrita y deja los botones despues', () => {
    const fixture = crear();
    const bloque = fixture.nativeElement.querySelector('[data-normativa]') as HTMLElement;
    expect(bloque.querySelector('h3')!.textContent!.trim()).toBe('Normativa aplicable');
    const parrafos: HTMLElement[] = Array.from(bloque.querySelectorAll('[data-parrafo-normativa]'));
    expect(parrafos.map(p => p.querySelector('b')!.textContent!.trim()))
      .toEqual(['ARTÍCULO 1º:', 'PARÁGRAFO:', 'ARTÍCULO 2:', 'ARTÍCULO 3:', 'ARTÍCULO 4:']);
    expect(parrafos[0].textContent!.trim()).toBe('ARTÍCULO 1º: Cuando un estudiante desee CANCELAR LA MATRÍCULA, podrá solicitarlo por escrito al Decano de la respectiva facultad, hasta el ÚLTIMO DÍA DE CLASES del periodo lectivo correspondiente, exponiendo los motivos que lo inducen a ello y adjuntando el paz y salvo general de la Universidad, así como el carné estudiantil.');
    expect(parrafos[1].textContent).toContain('el cupón denominado: “Confirmación de la Intervención Psicosocial”.');
    expect(parrafos[3].textContent).toContain('sólo podrá ingresar de nuevo al programa sometiéndose al proceso de admisión vigente.');
    expect(parrafos[4].textContent).toContain('será calificado con nota cero punto cero (0.0) en los exámenes dejados de presentar');
    const literales: string[] = Array.from(bloque.querySelectorAll('.normativa-literales li')).map((li: any) => li.textContent.trim());
    expect(literales.map(l => l.slice(0, 2))).toEqual(['a.', 'b.', 'c.', 'd.']);
    expect(literales[3]).toBe('d. Se consideran como no cursadas las restantes.');
    expect(parrafos[2].nextElementSibling!.classList).toContain('normativa-literales');
    const cuerpo = bloque.querySelector('.normativa') as HTMLElement;
    expect(getComputedStyle(cuerpo).overflowY).toBe('auto');
    expect(getComputedStyle(cuerpo).maxHeight).toBe('260px');
    const pie = fixture.nativeElement.querySelector('.pie-formulario') as HTMLElement;
    expect(bloque.compareDocumentPosition(pie) & Node.DOCUMENT_POSITION_FOLLOWING).toBeTruthy();
    expect(fixture.nativeElement.querySelector('#acepta-advertencia')).toBeTruthy();
    expect(fixture.nativeElement.textContent).toContain('Leí y entiendo la advertencia.');
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
    expect(fixture.nativeElement.querySelectorAll('[data-error-anexo]').length).toBe(0);
    for (const campo of Array.from(fixture.nativeElement.querySelectorAll('[data-campo-anexo]')) as HTMLElement[]) {
      expect((campo.textContent ?? '').split('Este anexo es obligatorio').length - 1).toBe(1);
    }
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

  it('tras radicar no deja el formulario limpio con errores', () => {
    servicio.radicar.and.returnValue(of({ uuidSolicitudAcademica: 's-9', radicado: '2026-CM-0009' }));
    const fixture = crear();
    llenar(fixture.componentInstance);
    fixture.componentInstance.radicar();
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[data-estado="radicada"]').textContent).toContain('2026-CM-0009');
    expect(fixture.nativeElement.querySelectorAll('[data-campo-anexo]').length).toBe(0);
    expect(fixture.nativeElement.textContent).not.toContain('Este anexo es obligatorio');
  });

  it('muestra el stepper guia, los datos del estudiante con la nota de DARCA y avanza al completar', () => {
    const fixture = crear();
    const estados = () => Array.from(fixture.nativeElement.querySelectorAll('app-stepper-formulario-component li'))
      .map((li: any) => li.getAttribute('data-paso'));
    expect(estados()).toEqual(['hecho', 'hecho', 'activo', 'pendiente', 'pendiente']);
    expect(fixture.nativeElement.querySelector('[data-nota-darca]').textContent).toContain('corregirlo ante DARCA');
    const valores = Array.from(fixture.nativeElement.querySelectorAll('[data-datos-sesion] input')).map((i: any) => i.value);
    expect(valores).toContain('FIET');

    llenar(fixture.componentInstance);
    fixture.detectChanges();
    expect(estados()).toEqual(['hecho', 'hecho', 'hecho', 'hecho', 'hecho']);
  });

  it('Volver lleva a Mis solicitudes', () => {
    const fixture = crear();
    (fixture.nativeElement.querySelector('[data-boton-volver] button') as HTMLButtonElement).click();
    expect(router.navigate).toHaveBeenCalledWith(['/estudiante/solicitudes']);
  });
});

@Component({ template: '<p data-destino>Detalle</p>' })
class DestinoComponent {}

describe('EstCancelacionMatriculaContentComponent con el Router real', () => {
  let servicio: jasmine.SpyObj<CancelacionMatriculaService>;
  const entrada = { reset: () => undefined } as unknown as InputAnexoUploadComponent;

  function configurar(detallePermitido: boolean): void {
    servicio = jasmine.createSpyObj<CancelacionMatriculaService>('CancelacionMatriculaService', ['getFormulario', 'radicar']);
    servicio.getFormulario.and.returnValue(of(formulario()));
    servicio.radicar.and.returnValue(of({ uuidSolicitudAcademica: 's-9', radicado: '2026-CM-0009' }));
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([
          { path: 'estudiante/cancelacion-matricula', component: EstCancelacionMatriculaContentComponent },
          { path: 'estudiante/solicitudes/:uuid', component: DestinoComponent, canActivate: [() => detallePermitido] }
        ]),
        { provide: CancelacionMatriculaService, useValue: servicio },
        { provide: ErrorHandlerService, useValue: jasmine.createSpyObj('ErrorHandlerService', ['handleError']) },
        { provide: ToastService, useValue: jasmine.createSpyObj('ToastService', ['showError', 'showSuccess']) }
      ]
    });
  }

  async function radicarDesdeLaRuta(): Promise<RouterTestingHarness> {
    const harness = await RouterTestingHarness.create();
    const componente = await harness.navigateByUrl('/estudiante/cancelacion-matricula', EstCancelacionMatriculaContentComponent);
    componente.alCambiarMotivo('Problemas de salud');
    componente.campos.forEach((campo, i) => componente.alSeleccionarAnexo(campo, archivo(i === 5 ? 'carne.jpeg' : `anexo${i}.pdf`), entrada));
    componente.aceptaAdvertencia = true;
    componente.radicar();
    await harness.fixture.whenStable();
    harness.detectChanges();
    return harness;
  }

  it('navega de verdad al detalle de la solicitud radicada', async () => {
    configurar(true);
    const harness = await radicarDesdeLaRuta();
    expect(TestBed.inject(Router).url).toBe('/estudiante/solicitudes/s-9');
    expect(harness.routeNativeElement?.querySelector('[data-destino]')).toBeTruthy();
  });

  it('si la navegacion no ocurre muestra el radicado y no el formulario limpio', async () => {
    configurar(false);
    const harness = await radicarDesdeLaRuta();
    expect(TestBed.inject(Router).url).toBe('/estudiante/cancelacion-matricula');
    const pantalla = harness.routeNativeElement as HTMLElement;
    expect(pantalla.querySelector('[data-estado="radicada"]')?.textContent).toContain('2026-CM-0009');
    expect(pantalla.querySelectorAll('[data-campo-anexo]').length).toBe(0);
    expect(pantalla.textContent).not.toContain('Este anexo es obligatorio');
  });
});
