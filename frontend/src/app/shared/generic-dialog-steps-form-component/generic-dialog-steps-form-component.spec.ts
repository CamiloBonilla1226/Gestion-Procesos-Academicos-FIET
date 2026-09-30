import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GenericDialogStepsFormComponent } from './generic-dialog-steps-form-component';

describe('GenericDialogStepsFormComponent', () => {
  let component: GenericDialogStepsFormComponent;
  let fixture: ComponentFixture<GenericDialogStepsFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GenericDialogStepsFormComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GenericDialogStepsFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
