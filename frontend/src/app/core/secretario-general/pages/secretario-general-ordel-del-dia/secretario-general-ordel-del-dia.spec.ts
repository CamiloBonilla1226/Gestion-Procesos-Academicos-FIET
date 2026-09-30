import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SecretarioGeneralOrdelDelDia } from './secretario-general-ordel-del-dia';

describe('SecretarioGeneralOrdelDelDia', () => {
  let component: SecretarioGeneralOrdelDelDia;
  let fixture: ComponentFixture<SecretarioGeneralOrdelDelDia>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SecretarioGeneralOrdelDelDia]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SecretarioGeneralOrdelDelDia);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
