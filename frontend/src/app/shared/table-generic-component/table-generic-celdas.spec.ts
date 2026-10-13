import { Component, TemplateRef, ViewChild } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { TableGenericComponent, TableHeader } from './table-generic-component';
import { CeldaEncabezado } from '../../core/utils/encabezado-agrupado';

@Component({
  imports: [TableGenericComponent],
  template: `
    <app-table-generic-component [headers]="headers" [data]="data" [encabezadoAgrupado]="agrupado"></app-table-generic-component>
    <ng-template #negrita let-fila let-valor="valor"><b class="celda">{{ valor }}-{{ fila.id }}</b></ng-template>
  `
})
class AnfitrionComponent {
  @ViewChild('negrita', { static: true }) negrita!: TemplateRef<unknown>;
  headers: TableHeader[] = [];
  agrupado: CeldaEncabezado[][] | null = null;
  data = [{ id: '1', Nombre: 'Ana', Estado: 'Pendiente' }];
}

describe('TableGenericComponent cellTemplate', () => {
  it('sin cellTemplate pinta el texto como siempre', () => {
    const fixture = TestBed.createComponent(AnfitrionComponent);
    fixture.componentInstance.headers = [{ title: 'Nombre' }, { title: 'Estado' }];
    fixture.detectChanges();
    const celdas = Array.from(fixture.nativeElement.querySelectorAll('tbody td')).map((td: any) => td.textContent.trim());
    expect(celdas).toEqual(['Ana', 'Pendiente']);
    expect(fixture.nativeElement.querySelector('.celda')).toBeNull();
  });

  it('con cellTemplate usa la plantilla con la fila y el valor de la columna', () => {
    const fixture = TestBed.createComponent(AnfitrionComponent);
    fixture.componentInstance.headers = [{ title: 'Nombre' }, { title: 'Estado', cellTemplate: fixture.componentInstance.negrita }];
    fixture.detectChanges();
    const celdas = fixture.nativeElement.querySelectorAll('tbody td');
    expect(celdas[0].textContent.trim()).toBe('Ana');
    expect(celdas[1].querySelector('.celda').textContent.trim()).toBe('Pendiente-1');
  });

  it('con encabezadoAgrupado pinta las filas agrupadas y deja el cuerpo igual', () => {
    const fixture = TestBed.createComponent(AnfitrionComponent);
    fixture.componentInstance.headers = [{ title: 'Nombre' }, { title: 'Estado' }];
    fixture.componentInstance.agrupado = [
      [{ titulo: 'Nombre', colspan: 1, rowspan: 2 }, { titulo: 'Grupo', colspan: 1, rowspan: 1 }],
      [{ titulo: 'Estado', colspan: 1, rowspan: 1 }]
    ];
    fixture.detectChanges();
    const filas = Array.from(fixture.nativeElement.querySelectorAll('thead tr')).map((tr: any) =>
      Array.from(tr.querySelectorAll('th')).map((th: any) => th.textContent.trim()));
    expect(filas).toEqual([['Nombre', 'Grupo'], ['Estado']]);
    expect(fixture.nativeElement.querySelector('thead th').getAttribute('rowspan')).toBe('2');
    expect(Array.from(fixture.nativeElement.querySelectorAll('tbody td')).map((td: any) => td.textContent.trim())).toEqual(['Ana', 'Pendiente']);
  });
});
