import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InputTextTareaComponent } from './input-text-tarea-component';

describe('InputTextTareaComponent', () => {
  let component: InputTextTareaComponent;
  let fixture: ComponentFixture<InputTextTareaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InputTextTareaComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InputTextTareaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
