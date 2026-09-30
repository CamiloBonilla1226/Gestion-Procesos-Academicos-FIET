import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SecretarioGeneralLogComponent } from './secretario-general-log-component';

describe('SecretarioGeneralLogComponent', () => {
  let component: SecretarioGeneralLogComponent;
  let fixture: ComponentFixture<SecretarioGeneralLogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SecretarioGeneralLogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SecretarioGeneralLogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
