import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OrdenDelDiaComponent } from './orden-del-dia-component';

describe('OrdenDelDiaComponent', () => {
  let component: OrdenDelDiaComponent;
  let fixture: ComponentFixture<OrdenDelDiaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OrdenDelDiaComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OrdenDelDiaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
