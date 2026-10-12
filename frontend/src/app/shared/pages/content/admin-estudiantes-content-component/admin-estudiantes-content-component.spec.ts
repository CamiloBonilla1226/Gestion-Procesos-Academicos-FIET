import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, TestRequest, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { AdminEstudiantesContentComponent } from './admin-estudiantes-content-component';
import { ToastService } from '../../../../core/services/toast-service';
import { EstudianteDTORespuesta } from '../../../../core/models/Estudiante/DTOResponse/EstudianteDTORespuesta';
import { ENCABEZADOS_EXCEL_ESTUDIANTES } from '../../../../core/constantes/procesos-academicos';
import { environment } from '../../../../../enviroments/environment';

const URL = `${environment.apiUrl}/estudiantes`;
const URL_ASIGNATURAS = `${environment.apiUrl}/asignaturas`;
const URL_PROGRAMAS = `${environment.apiUrl}/catalogos-academicos/programas`;
const CLAVE = 'clave-secreta-123';
const ISI = 'uuid-isi';
const TTE = 'uuid-tte';
const PROGRAMAS = [
  { uuidProgramaAcademico: 'uuid-iet', nombre: 'Ingeniería Electrónica y Telecomunicaciones', sigla: 'IET' },
  { uuidProgramaAcademico: ISI, nombre: 'Ingeniería de Sistemas', sigla: 'ISI' },
  { uuidProgramaAcademico: 'uuid-iai', nombre: 'Ingeniería en Automática Industrial', sigla: 'IAI' },
  { uuidProgramaAcademico: TTE, nombre: 'Tecnología en Telemática', sigla: 'TTE' },
  { uuidProgramaAcademico: 'uuid-iac', nombre: 'Ingeniería Aeroespacial', sigla: 'IAC' }
];

function asignaturasDe(sigla: string, uuidPrograma: string, nombrePrograma: string, cantidad = 3) {
  return Array.from({ length: cantidad }, (_, i) => ({
    uuidAsignatura: `${sigla}-${i + 1}`,
    codigoAsignatura: `${sigla}-00${i + 1}`,
    nombreAsignatura: `Materia ${i + 1}`,
    uuidProgramaAcademico: uuidPrograma,
    programaAcademico: nombrePrograma
  }));
}

