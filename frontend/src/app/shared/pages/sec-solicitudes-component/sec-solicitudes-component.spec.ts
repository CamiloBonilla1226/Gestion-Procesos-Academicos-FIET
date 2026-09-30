import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SecSolicitudesComponent } from './sec-solicitudes-component';

describe('SecSolicitudesComponent', () => {
  let component: SecSolicitudesComponent;
  let fixture: ComponentFixture<SecSolicitudesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SecSolicitudesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SecSolicitudesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
