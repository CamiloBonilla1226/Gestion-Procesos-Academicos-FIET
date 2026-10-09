import { Component } from '@angular/core';
import { ContentComponent } from '../content/content-component/content-component';
import { EstCancelacionMatriculaContentComponent } from '../content/est-cancelacion-matricula-content-component/est-cancelacion-matricula-content-component';

@Component({
  selector: 'app-est-cancelacion-matricula-component',
  imports: [ContentComponent],
  templateUrl: './est-cancelacion-matricula-component.html'
})
export class EstCancelacionMatriculaComponent {
  content = EstCancelacionMatriculaContentComponent;
}
