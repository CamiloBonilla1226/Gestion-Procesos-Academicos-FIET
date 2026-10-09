import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, TestRequest, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { AdminEstudiantesContentComponent } from './admin-estudiantes-content-component';
import { ToastService } from '../../../../core/services/toast-service';
import { EstudianteDTORespuesta } from '../../../../core/models/Estudiante/DTOResponse/EstudianteDTORespuesta';
import { environment } from '../../../../../enviroments/environment';

const URL = `${environment.apiUrl}/estudiantes`;
const CLAVE = 'clave-secreta-123';

function estudiante(uuid: string, estados: string[] = ['activa', 'cancelada']): EstudianteDTORespuesta {
  return {
    uuidUsuario: uuid, nombres: 'María', apellidos: 'Pérez', estado: true, tipoDocumento: 'Cédula de ciudadanía',
    numeroDocumento: '1061', telefono: '3001', correoElectronico: 'm@u.co', username: 'mperez', codigoEstudiantil: '1046',
    programaAcademico: 'Ingeniería de Sistemas', semestre: '5', facultad: 'FIET',
    asignaturasMatriculadas: estados.map((estado, i) => ({
      uuidAsignaturaMatriculada: `m-${i + 1}`, uuidAsignatura: `a-${i + 1}`, codigoAsignatura: `IS${i + 1}`,
      nombreAsignatura: `Asignatura ${i + 1}`, grupo: 'A', estado
    }))
  };
}

