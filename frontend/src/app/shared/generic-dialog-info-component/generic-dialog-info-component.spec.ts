import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GenericDialogInfoComponent } from './generic-dialog-info-component';

describe('GenericDialogInfoComponent', () => {
  let component: GenericDialogInfoComponent;
  let fixture: ComponentFixture<GenericDialogInfoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GenericDialogInfoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GenericDialogInfoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
