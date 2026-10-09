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
    expect(cargados).toEqual([3]);
    expect(toast.showSuccess).toHaveBeenCalledWith('Carga terminada', 'Se crearon 3 estudiantes.');
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
});
