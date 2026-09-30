import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OrdenDelDiaContentComponent } from './orden-del-dia-content-component';

describe('OrdenDelDiaContentComponent', () => {
  let component: OrdenDelDiaContentComponent;
  let fixture: ComponentFixture<OrdenDelDiaContentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OrdenDelDiaContentComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OrdenDelDiaContentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
