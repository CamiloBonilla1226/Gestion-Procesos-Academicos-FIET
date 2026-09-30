import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FuncionarioRespuestasComponent } from './funcionario-respuestas-component';

describe('FuncionarioRespuestasComponent', () => {
  let component: FuncionarioRespuestasComponent;
  let fixture: ComponentFixture<FuncionarioRespuestasComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FuncionarioRespuestasComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FuncionarioRespuestasComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
