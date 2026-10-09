import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, TestRequest, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { AdminAsignaturasContentComponent } from './admin-asignaturas-content-component';
import { ErrorHandlerService } from '../../../../core/services/error-handler-service';
import { ToastService } from '../../../../core/services/toast-service';
import { environment } from '../../../../../enviroments/environment';

const URL = `${environment.apiUrl}/asignaturas`;

function pagina(cantidad: number, total = cantidad) {
  return {
    content: Array.from({ length: cantidad }, (_, i) => ({ uuidAsignatura: `a-${i + 1}`, codigoAsignatura: `IS${i + 1}`, nombreAsignatura: `Asignatura ${i + 1}` })),
    totalElements: total
  };
}

describe('AdminAsignaturasContentComponent', () => {
  let http: HttpTestingController;
  let errores: jasmine.SpyObj<ErrorHandlerService>;
  let toast: jasmine.SpyObj<ToastService>;

  beforeEach(() => {
    errores = jasmine.createSpyObj<ErrorHandlerService>('ErrorHandlerService', ['handleError']);
    toast = jasmine.createSpyObj<ToastService>('ToastService', ['showSuccess', 'showError']);
    TestBed.configureTestingModule({
      imports: [AdminAsignaturasContentComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideNoopAnimations(),
        { provide: ErrorHandlerService, useValue: errores },
        { provide: ToastService, useValue: toast }
      ]
    });
    http = TestBed.inject(HttpTestingController);
  });

  function filtro(): TestRequest {
    return http.expectOne(req => req.url === `${URL}/filtro`);
  }

  function crear(datos = pagina(3, 25)): ComponentFixture<AdminAsignaturasContentComponent> {
    const fixture = TestBed.createComponent(AdminAsignaturasContentComponent);
    fixture.detectChanges();
    filtro().flush(datos);
    fixture.detectChanges();
    return fixture;
  }

  it('pide la primera pagina en base 0 y pinta codigo y nombre con su paginador', () => {
    const fixture = TestBed.createComponent(AdminAsignaturasContentComponent);
    fixture.detectChanges();
    const req = filtro();
    expect(req.request.params.get('pagina')).toBe('0');
    expect(req.request.params.get('tamanio')).toBe('10');
    expect(req.request.params.has('texto')).toBeFalse();
    req.flush(pagina(3, 25));
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelectorAll('tbody tr').length).toBe(3);
    expect(fixture.nativeElement.querySelector('tbody tr').textContent).toContain('IS1');
    expect(fixture.componentInstance.totalPaginas).toBe(3);
  });

  it('busca por texto desde la pagina 1 y el paginador pide la pagina en base 0', () => {
    const fixture = crear();
    const componente = fixture.componentInstance;
    componente.cambiarPagina(3);
    expect(filtro().request.params.get('pagina')).toBe('2');
    componente.texto = ' calculo ';
    componente.buscar();
    const req = filtro();
    expect(req.request.params.get('texto')).toBe('calculo');
    expect(req.request.params.get('pagina')).toBe('0');
    expect(componente.paginaActual).toBe(1);
  });

  it('muestra el estado vacio y el de error con reintento', () => {
    const vacio = crear(pagina(0));
    expect(vacio.nativeElement.querySelector('[data-estado="vacio"]')).toBeTruthy();
    const fixture = TestBed.createComponent(AdminAsignaturasContentComponent);
    fixture.detectChanges();
    filtro().flush({ codigoError: '6', mensaje: 'Error en la paginación...' }, { status: 500, statusText: 'Error' });
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[data-estado="error"]')).toBeTruthy();
    expect(errores.handleError).toHaveBeenCalled();
  });

  it('crea con codigo y nombre recortados y recarga la tabla', () => {
    const componente = crear().componentInstance;
    componente.abrirNueva();
    componente.codigo = ' IS999 ';
    componente.nombre = ' Compiladores ';
    componente.guardar();
    const req = http.expectOne(URL);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ codigoAsignatura: 'IS999', nombreAsignatura: 'Compiladores' });
    req.flush({ uuidAsignatura: 'n', codigoAsignatura: 'IS999', nombreAsignatura: 'Compiladores' });
    expect(componente.dialogoVisible).toBeFalse();
    filtro();
  });

  it('edita con PUT sobre el uuid de la fila', () => {
    const componente = crear().componentInstance;
    componente.abrirEdicion(componente.filas[1]);
    expect(componente.codigo).toBe('IS2');
    componente.nombre = 'Asignatura renombrada';
    componente.guardar();
    const req = http.expectOne(`${URL}/a-2`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual({ codigoAsignatura: 'IS2', nombreAsignatura: 'Asignatura renombrada' });
  });

  it('valida obligatorios y los maximos 45 y 150 sin llamar al backend', () => {
    const componente = crear().componentInstance;
    componente.abrirNueva();
    componente.guardar();
    expect(Object.keys(componente.errores).sort()).toEqual(['codigo', 'nombre']);
    componente.codigo = 'C'.repeat(46);
    componente.nombre = 'N'.repeat(151);
    componente.guardar();
    expect(componente.errores['codigo']).toContain('45');
    expect(componente.errores['nombre']).toContain('150');
    http.expectNone(URL);
  });

  it('muestra en el dialogo el error 2 de codigo repetido y bloquea el doble envio', () => {
    const fixture = crear();
    const componente = fixture.componentInstance;
    componente.abrirNueva();
    componente.codigo = 'IS1';
    componente.nombre = 'Repetida';
    componente.guardar();
    componente.guardar();
    const req = http.expectOne(URL);
    req.flush({ codigoError: '2', mensaje: 'Asignatura con codigo: IS1 existe en el sistema...' }, { status: 500, statusText: 'Error' });
    fixture.detectChanges();
    expect(componente.erroresServidor).toEqual(['Asignatura con codigo: IS1 existe en el sistema...']);
    expect(document.querySelector('[data-error="servidor"]')?.textContent).toContain('existe en el sistema');
    expect(componente.dialogoVisible).toBeTrue();
  });
});
