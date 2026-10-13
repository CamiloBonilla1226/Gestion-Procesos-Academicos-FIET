import { Component, TemplateRef, ViewChild } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { TableGenericComponent, TableHeader } from './table-generic-component';

@Component({
  imports: [TableGenericComponent],
  template: `
    <app-table-generic-component [headers]="headers" [data]="data"></app-table-generic-component>
    <ng-template #negrita let-fila let-valor="valor"><b class="celda">{{ valor }}-{{ fila.id }}</b></ng-template>
  `
})
class AnfitrionComponent {
  @ViewChild('negrita', { static: true }) negrita!: TemplateRef<unknown>;
  headers: TableHeader[] = [];
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
});
