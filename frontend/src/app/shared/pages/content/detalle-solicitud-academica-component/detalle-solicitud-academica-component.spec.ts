import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router, convertToParamMap } from '@angular/router';
import { HttpErrorResponse, provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';
import { DetalleSolicitudAcademicaComponent } from './detalle-solicitud-academica-component';
import { COMPONENTES_PROCESO } from './componentes-proceso';
import { SolicitudAcademicaService } from '../../../../core/services/solicitud-academica-service';
import { ErrorHandlerService } from '../../../../core/services/error-handler-service';
import { SolicitudAcademicaDetalleDTORespuesta } from '../../../../core/models/SolicitudAcademica/DTOResponse/SolicitudAcademicaDetalleDTORespuesta';
import { HistorialSolicitudAcademicaDTORespuesta } from '../../../../core/models/SolicitudAcademica/DTOResponse/HistorialSolicitudAcademicaDTORespuesta';

function detalle(tieneResolucion: boolean, puedeDescargarResolucion: boolean): SolicitudAcademicaDetalleDTORespuesta {
  return {
    uuidSolicitudAcademica: 'u-1',
    radicado: '2026-CM-0007',
    uuidTipoSolicitudAcademica: 't',
    tipoSolicitud: 'Cancelación de Matrícula',
    fechaCreacion: '2026-10-08T00:00:00',
    etiqueta: 'Aprobada',
    etapaCodigo: 'APROBADA',
    estudiante: {
      uuidUsuario: 'e-1',
      nombres: 'María',
      apellidos: 'Pérez',
      codigoEstudiantil: '104618021',
      programaAcademico: 'Ingeniería de Sistemas',
      semestre: '5',
      correoElectronico: 'maria@unicauca.edu.co'
    },
    anexos: [
      {
        uuidAnexoAcademico: 'a-1', nombreArchivo: 'bibliotecas.pdf', uuidTipoAnexoAcademico: 'ta-1',
        tipoAnexo: 'Paz y salvo - División de Bibliotecas', tipoArchivo: 'application/pdf', tamanioBytes: 250880,
        fechaSubida: '2026-10-08T00:00:01'
      },
      {
        uuidAnexoAcademico: 'a-2', nombreArchivo: 'incapacidad.png', uuidTipoAnexoAcademico: null,
        tipoAnexo: null, tipoArchivo: 'image/png', tamanioBytes: 1572864, fechaSubida: '2026-10-08T00:00:02'
      }
    ],
    tieneResolucion,
    puedeDescargarResolucion,
    accionesDisponibles: []
  };
}

const historial: HistorialSolicitudAcademicaDTORespuesta[] = [
  { accion: 'ENVIAR_RESPUESTA', etapaCodigo: 'APROBADA', observaciones: null, fecha: '2026-10-10T08:00:00', nombresUsuario: 'Ana', apellidosUsuario: 'Ruiz' },
  { accion: 'RADICAR', etapaCodigo: 'RADICADA', observaciones: null, fecha: '2026-10-08T00:00:00', nombresUsuario: 'María', apellidosUsuario: 'Pérez' },
  { accion: 'REMITIR_DECANO', etapaCodigo: null, observaciones: 'Cumple los requisitos', fecha: '2026-10-09T12:30:00', nombresUsuario: 'Ana', apellidosUsuario: 'Ruiz' }
];

describe('DetalleSolicitudAcademicaComponent', () => {
  let servicio: jasmine.SpyObj<SolicitudAcademicaService>;
  let errores: jasmine.SpyObj<ErrorHandlerService>;
  let http: HttpTestingController;

  beforeEach(() => {
    servicio = jasmine.createSpyObj<SolicitudAcademicaService>('SolicitudAcademicaService', [
      'getSolicitud', 'getHistorial', 'descargarAnexo', 'descargarResolucion'
    ]);
    errores = jasmine.createSpyObj<ErrorHandlerService>('ErrorHandlerService', ['handleError']);
    TestBed.configureTestingModule({
      imports: [DetalleSolicitudAcademicaComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideNoopAnimations(),
        { provide: SolicitudAcademicaService, useValue: servicio },
        { provide: ErrorHandlerService, useValue: errores },
        { provide: Router, useValue: jasmine.createSpyObj<Router>('Router', ['navigate']) },
        { provide: ActivatedRoute, useValue: { paramMap: of(convertToParamMap({ uuid: 'u-1' })) } }
      ]
    });
    http = TestBed.inject(HttpTestingController);
  });

  function crear(datos: SolicitudAcademicaDetalleDTORespuesta, rol = 'ESTUDIANTE'): ComponentFixture<DetalleSolicitudAcademicaComponent> {
    servicio.getSolicitud.and.returnValue(of(datos));
    servicio.getHistorial.and.returnValue(of(historial));
    const fixture = TestBed.createComponent(DetalleSolicitudAcademicaComponent);
    fixture.componentRef.setInput('rol', rol);
    fixture.detectChanges();
    return fixture;
  }

  function texto(fixture: ComponentFixture<DetalleSolicitudAcademicaComponent>, selector: string): string[] {
    return Array.from(fixture.nativeElement.querySelectorAll(selector)).map((e: any) => e.textContent.replace(/\s+/g, ' ').trim());
  }

  it('lee el uuid de la ruta y pinta datos generales, estudiante y estado', () => {
    const fixture = crear(detalle(false, false));
    expect(servicio.getSolicitud).toHaveBeenCalledWith('u-1');
    expect(servicio.getHistorial).toHaveBeenCalledWith('u-1');
    const contenido = fixture.nativeElement.textContent;
    expect(contenido).toContain('2026-CM-0007');
    expect(contenido).toContain('08/10/2026 00:00');
    expect(contenido).toContain('Ingeniería de Sistemas');
    const estado = fixture.nativeElement.querySelector('[data-estado-solicitud]');
    expect(estado.textContent.trim()).toBe('Aprobada');
    expect(estado.classList).toContain('text-bg-success');
  });

  it('pinta los anexos con tipo, Soporte para los libres y tamaño legible', () => {
    const anexos = texto(crear(detalle(false, false)), '[data-anexo]');
    expect(anexos.length).toBe(2);
    expect(anexos[0]).toContain('bibliotecas.pdf');
    expect(anexos[0]).toContain('Paz y salvo - División de Bibliotecas');
    expect(anexos[0]).toContain('245,0 KB');
    expect(anexos[1]).toContain('Soporte');
    expect(anexos[1]).toContain('1,5 MB');
  });

  it('pinta el historial de la mas antigua a la mas reciente con textos legibles', () => {
    const entradas = texto(crear(detalle(false, false)), '[data-historial]');
    expect(entradas.length).toBe(3);
    expect(entradas[0]).toContain('Radicación de la solicitud');
    expect(entradas[0]).toContain('María Pérez');
    expect(entradas[1]).toContain('Remisión al Decano');
    expect(entradas[1]).toContain('Cumple los requisitos');
    expect(entradas[1]).toContain('Etapa no determinada');
    expect(entradas[2]).toContain('Envío de la respuesta al estudiante');
    expect(entradas[2]).toContain('10/10/2026 08:00');
  });

  it('muestra el boton de Resolucion solo si puede descargarla', () => {
    expect(crear(detalle(true, true)).nativeElement.querySelector('[data-boton-resolucion]')).toBeTruthy();
  });

  it('no muestra el boton si hay Resolucion pero no puede descargarla', () => {
    expect(crear(detalle(true, false)).nativeElement.querySelector('[data-boton-resolucion]')).toBeNull();
  });

  it('no muestra el boton si no hay Resolucion', () => {
    expect(crear(detalle(false, false)).nativeElement.querySelector('[data-boton-resolucion]')).toBeNull();
  });

  it('descarga la Resolucion con el nombre Resolucion-<radicado>.pdf', () => {
    const nombres: string[] = [];
    spyOn(HTMLAnchorElement.prototype, 'click').and.callFake(function (this: HTMLAnchorElement) {
      nombres.push(this.download);
    });
    servicio.descargarResolucion.and.returnValue(of(new Blob(['%PDF-'])));
    const fixture = crear(detalle(true, true));
    fixture.nativeElement.querySelector('[data-boton-resolucion] button').click();
    expect(servicio.descargarResolucion).toHaveBeenCalledWith('u-1');
    expect(nombres).toEqual(['Resolucion-2026-CM-0007.pdf']);
  });

  it('con codigo 3 muestra Solicitud no encontrada sin pasar por el ErrorHandlerService', () => {
    const error = new HttpErrorResponse({ status: 500, error: { codigoError: '3', mensaje: 'no fue encontrado...' } });
    servicio.getSolicitud.and.returnValue(throwError(() => error));
    servicio.getHistorial.and.returnValue(throwError(() => error));
    const fixture = TestBed.createComponent(DetalleSolicitudAcademicaComponent);
    fixture.componentRef.setInput('rol', 'FUNCIONARIO');
    fixture.detectChanges();
    const aviso = fixture.nativeElement.querySelector('[data-estado="no-encontrada"]');
    expect(aviso.textContent).toContain('Solicitud no encontrada');
    expect(aviso.querySelector('a').getAttribute('href')).toBe('/funcionario-academico/solicitudes');
    expect(errores.handleError).not.toHaveBeenCalled();
  });

  it('otros errores van al ErrorHandlerService', () => {
    servicio.getSolicitud.and.returnValue(throwError(() => new HttpErrorResponse({ status: 0 })));
    servicio.getHistorial.and.returnValue(of([]));
    const fixture = TestBed.createComponent(DetalleSolicitudAcademicaComponent);
    fixture.componentRef.setInput('rol', 'DECANO');
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[data-estado="error"]')).toBeTruthy();
    expect(errores.handleError).toHaveBeenCalledTimes(1);
  });

  it('el bloque del proceso queda vacio si el proceso no tiene componente registrado', async () => {
    const fixture = crear({ ...detalle(false, false), tipoSolicitud: 'Examen Supletorio' });
    await fixture.whenStable();
    fixture.detectChanges();
    expect(fixture.componentInstance.proceso).toBe('examenes-supletorios');
    expect(fixture.nativeElement.querySelector('[data-bloque-proceso]')).toBeNull();
  });

  it('inserta el componente de Cancelacion de Matricula en el bloque del proceso', async () => {
    const fixture = crear(detalle(false, false), 'FUNCIONARIO');
    await COMPONENTES_PROCESO['cancelaciones-matricula']!();
    await new Promise(resolver => setTimeout(resolver));
    fixture.detectChanges();
    expect(fixture.componentInstance.proceso).toBe('cancelaciones-matricula');
    expect(fixture.nativeElement.querySelector('[data-bloque-proceso] app-cancelacion-matricula-proceso-component')).toBeTruthy();
    http.expectOne(req => req.url.endsWith('/cancelaciones-matricula/u-1'));
  });

  it('inserta el componente de Cancelacion de Asignatura en el bloque del proceso', async () => {
    const fixture = crear({ ...detalle(false, false), tipoSolicitud: 'Cancelación de Asignatura' }, 'DECANO');
    await COMPONENTES_PROCESO['cancelaciones-asignatura']!();
    await new Promise(resolver => setTimeout(resolver));
    fixture.detectChanges();
    expect(fixture.componentInstance.proceso).toBe('cancelaciones-asignatura');
    expect(fixture.nativeElement.querySelector('[data-bloque-proceso] app-cancelacion-asignatura-proceso-component')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('[data-bloque-proceso] app-cancelacion-matricula-proceso-component')).toBeNull();
    http.expectOne(req => req.url.endsWith('/cancelaciones-asignatura/u-1'));
  });

  it('inserta el componente de Examen Supletorio en el bloque del proceso', async () => {
    const fixture = crear({ ...detalle(false, false), tipoSolicitud: 'Examen Supletorio' }, 'ESTUDIANTE');
    await COMPONENTES_PROCESO['examenes-supletorios']!();
    await new Promise(resolver => setTimeout(resolver));
    fixture.detectChanges();
    expect(fixture.componentInstance.proceso).toBe('examenes-supletorios');
    expect(fixture.nativeElement.querySelector('[data-bloque-proceso] app-examen-supletorio-proceso-component')).toBeTruthy();
    http.expectOne(req => req.url.endsWith('/examenes-supletorios/u-1'));
  });

  it('lista el recibo y el comprobante de pago con su descarga', () => {
    const datos = detalle(false, false);
    const fixture = crear({
      ...datos,
      tipoSolicitud: 'Examen Supletorio',
      etapaCodigo: 'EN_VERIFICACION_PAGO',
      anexos: [
        { uuidAnexoAcademico: 'a-r', nombreArchivo: 'recibo.pdf', uuidTipoAnexoAcademico: 'tr', tipoAnexo: 'Recibo de pago',
          tipoArchivo: 'application/pdf', tamanioBytes: 1024, fechaSubida: '2026-10-09T08:00:00' },
        { uuidAnexoAcademico: 'a-c', nombreArchivo: 'pago.png', uuidTipoAnexoAcademico: 'tc', tipoAnexo: 'Comprobante de pago',
          tipoArchivo: 'image/png', tamanioBytes: 2048, fechaSubida: '2026-10-09T09:00:00' }
      ]
    });
    const anexos = texto(fixture, '[data-anexo]');
    expect(anexos[0]).toContain('Recibo de pago');
    expect(anexos[1]).toContain('Comprobante de pago');
    expect(fixture.nativeElement.querySelectorAll('[data-anexo] app-simple-button-component').length).toBe(2);
  });
});
