import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { authInterceptor } from './auth-interceptor';
import { ToastService } from '../../services/toast-service';
import { environment } from '../../../../enviroments/environment';

const API = environment.apiUrl;

async function esperarAvisos(): Promise<void> {
  for (let i = 0; i < 20; i++) await new Promise(resolver => setTimeout(resolver, 10));
}

describe('authInterceptor', () => {
  let http: HttpClient;
  let controlador: HttpTestingController;
  let toast: jasmine.SpyObj<ToastService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    localStorage.setItem('authToken', 'token-1');
    localStorage.setItem('currentUser', 'u-1');
    toast = jasmine.createSpyObj<ToastService>('ToastService', ['showError', 'showSuccess', 'showInfo']);
    router = jasmine.createSpyObj<Router>('Router', ['navigate']);
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        { provide: ToastService, useValue: toast },
        { provide: Router, useValue: router }
      ]
    });
    http = TestBed.inject(HttpClient);
    controlador = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    localStorage.removeItem('authToken');
    localStorage.removeItem('currentUser');
  });

  function fallar(url: string, status: number, metodo = 'GET'): void {
    const llamada = metodo === 'GET' ? http.get(url) : http.post(url, {});
    llamada.subscribe({ error: () => undefined });
    controlador.expectOne(url).flush({ codigoError: String(status), mensaje: 'x' }, { status, statusText: 'x' });
  }

  it('sigue poniendo la cabecera Authorization con el token guardado', () => {
    http.get(`${API}/estudiantes/paginado`).subscribe();
    expect(controlador.expectOne(`${API}/estudiantes/paginado`).request.headers.get('Authorization')).toBe('Bearer token-1');
  });

  it('un 401 en una peticion normal limpia la sesion, avisa y redirige al login', async () => {
    fallar(`${API}/solicitudes-academicas/estudiante`, 401);
    await esperarAvisos();
    expect(localStorage.getItem('authToken')).toBeNull();
    expect(localStorage.getItem('currentUser')).toBeNull();
    expect(toast.showError).toHaveBeenCalledOnceWith('Sesión vencida', 'Tu sesión venció, inicia sesión de nuevo');
    expect(router.navigate).toHaveBeenCalledOnceWith(['/login']);
  });

  it('un 401 en el login no hace nada especial', async () => {
    fallar(`${API}/sesiones`, 401, 'POST');
    await esperarAvisos();
    expect(localStorage.getItem('authToken')).toBe('token-1');
    expect(toast.showError).not.toHaveBeenCalled();
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('varios 401 simultaneos generan un solo aviso y una sola redireccion', async () => {
    const urls = ['a', 'b', 'c'].map(segmento => `${API}/${segmento}`);
    urls.forEach(url => http.get(url).subscribe({ error: () => undefined }));
    urls.forEach(url => controlador.expectOne(url).flush({}, { status: 401, statusText: 'x' }));
    await esperarAvisos();
    expect(toast.showError).toHaveBeenCalledTimes(1);
    expect(router.navigate).toHaveBeenCalledTimes(1);
  });

  it('un 403 o un 500 no cierran la sesion', async () => {
    fallar(`${API}/asignaturas`, 403);
    fallar(`${API}/cancelaciones-matricula/x`, 500);
    await esperarAvisos();
    expect(localStorage.getItem('authToken')).toBe('token-1');
    expect(toast.showError).not.toHaveBeenCalled();
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('el error sigue llegando a quien hizo la peticion', () => {
    let status = 0;
    http.get(`${API}/x`).subscribe({ error: error => (status = error.status) });
    controlador.expectOne(`${API}/x`).flush({}, { status: 401, statusText: 'x' });
    expect(status).toBe(401);
  });
});
