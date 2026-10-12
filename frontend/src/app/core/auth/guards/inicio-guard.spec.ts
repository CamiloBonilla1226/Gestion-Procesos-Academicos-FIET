import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router, provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { inicioGuard } from './inicio-guard';
import { routes } from '../../../app.routes';
import { rutaDeInicio } from '../../utils/rutas-por-rol';
import { environment } from '../../../../enviroments/environment';

@Component({ template: '<p data-pantalla>pantalla</p>' })
class PantallaComponent {}

describe('inicioGuard en la ruta raiz', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    localStorage.removeItem('authToken');
    localStorage.removeItem('currentUser');
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([
          { path: '', pathMatch: 'full', canActivate: [inicioGuard], children: [] },
          { path: 'login', component: PantallaComponent },
          { path: 'decano', component: PantallaComponent },
          { path: 'estudiante', component: PantallaComponent },
          { path: 'usuario-publico/solicitudes', component: PantallaComponent }
        ])
      ]
    });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    localStorage.removeItem('authToken');
    localStorage.removeItem('currentUser');
  });

  it('sin sesion la raiz lleva a /login', async () => {
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/');
    expect(TestBed.inject(Router).url).toBe('/login');
    http.expectNone(() => true);
  });

  it('con sesion valida la raiz lleva al inicio del rol', async () => {
    localStorage.setItem('authToken', 'token-1');
    localStorage.setItem('currentUser', 'u-1');
    const harness = await RouterTestingHarness.create();
    const navegacion = harness.navigateByUrl('/');
    await new Promise(resolver => setTimeout(resolver));
    http.expectOne(`${environment.apiUrl}/usuarios/u-1`).flush({ uuidUsuario: 'u-1', roles: [{ nombre: 'Decano' }] });
    await navegacion;
    expect(TestBed.inject(Router).url).toBe('/decano');
  });

  it('si la consulta del usuario falla la raiz lleva a /login', async () => {
    localStorage.setItem('authToken', 'token-1');
    localStorage.setItem('currentUser', 'u-1');
    const harness = await RouterTestingHarness.create();
    const navegacion = harness.navigateByUrl('/');
    await new Promise(resolver => setTimeout(resolver));
    http.expectOne(`${environment.apiUrl}/usuarios/u-1`).flush({ mensaje: 'Error' }, { status: 500, statusText: 'Error' });
    await navegacion;
    expect(TestBed.inject(Router).url).toBe('/login');
  });

  it('la ruta publica de solicitudes sigue funcionando si se escribe', async () => {
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/usuario-publico/solicitudes');
    expect(TestBed.inject(Router).url).toBe('/usuario-publico/solicitudes');
  });
});

describe('Ruta raiz de la aplicacion', () => {
  it('la ruta vacia ya no redirige a usuario-publico/solicitudes sino que pasa por inicioGuard', () => {
    const raiz = routes.find(r => r.path === '');
    expect(raiz?.redirectTo).toBeUndefined();
    expect(raiz?.canActivate).toEqual([inicioGuard]);
    expect(routes.some(r => r.path === 'usuario-publico/solicitudes' && !!r.loadComponent)).toBeTrue();
  });

  it('el inicio por rol es el mismo que usa el login', () => {
    expect(rutaDeInicio(['Estudiante'])).toBe('/estudiante');
    expect(rutaDeInicio(['Funcionario Académico', 'Decano'])).toBe('/decano');
    expect(rutaDeInicio(['Docente'])).toBe('/usuario-fiet');
    expect(rutaDeInicio(['Sin vistas'])).toBeNull();
  });
});
