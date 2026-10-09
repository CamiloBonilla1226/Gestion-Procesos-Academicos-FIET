import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { AdminResponsablesContentComponent } from './admin-responsables-content-component';
import { ErrorHandlerService } from '../../../../core/services/error-handler-service';
import { ToastService } from '../../../../core/services/toast-service';
import { environment } from '../../../../../enviroments/environment';

const API = environment.apiUrl;
const URL_TIPOS = `${API}/catalogos-academicos/tipos-solicitud`;

const TIPOS = [
  { uuidTipoSolicitudAcademica: 't-1', nombre: 'Cancelación de Matrícula', descripcion: '', uuidFuncionarioAcademico: 'f-1', nombreFuncionarioAcademico: 'Ana Ruiz' },
  { uuidTipoSolicitudAcademica: 't-2', nombre: 'Examen Supletorio', descripcion: '', uuidFuncionarioAcademico: null, nombreFuncionarioAcademico: null }
];

const FUNCIONARIOS = {
  content: [
    { uuidUsuario: 'f-1', nombres: 'Ana', apellidos: 'Ruiz', dependencia: 'Vicedecanatura' },
    { uuidUsuario: 'f-2', nombres: 'Luis', apellidos: 'Mora', dependencia: 'Secretaría' }
  ],
  totalElements: 2
};

describe('AdminResponsablesContentComponent', () => {
  let http: HttpTestingController;
  let errores: jasmine.SpyObj<ErrorHandlerService>;

  beforeEach(() => {
    errores = jasmine.createSpyObj<ErrorHandlerService>('ErrorHandlerService', ['handleError']);
    TestBed.configureTestingModule({
      imports: [AdminResponsablesContentComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideNoopAnimations(),
        { provide: ErrorHandlerService, useValue: errores },
        { provide: ToastService, useValue: jasmine.createSpyObj('ToastService', ['showSuccess', 'showError']) }
      ]
    });
    http = TestBed.inject(HttpTestingController);
  });

  function crear(): ComponentFixture<AdminResponsablesContentComponent> {
    const fixture = TestBed.createComponent(AdminResponsablesContentComponent);
    fixture.detectChanges();
    http.expectOne(URL_TIPOS).flush(TIPOS);
    fixture.detectChanges();
    return fixture;
  }

  function abrir(fixture: ComponentFixture<AdminResponsablesContentComponent>, indice: number): void {
    fixture.componentInstance.abrir(fixture.componentInstance.filas[indice]);
    const req = http.expectOne(r => r.url === `${API}/funcionarios-academicos/paginado`);
    expect(req.request.params.get('pagina')).toBe('0');
    expect(req.request.params.get('tamanio')).toBe('100');
    req.flush(FUNCIONARIOS);
    fixture.detectChanges();
  }

  it('lista los tipos con su responsable actual', () => {
    const filas = Array.from(crear().nativeElement.querySelectorAll('tbody tr')).map((tr: any) => tr.textContent);
    expect(filas[0]).toContain('Ana Ruiz');
    expect(filas[1]).toContain('Sin responsable');
  });

  it('el dialogo ofrece los funcionarios y avisa que cambia la bandeja', () => {
    const fixture = crear();
    abrir(fixture, 0);
    const componente = fixture.componentInstance;
    expect(componente.dialogoVisible).toBeTrue();
    expect(componente.opcionesFuncionario).toEqual([
      { label: 'Ruiz Ana - Vicedecanatura', value: 'f-1' },
      { label: 'Mora Luis - Secretaría', value: 'f-2' }
    ]);
    expect(componente.funcionarioElegido).toBe('f-1');
    expect(document.querySelector('[data-aviso-bandeja]')?.textContent).toContain('bandeja del nuevo funcionario');
  });

  it('envia funcionarioUuid con PUT y recarga la lista', () => {
    const fixture = crear();
    abrir(fixture, 0);
    const componente = fixture.componentInstance;
    componente.guardar();
    expect(componente.error).toContain('ya es el responsable');
    componente.funcionarioElegido = 'f-2';
    componente.guardar();
    componente.guardar();
    const req = http.expectOne(`${URL_TIPOS}/t-1/funcionario`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual({ funcionarioUuid: 'f-2' });
    req.flush({ ...TIPOS[0], uuidFuncionarioAcademico: 'f-2', nombreFuncionarioAcademico: 'Luis Mora' });
    expect(componente.dialogoVisible).toBeFalse();
    http.expectOne(URL_TIPOS);
  });

  it('muestra el error del backend al cambiar el responsable', () => {
    const fixture = crear();
    abrir(fixture, 1);
    const componente = fixture.componentInstance;
    componente.funcionarioElegido = 'f-2';
    componente.guardar();
    http.expectOne(`${URL_TIPOS}/t-2/funcionario`).flush(
      { codigoError: '3', mensaje: 'Funcionario Académico con id f-2 no fue encontrado...' },
      { status: 500, statusText: 'Error' }
    );
    fixture.detectChanges();
    expect(document.querySelector('[data-dialogo-responsable] [data-error="servidor"]')?.textContent).toContain('no fue encontrado');
    expect(errores.handleError).toHaveBeenCalled();
  });

  it('sin funcionarios registrados avisa y no abre el dialogo', () => {
    const fixture = crear();
    fixture.componentInstance.abrir(fixture.componentInstance.filas[1]);
    http.expectOne(r => r.url === `${API}/funcionarios-academicos/paginado`).flush({ content: [], totalElements: 0 });
    expect(fixture.componentInstance.dialogoVisible).toBeFalse();
    expect(errores.handleError).toHaveBeenCalled();
  });
});
