import { Component } from '@angular/core';
import { ContentComponent } from '../content/content-component/content-component';
import { DocNotificacionesContentComponent } from '../content/doc-notificaciones-content-component/doc-notificaciones-content-component';

@Component({
  selector: 'app-doc-notificaciones-component',
  imports: [ContentComponent],
  templateUrl: './doc-notificaciones-component.html'
})
export class DocNotificacionesComponent {
  content = DocNotificacionesContentComponent;
}
