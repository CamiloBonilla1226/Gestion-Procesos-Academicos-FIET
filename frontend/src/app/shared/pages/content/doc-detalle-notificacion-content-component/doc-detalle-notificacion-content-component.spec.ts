import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router, convertToParamMap } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { of, throwError } from 'rxjs';
import { DocDetalleNotificacionContentComponent } from './doc-detalle-notificacion-content-component';
import { NotificacionDocenteService } from '../../../../core/services/notificacion-docente-service';
import { ErrorHandlerService } from '../../../../core/services/error-handler-service';
import { NotificacionDocenteDetalleDTORespuesta } from '../../../../core/models/NotificacionDocente/DTOResponse/NotificacionDocenteDetalleDTORespuesta';

const DETALLE: NotificacionDocenteDetalleDTORespuesta = {
  notificacion: {
    uuidNotificacion: 'n-1', titulo: 'Examen supletorio aprobado, radicado 2026-ES-0001', uuidSolicitudAcademica: 's-1',
    radicado: '2026-ES-0001', estudiante: 'María Pérez', asignatura: 'ISI-001 - Cálculo I', resultado: 'Aprobada',
    fechaCreacion: '2026-10-08T10:00:00', fechaLectura: '2026-10-09T08:00:00', leida: true
  },
  tipoSolicitud: 'Examen Supletorio',
  fechaRadicacion: '2026-10-06T09:00:00',
  nombresEstudiante: 'María',
  apellidosEstudiante: 'Pérez',
  codigoEstudiantil: '1047',
  programaAcademico: 'Ingeniería de Sistemas',
  asignatura: { uuidAsignaturaMatriculada: 'am-1', codigoAsignatura: 'ISI-001', nombreAsignatura: 'Cálculo I', grupo: 'A' },
  tipoCausa: 'otra',
  fechaExamenNoPresentado: '2026-10-05',
  cruce: null,
  anexos: [
    { uuidAnexoAcademico: 'a-1', tipoAnexo: 'Formato firmado por el docente que orienta la asignatura', nombreArchivo: 'formato.pdf', tipoArchivo: 'application/pdf', fechaSubida: '2026-10-06T09:00:00' },
    { uuidAnexoAcademico: 'a-2', tipoAnexo: 'Comprobante de pago', nombreArchivo: 'comprobante.pdf', tipoArchivo: 'application/pdf', fechaSubida: '2026-10-07T09:00:00' }
  ]
};

describe('DocDetalleNotificacionContentComponent', () => {
  let servicio: jasmine.SpyObj<NotificacionDocenteService>;
  let errores: jasmine.SpyObj<ErrorHandlerService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    servicio = jasmine.createSpyObj<NotificacionDocenteService>('NotificacionDocenteService', ['marcarLeida', 'descargarAnexo']);
    errores = jasmine.createSpyObj<ErrorHandlerService>('ErrorHandlerService', ['handleError']);
    router = jasmine.createSpyObj<Router>('Router', ['navigateByUrl']);
    TestBed.configureTestingModule({
      imports: [DocDetalleNotificacionContentComponent],
      providers: [
        { provide: NotificacionDocenteService, useValue: servicio },
        { provide: ErrorHandlerService, useValue: errores },
        { provide: Router, useValue: router },
        { provide: ActivatedRoute, useValue: { paramMap: of(convertToParamMap({ uuid: 'n-1' })) } }
      ]
    });
  });

  it('al abrir marca la notificacion como leida y muestra la solicitud completa de solo lectura', () => {
    servicio.marcarLeida.and.returnValue(of(DETALLE));
    const fixture = TestBed.createComponent(DocDetalleNotificacionContentComponent);
    fixture.detectChanges();
    expect(servicio.marcarLeida).toHaveBeenCalledWith('n-1');
    const texto = fixture.nativeElement.textContent;
    expect(texto).toContain('2026-ES-0001');
    expect(fixture.nativeElement.querySelector('[data-resultado]').textContent).toContain('Aprobada');
    expect(fixture.nativeElement.querySelector('[data-estudiante]').textContent).toContain('María Pérez');
    expect(fixture.nativeElement.querySelector('[data-asignatura]').textContent).toContain('ISI-001 - Cálculo I (grupo A)');
    expect(fixture.nativeElement.querySelector('[data-causa]').textContent).toContain('Otra causa');
    expect(texto).toContain('05/10/2026');
    expect(fixture.nativeElement.querySelectorAll('[data-anexo]').length).toBe(2);
    expect(fixture.nativeElement.querySelector('textarea, input, select')).toBeNull();
    expect(texto).not.toContain('Observación');
  });

  it('descarga un anexo con su nombre', () => {
    servicio.marcarLeida.and.returnValue(of(DETALLE));
    servicio.descargarAnexo.and.returnValue(of(new Blob(['%PDF'])));
    const fixture = TestBed.createComponent(DocDetalleNotificacionContentComponent);
    fixture.detectChanges();
    spyOn(URL, 'createObjectURL').and.returnValue('blob:x');
    spyOn(URL, 'revokeObjectURL');
    spyOn(HTMLAnchorElement.prototype, 'click');
    (fixture.nativeElement.querySelector('[data-anexo="a-2"] button') as HTMLButtonElement).click();
    expect(servicio.descargarAnexo).toHaveBeenCalledWith('n-1', 'a-2');
  });

  it('una notificacion ajena o inexistente se ve como no encontrada', () => {
    servicio.marcarLeida.and.returnValue(throwError(() => new HttpErrorResponse({ status: 500, error: { codigoError: 3, mensaje: 'no existe' } })));
    const fixture = TestBed.createComponent(DocDetalleNotificacionContentComponent);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[data-estado="no-encontrada"]')).toBeTruthy();
    expect(errores.handleError).not.toHaveBeenCalled();
  });

  it('otro error muestra reintentar y Volver regresa a la lista', () => {
    servicio.marcarLeida.and.returnValue(throwError(() => new HttpErrorResponse({ status: 500, error: { codigoError: 1 } })));
    const fixture = TestBed.createComponent(DocDetalleNotificacionContentComponent);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[data-estado="error"]')).toBeTruthy();
    expect(errores.handleError).toHaveBeenCalled();
    fixture.componentInstance.volver(new Event('click'));
    expect(router.navigateByUrl).toHaveBeenCalledWith('/docente/notificaciones');
  });
});
