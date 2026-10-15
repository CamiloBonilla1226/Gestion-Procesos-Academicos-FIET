import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { CatalogoAcademicoService } from '../services/catalogo-academico-service';
import { SolicitudAcademicaService } from '../services/solicitud-academica-service';
import { admiteDocumentoAdicional, esDocumentoAdicional, subirDocumentoAdicional, tieneDocumentoAdicional } from './documento-adicional';
import { environment } from '../../../enviroments/environment';

describe('documento adicional', () => {
  let http: HttpTestingController;
  let catalogo: CatalogoAcademicoService;
  let solicitudes: SolicitudAcademicaService;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    http = TestBed.inject(HttpTestingController);
    catalogo = TestBed.inject(CatalogoAcademicoService);
    solicitudes = TestBed.inject(SolicitudAcademicaService);
  });

  afterEach(() => http.verify());

  it('reconoce el tipo por nombre sin importar mayusculas ni espacios', () => {
    expect(esDocumentoAdicional(' documento adicional de la respuesta ')).toBeTrue();
    expect(esDocumentoAdicional('Recibo de pago')).toBeFalse();
    expect(esDocumentoAdicional(null)).toBeFalse();
    expect(tieneDocumentoAdicional([{ tipoAnexo: 'Documento adicional de la respuesta' } as never])).toBeTrue();
    expect(tieneDocumentoAdicional([])).toBeFalse();
  });

  it('solo las acciones que cierran la solicitud admiten el documento', () => {
    for (const accion of ['RECHAZAR_FUNCIONARIO', 'ENVIAR_RESPUESTA', 'APROBAR_COMPROBANTE', 'RECHAZAR_COMPROBANTE'] as const)
      expect(admiteDocumentoAdicional(accion)).withContext(accion).toBeTrue();
    for (const accion of ['REMITIR_DECANO', 'APROBAR_DECANO', 'RECHAZAR_DECANO', 'ENVIAR_RECIBO', 'SUBIR_COMPROBANTE'] as const)
      expect(admiteDocumentoAdicional(accion)).withContext(accion).toBeFalse();
    expect(admiteDocumentoAdicional(null)).toBeFalse();
  });

  it('sin archivo no hace ninguna peticion', () => {
    let terminado = false;
    subirDocumentoAdicional(catalogo, solicitudes, 't', 's-1', null).subscribe(() => (terminado = true));
    expect(terminado).toBeTrue();
  });

  it('busca el tipo en el catalogo del proceso y sube el archivo como anexo de ese tipo', () => {
    const archivo = new File(['%PDF-'], 'acta.pdf');
    let terminado = false;
    subirDocumentoAdicional(catalogo, solicitudes, 't', 's-1', archivo).subscribe(() => (terminado = true));
    http.expectOne(`${environment.apiUrl}/catalogos-academicos/tipos-solicitud/t/tipos-anexo`).flush([
      { uuidTipoAnexoAcademico: 'u-adicional', uuidTipoSolicitudAcademica: 't', nombre: 'Documento adicional de la respuesta', formatosPermitidos: 'pdf', obligatorio: false }
    ]);
    const subida = http.expectOne(`${environment.apiUrl}/solicitudes-academicas/s-1/anexos`);
    expect(subida.request.method).toBe('POST');
    expect((subida.request.body as FormData).get('tipoAnexo')).toBe('u-adicional');
    subida.flush({});
    expect(terminado).toBeTrue();
  });

  it('si el catalogo no tiene el tipo falla sin subir nada', () => {
    let error: unknown = null;
    subirDocumentoAdicional(catalogo, solicitudes, 't', 's-1', new File(['%PDF-'], 'acta.pdf')).subscribe({ error: e => (error = e) });
    http.expectOne(`${environment.apiUrl}/catalogos-academicos/tipos-solicitud/t/tipos-anexo`).flush([]);
    expect(error).toEqual({ message: 'El catálogo no tiene el tipo de anexo Documento adicional de la respuesta' });
  });
});
