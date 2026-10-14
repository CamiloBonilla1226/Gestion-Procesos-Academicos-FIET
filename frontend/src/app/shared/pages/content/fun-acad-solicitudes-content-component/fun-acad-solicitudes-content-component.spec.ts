import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, Data, Router, provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { BehaviorSubject, of } from 'rxjs';
import { FunAcadSolicitudesContentComponent } from './fun-acad-solicitudes-content-component';
import { SolicitudAcademicaService } from '../../../../core/services/solicitud-academica-service';
import { ErrorHandlerService } from '../../../../core/services/error-handler-service';
import { SolicitudAcademicaResumenDTORespuesta } from '../../../../core/models/SolicitudAcademica/DTOResponse/SolicitudAcademicaResumenDTORespuesta';
import { FuncionarioAcademicoSolicitudesComponent } from '../../../../core/funcionario-academico/pages/funcionario-academico-solicitudes-component/funcionario-academico-solicitudes-component';
import { FuncionarioAcademicoRespuestasComponent } from '../../../../core/funcionario-academico/pages/funcionario-academico-respuestas-component/funcionario-academico-respuestas-component';

function solicitud(radicado: string, etapaCodigo: string): SolicitudAcademicaResumenDTORespuesta {
  return {
    uuidSolicitudAcademica: radicado,
    radicado,
    uuidTipoSolicitudAcademica: 't',
    tipoSolicitud: 'Cancelación de Asignatura',
    fechaCreacion: '2026-10-01T08:00:00',
    etiqueta: 'Pendiente',
    etapaCodigo,
    nombreEstudiante: 'Estudiante',
    codigoEstudiantil: '104600'
  };
}

const LISTA = [
  solicitud('R-ABIERTA', 'RADICADA'),
  solicitud('R-APROBADA', 'APROBADA'),
  solicitud('R-DECANO', 'APROBADA_POR_DECANO'),
  solicitud('R-RECHAZADA', 'RECHAZADA')
];

function titulo(elemento: HTMLElement): string {
  return elemento.querySelector('.tarjeta-head h2')?.textContent?.trim() ?? '';
}

function radicados(elemento: HTMLElement): string[] {
  return Array.from(elemento.querySelectorAll('tbody .radicado')).map((e: any) => e.textContent.trim());
}

describe('FunAcadSolicitudesContentComponent', () => {
  function crear(data: Data) {
    const data$ = new BehaviorSubject<Data>(data);
    TestBed.configureTestingModule({
      imports: [FunAcadSolicitudesContentComponent],
      providers: [
        { provide: ActivatedRoute, useValue: { snapshot: { data }, data: data$ } },
        { provide: SolicitudAcademicaService, useValue: { getSolicitudesFuncionario: () => of(LISTA) } },
        { provide: ErrorHandlerService, useValue: jasmine.createSpyObj('ErrorHandlerService', ['handleError']) },
        { provide: Router, useValue: jasmine.createSpyObj('Router', ['navigate']) }
      ]
    });
    const fixture = TestBed.createComponent(FunAcadSolicitudesContentComponent);
    fixture.detectChanges();
    return { fixture, data$ };
  }

  it('pasa a la bandeja el modo respuestas que trae la ruta', () => {
    const { fixture } = crear({ modo: 'respuestas' });
    expect(fixture.componentInstance.modo()).toBe('respuestas');
    expect(titulo(fixture.nativeElement)).toBe('Solicitudes respondidas');
    expect(radicados(fixture.nativeElement)).toEqual(['R-APROBADA', 'R-RECHAZADA']);
  });

  it('sin modo en la ruta usa el de solicitudes', () => {
    const { fixture } = crear({});
    expect(fixture.componentInstance.modo()).toBe('solicitudes');
    expect(titulo(fixture.nativeElement)).toBe('Gestión de solicitudes');
    expect(radicados(fixture.nativeElement)).toEqual(['R-ABIERTA', 'R-DECANO']);
  });

  it('si la ruta cambia su data sin recrear el componente, la bandeja cambia de lista', () => {
    const { fixture, data$ } = crear({});
    const instancia = fixture.componentInstance;
    data$.next({ modo: 'respuestas' });
    fixture.detectChanges();
    expect(fixture.componentInstance).toBe(instancia);
    expect(instancia.modo()).toBe('respuestas');
    expect(radicados(fixture.nativeElement)).toEqual(['R-APROBADA', 'R-RECHAZADA']);

    data$.next({});
    fixture.detectChanges();
    expect(radicados(fixture.nativeElement)).toEqual(['R-ABIERTA', 'R-DECANO']);
  });

  it('al navegar entre Solicitudes y Respuestas cada pagina muestra solo lo suyo', async () => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([
          { path: 'funcionario-academico/solicitudes', component: FuncionarioAcademicoSolicitudesComponent },
          { path: 'funcionario-academico/respuestas', data: { modo: 'respuestas' }, component: FuncionarioAcademicoRespuestasComponent }
        ]),
        { provide: SolicitudAcademicaService, useValue: { getSolicitudesFuncionario: () => of(LISTA) } }
      ]
    });
    const harness = await RouterTestingHarness.create();

    await harness.navigateByUrl('/funcionario-academico/solicitudes');
    expect(radicados(harness.routeNativeElement!)).toEqual(['R-ABIERTA', 'R-DECANO']);

    await harness.navigateByUrl('/funcionario-academico/respuestas');
    expect(titulo(harness.routeNativeElement!)).toBe('Solicitudes respondidas');
    expect(radicados(harness.routeNativeElement!)).toEqual(['R-APROBADA', 'R-RECHAZADA']);

    await harness.navigateByUrl('/funcionario-academico/solicitudes');
    expect(radicados(harness.routeNativeElement!)).toEqual(['R-ABIERTA', 'R-DECANO']);
  });
});
