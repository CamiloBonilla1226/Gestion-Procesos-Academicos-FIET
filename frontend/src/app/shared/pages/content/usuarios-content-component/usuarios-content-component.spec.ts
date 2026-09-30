import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UsuariosContentComponent } from './usuarios-content-component';

describe('UsuariosContentComponent', () => {
  let component: UsuariosContentComponent;
  let fixture: ComponentFixture<UsuariosContentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UsuariosContentComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UsuariosContentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
