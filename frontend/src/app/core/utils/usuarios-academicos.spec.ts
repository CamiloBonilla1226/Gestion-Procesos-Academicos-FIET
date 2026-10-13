import { CAMPOS_PERSONALES, cambiosDeTexto, datosUsuarioDe, validarCambiosPersonales } from './usuarios-academicos';

describe('usuarios academicos', () => {
  const actual = {
    nombres: 'Ana', apellidos: 'Ruiz', tipoDocumento: 'Cédula de ciudadanía', numeroDocumento: '1061',
    telefono: '3001', correoElectronico: 'ana@u.co', username: 'aruiz'
  };

  it('arma los datos de edicion desde el usuario sin contraseña', () => {
    const datos = datosUsuarioDe(actual);
    expect(datos.nombres).toBe('Ana');
    expect(datos.username).toBe('aruiz');
    expect(datos.password).toBe('');
  });

  it('solo toma los campos que cambiaron y un campo vacio no cambia', () => {
    const editado = { ...datosUsuarioDe(actual), nombres: ' Ana ', apellidos: '', telefono: ' 3115550000 ' };
    expect(cambiosDeTexto(CAMPOS_PERSONALES, actual, editado)).toEqual({ telefono: '3115550000' });
  });

  it('valida solo los campos cambiados con los limites del backend', () => {
    expect(validarCambiosPersonales({})).toEqual({});
    const errores = validarCambiosPersonales({ numeroDocumento: '12', telefono: '12', correoElectronico: 'sin-arroba', nombres: 'n'.repeat(1001) });
    expect(errores['numeroDocumento']).toContain('entre 5 y 255');
    expect(errores['telefono']).toContain('entre 5 y 1000');
    expect(errores['correoElectronico']).toContain('formato');
    expect(errores['nombres']).toContain('1000');
    expect(errores['apellidos']).toBeUndefined();
  });
});
