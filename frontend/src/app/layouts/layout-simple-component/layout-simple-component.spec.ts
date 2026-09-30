import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LayoutSimpleComponent } from './layout-simple-component';

describe('LayoutSimpleComponent', () => {
  let component: LayoutSimpleComponent;
  let fixture: ComponentFixture<LayoutSimpleComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LayoutSimpleComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LayoutSimpleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
