import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { DocumentoAdicionalComponent } from './documento-adicional-component';
import { InputAnexoUploadComponent } from '../../inputs/input-anexo-upload-component/input-anexo-upload-component';

describe('DocumentoAdicionalComponent', () => {
  let fixture: ComponentFixture<DocumentoAdicionalComponent>;
  let componente: DocumentoAdicionalComponent;
  let emitidos: (File | null)[];
  const entrada = { reset: jasmine.createSpy('reset') } as unknown as InputAnexoUploadComponent;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [DocumentoAdicionalComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    fixture = TestBed.createComponent(DocumentoAdicionalComponent);
    componente = fixture.componentInstance;
    emitidos = [];
    componente.archivoChange.subscribe(archivo => emitidos.push(archivo));
    fixture.detectChanges();
  });

  it('es opcional, solo PDF y explica que va en el correo', () => {
    expect(componente.tipo.obligatoriedad).toBeFalse();
    expect(componente.tipo.formato).toBe('pdf');
    const texto = fixture.nativeElement.textContent;
    expect(texto).toContain('Documento adicional (opcional)');
    expect(texto).toContain('se enviará al estudiante por correo');
    expect(texto).not.toContain('Este anexo es obligatorio');
  });

  it('emite el PDF valido y rechaza otro formato o uno de mas de 5 MB', () => {
    const pdf = new File(['%PDF-'], 'acta.pdf');
    componente.alSeleccionar(pdf, entrada);
    expect(emitidos).toEqual([pdf]);
    expect(componente.error).toBe('');

    componente.alSeleccionar(new File(['x'], 'foto.png'), entrada);
    expect(emitidos[1]).toBeNull();
    expect(componente.error).toContain('pdf');
    expect(entrada.reset).toHaveBeenCalled();

    componente.alSeleccionar(new File([new Uint8Array(5 * 1024 * 1024 + 1)], 'grande.pdf'), entrada);
    expect(componente.error).toContain('5 MB');
  });

  it('quitar el archivo emite nulo y avisa si ya hay uno cargado', () => {
    componente.alQuitar();
    expect(emitidos).toEqual([null]);
    expect(fixture.nativeElement.querySelector('[data-documento-adicional-cargado]')).toBeNull();
    fixture.componentRef.setInput('yaCargado', true);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[data-documento-adicional-cargado]')).toBeTruthy();
  });
});
