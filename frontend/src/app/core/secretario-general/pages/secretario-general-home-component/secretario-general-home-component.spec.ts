import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SecretarioGeneralHomeComponent } from './secretario-general-home-component';

describe('SecretarioGeneralHomeComponent', () => {
  let component: SecretarioGeneralHomeComponent;
  let fixture: ComponentFixture<SecretarioGeneralHomeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SecretarioGeneralHomeComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SecretarioGeneralHomeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
