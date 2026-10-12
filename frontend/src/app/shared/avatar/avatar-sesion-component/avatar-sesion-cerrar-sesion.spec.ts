import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { Router, provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { AvatarSesionComponent } from './avatar-sesion-component';
import { AuthGuard } from '../../../core/auth/guards/auth-guard';
import { AuthService } from '../../../core/services/auth-service';

@Component({ template: '<p data-login>login</p>' })
class LoginPruebaComponent {}

@Component({
  imports: [AvatarSesionComponent],
  template: '<app-avatar-sesion-component nombre="Ana" avatarLabel="A" currentRole="Decano" [roles]="[\'Decano\']"></app-avatar-sesion-component>'
})
class PantallaProtegidaComponent {}

describe('AvatarSesionComponent cerrar sesion', () => {
  beforeEach(() => {
    localStorage.setItem('authToken', 'token-1');
    localStorage.setItem('currentUser', 'u-1');
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([
          { path: 'login', component: LoginPruebaComponent },
          { path: 'decano', component: PantallaProtegidaComponent, canActivate: [AuthGuard] },
          { path: 'estudiante', component: PantallaProtegidaComponent, canActivate: [AuthGuard] }
        ])
      ]
    });
  });

  afterEach(() => {
    localStorage.removeItem('authToken');
    localStorage.removeItem('currentUser');
  });

  async function cerrarSesionDesde(ruta: string): Promise<{ harness: RouterTestingHarness; evento: MouseEvent }> {
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl(ruta);
    const enlace = Array.from(harness.routeNativeElement!.querySelectorAll('a.dropdown-item'))
      .find(a => a.textContent?.trim() === 'Cerrar sesión') as HTMLAnchorElement;
    const evento = new MouseEvent('click', { bubbles: true, cancelable: true });
    enlace.dispatchEvent(evento);
    await harness.fixture.whenStable();
    return { harness, evento };
  }

  it('borra el token y el usuario guardados y navega a /login sin seguir el href', async () => {
    const auth = TestBed.inject(AuthService);
    const { harness, evento } = await cerrarSesionDesde('/decano');
    expect(evento.defaultPrevented).toBeTrue();
    expect(localStorage.getItem('authToken')).toBeNull();
    expect(localStorage.getItem('currentUser')).toBeNull();
    expect(auth.getUsuarioActual()).toBeNull();
    expect(TestBed.inject(Router).url).toBe('/login');
    expect(harness.routeNativeElement?.querySelector('[data-login]')).toBeTruthy();
  });

  it('despues de cerrar sesion volver a una pantalla protegida manda a /login', async () => {
    await cerrarSesionDesde('/estudiante');
    const router = TestBed.inject(Router);
    await router.navigateByUrl('/estudiante');
    expect(router.url).toBe('/login');
  });

  it('el menu se abre alineado al borde derecho para no salirse de la pantalla', async () => {
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/decano');
    const menu = harness.routeNativeElement!.querySelector('.dropdown-menu') as HTMLElement;
    expect(menu.classList).toContain('dropdown-menu-end');
  });
});
