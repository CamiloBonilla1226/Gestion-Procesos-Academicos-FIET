import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Subject } from 'rxjs';
import { Router, provideRouter } from '@angular/router';
import { LoginFormComponent } from './login-form-component';
import { AuthService } from '../../core/services/auth-service';
import { ErrorHandlerService } from '../../core/services/error-handler-service';
import { ToastService } from '../../core/services/toast-service';
import { UsuarioDTORespuesta } from '../../core/models/Usuario/DTOResponse/UsuarioDTORespuesta';

describe('LoginFormComponent envio del formulario', () => {
  let auth: jasmine.SpyObj<AuthService>;
  let errores: jasmine.SpyObj<ErrorHandlerService>;
  let respuesta: Subject<UsuarioDTORespuesta>;

  beforeEach(() => {
    respuesta = new Subject<UsuarioDTORespuesta>();
    auth = jasmine.createSpyObj<AuthService>('AuthService', ['login']);
    auth.login.and.returnValue(respuesta.asObservable());
    errores = jasmine.createSpyObj<ErrorHandlerService>('ErrorHandlerService', ['handleError']);
    TestBed.configureTestingModule({
      imports: [LoginFormComponent],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: auth },
        { provide: ErrorHandlerService, useValue: errores },
        { provide: ToastService, useValue: jasmine.createSpyObj<ToastService>('ToastService', ['showError']) }
      ]
    });
  });

  function crear(): ComponentFixture<LoginFormComponent> {
    const fixture = TestBed.createComponent(LoginFormComponent);
    fixture.detectChanges();
    return fixture;
  }

  function escribir(fixture: ComponentFixture<LoginFormComponent>, selector: string, valor: string): void {
    const entrada = fixture.nativeElement.querySelector(selector) as HTMLInputElement;
    entrada.value = valor;
    entrada.dispatchEvent(new Event('input'));
  }

  function formulario(fixture: ComponentFixture<LoginFormComponent>): HTMLFormElement {
    return fixture.nativeElement.querySelector('form[data-form-login]') as HTMLFormElement;
  }

  function boton(fixture: ComponentFixture<LoginFormComponent>): HTMLButtonElement {
    return fixture.nativeElement.querySelector('.form-actions button') as HTMLButtonElement;
  }

  it('los campos estan dentro de un form real y el boton Iniciar es de tipo submit', () => {
    const fixture = crear();
    const form = formulario(fixture);
    expect(form).toBeTruthy();
    expect(form.querySelector('input[type="text"]')).toBeTruthy();
    expect(form.querySelector('input[type="password"]')).toBeTruthy();
    expect(boton(fixture).type).toBe('submit');
    expect(form.querySelectorAll('button[type="submit"]').length).toBe(1);
  });

  it('enviar el form, como hace Enter en usuario o contraseña, inicia sesion con lo escrito', () => {
    const fixture = crear();
    escribir(fixture, 'input[type="text"]', 'rootfiet');
    escribir(fixture, 'input[type="password"]', 'clave-1');
    formulario(fixture).requestSubmit();
    expect(auth.login).toHaveBeenCalledOnceWith({ username: 'rootfiet', password: 'clave-1' });
  });

  it('el clic en Iniciar envia el form una sola vez', () => {
    const fixture = crear();
    boton(fixture).click();
    expect(auth.login).toHaveBeenCalledTimes(1);
  });

  it('mientras la peticion esta en curso ni Enter ni clic envian otra', () => {
    const fixture = crear();
    formulario(fixture).requestSubmit();
    fixture.detectChanges();
    expect(boton(fixture).disabled).toBeTrue();
    formulario(fixture).requestSubmit();
    boton(fixture).click();
    expect(auth.login).toHaveBeenCalledTimes(1);
    respuesta.error({ status: 500 });
    fixture.detectChanges();
    expect(errores.handleError).toHaveBeenCalled();
    expect(boton(fixture).disabled).toBeFalse();
    formulario(fixture).requestSubmit();
    expect(auth.login).toHaveBeenCalledTimes(2);
  });

  it('tras iniciar sesion navega al inicio del rol', () => {
    const fixture = crear();
    const navegar = spyOn(TestBed.inject(Router), 'navigate').and.resolveTo(true);
    formulario(fixture).requestSubmit();
    respuesta.next({ roles: [{ nombre: 'Estudiante' }] } as unknown as UsuarioDTORespuesta);
    respuesta.complete();
    expect(navegar).toHaveBeenCalledOnceWith(['/estudiante']);
  });
});
