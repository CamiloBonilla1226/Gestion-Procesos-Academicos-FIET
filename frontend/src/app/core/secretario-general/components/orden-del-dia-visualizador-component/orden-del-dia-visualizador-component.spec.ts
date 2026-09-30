import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OrdenDelDiaVisualizadorComponent } from './orden-del-dia-visualizador-component';

describe('OrdenDelDiaVisualizadorComponent', () => {
  let component: OrdenDelDiaVisualizadorComponent;
  let fixture: ComponentFixture<OrdenDelDiaVisualizadorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OrdenDelDiaVisualizadorComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OrdenDelDiaVisualizadorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
