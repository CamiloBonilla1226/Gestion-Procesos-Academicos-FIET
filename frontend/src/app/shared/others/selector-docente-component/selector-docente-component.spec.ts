import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { SelectorDocenteComponent, TAMANIO_PAGINA_DOCENTES } from './selector-docente-component';
import { ExamenSupletorioService } from '../../../core/services/examen-supletorio-service';
import { ErrorHandlerService } from '../../../core/services/error-handler-service';
import { DocenteDTORespuesta } from '../../../core/models/ExamenSupletorio/DTOResponse/DocenteDTORespuesta';

const ANA: DocenteDTORespuesta = { uuidUsuario: 'doc-1', nombres: 'Ana', apellidos: 'Ruiz', numeroDocumento: '1061', username: 'aruiz' };
const LUIS: DocenteDTORespuesta = { uuidUsuario: 'doc-2', nombres: 'Luis', apellidos: 'Paz', numeroDocumento: '1062', username: 'lpaz' };

describe('SelectorDocenteComponent', () => {
  let servicio: jasmine.SpyObj<ExamenSupletorioService>;
  let errores: jasmine.SpyObj<ErrorHandlerService>;

  beforeEach(() => {
    servicio = jasmine.createSpyObj<ExamenSupletorioService>('ExamenSupletorioService', ['buscarDocentes']);
    errores = jasmine.createSpyObj<ErrorHandlerService>('ErrorHandlerService', ['handleError']);
    servicio.buscarDocentes.and.returnValue(of({ content: [ANA, LUIS], totalElements: 2 }));
    TestBed.configureTestingModule({
      imports: [SelectorDocenteComponent],
      providers: [
        { provide: ExamenSupletorioService, useValue: servicio },
        { provide: ErrorHandlerService, useValue: errores }
      ]
    });
  });

  function crear(): ComponentFixture<SelectorDocenteComponent> {
    const fixture = TestBed.createComponent(SelectorDocenteComponent);
    fixture.detectChanges();
    return fixture;
  }

  it('al iniciar lista la primera pagina de docentes activos sin filtros', () => {
    const fixture = crear();
    expect(servicio.buscarDocentes).toHaveBeenCalledWith({ nombre: '', numeroDocumento: '', username: '' }, 0, TAMANIO_PAGINA_DOCENTES);
    const filas = fixture.nativeElement.querySelectorAll('[data-tabla-docentes] tbody tr');
    expect(filas.length).toBe(2);
    expect(filas[0].textContent).toContain('Ana Ruiz');
    expect(filas[0].textContent).toContain('1061');
    expect(filas[0].textContent).toContain('aruiz');
    const encabezados = Array.from(fixture.nativeElement.querySelectorAll('th')).map((th: any) => th.textContent.trim());
    expect(encabezados.slice(0, 3)).toEqual(['Nombre', 'Cédula', 'Usuario']);
  });

  it('tiene los tres filtros de texto opcionales y busca en el servidor con ellos recortados', () => {
    const fixture = crear();
    expect(fixture.nativeElement.querySelector('[data-filtro="nombre"]')?.textContent).toContain('Nombre');
    expect(fixture.nativeElement.querySelector('[data-filtro="numeroDocumento"]')?.textContent).toContain('Número de cédula');
    expect(fixture.nativeElement.querySelector('[data-filtro="username"]')?.textContent).toContain('Usuario');
    fixture.componentInstance.filtro = { nombre: ' Ana ', numeroDocumento: ' 106 ', username: '' };
    fixture.componentInstance.paginaActual = 3;
    fixture.componentInstance.buscar();
    expect(servicio.buscarDocentes).toHaveBeenCalledWith({ nombre: 'Ana', numeroDocumento: '106', username: '' }, 0, TAMANIO_PAGINA_DOCENTES);
  });

  it('pagina en el servidor y muestra el paginador solo con mas de una pagina', () => {
    servicio.buscarDocentes.and.returnValue(of({ content: [ANA], totalElements: 12 }));
    const fixture = crear();
    expect(fixture.componentInstance.totalPaginas).toBe(3);
    expect(fixture.nativeElement.querySelector('app-paginator')).toBeTruthy();
    fixture.componentInstance.cambiarPagina(2);
    expect(servicio.buscarDocentes.calls.mostRecent().args[1]).toBe(1);
  });

  it('elegir un docente lo emite y lo marca como elegido', () => {
    const fixture = crear();
    const emitidos: (DocenteDTORespuesta | null)[] = [];
    fixture.componentInstance.seleccionadoChange.subscribe(d => emitidos.push(d));
    (fixture.nativeElement.querySelector('[data-docente="doc-2"] button') as HTMLButtonElement).click();
    fixture.detectChanges();
    expect(emitidos).toEqual([LUIS]);
    expect(fixture.nativeElement.querySelector('[data-docente="doc-2"]').classList).toContain('fila-resaltada');
    expect(fixture.nativeElement.querySelector('[data-docente-elegido]').textContent).toContain('Luis Paz');
  });

  it('sin resultados avisa y un error de busqueda pasa al manejador de errores', () => {
    servicio.buscarDocentes.and.returnValue(of({ content: [], totalElements: 0 }));
    const fixture = crear();
    expect(fixture.nativeElement.querySelector('[data-sin-docentes]')).toBeTruthy();
    servicio.buscarDocentes.and.returnValue(throwError(() => ({ status: 500 })));
    fixture.componentInstance.buscar();
    expect(errores.handleError).toHaveBeenCalledWith({ status: 500 }, 'Error', 'No se pudieron buscar los profesores');
  });

  it('deshabilitado no permite elegir', () => {
    const fixture = crear();
    fixture.componentInstance.deshabilitado = true;
    fixture.componentInstance.elegir(ANA);
    expect(fixture.componentInstance.seleccionado).toBeNull();
  });
});
