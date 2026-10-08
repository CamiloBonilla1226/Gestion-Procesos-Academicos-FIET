import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../enviroments/environment';
import { CancelacionMatriculaService } from './cancelacion-matricula-service';

describe('CancelacionMatriculaService', () => {
  let servicio: CancelacionMatriculaService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    servicio = TestBed.inject(CancelacionMatriculaService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('arma la radicacion con motivo, una parte por tipo de anexo y partes soporte', () => {
    const uuidBibliotecas = '11111111-1111-1111-1111-111111111111';
    const uuidDarca = '22222222-2222-2222-2222-222222222222';
    const bibliotecas = new File(['%PDF-'], 'bibliotecas.pdf', { type: 'application/pdf' });
    const darca = new File(['png'], 'carne.png', { type: 'image/png' });
    const soporte1 = new File(['%PDF-'], 'incapacidad.pdf', { type: 'application/pdf' });
    const soporte2 = new File(['jpg'], 'foto.jpg', { type: 'image/jpeg' });

    servicio.radicar({
      motivo: 'Motivos personales',
      anexos: [
        { uuidTipoAnexoAcademico: uuidBibliotecas, archivo: bibliotecas },
        { uuidTipoAnexoAcademico: uuidDarca, archivo: darca }
      ],
      soportes: [soporte1, soporte2]
    }).subscribe();

    const req = http.expectOne(`${environment.apiUrl}/cancelaciones-matricula`);
    const cuerpo = req.request.body as FormData;

    expect(req.request.method).toBe('POST');
    expect(req.request.headers.has('Content-Type')).toBeFalse();
    expect(cuerpo instanceof FormData).toBeTrue();
    expect(cuerpo.get('motivo')).toBe('Motivos personales');
    expect((cuerpo.get(uuidBibliotecas) as File).name).toBe('bibliotecas.pdf');
    expect((cuerpo.get(uuidDarca) as File).name).toBe('carne.png');
    expect(cuerpo.getAll('soporte').map(f => (f as File).name)).toEqual(['incapacidad.pdf', 'foto.jpg']);
    expect(Array.from(cuerpo.keys())).toEqual(['motivo', uuidBibliotecas, uuidDarca, 'soporte', 'soporte']);

    req.flush({ uuidSolicitudAcademica: 'x', radicado: '2026-CM-0001' });
  });

  it('envia responder sin cuerpo', () => {
    const uuid = '33333333-3333-3333-3333-333333333333';
    servicio.enviarRespuesta(uuid).subscribe();
    const req = http.expectOne(`${environment.apiUrl}/cancelaciones-matricula/${uuid}/funcionario/responder`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toBeNull();
    req.flush({});
  });
});
