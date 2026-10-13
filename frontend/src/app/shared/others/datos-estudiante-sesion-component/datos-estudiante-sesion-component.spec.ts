import { TestBed } from '@angular/core/testing';
import { BehaviorSubject } from 'rxjs';
import { DatosEstudianteSesionComponent, NOTA_DATOS_DARCA } from './datos-estudiante-sesion-component';
import { AuthService } from '../../../core/services/auth-service';

describe('DatosEstudianteSesionComponent', () => {
  function crear(usuario: unknown) {
    TestBed.configureTestingModule({
      imports: [DatosEstudianteSesionComponent],
      providers: [{ provide: AuthService, useValue: { usuario$: new BehaviorSubject(usuario) } }]
    });
    const fixture = TestBed.createComponent(DatosEstudianteSesionComponent);
    fixture.detectChanges();
    return fixture;
  }

  it('muestra los datos de la sesion de solo lectura, la facultad FIET y la nota de DARCA', () => {
    const fixture = crear({
      nombres: 'Laura', apellidos: 'Muñoz', tipoDocumento: 'Cédula de ciudadanía', numeroDocumento: '1061789234',
      correoElectronico: 'lmunoz@unicauca.edu.co'
    });
    const etiquetas = Array.from(fixture.nativeElement.querySelectorAll('.dato-etiqueta')).map((e: any) => e.textContent.trim());
    const valores = Array.from(fixture.nativeElement.querySelectorAll('input')) as HTMLInputElement[];
    expect(etiquetas).toEqual(['Nombres', 'Apellidos', 'Tipo de documento', 'Número de documento', 'Correo electrónico', 'Facultad']);
    expect(valores.map(v => v.value)).toEqual(['Laura', 'Muñoz', 'Cédula de ciudadanía', '1061789234', 'lmunoz@unicauca.edu.co', 'FIET']);
    expect(valores.every(v => v.readOnly)).toBeTrue();
    expect(fixture.nativeElement.querySelector('[data-nota-darca]').textContent.trim()).toBe(NOTA_DATOS_DARCA);
  });

  it('sin usuario solo muestra la facultad', () => {
    const fixture = crear(null);
    const etiquetas = Array.from(fixture.nativeElement.querySelectorAll('.dato-etiqueta')).map((e: any) => e.textContent.trim());
    expect(etiquetas).toEqual(['Facultad']);
  });
});
