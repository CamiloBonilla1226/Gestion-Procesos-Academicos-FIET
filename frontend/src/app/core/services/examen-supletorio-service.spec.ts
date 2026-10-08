import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../enviroments/environment';
import { ExamenSupletorioService } from './examen-supletorio-service';

describe('ExamenSupletorioService', () => {
  const url = `${environment.apiUrl}/examenes-supletorios`;
  const uuidFor23 = '44444444-4444-4444-4444-444444444444';
  const uuidCruce = '55555555-5555-5555-5555-555555555555';
  const uuidJustificacion = '66666666-6666-6666-6666-666666666666';
  let servicio: ExamenSupletorioService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    servicio = TestBed.inject(ExamenSupletorioService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('con causa cruce envia los datos del examen cruzado y los anexos por uuid', () => {
    servicio.radicar({
      asignaturaMatriculada: 'am-1',
      fechaExamenNoPresentado: '2026-10-06',
      tipoCausa: 'cruce',
      asignaturaCruzada: 'am-2',
      fechaExamenCruzada: '2026-10-06',
      horaExamenCruzada: '14:00',
      anexos: [
        { uuidTipoAnexoAcademico: uuidFor23, archivo: new File(['%PDF-'], 'for23.pdf') },
        { uuidTipoAnexoAcademico: uuidCruce, archivo: new File(['%PDF-'], 'docente.pdf') }
      ]
    }).subscribe();

    const req = http.expectOne(url);
    const cuerpo = req.request.body as FormData;

    expect(req.request.method).toBe('POST');
    expect(req.request.headers.has('Content-Type')).toBeFalse();
    expect(Array.from(cuerpo.keys())).toEqual([
      'asignaturaMatriculada', 'fechaExamenNoPresentado', 'tipoCausa',
      'asignaturaCruzada', 'fechaExamenCruzada', 'horaExamenCruzada', uuidFor23, uuidCruce
    ]);
    expect(cuerpo.get('horaExamenCruzada')).toBe('14:00');
    expect((cuerpo.get(uuidCruce) as File).name).toBe('docente.pdf');

    req.flush({ uuidSolicitudAcademica: 'x', radicado: '2026-ES-0001' });
  });

  it('con causa otra no envia ningun dato de cruce aunque venga en el objeto', () => {
    servicio.radicar({
      asignaturaMatriculada: 'am-1',
      fechaExamenNoPresentado: '2026-10-06',
      tipoCausa: 'otra',
      asignaturaCruzada: 'am-2',
      fechaExamenCruzada: '2026-10-06',
      horaExamenCruzada: '14:00',
      anexos: [
        { uuidTipoAnexoAcademico: uuidFor23, archivo: new File(['%PDF-'], 'for23.pdf') },
        { uuidTipoAnexoAcademico: uuidJustificacion, archivo: new File(['%PDF-'], 'incapacidad.pdf') }
      ]
    }).subscribe();

    const cuerpo = http.expectOne(url).request.body as FormData;

    expect(Array.from(cuerpo.keys())).toEqual([
      'asignaturaMatriculada', 'fechaExamenNoPresentado', 'tipoCausa', uuidFor23, uuidJustificacion
    ]);
    expect(cuerpo.get('tipoCausa')).toBe('otra');
  });

  it('aprueba el comprobante sin cuerpo cuando no hay fecha acordada', () => {
    servicio.aprobarComprobante('s-1').subscribe();
    const req = http.expectOne(`${url}/s-1/funcionario/comprobante/aprobar`);
    expect(req.request.body).toBeNull();
    req.flush({});
  });
});
