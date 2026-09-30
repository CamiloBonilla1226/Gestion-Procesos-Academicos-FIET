import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SideBarSecretariaFietComponent } from './side-bar-secretaria-fiet-component';

describe('SideBarSecretariaFietComponent', () => {
  let component: SideBarSecretariaFietComponent;
  let fixture: ComponentFixture<SideBarSecretariaFietComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SideBarSecretariaFietComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SideBarSecretariaFietComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
