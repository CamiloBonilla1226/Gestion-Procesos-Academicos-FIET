import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, TestRequest, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { AdminAsignaturasContentComponent } from './admin-asignaturas-content-component';
import { ErrorHandlerService } from '../../../../core/services/error-handler-service';
import { ToastService } from '../../../../core/services/toast-service';
import { ENCABEZADOS_EXCEL_ASIGNATURAS } from '../../../../core/constantes/procesos-academicos';
import { environment } from '../../../../../enviroments/environment';

const URL = `${environment.apiUrl}/asignaturas`;
const URL_PROGRAMAS = `${environment.apiUrl}/catalogos-academicos/programas`;
const ISI = 'uuid-isi';
const PROGRAMAS = [
  { uuidProgramaAcademico: 'uuid-iet', nombre: 'Ingeniería Electrónica y Telecomunicaciones', sigla: 'IET' },
  { uuidProgramaAcademico: ISI, nombre: 'Ingeniería de Sistemas', sigla: 'ISI' },
  { uuidProgramaAcademico: 'uuid-iai', nombre: 'Ingeniería en Automática Industrial', sigla: 'IAI' },
  { uuidProgramaAcademico: 'uuid-tte', nombre: 'Tecnología en Telemática', sigla: 'TTE' },
  { uuidProgramaAcademico: 'uuid-iac', nombre: 'Ingeniería Aeroespacial', sigla: 'IAC' }
];

function pagina(cantidad: number, total = cantidad) {
  return {
    content: Array.from({ length: cantidad }, (_, i) => ({
      uuidAsignatura: `a-${i + 1}`,
      codigoAsignatura: `ISI-00${i + 1}`,
      nombreAsignatura: `Asignatura ${i + 1}`,
      uuidProgramaAcademico: ISI,
      programaAcademico: 'Ingeniería de Sistemas'
    })),
    totalElements: total
  };
}

