import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SecretarioGeneralTipoSolicitudesComponent } from './secretario-general-tipo-solicitudes-component';

describe('SecretarioGeneralTipoSolicitudesComponent', () => {
  let component: SecretarioGeneralTipoSolicitudesComponent;
  let fixture: ComponentFixture<SecretarioGeneralTipoSolicitudesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SecretarioGeneralTipoSolicitudesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SecretarioGeneralTipoSolicitudesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
