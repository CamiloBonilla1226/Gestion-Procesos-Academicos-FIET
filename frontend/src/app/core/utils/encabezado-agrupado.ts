export interface CeldaEncabezado {
  titulo: string;
  colspan: number;
  rowspan: number;
}

function mismoPrefijo(a: string[], b: string[], largo: number): boolean {
  for (let i = 0; i < largo; i++) if (a[i] !== b[i]) return false;
  return true;
}

export function agruparEncabezados(titulos: string[], rutas: Record<string, string[]>): CeldaEncabezado[][] {
  const caminos = titulos.map(titulo => rutas[titulo] ?? [titulo]);
  const profundidad = Math.max(1, ...caminos.map(camino => camino.length));
  const filas: CeldaEncabezado[][] = [];
  for (let nivel = 0; nivel < profundidad; nivel++) {
    const fila: CeldaEncabezado[] = [];
    let i = 0;
    while (i < caminos.length) {
      const camino = caminos[i];
      if (nivel >= camino.length) {
        i++;
        continue;
      }
      if (nivel === camino.length - 1) {
        fila.push({ titulo: camino[nivel], colspan: 1, rowspan: profundidad - nivel });
        i++;
        continue;
      }
      let j = i + 1;
      while (j < caminos.length && caminos[j].length - 1 > nivel && mismoPrefijo(caminos[j], camino, nivel + 1)) j++;
      fila.push({ titulo: camino[nivel], colspan: j - i, rowspan: 1 });
      i = j;
    }
    filas.push(fila);
  }
  return filas;
}
