import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GenericDialogFormComponent } from './generic-dialog-form-component';

describe('GenericDialogFormComponent', () => {
  let component: GenericDialogFormComponent;
  let fixture: ComponentFixture<GenericDialogFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GenericDialogFormComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GenericDialogFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
