import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SecFietRespuestasComponent } from './sec-fiet-respuestas-component';

describe('SecFietRespuestasComponent', () => {
  let component: SecFietRespuestasComponent;
  let fixture: ComponentFixture<SecFietRespuestasComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SecFietRespuestasComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SecFietRespuestasComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
