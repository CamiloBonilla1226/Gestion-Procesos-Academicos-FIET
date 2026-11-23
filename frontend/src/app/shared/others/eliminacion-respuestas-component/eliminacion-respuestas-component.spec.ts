import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';
import { EliminacionRespuestasComponent, erroresDeRango, textoFechaCorta } from './eliminacion-respuestas-component';
import { SolicitudAcademicaService } from '../../../core/services/solicitud-academica-service';
import { ErrorHandlerService } from '../../../core/services/error-handler-service';
import { ToastService } from '../../../core/services/toast-service';

describe('EliminacionRespuestasComponent', () => {
  let servicio: jasmine.SpyObj<SolicitudAcademicaService>;
  let errores: jasmine.SpyObj<ErrorHandlerService>;
  let toast: jasmine.SpyObj<ToastService>;

  beforeEach(() => {
    servicio = jasmine.createSpyObj<SolicitudAcademicaService>('SolicitudAcademicaService', ['contarRespuestasEliminables', 'eliminarRespuestas']);
    errores = jasmine.createSpyObj<ErrorHandlerService>('ErrorHandlerService', ['handleError']);
    toast = jasmine.createSpyObj<ToastService>('ToastService', ['showSuccess', 'showInfo', 'showError']);
    TestBed.configureTestingModule({
      imports: [EliminacionRespuestasComponent],
      providers: [
        provideNoopAnimations(),
        { provide: SolicitudAcademicaService, useValue: servicio },
        { provide: ErrorHandlerService, useValue: errores },
        { provide: ToastService, useValue: toast }
      ]
    });
  });

  function crear(): ComponentFixture<EliminacionRespuestasComponent> {
    const fixture = TestBed.createComponent(EliminacionRespuestasComponent);
    fixture.detectChanges();
    return fixture;
  }

  it('valida que las dos fechas existan, no sean futuras y esten en orden', () => {
    expect(Object.keys(erroresDeRango('', '', '2026-10-21')).sort()).toEqual(['desde', 'hasta']);
    expect(erroresDeRango('2026-10-22', '2026-10-21', '2026-10-21')['desde']).toContain('posterior a hoy');
    expect(erroresDeRango('2026-10-01', '2026-10-22', '2026-10-21')['hasta']).toContain('posterior a hoy');
    expect(erroresDeRango('2026-10-10', '2026-10-01', '2026-10-21')['hasta']).toContain('igual o posterior');
    expect(erroresDeRango('2026-10-01', '2026-10-01', '2026-10-21')).toEqual({});
    expect(textoFechaCorta('2026-10-01')).toBe('1 de octubre de 2026');
  });

  it('con datos invalidos no consulta al servidor', () => {
    const componente = crear().componentInstance;
    componente.revisar();
    expect(componente.errores['desde']).toBeTruthy();
    expect(servicio.contarRespuestasEliminables).not.toHaveBeenCalled();
  });

  it('pide el conteo, confirma con el mensaje y al aceptar elimina y avisa', () => {
    servicio.contarRespuestasEliminables.and.returnValue(of({ cantidad: 3, desde: '2026-10-01', hasta: '2026-10-05' }));
    servicio.eliminarRespuestas.and.returnValue(of({ cantidad: 3, desde: '2026-10-01', hasta: '2026-10-05' }));
    const fixture = crear();
    const componente = fixture.componentInstance;
    const eliminadas = jasmine.createSpy('eliminadas');
    componente.eliminadas.subscribe(eliminadas);
    componente.desde = '2026-10-01';
    componente.hasta = '2026-10-05';
    componente.revisar();
    fixture.detectChanges();
    expect(servicio.contarRespuestasEliminables).toHaveBeenCalledWith('2026-10-01', '2026-10-05');
    expect(componente.dialogoVisible).toBeTrue();
    expect(componente.mensajeConfirmacion).toBe(
      'Se eliminarán 3 respuestas entre 1 de octubre de 2026 y 5 de octubre de 2026. Esta acción no se puede deshacer. ¿Seguro?');
    componente.desde = '2026-01-01';
    componente.confirmar();
    expect(servicio.eliminarRespuestas).toHaveBeenCalledWith('2026-10-01', '2026-10-05');
    expect(componente.dialogoVisible).toBeFalse();
    expect(toast.showSuccess).toHaveBeenCalledWith('Listo', 'Se eliminaron 3 respuestas.');
    expect(eliminadas).toHaveBeenCalledWith(3);
  });

  it('cancelar cierra el dialogo sin eliminar', () => {
    servicio.contarRespuestasEliminables.and.returnValue(of({ cantidad: 1, desde: '2026-10-01', hasta: '2026-10-01' }));
    const componente = crear().componentInstance;
    componente.desde = '2026-10-01';
    componente.hasta = '2026-10-01';
    componente.revisar();
    expect(componente.mensajeConfirmacion).toContain('Se eliminará 1 respuesta');
    componente.cerrar(false);
    expect(componente.dialogoVisible).toBeFalse();
    expect(servicio.eliminarRespuestas).not.toHaveBeenCalled();
  });

  it('sin respuestas en el rango avisa y no abre la confirmacion', () => {
    servicio.contarRespuestasEliminables.and.returnValue(of({ cantidad: 0, desde: '2026-10-01', hasta: '2026-10-02' }));
    const componente = crear().componentInstance;
    componente.desde = '2026-10-01';
    componente.hasta = '2026-10-02';
    componente.revisar();
    expect(componente.dialogoVisible).toBeFalse();
    expect(toast.showInfo).toHaveBeenCalled();
  });

  it('un error del servidor se muestra y no cierra el dialogo', () => {
    servicio.contarRespuestasEliminables.and.returnValue(of({ cantidad: 2, desde: '2026-10-01', hasta: '2026-10-02' }));
    servicio.eliminarRespuestas.and.returnValue(throwError(() => new HttpErrorResponse({ status: 500 })));
    const componente = crear().componentInstance;
    componente.desde = '2026-10-01';
    componente.hasta = '2026-10-02';
    componente.revisar();
    componente.confirmar();
    expect(errores.handleError).toHaveBeenCalledWith(jasmine.anything(), 'Error', 'No se pudieron eliminar las respuestas');
    expect(componente.dialogoVisible).toBeTrue();
  });
});
