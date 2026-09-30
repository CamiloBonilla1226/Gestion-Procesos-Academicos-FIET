import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SecRespuestasComponent } from './sec-respuestas-component';

describe('SecRespuestasComponent', () => {
  let component: SecRespuestasComponent;
  let fixture: ComponentFixture<SecRespuestasComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SecRespuestasComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SecRespuestasComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
