import { TestBed } from '@angular/core/testing';
import { Paginator } from './paginator';

describe('Paginator textos', () => {
  it('muestra Anterior, las paginas y Siguiente con la actual marcada', () => {
    const fixture = TestBed.createComponent(Paginator);
    fixture.componentRef.setInput('totalPages', 3);
    fixture.componentRef.setInput('currentPage', 1);
    fixture.detectChanges();
    const botones = Array.from(fixture.nativeElement.querySelectorAll('.page-link')) as HTMLButtonElement[];
    expect(botones.map(b => b.textContent?.trim())).toEqual(['‹ Anterior', '1', '2', '3', 'Siguiente ›']);
    expect(botones[0].disabled).toBeTrue();
    expect(botones[1].getAttribute('aria-current')).toBe('page');

    const paginas: number[] = [];
    fixture.componentInstance.pageChange.subscribe(p => paginas.push(p));
    botones[4].click();
    expect(paginas).toEqual([2]);
  });
});
