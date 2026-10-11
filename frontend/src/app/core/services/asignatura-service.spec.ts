import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../enviroments/environment';
import { AsignaturaService } from './asignatura-service';

const URL = `${environment.apiUrl}/asignaturas`;

describe('AsignaturaService', () => {
  let servicio: AsignaturaService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    servicio = TestBed.inject(AsignaturaService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('crea con nombre y programa, sin codigo', () => {
    servicio.crearAsignatura({ nombreAsignatura: 'Compiladores', uuidProgramaAcademico: 'uuid-isi' }).subscribe();
    const req = http.expectOne(URL);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ nombreAsignatura: 'Compiladores', uuidProgramaAcademico: 'uuid-isi' });
  });

  it('actualiza solo el nombre', () => {
    servicio.actualizarAsignatura('a-1', { nombreAsignatura: 'Compiladores II' }).subscribe();
    const req = http.expectOne(`${URL}/a-1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual({ nombreAsignatura: 'Compiladores II' });
  });

  it('filtra por texto y programa y omite los vacios', () => {
    servicio.getAsignaturasFiltradas({ texto: 'calc', uuidProgramaAcademico: 'uuid-isi' }, 0, 10).subscribe();
    const req = http.expectOne(r => r.url === `${URL}/filtro`);
    expect(req.request.params.get('texto')).toBe('calc');
    expect(req.request.params.get('uuidProgramaAcademico')).toBe('uuid-isi');
    expect(req.request.params.get('pagina')).toBe('0');
    expect(req.request.params.get('tamanio')).toBe('10');
    servicio.getAsignaturasFiltradas({ texto: '', uuidProgramaAcademico: '' }, 1, 10).subscribe();
    const sinFiltro = http.expectOne(r => r.url === `${URL}/filtro`);
    expect(sinFiltro.request.params.keys().sort()).toEqual(['pagina', 'tamanio']);
  });

  it('lista las asignaturas de un programa sin paginar', () => {
    servicio.getAsignaturasPorPrograma('uuid-isi').subscribe();
    const req = http.expectOne(`${URL}/programa/uuid-isi`);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.keys()).toEqual([]);
  });

  it('carga el Excel de asignaturas como multipart con la parte file', () => {
    const archivo = new File([new Uint8Array(4)], 'asignaturas.xlsx');
    servicio.crearAsignaturasDesdeArchivo(archivo).subscribe();
    const req = http.expectOne(`${URL}/cargar/archivo`);
    expect(req.request.method).toBe('POST');
    const cuerpo = req.request.body as FormData;
    expect(Array.from(cuerpo.keys())).toEqual(['file']);
    expect((cuerpo.get('file') as File).name).toBe('asignaturas.xlsx');
    expect(req.request.headers.has('Content-Type')).toBeFalse();
  });
});
