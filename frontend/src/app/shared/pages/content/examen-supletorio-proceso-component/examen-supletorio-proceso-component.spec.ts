import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, TestRequest, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { ExamenSupletorioProcesoComponent } from './examen-supletorio-proceso-component';
import { ErrorHandlerService } from '../../../../core/services/error-handler-service';
import { ToastService } from '../../../../core/services/toast-service';
import { SolicitudAcademicaDetalleDTORespuesta } from '../../../../core/models/SolicitudAcademica/DTOResponse/SolicitudAcademicaDetalleDTORespuesta';
import { AnexoAcademicoDTORespuesta } from '../../../../core/models/SolicitudAcademica/DTOResponse/AnexoAcademicoDTORespuesta';
import { ExamenSupletorioDetalleDTORespuesta } from '../../../../core/models/ExamenSupletorio/DTOResponse/ExamenSupletorioDetalleDTORespuesta';
import { InputAnexoUploadComponent } from '../../../inputs/input-anexo-upload-component/input-anexo-upload-component';
import { environment } from '../../../../../enviroments/environment';

const API = environment.apiUrl;
const URL = `${API}/examenes-supletorios/s-1`;
const URL_TIPOS = `${API}/catalogos-academicos/tipos-solicitud/t-sup/tipos-anexo`;
const URL_ANEXOS = `${API}/solicitudes-academicas/s-1/anexos`;

function solicitud(acciones: string[], etapaCodigo = 'RADICADA', anexos: AnexoAcademicoDTORespuesta[] = []): SolicitudAcademicaDetalleDTORespuesta {
  return {
    uuidSolicitudAcademica: 's-1', radicado: '2026-ES-0001', uuidTipoSolicitudAcademica: 't-sup',
    tipoSolicitud: 'Examen Supletorio', fechaCreacion: '2026-10-08T10:00:00', etiqueta: 'Pendiente', etapaCodigo,
    estudiante: {
      uuidUsuario: 'e', nombres: 'María', apellidos: 'Pérez', codigoEstudiantil: '1', programaAcademico: 'P',
      semestre: '1', correoElectronico: 'm@u.co'
    },
    anexos, tieneResolucion: false, puedeDescargarResolucion: false, accionesDisponibles: acciones
  };
}

function detalleCruce(): ExamenSupletorioDetalleDTORespuesta {
  return {
    solicitud: solicitud([]),
    asignatura: { uuidAsignaturaMatriculada: 'am-1', codigoAsignatura: 'A1', nombreAsignatura: 'Cálculo', grupo: 'A' },
    tipoCausa: 'cruce',
    fechaExamenNoPresentado: '2026-10-06',
    cruce: {
      asignatura: { uuidAsignaturaMatriculada: 'am-2', codigoAsignatura: 'A2', nombreAsignatura: 'Física', grupo: 'B' },
      fechaExamenCruzada: '2026-10-06',
      horaExamenCruzada: '14:00'
    },
    fechaAcordadaExamen: null
  };
}

const TIPOS = [
  { uuidTipoAnexoAcademico: 'u-comprobante', uuidTipoSolicitudAcademica: 't-sup', nombre: 'Comprobante de pago', formatosPermitidos: 'pdf,jpg,png', obligatorio: false },
  { uuidTipoAnexoAcademico: 'u-for23', uuidTipoSolicitudAcademica: 't-sup', nombre: 'Formato PM-FO-4-FOR-23', formatosPermitidos: 'pdf,jpg,png', obligatorio: true },
  { uuidTipoAnexoAcademico: 'u-recibo', uuidTipoSolicitudAcademica: 't-sup', nombre: 'Recibo de pago', formatosPermitidos: 'pdf', obligatorio: false }
];

function anexo(uuidTipo: string, tipo: string): AnexoAcademicoDTORespuesta {
  return {
    uuidAnexoAcademico: `a-${uuidTipo}`, nombreArchivo: 'x.pdf', uuidTipoAnexoAcademico: uuidTipo, tipoAnexo: tipo,
    tipoArchivo: 'application/pdf', tamanioBytes: 10, fechaSubida: '2026-10-09T08:00:00'
  };
}

