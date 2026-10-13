import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, TestRequest, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { AdminFuncionariosContentComponent } from './admin-funcionarios-content-component';
import { ErrorHandlerService } from '../../../../core/services/error-handler-service';
import { ToastService } from '../../../../core/services/toast-service';
import { environment } from '../../../../../enviroments/environment';

const URL = `${environment.apiUrl}/funcionarios-academicos`;

const FUNCIONARIO = {
  uuidUsuario: 'f-1', nombres: 'Ana', apellidos: 'Ruiz', estado: true, tipoDocumento: 'Cédula de ciudadanía',
  numeroDocumento: '1061', telefono: '3001', correoElectronico: 'ana@u.co', username: 'aruiz', cargo: 'Vicedecanatura',
  tiposSolicitud: [{ uuidTipoSolicitudAcademica: 't-1', nombre: 'Examen Supletorio' }]
};

describe('AdminFuncionariosContentComponent', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [AdminFuncionariosContentComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideNoopAnimations(),
        { provide: ErrorHandlerService, useValue: jasmine.createSpyObj('ErrorHandlerService', ['handleError']) },
        { provide: ToastService, useValue: jasmine.createSpyObj('ToastService', ['showSuccess', 'showError']) }
      ]
    });
    http = TestBed.inject(HttpTestingController);
  });

  function filtro(): TestRequest {
    return http.expectOne(req => req.url === `${URL}/filtro`);
  }

  function crear(): ComponentFixture<AdminFuncionariosContentComponent> {
    const fixture = TestBed.createComponent(AdminFuncionariosContentComponent);
    fixture.detectChanges();
    filtro().flush({ content: [FUNCIONARIO], totalElements: 1 });
    fixture.detectChanges();
    return fixture;
  }

  it('pinta la tabla con el cargo y los procesos a cargo y filtra por nombre, apellido y cargo', () => {
    const fixture = crear();
    const tabla = fixture.nativeElement.querySelector('table') as HTMLElement;
    expect(tabla.textContent).toContain('Cargo');
    expect(tabla.textContent).not.toContain('Dependencia');
    expect(tabla.querySelector('tbody')!.textContent).toContain('Vicedecanatura');
    expect(tabla.querySelector('tbody')!.textContent).toContain('Examen Supletorio');
    const componente = fixture.componentInstance;
    componente.filtro = { nombre: '', apellido: 'Ruiz', cargo: 'Vice' };
    componente.buscar();
    const req = filtro();
    expect(req.request.params.has('nombre')).toBeFalse();
    expect(req.request.params.has('dependencia')).toBeFalse();
    expect(req.request.params.get('apellido')).toBe('Ruiz');
    expect(req.request.params.get('cargo')).toBe('Vice');
    expect(req.request.params.get('pagina')).toBe('0');
  });

  it('crea con los datos personales, de acceso y el cargo, sin enviar dependencia', () => {
    const componente = crear().componentInstance;
    componente.abrirCrear();
    Object.assign(componente.datosUsuario, {
      nombres: 'Luis', apellidos: 'Mora', tipoDocumento: 'Cédula de extranjería', numeroDocumento: '98765',
      telefono: '3209876', correoElectronico: 'luis@unicauca.edu.co', username: 'lmora', password: 'segura123'
    });
    componente.cargo = ' Secretaría Académica ';
    componente.crear();
    const req = http.expectOne(URL);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({
      nombres: 'Luis', apellidos: 'Mora', tipoDocumento: 'Cédula de extranjería', numeroDocumento: '98765',
      telefono: '3209876', correoElectronico: 'luis@unicauca.edu.co', username: 'lmora', password: 'segura123',
      cargo: 'Secretaría Académica'
    });
  });

  it('exige el cargo de maximo 100 al crear y lo limita al editar', () => {
    const componente = crear().componentInstance;
    componente.abrirCrear();
    componente.crear();
    expect(componente.errores['cargo']).toContain('obligatorio');
    componente.abrirEditar(componente.filas[0]);
    componente.cargoEditado = 'D'.repeat(101);
    componente.editar();
    expect(componente.errores['cargo']).toContain('100');
    http.expectNone(URL);
    http.expectNone(`${URL}/f-1`);
  });

  it('el error del cargo desaparece en cuanto se escribe', () => {
    const fixture = crear();
    const componente = fixture.componentInstance;
    componente.abrirCrear();
    componente.crear();
    fixture.detectChanges();
    expect(componente.errores['cargo']).toBeTruthy();
    const entrada = document.querySelector('[data-dialogo-crear] input[id="Cargo"]') as HTMLInputElement;
    entrada.value = 'Decanatura';
    entrada.dispatchEvent(new Event('input', { bubbles: true }));
    fixture.detectChanges();
    expect(componente.errores['cargo']).toBeUndefined();
    expect(componente.errores['nombres']).toBeTruthy();
  });

  it('el dialogo de edicion trae los datos actuales y muestra de solo lectura el usuario y los procesos', () => {
    const fixture = crear();
    const componente = fixture.componentInstance;
    componente.abrirEditar(componente.filas[0]);
    fixture.detectChanges();
    expect(componente.datosEdicion.nombres).toBe('Ana');
    expect(componente.datosEdicion.tipoDocumento).toBe('Cédula de ciudadanía');
    expect(componente.cargoEditado).toBe('Vicedecanatura');
    const dialogo = document.querySelector('[data-dialogo-editar]') as HTMLElement;
    expect(dialogo.querySelector('[data-username]')!.textContent).toContain('aruiz');
    expect(dialogo.querySelector('[data-procesos]')!.textContent).toContain('Examen Supletorio');
    expect(dialogo.querySelector('[data-aviso-procesos]')).not.toBeNull();
    expect(dialogo.querySelector('input[id="Nombre de usuario"]')).toBeNull();
    expect(dialogo.querySelector('input[id="Contraseña"]')).toBeNull();
  });

  it('edita con PUT solo los campos que cambiaron, incluidos datos personales y cargo', () => {
    const componente = crear().componentInstance;
    componente.abrirEditar(componente.filas[0]);
    componente.datosEdicion.telefono = ' 3115550000 ';
    componente.datosEdicion.correoElectronico = 'ana.ruiz@unicauca.edu.co';
    componente.datosEdicion.apellidos = '';
    componente.cargoEditado = 'Decanatura';
    componente.editar();
    componente.editar();
    const req = http.expectOne(`${URL}/f-1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual({ telefono: '3115550000', correoElectronico: 'ana.ruiz@unicauca.edu.co', cargo: 'Decanatura' });
  });

  it('no envia nada si no cambio ningun dato y valida el formato de lo que cambio', () => {
    const componente = crear().componentInstance;
    componente.abrirEditar(componente.filas[0]);
    componente.editar();
    expect(componente.errores['edicion']).toBeTruthy();
    componente.datosEdicion.correoElectronico = 'sin-arroba';
    componente.datosEdicion.numeroDocumento = '12';
    componente.editar();
    expect(componente.errores['correoElectronico']).toContain('formato');
    expect(componente.errores['numeroDocumento']).toContain('entre 5 y 255');
    http.expectNone(`${URL}/f-1`);
  });

  it('muestra en el dialogo el mensaje del codigo 2 cuando el correo ya existe', () => {
    const componente = crear().componentInstance;
    componente.abrirEditar(componente.filas[0]);
    componente.datosEdicion.correoElectronico = 'otro@unicauca.edu.co';
    componente.editar();
    http.expectOne(`${URL}/f-1`).flush(
      { codigoError: 2, mensaje: 'Ya existe un usuario con el correo otro@unicauca.edu.co' },
      { status: 500, statusText: 'Error' }
    );
    expect(componente.erroresServidor).toEqual(['Ya existe un usuario con el correo otro@unicauca.edu.co']);
    expect(componente.dialogoEditar).toBeTrue();
  });

  it('la carga de Excel usa el endpoint de funcionarios con la parte file', () => {
    const componente = crear().componentInstance;
    componente.cargarExcel(new File([new Uint8Array(5)], 'f.xlsx')).subscribe();
    const req = http.expectOne(`${URL}/cargar/archivo`);
    expect(Array.from((req.request.body as FormData).keys())).toEqual(['file']);
  });
});
