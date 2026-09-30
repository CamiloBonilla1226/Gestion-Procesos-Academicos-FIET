import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DialogGestionRespuestaComponent } from './dialog-gestion-respuesta-component';

describe('DialogGestionRespuestaComponent', () => {
  let component: DialogGestionRespuestaComponent;
  let fixture: ComponentFixture<DialogGestionRespuestaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DialogGestionRespuestaComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DialogGestionRespuestaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