function estudiante(uuid: string, estados: string[] = ['activa', 'cancelada']): EstudianteDTORespuesta {
  return {
    uuidUsuario: uuid, nombres: 'María', apellidos: 'Pérez', estado: true, tipoDocumento: 'Cédula de ciudadanía',
    numeroDocumento: '1061', telefono: '3001', correoElectronico: 'm@u.co', username: 'mperez', codigoEstudiantil: '1046',
    uuidProgramaAcademico: ISI, programaAcademico: 'Ingeniería de Sistemas', semestre: '5', facultad: 'FIET',
    asignaturasMatriculadas: estados.map((estado, i) => ({
      uuidAsignaturaMatriculada: `m-${i + 1}`, uuidAsignatura: `ISI-${i + 1}`, codigoAsignatura: `ISI-00${i + 1}`,
      nombreAsignatura: `Materia ${i + 1}`, grupo: 'A', estado
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

  function porPrograma(uuid: string): TestRequest {
    return http.expectOne(`${URL_ASIGNATURAS}/programa/${uuid}`);
  }

  function crear(): ComponentFixture<AdminEstudiantesContentComponent> {
    const fixture = TestBed.createComponent(AdminEstudiantesContentComponent);
    fixture.detectChanges();
    filtro().flush({ content: [estudiante('e-1'), estudiante('e-2', ['aprobada'])], totalElements: 12 });
    fixture.detectChanges();
    return fixture;
  }

  function abrirCrear(fixture: ComponentFixture<AdminEstudiantesContentComponent>): AdminEstudiantesContentComponent {
    const componente = fixture.componentInstance;
    componente.abrirCrear();
    http.expectOne(URL_PROGRAMAS).flush(PROGRAMAS);
    fixture.detectChanges();
    return componente;
  }

  function elegirSistemas(fixture: ComponentFixture<AdminEstudiantesContentComponent>): void {
    fixture.componentInstance.elegirPrograma(ISI);
    porPrograma(ISI).flush(asignaturasDe('ISI', ISI, 'Ingeniería de Sistemas'));
    fixture.detectChanges();
  }

  function llenar(fixture: ComponentFixture<AdminEstudiantesContentComponent>): AdminEstudiantesContentComponent {
    const componente = abrirCrear(fixture);
    elegirSistemas(fixture);
    Object.assign(componente.datosUsuario, {
      nombres: ' Ana ', apellidos: 'Ruiz', tipoDocumento: 'Tarjeta de identidad', numeroDocumento: '1002003',
      telefono: '3101234', correoElectronico: 'ana@unicauca.edu.co', username: 'aruiz', password: CLAVE
    });
    Object.assign(componente.datosAcademicos, { codigoEstudiantil: '104620', semestre: '2', facultad: 'FIET' });
    componente.elegirAsignatura(0, 'ISI-001');
    componente.filasMatricula[0].grupo = ' A ';
    componente.agregarFilaAsignatura();
    componente.elegirAsignatura(1, 'ISI-003');
    componente.filasMatricula[1].grupo = 'B';
    fixture.detectChanges();
    return componente;
  }

  it('pinta la tabla con el nombre del programa y filtra por nombre, apellido y codigo desde la pagina 0', () => {
    const fixture = crear();
    const componente = fixture.componentInstance;
    expect(fixture.nativeElement.querySelectorAll('tbody tr').length).toBe(2);
    expect(fixture.nativeElement.querySelector('thead').textContent).toContain('Código estudiantil');
    expect(fixture.nativeElement.querySelector('tbody tr').textContent).toContain('Ingeniería de Sistemas');
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

  it('el select de programa va primero y las asignaturas quedan deshabilitadas hasta elegirlo', () => {
    const fixture = crear();
    const componente = abrirCrear(fixture);
    const dialogo = document.querySelector('[data-dialogo-crear]')!;
    const primerCampo = dialogo.querySelector('select, input');
    expect(primerCampo?.closest('[data-campo-programa]')).toBeTruthy();
    expect(componente.opcionesPrograma.map(o => o.value)).toEqual(PROGRAMAS.map(p => p.uuidProgramaAcademico));
    const seccion = dialogo.querySelector('[data-seccion-asignaturas]') as HTMLFieldSetElement;
    expect(seccion.disabled).toBeTrue();
    expect(seccion.querySelector('select')?.matches(':disabled')).toBeTrue();
    expect(dialogo.querySelector('[data-aviso-asignaturas="sin-programa"]')).toBeTruthy();
    http.expectNone(req => req.url.startsWith(`${URL_ASIGNATURAS}/programa/`));
    elegirSistemas(fixture);
    expect(seccion.disabled).toBeFalse();
    const opciones = Array.from(seccion.querySelectorAll('[data-fila-asignatura] option')).map(o => o.textContent?.trim());
    expect(opciones).toContain('ISI-001 - Materia 1');
  });

  it('crea el estudiante con uuidProgramaAcademico y asignaturas por codigo y grupo', () => {
    const fixture = crear();
    const componente = llenar(fixture);
    componente.crear();
    const req = http.expectOne(URL);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({
      nombres: 'Ana', apellidos: 'Ruiz', tipoDocumento: 'Tarjeta de identidad', numeroDocumento: '1002003',
      telefono: '3101234', correoElectronico: 'ana@unicauca.edu.co', username: 'aruiz', password: CLAVE,
      codigoEstudiantil: '104620', uuidProgramaAcademico: ISI, semestre: '2', facultad: 'FIET',
      asignaturas: [
        { codigoAsignatura: 'ISI-001', grupo: 'A' },
        { codigoAsignatura: 'ISI-003', grupo: 'B' }
      ]
    });
    req.flush(estudiante('nuevo'));
    expect(componente.dialogoCrear).toBeFalse();
    filtro();
  });

  it('no deja repetir una asignatura en el formulario', () => {
    const fixture = crear();
    const componente = llenar(fixture);
    expect(componente.opcionesPorFila[1].map(o => o.value)).not.toContain('ISI-001');
    expect(componente.opcionesPorFila[0].map(o => o.value)).not.toContain('ISI-003');
    componente.filasMatricula[1].codigoAsignatura = 'ISI-001';
    componente.crear();
    expect(componente.errores['asignatura-1-codigo']).toContain('ya está en otra fila');
    http.expectNone(URL);
  });

  it('no deja agregar mas filas que asignaturas tiene el programa', () => {
    const fixture = crear();
    const componente = llenar(fixture);
    componente.agregarFilaAsignatura();
    expect(componente.filasMatricula.length).toBe(3);
    expect(componente.puedeAgregarFila).toBeFalse();
    componente.agregarFilaAsignatura();
    expect(componente.filasMatricula.length).toBe(3);
  });

  it('cambiar el programa con filas pide confirmacion; conservar no toca nada y confirmar limpia las filas', () => {
    const fixture = crear();
    const componente = llenar(fixture);
    componente.elegirPrograma(TTE);
    fixture.detectChanges();
    expect(document.querySelector('[data-confirmar-programa]')).toBeTruthy();
    expect((document.querySelector('[data-seccion-asignaturas]') as HTMLFieldSetElement).disabled).toBeTrue();
    http.expectNone(`${URL_ASIGNATURAS}/programa/${TTE}`);
    componente.crear();
    expect(componente.errores['programa']).toContain('Confirma o cancela');
    http.expectNone(URL);

    componente.cancelarCambioPrograma();
    fixture.detectChanges();
    expect(componente.uuidPrograma).toBe(ISI);
    expect(componente.filasMatricula.map(f => f.codigoAsignatura)).toEqual(['ISI-001', 'ISI-003']);
    expect(document.querySelector('[data-confirmar-programa]')).toBeNull();

    componente.elegirPrograma(TTE);
    componente.confirmarCambioPrograma();
    porPrograma(TTE).flush(asignaturasDe('TTE', TTE, 'Tecnología en Telemática'));
    fixture.detectChanges();
    expect(componente.uuidPrograma).toBe(TTE);
    expect(componente.filasMatricula).toEqual([{ codigoAsignatura: null, grupo: '' }]);
    expect(componente.opcionesPorFila[0].map(o => o.value)).toEqual(['TTE-001', 'TTE-002', 'TTE-003']);
  });

  it('cambiar el programa sin filas llenas no pide confirmacion', () => {
    const fixture = crear();
    const componente = abrirCrear(fixture);
    elegirSistemas(fixture);
    componente.elegirPrograma(TTE);
    expect(componente.confirmandoCambioPrograma).toBeFalse();
    porPrograma(TTE).flush([]);
  });

  it('un programa sin asignaturas muestra el aviso y no deja crear', () => {
    const fixture = crear();
    const componente = abrirCrear(fixture);
    componente.elegirPrograma(TTE);
    porPrograma(TTE).flush([]);
    fixture.detectChanges();
    const aviso = document.querySelector('[data-aviso-asignaturas="sin-asignaturas"]');
    expect(aviso?.textContent).toContain('No hay asignaturas registradas para el programa Tecnología en Telemática');
    expect(aviso?.textContent).toContain('pestaña Asignaturas');
    expect((document.querySelector('[data-seccion-asignaturas]') as HTMLFieldSetElement).disabled).toBeTrue();
    componente.crear();
    expect(componente.errores['asignaturas']).toContain('créalas primero');
    http.expectNone(URL);
  });

  it('valida longitudes, correo, contraseña, programa y asignatura elegida', () => {
    const fixture = crear();
    const componente = abrirCrear(fixture);
    componente.datosUsuario.password = '1234';
    componente.datosUsuario.correoElectronico = 'sin-arroba';
    componente.datosAcademicos.semestre = '12345678901';
    componente.crear();
    expect(componente.errores['password']).toContain('entre 5 y 255');
    expect(componente.errores['correoElectronico']).toContain('formato');
    expect(componente.errores['semestre']).toContain('10');
    expect(componente.errores['programa']).toBeTruthy();
    expect(componente.errores['asignatura-0-codigo']).toBeTruthy();
    expect(componente.errores['asignatura-0-grupo']).toBeTruthy();
    http.expectNone(URL);
  });

  it('la contraseña no aparece en la consola ni en los mensajes y el error 4 del backend queda visible', () => {
    const fixture = crear();
    const componente = llenar(fixture);
    componente.crear();
    const mensaje = 'La asignatura TTE-001 (Redes) es del programa Tecnología en Telemática y el estudiante es del programa Ingeniería de Sistemas...';
    http.expectOne(URL).flush({ codigoError: '4', mensaje }, { status: 500, statusText: 'Error' });
    fixture.detectChanges();
    expect(componente.erroresServidor).toEqual([mensaje]);
    expect(document.querySelector('[data-dialogo-crear] [data-error="servidor"]')?.textContent).toContain('es del programa');
    expect(JSON.stringify(consola.calls.allArgs())).not.toContain(CLAVE);
    expect(JSON.stringify(toast.showError.calls.allArgs())).not.toContain(CLAVE);
    expect(JSON.stringify(componente.erroresServidor)).not.toContain(CLAVE);
  });

  it('si el catalogo de programas falla el alta no abre', () => {
    const componente = crear().componentInstance;
    componente.abrirCrear();
    http.expectOne(URL_PROGRAMAS).flush({ codigoError: '1', mensaje: 'Error' }, { status: 500, statusText: 'Error' });
    expect(componente.dialogoCrear).toBeFalse();
  });

  it('edita solo codigo estudiantil, semestre y facultad y muestra el programa de solo lectura', () => {
    const fixture = crear();
    const componente = fixture.componentInstance;
    componente.abrirEditar(componente.filas[0]);
    fixture.detectChanges();
    expect(document.querySelector('[data-dialogo-editar] [data-programa]')?.textContent).toContain('Ingeniería de Sistemas');
    expect(document.querySelectorAll('[data-dialogo-editar] input').length).toBe(3);
    componente.editar();
    expect(componente.errores['edicion']).toBeTruthy();
    componente.edicion.semestre = ' 6 ';
    componente.edicion.facultad = 'FIET';
    componente.editar();
    const req = http.expectOne(`${URL}/e-1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual({ semestre: '6', facultad: 'FIET' });
  });

  it('agrega una matricula eligiendo la asignatura del programa del estudiante con su grupo', () => {
    const fixture = crear();
    const componente = fixture.componentInstance;
    componente.abrirDetalle(componente.filas[0]);
    porPrograma(ISI).flush(asignaturasDe('ISI', ISI, 'Ingeniería de Sistemas'));
    fixture.detectChanges();
    const opciones = Array.from(document.querySelectorAll('[data-agregar-matricula] option')).map(o => o.textContent?.trim());
    expect(opciones).toEqual(['Seleccione una opción', 'ISI-002 - Materia 2', 'ISI-003 - Materia 3']);
    componente.agregarMatricula();
    expect(componente.errores['nueva-codigo']).toBeTruthy();
    http.expectNone(`${URL}/e-1/asignaturas`);
    componente.nuevaMatricula = { codigoAsignatura: 'ISI-003', grupo: ' C ' };
    componente.agregarMatricula();
    const req = http.expectOne(`${URL}/e-1/asignaturas`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ codigoAsignatura: 'ISI-003', grupo: 'C' });
    req.flush({ uuidAsignaturaMatriculada: 'm-9', uuidAsignatura: 'ISI-3', codigoAsignatura: 'ISI-003', nombreAsignatura: 'Materia 3', grupo: 'C', estado: 'activa' });
    expect(componente.seleccionado?.asignaturasMatriculadas.map(a => a.uuidAsignaturaMatriculada)).toEqual(['m-1', 'm-2', 'm-9']);
    expect(componente.opcionesDetalle.map(o => o.value)).toEqual(['ISI-002']);
  });

  it('el selector de matricula deja fuera las activas y conserva las canceladas, que el backend deja volver a matricular', () => {
    const fixture = crear();
    const componente = fixture.componentInstance;
    componente.abrirDetalle(componente.filas[0]);
    porPrograma(ISI).flush(asignaturasDe('ISI', ISI, 'Ingeniería de Sistemas'));
    expect(componente.opcionesDetalle.map(o => o.value)).toEqual(['ISI-002', 'ISI-003']);
    componente.estadosNuevos['m-1'] = 'cancelada';
    componente.cambiarEstado(componente.seleccionado!.asignaturasMatriculadas[0]);
    http.expectOne(`${URL}/e-1/asignaturas/m-1/estado`).flush({ ...componente.seleccionado!.asignaturasMatriculadas[0], estado: 'cancelada' });
    expect(componente.opcionesDetalle.map(o => o.value)).toEqual(['ISI-001', 'ISI-002', 'ISI-003']);
  });

  it('avisa cuando el estudiante ya tiene activas todas las asignaturas del programa', () => {
    const fixture = crear();
    const componente = fixture.componentInstance;
    componente.abrirDetalle(componente.filas[0]);
    porPrograma(ISI).flush(asignaturasDe('ISI', ISI, 'Ingeniería de Sistemas', 1));
    fixture.detectChanges();
    expect(document.querySelector('[data-aviso-detalle="todas-activas"]')).toBeTruthy();
    expect(document.querySelector('[data-boton-agregar-matricula]')).toBeNull();
  });

  it('el error de un campo desaparece en cuanto el campo queda valido', () => {
    const fixture = crear();
    const componente = abrirCrear(fixture);
    componente.crear();
    fixture.detectChanges();
    expect(componente.errores['codigoEstudiantil']).toBeTruthy();
    const entrada = document.querySelector('[data-dialogo-crear] input[id="Código estudiantil"]') as HTMLInputElement;
    entrada.value = '104620';
    entrada.dispatchEvent(new Event('input', { bubbles: true }));
    fixture.detectChanges();
    expect(componente.errores['codigoEstudiantil']).toBeUndefined();
    expect(document.querySelector('[data-dialogo-crear] [data-error="codigoEstudiantil"]')?.textContent?.trim()).toBe('');
    expect(componente.errores['semestre']).toBeTruthy();
  });

  it('agregar matricula muestra el error del backend si ya la tiene activa', () => {
    const fixture = crear();
    const componente = fixture.componentInstance;
    componente.abrirDetalle(componente.filas[0]);
    porPrograma(ISI).flush(asignaturasDe('ISI', ISI, 'Ingeniería de Sistemas'));
    componente.nuevaMatricula = { codigoAsignatura: 'ISI-001', grupo: 'A' };
    componente.agregarMatricula();
    http.expectOne(`${URL}/e-1/asignaturas`).flush(
      { codigoError: '6', mensaje: 'El estudiante ya tiene activa la asignatura ISI-001 en el grupo A...' },
      { status: 500, statusText: 'Error' }
    );
    fixture.detectChanges();
    expect(document.querySelector('[data-dialogo-detalle] [data-error="servidor"]')?.textContent).toContain('ya tiene activa');
  });

  it('el detalle avisa si el programa del estudiante no tiene asignaturas', () => {
    const fixture = crear();
    const componente = fixture.componentInstance;
    componente.abrirDetalle(componente.filas[0]);
    porPrograma(ISI).flush([]);
    fixture.detectChanges();
    expect(document.querySelector('[data-aviso-detalle="sin-asignaturas"]')?.textContent).toContain('pestaña Asignaturas');
    expect(document.querySelector('[data-boton-agregar-matricula]')).toBeNull();
  });

  it('cambia el estado solo de las activas con PATCH y el cuerpo estado', () => {
    const fixture = crear();
    const componente = fixture.componentInstance;
    componente.abrirDetalle(componente.filas[0]);
    porPrograma(ISI).flush(asignaturasDe('ISI', ISI, 'Ingeniería de Sistemas'));
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
    porPrograma(ISI).flush(asignaturasDe('ISI', ISI, 'Ingeniería de Sistemas'));
    componente.estadosNuevos['m-1'] = 'perdida';
    componente.cambiarEstado(componente.seleccionado!.asignaturasMatriculadas[0]);
    http.expectOne(`${URL}/e-1/asignaturas/m-1/estado`).flush(
      { codigoError: '6', mensaje: 'No se puede cambiar la asignatura matriculada de cancelada a perdida...' },
      { status: 500, statusText: 'Error' }
    );
    fixture.detectChanges();
    expect(document.querySelector('[data-dialogo-detalle] [data-error="servidor"]')?.textContent).toContain('No se puede cambiar');
  });

  it('la carga de Excel muestra los 14 encabezados, los programas validos y la regla de asignaturas del programa', () => {
    const fixture = crear();
    const componente = fixture.componentInstance;
    componente.abrirExcel();
    http.expectOne(URL_PROGRAMAS).flush(PROGRAMAS);
    fixture.detectChanges();
    const encabezados = Array.from(document.querySelectorAll('[data-encabezados] li')).map(li => li.textContent?.trim());
    expect(encabezados).toEqual(ENCABEZADOS_EXCEL_ESTUDIANTES);
    expect(encabezados.length).toBe(14);
    expect(encabezados).not.toContain('nombreAsignatura');
    const valores = Array.from(document.querySelectorAll('[data-valores] li')).map(li => li.textContent?.trim());
    expect(valores).toEqual(PROGRAMAS.map(p => p.nombre));
    expect(document.querySelector('[data-reglas]')?.textContent).toContain('ser del programa del estudiante');
    componente.cargarExcel(new File([new Uint8Array(5)], 'e.xlsx')).subscribe();
    expect(Array.from((http.expectOne(`${URL}/cargar/archivo`).request.body as FormData).keys())).toEqual(['file']);
    componente.alCargarExcel();
    expect(filtro().request.params.get('pagina')).toBe('0');
  });
});
