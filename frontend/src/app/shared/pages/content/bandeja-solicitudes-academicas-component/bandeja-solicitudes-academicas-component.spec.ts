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
    return Array.from(fixture.nativeElement.querySelectorAll('thead th'))
      .map((th: any) => th.textContent.replace('⇅', '').trim());
  }

  function estados(fixture: ComponentFixture<BandejaSolicitudesAcademicasComponent>): (string | null)[] {
    return Array.from(fixture.nativeElement.querySelectorAll('tbody .estado')).map((e: any) => e.getAttribute('data-categoria'));
  }

  it('el Funcionario ve 10 filas por pagina con las columnas del estudiante', () => {
    servicio.getSolicitudesFuncionario.and.returnValue(of(solicitudes(23)));
    const fixture = crear('FUNCIONARIO');

    expect(servicio.getSolicitudesFuncionario).toHaveBeenCalledTimes(1);
    expect(filas(fixture).length).toBe(10);
    expect(encabezados(fixture)).toEqual([
      'Radicado', 'Estudiante', 'Documento', 'Tipo de proceso', 'Fecha de radicación', 'Estado', 'Acciones'
    ]);
    expect(filas(fixture)[0].textContent).toContain('31/12/2026 23:59');
    expect(filas(fixture)[0].querySelector('.radicado')?.textContent?.trim()).toBe('2026-CA-0001');
    expect(fixture.nativeElement.querySelector('.tarjeta-head h2').textContent.trim()).toBe('Gestión de solicitudes');
    expect(fixture.nativeElement.querySelector('app-barra-busqueda-component')).toBeTruthy();

    fixture.componentInstance.onPageChange(3);
    fixture.detectChanges();
    expect(filas(fixture).length).toBe(3);
  });

  it('Funcionario y Decano ven el documento y lo buscan con la etiqueta de identificacion', () => {
    const lista = solicitudes(3);
    lista[1].numeroDocumento = '1061789234';
    servicio.getSolicitudesFuncionario.and.returnValue(of(lista));
    const fixture = crear('FUNCIONARIO');
    expect(filas(fixture)[1].querySelectorAll('td')[2].textContent.trim()).toBe('1061789234');
    expect(fixture.nativeElement.querySelector('.campo-etiqueta').textContent.trim()).toBe('Número de identificación');
    expect(fixture.nativeElement.querySelector('app-barra-busqueda-component input').getAttribute('placeholder')).toBe('Ej. 1061789234');

    fixture.componentInstance.onBusquedaChange('1061789');
    fixture.detectChanges();
    expect(filas(fixture).length).toBe(1);
    expect(filas(fixture)[0].querySelector('.radicado')?.textContent?.trim()).toBe('2026-CA-0002');
  });

  it('el Estudiante solo ve sus columnas y no tiene filtro por codigo', () => {
    servicio.getSolicitudesEstudiante.and.returnValue(of(solicitudes(2)));
    const fixture = crear('ESTUDIANTE');
    expect(encabezados(fixture)).toEqual(['Radicado', 'Tipo de proceso', 'Fecha de radicación', 'Estado', 'Acciones']);
    expect(fixture.nativeElement.textContent).not.toContain('Número de identificación');
    expect(filas(fixture).length).toBe(2);
    expect(fixture.nativeElement.querySelector('app-barra-busqueda-component')).toBeNull();
    expect(fixture.nativeElement.querySelector('.tarjeta-head h2').textContent.trim()).toBe('Mis trámites radicados');
  });

  it('el Decano ve la columna y el filtro de decision', () => {
    servicio.getSolicitudesDecano.and.returnValue(of(solicitudes(1)));
    const fixture = crear('DECANO');
    expect(encabezados(fixture)).toContain('Decisión');
    expect(fixture.nativeElement.querySelector('label[for="filtro-estado"]').textContent.trim()).toBe('Decisión');
    expect(fixture.nativeElement.querySelector('.tarjeta-head h2').textContent.trim())
      .toBe('Solicitudes remitidas por el Funcionario Académico');
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

    componente.onBusquedaChange('104614');
    fixture.detectChanges();
    expect(filas(fixture).length).toBe(1);
    expect(filas(fixture)[0].textContent).toContain('Estudiante 4');
    expect(filas(fixture)[0].textContent).not.toContain('104614');
  });

  it('filtra por estado con las etiquetas presentes y limpia los filtros', () => {
    const lista = solicitudes(4);
    lista[1].etiqueta = 'En Gestión';
    lista[3].etiqueta = 'En Gestión';
    servicio.getSolicitudesFuncionario.and.returnValue(of(lista));
    const fixture = crear('FUNCIONARIO');
    const componente = fixture.componentInstance;
    expect(componente.opcionesEstado).toEqual(['En Gestión', 'Pendiente']);

    const select = fixture.nativeElement.querySelector('#filtro-estado') as HTMLSelectElement;
    select.value = 'En Gestión';
    select.dispatchEvent(new Event('change'));
    fixture.detectChanges();
    expect(filas(fixture).length).toBe(2);

    componente.onBusquedaChange('sin coincidencias');
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[data-estado="sin-resultados"]')).toBeTruthy();

    (fixture.nativeElement.querySelector('[data-estado="sin-resultados"] .btn-limpiar') as HTMLButtonElement).click();
    fixture.detectChanges();
    expect(componente.estadoFiltro).toBe('TODOS');
    expect(componente.textoFiltro).toBe('');
    expect(filas(fixture).length).toBe(4);
  });

  it('ordena por fecha al pulsar el encabezado y alterna la direccion', () => {
    const lista = solicitudes(3);
    lista[0].fechaCreacion = '2026-10-02T10:00:00';
    lista[1].fechaCreacion = '2026-10-01T10:00:00';
    lista[2].fechaCreacion = '2026-10-03T10:00:00';
    servicio.getSolicitudesDecano.and.returnValue(of(lista));
    const fixture = crear('DECANO');
    const radicados = () => filas(fixture).map(f => f.querySelector('.radicado')?.textContent?.trim());

    const boton = fixture.nativeElement.querySelector('button[aria-label="Ordenar por fecha de radicación"]') as HTMLButtonElement;
    boton.click();
    fixture.detectChanges();
    expect(radicados()).toEqual(['2026-CA-0002', '2026-CA-0001', '2026-CA-0003']);

    boton.click();
    fixture.detectChanges();
    expect(radicados()).toEqual(['2026-CA-0003', '2026-CA-0001', '2026-CA-0002']);
  });

  it('el ojo de Acciones navega al detalle del rol', () => {
    servicio.getSolicitudesDecano.and.returnValue(of(solicitudes(1)));
    const fixture = crear('DECANO');
    const ojo = fixture.nativeElement.querySelector('tbody tr .btn-ojo') as HTMLButtonElement;
    expect(ojo.getAttribute('aria-label')).toBe('Ver solicitud 2026-CA-0001');
    ojo.click();
    expect(router.navigate).toHaveBeenCalledWith(['/decano/solicitudes', 'uuid-1']);
  });

  it('el Funcionario ve la decision del Decano en Estado con su icono y sin insignia aparte', () => {
    const lista = solicitudes(3);
    lista[0].etiqueta = 'Aprobada por el Decano';
    lista[1].etiqueta = 'Rechazada por el Decano';
    servicio.getSolicitudesFuncionario.and.returnValue(of(lista));
    const fixture = crear('FUNCIONARIO');
    expect(estados(fixture)).toEqual(['aprobada', 'rechazada', 'pendiente']);
    expect(filas(fixture)[0].querySelector('.estado')?.textContent?.trim()).toBe('Aprobada por el Decano');
    expect(filas(fixture)[1].querySelector('.estado')?.textContent?.trim()).toBe('Rechazada por el Decano');
    expect(fixture.nativeElement.querySelector('[data-decision-decano]')).toBeNull();
    expect(fixture.nativeElement.querySelector('.badge')).toBeNull();
  });

  it('el icono del Estudiante sale solo de la etiqueta, nunca de etapaCodigo', () => {
    const lista = solicitudes(4);
    lista[0].etiqueta = 'En trámite';
    lista[0].etapaCodigo = 'APROBADA';
    lista[1].etiqueta = 'Pendiente de pago';
    lista[1].etapaCodigo = 'RECHAZADA';
    lista[2].etiqueta = 'Aprobada';
    lista[2].etapaCodigo = 'RADICADA';
    lista[3].etiqueta = 'Rechazada';
    lista[3].etapaCodigo = 'RADICADA';
    servicio.getSolicitudesEstudiante.and.returnValue(of(lista));
    const fixture = crear('ESTUDIANTE');
    expect(estados(fixture)).toEqual(['gestion', 'pago', 'aprobada', 'rechazada']);
    expect(fixture.nativeElement.textContent).not.toContain('APROBADA');
    expect(fixture.nativeElement.textContent).not.toContain('RADICADA');
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
