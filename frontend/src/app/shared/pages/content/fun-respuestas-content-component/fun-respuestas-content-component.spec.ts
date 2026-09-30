import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FunRespuestasContentComponent } from './fun-respuestas-content-component';

describe('FunRespuestasContentComponent', () => {
  let component: FunRespuestasContentComponent;
  let fixture: ComponentFixture<FunRespuestasContentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FunRespuestasContentComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FunRespuestasContentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
