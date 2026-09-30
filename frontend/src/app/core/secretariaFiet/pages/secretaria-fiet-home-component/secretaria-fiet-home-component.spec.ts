import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SecretariaFietHomeComponent } from './secretaria-fiet-home-component';

describe('SecretariaFietHomeComponent', () => {
  let component: SecretariaFietHomeComponent;
  let fixture: ComponentFixture<SecretariaFietHomeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SecretariaFietHomeComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SecretariaFietHomeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
