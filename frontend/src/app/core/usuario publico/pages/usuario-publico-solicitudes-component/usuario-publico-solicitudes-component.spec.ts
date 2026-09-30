import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UsuarioPublicoSolicitudesComponent } from './usuario-publico-solicitudes-component';

describe('UsuarioPublicoSolicitudesComponent', () => {
  let component: UsuarioPublicoSolicitudesComponent;
  let fixture: ComponentFixture<UsuarioPublicoSolicitudesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UsuarioPublicoSolicitudesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UsuarioPublicoSolicitudesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
