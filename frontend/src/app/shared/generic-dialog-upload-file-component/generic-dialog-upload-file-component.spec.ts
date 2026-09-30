import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GenericDialogUploadFileComponent } from './generic-dialog-upload-file-component';

describe('GenericDialogUploadFileComponent', () => {
  let component: GenericDialogUploadFileComponent;
  let fixture: ComponentFixture<GenericDialogUploadFileComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GenericDialogUploadFileComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GenericDialogUploadFileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
