import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { AdminAcademicaContentComponent } from './admin-academica-content-component';
import { routes } from '../../../../app.routes';
import { AuthGuard } from '../../../../core/auth/guards/auth-guard';
import { RoleGuard } from '../../../../core/auth/guards/role-guard';
import { environment } from '../../../../../enviroments/environment';

describe('AdminAcademicaContentComponent', () => {
  it('cambia de seccion con las pestañas y carga solo la seccion visible', () => {
    TestBed.configureTestingModule({
      imports: [AdminAcademicaContentComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideNoopAnimations()]
    });
    const http = TestBed.inject(HttpTestingController);
    const fixture = TestBed.createComponent(AdminAcademicaContentComponent);
    fixture.detectChanges();
    http.expectOne(r => r.url === `${environment.apiUrl}/asignaturas/filtro`);
    expect(fixture.nativeElement.querySelector('app-admin-asignaturas-content-component')).toBeTruthy();
    (fixture.nativeElement.querySelector('[data-seccion="responsables"]') as HTMLAnchorElement).click();
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('app-admin-asignaturas-content-component')).toBeNull();
    expect(fixture.nativeElement.querySelector('app-admin-responsables-content-component')).toBeTruthy();
    http.expectOne(`${environment.apiUrl}/catalogos-academicos/tipos-solicitud`);
  });
});

describe('Rutas de administracion academica', () => {
  function ruta(path: string) {
    return routes.find(r => r.path === path);
  }

  it('el Decano entra por su ruta con los dos guardias y solo su rol', () => {
    const decano = ruta('decano/administracion-academica');
    expect(decano?.canActivate).toEqual([AuthGuard, RoleGuard]);
    expect(decano?.data?.['roles']).toEqual(['Decano']);
    expect(decano?.loadComponent).toBeDefined();
  });

  it('el Secretario General entra por su ruta con los dos guardias y solo su rol', () => {
    const secretario = ruta('sec-general/administracion-academica');
    expect(secretario?.canActivate).toEqual([AuthGuard, RoleGuard]);
    expect(secretario?.data?.['roles']).toEqual(['Secretario General']);
    expect(secretario?.loadComponent).toBeDefined();
  });

  it('ninguna otra ruta de administracion academica queda abierta a otros roles', () => {
    const rutas = routes.filter(r => r.path?.includes('administracion-academica'));
    expect(rutas.length).toBe(2);
    for (const r of rutas) expect(r.data?.['roles']).not.toContain('Funcionario Académico');
  });
});
