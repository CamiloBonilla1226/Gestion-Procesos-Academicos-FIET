import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../enviroments/environment';
import { FuncionarioAcademicoService } from './funcionario-academico-service';

describe('FuncionarioAcademicoService', () => {
  const url = `${environment.apiUrl}/funcionarios-academicos`;
  let servicio: FuncionarioAcademicoService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    servicio = TestBed.inject(FuncionarioAcademicoService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('filtra por cargo y nunca manda dependencia', () => {
    servicio.getFuncionariosAcademicosFiltrados({ nombre: '', apellido: null, cargo: 'Secretaría' }, 0, 10).subscribe();
    const req = http.expectOne(r => r.url === `${url}/filtro`);
    expect(req.request.params.get('cargo')).toBe('Secretaría');
    expect(req.request.params.has('dependencia')).toBeFalse();
    expect(req.request.params.has('nombre')).toBeFalse();
    req.flush({ content: [], totalElements: 0 });
  });

  it('actualiza con PUT solo los campos que recibe', () => {
    servicio.actualizarFuncionarioAcademico('f-1', { telefono: '3001234', cargo: 'Decanatura' }).subscribe();
    const req = http.expectOne(`${url}/f-1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual({ telefono: '3001234', cargo: 'Decanatura' });
    req.flush({});
  });
});
