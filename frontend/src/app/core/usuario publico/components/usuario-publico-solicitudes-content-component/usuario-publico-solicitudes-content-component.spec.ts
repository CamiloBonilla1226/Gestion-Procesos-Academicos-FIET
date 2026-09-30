import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UsuarioPublicoSolicitudesContentComponent } from './usuario-publico-solicitudes-content-component';

describe('UsuarioPublicoSolicitudesContentComponent', () => {
  let component: UsuarioPublicoSolicitudesContentComponent;
  let fixture: ComponentFixture<UsuarioPublicoSolicitudesContentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UsuarioPublicoSolicitudesContentComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UsuarioPublicoSolicitudesContentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
