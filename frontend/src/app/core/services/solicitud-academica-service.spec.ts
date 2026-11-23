import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { SolicitudAcademicaService } from './solicitud-academica-service';
import { environment } from '../../../enviroments/environment';

describe('SolicitudAcademicaService, borrado de respuestas', () => {
  const url = `${environment.apiUrl}/solicitudes-academicas/funcionario/respuestas`;
  let servicio: SolicitudAcademicaService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    servicio = TestBed.inject(SolicitudAcademicaService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('cuenta con GET y el rango en los parametros', () => {
    servicio.contarRespuestasEliminables('2026-10-01', '2026-10-21').subscribe();
    const req = http.expectOne(r => r.url === `${url}/conteo`);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('desde')).toBe('2026-10-01');
    expect(req.request.params.get('hasta')).toBe('2026-10-21');
    req.flush({ cantidad: 3, desde: '2026-10-01', hasta: '2026-10-21' });
  });

  it('elimina con DELETE y el mismo rango', () => {
    servicio.eliminarRespuestas('2026-10-01', '2026-10-21').subscribe();
    const req = http.expectOne(r => r.url === url);
    expect(req.request.method).toBe('DELETE');
    expect(req.request.params.get('desde')).toBe('2026-10-01');
    expect(req.request.params.get('hasta')).toBe('2026-10-21');
    req.flush({ cantidad: 3, desde: '2026-10-01', hasta: '2026-10-21' });
  });
});
