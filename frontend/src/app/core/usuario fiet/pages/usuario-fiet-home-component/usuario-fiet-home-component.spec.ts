import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UsuarioFietHomeComponent } from './usuario-fiet-home-component';

describe('UsuarioFietHomeComponent', () => {
  let component: UsuarioFietHomeComponent;
  let fixture: ComponentFixture<UsuarioFietHomeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UsuarioFietHomeComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UsuarioFietHomeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
