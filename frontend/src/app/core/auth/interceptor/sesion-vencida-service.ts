import { Injectable, Injector } from '@angular/core';
import { HttpErrorResponse, HttpRequest } from '@angular/common/http';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth-service';

const PREFIJO_TOKEN = 'Bearer ';

@Injectable({
  providedIn: 'root'
})
export class SesionVencidaService {
  constructor(
    private authService: AuthService,
    private router: Router,
    private injector: Injector
  ) {}

  atender(peticion: HttpRequest<unknown>, error: unknown): void {
    if (!(error instanceof HttpErrorResponse) || error.status !== 401) return;
    if (this.esLogin(peticion) || typeof window === 'undefined') return;
    const tokenActual = localStorage.getItem('authToken');
    const tokenEnviado = (peticion.headers.get('Authorization') ?? '').replace(PREFIJO_TOKEN, '');
    if (!tokenActual || tokenActual !== tokenEnviado) return;
    this.authService.logout();
    this.avisar();
    this.router.navigate(['/login']);
  }

  private avisar(): void {
    import('../../services/toast-service')
      .then(modulo => this.injector.get(modulo.ToastService).showError('Sesión vencida', 'Tu sesión venció, inicia sesión de nuevo'))
      .catch(() => undefined);
  }

  private esLogin(peticion: HttpRequest<unknown>): boolean {
    return peticion.method === 'POST' && /\/sesiones\/?$/.test(peticion.url.split('?')[0]);
  }
}
