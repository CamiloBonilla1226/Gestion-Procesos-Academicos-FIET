import { HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { firstValueFrom, of, throwError } from 'rxjs';
import { parsearErrorBlob } from './descargas';
import { leerCodigoError } from './errores-http';

function fallar(error: unknown): Promise<any> {
  return firstValueFrom(throwError(() => error).pipe(parsearErrorBlob())).then(
    () => fail('se esperaba un error'),
    err => err
  );
}

describe('parsearErrorBlob', () => {
  it('convierte el Blob del error en el JSON del backend', async () => {
    const cuerpo = { codigoError: '4', mensaje: 'La Resolución de la solicitud 2026-CM-0001 estará disponible cuando la solicitud termine...' };
    const original = new HttpErrorResponse({
      error: new Blob([JSON.stringify(cuerpo)], { type: 'application/json' }),
      headers: new HttpHeaders({ 'Content-Type': 'application/json' }),
      status: 500,
      statusText: 'Internal Server Error',
      url: 'http://localhost:8080/api/unicauca/fiet/consejo/solicitudes-academicas/x/resolucion'
    });

    const error = await fallar(original);

    expect(error instanceof HttpErrorResponse).toBeTrue();
    expect(error.status).toBe(500);
    expect(error.url).toBe(original.url);
    expect(error.headers.get('Content-Type')).toBe('application/json');
    expect(error.error).toEqual(cuerpo);
    expect(leerCodigoError(error)).toBe(4);
  });

  it('deja el texto plano cuando el Blob no es JSON', async () => {
    const original = new HttpErrorResponse({ error: new Blob(['Bad Gateway']), status: 502 });
    const error = await fallar(original);
    expect(error.error).toBe('Bad Gateway');
    expect(leerCodigoError(error)).toBeUndefined();
  });

  it('deja el cuerpo en null cuando el Blob viene vacio', async () => {
    const error = await fallar(new HttpErrorResponse({ error: new Blob([]), status: 401 }));
    expect(error.error).toBeNull();
  });

  it('relanza sin cambios un error que no trae Blob', async () => {
    const original = new HttpErrorResponse({ error: { codigoError: '3', mensaje: 'no existe' }, status: 500 });
    const error = await fallar(original);
    expect(error).toBe(original);
  });

  it('no altera una descarga exitosa', async () => {
    const blob = new Blob(['%PDF-1.4']);
    const resultado = await firstValueFrom(of(blob).pipe(parsearErrorBlob()));
    expect(resultado).toBe(blob);
  });
});

describe('leerCodigoError', () => {
  it('lee el codigo como numero sin importar si llega como texto', () => {
    expect(leerCodigoError({ error: { codigoError: '6', mensaje: 'x' } })).toBe(6);
    expect(leerCodigoError({ error: { codigoError: 2 } })).toBe(2);
  });

  it('devuelve undefined sin lanzar cuando no hay codigo', () => {
    expect(leerCodigoError(null)).toBeUndefined();
    expect(leerCodigoError('error')).toBeUndefined();
    expect(leerCodigoError({ error: null })).toBeUndefined();
    expect(leerCodigoError({ error: { campo: 'mensaje' } })).toBeUndefined();
    expect(leerCodigoError({ error: { codigoError: '' } })).toBeUndefined();
    expect(leerCodigoError({ error: { codigoError: 'abc' } })).toBeUndefined();
  });
});
