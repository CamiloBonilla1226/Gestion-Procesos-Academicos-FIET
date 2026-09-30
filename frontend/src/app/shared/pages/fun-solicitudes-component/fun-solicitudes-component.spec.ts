import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FunSolicitudesComponent } from './fun-solicitudes-component';

describe('FunSolicitudesComponent', () => {
  let component: FunSolicitudesComponent;
  let fixture: ComponentFixture<FunSolicitudesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FunSolicitudesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FunSolicitudesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
