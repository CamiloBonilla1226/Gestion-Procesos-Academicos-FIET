import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../enviroments/environment';
import { CatalogoAcademicoService } from './catalogo-academico-service';
import { ProgramaAcademicoDTORespuesta } from '../models/CatalogoAcademico/DTOResponse/ProgramaAcademicoDTORespuesta';

describe('CatalogoAcademicoService', () => {
  let servicio: CatalogoAcademicoService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    servicio = TestBed.inject(CatalogoAcademicoService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('pide el catalogo de programas con GET y entrega uuid, nombre y sigla', () => {
    const programas: ProgramaAcademicoDTORespuesta[] = [
      { uuidProgramaAcademico: 'uuid-isi', nombre: 'Ingeniería de Sistemas', sigla: 'ISI' },
      { uuidProgramaAcademico: 'uuid-tte', nombre: 'Tecnología en Telemática', sigla: 'TTE' }
    ];
    let recibidos: ProgramaAcademicoDTORespuesta[] = [];
    servicio.getProgramas().subscribe(lista => (recibidos = lista));
    const req = http.expectOne(`${environment.apiUrl}/catalogos-academicos/programas`);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.keys()).toEqual([]);
    req.flush(programas);
    expect(recibidos).toEqual(programas);
  });
});
