import { TestBed } from '@angular/core/testing';
import { Subject, of } from 'rxjs';
import { AnexosAcademicosComponent } from './anexos-academicos-component';
import { SolicitudAcademicaService } from '../../../core/services/solicitud-academica-service';
import { ErrorHandlerService } from '../../../core/services/error-handler-service';

describe('AnexosAcademicosComponent', () => {
  const anexo = {
    uuidAnexoAcademico: 'a-1', nombreArchivo: 'paz y salvo.pdf', uuidTipoAnexoAcademico: null, tipoAnexo: null,
    tipoArchivo: 'application/pdf', tamanioBytes: 100, fechaSubida: '2026-10-08T10:00:00'
  };
  let servicio: jasmine.SpyObj<SolicitudAcademicaService>;

  function crear() {
    servicio = jasmine.createSpyObj<SolicitudAcademicaService>('SolicitudAcademicaService', ['descargarAnexo']);
    TestBed.configureTestingModule({
      imports: [AnexosAcademicosComponent],
      providers: [
        { provide: SolicitudAcademicaService, useValue: servicio },
        { provide: ErrorHandlerService, useValue: jasmine.createSpyObj('ErrorHandlerService', ['handleError']) }
      ]
    });
    const fixture = TestBed.createComponent(AnexosAcademicosComponent);
    fixture.componentRef.setInput('uuidSolicitud', 's-1');
    fixture.componentRef.setInput('anexos', [anexo]);
    fixture.detectChanges();
    return fixture;
  }

  it('cada anexo ofrece Visualizar y Descargar', () => {
    const fixture = crear();
    const botones = Array.from(fixture.nativeElement.querySelectorAll('[data-anexo] button')).map((b: any) => b.textContent.trim());
    expect(botones).toEqual(['Visualizar', 'Descargar']);
  });

  it('no repite la descarga mientras la primera sigue en curso y nombra el archivo con nombreArchivo', () => {
    const fixture = crear();
    const respuesta = new Subject<Blob>();
    servicio.descargarAnexo.and.returnValue(respuesta);
    const nombres: string[] = [];
    spyOn(HTMLAnchorElement.prototype, 'click').and.callFake(function (this: HTMLAnchorElement) {
      nombres.push(this.download);
    });

    const boton = () => fixture.nativeElement.querySelector('[data-descargar]') as HTMLButtonElement;
    boton().click();
    fixture.detectChanges();
    expect(boton().disabled).toBeTrue();
    expect(boton().textContent).toContain('Descargando...');
    fixture.componentInstance.descargar(fixture.componentInstance.anexos[0]);
    expect(servicio.descargarAnexo).toHaveBeenCalledOnceWith('s-1', 'a-1');

    respuesta.next(new Blob(['%PDF-']));
    respuesta.complete();
    fixture.detectChanges();
    expect(nombres).toEqual(['paz y salvo.pdf']);
    expect(boton().disabled).toBeFalse();
  });

  it('Visualizar abre el archivo en una pestaña nueva', () => {
    const fixture = crear();
    servicio.descargarAnexo.and.returnValue(of(new Blob(['%PDF-'])));
    const ventana = { location: { href: '' }, close: jasmine.createSpy('close') };
    spyOn(window, 'open').and.returnValue(ventana as unknown as Window);

    (fixture.nativeElement.querySelector('[data-visualizar]') as HTMLButtonElement).click();

    expect(window.open).toHaveBeenCalledWith('', '_blank');
    expect(ventana.location.href).toMatch(/^blob:/);
  });

  it('si el navegador bloquea la pestaña descarga el archivo', () => {
    const fixture = crear();
    servicio.descargarAnexo.and.returnValue(of(new Blob(['%PDF-'])));
    spyOn(window, 'open').and.returnValue(null);
    const nombres: string[] = [];
    spyOn(HTMLAnchorElement.prototype, 'click').and.callFake(function (this: HTMLAnchorElement) {
      nombres.push(this.download);
    });

    (fixture.nativeElement.querySelector('[data-visualizar]') as HTMLButtonElement).click();

    expect(nombres).toEqual(['paz y salvo.pdf']);
  });
});
