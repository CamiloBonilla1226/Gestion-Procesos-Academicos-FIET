import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SideBarUsuarioFietComponent } from './side-bar-usuario-fiet-component';

describe('SideBarUsuarioFietComponent', () => {
  let component: SideBarUsuarioFietComponent;
  let fixture: ComponentFixture<SideBarUsuarioFietComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SideBarUsuarioFietComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SideBarUsuarioFietComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
