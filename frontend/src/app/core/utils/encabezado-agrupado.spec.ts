import { agruparEncabezados } from './encabezado-agrupado';

describe('agruparEncabezados', () => {
  const rutas = {
    'Faltas': ['Facultad', 'No. faltas'],
    'Nota': ['Facultad', 'Nota'],
    'Matrícula': ['Facultad', 'Situación académica', 'En la matrícula'],
    'Cancelar': ['Facultad', 'Situación académica', 'Al cancelar']
  };

  it('sin rutas deja una sola fila con los titulos', () => {
    expect(agruparEncabezados(['Código', 'Asignatura'], {})).toEqual([[
      { titulo: 'Código', colspan: 1, rowspan: 1 },
      { titulo: 'Asignatura', colspan: 1, rowspan: 1 }
    ]]);
  });

  it('agrupa por niveles con colspan y rowspan', () => {
    const filas = agruparEncabezados(['Código', 'Faltas', 'Nota', 'Matrícula', 'Cancelar'], rutas);
    expect(filas).toEqual([
      [{ titulo: 'Código', colspan: 1, rowspan: 3 }, { titulo: 'Facultad', colspan: 4, rowspan: 1 }],
      [
        { titulo: 'No. faltas', colspan: 1, rowspan: 2 },
        { titulo: 'Nota', colspan: 1, rowspan: 2 },
        { titulo: 'Situación académica', colspan: 2, rowspan: 1 }
      ],
      [{ titulo: 'En la matrícula', colspan: 1, rowspan: 1 }, { titulo: 'Al cancelar', colspan: 1, rowspan: 1 }]
    ]);
  });

  it('se adapta a las columnas visibles', () => {
    const filas = agruparEncabezados(['Código', 'Matrícula'], rutas);
    expect(filas[0]).toEqual([{ titulo: 'Código', colspan: 1, rowspan: 3 }, { titulo: 'Facultad', colspan: 1, rowspan: 1 }]);
    expect(filas[1]).toEqual([{ titulo: 'Situación académica', colspan: 1, rowspan: 1 }]);
    expect(filas[2]).toEqual([{ titulo: 'En la matrícula', colspan: 1, rowspan: 1 }]);
  });

  it('no une grupos iguales que no estan seguidos', () => {
    const filas = agruparEncabezados(['Faltas', 'Código', 'Nota'], rutas);
    expect(filas[0].map(c => [c.titulo, c.colspan])).toEqual([['Facultad', 1], ['Código', 1], ['Facultad', 1]]);
  });
});
