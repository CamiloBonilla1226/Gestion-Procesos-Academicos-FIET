import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AnexosViewComponent } from './anexos-view-component';

describe('AnexosViewComponent', () => {
  let component: AnexosViewComponent;
  let fixture: ComponentFixture<AnexosViewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AnexosViewComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AnexosViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
