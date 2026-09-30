import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SideBarSecretarioGeneralComponent } from './side-bar-secretario-general-component';

describe('SideBarSecretarioGeneralComponent', () => {
  let component: SideBarSecretarioGeneralComponent;
  let fixture: ComponentFixture<SideBarSecretarioGeneralComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SideBarSecretarioGeneralComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SideBarSecretarioGeneralComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
