import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FuncionarioSolicitudes } from './funcionario-solicitudes';

describe('FuncionarioSolicitudes', () => {
  let component: FuncionarioSolicitudes;
  let fixture: ComponentFixture<FuncionarioSolicitudes>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FuncionarioSolicitudes]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FuncionarioSolicitudes);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
