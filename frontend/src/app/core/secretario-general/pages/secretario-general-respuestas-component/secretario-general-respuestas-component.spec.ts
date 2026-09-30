import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SecretarioGeneralRespuestasComponent } from './secretario-general-respuestas-component';

describe('SecretarioGeneralRespuestasComponent', () => {
  let component: SecretarioGeneralRespuestasComponent;
  let fixture: ComponentFixture<SecretarioGeneralRespuestasComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SecretarioGeneralRespuestasComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SecretarioGeneralRespuestasComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
