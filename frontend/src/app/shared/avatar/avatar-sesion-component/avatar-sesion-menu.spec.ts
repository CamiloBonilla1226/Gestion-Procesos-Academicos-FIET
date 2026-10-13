import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { AvatarSesionComponent } from './avatar-sesion-component';
import { AuthService } from '../../../core/services/auth-service';

describe('AvatarSesionComponent menu', () => {
  let router: jasmine.SpyObj<Router>;
  let auth: jasmine.SpyObj<AuthService>;

  function crear(rol: string) {
    router = jasmine.createSpyObj<Router>('Router', ['navigate']);
    auth = jasmine.createSpyObj<AuthService>('AuthService', ['logout']);
    TestBed.configureTestingModule({
      imports: [AvatarSesionComponent],
      providers: [
        { provide: Router, useValue: router },
        { provide: AuthService, useValue: auth }
      ]
    });
    const fixture = TestBed.createComponent(AvatarSesionComponent);
    fixture.componentRef.setInput('nombre', 'Andrés');
    fixture.componentRef.setInput('avatarLabel', 'AM');
    fixture.componentRef.setInput('currentRole', rol);
    fixture.detectChanges();
    return fixture;
  }

  it('el chip muestra iniciales, nombre y rol', () => {
    const fixture = crear('Decano');
    const chip = fixture.nativeElement.querySelector('.user-chip-btn');
    expect(chip.querySelector('.avatar-custom').textContent.trim()).toBe('AM');
    expect(chip.querySelector('.user-name').textContent.trim()).toBe('Andrés');
    expect(chip.querySelector('.user-role').textContent.trim()).toBe('Decano');
  });

  it('el menu tiene Mi usuario y Cerrar sesion', () => {
    const fixture = crear('Decano');
    const items = Array.from(fixture.nativeElement.querySelectorAll('.dropdown-item')).map((a: any) => a.textContent.trim());
    expect(items).toEqual(['Mi usuario', 'Cerrar sesión']);
  });

  it('Mi usuario lleva al inicio del rol actual', () => {
    const fixture = crear('Funcionario Académico');
    (fixture.nativeElement.querySelectorAll('.dropdown-item')[0] as HTMLAnchorElement).click();
    expect(router.navigate).toHaveBeenCalledWith(['/funcionario-academico']);
  });

  it('Cerrar sesion cierra y vuelve al login', () => {
    const fixture = crear('Estudiante');
    (fixture.nativeElement.querySelector('.item-salir') as HTMLAnchorElement).click();
    expect(auth.logout).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });
});
