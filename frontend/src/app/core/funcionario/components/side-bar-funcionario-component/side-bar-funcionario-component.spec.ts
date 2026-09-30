import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SideBarFuncionarioComponent } from './side-bar-funcionario-component';

describe('SideBarFuncionarioComponent', () => {
  let component: SideBarFuncionarioComponent;
  let fixture: ComponentFixture<SideBarFuncionarioComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SideBarFuncionarioComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SideBarFuncionarioComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
