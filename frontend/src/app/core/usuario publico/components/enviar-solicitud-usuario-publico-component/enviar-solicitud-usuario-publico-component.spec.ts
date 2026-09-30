import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EnviarSolicitudUsuarioPublicoComponent } from './enviar-solicitud-usuario-publico-component';

describe('EnviarSolicitudUsuarioPublicoComponent', () => {
  let component: EnviarSolicitudUsuarioPublicoComponent;
  let fixture: ComponentFixture<EnviarSolicitudUsuarioPublicoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EnviarSolicitudUsuarioPublicoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EnviarSolicitudUsuarioPublicoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
