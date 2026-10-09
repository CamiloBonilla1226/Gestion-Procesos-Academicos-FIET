import { Component } from '@angular/core';
import { ContentComponent } from '../content/content-component/content-component';
import { EstCancelacionAsignaturaContentComponent } from '../content/est-cancelacion-asignatura-content-component/est-cancelacion-asignatura-content-component';

@Component({
  selector: 'app-est-cancelacion-asignatura-component',
  imports: [ContentComponent],
  templateUrl: './est-cancelacion-asignatura-component.html'
})
export class EstCancelacionAsignaturaComponent {
  content = EstCancelacionAsignaturaContentComponent;
}
