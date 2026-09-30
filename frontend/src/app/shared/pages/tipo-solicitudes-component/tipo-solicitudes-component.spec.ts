import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TipoSolicitudesComponent } from './tipo-solicitudes-component';

describe('TipoSolicitudesComponent', () => {
  let component: TipoSolicitudesComponent;
  let fixture: ComponentFixture<TipoSolicitudesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TipoSolicitudesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TipoSolicitudesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
