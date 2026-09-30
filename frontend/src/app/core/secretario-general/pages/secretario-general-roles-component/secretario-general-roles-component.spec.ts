import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SecretarioGeneralRolesComponent } from './secretario-general-roles-component';

describe('SecretarioGeneralRolesComponent', () => {
  let component: SecretarioGeneralRolesComponent;
  let fixture: ComponentFixture<SecretarioGeneralRolesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SecretarioGeneralRolesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SecretarioGeneralRolesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