describe('AdminEstudiantesContentComponent', () => {
  let http: HttpTestingController;
  let toast: jasmine.SpyObj<ToastService>;
  let consola: jasmine.Spy;

  beforeEach(() => {
    toast = jasmine.createSpyObj<ToastService>('ToastService', ['showSuccess', 'showError']);
    TestBed.configureTestingModule({
      imports: [AdminEstudiantesContentComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideNoopAnimations(), { provide: ToastService, useValue: toast }]
    });
    http = TestBed.inject(HttpTestingController);
    consola = spyOn(console, 'error');
  });

  function filtro(): TestRequest {
    return http.expectOne(req => req.url === `${URL}/filtro`);
  }

  function crear(): ComponentFixture<AdminEstudiantesContentComponent> {
    const fixture = TestBed.createComponent(AdminEstudiantesContentComponent);
    fixture.detectChanges();
    filtro().flush({ content: [estudiante('e-1'), estudiante('e-2', ['aprobada'])], totalElements: 12 });
    fixture.detectChanges();
    return fixture;
  }

  function llenar(componente: AdminEstudiantesContentComponent): void {
    componente.abrirCrear();
    Object.assign(componente.datosUsuario, {
      nombres: ' Ana ', apellidos: 'Ruiz', tipoDocumento: 'Tarjeta de identidad', numeroDocumento: '1002003',
      telefono: '3101234', correoElectronico: 'ana@unicauca.edu.co', username: 'aruiz', password: CLAVE
    });
    Object.assign(componente.datosAcademicos, { codigoEstudiantil: '104620', programaAcademico: 'Ingeniería Electrónica', semestre: '2', facultad: 'FIET' });
    Object.assign(componente.asignaturasNuevas[0], { codigoAsignatura: 'IS101', nombreAsignatura: 'Cálculo I', grupo: 'A' });
    componente.agregarFilaAsignatura();
    Object.assign(componente.asignaturasNuevas[1], { codigoAsignatura: 'IS102', nombreAsignatura: 'Física I', grupo: 'B' });
  }

  it('pinta la tabla y filtra por nombre, apellido y codigo desde la pagina 0', () => {
    const fixture = crear();
    const componente = fixture.componentInstance;
    expect(fixture.nativeElement.querySelectorAll('tbody tr').length).toBe(2);
    expect(fixture.nativeElement.querySelector('thead').textContent).toContain('Código estudiantil');
    expect(componente.totalPaginas).toBe(2);
    componente.cambiarPagina(2);
    expect(filtro().request.params.get('pagina')).toBe('1');
    componente.filtro = { nombre: 'Mar', apellido: '', codigo: '104' };
    componente.buscar();
    const req = filtro();
    expect(req.request.params.get('nombre')).toBe('Mar');
    expect(req.request.params.has('apellido')).toBeFalse();
    expect(req.request.params.get('codigo')).toBe('104');
    expect(req.request.params.get('pagina')).toBe('0');
    expect(req.request.params.get('tamanio')).toBe('10');
  });

  it('crea el estudiante con el cuerpo exacto y sus asignaturas', () => {
    const componente = crear().componentInstance;
    llenar(componente);
    componente.crear();
    const req = http.expectOne(URL);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({
      nombres: 'Ana', apellidos: 'Ruiz', tipoDocumento: 'Tarjeta de identidad', numeroDocumento: '1002003',
      telefono: '3101234', correoElectronico: 'ana@unicauca.edu.co', username: 'aruiz', password: CLAVE,
      codigoEstudiantil: '104620', programaAcademico: 'Ingeniería Electrónica', semestre: '2', facultad: 'FIET',
      asignaturas: [
        { codigoAsignatura: 'IS101', nombreAsignatura: 'Cálculo I', grupo: 'A' },
        { codigoAsignatura: 'IS102', nombreAsignatura: 'Física I', grupo: 'B' }
      ]
    });
    req.flush(estudiante('nuevo'));
    expect(componente.dialogoCrear).toBeFalse();
    filtro();
  });

  it('valida longitudes, correo, contraseña y al menos una asignatura', () => {
    const componente = crear().componentInstance;
    llenar(componente);
    componente.datosUsuario.password = '1234';
    componente.datosUsuario.correoElectronico = 'sin-arroba';
    componente.datosAcademicos.semestre = '12345678901';
    componente.quitarFilaAsignatura(0);
    componente.quitarFilaAsignatura(0);
    componente.crear();
    expect(componente.errores['password']).toContain('entre 5 y 255');
    expect(componente.errores['correoElectronico']).toContain('formato');
    expect(componente.errores['semestre']).toContain('10');
    expect(componente.errores['asignaturas']).toBeTruthy();
    http.expectNone(URL);
  });

  it('la contraseña no aparece en la consola ni en los mensajes cuando el alta falla', () => {
    const componente = crear().componentInstance;
    llenar(componente);
    componente.crear();
    http.expectOne(URL).flush({ codigoError: '2', mensaje: 'Usuario con username: aruiz existe...' }, { status: 500, statusText: 'Error' });
    expect(componente.erroresServidor).toEqual(['Usuario con username: aruiz existe...']);
    expect(JSON.stringify(consola.calls.allArgs())).not.toContain(CLAVE);
    expect(JSON.stringify(toast.showError.calls.allArgs())).not.toContain(CLAVE);
    expect(JSON.stringify(componente.erroresServidor)).not.toContain(CLAVE);
  });

  it('edita solo los datos que se escriben', () => {
    const componente = crear().componentInstance;
    componente.abrirEditar(componente.filas[0]);
    componente.editar();
    expect(componente.errores['edicion']).toBeTruthy();
    componente.edicion.semestre = ' 6 ';
    componente.edicion.facultad = 'FIET';
    componente.editar();
    const req = http.expectOne(`${URL}/e-1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual({ semestre: '6', facultad: 'FIET' });
  });

  it('agrega una asignatura matriculada con codigo, nombre y grupo', () => {
    const componente = crear().componentInstance;
    componente.abrirDetalle(componente.filas[0]);
    Object.assign(componente.nuevaMatricula, { codigoAsignatura: 'IS300', nombreAsignatura: 'Redes', grupo: 'C' });
    componente.agregarMatricula();
    const req = http.expectOne(`${URL}/e-1/asignaturas`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ codigoAsignatura: 'IS300', nombreAsignatura: 'Redes', grupo: 'C' });
    req.flush({ uuidAsignaturaMatriculada: 'm-9', uuidAsignatura: 'a-9', codigoAsignatura: 'IS300', nombreAsignatura: 'Redes', grupo: 'C', estado: 'activa' });
    expect(componente.seleccionado?.asignaturasMatriculadas.map(a => a.uuidAsignaturaMatriculada)).toEqual(['m-1', 'm-2', 'm-9']);
  });

  it('cambia el estado solo de las activas con PATCH y el cuerpo estado', () => {
    const fixture = crear();
    const componente = fixture.componentInstance;
    componente.abrirDetalle(componente.filas[0]);
    fixture.detectChanges();
    expect(document.querySelector('[data-matricula="m-1"] [data-boton-estado]')).toBeTruthy();
    expect(document.querySelector('[data-matricula="m-2"] [data-boton-estado]')).toBeNull();
    expect(document.querySelector('[data-matricula="m-2"] [data-sin-cambio]')).toBeTruthy();
    componente.estadosNuevos['m-2'] = 'aprobada';
    componente.cambiarEstado(componente.seleccionado!.asignaturasMatriculadas[1]);
    http.expectNone(`${URL}/e-1/asignaturas/m-2/estado`);
    componente.estadosNuevos['m-1'] = 'cancelada';
    componente.cambiarEstado(componente.seleccionado!.asignaturasMatriculadas[0]);
    const req = http.expectOne(`${URL}/e-1/asignaturas/m-1/estado`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ estado: 'cancelada' });
    req.flush({ ...componente.seleccionado!.asignaturasMatriculadas[0], estado: 'cancelada' });
    expect(componente.seleccionado!.asignaturasMatriculadas[0].estado).toBe('cancelada');
  });

  it('muestra el error del backend al cambiar el estado', () => {
    const fixture = crear();
    const componente = fixture.componentInstance;
    componente.abrirDetalle(componente.filas[0]);
    componente.estadosNuevos['m-1'] = 'perdida';
    componente.cambiarEstado(componente.seleccionado!.asignaturasMatriculadas[0]);
    http.expectOne(`${URL}/e-1/asignaturas/m-1/estado`).flush(
      { codigoError: '6', mensaje: 'No se puede cambiar la asignatura matriculada de cancelada a perdida...' },
      { status: 500, statusText: 'Error' }
    );
    fixture.detectChanges();
    expect(document.querySelector('[data-dialogo-detalle] [data-error="servidor"]')?.textContent).toContain('No se puede cambiar');
  });

  it('la carga de Excel usa el endpoint de estudiantes y recarga la tabla', () => {
    const componente = crear().componentInstance;
    componente.cargarExcel(new File([new Uint8Array(5)], 'e.xlsx')).subscribe();
    expect(http.expectOne(`${URL}/cargar/archivo`).request.body instanceof FormData).toBeTrue();
    componente.alCargarExcel();
    expect(filtro().request.params.get('pagina')).toBe('0');
  });
});