describe('ExamenSupletorioProcesoComponent', () => {
  let http: HttpTestingController;
  let errores: jasmine.SpyObj<ErrorHandlerService>;
  let toast: jasmine.SpyObj<ToastService>;
  let recargar: jasmine.Spy;
  const entrada = { reset: () => undefined } as unknown as InputAnexoUploadComponent;

  beforeEach(() => {
    errores = jasmine.createSpyObj<ErrorHandlerService>('ErrorHandlerService', ['handleError']);
    toast = jasmine.createSpyObj<ToastService>('ToastService', ['showSuccess', 'showError']);
    recargar = jasmine.createSpy('recargar');
    TestBed.configureTestingModule({
      imports: [ExamenSupletorioProcesoComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideNoopAnimations(),
        { provide: ErrorHandlerService, useValue: errores },
        { provide: ToastService, useValue: toast }
      ]
    });
    http = TestBed.inject(HttpTestingController);
  });

  function crear(
    datos: SolicitudAcademicaDetalleDTORespuesta,
    rol = 'FUNCIONARIO',
    detalle = detalleCruce()
  ): ComponentFixture<ExamenSupletorioProcesoComponent> {
    const fixture = TestBed.createComponent(ExamenSupletorioProcesoComponent);
    fixture.componentRef.setInput('uuidSolicitud', 's-1');
    fixture.componentRef.setInput('solicitud', datos);
    fixture.componentRef.setInput('rol', rol);
    fixture.componentRef.setInput('recargar', recargar);
    fixture.detectChanges();
    http.expectOne(URL).flush({ ...detalle, solicitud: datos });
    fixture.detectChanges();
    return fixture;
  }

  function botones(fixture: ComponentFixture<ExamenSupletorioProcesoComponent>): string[] {
    return Array.from(fixture.nativeElement.querySelectorAll('[data-accion]')).map((b: any) => b.getAttribute('data-accion'));
  }

  function accion(ruta: string): TestRequest {
    const req = http.expectOne(`${URL}/${ruta}`);
    expect(req.request.method).toBe('POST');
    return req;
  }

  function terminar(req: TestRequest): void {
    req.flush(detalleCruce());
    expect(recargar).toHaveBeenCalled();
  }

  it('muestra asignatura, causa, fechas en dd/MM/aaaa y el cruce', () => {
    const fixture = crear(solicitud([]), 'ESTUDIANTE');
    const contenido = fixture.nativeElement;
    expect(contenido.querySelector('[data-asignatura]').textContent).toContain('A1 - Cálculo (grupo A)');
    expect(contenido.querySelector('[data-fecha-examen]').textContent).toContain('06/10/2026');
    expect(contenido.querySelector('[data-causa]').textContent).toContain('Cruce');
    expect(contenido.querySelector('[data-cruce]').textContent).toContain('A2 - Física (grupo B)');
    expect(contenido.querySelector('[data-cruce-fecha]').textContent).toContain('06/10/2026 14:00');
    expect(contenido.querySelector('[data-fecha-acordada]')).toBeNull();
  });

  it('con causa otra no muestra cruce y muestra la fecha acordada si existe', () => {
    const fixture = crear(solicitud([], 'APROBADA'), 'DECANO', {
      ...detalleCruce(), tipoCausa: 'otra', cruce: null, fechaAcordadaExamen: '2026-10-20'
    });
    expect(fixture.nativeElement.querySelector('[data-cruce]')).toBeNull();
    expect(fixture.nativeElement.querySelector('[data-causa]').textContent).toContain('Otra causa');
    expect(fixture.nativeElement.querySelector('[data-fecha-acordada]').textContent).toContain('20/10/2026');
  });

  it('pinta solo las acciones de accionesDisponibles, tambien la del Estudiante', () => {
    expect(botones(crear(solicitud(['RECHAZAR_FUNCIONARIO', 'REMITIR_DECANO'])))).toEqual(['RECHAZAR_FUNCIONARIO', 'REMITIR_DECANO']);
    expect(botones(crear(solicitud(['SUBIR_COMPROBANTE'], 'PENDIENTE_PAGO'), 'ESTUDIANTE'))).toEqual(['SUBIR_COMPROBANTE']);
    expect(botones(crear(solicitud(['APROBAR_COMPROBANTE', 'RECHAZAR_COMPROBANTE', 'OTRA'], 'EN_VERIFICACION_PAGO'))))
      .toEqual(['APROBAR_COMPROBANTE', 'RECHAZAR_COMPROBANTE']);
  });

  it('el Funcionario rechaza con observacion obligatoria y sin Resolucion', () => {
    const fixture = crear(solicitud(['RECHAZAR_FUNCIONARIO']));
    const componente = fixture.componentInstance;
    (fixture.nativeElement.querySelector('[data-accion="RECHAZAR_FUNCIONARIO"] button') as HTMLButtonElement).click();
    expect(componente.dialogoVisible).toBeTrue();
    componente.confirmar();
    expect(componente.errores['observacion']).toContain('obligatoria');
    componente.observacion = 'a'.repeat(501);
    componente.confirmar();
    expect(componente.errores['observacion']).toContain('500');
    http.expectNone(`${URL}/funcionario/rechazar`);
    componente.observacion = 'No adjuntó el FOR-23 firmado';
    componente.confirmar();
    const req = accion('funcionario/rechazar');
    expect(req.request.body).toEqual({ observacion: 'No adjuntó el FOR-23 firmado' });
    http.expectNone(`${API}/solicitudes-academicas/s-1/resolucion`);
    terminar(req);
    expect(componente.dialogoVisible).toBeFalse();
  });

  it('remitir exige la casilla de requisitos verificados y envia true', () => {
    const componente = crear(solicitud(['REMITIR_DECANO'])).componentInstance;
    componente.abrir('REMITIR_DECANO');
    componente.confirmar();
    expect(componente.errores['requisitos']).toBeTruthy();
    http.expectNone(`${URL}/funcionario/remitir`);
    componente.alCambiarVerificacion({ target: { checked: true } } as unknown as Event);
    componente.confirmar();
    const req = accion('funcionario/remitir');
    expect(req.request.body).toEqual({ requisitosVerificados: true, observacion: null });
    terminar(req);
  });

  it('el Decano aprueba sin cuerpo o con observacion, y rechaza con observacion obligatoria', () => {
    const componente = crear(solicitud(['APROBAR_DECANO', 'RECHAZAR_DECANO'], 'EN_REVISION_DECANO'), 'DECANO').componentInstance;
    componente.abrir('APROBAR_DECANO');
    componente.confirmar();
    const sinCuerpo = accion('decano/aprobar');
    expect(sinCuerpo.request.body).toBeNull();
    sinCuerpo.flush(detalleCruce());
    componente.abrir('APROBAR_DECANO');
    componente.observacion = 'Causa justificada';
    componente.confirmar();
    const conObservacion = accion('decano/aprobar');
    expect(conObservacion.request.body).toEqual({ observacion: 'Causa justificada' });
    conObservacion.flush(detalleCruce());
    componente.abrir('RECHAZAR_DECANO');
    componente.confirmar();
    http.expectNone(`${URL}/decano/rechazar`);
    componente.observacion = 'No procede';
    componente.confirmar();
    const req = accion('decano/rechazar');
    expect(req.request.body).toEqual({ observacion: 'No procede' });
    terminar(req);
  });

  it('enviar la respuesta va sin cuerpo y sin Resolucion', () => {
    const componente = crear(solicitud(['ENVIAR_RESPUESTA'], 'RECHAZADA_POR_DECANO')).componentInstance;
    componente.abrir('ENVIAR_RESPUESTA');
    componente.confirmar();
    const req = accion('funcionario/responder');
    expect(req.request.body).toBeNull();
    terminar(req);
  });

  it('el recibo toma su uuid del catalogo, sube primero el anexo y luego llama la accion', () => {
    const fixture = crear(solicitud(['ENVIAR_RECIBO'], 'APROBADA_POR_DECANO'));
    const componente = fixture.componentInstance;
    componente.abrir('ENVIAR_RECIBO');
    expect(componente.dialogoVisible).toBeFalse();
    http.expectOne(URL_TIPOS).flush(TIPOS);
    fixture.detectChanges();
    expect(componente.dialogoVisible).toBeTrue();
    expect(componente.tipoAnexoPago?.uuidTipoAnexoAcademico).toBe('u-recibo');
    componente.confirmar();
    expect(componente.errores['anexoPago']).toContain('recibo de pago');
    componente.alSeleccionarAnexoPago(new File(['x'], 'recibo.png'), entrada);
    expect(componente.errores['anexoPago']).toContain('Formato no permitido');
    componente.alSeleccionarAnexoPago(new File(['%PDF-'], 'recibo.pdf'), entrada);
    componente.confirmar();
    http.expectNone(`${URL}/funcionario/recibo`);
    const subida = http.expectOne(URL_ANEXOS);
    const cuerpo = subida.request.body as FormData;
    expect(Array.from(cuerpo.keys())).toEqual(['archivo', 'tipoAnexo']);
    expect(cuerpo.get('tipoAnexo')).toBe('u-recibo');
    subida.flush(anexo('u-recibo', 'Recibo de pago'));
    const req = accion('funcionario/recibo');
    expect(req.request.body).toBeNull();
    terminar(req);
  });

  it('si la accion falla despues de subir el recibo, el reintento no lo sube otra vez', () => {
    const componente = crear(solicitud(['ENVIAR_RECIBO'], 'APROBADA_POR_DECANO')).componentInstance;
    componente.abrir('ENVIAR_RECIBO');
    http.expectOne(URL_TIPOS).flush(TIPOS);
    componente.alSeleccionarAnexoPago(new File(['%PDF-'], 'recibo.pdf'), entrada);
    componente.confirmar();
    http.expectOne(URL_ANEXOS).flush(anexo('u-recibo', 'Recibo de pago'));
    accion('funcionario/recibo').flush({ codigoError: '1', mensaje: 'Error interno' }, { status: 500, statusText: 'Error' });
    expect(componente.dialogoVisible).toBeTrue();
    componente.confirmar();
    http.expectNone(URL_ANEXOS);
    terminar(accion('funcionario/recibo'));
  });

  it('el Estudiante sube el comprobante y luego confirma la accion', () => {
    const componente = crear(solicitud(['SUBIR_COMPROBANTE'], 'PENDIENTE_PAGO'), 'ESTUDIANTE').componentInstance;
    componente.abrir('SUBIR_COMPROBANTE');
    http.expectOne(URL_TIPOS).flush(TIPOS);
    componente.alSeleccionarAnexoPago(new File(['x'], 'pago.jpg'), entrada);
    componente.confirmar();
    const subida = http.expectOne(URL_ANEXOS);
    expect((subida.request.body as FormData).get('tipoAnexo')).toBe('u-comprobante');
    subida.flush(anexo('u-comprobante', 'Comprobante de pago'));
    const req = accion('estudiante/comprobante');
    expect(req.request.body).toBeNull();
    terminar(req);
  });

  it('si el catalogo no trae el tipo de anexo muestra el error y no abre el dialogo', () => {
    const componente = crear(solicitud(['SUBIR_COMPROBANTE'], 'PENDIENTE_PAGO'), 'ESTUDIANTE').componentInstance;
    componente.abrir('SUBIR_COMPROBANTE');
    http.expectOne(URL_TIPOS).flush(TIPOS.filter(t => t.uuidTipoAnexoAcademico !== 'u-comprobante'));
    expect(componente.dialogoVisible).toBeFalse();
    expect(errores.handleError).toHaveBeenCalled();
  });

  it('aprobar el comprobante bloquea una fecha acordada anterior al examen y envia la fecha o nada', () => {
    const componente = crear(solicitud(['APROBAR_COMPROBANTE'], 'EN_VERIFICACION_PAGO')).componentInstance;
    componente.abrir('APROBAR_COMPROBANTE');
    componente.fechaAcordada = '2026-10-05';
    componente.confirmar();
    expect(componente.errores['fechaAcordada']).toContain('06/10/2026');
    http.expectNone(`${URL}/funcionario/comprobante/aprobar`);
    componente.fechaAcordada = '2026-10-15';
    componente.confirmar();
    const conFecha = accion('funcionario/comprobante/aprobar');
    expect(conFecha.request.body).toEqual({ fechaAcordadaExamen: '2026-10-15' });
    conFecha.flush(detalleCruce());
    componente.abrir('APROBAR_COMPROBANTE');
    componente.confirmar();
    const req = accion('funcionario/comprobante/aprobar');
    expect(req.request.body).toBeNull();
    terminar(req);
  });

  it('rechazar el comprobante exige observacion', () => {
    const componente = crear(solicitud(['RECHAZAR_COMPROBANTE'], 'EN_VERIFICACION_PAGO')).componentInstance;
    componente.abrir('RECHAZAR_COMPROBANTE');
    componente.confirmar();
    http.expectNone(`${URL}/funcionario/comprobante/rechazar`);
    componente.observacion = 'El valor pagado no coincide';
    componente.confirmar();
    const req = accion('funcionario/comprobante/rechazar');
    expect(req.request.body).toEqual({ observacion: 'El valor pagado no coincide' });
    terminar(req);
  });

  it('un 500 con codigoError va al ErrorHandlerService y deja el dialogo abierto', () => {
    const componente = crear(solicitud(['APROBAR_COMPROBANTE'], 'EN_VERIFICACION_PAGO')).componentInstance;
    componente.abrir('APROBAR_COMPROBANTE');
    componente.confirmar();
    accion('funcionario/comprobante/aprobar').flush(
      { codigoError: '4', mensaje: 'La acción APROBAR_COMPROBANTE no está permitida...' },
      { status: 500, statusText: 'Error' }
    );
    const [error, titulo, mensaje] = errores.handleError.calls.mostRecent().args;
    expect(error.error.mensaje).toContain('no está permitida');
    expect([titulo, mensaje]).toEqual(['Error', 'No se pudo completar la acción']);
    expect(componente.dialogoVisible).toBeTrue();
    expect(componente.enviando).toBeFalse();
    expect(recargar).not.toHaveBeenCalled();
  });

  it('no permite un doble envio mientras la accion esta en curso', () => {
    const componente = crear(solicitud(['ENVIAR_RESPUESTA'], 'RECHAZADA_POR_DECANO')).componentInstance;
    componente.abrir('ENVIAR_RESPUESTA');
    componente.confirmar();
    componente.confirmar();
    expect(http.match(`${URL}/funcionario/responder`).length).toBe(1);
  });

  it('en PENDIENTE_PAGO le indica al Estudiante descargar el recibo, pagar y subir el comprobante', () => {
    const fixture = crear(solicitud(['SUBIR_COMPROBANTE'], 'PENDIENTE_PAGO', [anexo('u-recibo', 'Recibo de pago')]), 'ESTUDIANTE');
    const aviso = fixture.nativeElement.querySelector('[data-aviso-etapa="PENDIENTE_PAGO"]').textContent;
    expect(aviso).toContain('Descarga el recibo');
    expect(aviso).toContain('comprobante');
  });

  it('en EN_VERIFICACION_PAGO avisa que el comprobante esta en verificacion', () => {
    const fixture = crear(solicitud([], 'EN_VERIFICACION_PAGO'), 'ESTUDIANTE');
    expect(fixture.nativeElement.querySelector('[data-aviso-etapa="EN_VERIFICACION_PAGO"]').textContent).toContain('verificando');
  });

  it('con el comprobante rechazado indica radicar una solicitud nueva', () => {
    const conComprobante = crear(solicitud([], 'RECHAZADA', [anexo('u-comprobante', 'Comprobante de pago')]), 'ESTUDIANTE');
    expect(conComprobante.nativeElement.querySelector('[data-aviso-etapa="COMPROBANTE_RECHAZADO"]').textContent)
      .toContain('radicar una solicitud nueva');
    const sinComprobante = crear(solicitud([], 'RECHAZADA'), 'ESTUDIANTE');
    expect(sinComprobante.nativeElement.querySelector('[data-aviso-etapa]')).toBeNull();
  });
});
