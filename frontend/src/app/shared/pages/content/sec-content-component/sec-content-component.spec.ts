import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SecContentComponent } from './sec-content-component';

describe('SecContentComponent', () => {
  let component: SecContentComponent;
  let fixture: ComponentFixture<SecContentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SecContentComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SecContentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
