import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UsuarioFietSolicitudesPrivadasComponent } from './usuario-fiet-solicitudes-privadas-component';

describe('UsuarioFietSolicitudesPrivadasComponent', () => {
  let component: UsuarioFietSolicitudesPrivadasComponent;
  let fixture: ComponentFixture<UsuarioFietSolicitudesPrivadasComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UsuarioFietSolicitudesPrivadasComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UsuarioFietSolicitudesPrivadasComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
