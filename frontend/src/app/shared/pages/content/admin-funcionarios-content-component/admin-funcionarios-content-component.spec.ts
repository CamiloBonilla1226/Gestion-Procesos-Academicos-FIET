import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, TestRequest, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { AdminFuncionariosContentComponent } from './admin-funcionarios-content-component';
import { ErrorHandlerService } from '../../../../core/services/error-handler-service';
import { ToastService } from '../../../../core/services/toast-service';
import { environment } from '../../../../../enviroments/environment';

const URL = `${environment.apiUrl}/funcionarios-academicos`;

const FUNCIONARIO = {
  uuidUsuario: 'f-1', nombres: 'Ana', apellidos: 'Ruiz', estado: true, tipoDocumento: 'Cédula de ciudadanía',
  numeroDocumento: '1061', telefono: '3001', correoElectronico: 'ana@u.co', username: 'aruiz', dependencia: 'Vicedecanatura',
  tiposSolicitud: [{ uuidTipoSolicitudAcademica: 't-1', nombre: 'Examen Supletorio' }]
};

describe('AdminFuncionariosContentComponent', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [AdminFuncionariosContentComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideNoopAnimations(),
        { provide: ErrorHandlerService, useValue: jasmine.createSpyObj('ErrorHandlerService', ['handleError']) },
        { provide: ToastService, useValue: jasmine.createSpyObj('ToastService', ['showSuccess', 'showError']) }
      ]
    });
    http = TestBed.inject(HttpTestingController);
  });

  function filtro(): TestRequest {
    return http.expectOne(req => req.url === `${URL}/filtro`);
  }

  function crear(): ComponentFixture<AdminFuncionariosContentComponent> {
    const fixture = TestBed.createComponent(AdminFuncionariosContentComponent);
    fixture.detectChanges();
    filtro().flush({ content: [FUNCIONARIO], totalElements: 1 });
    fixture.detectChanges();
    return fixture;
  }

  it('pinta la tabla con los procesos a cargo y filtra por nombre, apellido y dependencia', () => {
    const fixture = crear();
    expect(fixture.nativeElement.querySelector('tbody').textContent).toContain('Examen Supletorio');
    const componente = fixture.componentInstance;
    componente.filtro = { nombre: '', apellido: 'Ruiz', dependencia: 'Vice' };
    componente.buscar();
    const req = filtro();
    expect(req.request.params.has('nombre')).toBeFalse();
    expect(req.request.params.get('apellido')).toBe('Ruiz');
    expect(req.request.params.get('dependencia')).toBe('Vice');
    expect(req.request.params.get('pagina')).toBe('0');
  });

  it('crea con los datos personales, de acceso y la dependencia', () => {
    const componente = crear().componentInstance;
    componente.abrirCrear();
    Object.assign(componente.datosUsuario, {
      nombres: 'Luis', apellidos: 'Mora', tipoDocumento: 'Cédula de extranjería', numeroDocumento: '98765',
      telefono: '3209876', correoElectronico: 'luis@unicauca.edu.co', username: 'lmora', password: 'segura123'
    });
    componente.dependencia = ' Secretaría Académica ';
    componente.crear();
    const req = http.expectOne(URL);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({
      nombres: 'Luis', apellidos: 'Mora', tipoDocumento: 'Cédula de extranjería', numeroDocumento: '98765',
      telefono: '3209876', correoElectronico: 'luis@unicauca.edu.co', username: 'lmora', password: 'segura123',
      dependencia: 'Secretaría Académica'
    });
  });

  it('exige la dependencia de maximo 100 al crear y al editar', () => {
    const componente = crear().componentInstance;
    componente.abrirCrear();
    componente.crear();
    expect(componente.errores['dependencia']).toContain('obligatoria');
    componente.abrirEditar(componente.filas[0]);
    componente.dependenciaEditada = 'D'.repeat(101);
    componente.editar();
    expect(componente.errores['dependencia']).toContain('100');
    http.expectNone(URL);
    http.expectNone(`${URL}/f-1`);
  });

  it('edita solo la dependencia con PUT', () => {
    const componente = crear().componentInstance;
    componente.abrirEditar(componente.filas[0]);
    expect(componente.dependenciaEditada).toBe('Vicedecanatura');
    componente.dependenciaEditada = 'Decanatura';
    componente.editar();
    componente.editar();
    const req = http.expectOne(`${URL}/f-1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual({ dependencia: 'Decanatura' });
  });

  it('la carga de Excel usa el endpoint de funcionarios con la parte file', () => {
    const componente = crear().componentInstance;
    componente.cargarExcel(new File([new Uint8Array(5)], 'f.xlsx')).subscribe();
    const req = http.expectOne(`${URL}/cargar/archivo`);
    expect(Array.from((req.request.body as FormData).keys())).toEqual(['file']);
  });
});
