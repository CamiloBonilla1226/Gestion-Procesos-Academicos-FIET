import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { DocNotificacionesContentComponent } from './doc-notificaciones-content-component';
import { NotificacionDocenteService } from '../../../../core/services/notificacion-docente-service';
import { ErrorHandlerService } from '../../../../core/services/error-handler-service';
import { NotificacionDocenteResumenDTORespuesta } from '../../../../core/models/NotificacionDocente/DTOResponse/NotificacionDocenteResumenDTORespuesta';

function notificacion(uuid: string, leida: boolean): NotificacionDocenteResumenDTORespuesta {
  return {
    uuidNotificacion: uuid, titulo: 'Examen supletorio aprobado, radicado 2026-ES-0001', uuidSolicitudAcademica: 's-' + uuid,
    radicado: '2026-ES-000' + uuid.slice(-1), estudiante: 'María Pérez', asignatura: 'ISI-001 - Cálculo I', resultado: 'Aprobada',
    fechaCreacion: '2026-10-08T10:00:00', fechaLectura: leida ? '2026-10-08T11:00:00' : null, leida
  };
}

describe('DocNotificacionesContentComponent', () => {
  let servicio: jasmine.SpyObj<NotificacionDocenteService>;
  let errores: jasmine.SpyObj<ErrorHandlerService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    servicio = jasmine.createSpyObj<NotificacionDocenteService>('NotificacionDocenteService', ['getMisNotificaciones']);
    errores = jasmine.createSpyObj<ErrorHandlerService>('ErrorHandlerService', ['handleError']);
    router = jasmine.createSpyObj<Router>('Router', ['navigateByUrl']);
    TestBed.configureTestingModule({
      imports: [DocNotificacionesContentComponent],
      providers: [
        { provide: NotificacionDocenteService, useValue: servicio },
        { provide: ErrorHandlerService, useValue: errores },
        { provide: Router, useValue: router }
      ]
    });
  });

  it('lista radicado, estudiante, asignatura, resultado y fecha en el orden que llega, destacando las no leidas', () => {
    servicio.getMisNotificaciones.and.returnValue(of([notificacion('n-2', false), notificacion('n-1', true)]));
    const fixture = TestBed.createComponent(DocNotificacionesContentComponent);
    fixture.detectChanges();
    const encabezados = Array.from(fixture.nativeElement.querySelectorAll('th')).map((th: any) => th.textContent.trim());
    expect(encabezados.slice(0, 5)).toEqual(['Radicado', 'Estudiante', 'Asignatura', 'Resultado', 'Fecha']);
    const filas = fixture.nativeElement.querySelectorAll('tbody tr');
    expect(filas[0].getAttribute('data-notificacion')).toBe('n-2');
    expect(filas[0].classList).toContain('no-leida');
    expect(filas[1].classList).not.toContain('no-leida');
    expect(filas[0].textContent).toContain('María Pérez');
    expect(filas[0].textContent).toContain('ISI-001 - Cálculo I');
    expect(filas[0].textContent).toContain('Aprobada');
    expect(filas[0].textContent).toContain('08/10/2026');
    expect(fixture.nativeElement.querySelector('[data-sin-leer]').textContent).toContain('1 sin leer');
  });

  it('abrir una notificacion navega al detalle de solo lectura', () => {
    servicio.getMisNotificaciones.and.returnValue(of([notificacion('n-1', false)]));
    const fixture = TestBed.createComponent(DocNotificacionesContentComponent);
    fixture.detectChanges();
    const enlace = fixture.nativeElement.querySelector('[data-abrir]') as HTMLAnchorElement;
    expect(enlace.getAttribute('href')).toBe('/docente/notificaciones/n-1');
    enlace.click();
    expect(router.navigateByUrl).toHaveBeenCalledWith('/docente/notificaciones/n-1');
  });

  it('sin notificaciones lo dice y un error muestra reintentar', () => {
    servicio.getMisNotificaciones.and.returnValue(of([]));
    const fixture = TestBed.createComponent(DocNotificacionesContentComponent);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[data-estado="vacio"]')).toBeTruthy();
    servicio.getMisNotificaciones.and.returnValue(throwError(() => ({ status: 500 })));
    fixture.componentInstance.cargar();
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[data-estado="error"]')).toBeTruthy();
    expect(errores.handleError).toHaveBeenCalled();
  });
});
