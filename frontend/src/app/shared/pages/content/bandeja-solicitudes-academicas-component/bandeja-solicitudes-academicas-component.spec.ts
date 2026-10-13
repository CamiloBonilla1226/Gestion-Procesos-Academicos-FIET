import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { BandejaSolicitudesAcademicasComponent } from './bandeja-solicitudes-academicas-component';
import { SolicitudAcademicaService } from '../../../../core/services/solicitud-academica-service';
import { ErrorHandlerService } from '../../../../core/services/error-handler-service';
import { SolicitudAcademicaResumenDTORespuesta } from '../../../../core/models/SolicitudAcademica/DTOResponse/SolicitudAcademicaResumenDTORespuesta';

function solicitudes(cantidad: number): SolicitudAcademicaResumenDTORespuesta[] {
  return Array.from({ length: cantidad }, (_, i) => ({
    uuidSolicitudAcademica: `uuid-${i + 1}`,
    radicado: `2026-CA-${String(i + 1).padStart(4, '0')}`,
    uuidTipoSolicitudAcademica: 't',
    tipoSolicitud: i % 2 === 0 ? 'Cancelación de Asignatura' : 'Examen Supletorio',
    fechaCreacion: '2026-12-31T23:59:59',
    etiqueta: 'Pendiente',
    etapaCodigo: 'RADICADA',
    nombreEstudiante: `Estudiante ${i + 1}`,
    codigoEstudiantil: `10461${i + 1}`
  }));
}

