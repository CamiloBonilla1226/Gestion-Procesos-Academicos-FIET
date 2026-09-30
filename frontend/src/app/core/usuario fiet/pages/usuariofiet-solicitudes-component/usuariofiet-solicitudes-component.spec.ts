import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UsuariofietSolicitudesComponent } from './usuariofiet-solicitudes-component';

describe('UsuariofietSolicitudesComponent', () => {
  let component: UsuariofietSolicitudesComponent;
  let fixture: ComponentFixture<UsuariofietSolicitudesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UsuariofietSolicitudesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UsuariofietSolicitudesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
