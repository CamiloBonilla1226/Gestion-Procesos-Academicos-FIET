import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SimpleButtonGroupComponent } from './simple-button-group-component';

describe('SimpleButtonGroupComponent', () => {
  let component: SimpleButtonGroupComponent;
  let fixture: ComponentFixture<SimpleButtonGroupComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SimpleButtonGroupComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SimpleButtonGroupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
