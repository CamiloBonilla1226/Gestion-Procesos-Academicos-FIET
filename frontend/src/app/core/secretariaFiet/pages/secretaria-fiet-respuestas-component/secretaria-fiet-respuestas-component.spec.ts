import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SecretariaFietRespuestasComponent } from './secretaria-fiet-respuestas-component';

describe('SecretariaFietRespuestasComponent', () => {
  let component: SecretariaFietRespuestasComponent;
  let fixture: ComponentFixture<SecretariaFietRespuestasComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SecretariaFietRespuestasComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SecretariaFietRespuestasComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
