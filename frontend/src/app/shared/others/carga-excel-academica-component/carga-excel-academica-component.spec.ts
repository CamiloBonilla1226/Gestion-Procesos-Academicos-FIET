import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { CargaExcelAcademicaComponent } from './carga-excel-academica-component';
import { EstudianteService } from '../../../core/services/estudiante-service';
import { ErrorHandlerService } from '../../../core/services/error-handler-service';
import { ToastService } from '../../../core/services/toast-service';
import { InputAnexoUploadComponent } from '../../inputs/input-anexo-upload-component/input-anexo-upload-component';
import { ENCABEZADOS_EXCEL_ESTUDIANTES } from '../../../core/constantes/procesos-academicos';
import { environment } from '../../../../enviroments/environment';

const URL = `${environment.apiUrl}/estudiantes/cargar/archivo`;

describe('CargaExcelAcademicaComponent', () => {
  let http: HttpTestingController;
  let errores: jasmine.SpyObj<ErrorHandlerService>;
  let toast: jasmine.SpyObj<ToastService>;
  const entrada = { reset: jasmine.createSpy('reset') } as unknown as InputAnexoUploadComponent;

  beforeEach(() => {
    errores = jasmine.createSpyObj<ErrorHandlerService>('ErrorHandlerService', ['handleError']);
    toast = jasmine.createSpyObj<ToastService>('ToastService', ['showSuccess', 'showError']);
    TestBed.configureTestingModule({
      imports: [CargaExcelAcademicaComponent],
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

  function crear(): ComponentFixture<CargaExcelAcademicaComponent> {
    const servicio = TestBed.inject(EstudianteService);
    const fixture = TestBed.createComponent(CargaExcelAcademicaComponent);
    fixture.componentRef.setInput('visible', true);
    fixture.componentRef.setInput('titulo', 'Cargar estudiantes');
    fixture.componentRef.setInput('encabezados', ENCABEZADOS_EXCEL_ESTUDIANTES);
    fixture.componentRef.setInput('regla', 'Una fila por asignatura matriculada.');
    fixture.componentRef.setInput('nombrePlural', 'estudiantes');
    fixture.componentRef.setInput('cargar', (archivo: File) => servicio.crearEstudiantesDesdeArchivo(archivo));
    fixture.detectChanges();
    return fixture;
  }

  function excel(): File {
    return new File([new Uint8Array(20)], 'estudiantes.xlsx');
  }

  it('muestra los encabezados exactos en su orden y la regla de una fila por asignatura', () => {
    crear();
    const items = Array.from(document.querySelectorAll('[data-encabezados] li')).map(li => li.textContent?.trim());
    expect(items).toEqual(ENCABEZADOS_EXCEL_ESTUDIANTES);
    expect(document.querySelector('[data-regla]')?.textContent).toContain('Una fila por asignatura');
  });

  it('sin reglas ni valores no pinta esas listas, como en los usos anteriores', () => {
    crear();
    expect(document.querySelector('[data-reglas]')).toBeNull();
    expect(document.querySelector('[data-valores]')).toBeNull();
  });

  it('pinta la lista de reglas y la de valores permitidos con su titulo', () => {
    const fixture = crear();
    fixture.componentRef.setInput('reglas', ['El código se genera solo.', 'La lectura termina en la primera fila vacía.']);
    fixture.componentRef.setInput('tituloValores', 'Programas válidos para programaAcademico:');
    fixture.componentRef.setInput('valores', ['Ingeniería de Sistemas', 'Tecnología en Telemática']);
    fixture.detectChanges();
    const reglas = Array.from(document.querySelectorAll('[data-reglas] li')).map(li => li.textContent?.trim());
    expect(reglas).toEqual(['El código se genera solo.', 'La lectura termina en la primera fila vacía.']);
    expect(document.querySelector('[data-valores]')?.textContent).toContain('Programas válidos para programaAcademico:');
    const valores = Array.from(document.querySelectorAll('[data-valores] li')).map(li => li.textContent?.trim());
    expect(valores).toEqual(['Ingeniería de Sistemas', 'Tecnología en Telemática']);
  });

  it('solo acepta .xlsx y exige el archivo antes de enviar', () => {
    const componente = crear().componentInstance;
    componente.confirmar();
    expect(componente.error).toContain('Elige el archivo');
    componente.alSeleccionar(new File(['x'], 'datos.csv'), entrada);
    expect(componente.archivo).toBeNull();
    expect(componente.error).toContain('.xlsx');
    http.expectNone(URL);
  });

  it('envia el archivo como multipart en la parte file y muestra cuantos se crearon', () => {
    const fixture = crear();
    const componente = fixture.componentInstance;
    const cargados: number[] = [];
    componente.cargado.subscribe(cantidad => cargados.push(cantidad));
    componente.alSeleccionar(excel(), entrada);
    componente.confirmar();
    componente.confirmar();
    const req = http.expectOne(URL);
    const cuerpo = req.request.body as FormData;
    expect(Array.from(cuerpo.keys())).toEqual(['file']);
    expect((cuerpo.get('file') as File).name).toBe('estudiantes.xlsx');
    req.flush([{}, {}, {}]);
    fixture.detectChanges();
    expect(document.querySelector('[data-resultado-carga]')?.textContent).toContain('Se crearon 3 estudiantes.');
    expect(toast.showSuccess).toHaveBeenCalledWith('Carga terminada', 'Se crearon 3 estudiantes.');
    expect(cargados).toEqual([]);
  });

  it('tras una carga exitosa muestra solo el resultado, sin el selector ni errores, y al cerrar avisa para refrescar', () => {
    const fixture = crear();
    const componente = fixture.componentInstance;
    const cargados: number[] = [];
    const visibles: boolean[] = [];
    componente.cargado.subscribe(cantidad => cargados.push(cantidad));
    componente.visibleChange.subscribe(visible => visibles.push(visible));
    fixture.componentRef.setInput('describir', (registro: { codigoAsignatura: string }) => registro.codigoAsignatura);
    componente.alSeleccionar(excel(), entrada);
    componente.confirmar();
    http.expectOne(URL).flush([{ codigoAsignatura: 'ISI-001' }, { codigoAsignatura: 'ISI-002' }]);
    fixture.detectChanges();
    const dialogo = document.querySelector('[data-carga-excel]') as HTMLElement;
    expect(dialogo.querySelector('app-input-anexo-upload-component')).toBeNull();
    expect(dialogo.textContent).not.toContain('Este anexo es obligatorio');
    expect(dialogo.querySelector('[data-error="archivo"]')).toBeNull();
    const creados = Array.from(dialogo.querySelectorAll('[data-creados] li')).map(li => li.textContent?.trim());
    expect(creados).toEqual(['ISI-001', 'ISI-002']);
    expect((document.querySelector('[data-boton-dialogo] button')?.textContent ?? '').trim()).toBe('Cerrar');
    componente.confirmar();
    http.expectNone(URL);
    expect(visibles).toEqual([false]);
    expect(cargados).toEqual([2]);
  });

  it('el 400 de la carga se muestra como lista dentro del dialogo', () => {
    const fixture = crear();
    const componente = fixture.componentInstance;
    componente.alSeleccionar(excel(), entrada);
    componente.confirmar();
    http.expectOne(URL).flush(
      {
        correoElectronico: 'Fila 3, columna F (correoElectronico): El correo electrónico debe tener un formato válido',
        semestre: 'Fila 4, columna K (semestre): El semestre debe tener máximo 10 caracteres'
      },
      { status: 400, statusText: 'Bad Request' }
    );
    fixture.detectChanges();
    const lista = Array.from(document.querySelectorAll('[data-errores-carga] li')).map(li => li.textContent?.trim());
    expect(lista).toEqual([
      'Fila 3, columna F (correoElectronico): El correo electrónico debe tener un formato válido',
      'Fila 4, columna K (semestre): El semestre debe tener máximo 10 caracteres'
    ]);
    expect(errores.handleError).toHaveBeenCalled();
  });

  it('el 500 de la carga muestra el mensaje de la fila y columna en el dialogo', () => {
    const fixture = crear();
    fixture.componentInstance.alSeleccionar(excel(), entrada);
    fixture.componentInstance.confirmar();
    http.expectOne(URL).flush(
      { codigoError: '6', mensaje: 'Fila 1, columna C: se esperaba el encabezado tipoDocumento y se encontró \'tipo\'...' },
      { status: 500, statusText: 'Error' }
    );
    fixture.detectChanges();
    expect(document.querySelector('[data-errores-carga]')?.textContent).toContain('Fila 1, columna C');
    expect(fixture.componentInstance.enviando).toBeFalse();
  });

  it('el boton del dialogo dice Cargar archivo', () => {
    crear();
    expect((document.querySelector('[data-boton-dialogo] button')?.textContent ?? '').trim()).toBe('Cargar archivo');
  });
});
