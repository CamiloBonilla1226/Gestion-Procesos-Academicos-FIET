import { of } from 'rxjs';
import { Router } from '@angular/router';
import { LoginFormComponent } from './login-form-component';
import { AuthService } from '../../core/services/auth-service';
import { ErrorHandlerService } from '../../core/services/error-handler-service';
import { ToastService } from '../../core/services/toast-service';

describe('LoginFormComponent redireccion por rol', () => {
  let router: jasmine.SpyObj<Router>;
  let toast: jasmine.SpyObj<ToastService>;

  function iniciarSesionCon(roles: string[]): void {
    const auth = jasmine.createSpyObj<AuthService>('AuthService', ['login']);
    auth.login.and.returnValue(of({ roles: roles.map(nombre => ({ nombre })) } as any));
    const errores = jasmine.createSpyObj<ErrorHandlerService>('ErrorHandlerService', ['handleError']);
    router = jasmine.createSpyObj<Router>('Router', ['navigate']);
    toast = jasmine.createSpyObj<ToastService>('ToastService', ['showError']);
    new LoginFormComponent(auth, errores, router, toast).onSubmit();
  }

  const casos: [string[], string][] = [
    [['Secretario General'], '/sec-general'],
    [['Funcionario'], '/funcionario'],
    [['Secretaria Decanatura FIET'], '/sec-fiet'],
    [['Decano'], '/decano'],
    [['Funcionario Académico'], '/funcionario-academico'],
    [['Estudiante'], '/estudiante'],
    [['Docente'], '/usuario-fiet'],
    [['Jefe de Departamento'], '/usuario-fiet'],
    [['Funcionario Académico', 'Decano'], '/decano'],
    [['Decano', 'Funcionario Académico'], '/decano'],
    [['Estudiante', 'Funcionario Académico'], '/funcionario-academico'],
    [['Docente', 'Estudiante'], '/estudiante'],
    [['Secretario General', 'Decano'], '/sec-general'],
    [['Funcionario', 'Decano'], '/funcionario'],
    [['Secretaria Decanatura FIET', 'Decano'], '/sec-fiet']
  ];

  for (const [roles, destino] of casos) {
    it(`${roles.join(' + ')} va a ${destino}`, () => {
      iniciarSesionCon(roles);
      expect(router.navigate).toHaveBeenCalledOnceWith([destino]);
      expect(toast.showError).not.toHaveBeenCalled();
    });
  }

  it('Funcionario Academico sin tilde no coincide con ningun rol', () => {
    iniciarSesionCon(['Funcionario Academico']);
    expect(router.navigate).not.toHaveBeenCalled();
    expect(toast.showError).toHaveBeenCalled();
  });
});
