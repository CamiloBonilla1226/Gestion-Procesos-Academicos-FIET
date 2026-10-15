import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { NotificacionDocenteService } from './notificacion-docente-service';
import { environment } from '../../../enviroments/environment';

describe('NotificacionDocenteService', () => {
  const url = `${environment.apiUrl}/notificaciones-docente`;
  let servicio: NotificacionDocenteService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    servicio = TestBed.inject(NotificacionDocenteService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('pide las notificaciones del docente autenticado', () => {
    servicio.getMisNotificaciones().subscribe();
    const req = http.expectOne(url);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('pide el detalle y marca como leida con PATCH sin cuerpo', () => {
    servicio.getNotificacion('n-1').subscribe();
    http.expectOne(`${url}/n-1`).flush({});
    servicio.marcarLeida('n-1').subscribe();
    const req = http.expectOne(`${url}/n-1/leida`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toBeNull();
    req.flush({});
  });

  it('descarga el anexo como blob y convierte el error en JSON', () => {
    let codigo: unknown;
    servicio.descargarAnexo('n-1', 'a-1').subscribe({ error: err => (codigo = err.error?.codigoError) });
    const req = http.expectOne(`${url}/n-1/anexos/a-1`);
    expect(req.request.responseType).toBe('blob');
    req.flush(new Blob([JSON.stringify({ codigoError: '3' })], { type: 'application/json' }), { status: 500, statusText: 'Error' });
    return new Promise<void>(resolve => setTimeout(() => {
      expect(codigo).toBe('3');
      resolve();
    }, 50));
  });
});
