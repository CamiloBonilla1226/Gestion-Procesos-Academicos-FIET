import { HttpErrorResponse } from '@angular/common/http';
import {
  comoTipoAnexo,
  errorDeArchivo,
  errorDeTexto,
  erroresDeCampos,
  esFechaValida,
  esHoraValida,
  fechaDeHoy,
  extensionPermitida,
  leerFaltas,
  leerNota,
  listaFormatos
} from './validaciones-academicas';

function archivo(nombre: string, bytes: number): File {
  return new File([new Uint8Array(bytes)], nombre);
}

describe('validaciones academicas', () => {
  it('jpg y jpeg son el mismo formato', () => {
    expect(listaFormatos('pdf,jpg,png')).toEqual(['pdf', 'jpg', 'jpeg', 'png']);
    expect(listaFormatos(' PDF , jpeg ')).toEqual(['pdf', 'jpg', 'jpeg']);
    expect(extensionPermitida('foto.JPEG', 'pdf,jpg,png')).toBeTrue();
    expect(extensionPermitida('foto.jpg', 'pdf,jpeg')).toBeTrue();
    expect(extensionPermitida('carta.docx', 'pdf,jpg,jpeg,png')).toBeFalse();
    expect(extensionPermitida('sin-extension', 'pdf')).toBeFalse();
  });

  it('el adaptador entrega a InputAnexoUploadComponent los formatos ampliados', () => {
    const tipo = comoTipoAnexo('u-1', 'Carné estudiantil', 'pdf,jpg,png', true);
    expect(tipo).toEqual({ uuidTipoAnexo: 'u-1', nombre: 'Carné estudiantil', descripcion: '', formato: 'pdf,jpg,jpeg,png', obligatoriedad: true });
  });

  it('rechaza archivos vacios, de mas de 5 MB y de formato no permitido', () => {
    expect(errorDeArchivo(archivo('a.pdf', 0), 'pdf')).toContain('vacío');
    expect(errorDeArchivo(archivo('a.pdf', 5242881), 'pdf')).toContain('5 MB');
    expect(errorDeArchivo(archivo('a.pdf', 5242880), 'pdf')).toBeNull();
    expect(errorDeArchivo(archivo('a.png', 10), 'pdf')).toContain('pdf');
  });

  it('acepta un maximo menor por tipo, pero nunca mayor a 5 MB', () => {
    expect(errorDeArchivo(archivo('a.pdf', 2048), 'pdf', 1024)).toContain('1,0 KB');
    expect(errorDeArchivo(archivo('a.pdf', 5242881), 'pdf', 10485760)).toContain('5 MB');
    expect(errorDeArchivo(archivo('a.pdf', 5242880), 'pdf', 0)).toBeNull();
  });

  it('valida textos obligatorios y su largo maximo', () => {
    expect(errorDeTexto('', 255, true, 'El motivo')).toBe('El motivo es obligatorio.');
    expect(errorDeTexto('   ', 255, true, 'El motivo')).toBe('El motivo es obligatorio.');
    expect(errorDeTexto('', 500, false, 'La observación')).toBeNull();
    expect(errorDeTexto('', 500, true, 'La observación', true)).toBe('La observación es obligatoria.');
    expect(errorDeTexto('a'.repeat(255), 255, true, 'El motivo')).toBeNull();
    expect(errorDeTexto('a'.repeat(256), 255, true, 'El motivo')).toContain('255');
    expect(errorDeTexto('a'.repeat(501), 500, false, 'La observación')).toContain('500');
  });

  it('lee notas entre 0.0 y 5.0 con un decimal', () => {
    expect(leerNota('0')).toBe(0);
    expect(leerNota('5.0')).toBe(5);
    expect(leerNota('4.5')).toBe(4.5);
    expect(leerNota('3,2')).toBe(3.2);
    expect(leerNota('5.1')).toBeNull();
    expect(leerNota('4.55')).toBeNull();
    expect(leerNota('-1')).toBeNull();
    expect(leerNota('10')).toBeNull();
    expect(leerNota('')).toBeNull();
    expect(leerNota('abc')).toBeNull();
  });

  it('lee faltas como entero mayor o igual a 0', () => {
    expect(leerFaltas('0')).toBe(0);
    expect(leerFaltas('12')).toBe(12);
    expect(leerFaltas('-1')).toBeNull();
    expect(leerFaltas('1.5')).toBeNull();
    expect(leerFaltas('')).toBeNull();
  });

  it('solo toma el mapa de campos de un error 400', () => {
    const error400 = new HttpErrorResponse({ status: 400, error: { motivo: 'no puede estar vacío', otro: 3 } });
    expect(erroresDeCampos(error400)).toEqual({ motivo: 'no puede estar vacío' });
    const error500 = new HttpErrorResponse({ status: 500, error: { codigoError: '4', mensaje: 'x' } });
    expect(erroresDeCampos(error500)).toEqual({});
    expect(erroresDeCampos(null)).toEqual({});
  });

  it('valida fechas AAAA-MM-DD reales, horas HH:mm y arma la fecha de hoy', () => {
    expect(esFechaValida('2026-10-09')).toBeTrue();
    expect(esFechaValida('2026-02-30')).toBeFalse();
    expect(esFechaValida('09/10/2026')).toBeFalse();
    expect(esFechaValida('')).toBeFalse();
    expect(esHoraValida('07:30')).toBeTrue();
    expect(esHoraValida('23:59')).toBeTrue();
    expect(esHoraValida('24:00')).toBeFalse();
    expect(esHoraValida('7:30')).toBeFalse();
    expect(fechaDeHoy(new Date(2026, 0, 5))).toBe('2026-01-05');
  });
});
