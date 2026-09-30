import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UsuarioFietSolicitudesPrivadasContentComponent } from './usuario-fiet-solicitudes-privadas-content-component';

describe('UsuarioFietSolicitudesPrivadasContentComponent', () => {
  let component: UsuarioFietSolicitudesPrivadasContentComponent;
  let fixture: ComponentFixture<UsuarioFietSolicitudesPrivadasContentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UsuarioFietSolicitudesPrivadasContentComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UsuarioFietSolicitudesPrivadasContentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
