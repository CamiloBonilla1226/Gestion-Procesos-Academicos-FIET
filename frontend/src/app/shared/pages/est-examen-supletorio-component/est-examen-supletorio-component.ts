import { Component } from '@angular/core';
import { ContentComponent } from '../content/content-component/content-component';
import { EstExamenSupletorioContentComponent } from '../content/est-examen-supletorio-content-component/est-examen-supletorio-content-component';

@Component({
  selector: 'app-est-examen-supletorio-component',
  imports: [ContentComponent],
  templateUrl: './est-examen-supletorio-component.html'
})
export class EstExamenSupletorioComponent {
  content = EstExamenSupletorioContentComponent;
}
