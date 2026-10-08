import { HttpParams } from '@angular/common/http';

export function armarParametros(valores: Record<string, string | number | null | undefined>): HttpParams {
  let parametros = new HttpParams();
  for (const [clave, valor] of Object.entries(valores)) {
    if (valor === null || valor === undefined) continue;
    const texto = String(valor).trim();
    if (texto === '') continue;
    parametros = parametros.set(clave, texto);
  }
  return parametros;
}
