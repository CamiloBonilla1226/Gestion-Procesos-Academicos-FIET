import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpErrorResponse, provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';
import { NotificacionResolucionComponent } from './notificacion-resolucion-component';
import { CancelacionAsignaturaService } from '../../../core/services/cancelacion-asignatura-service';
import { ErrorHandlerService } from '../../../core/services/error-handler-service';
import { ToastService } from '../../../core/services/toast-service';
import { NotificacionResolucionDTORespuesta } from '../../../core/models/CancelacionAsignatura/DTOResponse/NotificacionResolucionDTORespuesta';
import { SolicitudAcademicaDetalleDTORespuesta } from '../../../core/models/SolicitudAcademica/DTOResponse/SolicitudAcademicaDetalleDTORespuesta';
import { InputAnexoUploadComponent } from '../../inputs/input-anexo-upload-component/input-anexo-upload-component';

const SOLICITUD = {
  uuidSolicitudAcademica: 's-1', radicado: '2026-CA-0007', uuidTipoSolicitudAcademica: 't', tipoSolicitud: 'Cancelación de Asignatura',
  fechaCreacion: '2026-10-08T10:00:00', etiqueta: 'Respondida', etapaCodigo: 'APROBADA',
  estudiante: { uuidUsuario: 'e', nombres: 'Ana', apellidos: 'Muñoz', codigoEstudiantil: '1', programaAcademico: 'P', semestre: '1', correoElectronico: 'a@u.co' },
  anexos: [], tieneResolucion: true, puedeDescargarResolucion: true, accionesDisponibles: []
} as SolicitudAcademicaDetalleDTORespuesta;

function notificacion(cambios: Partial<NotificacionResolucionDTORespuesta> = {}): NotificacionResolucionDTORespuesta {
  return {
    estado: 'PROGRAMADA', fechaProgramada: '2099-01-01T08:30:00', firmada: false, numeroResolucion: '123-26', anioResolucion: 2026,
    sufijoResolucion: '/178', decision: 'aprueba la', mensajeInicial: 'Buen día', fechaEnvioEstudiante: null, fechaEnvioDarca: null,
    intentos: 0, ultimoError: null, fechaEnvio: null, fechaLiberacion: null, editable: true, ...cambios
  };
}

