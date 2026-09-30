import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SecFietRespuestasContentComponent } from './sec-fiet-respuestas-content-component';

describe('SecFietRespuestasContentComponent', () => {
  let component: SecFietRespuestasContentComponent;
  let fixture: ComponentFixture<SecFietRespuestasContentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SecFietRespuestasContentComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SecFietRespuestasContentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
