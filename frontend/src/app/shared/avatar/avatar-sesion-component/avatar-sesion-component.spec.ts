import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AvatarSesionComponent } from './avatar-sesion-component';

describe('AvatarSesionComponent', () => {
  let component: AvatarSesionComponent;
  let fixture: ComponentFixture<AvatarSesionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AvatarSesionComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AvatarSesionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
