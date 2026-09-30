import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TipoSolicitudesContentComponent } from './tipo-solicitudes-content-component';

describe('TipoSolicitudesContentComponent', () => {
  let component: TipoSolicitudesContentComponent;
  let fixture: ComponentFixture<TipoSolicitudesContentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TipoSolicitudesContentComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TipoSolicitudesContentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
