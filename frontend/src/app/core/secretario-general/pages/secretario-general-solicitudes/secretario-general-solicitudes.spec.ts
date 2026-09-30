import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SecretarioGeneralSolicitudes } from './secretario-general-solicitudes';

describe('SecretarioGeneralSolicitudes', () => {
  let component: SecretarioGeneralSolicitudes;
  let fixture: ComponentFixture<SecretarioGeneralSolicitudes>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SecretarioGeneralSolicitudes]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SecretarioGeneralSolicitudes);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