describe('NotificacionResolucionComponent', () => {
  let servicio: jasmine.SpyObj<CancelacionAsignaturaService>;
  let errores: jasmine.SpyObj<ErrorHandlerService>;
  let toast: jasmine.SpyObj<ToastService>;
  const entrada = { reset: jasmine.createSpy('reset') } as unknown as InputAnexoUploadComponent;

  beforeEach(() => {
    servicio = jasmine.createSpyObj<CancelacionAsignaturaService>('CancelacionAsignaturaService',
      ['getNotificacion', 'reprogramarNotificacion', 'registrarFirma', 'reintentarNotificacion']);
    errores = jasmine.createSpyObj<ErrorHandlerService>('ErrorHandlerService', ['handleError']);
    toast = jasmine.createSpyObj<ToastService>('ToastService', ['showSuccess', 'showError']);
    TestBed.configureTestingModule({
      imports: [NotificacionResolucionComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideNoopAnimations(),
        { provide: CancelacionAsignaturaService, useValue: servicio },
        { provide: ErrorHandlerService, useValue: errores },
        { provide: ToastService, useValue: toast }
      ]
    });
  });

  function crear(datos: NotificacionResolucionDTORespuesta | null): ComponentFixture<NotificacionResolucionComponent> {
    servicio.getNotificacion.and.returnValue(of(datos));
    const fixture = TestBed.createComponent(NotificacionResolucionComponent);
    fixture.componentRef.setInput('uuidSolicitud', 's-1');
    fixture.componentRef.setInput('solicitud', SOLICITUD);
    fixture.detectChanges();
    return fixture;
  }

  function acciones(fixture: ComponentFixture<NotificacionResolucionComponent>): string[] {
    return Array.from(fixture.nativeElement.querySelectorAll('[data-acciones-notificacion] [data-accion]'))
      .map((b: any) => b.getAttribute('data-accion'));
  }

  it('sin programacion no pinta nada', () => {
    const fixture = crear(null);
    expect(fixture.nativeElement.querySelector('[data-panel-notificacion]')).toBeNull();
    expect(servicio.getNotificacion).toHaveBeenCalledWith('s-1');
  });

  it('programada y antes de la fecha muestra los datos y deja reprogramar o registrar la firma', () => {
    const fixture = crear(notificacion());
    const panel = fixture.nativeElement.querySelector('[data-panel-notificacion]');
    expect(panel.querySelector('[data-estado]').textContent.trim()).toBe('Programada');
    expect(panel.querySelector('[data-resolucion]').textContent.trim()).toBe('No. 123-26/178 de 2026');
    expect(panel.querySelector('[data-fecha-programada]').textContent).toContain('1 de enero de 2099');
    expect(panel.querySelector('[data-mensaje-inicial]').textContent).toContain('Buen día');
    expect(acciones(fixture)).toEqual(['REPROGRAMAR', 'FIRMA']);
  });

  it('enviada es de solo lectura', () => {
    const fixture = crear(notificacion({ estado: 'ENVIADA', editable: false, fechaEnvioDarca: '2099-01-01T08:31:00', mensajeInicial: null }));
    expect(fixture.nativeElement.querySelector('[data-acciones-notificacion]')).toBeNull();
    expect(fixture.nativeElement.querySelector('[data-sin-mensaje]')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('[data-envio-darca]')).toBeTruthy();
  });

  it('fallida muestra el error y solo deja reintentar', () => {
    const fixture = crear(notificacion({ estado: 'FALLIDA', editable: false, intentos: 3, ultimoError: 'Correo a DARCA: smtp caído' }));
    expect(fixture.nativeElement.querySelector('[data-ultimo-error]').textContent).toContain('smtp caído');
    expect(acciones(fixture)).toEqual(['REINTENTAR']);
  });

  it('pasada la fecha no deja editar ni registrar la firma aunque se llame directo', () => {
    const fixture = crear(notificacion({ editable: false }));
    const componente = fixture.componentInstance;
    componente.abrir('reprogramar');
    componente.abrir('firma');
    componente.abrir('reintentar');
    expect(componente.dialogo).toBeNull();
    expect(acciones(fixture)).toEqual([]);
    expect(fixture.nativeElement.querySelector('[data-nota-en-envio]')).toBeTruthy();
  });

  it('reprogramar valida la fecha futura y envia la programacion', () => {
    servicio.reprogramarNotificacion.and.returnValue(of(notificacion({ fechaProgramada: '2099-02-01T09:00:00' })));
    const fixture = crear(notificacion());
    const componente = fixture.componentInstance;
    const cambio = jasmine.createSpy('cambio');
    componente.cambio.subscribe(cambio);
    componente.abrir('reprogramar');
    expect(componente.programacion).toEqual({ fecha: '2099-01-01T08:30', numero: '123-26', anio: '2026', decision: 'aprueba la' });
    componente.programacion.fecha = '2000-01-01T08:00';
    componente.confirmar();
    expect(componente.errores['fecha']).toContain('posteriores');
    expect(servicio.reprogramarNotificacion).not.toHaveBeenCalled();
    componente.programacion.fecha = '2099-02-01T09:00';
    componente.programacion.numero = '124-26';
    componente.confirmar();
    expect(servicio.reprogramarNotificacion).toHaveBeenCalledWith('s-1', {
      fechaProgramada: '2099-02-01T09:00', numeroResolucion: '124-26', anioResolucion: 2026, decision: 'aprueba la'
    });
    expect(componente.dialogo).toBeNull();
    expect(cambio).toHaveBeenCalled();
    expect(toast.showSuccess).toHaveBeenCalled();
  });

  it('la firma exige el PDF y lo envia', () => {
    servicio.registrarFirma.and.returnValue(of(notificacion({ firmada: true })));
    const fixture = crear(notificacion());
    const componente = fixture.componentInstance;
    componente.abrir('firma');
    componente.confirmar();
    expect(componente.errores['firma']).toContain('PDF');
    componente.alSeleccionarFirma(new File(['x'], 'firmada.png'), entrada);
    expect(componente.archivoFirma).toBeNull();
    const pdf = new File(['%PDF-'], 'firmada.pdf', { type: 'application/pdf' });
    componente.alSeleccionarFirma(pdf, entrada);
    componente.confirmar();
    expect(servicio.registrarFirma).toHaveBeenCalledWith('s-1', pdf);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[data-nota-firmada]')).toBeTruthy();
  });

  it('si el servidor rechaza la accion muestra el error y recarga el panel', () => {
    servicio.reintentarNotificacion.and.returnValue(throwError(() => new HttpErrorResponse({ status: 500, error: { codigoError: '4' } })));
    const fixture = crear(notificacion({ estado: 'FALLIDA', editable: false }));
    const componente = fixture.componentInstance;
    componente.abrir('reintentar');
    componente.confirmar();
    expect(errores.handleError).toHaveBeenCalledWith(jasmine.anything(), 'Error', 'No se pudo completar la acción');
    expect(servicio.getNotificacion).toHaveBeenCalledTimes(2);
  });
});
