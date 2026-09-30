import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InfoUsuarioContentComponent } from './info-usuario-content-component';

describe('InfoUsuarioContentComponent', () => {
  let component: InfoUsuarioContentComponent;
  let fixture: ComponentFixture<InfoUsuarioContentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InfoUsuarioContentComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InfoUsuarioContentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
