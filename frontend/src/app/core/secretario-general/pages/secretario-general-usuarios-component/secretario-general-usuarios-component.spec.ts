import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SecretarioGeneralUsuariosComponent } from './secretario-general-usuarios-component';

describe('SecretarioGeneralUsuariosComponent', () => {
  let component: SecretarioGeneralUsuariosComponent;
  let fixture: ComponentFixture<SecretarioGeneralUsuariosComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SecretarioGeneralUsuariosComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SecretarioGeneralUsuariosComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