describe('AdminAsignaturasContentComponent', () => {
  let http: HttpTestingController;
  let errores: jasmine.SpyObj<ErrorHandlerService>;
  let toast: jasmine.SpyObj<ToastService>;

  beforeEach(() => {
    errores = jasmine.createSpyObj<ErrorHandlerService>('ErrorHandlerService', ['handleError']);
    toast = jasmine.createSpyObj<ToastService>('ToastService', ['showSuccess', 'showError']);
    TestBed.configureTestingModule({
      imports: [AdminAsignaturasContentComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideNoopAnimations(),
        { provide: ErrorHandlerService, useValue: errores },
        { provide: ToastService, useValue: toast }
      ]
    });
    http = TestBed.inject(HttpTestingController);
  });

  function filtro(): TestRequest {
    return http.expectOne(req => req.url === `${URL}/filtro`);
  }

  function crear(datos = pagina(3, 25)): ComponentFixture<AdminAsignaturasContentComponent> {
    const fixture = TestBed.createComponent(AdminAsignaturasContentComponent);
    fixture.detectChanges();
    http.expectOne(URL_PROGRAMAS).flush(PROGRAMAS);
    filtro().flush(datos);
    fixture.detectChanges();
    return fixture;
  }

  it('pide la primera pagina en base 0 y pinta codigo, nombre y programa con su paginador', () => {
    const fixture = TestBed.createComponent(AdminAsignaturasContentComponent);
    fixture.detectChanges();
    http.expectOne(URL_PROGRAMAS).flush(PROGRAMAS);
    const req = filtro();
    expect(req.request.params.get('pagina')).toBe('0');
    expect(req.request.params.get('tamanio')).toBe('10');
    expect(req.request.params.has('texto')).toBeFalse();
    expect(req.request.params.has('uuidProgramaAcademico')).toBeFalse();
    req.flush(pagina(3, 25));
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('thead').textContent).toContain('Programa');
    const primera = fixture.nativeElement.querySelector('tbody tr').textContent;
    expect(primera).toContain('ISI-001');
    expect(primera).toContain('Ingeniería de Sistemas');
    expect(fixture.componentInstance.totalPaginas).toBe(3);
  });

  it('el filtro de programa ofrece Todos y los cinco programas del catalogo', () => {
    const fixture = crear();
    const opciones = Array.from(fixture.nativeElement.querySelectorAll('[data-filtro-programa] option')).map(o => (o as HTMLOptionElement).textContent?.trim());
    expect(opciones).toContain('Todos');
    expect(fixture.componentInstance.opcionesFiltro.map(o => o.value)).toEqual(['', ...PROGRAMAS.map(p => p.uuidProgramaAcademico)]);
  });

  it('busca por texto y programa desde la pagina 1 y Todos no manda el programa', () => {
    const fixture = crear();
    const componente = fixture.componentInstance;
    componente.cambiarPagina(3);
    expect(filtro().request.params.get('pagina')).toBe('2');
    componente.texto = ' calculo ';
    componente.programaFiltro = ISI;
    componente.buscar();
    const req = filtro();
    expect(req.request.params.get('texto')).toBe('calculo');
    expect(req.request.params.get('uuidProgramaAcademico')).toBe(ISI);
    expect(req.request.params.get('pagina')).toBe('0');
    expect(componente.paginaActual).toBe(1);
    componente.programaFiltro = '';
    componente.buscar();
    expect(filtro().request.params.has('uuidProgramaAcademico')).toBeFalse();
  });

  it('muestra el estado vacio y el de error con reintento', () => {
    const vacio = crear(pagina(0));
    expect(vacio.nativeElement.querySelector('[data-estado="vacio"]')).toBeTruthy();
    const fixture = TestBed.createComponent(AdminAsignaturasContentComponent);
    fixture.detectChanges();
    http.expectOne(URL_PROGRAMAS).flush(PROGRAMAS);
    filtro().flush({ codigoError: '6', mensaje: 'Error en la paginación...' }, { status: 500, statusText: 'Error' });
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[data-estado="error"]')).toBeTruthy();
    expect(errores.handleError).toHaveBeenCalled();
  });

  it('crea con nombre y programa, sin codigo, y avisa el codigo generado', () => {
    const fixture = crear();
    const componente = fixture.componentInstance;
    componente.abrirNueva();
    fixture.detectChanges();
    const dialogo = document.querySelector('[data-dialogo-asignatura]')!;
    expect(dialogo.querySelector('[data-campo-programa] select')).toBeTruthy();
    expect(dialogo.textContent).not.toContain('Código');
    componente.nombre = ' Compiladores ';
    componente.uuidPrograma = ISI;
    componente.guardar();
    const req = http.expectOne(URL);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ nombreAsignatura: 'Compiladores', uuidProgramaAcademico: ISI });
    req.flush({ uuidAsignatura: 'n', codigoAsignatura: 'ISI-001', nombreAsignatura: 'Compiladores', uuidProgramaAcademico: ISI, programaAcademico: 'Ingeniería de Sistemas' });
    expect(toast.showSuccess).toHaveBeenCalledWith('Listo', 'Asignatura creada con el código ISI-001');
    expect(componente.dialogoVisible).toBeFalse();
    filtro();
  });

  it('el programa es obligatorio al crear y el nombre no pasa de 150', () => {
    const componente = crear().componentInstance;
    componente.abrirNueva();
    componente.guardar();
    expect(Object.keys(componente.errores).sort()).toEqual(['nombre', 'programa']);
    componente.nombre = 'N'.repeat(151);
    componente.uuidPrograma = ISI;
    componente.guardar();
    expect(componente.errores['nombre']).toContain('150');
    expect(componente.errores['programa']).toBeUndefined();
    http.expectNone(URL);
  });

  it('El nombre es obligatorio desaparece en cuanto el nombre es valido', () => {
    const fixture = crear();
    const componente = fixture.componentInstance;
    componente.abrirNueva();
    componente.guardar();
    fixture.detectChanges();
    const error = () => document.querySelector('[data-dialogo-asignatura] [data-error="nombre"]')?.textContent?.trim();
    expect(error()).toBe('El nombre es obligatorio.');
    const entrada = document.querySelector('[data-dialogo-asignatura] input[id="Nombre"]') as HTMLInputElement;
    entrada.value = 'Compiladores';
    entrada.dispatchEvent(new Event('input', { bubbles: true }));
    fixture.detectChanges();
    expect(error()).toBe('');
    expect(componente.errores['programa']).toBeTruthy();
  });

  it('la carga Excel lista los codigos generados con su nombre', () => {
    const componente = crear().componentInstance;
    expect(componente.describirAsignatura(componente.asignaturas[0])).toBe('ISI-001 - Asignatura 1');
  });

  it('edita solo el nombre con PUT y muestra codigo y programa de solo lectura', () => {
    const fixture = crear();
    const componente = fixture.componentInstance;
    componente.abrirEdicion(componente.filas[1]);
    fixture.detectChanges();
    expect(document.querySelector('[data-solo-lectura] [data-codigo]')?.textContent).toContain('ISI-002');
    expect(document.querySelector('[data-solo-lectura] [data-programa]')?.textContent).toContain('Ingeniería de Sistemas');
    expect(document.querySelector('[data-dialogo-asignatura] [data-campo-programa]')).toBeNull();
    componente.nombre = ' Asignatura renombrada ';
    componente.guardar();
    const req = http.expectOne(`${URL}/a-2`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual({ nombreAsignatura: 'Asignatura renombrada' });
    req.flush({ ...componente.asignaturas[1], nombreAsignatura: 'Asignatura renombrada' });
    expect(toast.showSuccess).toHaveBeenCalledWith('Listo', 'La asignatura quedó actualizada.');
  });

  it('muestra en el dialogo el error 2 de nombre repetido en el programa y bloquea el doble envio', () => {
    const fixture = crear();
    const componente = fixture.componentInstance;
    componente.abrirNueva();
    componente.nombre = 'Asignatura 1';
    componente.uuidPrograma = ISI;
    componente.guardar();
    componente.guardar();
    const mensaje = 'La asignatura Asignatura 1 ya existe en el programa Ingeniería de Sistemas con el código ISI-001...';
    http.expectOne(URL).flush({ codigoError: '2', mensaje }, { status: 500, statusText: 'Error' });
    fixture.detectChanges();
    expect(componente.erroresServidor).toEqual([mensaje]);
    expect(document.querySelector('[data-error="servidor"]')?.textContent).toContain('ya existe en el programa');
    expect(componente.dialogoVisible).toBeTrue();
  });

  it('el boton del dialogo dice Crear al crear y Guardar cambios al editar', () => {
    const fixture = crear();
    fixture.componentInstance.abrirNueva();
    fixture.detectChanges();
    expect((document.querySelector('[data-boton-dialogo] button')?.textContent ?? '').trim()).toBe('Crear');
    fixture.componentInstance.cerrar(false);
    fixture.componentInstance.abrirEdicion(fixture.componentInstance.filas[0]);
    fixture.detectChanges();
    expect((document.querySelector('[data-boton-dialogo] button')?.textContent ?? '').trim()).toBe('Guardar cambios');
  });

  it('si el catalogo de programas fallo, el alta lo vuelve a pedir y no abre si viene vacio', () => {
    const fixture = TestBed.createComponent(AdminAsignaturasContentComponent);
    fixture.detectChanges();
    http.expectOne(URL_PROGRAMAS).flush({ codigoError: '1', mensaje: 'Error' }, { status: 500, statusText: 'Error' });
    filtro().flush(pagina(1));
    const componente = fixture.componentInstance;
    componente.abrirNueva();
    http.expectOne(URL_PROGRAMAS).flush([]);
    expect(componente.dialogoVisible).toBeFalse();
    expect(errores.handleError).toHaveBeenCalledTimes(2);
    componente.abrirNueva();
    http.expectOne(URL_PROGRAMAS).flush(PROGRAMAS);
    expect(componente.dialogoVisible).toBeTrue();
  });

  it('la carga Excel de asignaturas manda el archivo en la parte file y muestra encabezados, programas y reglas', () => {
    const fixture = crear();
    const componente = fixture.componentInstance;
    componente.abrirExcel();
    fixture.detectChanges();
    const encabezados = Array.from(document.querySelectorAll('[data-encabezados] li')).map(li => li.textContent?.trim());
    expect(encabezados).toEqual(ENCABEZADOS_EXCEL_ASIGNATURAS);
    expect(encabezados).toEqual(['nombreAsignatura', 'programaAcademico']);
    const valores = Array.from(document.querySelectorAll('[data-valores] li')).map(li => li.textContent?.trim());
    expect(valores).toEqual(PROGRAMAS.map(p => p.nombre));
    expect(document.querySelector('[data-reglas]')?.textContent).toContain('el sistema lo genera');
    componente.cargarExcel(new File([new Uint8Array(5)], 'asignaturas.xlsx')).subscribe();
    const req = http.expectOne(`${URL}/cargar/archivo`);
    expect(req.request.method).toBe('POST');
    const cuerpo = req.request.body as FormData;
    expect(Array.from(cuerpo.keys())).toEqual(['file']);
    expect((cuerpo.get('file') as File).name).toBe('asignaturas.xlsx');
    componente.alCargarExcel();
    expect(filtro().request.params.get('pagina')).toBe('0');
  });

  it('los errores de fila y columna de la carga de asignaturas se listan dentro del dialogo', () => {
    const fixture = crear();
    fixture.componentInstance.abrirExcel();
    fixture.detectChanges();
    const carga = fixture.debugElement.query(el => el.name === 'app-carga-excel-academica-component').componentInstance;
    carga.alSeleccionar(new File([new Uint8Array(5)], 'asignaturas.xlsx'), { reset: () => {} });
    carga.confirmar();
    const mensaje = 'Fila 3, columna B (programaAcademico): el programa \'Medicina\' no existe, los programas permitidos son Ingeniería de Sistemas...';
    http.expectOne(`${URL}/cargar/archivo`).flush({ codigoError: '6', mensaje }, { status: 500, statusText: 'Error' });
    fixture.detectChanges();
    expect(document.querySelector('[data-errores-carga]')?.textContent).toContain('Fila 3, columna B (programaAcademico)');
  });
});
