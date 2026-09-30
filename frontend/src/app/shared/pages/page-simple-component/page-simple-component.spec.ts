import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PageSimpleComponent } from './page-simple-component';

describe('PageSimpleComponent', () => {
  let component: PageSimpleComponent;
  let fixture: ComponentFixture<PageSimpleComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PageSimpleComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PageSimpleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
