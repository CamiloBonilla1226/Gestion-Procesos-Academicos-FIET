import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InputAnexoUploadComponent } from './input-anexo-upload-component';

describe('InputAnexoUploadComponent', () => {
  let component: InputAnexoUploadComponent;
  let fixture: ComponentFixture<InputAnexoUploadComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InputAnexoUploadComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InputAnexoUploadComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
