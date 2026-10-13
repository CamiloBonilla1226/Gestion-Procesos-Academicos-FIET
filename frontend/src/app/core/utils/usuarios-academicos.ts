import { HttpErrorResponse } from '@angular/common/http';
import { errorDeTexto } from './validaciones-academicas';

export interface DatosUsuarioAcademico {
  nombres: string;
  apellidos: string;
  tipoDocumento: string | null;
  numeroDocumento: string;
  telefono: string;
  correoElectronico: string;
  username: string;
  password: string;
}

export function datosUsuarioVacios(): DatosUsuarioAcademico {
  return {
    nombres: '',
    apellidos: '',
    tipoDocumento: null,
    numeroDocumento: '',
    telefono: '',
    correoElectronico: '',
    username: '',
    password: ''
  };
}

export function errorDeLongitud(
  valor: string | null | undefined,
  minimo: number,
  maximo: number,
  campo: string,
  femenino = false
): string | null {
  const texto = (valor ?? '').trim();
  const obligatorio = errorDeTexto(texto, maximo, true, campo, femenino);
  if (obligatorio) return obligatorio;
  if (texto.length < minimo) return `${campo} debe tener entre ${minimo} y ${maximo} caracteres.`;
  return null;
}

export function esCorreoValido(valor: string | null | undefined): boolean {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test((valor ?? '').trim());
}

export function validarDatosUsuario(datos: DatosUsuarioAcademico): Record<string, string> {
  const errores: Record<string, string> = {};
  const agregar = (campo: string, error: string | null) => {
    if (error) errores[campo] = error;
  };
  agregar('nombres', errorDeTexto(datos.nombres, 1000, true, 'Los nombres'));
  agregar('apellidos', errorDeTexto(datos.apellidos, 1000, true, 'Los apellidos'));
  if (!datos.tipoDocumento) errores['tipoDocumento'] = 'Elige el tipo de documento.';
  agregar('numeroDocumento', errorDeLongitud(datos.numeroDocumento, 5, 255, 'El número de documento'));
  agregar('telefono', errorDeLongitud(datos.telefono, 5, 1000, 'El teléfono'));
  agregar('correoElectronico', errorDeLongitud(datos.correoElectronico, 5, 1000, 'El correo electrónico'));
  if (!errores['correoElectronico'] && !esCorreoValido(datos.correoElectronico)) {
    errores['correoElectronico'] = 'El correo electrónico no tiene un formato válido.';
  }
  agregar('username', errorDeLongitud(datos.username, 5, 255, 'El nombre de usuario'));
  agregar('password', errorDeLongitud(datos.password, 5, 255, 'La contraseña', true));
  return errores;
}

export function datosUsuarioRecortados(datos: DatosUsuarioAcademico): Omit<DatosUsuarioAcademico, 'tipoDocumento'> & { tipoDocumento: string } {
  return {
    nombres: datos.nombres.trim(),
    apellidos: datos.apellidos.trim(),
    tipoDocumento: datos.tipoDocumento ?? '',
    numeroDocumento: datos.numeroDocumento.trim(),
    telefono: datos.telefono.trim(),
    correoElectronico: datos.correoElectronico.trim(),
    username: datos.username.trim(),
    password: datos.password
  };
}

export type CamposPersonales = 'nombres' | 'apellidos' | 'tipoDocumento' | 'numeroDocumento' | 'telefono' | 'correoElectronico';

export const CAMPOS_PERSONALES: CamposPersonales[] = ['nombres', 'apellidos', 'tipoDocumento', 'numeroDocumento', 'telefono', 'correoElectronico'];

export function datosUsuarioDe(usuario: Partial<Record<CamposPersonales | 'username', string | null>>): DatosUsuarioAcademico {
  return {
    nombres: usuario.nombres ?? '',
    apellidos: usuario.apellidos ?? '',
    tipoDocumento: usuario.tipoDocumento || null,
    numeroDocumento: usuario.numeroDocumento ?? '',
    telefono: usuario.telefono ?? '',
    correoElectronico: usuario.correoElectronico ?? '',
    username: usuario.username ?? '',
    password: ''
  };
}

export function cambiosDeTexto<C extends string>(
  campos: readonly C[],
  original: Partial<Record<C, string | null>>,
  editado: Partial<Record<C, string | null>>
): Partial<Record<C, string>> {
  const cambios: Partial<Record<C, string>> = {};
  for (const campo of campos) {
    const valor = (editado[campo] ?? '').trim();
    if (valor !== '' && valor !== (original[campo] ?? '').trim()) cambios[campo] = valor;
  }
  return cambios;
}

export function validarCambiosPersonales(cambios: Partial<Record<CamposPersonales, string>>): Record<string, string> {
  const errores: Record<string, string> = {};
  const agregar = (campo: CamposPersonales, error: string | null) => {
    if (campo in cambios && error) errores[campo] = error;
  };
  agregar('nombres', errorDeTexto(cambios.nombres, 1000, false, 'Los nombres'));
  agregar('apellidos', errorDeTexto(cambios.apellidos, 1000, false, 'Los apellidos'));
  agregar('tipoDocumento', errorDeLongitud(cambios.tipoDocumento, 5, 1000, 'El tipo de documento'));
  agregar('numeroDocumento', errorDeLongitud(cambios.numeroDocumento, 5, 255, 'El número de documento'));
  agregar('telefono', errorDeLongitud(cambios.telefono, 5, 1000, 'El teléfono'));
  agregar('correoElectronico', errorDeLongitud(cambios.correoElectronico, 5, 1000, 'El correo electrónico'));
  if ('correoElectronico' in cambios && !errores['correoElectronico'] && !esCorreoValido(cambios.correoElectronico)) {
    errores['correoElectronico'] = 'El correo electrónico no tiene un formato válido.';
  }
  return errores;
}

export function mensajesDeError(err: unknown): string[] {
  if (!(err instanceof HttpErrorResponse)) return ['Ocurrió un error inesperado.'];
  const cuerpo = err.error;
  if (cuerpo && typeof cuerpo === 'object') {
    if (typeof cuerpo.mensaje === 'string') return [cuerpo.mensaje];
    const valores = Object.values(cuerpo).filter((valor): valor is string => typeof valor === 'string');
    if (err.status === 400 && valores.length > 0) return valores;
  }
  return [err.message || 'Ocurrió un error inesperado.'];
}

export function totalDePaginas(totalElementos: number, tamanio: number): number {
  return Math.max(1, Math.ceil((totalElementos || 0) / tamanio));
}
