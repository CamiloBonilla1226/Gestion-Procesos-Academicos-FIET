import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { MessageService } from 'primeng/api';
import { of, throwError } from 'rxjs';
import { InfoUsuarioContentComponent } from './info-usuario-content-component';
import { AuthService } from '../../../../core/services/auth-service';
import { FuncionarioAcademicoService } from '../../../../core/services/funcionario-academico-service';

describe('InfoUsuarioContentComponent Mi usuario', () => {
  let funcionarios: jasmine.SpyObj<FuncionarioAcademicoService>;

  function usuario(rol: string, telefono = '3001234567') {
    return {
      uuidUsuario: 'u-1', nombres: 'Laura', apellidos: 'Muñoz', estado: true, tipoDocumento: 'Cédula de ciudadanía',
      numeroDocumento: '1061789234', telefono, correoElectronico: 'lmunoz@unicauca.edu.co', username: 'lmunoz',
      roles: [{ uuidRol: 'r', nombre: rol, descripcion: '', estado: true }]
    };
  }

  function crear(user: unknown) {
    funcionarios = jasmine.createSpyObj<FuncionarioAcademicoService>('FuncionarioAcademicoService', ['getFuncionarioAcademico']);
    TestBed.configureTestingModule({
      imports: [InfoUsuarioContentComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideNoopAnimations(),
        MessageService,
        { provide: AuthService, useValue: { usuario$: of(user) } },
        { provide: FuncionarioAcademicoService, useValue: funcionarios }
      ]
    });
    return TestBed.createComponent(InfoUsuarioContentComponent);
  }

  function campos(fixture: ReturnType<typeof crear>): string[] {
    return Array.from(fixture.nativeElement.querySelectorAll('.user-fields dt')).map((dt: any) => dt.textContent.trim());
  }

  it('saluda, muestra las iniciales y los campos que entrega la API', () => {
    const fixture = crear(usuario('Decano'));
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.user-welcome').textContent.trim()).toBe('Bienvenid@, Laura!');
    expect(fixture.nativeElement.querySelector('.user-avatar-big').textContent.trim()).toBe('LM');
    expect(campos(fixture)).toEqual(['Nombres', 'Apellidos', 'Tipo de documento', 'N° identificación', 'Teléfono', 'Usuario', 'Correo']);
    expect(fixture.nativeElement.querySelector('.user-roles').textContent).toContain('Decano');
    expect(fixture.nativeElement.textContent).not.toContain('Te uniste el');
    expect(funcionarios.getFuncionarioAcademico).not.toHaveBeenCalled();
  });

  it('omite los campos vacios', () => {
    const fixture = crear(usuario('Estudiante', ''));
    fixture.detectChanges();
    expect(campos(fixture)).not.toContain('Teléfono');
    expect(fixture.nativeElement.querySelector('[data-seccion="procesos-asignados"]')).toBeNull();
  });

  it('el Funcionario Academico ve su cargo y sus procesos asignados', () => {
    const fixture = crear(usuario('Funcionario Académico'));
    funcionarios.getFuncionarioAcademico.and.returnValue(of({
      ...usuario('Funcionario Académico'), cargo: 'Secretaría académica',
      tiposSolicitud: [
        { uuidTipoSolicitudAcademica: 't1', nombre: 'Cancelación de Matrícula' },
        { uuidTipoSolicitudAcademica: 't2', nombre: 'Examen Supletorio' }
      ]
    }));
    fixture.detectChanges();
    expect(funcionarios.getFuncionarioAcademico).toHaveBeenCalledOnceWith('u-1');
    expect(campos(fixture)).toContain('Cargo');
    expect(fixture.nativeElement.textContent).toContain('Secretaría académica');
    const procesos = Array.from(fixture.nativeElement.querySelectorAll('[data-seccion="procesos-asignados"] li'))
      .map((li: any) => li.textContent.trim());
    expect(procesos).toEqual(['Cancelación de Matrícula', 'Examen Supletorio']);
  });

  it('si falla la consulta del funcionario no muestra cargo ni procesos', () => {
    const fixture = crear(usuario('Funcionario Académico'));
    funcionarios.getFuncionarioAcademico.and.returnValue(throwError(() => new Error('x')));
    fixture.detectChanges();
    expect(campos(fixture)).not.toContain('Cargo');
    expect(fixture.nativeElement.querySelector('[data-seccion="procesos-asignados"]')).toBeNull();
  });
});