describe('BandejaSolicitudesAcademicasComponent', () => {
  let servicio: jasmine.SpyObj<SolicitudAcademicaService>;
  let errores: jasmine.SpyObj<ErrorHandlerService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    servicio = jasmine.createSpyObj<SolicitudAcademicaService>('SolicitudAcademicaService', [
      'getSolicitudesEstudiante', 'getSolicitudesFuncionario', 'getSolicitudesDecano'
    ]);
    errores = jasmine.createSpyObj<ErrorHandlerService>('ErrorHandlerService', ['handleError']);
    router = jasmine.createSpyObj<Router>('Router', ['navigate']);
    TestBed.configureTestingModule({
      imports: [BandejaSolicitudesAcademicasComponent],
      providers: [
        { provide: SolicitudAcademicaService, useValue: servicio },
        { provide: ErrorHandlerService, useValue: errores },
        { provide: Router, useValue: router }
      ]
    });
  });

  function crear(rol: string): ComponentFixture<BandejaSolicitudesAcademicasComponent> {
    const fixture = TestBed.createComponent(BandejaSolicitudesAcademicasComponent);
    fixture.componentRef.setInput('rol', rol);
    fixture.detectChanges();
    return fixture;
  }

  function filas(fixture: ComponentFixture<BandejaSolicitudesAcademicasComponent>): HTMLTableRowElement[] {
    return Array.from(fixture.nativeElement.querySelectorAll('tbody tr'));
  }

  function encabezados(fixture: ComponentFixture<BandejaSolicitudesAcademicasComponent>): string[] {
    return Array.from(fixture.nativeElement.querySelectorAll('thead th')).map((th: any) => th.textContent.trim());
  }

  it('el Funcionario ve 10 filas por pagina con las columnas del estudiante', () => {
    servicio.getSolicitudesFuncionario.and.returnValue(of(solicitudes(23)));
    const fixture = crear('FUNCIONARIO');

    expect(servicio.getSolicitudesFuncionario).toHaveBeenCalledTimes(1);
    expect(filas(fixture).length).toBe(10);
    expect(encabezados(fixture)).toEqual([
      'Radicado', 'Tipo de solicitud', 'Fecha', 'Estado', 'Estudiante', 'Código estudiantil', 'Acciones'
    ]);
    expect(filas(fixture)[0].textContent).toContain('31/12/2026 23:59');

    fixture.componentInstance.onPageChange(3);
    fixture.detectChanges();
    expect(filas(fixture).length).toBe(3);
  });

  it('el Estudiante no ve las columnas del estudiante', () => {
    servicio.getSolicitudesEstudiante.and.returnValue(of(solicitudes(2)));
    const fixture = crear('ESTUDIANTE');
    expect(encabezados(fixture)).toEqual(['Radicado', 'Tipo de solicitud', 'Fecha', 'Estado', 'Acciones']);
    expect(filas(fixture).length).toBe(2);
  });

  it('filtrar vuelve a la pagina 1', () => {
    servicio.getSolicitudesDecano.and.returnValue(of(solicitudes(23)));
    const fixture = crear('DECANO');
    const componente = fixture.componentInstance;

    componente.onPageChange(2);
    componente.onTipoChange('Examen Supletorio');
    fixture.detectChanges();

    expect(componente.paginaActual).toBe(1);
    expect(componente.filtradas.length).toBe(11);
    expect(filas(fixture).length).toBe(10);

    componente.onBusquedaChange('2026-CA-0004');
    fixture.detectChanges();
    expect(filas(fixture).length).toBe(1);
  });

  it('Ver navega al detalle del rol', () => {
    servicio.getSolicitudesDecano.and.returnValue(of(solicitudes(1)));
    const fixture = crear('DECANO');
    (fixture.nativeElement.querySelector('tbody tr button') as HTMLButtonElement).click();
    expect(router.navigate).toHaveBeenCalledWith(['/decano/solicitudes', 'uuid-1']);
  });

  it('el Funcionario ve una insignia con la decision del Decano que trae la etiqueta', () => {
    const lista = solicitudes(3);
    lista[0].etiqueta = 'Aprobada por el Decano';
    lista[1].etiqueta = 'Rechazada por el Decano';
    servicio.getSolicitudesFuncionario.and.returnValue(of(lista));
    const fixture = crear('FUNCIONARIO');
    const aprobada = fixture.nativeElement.querySelector('[data-decision-decano="uuid-1"]');
    const rechazada = fixture.nativeElement.querySelector('[data-decision-decano="uuid-2"]');
    expect(aprobada.textContent.trim()).toBe('Aprobada por el Decano');
    expect(aprobada.classList).toContain('text-bg-success');
    expect(rechazada.textContent.trim()).toBe('Rechazada por el Decano');
    expect(rechazada.classList).toContain('text-bg-danger');
    expect(fixture.nativeElement.querySelector('[data-decision-decano="uuid-3"]')).toBeNull();
  });

  it('el Estudiante y el Decano no ven insignias de decision', () => {
    const lista = solicitudes(1);
    lista[0].etiqueta = 'Aprobada por el Decano';
    servicio.getSolicitudesEstudiante.and.returnValue(of(lista));
    servicio.getSolicitudesDecano.and.returnValue(of(lista));
    expect(crear('ESTUDIANTE').nativeElement.querySelector('[data-decision-decano]')).toBeNull();
    expect(crear('DECANO').nativeElement.querySelector('[data-decision-decano]')).toBeNull();
  });

  it('muestra el estado vacio', () => {
    servicio.getSolicitudesEstudiante.and.returnValue(of([]));
    const fixture = crear('ESTUDIANTE');
    const vacio = fixture.nativeElement.querySelector('[data-estado="vacio"]');
    expect(vacio?.textContent.trim()).toBe('No hay solicitudes para mostrar');
    expect(filas(fixture).length).toBe(0);
  });

  it('muestra el error y lo pasa al ErrorHandlerService', () => {
    servicio.getSolicitudesFuncionario.and.returnValue(throwError(() => new HttpErrorResponse({ status: 500 })));
    const fixture = crear('FUNCIONARIO');
    expect(fixture.nativeElement.querySelector('[data-estado="error"]')).toBeTruthy();
    expect(errores.handleError).toHaveBeenCalledTimes(1);
    expect(filas(fixture).length).toBe(0);
  });
});
