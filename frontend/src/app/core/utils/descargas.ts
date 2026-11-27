import { HttpErrorResponse } from '@angular/common/http';
import { MonoTypeOperatorFunction, catchError, from, switchMap, throwError } from 'rxjs';

export function guardarArchivo(blob: Blob, nombre: string): void {
  if (typeof window === 'undefined' || typeof document === 'undefined') return;
  const url = window.URL.createObjectURL(blob);
  const enlace = document.createElement('a');
  enlace.href = url;
  enlace.download = nombre;
  enlace.click();
  setTimeout(() => window.URL.revokeObjectURL(url), 1000);
}

export function abrirVentana(): Window | null {
  if (typeof window === 'undefined') return null;
  return window.open('', '_blank');
}

export function mostrarArchivo(ventana: Window | null, blob: Blob, nombre: string): void {
  if (!ventana || ventana.closed) {
    guardarArchivo(blob, nombre);
    return;
  }
  const url = window.URL.createObjectURL(new Blob([blob], { type: 'application/pdf' }));
  ventana.location.href = url;
  setTimeout(() => window.URL.revokeObjectURL(url), 60000);
}

export function parsearErrorBlob<T>(): MonoTypeOperatorFunction<T> {
  return catchError((err: unknown) => {
    if (!(err instanceof HttpErrorResponse) || !(err.error instanceof Blob)) {
      return throwError(() => err);
    }
    return from(err.error.text()).pipe(
      catchError(() => throwError(() => err)),
      switchMap(texto => throwError(() => new HttpErrorResponse({
        error: convertirCuerpo(texto),
        headers: err.headers,
        status: err.status,
        statusText: err.statusText,
        url: err.url ?? undefined
      })))
    );
  });
}

function convertirCuerpo(texto: string): unknown {
  if (!texto) return null;
  try {
    return JSON.parse(texto);
  } catch {
    return texto;
  }
}
