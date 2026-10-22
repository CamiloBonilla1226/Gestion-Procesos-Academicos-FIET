import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { CancelacionAsignaturaService } from './cancelacion-asignatura-service';
import { environment } from '../../../enviroments/environment';

describe('CancelacionAsignaturaService, notificacion de la Resolucion', () => {
  const url = `${environment.apiUrl}/cancelaciones-asignatura/s-1/funcionario`;
  const programacion = { fechaProgramada: '2026-10-21T08:30', numeroResolucion: '123-26', anioResolucion: 2026, decision: 'aprueba la' };
  let servicio: CancelacionAsignaturaService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    servicio = TestBed.inject(CancelacionAsignaturaService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('pide la plantilla y la notificacion, y un 204 llega como null', () => {
    servicio.getPlantillaNotificacion('s-1').subscribe();
    expect(http.expectOne(`${url}/notificacion/plantilla`).request.method).toBe('GET');
    let recibida: unknown = 'sin respuesta';
    servicio.getNotificacion('s-1').subscribe(notificacion => (recibida = notificacion));
    http.expectOne(`${url}/notificacion`).flush(null, { status: 204, statusText: 'No Content' });
    expect(recibida).toBeNull();
  });

  it('reprograma con PUT y el cuerpo de la programacion', () => {
    servicio.reprogramarNotificacion('s-1', programacion).subscribe();
    const req = http.expectOne(`${url}/notificacion`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(programacion);
    req.flush({});
  });

  it('registra la firma con el PDF en la parte archivo y reintenta sin cuerpo', () => {
    const archivo = new File(['%PDF-'], 'firmada.pdf', { type: 'application/pdf' });
    servicio.registrarFirma('s-1', archivo).subscribe();
    const firma = http.expectOne(`${url}/notificacion/firma`);
    expect(firma.request.method).toBe('POST');
    expect((firma.request.body as FormData).get('archivo')).toEqual(archivo);
    expect((firma.request.body as FormData).has('textoDarca')).toBeFalse();
    firma.flush({});
    servicio.registrarFirma('s-1', archivo, 'Texto para DARCA {fecha}').subscribe();
    const conTexto = http.expectOne(`${url}/notificacion/firma`);
    expect((conTexto.request.body as FormData).get('textoDarca')).toBe('Texto para DARCA {fecha}');
    conTexto.flush({});
    servicio.reintentarNotificacion('s-1').subscribe();
    const reintento = http.expectOne(`${url}/notificacion/reintentar`);
    expect(reintento.request.method).toBe('POST');
    expect(reintento.request.body).toBeNull();
    reintento.flush({});
  });

  it('la respuesta final lleva el mensaje y la programacion en el cuerpo', () => {
    servicio.enviarRespuesta('s-1', { observacion: null, enviarMensaje: true, mensaje: 'Hola', programacion }).subscribe();
    const req = http.expectOne(`${url}/responder`);
    expect(req.request.body).toEqual({ observacion: null, enviarMensaje: true, mensaje: 'Hola', programacion });
    req.flush({});
  });
});
