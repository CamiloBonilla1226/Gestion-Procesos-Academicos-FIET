import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CargarRespuestaComponent } from './cargar-respuesta-component';

describe('CargarRespuestaComponent', () => {
  let component: CargarRespuestaComponent;
  let fixture: ComponentFixture<CargarRespuestaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CargarRespuestaComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CargarRespuestaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
