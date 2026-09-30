import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SecRespuestasContentComponent } from './sec-respuestas-content-component';

describe('SecRespuestasContentComponent', () => {
  let component: SecRespuestasContentComponent;
  let fixture: ComponentFixture<SecRespuestasContentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SecRespuestasContentComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SecRespuestasContentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
