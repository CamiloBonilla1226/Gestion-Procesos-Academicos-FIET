import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FunSolicitudesContentComponent } from './fun-solicitudes-content-component';

describe('FunSolicitudesContentComponent', () => {
  let component: FunSolicitudesContentComponent;
  let fixture: ComponentFixture<FunSolicitudesContentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FunSolicitudesContentComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FunSolicitudesContentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
