import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DescargarRespuestaComponent } from './descargar-respuesta-component';

describe('DescargarRespuestaComponent', () => {
  let component: DescargarRespuestaComponent;
  let fixture: ComponentFixture<DescargarRespuestaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DescargarRespuestaComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DescargarRespuestaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
