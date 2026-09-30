import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EnviarSolicitudUsuarioFietComponent } from './enviar-solicitud-usuario-fiet-component';

describe('EnviarSolicitudUsuarioFietComponent', () => {
  let component: EnviarSolicitudUsuarioFietComponent;
  let fixture: ComponentFixture<EnviarSolicitudUsuarioFietComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EnviarSolicitudUsuarioFietComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EnviarSolicitudUsuarioFietComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
