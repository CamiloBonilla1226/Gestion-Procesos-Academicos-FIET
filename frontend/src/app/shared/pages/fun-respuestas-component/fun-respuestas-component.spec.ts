import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FunRespuestasComponent } from './fun-respuestas-component';

describe('FunRespuestasComponent', () => {
  let component: FunRespuestasComponent;
  let fixture: ComponentFixture<FunRespuestasComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FunRespuestasComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FunRespuestasComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
