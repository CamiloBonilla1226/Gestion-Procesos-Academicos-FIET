import { TestBed } from '@angular/core/testing';
import { Subject } from 'rxjs';
import { AnexosAcademicosComponent } from './anexos-academicos-component';
import { SolicitudAcademicaService } from '../../../core/services/solicitud-academica-service';
import { ErrorHandlerService } from '../../../core/services/error-handler-service';

describe('AnexosAcademicosComponent', () => {
  it('no repite la descarga mientras la primera sigue en curso y nombra el archivo con nombreArchivo', () => {
    const respuesta = new Subject<Blob>();
    const servicio = jasmine.createSpyObj<SolicitudAcademicaService>('SolicitudAcademicaService', ['descargarAnexo']);
    servicio.descargarAnexo.and.returnValue(respuesta);
    const nombres: string[] = [];
    spyOn(HTMLAnchorElement.prototype, 'click').and.callFake(function (this: HTMLAnchorElement) {
      nombres.push(this.download);
    });
    TestBed.configureTestingModule({
      imports: [AnexosAcademicosComponent],
      providers: [
        { provide: SolicitudAcademicaService, useValue: servicio },
        { provide: ErrorHandlerService, useValue: jasmine.createSpyObj('ErrorHandlerService', ['handleError']) }
      ]
    });
    const fixture = TestBed.createComponent(AnexosAcademicosComponent);
    fixture.componentRef.setInput('uuidSolicitud', 's-1');
    fixture.componentRef.setInput('anexos', [{
      uuidAnexoAcademico: 'a-1', nombreArchivo: 'paz y salvo.pdf', uuidTipoAnexoAcademico: null, tipoAnexo: null,
      tipoArchivo: 'application/pdf', tamanioBytes: 100, fechaSubida: '2026-10-08T10:00:00'
    }]);
    fixture.detectChanges();

    const boton = () => fixture.nativeElement.querySelector('[data-anexo] button') as HTMLButtonElement;
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
});
